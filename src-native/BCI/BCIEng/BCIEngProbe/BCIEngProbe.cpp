 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngProbe.cpp,v 1.1.2.5 2007-10-18 12:52:54 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// BCIEngProbe.cpp
//------------------------------------------------------------------------------
// BCI Engine for the probe kit
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#ifdef WIN32
#pragma warning(disable:4786)
#endif

#include "CommonDef.h"
#include "ModuleJ.h"				// Instrumentation module for Java
#include "JVMInsSet.h"				// JVM Instruction set
#include "ExtRefJ_StatMethod.h"		// Java external ref. implemented as static
#include "JMemStream.h"				// Java memory stream
#include "BCIEngProbe.h"			// BCI Engine for Probe Kit
#include "BCIEngInterface.h"		// for callback message number defines

#include <stdio.h> // for sprintf on Linux
#ifdef MVS
#    include <unistd.h> /* for __etoa */
#endif /* MVS */

#ifdef HPUX
#	include <iostream.h>
#	include <fstream.h>
#else
#	include <iostream>
#	include <fstream>
#endif

#if defined(MVS) || defined(__OS400__)
#	include <strstream>
#endif

//==============================================================================
// Class file helpers.
// 
// TODO: Now that we have a ClassBuilder class, it should really
// incorporate some of this functionality.
//

// emit_ldc_for_string: helper function used by PushArguments
// Used largely for class names, method names, and signatures.
// Also used for wholly new strings like method and line tables.
static void emit_ldc_for_string(CProbeInsertionContext& ctxt,
								CCPUtf8Info& utf8Name)
{
	unsigned const_pool_temp = ctxt.pConstPool->Find(&utf8Name);
	if (const_pool_temp == 0) {
		// The string isn't already in the constant pool - so add it.
		// We called Find first to save the heap churn of 
		// creating one, only to have CConstPool::Add delete it
		// in the (common) case where it's already there.
		const_pool_temp = ctxt.pConstPool->Add(new CCPUtf8Info(utf8Name));
	}
	CCPStringInfo* pccpstrName =  new CCPStringInfo(const_pool_temp);
	const_pool_temp = ctxt.pConstPool->Add(pccpstrName);
	ctxt.Insert(CInsSetJ::Create_ldc(const_pool_temp));
}

// Get the constant pool index of a ClassRef to the given classname.
// Create one if necessary.
static unsigned get_class_ref(CProbeInsertionContext& ctxt, const char* classname)
{
	unsigned class_utf8num = ctxt.pConstPool->Add(new CCPUtf8Info(classname));
	return ctxt.pConstPool->Add(new CCPClassInfo(class_utf8num));
}

// Get the constant pool index of a NameAndTypeInfo to the given name and type.
// Create one if necessary.
static unsigned get_name_and_type_ref(CProbeInsertionContext& ctxt, 
									  const char* methodName, const char* signature)
{
	unsigned method_ref = ctxt.pConstPool->Add(new CCPUtf8Info(methodName));
	unsigned type_ref = ctxt.pConstPool->Add(new CCPUtf8Info(signature));
	return ctxt.pConstPool->Add(new CCPNameAndTypeInfo(method_ref, type_ref));
}

// Get the constant pool index of a MethodRef to the given class, method, and signature.
// Create one if necessary.
static unsigned get_method_ref(CProbeInsertionContext& ctxt, 
							   unsigned classRef, 
							   const char* methodName, 
							   const char* signature)
{
	unsigned nameAndTypeRef = get_name_and_type_ref(ctxt, methodName, signature);
	return ctxt.pConstPool->Add(new CCPMethodrefInfo(classRef, nameAndTypeRef));
}

// Get the constant pool index of a FieldREf to the given field name and signature.
// Create one if necessary.
static unsigned get_field_ref(CProbeInsertionContext& ctxt,
							  unsigned classRef,
							  const char* fieldName,
							  const char* signature)
{
	unsigned nameAndTypeRef = get_name_and_type_ref(ctxt, fieldName, signature);
	return ctxt.pConstPool->Add(new CCPFieldrefInfo(classRef, nameAndTypeRef));
}

// Strings for the argument names
// These have to be in the same order as the ARG_BITS enum 

static const char *argNames[] = {
	"returnedObject",
	"exceptionObject",
	"className",
	"methodName",
	"methodSig",
	"thisObject",
	"args",
	"isFinally",
	"staticField",
	"classSourceFile",
	"methodNames",
	"methodLineTables",
	"methodNumber",
	"executableUnitNumber",
	NULL
};

//------------------------------------------------------------------------------
// Helper which synthesizes a new, randomly named static field 
// in the indicated module.
//
static string
synthesize_static_field_name()
{
	// TODO: worry about multiple threads instrumenting simultaneously,
	// make "counter" thread-safe.
	static int counter = 0;
	char buf[34];

#if defined(MVS)
#pragma convlit(suspend)
	sprintf(buf, "probekit$staticField_%d", counter);
	__etoa(buf);
#pragma convlit(resume)
#elif defined(__OS400__)
#pragma convert(0)
	sprintf(buf, "probekit$staticField_%d", counter);
	__etoa(buf);
#pragma convert(819)
#else
	sprintf(buf, "probekit$staticField_%d", counter);
#endif

	counter++;
	return (string)buf;
}

//
// Special note on this function: the static field we synthesize to
// hold the StaticField for a probe has to be private. Otherwise
// serialization would break for those classes that already have
// a <clinit> method, but which do not have an explicit serialVersionUID.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//

static unsigned
synthesize_static_field(CModuleJ* pmodj, CProbe* pProbe)
{
	string fieldName = synthesize_static_field_name();
	unsigned fieldAccess = ACC_PRIVATE | ACC_STATIC;
	CCPFieldrefInfo* pFieldRefInfo;
	pFieldRefInfo = pmodj->CreateFieldRef(
		fieldAccess, 
		fieldName.c_str(), 
		CJavaType(CJavaType::J_CLASS, 0, pProbe->GetStaticFieldType()));
	unsigned fieldRef = pFieldRefInfo->GetCpIndex();
	return fieldRef;
}

//
// Sythesize a private, static, final data member called serialVersionUID.
// This is used when we add a <clinit> method to a class, because
// doing so will change the auto-generated serial version UID.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//

static unsigned
synthesize_serialVersionUID_field(CModuleJ* pmodj)
{
	unsigned fieldAccess = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
	CCPFieldrefInfo* pFieldRefInfo;
	pFieldRefInfo = pmodj->CreateFieldRef(
		fieldAccess,
		"serialVersionUID",
		CJavaType(CJavaType::J_LONG, 0, NULL));
	unsigned fieldRef = pFieldRefInfo->GetCpIndex();
	return fieldRef;
}

//==============================================================================
// CLineTableEncoder implementation
//
// The algorithm is as follows:
//
// Start with a "previous number" of zero.
// To emit a number:
//		if the increment from the previous is in the range [0..9],
//			emit the increment, with a leading '+' if this is the first
//			increment digit since the previous full number or the start of the string.
//		else
//			emit a number sign and the full number.
//
void 
CLineTableEncoder::appendLineNumber(int i)
{
	if ((i < m_previous_number) || i > (m_previous_number + 9)) {
		// Use a new full number
		char numBuffer[32];
#if defined(MVS)
#pragma convlit(suspend)
		sprintf(numBuffer, "#%d", i);
		__etoa(numBuffer);
#pragma convlit(resume)
#elif defined(__OS400__)
#pragma convert(0)
		sprintf(numBuffer, "#%d", i);
		__etoa(numBuffer);
#pragma convert(819)
#else
		sprintf(numBuffer, "#%d", i);
#endif

		append(numBuffer);
		m_increment_mode = false;
	}
	else {
		// Use increment mode. Enter that mode if necessary.
		if (!m_increment_mode) {
			append("+");
			m_increment_mode = true;
		}
		char short_str[2];
		short_str[0] = '0' + (i - m_previous_number);
		short_str[1] = '\0';
		append(short_str);
	}
	m_previous_number = i;
}

//==============================================================================
// CProbeFragment implementation
//


//------------------------------------------------------------------------------
// CProbeFragment::ComputeArgBits
//
// Compute argument bits from the list of arguments
//
unsigned int
CProbeFragment::ComputeArgBits(CSTR i_szArgList)
{
	unsigned int result = 0;
	char* argListCopy = strdup(i_szArgList);
	char* token = strtok(argListCopy, ",");
	
	while (token) {
		int i = 0;
		unsigned int bit = 1;
		bool found = false;
		for ( ; argNames[i] != NULL; i++, bit <<= 1) {
			if (strcmp(token, argNames[i]) == 0) {
				result |= bit;
				found = true;
				token = strtok(NULL, ",");
				break;
			}
		}
		if (!found) {
			free(argListCopy);
			throw CBCIEngProbeException("Unknown argument name in argument list");
		}
	}
	free(argListCopy);
	return result;
}

//------------------------------------------------------------------------------
//
// CProbeFragment::Instrument
//
// Set the context fields that are specific to this fragment, 
// call PushArguments, and insert the invokestatic instruction
// that will call this fragment's function.
//

void
CProbeFragment::Instrument(CProbeInsertionContext& ctxt)
{
	ctxt.fragmentType = m_fragType;
	ctxt.argBits = m_argBits;
	PushArguments(ctxt);
	CInstruction* invokeStaticInstruction = m_pextref->CreateInstruction();
	ctxt.Insert(invokeStaticInstruction);
}

//------------------------------------------------------------------------
//
// BOXING AND UNBOXING
//
// The box_data_table array holds information about how to "box"
// a Java value.
//	typechar is the char for the Java type this row describes.
//	typesize is the number of stack positions that value uses, aka "category" (1 or 2)
//	pLoad_creator is a pointer to a function that takes a local variable slot number and returns the
//		appropriate "load" instruction for the type -- iload for integers, dload for doubles, etc.
//	class_name is the name of the Java box class for this type (like Integer for int)
//	init_sig is the signature of the <init> method for the box class.
//
// Important!
// Order of elements in this table depends on the jtype_t enumerator ordering
// (see JavaHelpers.h) Improper ordering may result in the assertion failure 
// or ABR.
// Also note that the first element of each record is used for the 
// consistency control. The value of the type enumerator must correspond to 
// the element's index in the array.
//

static struct box_data {
	CJavaType::jtype_t jtype;
	char typechar;
	char typesize;
	CInstruction* (*pLoad_creator)(int arg_slot);
	const char* class_name;
	const char* init_sig;
	const char* unbox_method;
	const char* unbox_sig;
} box_data_table[] = {
	{ CJavaType::J_BYTE,	'B', 1, CInsSetJ::Create_iload	, "java/lang/Byte"		, "(B)V", "byteValue"	, "()B" },
	{ CJavaType::J_CHAR,	'C', 1, CInsSetJ::Create_iload	, "java/lang/Character"	, "(C)V", "charValue"	, "()C" },
	{ CJavaType::J_DOUBLE,	'D', 2, CInsSetJ::Create_dload	, "java/lang/Double"	, "(D)V", "doubleValue"	, "()D" },
	{ CJavaType::J_FLOAT,	'F', 1, CInsSetJ::Create_fload	, "java/lang/Float"		, "(F)V", "floatValue"	, "()F" },
	{ CJavaType::J_INT,	    'I', 1, CInsSetJ::Create_iload	, "java/lang/Integer"	, "(I)V", "intValue"	, "()I" },
	{ CJavaType::J_LONG,	'J', 2, CInsSetJ::Create_lload	, "java/lang/Long"		, "(J)V", "longValue"	, "()J" },
	{ CJavaType::J_CLASS,	 0,  0, NULL					, NULL					, NULL  , NULL			, NULL  }, /* Placeholder */
	{ CJavaType::J_SHORT,	'S', 1, CInsSetJ::Create_iload	, "java/lang/Short"		, "(S)V", "shortValue"	, "()S" },
	{ CJavaType::J_BOOLEAN, 'Z', 1, CInsSetJ::Create_iload	, "java/lang/Boolean"	, "(Z)V", "booleanValue", "()Z" },
};

// BoxStackedValue
//
// Takes a char telling what type the top of the stack has,
// and a bunch of parameters that let us emit instructions
// and access the constant pool. Returns the number of
// stack slots used to achieve the result.
// 
// This function emits instructions that make a boxed version of 
// the value at the top of the stack. By "boxed" I mean "placed
// inside an object of the appropriate type."
//
// The boolean "keep_original" controls whether the original value
// is preserved at the top of the stack, and we box a COPY of that value.
//
// Initially, the value we want to box is on top of the stack.
// When these instructions are finished, there is a new thing on top 
// of the stack: a reference to an object of the right type 
// (e.g. Integer for int) that holds that value. 
// If keep_original is true, the original value is
// still on the stack, under the reference to the box object.
//
// This is used for converting a primitive return value into
// an object for passing to the exit fragment. The original return
// value starts on the top of the stack, and keep_original is true:
// when the exit fragment is finished, the original return value 
// is STILL on top of the stack.
//
// This is also used from CallsiteStoreArgs, which moves values
// from the stack into an Object array. In that case keep_original
// is false, and the values are actually consumed off the stack.
//
// The instructions you emit are different for Category 1 and
// Category 2 values. (Long and Double are Category 2 values.)
//
// For Category 1 values emit this:
//	dup (conditional on keep_original)
//	new class_name
//	dup_x1
//	swap
//	invokespecial <init> (I)V (or whatever signature)
//
// For Category 2 values (double and long) emit this:
//	dup2 (conditional on keep_original)
//	new class_name
//	dup_x2
//	dup_x2
//	pop
//	invokespecial <init> (D)V / (J)V
//
// Note: the combination "dup_x2 / pop" is used to achieve "swap"
// where the top value is category 1 and the next value is category 2.

void
CProbeInsertionContext::BoxStackedValue(CJavaType::jtype_t jtype, bool keep_on_stack)
{
	const box_data* pBoxData = &box_data_table[jtype];
	// assert(pBoxData->jtype == jtype);

	unsigned int class_ref = get_class_ref(*this, pBoxData->class_name);
	unsigned int meth_ref = get_method_ref(*this, class_ref, "<init>", pBoxData->init_sig);

	if (pBoxData->typesize == 2) {
		// Category 2 value on the stack (double or long)
		// See top of function for the instructions we implement and why

		// Initial stack is "v1 v2 -- (that is, two parts of a cat2 value)
		if (keep_on_stack) {
			Insert(CInsSetJ::Create_simple(JVMI_dup2));
		}

		// now stack (not counting dup'd value if any) is v1 v2 --
		Insert(CInsSetJ::Create_new(class_ref));
		// now stack is v1 v2 obj --
		Insert(CInsSetJ::Create_simple(JVMI_dup_x2));
		// now stack is obj v1 v2 obj --
		Insert(CInsSetJ::Create_simple(JVMI_dup_x2));
		// now stack is obj obj v1 v2 obj --
		Insert(CInsSetJ::Create_simple(JVMI_pop));
		// now stack is obj obj v1 v2 --
		Insert(CInsSetJ::Create_invokespecial(meth_ref));
		// now stack is obj --
	}
	else {
		// Category 1 value on the stack
		// Initial stack is "v --" (that is, the single category 1 value)
		if (keep_on_stack) {
			Insert(CInsSetJ::Create_simple(JVMI_dup));
		}
		// now stack (not counting dup'd value if any) is v --
		Insert(CInsSetJ::Create_new(class_ref));
		// now stack is v obj --
		Insert(CInsSetJ::Create_simple(JVMI_dup_x1));
		// now stack is obj v obj --
		Insert(CInsSetJ::Create_simple(JVMI_swap));
		// now stack is obj obj v --
		Insert(CInsSetJ::Create_invokespecial(meth_ref));
		// now stack is obj --
	}
}

// BoxLocalValue
//
// This function does the same as BoxStackedValue,
// except that the value starts out as a local variable.
// Leaves the boxed object reference on the stack.
//
// Returns a bool: true if the local was a "category 2"
// (and therefore consumed two slots in the local variable table).

void
CProbeInsertionContext::BoxLocalValue(CJavaType::jtype_t jtype, int local_num)
{
	const box_data* pBoxData = &box_data_table[jtype];
	// assert(pBoxData->jtype == jtype);

	// Could assert that we found it, but failure Should Never Happen

	unsigned class_ref = get_class_ref(*this, pBoxData->class_name);
	unsigned meth_ref = get_method_ref(*this, class_ref, "<init>", pBoxData->init_sig);
	Insert(CInsSetJ::Create_new(class_ref));
	Insert(CInsSetJ::Create_simple(JVMI_dup));
	Insert((*(pBoxData->pLoad_creator))(local_num));
	Insert(CInsSetJ::Create_invokespecial(meth_ref));
}

// UnboxStackedValue
//
// Takes a char telling what type the top of the stack should be,
// and a bunch of parameters that let us emit instructions
// and access the constant pool. Returns the number of
// stack slots used to achieve the result.
// 
// This function emits instructions that extract the value from
// a boxed version of the value at the top of the stack. 
// By "boxed" I mean "placed inside an object of the appropriate type."
//
// The top value on the stack expected to be an Object;
// the first thing this function emits is a checkcast instruction
// to convert it to the proper type, like Integer for int.
//
// Then we emit an "invokevirtual" instruction to get the
// value out of the box. The function being called and its 
// signature are always different.
// For an Integer, for example, we emit this:
//	checkcast java/lang/Integer
//	invokevirtual intValue(Ljava/lang/Integer)I
//
// But for a Long we emit this:
//	checkcast java/lang/Long
//	invokevirtual longValue(Ljava/lang/Long)J

int
CProbeInsertionContext::UnboxStackedValue(CJavaType::jtype_t jtype)
{
	const box_data* pBoxData = &box_data_table[jtype];
	// assert(pBoxData->jtype == jtype);

	unsigned int class_ref = get_class_ref(*this, pBoxData->class_name);
	unsigned int meth_ref = get_method_ref(*this, class_ref, pBoxData->unbox_method, pBoxData->unbox_sig);

	Insert(CInsSetJ::Create_checkcast(class_ref));
	Insert(CInsSetJ::Create_invokevirtual(meth_ref));

	// Return the added stack depth: zero for Category 1, one for Category 2.
	return (pBoxData->typesize == 2 ? 1 : 0);
}

//------------------------------------------------------------------------
//
// Notes on doing insertion to pass the argument list
//
// If any fragment that applies to the current method wants the
// argument list as a parameter, we do insertion on entry to capture
// the arguments in an array of Objects. We store that array in
// a new local variable, and pass it to those fragments that want it.
//
// (If the argument list is only needed once, this is slightly suboptimal:
// we could create it in-place without allocating a local. But the current
// approach is more optimal in other cases, and simpler to maintain.)
//
// The inserted instruction sequence starts with creating the object array:
//		iconst (argcount)
//		anewarray ("java/lang/Object")
//
// Now the object array reference is at the top of the stack.
// It will stay there for the whole remainder of the sequence,
// until it's finally stored in a new local variable.
//
// Then, for a reference-type argument, we arrange the stack
// for the aastore instruction, which wants the array object, the
// slot number, and the new value on the stack in that order.
// The slot number is "arrayslot" and the new value is obtained
// from the argument/local array using aload from "argslot."
// The insertion looks like this:
//		dup
//		iconst (arrayslot)
//		aload (argslot)
//		aastore
//
// For a primitive-type argument, we have to "box" the primitive first.
// That is, we have to wrap an "int" into an Integer object on the heap.
// So we dup the array object, push the slot number (in preparation
// for the ultimate aastore instruction), and then push the
// primitive value and box it, leaving the box object on the stack
// for the aastore instruction to see. It looks like this 
// (this example is for an integer):
//		dup
//		iconst (arrayslot)
//		// Next 4 insns emitted by BoxLocalValue(argslot, argtype)
//		new (java/lang/Integer)
//		dup
//		iload (argslot)
//		invokespecial java/lang/Integer <init> (I)V
//		// end of BoxLocalValue()
//		aastore
//

void
CProbeInsertionContext::EmitArgsList()
{
	CJavaMethodName methodInfo;

	methodInfo.SetName(methodName);
	methodInfo.SetSignature(methodSignature);
	// Count arguments from the signature
	int argcount = methodInfo.GetArgCount();
	bool any_boxing = false;
	bool any_long_boxing = false;

	// Emit instructions to create a new array of type Object[argcount]
	unsigned classRef = get_class_ref(*this, "java/lang/Object");
	Insert(CInsSetJ::Create_push_constant(argcount));
	Insert(CInsSetJ::Create_anewarray(classRef));

	// The argslot variable is the aload parameter for the
	// current argument. It starts at zero for static methods,
	// or one for instance methods (because zero is "this").
	// Advances by "argsize" each time through the loop.
	// Argsize is usually 1, but it is 2 for doubles and longs.
	int argslot = (hasThis ? 1 : 0);
	int argsize;

	// The arrayslot variable holds the slot number
	// into which each argument will be placed. Increments by one each time.
	int arrayslot = 0;


	CJavaMethodName::args_t args = methodInfo.GetArgs();
	CJavaMethodName::args_t::iterator iter;
	for (iter = args.begin(); iter != args.end(); arrayslot++, iter++) 
	{
		// Dup the array object and push the index to store into.
		Insert(CInsSetJ::Create_simple(JVMI_dup));
		Insert(CInsSetJ::Create_push_constant(arrayslot));

		// Now push the value to store.
		// In the 'a' case (object or array), use aload.
		// Otherwise use BoxLocalValue to create a temporary.
		if (iter->IsPrimitive()) {
			// Primitive type. Box it.
			BoxLocalValue(iter->GetType(), argslot);

			// Track knowledge we need for 
			// advancing to the next arg in the table
			any_boxing = true;
			if (iter->GetCategory() == 2) {
				// Category 2
				any_long_boxing = true;
				argsize = 2;
			}
			else {
				argsize = 1;
			}
		}
		else {
			// Non-primitive type
			Insert(CInsSetJ::Create_aload(argslot));
			argsize = 1;
		}

		// Emit "aastore" now that the right args are on the stack
		Insert(CInsSetJ::Create_simple(JVMI_aastore));

		// Advance argslot by argsize, which is two for Type 2 arguments (long and double)
		argslot += argsize;
	}
}

// This function returns "true" if a "this" argument is accessible to
// the current fragment as applied to the current method. 
// ("Current" means "the one described in the CProbeInsertionContext.")
//
// Just because the context says the method hasThis, that doesn't
// mean the probe can access "this."
//
// In addition, an exception-exit handler for a constructor
// can't access "this." The reason: we put a "finally"
// wrapper around the whole original function, and that means
// we're wrapping some code where "this" is uninitialized.
// TODO: perform liveness analysis to identify when "this"
// becomes initialized, and create two exception-exit wrappers:
// one for before that time (passing null as this) and
// one for after that time (passing the real this value).
//
// Result: entry probes, beforecall probes, and exception exit probes
// can't access "this" in constructors.
//

bool
CProbeInsertionContext::CanAccessThis()
{
	if (hasThis)
	{
		if ((fragmentType == CProbeFragment::PROBE_ONENTRY ||
			 fragmentType == CProbeFragment::PROBE_BEFORECALL ||
			 isExceptionExit) &&
			strcmp(methodName, "<init>") == 0) 
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	else
	{
		return false;
	}
}

// PushArguments
// This function emits instructions that push the arguments indicated by m_argBits.
// Some auxiliary parameters are used to pass extra info, like isFinally.
//
// The string-type parameters are pushed using the helper function emit_ldc_for_string,
// which puts the string in the constant pool and emits "ldc" for that string.
//
// Returns the added stack depth - the maximum depth of stack used to
// compute and pass the arguments to the probe function.
//
// ---- HACK ALERT ---- HACK ALERT ---- HACK ALERT ---
//
// The BCI for the returnedObject and exceptionObject parameter types is
// tricky. First of all, it relies on those being the first two
// parameters in the canonical ordering, so the BCI for passing
// those parameters occurs first, before the BCI for passing
// other parameters. Second, they are entangled: the BCI for 
// exceptionObject is slightly different if the fragment also 
// asked for returnedObject.
//
// Here's the story:
//
// returnedObject occurs at return instructions. Since returnedObject is
// the first parameter type in the canonical ordering, the value being returned
// is at the top of the stack. We can use a simple "dup" instruction
// to create the parameter to the fragment function. (We then need to
// "box" the value into an object, if it's a primitive type.) This only
// works because returnedObject is the first parameter in the canonical ordering.
//
// exceptionObject is significant at the tops of catch and finally clauses.
// In this case, again, the thrown object is at the top of the stack.
// If the fragment doesn't also want the returnedObject, a simple "dup"
// instruction creates the parameter we need. If the fragment does want
// the returnedObject, then the BCI will already have pushed a "null"
// for that, and now the thrown object is two items down on the stack.
// In that case we emit "swap / dup_x1" to create the stack pattern
// we need, with the exception object, a null, and the exception object
// on the stack in that order.
//
// The insertion done here for returnedObject and exceptionObject
// always leaves the relevant object on the stack - that is, they start
// by duplicating the object.
//

void
CProbeFragment::PushArguments(CProbeInsertionContext& ctxt)
{
	// push the arguments, one after another, in the canonical order,
	// based on the information in the Probe Insertion Context.
	CJavaMethodName methodInfo;
	methodInfo.SetName(ctxt.methodName);
	methodInfo.SetSignature(ctxt.methodSignature);
	
	for (unsigned int i = 1; i < ARG_BITS_LAST; i <<= 1) {
		if (ctxt.argBits & i) {
			switch (i) {
				case ARG_BITS_CLASSNAME: {
					CCPUtf8Info utf8Name(ctxt.className);
					emit_ldc_for_string(ctxt, utf8Name);
					break;
				}
				case ARG_BITS_METHODNAME: {
					CCPUtf8Info utf8Name(ctxt.methodName);
					emit_ldc_for_string(ctxt, utf8Name);
					break;
				}
				case ARG_BITS_METHODSIG: {
					CCPUtf8Info utf8Name(ctxt.methodSignature);
					emit_ldc_for_string(ctxt, utf8Name);
					break;
				}
				case ARG_BITS_THISOBJ: {
					if (ctxt.CanAccessThis()) {
						ctxt.Insert(CInsSetJ::Create_aload(ctxt.localVariableForThis));
					}
					else {
						// No accessible "this" object - pass NULL
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
					}
					break;
				}
				case ARG_BITS_ARGSLIST: {
					// The caller must already have created the local containing the Object[] array.
					// Function insertion callers did this with EmitArgsList();
					// callsite insertion callers did it with another function.
					// assert(ctxt.localVariableForArgs != -1);
					ctxt.Insert(CInsSetJ::Create_aload(ctxt.localVariableForArgs));
					break;
				}
				case ARG_BITS_RETURNEDOBJ: {
					// ---- HACK ALERT ---- HACK ALERT ---- HACK ALERT ---
					// See comments at the top of this function. returnedObject must
					// be the first parameter type in the canonical order.

					CBCIEngProbeException::Assert(
						ctxt.fragmentType == PROBE_ONEXIT || ctxt.fragmentType == PROBE_AFTERCALL,
						"Invalid use of returnedObject: not in exit probe");

					if (ctxt.isExceptionExit) {
						// The function is exiting by exception, not a return instruction.
						// Pass null as the returnedObject data item.
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
					}
					else {
						CJavaType jtypeRet = methodInfo.GetRetType();
						if (jtypeRet.GetType() == CJavaType::J_VOID) {
							// function is void: pass null
							ctxt.Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
						}
						else if (jtypeRet.IsPrimitive()) {
							// BoxStackedValue takes a bool telling whether to leave
							// the value on the stack or not. In this case, yes.
							ctxt.BoxStackedValue(jtypeRet.GetType(), true);
						}
						else {
							// returns an object
							ctxt.Insert(CInsSetJ::Create_simple(JVMI_dup));
						}
					}
					break;
				}
				case ARG_BITS_EXOBJ: {
					// ---- HACK ALERT ---- HACK ALERT ---- HACK ALERT ---
					// See comments at the top of this function. exceptionObject must
					// be the second parameter type in the canonical order.
					bool bExitProbe = ctxt.fragmentType == PROBE_ONCATCH 
						           || ctxt.fragmentType == PROBE_ONEXIT
								   || ctxt.fragmentType == PROBE_AFTERCALL;

					CBCIEngProbeException::Assert(bExitProbe,
						"Invalid use of exceptionObject: not in catch or exit probe");

					if ((ctxt.fragmentType == PROBE_ONEXIT || ctxt.fragmentType == PROBE_AFTERCALL) && 
						!ctxt.isExceptionExit) 
					{
						// The insertion point is a "return" instruction, not an exception handler.
						// Pass null.
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
					}
					else 
					{
						// The insertion point is an exception point: catch, finally, or exception exit.
						// If this fragment also wanted the return value,
						// then the ARG_BITS_RETURNEDOBJ case above has already pushed a null.
						// In that case, we have to emit swap / dup_x1 to get a copy of the exception 
						// object onto the stack in the right place. 
						// If this fragment did not want the return value, just emit "dup."
						if (ctxt.argBits & ARG_BITS_RETURNEDOBJ) {
							CBCIEngProbeException::Assert(ctxt.fragmentType != PROBE_ONCATCH,
								"Internal error: a catch probe wanted the return object?!");
							ctxt.Insert(CInsSetJ::Create_simple(JVMI_swap));
							ctxt.Insert(CInsSetJ::Create_simple(JVMI_dup_x1));
						}
						else {
							ctxt.Insert(CInsSetJ::Create_simple(JVMI_dup));
						}
					}
					break;
				}
				case ARG_BITS_ISFINALLY: {
					CBCIEngProbeException::Assert(ctxt.fragmentType == PROBE_ONCATCH, 
						"Invalid use of isFinally: not in catch probe");

					if (ctxt.isFinally) {
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_iconst_1));
					}
					else {
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_iconst_0));
					}
					break;
				}
				case ARG_BITS_STATICFIELD: {
					// Emit getstatic for the static field synthesized from this probe.
					CBCIEngProbeException::Assert(ctxt.staticFieldRef != 0,
						"Invalid use of staticField: no static field was defined.");
					ctxt.Insert(CInsSetJ::Create_getstatic(ctxt.staticFieldRef));
					break;
				}
				case ARG_BITS_CLASSSOURCEFILE: {
					// Emit ldc for the source file name string, or null if there is none
					if (ctxt.classSourceFile != NULL) {
						CCPUtf8Info utf8Name(ctxt.classSourceFile);
						emit_ldc_for_string(ctxt, utf8Name);
					}
					else {
						// No source available - push null
						ctxt.Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
					}
					break;
				}
				case ARG_BITS_METHODNAMES: {
					CCPUtf8Info utf8Name(ctxt.methodNamesString);
					emit_ldc_for_string(ctxt, utf8Name);
					break;
				}
				case ARG_BITS_METHODLINETABLES: {
					CCPUtf8Info utf8Name(ctxt.lineTableString);
					emit_ldc_for_string(ctxt, utf8Name);
					break;
				}
				case ARG_BITS_METHODNUMBER: {
					// Emit a constant for the method number
					ctxt.Insert(CInsSetJ::Create_push_constant(ctxt.methodNumber));
					break;
				}
				case ARG_BITS_EXECUTABLEUNITNUMBER: {
					// Emit a constant for the executable unit number
					ctxt.Insert(CInsSetJ::Create_push_constant(ctxt.executableUnitNumber));
					break;
				}
			}
		}
	}
}

//==============================================================================
// Helper function that performs a quick consistency check: the number of '1' bits 
// in the argBits should be the same as the number of arguments in the signature.
// Also, the signature should end with )V since no probes return values yet.
// TODO: change this when we implement InvocationObject, where the entry probe
// returns a value.

static void
verify_argbits_and_sig(unsigned i_argBits, CSTR i_szSig)
{
	CJavaMethodName methodInfo;
	methodInfo.SetSignature(i_szSig);
	int bitcount = 0;

	for (unsigned int bit = 1; bit < CProbeFragment::ARG_BITS_LAST; bit <<= 1) {
		if (i_argBits & bit) bitcount++;
	}

	const char *p = i_szSig;

	if (*p != '(') {
		throw CBCIEngProbeException("Malformed probe method signature (1)");
	}

	int argcount = methodInfo.GetArgCount();

	// Scan 'p' to the closing paren
	while (*p && *p != ')') p++;
	if (!*p) {
		throw CBCIEngProbeException("Malformed probe method signature (2)");
	}

	// p points to the closing paren. Check that the return type is void.
	if (methodInfo.GetRetType().GetType() != CJavaType::J_VOID) {
		throw CBCIEngProbeException("Malformed probe method signature (3)");
	}

	if (argcount != bitcount) {
		throw CBCIEngProbeException("Probe signature and arg list size mismatch");
	}

	// else return without throwing an exception
}

//==============================================================================
//
// CALLSITE INSERTION HELPER FUNCTIONS
//
// The callsite insertion logic is a slightly different animal compared to
// function insertion. It works by instrumenting (duh) the call site instead of 
// the head and tail of the function you want to find out about.
//
// If there are any callsite probes in the mix, the BCI engine has to scan
// every function that gets loaded to see if it makes any calls any of the functions
// you want to apply callsite probes to. If so, it has to do callsite
// insertion around those calls: entry insertion before the call, and exit
// insertion after.
//
// You'd use callsite insertion if, say, you wanted to trace calls to the
// Jdbc.query() method, but you can't do BCI on the class Jdbc because it
// loads in a way that your BCI engine doesn't have access to. (Many app servers
// provide a "class load hook" to give a chance for BCI, but only "application
// classes" get passed through that hook. System classes do not.)
//
// A callsite probe "entry" fragment can access the same data that the other
// kind can, but the byte codes are different. The BCI occurs immediately
// before the "invokevirtual" / static / special / interface instruction.
//
// To build the Object array that holds the argument list, we have to pop the 
// arguments off the stack one by one and store them in it. Then we 
// can pass the Object array to the entry probe. Then, to make the
// original call, we we push the arguments back on the stack.
//
// If the callsite probe "exit" fragment wants the argument list, well, we've
// got it sitting right there. 
//
// If the entry fragment wants the "this" argument, we have to create the
// Object array to store all the arguments from the stack in order to get down
// to the "this" argument. We can probably use "dup" carefully to keep the
// "this" argument on the stack and still pass it to the entry fragment.
// We might even dup it twice before the call, if the exit fragment
// wants it too. If we implement exception-exit, we'll have to store it
// in a local variable.
//
// A note about creating a new local variable: the JVM standard says 
// we don't need to add an entry to the local variable table - that's
// optional and used for debuggers anyway. But the lifetime of the value in
// the local covers the whole function, so if we store an object reference
// into one, we should null it out when we're finished with it.
//
// TODO: figure out a way to pass "this" to exit insertion when the
// function being called is a constructor. Today we can't do it because
// we can't assign "this" into a local variable until it's been initialized.
// The way to do it is to pop the arguments off the stack, then dup the "this"
// value on the stack, then put the args back on and call the constructor.
// When the constructor returns, the "this" object on the stack will
// have been been initialized and we can store it into a local for later
// passing to the exit insertion.

// CallsiteStoreArgs
//
// Create an array of Objects and put it into a local.
// Assign values into it by popping them off the stack. The types of the
// values on the stack are given by a signature string.
// The LAST argument in the signature string is the TOP item on the stack.
// 

void
CProbeInsertionContext::CallsiteStoreArgs()
{
	CJavaMethodName methodInfo;

	methodInfo.SetName(methodName);
	methodInfo.SetSignature(methodSignature);
	// Count arguments from the signature
	int argcount = methodInfo.GetArgCount();

	// Create the object array and put it in a local
	unsigned classRef = get_class_ref(*this, "java/lang/Object");
	Insert(CInsSetJ::Create_push_constant(argcount));
	Insert(CInsSetJ::Create_anewarray(classRef));
	Insert(CInsSetJ::Create_astore(localVariableForArgs));

	if (argcount != 0) {
		// Walk the arg type char array backwards.
		// Those stack elements which are primitive types must be
		// boxed first, by calling BoxStackedValue.
		//
		// Since the value we want to store into the array is
		// already on top of the stack, we have to push the
		// array reference and index behind it, using a swap
		// instruction after pushing each one. 
		//
		// After boxing, the type at the top of the stack
		// is of type "reference." Then we emit this sequence:
		//	aload i_localForCallsiteArgs
		//	swap
		//	iconst arg_number
		//	swap
		//	aastore

		CJavaMethodName::args_t args = methodInfo.GetArgs();
		CJavaMethodName::args_t::iterator iter = args.end();
		int i;
		for (i = argcount-1; i >= 0; i--) {
			iter--;
			// If necessary, box the value at the top of the stack
			if (iter->IsPrimitive()) {
				BoxStackedValue(iter->GetType(), false);
			}
			// Store the value at the top of the stack into obj_array[i]
			Insert(CInsSetJ::Create_aload(localVariableForArgs));
			Insert(CInsSetJ::Create_simple(JVMI_swap));
			Insert(CInsSetJ::Create_push_constant(i));
			Insert(CInsSetJ::Create_simple(JVMI_swap));
			Insert(CInsSetJ::Create_simple(JVMI_aastore));
		}
	}
	else {
		// Arg count is zero.
		// We have created a valid, zero-length array of Object
		// and stored a reference to it in the appropriate local variable.
		int dummy = 0; // for coverage purposes
	}

	// Finished. All arguments (if any) are stored into the Object array.
}

// CallsiteStoreThis
//
// Store the "this" argument for the called method into the local variable
// slot given as i_localForCallsiteThis. If the called method is static,
// or the called method is a constructor, then function should not be called.
// See TODO elsewhere about storing "this" *after* the call to a constructor.
//

void
CProbeInsertionContext::CallsiteStoreThis()
{
	if (CanAccessThis()) {
		Insert(CInsSetJ::Create_astore(localVariableForThis));
	}
	else {
		Insert(CInsSetJ::Create_simple(JVMI_aconst_null));
		Insert(CInsSetJ::Create_astore(localVariableForThis));
	}
}

// CallsiteReloadArgsAndThis
//
// Restore the "this" pointer and arguments from the
// given local variables.
//
// Primitive values which were boxed need to be unboxed.
//
// If i_localForCallsiteThis is -1, it means there is no "this" parameter.
// If i_localForCallsiteArgs is -1, it means there are no args to restore.
//
// Getting each arg out of the object array looks like this:
//
//	aload (local)
//	push slot_number
//	aaload			(get object ref out of array)
//	checkcast		(cast it to its proper type)
//
// If the value was originally a primitive, the checkcast leaves it
// as the box type -- Integer for int. THen it needs to be unboxed;
// see unbox_stacked_value for how that's done.
//

void
CProbeInsertionContext::CallsiteReloadArgsAndThis()
{
	CJavaMethodName methodInfo;
	methodInfo.SetName(methodName);
	methodInfo.SetSignature(methodSignature);

	if (localVariableForThis != -1 && CanAccessThis()) {
		Insert(CInsSetJ::Create_aload(localVariableForThis));
	}

	if (localVariableForArgs != -1) {
		CJavaMethodName::args_t args = methodInfo.GetArgs();
		CJavaMethodName::args_t::iterator iter;
		int i = 0;
		for (iter = args.begin(); iter != args.end(); iter++) 
		{
			Insert(CInsSetJ::Create_aload(localVariableForArgs));
			Insert(CInsSetJ::Create_push_constant(i));
			Insert(CInsSetJ::Create_simple(JVMI_aaload));
			if (iter->IsPrimitive()) {
				UnboxStackedValue(iter->GetType());
			}
			else {
				// Emit checkcast to convert to the proper type
				string strType;
				if(iter->IsArray())
					strType = iter->GetTypeString();
				else
					strType = iter->GetClassName();
				unsigned int cpInd = get_class_ref(*this, strType.c_str());
				Insert(CInsSetJ::Create_checkcast(cpInd));
			}
			++i;
		}
	}
}

//==============================================================================
CProbeFragment::CProbeFragment(CProbe* parent, fragmentType_t i_fragType, CSTR i_szClass, CSTR i_szMethod, CSTR i_szSig, CSTR i_szArgList)
{
	m_fragType = i_fragType;
	m_pextref = new CExtRefJ_StatMethod(i_szClass, i_szMethod, i_szSig);
	m_argBits = ComputeArgBits(i_szArgList);
	m_parent = parent;
	verify_argbits_and_sig(m_argBits, i_szSig);	// throws an exception on error
}

CProbeFragment::~CProbeFragment()
{
	delete m_pextref;
}

//==============================================================================
// Implementations for functions of class CFilterRule

// Constructor
CFilterRule::CFilterRule(CSTR i_szPackageName, 
					  CSTR i_szClassName, 
					  CSTR i_szMethodName, 
					  CSTR i_szMethodSig, 
					  CFilterRule::action_t i_action)
{
	m_strPackageName = strdup(i_szPackageName);
	m_strClassName = strdup(i_szClassName);
	m_strMethodName = strdup(i_szMethodName);
	m_strMethodSig = strdup(i_szMethodSig);
	m_action = i_action;

	char* p = m_strPackageName;
	while (*p) {
		if (*p == '.') 
			*p = '/';
		p++;
	}
}

// Copy constructor
CFilterRule::CFilterRule(const CFilterRule& other)
{
	m_strPackageName = strdup(other.m_strPackageName);
	m_strClassName = strdup(other.m_strClassName);
	m_strMethodName = strdup(other.m_strMethodName);
	m_strMethodSig = strdup(other.m_strMethodSig);
	m_action = other.m_action;
}

// Assignment operator
CFilterRule&
CFilterRule::operator=(const CFilterRule& other)
{
	m_strPackageName = strdup(other.m_strPackageName);
	m_strClassName = strdup(other.m_strClassName);
	m_strMethodName = strdup(other.m_strMethodName);
	m_strMethodSig = strdup(other.m_strMethodSig);
	m_action = other.m_action;
	return *this;
}

// Destructor
CFilterRule::~CFilterRule()
{
	free(m_strPackageName);
	free(m_strClassName);
	free(m_strMethodName);
	free(m_strMethodSig);
}

//------------------------------------------------------------------------------
bool 
CFilterRule::WildcardStringMatch(const char* pattern, const char* candidate)
{
	// Special case: don't-care on the candidate side
	if (strcmp(candidate, "*") == 0) {
		return true;
	}
	else if (strcmp(pattern, "*") == 0) {
		// star pattern matches anything
		return true;
	}
	else if (strchr(pattern, '*') == NULL) {
		// no wildcards, just compare strings
		return (strcmp(pattern, candidate) == 0);
	}

	// Else do the wild(card) thing. We know pattern has a star in it.
	//
	// We divide the pattern into substrings delimited by stars.
	// For each substring, we scan ahead in the candidate 
	// for the first match. If we don't find one,
	// the whole wildcard match fails. If we do find one, 
	// we "consume" the candidate up to the end of the match.
	// The next iteration will start matching at that point.
	//
	// The first iteration is different: if the pattern doesn't start 
	// with a star, the initial substring of the pattern must appear 
	// at the head of the candidate.
	//
	// The last iteration is different, too. If the pattern does not
	// end with a star, we explicitly check the END of the candidate 
	// against the last non-star substring. This way we get
	// the right behavior even if the candidate has more than one
	// instance of that last substring. EXAMPLE: the pattern is "a*c" and
	// the candidate is "abcabc" ... if the pattern 'c' matched the first
	// 'c' in the candidate, we'd think we fell off the end of the pattern
	// before consuming the whole candidate, and fail. But this candidate
	// DOES match the pattern, so we have to treat the last non-star
	// substring specially if the pattern doesn't end with a star.

	const char* pattern_pos;
	const char* candidate_pos;
	const char* star_pos = strchr(pattern, '*');

	// With no initial star, the heads of the strings have to match.
	if (star_pos != pattern) {
		// Star is not at start of pattern.
		// Start of candidate must match substring
		if (strncmp(candidate, pattern, (star_pos - pattern)) != 0) {
			// no leading star, heads don't match, FAIL.
			return false;
		}
		else {
			// Start the loop below at the end of this match in candidate.
			candidate_pos = candidate + (star_pos - pattern);
		}
	}
	else {
		// Pattern has an initial star. Start the loop below at the start of candidate.
		candidate_pos = candidate;
	}

	// Set pattern_pos past the star (either an initial star or after an initial non-star substring)
	pattern_pos = star_pos + 1;

	// Now we start a loop. 
	// At the top of each iteration, pattern_pos is the start of a non-star substring
	// in the pattern, following a star.
	// Also, at the top of each iteration, candidate_pos is the first position of 
	// the candidate that we haven't consumed yet. 
	//
	// See comments above for the algorithm, including notes on the last non-star
	// substring when the pattern doesn't end with a star.

	while (*pattern_pos && *candidate_pos) {
		// Find next star in pattern
		star_pos = strchr(pattern_pos, '*');
		if (star_pos == NULL) {
			// No remaining stars, compare tail of pattern against tail of candidate
			int tail_len = strlen(pattern_pos);
			if (strlen(candidate_pos) < tail_len) {
				// remainder of candidate isn't long enough, so there's no match
				return false;
			}
			else {
				const char* candidate_tail = candidate + strlen(candidate) - tail_len;
				if (strcmp(pattern_pos, candidate_tail) == 0) {
					// no remaining stars, tails match, MATCH
					return true;
				}
				else {
					// no remaining stars, tails don't match. NO MATCH
					return false;
				}
			}
		}

		// OK, there is a star out there at star_pos.
		// Find the non-star substring of pattern in candidate
		int sub_len = star_pos - pattern_pos;
		const char* match_pos;
		{
			// Look for the substring of pattern_pos (sub_len long) in candidate_pos..
			// Set match_pos to the start of the match in the candidate.
			match_pos = candidate_pos;
			int candidate_remainder_len = strlen(candidate_pos);
			if (sub_len > candidate_remainder_len) {
				// Not enough candidate left to match
				return false;
			}
			const char* last_match_start = candidate_pos + (candidate_remainder_len - sub_len);
			while (match_pos <= last_match_start && strncmp(match_pos, pattern_pos, sub_len) != 0) {
				++match_pos;
			}
			if (match_pos > last_match_start) {
				// Fell out of the loop with NO MATCH.
				return false;
			}
		}

		// Found this non-star substring in candidate.
		// Advance candidate_pos past the match, 
		// and advance pattern_pos past the substring and stars,
		// and loop again.
		candidate_pos = match_pos + sub_len;
		pattern_pos = pattern_pos + sub_len;
		while (*pattern_pos == '*') {
			pattern_pos++;
		}
	}

	// We fall out of this loop when we've consumed one or the other string.
	// The loop would have returned if we'd consumed the whole pattern and it
	// didn't end with a star, so if the pattern is consumed we know it DID
	// end with a star.
	if (!*pattern_pos) {
		// Bottomed out, star at end of pattern, MATCH
		return true;
	}
	else {
		// We know we're at the end of candidate (otherwise we'd have looped again).
		// This means we consumed the whole candidate without consuming the last non-star
		// part of the pattern. NO MATCH.
		return false;
	}
}

//------------------------------------------------------------------------------
//
// Return true if this filter rule's wildcards match the input strings.
//
bool 
CFilterRule::Match(const char* i_strPackage, 
	   const char* i_strClass, 
	   const char* i_strMethod, 
	   const char* i_strMethodSig)
{
	// Match package name first
	if ((strcmp(m_strPackageName, ".") == 0) || (strcmp(m_strPackageName, "/") == 0)) {
		// Package pattern of "." means we should match only 
		// if i_strPackage is the empty string.
		// (We test for "/" because somebody in the chain of evidence might have
		// turned dots into slashes in the package pattern.)
		if (*i_strPackage != '\0') return false;
	}
	else if (!WildcardStringMatch(m_strPackageName, i_strPackage)) {
		return false;
	}
	// else the package matched, so continue

	// Match class name next
	if (!WildcardStringMatch(m_strClassName, i_strClass)) {
		return false;
	}
	if (!WildcardStringMatch(m_strMethodName, i_strMethod)) {
		// pkg/class match but method doesn't
		return false;
	}
	if (!WildcardStringMatch(m_strMethodSig, i_strMethodSig)) {
		// pkg/class/method match, but signature doesn't
		return false;
	}
	// complete match: package, class, method, and signature
	return true;
}

//==============================================================================
// CPrefilters implementation

CPrefilters::~CPrefilters()
{
	// release the rules
	for(CPrefilters::iterator itr = begin(); itr != end(); itr++)
	{
		delete *itr;
	}
}

//==============================================================================
//------------------------------------------------------------------------------

void
CProbe::AddFilterRule(CSTR i_szPackageName, 
					  CSTR i_szClassName, 
					  CSTR i_szMethodName, 
					  CSTR i_szMethodSig, 
					  CFilterRule::action_t i_action)
{
	CFilterRule fr(i_szPackageName, i_szClassName, i_szMethodName, i_szMethodSig, i_action);
	m_ruleList.push_back(fr);
}

//==============================================================================
//------------------------------------------------------------------------------

void
CProbe::AddFilterRuleWithin(CSTR i_szPackageName, 
					  CSTR i_szClassName, 
					  CSTR i_szMethodName, 
					  CSTR i_szMethodSig, 
					  CFilterRule::action_t i_action)
{
	CFilterRule fr(i_szPackageName, i_szClassName, i_szMethodName, i_szMethodSig, i_action);
	m_ruleListWithin.push_back(fr);
}

//------------------------------------------------------------------------------
CProbe::~CProbe()
{
	// Delete the CProbeFragments for this probe
	for (CProbeFragmentList::iterator frag_iter = m_probefraglst.begin();
		 frag_iter != m_probefraglst.end();
		 frag_iter++) 
	{
		delete (*frag_iter);
	}
}

//------------------------------------------------------------------------------
void
CProbe::AddFragment(CProbeFragment::fragmentType_t i_fragType, 
		            CSTR i_szClass, CSTR i_szMethod, CSTR i_szMethodSig, 
					CSTR i_szRefType, CSTR i_szArgList)
{
	CProbeFragment* pprobefrag = new CProbeFragment(this, i_fragType, i_szClass, i_szMethod, i_szMethodSig, i_szArgList);
	m_probefraglst.push_back(pprobefrag);
}

//------------------------------------------------------------------------------
CProbeFragmentList&	
CProbe::GetProbeFragmentList()
{
	return m_probefraglst;
}

//------------------------------------------------------------------------------
void
CProbe::AddStaticField(CSTR i_szTypeString)
{
	if (m_staticFieldType.length() != 0) {
		// Internal error - setting the static field type twice!
		// TODO: throw an exception?
	}
	m_staticFieldType = i_szTypeString;
}

//------------------------------------------------------------------------------
// IsCallsiteProbe
// Return true if this probe is a callsite probe.
//
// We only support fragments which have ONLY before/after call, 
// or those which ONLY have entry/exit/catch.
// You shouldn't mix them.
//
// This limitation means we can answer the question by looking
// at the first CProbeFragment in this probe's list.
//

bool
CProbe::IsCallsiteProbe()
{
	if (m_probefraglst.size() == 0) 
		return false;

	CProbeFragment* firstfrag = m_probefraglst[0];
	if (firstfrag->GetType() == CProbeFragment::PROBE_BEFORECALL ||
		firstfrag->GetType() == CProbeFragment::PROBE_AFTERCALL) 
	{
		return true;
	}
	else {
		return false;
	}
}

//------------------------------------------------------------------------------
// Match function: the arguments are the package, class, method, and signature
// strings of a candidate function. The package is in internal format, with slashes.
//
// Process the rules stored in this CProbe in order.
// If there's a rule that says you should include this pkg/class/method/sig
// then return true. If there's one that says you should exclude, return false.
//
// There is ALWAYS a rule that matches, because there is an implicit last
// rule that says "include all." This function returns TRUE if no explicit
// rule matches.
//

bool
CProbe::Match(CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szSig, bool* pIsExplicit /* = NULL */)
{
	MatchCommon( m_ruleList, m_plstPrefilter, i_szPkgAndClass, i_szMethod, i_szSig, pIsExplicit );
}

bool
CProbe::MatchWithin(CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szSig, bool* pIsExplicit /* = NULL */)
{
	MatchCommon( m_ruleListWithin, m_plstPrefilterWithin, i_szPkgAndClass, i_szMethod, i_szSig, pIsExplicit );
}

//Match is made by this common method for normal match and within match	
bool
CProbe::MatchCommon( CFilterRuleList& i_ruleList, CFilterRuleList*	i_plstPrefilter, CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szSig, bool* pIsExplicit /* = NULL */)
{
	// Set up the isExplicit return value
	bool dummy;
	if (pIsExplicit == NULL) 
		pIsExplicit = &dummy;
	(*pIsExplicit) = false;

	// Split out the package from the class
	const char* szClass;
	char* pkg = strdup(i_szPkgAndClass);

	// First, convert dots to slashes in the incoming PkgAndClass name
	// TODO: Is this needed? Java will never send us a dotted name...)
	int len = strlen(pkg);
	for (int i = 0; i < len; i++) {
		if (pkg[i] == '.') 
			pkg[i] = '/'; 
	}

	// Now find the last slash - everything before it is the package name
	char* lastslash = strrchr(pkg, '/');
	if (lastslash != NULL) {
		*lastslash = '\0';
		szClass = lastslash + 1;
	}
	else {
		// No slashes, so package name is the empty string
		// and class name is the whole input string
		*pkg = '\0';
		szClass = i_szPkgAndClass;
	}

	// If there's no match of any rule, result will be "true"
	// because this is how the Hyades profiling tools' filter system works.
	// But we also report whether the match was explicit or implicit,
	// so the caller can tell the difference.
	bool result = true;
	CFilterRuleList::iterator iter;
	// Apply first tier of filtering
	if(NULL != i_plstPrefilter)
	{
		for (iter = i_plstPrefilter->begin(); iter != i_plstPrefilter->end(); iter++)
		{
			if ((*iter).Match(pkg, szClass, i_szMethod, i_szSig)) {
				CFilterRule& r = (*iter);
				if (r.m_action == CFilterRule::ACTION_INCLUDE) {
					result = true;
					(*pIsExplicit) = true;
					break;
				}
				else {
					// Matched, and the action is "exclude."
					result = false;
					(*pIsExplicit) = true;
					break;
				}
			}
		}
	}
	if(result)
	{	// Probe has passed the first tier. Apply the second tier.
		for (iter = i_ruleList.begin(); iter != i_ruleList.end(); iter++) {
			if ((*iter).Match(pkg, szClass, i_szMethod, i_szSig)) {
				CFilterRule& r = (*iter);
				if (r.m_action == CFilterRule::ACTION_INCLUDE) {
					result = true;
					(*pIsExplicit) = true;
					break;
				}
				else {
					// Matched, and the action is "exclude."
					result = false;
					(*pIsExplicit) = true;
					break;
				}
			}
		}
	}
	free(pkg);
	cout << "match : " << result << endl;
	return result;
}

//==============================================================================

//------------------------------------------------------------------------------
CProbeList::CProbeList()
{
}

//------------------------------------------------------------------------------
void	
CProbeList::AddProbe(CProbe* i_pprobe)
{
	push_back(i_pprobe);
}

//==============================================================================
// CBCIEngProbe implementation

//------------------------------------------------------------------------------
// Constructor
CBCIEngProbe::CBCIEngProbe()
{
	m_ipFinally = (IP_t)-1;
	m_ipCatchAll = (IP_t)-1;
}

//------------------------------------------------------------------------------
// Destructor
CBCIEngProbe::~CBCIEngProbe()
{
	// Delete the probes in m_probelst.
	for (CProbeList::iterator probeIter = m_probelst.begin();
		probeIter != m_probelst.end();
		probeIter++) 
	{
		delete (*probeIter);
	}
}

//------------------------------------------------------------------------------
//
// Helper function to match a single method in a class against all target rules
// in a probe.
//
// Returns true if this probe applies to this method.
//
// Callsite probes apply to all methods of all classes, in the sense that we have to
// call Instrument every time, to check for calls to the target function.
//
// The combination of class/method/sig can hit an "exclude" before an "include"
// in the first call to Match(). That isn't the end of the story.
//
// We also check for a match using the names of the interfaces that this
// class directly implements. But in that case, the default last rule
// is to EXCLUDE all interfaces, not include them. So a class won't
// be included just because it implements an interface that matches
// the final implicit include rule.
//
// This means a class which was excluded by its name
// can still be included by the interfaces it implements.
//
// But notice: if an interface matches an exclude rule, that's
// not enough to disqualify this class. 
//
// Otherwise a rule like "exclude package java*" would
// disqualify anybody who implements java.util.Map!
//

static bool probeAppliesToMethod(CProbe* pProbe,
								 CSTR className, 
								 CSTR methodName, 
								 CSTR methodSig,
								 CJClassBuilder* pClass)
{
	
	bool (CProbe::*matchFunction)(CSTR , CSTR , CSTR , bool* ) = NULL;
	
	//IF we are inside a callsite we have to check that the probes applies to the object doing the call
	//	and this is done using CProbe::MatchWithin
	//ELSE we are inside a entry/exit probe so we have to check if the probe applies to the called method
	//and this is done using the CPRobe::MAtch method 
	
	if (pProbe->IsCallsiteProbe()) {
		matchFunction = &CProbe::MatchWithin;
	}
	else {
		matchFunction = &CProbe::Match;
	}
	
		if ((pProbe->*matchFunction)(className, methodName, methodSig, NULL )) {
			return true;
		}
		else {
			CJInterfaces* pInterfaces = pClass->GetInterfaces();
			CJInterfaces::iterator interface_iter;

			for (interface_iter = pInterfaces->begin();
				 interface_iter != pInterfaces->end();
				 interface_iter++)
			{
				// Check against this interface.
				// Only explicit matches will make us return true from here.
				// See comments about interfaces above for why.
				CInterfaceInfo* pInterface = (*interface_iter);
				string interface_name = pClass->GetNameFromInterfaceIndex(pInterface->GetIndex());
				bool isExplicit;
				bool matchSucceeded = (pProbe->*matchFunction)(interface_name.c_str(), methodName, methodSig, &isExplicit);
				if (matchSucceeded && isExplicit) {
					return true;
				}
			}
		
	}

	// We only get here if we hit an "exclude" rule before we hit an "include"
	// rule for the class/method/sig, AND we hit an "exclude" rule before
	// hitting an "include" rule for every interface/method/sig combination.
	// (Implicit "include all" at the end of the rule set doesn't apply to interfaces.)

	return false;
}

static bool probeAppliesToClass(CProbe* pProbe, CJClassBuilder* pClass)
{
	if (pProbe->IsCallsiteProbe()) {
		return true;
	}
	else {
		string classNameStr = pClass->GetThisClassName();
		CSTR className = classNameStr.c_str();
		CJMethods* pMeths = pClass->GetMethods();
		CJMethods::iterator mIter;
		for (mIter = pMeths->begin(); mIter != pMeths->end(); mIter++) {
			CJMethodInfo* mInfo = (*mIter);
			string mName = (string)*(mInfo->GetName());
			string mSig = (string)*(mInfo->GetDescriptor());
			if (probeAppliesToMethod(pProbe, 
							className, 
							mName.c_str(), 
							mSig.c_str(), 
							pClass))
			{
				// If ANY method makes it through our filtering then
				// this probe applies to this class, so return true.
				return true;
			}
		}
	}
	return false;
}

//------------------------------------------------------------------------------
// Instrument class
// In:
//	i_pInClass	- pointer to the beginning of Java class
//	i_cbInClass	- size of the input class
// Out:
//	o_ppOutClass - address of pointer to the instrumented class
//	o_pcbOutClass - address of size of the instrumented class in bytes
//
// Returns:
//	true if we did instrumentation, otherwise false
//
// If you want to stub out instrumentation, replace
// this function with these lines:
//	*o_pcbOutClass = i_cbInClass;
//	*o_ppOutClass = m_pfnMalloc(*o_pcbOutClass);
//	memcpy(*o_ppOutClass, i_pInClass, *o_pcbOutClass);
//

bool
CBCIEngProbe::Instrument(void* i_pInClass, size_t i_cbInClass,
			 		     void** o_ppOutClass, size_t* o_pcbOutClass)
{
	bool bResult = true;
	CJMemStream		InStream;					// Memory stream
	CJStream		JStreamIn(&InStream);		// Java input stream
	InStream.Open(i_pInClass, i_cbInClass);
	CJClassBuilder* pClass = new CJClassBuilder;
	pClass->Read(JStreamIn);

	// We don't touch interfaces.
	if (pClass->GetAccessFlags() & ACC_INTERFACE) {
		*o_pcbOutClass = i_cbInClass;
		*o_ppOutClass = i_pInClass;
		delete pClass;
		return false;
	}

	string clName = pClass->GetThisClassName();

	// Allow the client that's driving us to veto instrumentation of this module
	if(NULL != m_pfnCallback 
		&& 0 != (m_wCBFlags & BCIENGINTERFACE_CALLBACK_MODULE)
		&& !m_pfnCallback(clName.c_str(), clName.length(), BCIENGINTERFACE_CALLBACK_MODULE))
	{
		*o_pcbOutClass = i_cbInClass;
		*o_ppOutClass = i_pInClass;
		delete pClass;
		return false;
	}

	// Reset the list of probes that should be examined for this module (probelstMod)
	m_probelstMod.clear();

	// Walk the list of probes. Reset the per-module (per-probed-class) state of
	// each one, namely the staticField reference.
	//
	// Also, add each one that applies to this module to m_probelstMod. That is
	// the list of probes that apply to this module.
	//
	// This loop results in identifying this module as being a "probed class" or not.
	// If a probe's target rule set matches any method of this class, then that probe
	// is considered as applying to this class, and its class-wide effects
	// (staticInitializer, staticField) will be applied to this class as well.
	//

	for(CProbeList::iterator itrProbes = m_probelst.begin(); itrProbes != m_probelst.end(); itrProbes++)
	{
		CProbe* pProbe = (*itrProbes);
		pProbe->SetStaticFieldRef(0);
		if (probeAppliesToClass(pProbe, pClass)) {
			m_probelstMod.push_back(pProbe);
		}
	}

	if (m_probelstMod.size() == 0)  {
		// no probes apply to this class
		*o_pcbOutClass = i_cbInClass;
		*o_ppOutClass = i_pInClass;
		delete pClass;
		return false;
	}

	// Starting with the call to Open, we don't need to delete pClass any more:
	// The module owns it.
	CModuleJ* pModuleJ = new CModuleJ;
	pModuleJ->Open(pClass, true);
	pModuleJ->SetAccessFlags(pClass->GetAccessFlags());
	pModuleJ->Parse();

	// If we were asked to, then dump the module before and after instrumentation
	const char *dump_output_file;
	if ((dump_output_file = getenv("PROBEKIT_DUMP_FILE")) != NULL) {
#if defined(MVS) || defined(__OS400__)
		ostrstream output_stream;
#else
		ofstream output_stream(dump_output_file, ios::app);
#endif
		output_stream << "=========== DUMP BEFORE INSTRUMENTATION" << endl;
		pModuleJ->Dump(output_stream);

#if defined(MVS) || defined(__OS400__)
		/* On EBCDIC systems, convert ASCII to EBCDIC */
		char* data = output_stream.str();
		__atoe(data);
		ofstream ofs(dump_output_file, ios::app);
		ofs << data;
		ofs.close();
#else
		output_stream.close();
#endif
	}

	Instrument(pModuleJ);

	// TODO: notice here if we did not actually instrument anything.
	// Other filtering should have stopped method probes that didn't apply
	// to this module, but we still ran callsite probes against this
	// module and maybe we didn't actually do anything.
	// In that case we should set out == in and return without calling emit.

	pModuleJ->Emit();

	// Tell the client that's driving us that we did something to this module
	// (so the client can report it to a log somewhere, based on debug info, for example)
	if(NULL != m_pfnCallback 
		&& 0 != (m_wCBFlags & BCIENGINTERFACE_CALLBACK_MODULE_INSTR))
	{
		m_pfnCallback(clName.c_str(), clName.length(), BCIENGINTERFACE_CALLBACK_MODULE_INSTR);
		// ignore the return value: there's nothing the client can do about it now
	}

	if (dump_output_file != NULL) {
#if defined(MVS) || defined(__OS400__)
		ostrstream output_stream;
#else
		ofstream output_stream(dump_output_file, ios::app);
#endif
		output_stream << "=========== DUMP AFTER INSTRUMENTATION" << endl;
		pModuleJ->Dump(output_stream);

#if defined(MVS) || defined(__OS400__)
		/* On EBCDIC systems, convert ASCII to EBCDIC */
		char* data = output_stream.str();
		__atoe(data);
		ofstream ofs(dump_output_file, ios::app);
		ofs << data;
		ofs.close();
#else
		output_stream.close();
#endif
	}

	*o_pcbOutClass = pClass->GetSize();
	*o_ppOutClass = m_pfnMalloc(*o_pcbOutClass);

	CJMemStream		OutStream;					// Memory stream
	OutStream.Open(*o_ppOutClass, *o_pcbOutClass);
	CJStream		JStreamOut(&OutStream);		// Java output stream
	pClass->Write(JStreamOut);
	delete pModuleJ;
	return bResult;
}


//------------------------------------------------------------------------------
const	CProbeList& 
CBCIEngProbe::GetProbes() const
{
	return m_probelst;
}

//------------------------------------------------------------------------------
void	
CBCIEngProbe::AddProbe(CProbe* i_pprobe)
{
	m_probelst.AddProbe(i_pprobe);
}

//------------------------------------------------------------------------------
CProbe* 
CBCIEngProbe::CreateProbe() const
{
	CProbe* pProbe = new CProbe();
	return pProbe;
}

//------------------------------------------------------------------------------
int				
CBCIEngProbe::AddPrefilter(CFilterRuleList* i_plstRules)
{
	m_prefilters.push_back(i_plstRules);
	return m_prefilters.size() - 1;
}

//------------------------------------------------------------------------------
int				
CBCIEngProbe::AddPrefilterWithin(CFilterRuleList* i_plstRules)
{
	m_prefiltersWithin.push_back(i_plstRules);
	return m_prefiltersWithin.size() - 1;
}

//- Protected ------------------------------------------------------------------
//------------------------------------------------------------------------------
// Instrument
// In: 
//	i_pmod	- pointer to the module to instrument
// Out:
//	-
// Returns:
//
// Throws:
//	CBCIEngProbeException
//

void
CBCIEngProbe::Instrument(CModule* i_pmod)
{
	CModuleJ* pmodj= (CModuleJ*)i_pmod;

	// Mark module as instrumented
	pmodj->AddStringAttrib("Instrumented", "BCIEngProbe");

	// InstrumentMethod will go through the probes in m_probelstMod, the
	// list of probes to consider applying to this module.

	if(m_probelstMod.size() == 0) {
		// No probes apply to this class.
		// This should not happen - in this case nobody should have called this function.
		return;
	}

	// For each probe that applies to this module,
	// See if this probe has a staticField element, synthesize the reference if so.
	// Note: this would be a mistake for interfaces, but interfaces don't get here.
	CProbeList::iterator probeiter;

	for (probeiter = m_probelstMod.begin();
		 probeiter != m_probelstMod.end();
		 probeiter++)
	{
		CProbe* pProbe = (*probeiter);
		if (pProbe->GetStaticFieldType() != NULL) {
			unsigned fieldRef;
			fieldRef = synthesize_static_field(pmodj, pProbe);
			pProbe->SetStaticFieldRef(fieldRef);
		}
	}

	// Build the strings for the module's line number table and method name list.
	// Do it now, before we do any instrumentation and mess up the ordering.
	// TODO: optimize: save the time and memory required to do this if nobody wants it.
	m_lineTableString = BuildLineTableString(pmodj);
	m_methodNamesString = BuildMethodNamesString(pmodj);

	// Instrument all methods in this module.
	CMethods* pmethods = i_pmod->GetMethods();
	for(CMethods::iterator itrMeth = pmethods->begin(); itrMeth != pmethods->end(); itrMeth++)
	{
		InstrumentMethod(*itrMeth); 
	}

	// Process any staticInitializer fragments that matched this class,
	// and default initializers for staticField elements in probes that matched.
	HandleStaticInitializers(pmodj);
}

//------------------------------------------------------------------------------
// EmitInitializerForSerialVersionUID
//
// This function emits instructions which will initialize the 
// (synthesized) serialVersionUID field to its proper value.
//
// We do not know the proper value at instrumentation time. What
// we know is the byte array from which the proper value can be computed.
//
// To get this byte array into the Java program, this function constructs
// a string whose chars are the desired bytes. Then we decode the string
// to populate a byte array.
//
// IMPORTANT: This code is supposed to be called just once, 
// from a specific place in the program: in HandleStaticInitializers,
// when we decide we have to synthesize a <clinit> method (and
// certain other conditions are met).
//
// The method pointed to by pStaticCtorMethod should be empty
// when this function starts.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//

/*

  This function writes byte codes into the <clinit> method.
  The byte codes pull a string from the constant pool,
  and convert the string into a byte array. The characters
  in the string were generated by BuildSUIDByteArray().

  Then we pass the byte array to MessageDigest.digest() and get
  the SHA digest value for the byte array; finally we pull the
  first eight bytes of that digest as a little-endian long and
  store that value into the serialVersionUID variable.

  The whole point is that we don't want to implement the SHA
  algorithm for legal reasons, so we build the byte array and then call the JVM's
  MessageDigest system to compute SHA over the proper array of bytes.
  (Implementing SHA would mean bringing our component under 
  export restrictions.)

  [If we get legal permission to implement SHA (and find out what
  document we are allowed to refer to as a basis for doing so),
  then we should just implement it here and write the result
  as the initializer for the long, removing all this complex logic.]

  Even though MessageDigest.getInstance can throw an exception,
  we don't need to emit catch logic: the VM spec (2nd ed) says
  exception specifications are enforced at compile time, not by the JVM.
  Since getInstance won't really throw when given "SHA" as its arg,
  we should be safe.
	
  Here is a pseudo-Java form of the function being generated here:

	String string = "xxx";			// the generated string
	byte[] array = new byte[LEN];	// length is known here, at generation time
	int counter = 0;
	long result = 0;

	loop1_label:
		array[counter] = (byte)string.charAt(counter);
		++counter;
		if (counter < len) goto loop1_label;

	// Call MessageDigest to compute the SHA on the array.
	// Store the result back in the "array" local variable.
	array = java.security.MessageDigest.getInstance("SHA").digest(array);

	// Now pull the first 8 bytes from the array as a little-endian long
	counter = 7;
	loop2_label:
		result = (result << 8) + (long)(array[counter] & 0xff);
		--counter;
		if (counter >= 0) goto loop2_label;

	serialUIDFieldRef = result;

*/


void
CBCIEngProbe::EmitInitializerForSerialVersionUID(
				CMethod* pStaticCtorMethod, 
				CSerialVersionUIDHelper* pSerialVersionHelper,
				unsigned serialUIDFieldRef)
{
	int len = pSerialVersionHelper->GetLength();
	u1* bytes = pSerialVersionHelper->GetBytes();

	// Create a string whose characters are the bytes of the array, UTF8-encoded.
	// A byte in the range 0x80-0xff will be encoded as multiple bytes in the UTF8 string.
	//
	// This is inefficient: we could look at pairs of bytes in the array as shorts, 
	// then look at the shorts as characters, and encode the characters using UTF8.
	// The String would be half the size, and we could call String.toByteArray 
	// to recover the byte array. But we'd have to worry about padding an odd-length
	// original byte array, and the String would contain invalid Unicode chars, which makes
	// me worry that toByteArray might throw an exception - or something. Better to stay
	// with what's safe.

	// Allocate enough bytes for 2x the byte array length. The encoded string 
	// will not be more than twice as long as the original byte array.
	u1* encodedBytes = new u1[(len * 2) + 1];
	int out = 0;

	for (int in = 0; in < len; in++) {
		u1 c = bytes[in];
		if (c > 0 && c < 128) {
			// standard one-byte encoding
			encodedBytes[out++] = c;
		}
		else {
			// Two-byte encoding for zero and 128-255
			u1 highTwo = (c >> 6);
			encodedBytes[out++] = 0xC0 | highTwo;
			u1 lowSix = (c & 0x3f);
			encodedBytes[out++] = 0x80 | lowSix;
		}
	}
	// Null-terminate the UTF8 encoded string
	encodedBytes[out] = '\0';

	// Define our local variables. 
	// The <clinit> method started out empty, not declaring any.
	unsigned string_local = 0;
	unsigned array_local = 1;
	unsigned counter_local = 2;
	unsigned result_local = 3;	// remember: takes two slots!
	unsigned num_local_slots = 5;

	// Set the "MaxLocals" of this method to the number of slots needed.
	// We assume we are the first/only bit of code allocating locals
	// in this <clinit> method.
	CCodeAttribute* pCode = ((CMethodJ*)pStaticCtorMethod)->GetCodeAttribute();
	pCode->SetMaxLocals(num_local_slots);

	// Magic numbers: the "original IP" values for
	// two CInsBlks we will create that are branch targets.
	// These numbers could be anything - it doesn't matter, as long
	// as they're different, and we use them consistently in making labels etc.
	unsigned loop1_target_ip = 5;
	unsigned loop2_target_ip = 10;

	// Create an insertion block to hold the first part of
	// our initialization logic. Then initialize a ctxt from it.

	CInsBlock* pNewBlk = new CInsBlock(-1);
	CInstructions::iterator insiter = pNewBlk->GetInstructions()->begin();
	CProbeInsertionContext ctxt(this, pStaticCtorMethod);
	ctxt.pIns = pNewBlk->GetInstructions();
	ctxt.itrIns = &insiter;

	// The first instructions initialize "result" to zero.
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_lconst_0));
	ctxt.Insert(CInsSetJ::Create_lstore(result_local));

	// Create the string in the constant pool, and save it in "string"
	CCPUtf8Info utf8Name((char*)encodedBytes);
	emit_ldc_for_string(ctxt, utf8Name);
	ctxt.Insert(CInsSetJ::Create_astore(string_local));
	delete[] encodedBytes;	// we're finished with the encodedBytes array now

	// Create a new byte array of the proper size, store in "array"
	ctxt.Insert(CInsSetJ::Create_push_constant(len));
	ctxt.Insert(CInsSetJ::Create_newarray(T_BYTE));
	ctxt.Insert(CInsSetJ::Create_astore(array_local));

	// Set "counter" = 0
	ctxt.Insert(CInsSetJ::Create_push_constant(0));
	ctxt.Insert(CInsSetJ::Create_istore(counter_local));

	// Finish off the previous CInsBlock and start a new one
	// We need a new CInsBlock because we're going to create
	// a branch back to our next instruction, the top of the loop.
	//
	// Magic number: the argument to the "CInsBlock" constructor is used as the
	// target of the branch we will synthesize. It doesn't matter what the
	// value is as long as no other block in this function has the same value.
	// (Normally it's the IP of the first instruction in this block, and hence
	// the target IP of branches that branch to this block.)

	CInsBlocks* pInsBlocks = pStaticCtorMethod->GetInsBlocks();
	pInsBlocks->push_back(pNewBlk);

	pNewBlk = new CInsBlock(loop1_target_ip);
	insiter = pNewBlk->GetInstructions()->begin();
	ctxt.pIns = pNewBlk->GetInstructions();
	ctxt.itrIns = &insiter;
	CLabels* plabels = ctxt.pMethodJ->GetLabels();
	plabels->AddLabel(loop1_target_ip);

	// Loop over the string and populate the array.
	// Emit code for array[counter] = string.charAt(counter);

	ctxt.Insert(CInsSetJ::Create_aload(array_local));	// get the array reference
	ctxt.Insert(CInsSetJ::Create_iload(counter_local));	// get the counter
	ctxt.Insert(CInsSetJ::Create_aload(string_local));	// get the string
	ctxt.Insert(CInsSetJ::Create_iload(counter_local));	// get the counter again

	// call charAt
	unsigned stringClassRef = get_class_ref(ctxt, "java/lang/String");
	unsigned charAtMethodRef = get_method_ref(ctxt, stringClassRef, "charAt", "(I)C");
	ctxt.Insert(CInsSetJ::Create_invokevirtual(charAtMethodRef));	

	// Here's what's left on the stack: array, counter, char
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_i2b));		// Convert the char to a byte
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_bastore));	// Store the byte in the array

	// (stack is now empty)
	// Emit counter++
	ctxt.Insert(CInsSetJ::Create_iinc(counter_local, 1));// increment the counter

	// Emit "if (counter < len) goto loop1_target_ip"
	ctxt.Insert(CInsSetJ::Create_iload(counter_local));	// get the counter
	ctxt.Insert(CInsSetJ::Create_push_constant(len));	// push the length constant
	ctxt.Insert(CInsSetJ::Create_if(JVMI_if_icmplt, loop1_target_ip));	// if less, loop.

	// The next instruction follows a conditional branch, which would
	// ordinarily mean it starts a new CInsBlk, but since we're not
	// going to do more insertion on this method it doesn't matter.

	/* {
		// Debug logic: store the array in a newly-synthesized field
		// called "byteArray" of type "Object." You can write a probe
		// that uses reflection to examine this value.
		CModuleJ* pmodj = (CModuleJ*)ctxt.pMethodJ->GetModule();
		unsigned fieldAccess = ACC_PUBLIC | ACC_STATIC;
		CCPFieldrefInfo* pFieldRefInfo;
		pFieldRefInfo = pmodj->CreateFieldRef(
			fieldAccess,
			"byteArray",
			CJavaType(CJavaType::J_CLASS, 0, "java/lang/Object"));
		unsigned byteArrayFieldRef = pFieldRefInfo->GetCpIndex();
		ctxt.Insert(CInsSetJ::Create_aload(array_local));
		ctxt.Insert(CInsSetJ::Create_putstatic(byteArrayFieldRef));
	} */

	// Next step: now that we have the byte array, create
	// a MessageDigest call digest.digest(bytes);
	unsigned messageDigestClassRef = get_class_ref(ctxt, "java/security/MessageDigest");
	unsigned getInstanceMethodRef = get_method_ref(
		ctxt, 
		messageDigestClassRef, 
		"getInstance", 
		"(Ljava/lang/String;)Ljava/security/MessageDigest;");
	CCPUtf8Info utf8SHA("SHA");
	emit_ldc_for_string(ctxt, utf8SHA);
	ctxt.Insert(CInsSetJ::Create_invokestatic(getInstanceMethodRef));

	// Now the digest object is on the stack
	// Push the array and call "digest"
	ctxt.Insert(CInsSetJ::Create_aload(array_local));

	unsigned digestMethodRef = get_method_ref(
		ctxt,
		messageDigestClassRef,
		"digest",
		"([B)[B");
	ctxt.Insert(CInsSetJ::Create_invokevirtual(digestMethodRef));

	// Now the digest is on the stack as a byte array.
	// Store it back into array_local -- we can reuse the
	// local variable since we're finished with the old array.
	ctxt.Insert(CInsSetJ::Create_astore(array_local));

	// Convert the first eight bytes of the digest to a long 
	// using little-endian ordering.
	// Remember, result_local is already zero.
	// Java logic looks like this:
	//	counter = 7;
	//	do {
	//		result = (result << 8) + (long)(array[counter] & 0xff);
	//		--counter;
	//	} while (counter >= 0);
	//

	// Emit counter = 7;
	ctxt.Insert(CInsSetJ::Create_push_constant(7));
	ctxt.Insert(CInsSetJ::Create_istore(counter_local));

	// End the previous CInsBlock and start a new one. 
	// Give the new one loop2_label as its "original IP" so it can be a branch target
	pInsBlocks = pStaticCtorMethod->GetInsBlocks();
	pInsBlocks->push_back(pNewBlk);

	pNewBlk = new CInsBlock(loop2_target_ip);
	insiter = pNewBlk->GetInstructions()->begin();
	ctxt.pIns = pNewBlk->GetInstructions();
	ctxt.itrIns = &insiter;
	plabels = ctxt.pMethodJ->GetLabels();
	plabels->AddLabel(loop2_target_ip);

	// In the stack diagram comments below, the top of the stack
	// is on the right, represented by the "--" marker.
	// r means "the value of result_local" 
	// r' means "result_local shifted left by eight bits"
	// r'' means "r' plus the next byte's value"

	ctxt.Insert(CInsSetJ::Create_lload(result_local));	//                 r --
	ctxt.Insert(CInsSetJ::Create_push_constant(8));		//               r 8 --
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_lshl));	//                r' --
	ctxt.Insert(CInsSetJ::Create_aload(array_local));	//          r' array --
	ctxt.Insert(CInsSetJ::Create_iload(counter_local));	//  r' array counter --
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_baload));	//      r' next_byte --
	ctxt.Insert(CInsSetJ::Create_push_constant(0xff));	// r' next_byte 0xff --
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_iand));	//    r' next_as_int --
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_i2l));		//   r' next_as_long --
	ctxt.Insert(CInsSetJ::Create_simple(JVMI_ladd));	//               r'' --
	ctxt.Insert(CInsSetJ::Create_lstore(result_local));	//                   --

	// Stack is now empty. Decrement the counter, test and loop.
	ctxt.Insert(CInsSetJ::Create_iinc(counter_local, -1));
	ctxt.Insert(CInsSetJ::Create_iload(counter_local));	// counter
	ctxt.Insert(CInsSetJ::Create_if(JVMI_ifge, loop2_target_ip));

	// end of loop.
	// Now push computed result value and store it in the static field
	ctxt.Insert(CInsSetJ::Create_lload(result_local));
	ctxt.Insert(CInsSetJ::Create_putstatic(serialUIDFieldRef));

	// Finish off the previous CInsBlock and return.
	pInsBlocks = pStaticCtorMethod->GetInsBlocks();
	pInsBlocks->push_back(pNewBlk);
}

//------------------------------------------------------------------------------
// moduleNeedsSerialVersionUIDField: return true if this module (class) needs
// us to synthesize a serialVersionUID field.
//
// Classes do NOT need the field if:
//	They already have one, or
//	Their direct base is Object and they implement no interfaces.
//
// If we can't prove that the class does NOT need the field,
// then we have to return true and ultimately synthesize one.
//
// Note: we return false if the class has ANY field called
// serialVersionUID, even if it's not "private static final long"
// as it is supposed to be. This is necessary because we can't
// synthesize a second field with that name. In that case the user
// might experience broken serialization because of Probekit.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//

static bool
moduleNeedsSerialVersionUIDField(CModuleJ* pModJ)
{
	CJClassFile &c = pModJ->GetClass();
	CConstPool* cpool = c.GetConstPool();
	CJFields* pFields = c.GetFields();

	for (int i = 0; i < pFields->size(); i++) {
		CFieldInfo* pFieldInfo = (*pFields)[i];
		u2 nameInd = pFieldInfo->GetNameInd();
		CCPUtf8Info* nameUTFInfo = (CCPUtf8Info*)(*cpool)[nameInd];
		u1* bytes = nameUTFInfo->GetBytes();
		u2 length = nameUTFInfo->GetLength();

		// Magic number 16 below is strlen(serialVersionUID)
		if (length == 16) {
			if (strncmp((char*)bytes, "serialVersionUID", 16) == 0) {
				// If you've already got the field, you don't need another one.
				// See the note above about not checking
				// the other characteristics of this field.
				return false;
			}
		}
	}

	// OK, no existing field.
	// Now see about this guy's base class and interfaces

	CJInterfaces* pInterfaces = c.GetInterfaces();
	if (pInterfaces->size() != 0) {
		// Implements some interfaces.
		// Since we can't know whether they are serializable,
		// we have to assume they are, and return true.
		return true;
	}

	u2 superclassIndex = c.GetSuperClass();
	CCPUtf8Info* superclassNameUTFInfo = cpool->GetClass(superclassIndex);
	u1* bytes = superclassNameUTFInfo->GetBytes();
	u2 length = superclassNameUTFInfo->GetLength();
	
	// Magic number 16 is strlen("java/lang/Object")
	if (length == 16) {
		if (strncmp((char*)bytes, "java/lang/Object", 16) == 0) {
			// Direct base is Object, not some possibly-streamable class
			// Therefore, combined with the other facts above,
			// we do not need a serialVersionUID field.
			return false;
		}
	}

	// Base class isn't java/lang/Object,
	// so it might be streamable,
	// so we need to add the field.
	return true;
}


// ---------------------------------------------------------------------
// HandleStaticInitializers
//
// If any static initialization logic is needed, emit it into <clinit>,
// synthesizing that method if necessary.
//
// We insert three kinds of code into static initializers: 
// the code to default-construct static fields created by staticField elements,
// the code that calls staticInitializer fragments,
// and the code to initialize the serialVersionUID field, if we had to build one.
//
// We know we need the first type when a probe's GetStaticFieldRef is not zero.
// We know we need the second type because we scan m_probelstMod looking
// for those fragments in the probes that apply to this module.
// And we need the third type if we had to synthesize a new <clinit> method,
// and some other conditions are met.
//
// After determining whether any of this is needed and synthesizing a <clinit>
// if necessary, we create a new CInsBlock to hold the new instructions,
// set up a CProbeInsertionContext to refer to the right location, and
// insert the necessary instructions. This new block goes at the top of
// the static constructor.
//
// If we find the class doesn't already have a <clinit> method and we have
// to add one, we also consider adding a new serialVersionUID static field.
// This is needed if the class gets serialized and uses the default computation
// of a SUID: by adding a method (<clinit>) we're changing the SUID. So we compute what
// the default SUID would have been without our changes, and create and
// initialize an explicit static to hold it. This is never necessary in
// classes that already have a serialVersionUID field, or those
// which extend java.lang.Object directly and do not implement any interfaces.
// (Since any interface might be a subinterface of Serializable, we have
// to assume that implementing any interfaces makes a class a candidate for this.)
//
// SPECIAL NOTE: the byte array that controls the serialVersionUID value
// must be built from the class definition (name, fields, methods, etc.) as it
// looked before instrumentation. As long as Probekit doesn't add any new
// methods besides <clinit>, and no new fields besides private statics
// and private transients, we can build that byte array here.
// But if Probekit ever adds other kinds of things or changes method
// signatures, we'll have to capture the shape before instrumentation
// and save it until it's needed here.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//
// The staticFields are default-constructed first; then the
// staticInitializer fragments go in. Remember that an existing
// <clinit> method might already have undergone method entry insertion
// and executableUnit insertion - we want static init stuff
// to execute before either of those.
//
// Argument passing in this case is simple. staticInitializer
// fragments can't use troublesome things like "thisObj" or "argsList."
//

void
CBCIEngProbe::HandleStaticInitializers(CModuleJ* i_pmodj)
{
	bool any_static_fields = false;
	bool need_serial_uid = false;
	unsigned serialUIDFieldRef;
	CSerialVersionUIDHelper* pSerialVersionHelper = NULL;
	CProbeList::iterator probeiter;

	m_staticInitializerFragments.clear();

	for (probeiter = m_probelstMod.begin();
		 probeiter != m_probelstMod.end();
		 probeiter++)
	{
		CProbe* pProbe = (*probeiter);
		// See if this probe has a staticField element
		if (pProbe->GetStaticFieldRef() != 0) {
			any_static_fields = true;
		}

		// Scan for static initializer fragments, add them to m_staticInitializerFragments
		// TODO: this shouldn't be a member variable, it's only used in this function
		CProbeFragmentList& this_probes_frags = pProbe->GetProbeFragmentList();
		CProbeFragmentList::iterator itrfrag;
		for(itrfrag = this_probes_frags.begin(); 
			itrfrag != this_probes_frags.end(); 
			itrfrag++)
		{
			CProbeFragment* pfrag = (*itrfrag);
			int nType = pfrag->GetType();
			if (nType == CProbeFragment::PROBE_STATICINITIALIZER) {
				m_staticInitializerFragments.insert(pfrag);
			}
		}
	}

	if (any_static_fields || m_staticInitializerFragments.size() != 0) {
		// Loop through the module looking for <clinit>
		// We'll synthesize one if we don't find one
		CMethod* pStaticCtorMethod = NULL;
		CMethods* pmeths = i_pmodj->GetMethods();
		for (int i = 0; i < pmeths->size(); i++) {
			CMethod* pmeth = (*pmeths)[i];
			const char* mname = pmeth->GetName();
			if (strcmp(mname, "<clinit>") == 0) {
				pStaticCtorMethod = pmeth;
				break;
			}
		}

		if (pStaticCtorMethod == NULL) {
			// There is no <clinit> already, and yet we need one.
			// So we will add one. But doing so will change
			// the auto-computed serialization UID (SUID) for the class.
			// So we have to take care of that.
			//
			// If the class already has an explicit serialVersionUID
			// field, we don't need to do anything.
			//
			// If the class inherits directly from java.lang.Object and
			// doesn't implement any interfaces of its own, then it isn't
			// streamable and we don't need to do anything.
			//
			// Otherwise it *might* be streamable. (We can't tell, given that
			// we don't see and track the class and interface hierarchies.)
			// So to protect streaming, we have to give this class an
			// explicit serialVersionUID field, and initialize it to the
			// same value the automatic computation would have used
			// for the uninstrumented class.
			//
			// Here is one odd corner case: if the class already has
			// *any* field called serialVersionUID, we can't add a new one,
			// even if the one that's there doesn't have the right
			// type and access (private static final long). Such a
			// program has a broken attempt at making streaming robust,
			// but in fact streaming would work within limits. After instrumentation
			// by Probekit, that program would no longer be able to read streams
			// created by uninstrmented code, and vice-versa. 
			// We can't help it - the user defeated us. Such is life.
			//
			// We have to create the serialVersionHelper *before* we
			// synthesize the new <clinit> method - otherwise the helper will
			// see the new method and that would change the computation.
			//
			// [All areas of code related to serialVersionUID can be found by looking
			// for that word in the comments.]
			//

			if (moduleNeedsSerialVersionUIDField(i_pmodj)) {
				need_serial_uid = true;

				// Compute the byte array contents that we are to run SHA over.
				pSerialVersionHelper = new CSerialVersionUIDHelper(i_pmodj);

				// Synthesize the new serialVersionUID field, which we will initialize
				serialUIDFieldRef = synthesize_serialVersionUID_field(i_pmodj);
			}

			// Now synthesize a public, static <clinit> method.
			// Give it an instruction block, which contains at least a "return"
			// instruction and maybe logic to compute the serialVersionUID value.
			pStaticCtorMethod = i_pmodj->CreateMethod(ACC_STATIC, "<clinit>", "()V");

			// If needed, emit the initializer for the new serialVersionUID field.
			if (need_serial_uid) {
				// Insert the byte codes that will initialize the serialVersionUID field.
				EmitInitializerForSerialVersionUID(pStaticCtorMethod, pSerialVersionHelper, serialUIDFieldRef);
				delete pSerialVersionHelper;
			}

			// Whether or not we had to emit the serialVersionUID
			// initializer above, emit a final CInsBlock containing a "return" instruction
			// into the newly-synthesized <clinit> method.
			CInsBlocks* pInsBlocks = pStaticCtorMethod->GetInsBlocks();
			CInsBlock* pBlk = new CInsBlock(-1);
			pBlk->AddInstruction(CInsSetJ::Create_simple(JVMI_return));
			pInsBlocks->push_back(pBlk);
		}

		// Now we definitely have a (complete, valid) static constructor.
		// If necessary, it contains logic that initializes a newly-synthesized
		// serialVersionUID field, and in any case it ends with a "return" instruction.

		// Create a new CInsBlock to hold the new instructions,
		// and create a CProbeInsertionContext for inserting instructions
		// into the new block.
		CInsBlock* pNewBlk = new CInsBlock(-1);
		CInstructions::iterator insiter = pNewBlk->GetInstructions()->begin();
		CProbeInsertionContext ctxt(this, pStaticCtorMethod);
		ctxt.pIns = pNewBlk->GetInstructions();
		ctxt.itrIns = &insiter;

		// Now walk the list of probes looking for all the ones
		// with nonzero GetStaticFieldRefs. Emit default constructor
		// logic for each one.

		for (probeiter = m_probelstMod.begin();
			 probeiter != m_probelstMod.end();
			 probeiter++)
		{
			CProbe* pProbe = (*probeiter);
			if (pProbe->GetStaticFieldRef() != 0) {
				// Instruction sequence is:
				//		new classRef
				//		dup
				//		invokespecial classRef <init> ()V
				//		putstatic field_ref
				unsigned classRef = get_class_ref(ctxt, pProbe->GetStaticFieldType());
				unsigned ctor_ref = get_method_ref(ctxt, classRef, "<init>", "()V");
				unsigned fieldRef = pProbe->GetStaticFieldRef();
				ctxt.Insert(CInsSetJ::Create_new(classRef));
				ctxt.Insert(CInsSetJ::Create_simple(JVMI_dup));
				ctxt.Insert(CInsSetJ::Create_invokespecial(ctor_ref));
				ctxt.Insert(CInsSetJ::Create_putstatic(fieldRef));
			}
		}

		// Now walk the set of staticInitializer probe refs, letting each
		// one do its insertion. We have to add each one's external reference
		// to the module before we can call its Instrument method.
		set<CProbeFragment*>::iterator fragiter;
		for (fragiter = m_staticInitializerFragments.begin();
			 fragiter != m_staticInitializerFragments.end();
			 fragiter++)
		{
			CProbeFragment *pFrag = (*fragiter);
			i_pmodj->AddExtRef(*(pFrag->GetExtRef()));
			// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
			ctxt.staticFieldRef = pFrag->GetParentProbe()->GetStaticFieldRef();
			pFrag->Instrument(ctxt);
		}

		// Finally, add the new block to the head of the <clinit> method
		CInsBlocks* methBlocks = pStaticCtorMethod->GetInsBlocks();
		methBlocks->insert(methBlocks->begin(), pNewBlk);

	}
}


// ---------------------------------------------------------------------
// BuildLineTableString
//
// Call this method after calling CModule.Parse() and before
// any BCI has been done. It walks the line-number tables
// and CInsBlocks of every method to build the lineNumberTables
// string we need to pass to any probe that requests it.
//

string
CBCIEngProbe::BuildLineTableString(CModuleJ* i_pmodj)
{
	CMethods* pMethods = i_pmodj->GetMethods();
	int num_methods = pMethods->size();

	CLineTableEncoder lineTableEncoder;

	for (int methodNumber = 0; methodNumber < num_methods; methodNumber++) {
		CMethod* pMethod = (*pMethods)[methodNumber];
		CLineNumbers* pLineNumbers = pMethod->GetLineNumbers();
		CInsBlocks* pInsBlocks = pMethod->GetInsBlocks();
		int num_eus = pInsBlocks->size();

		// Start of a new method.
		// Emit a comma except for the first one.
		if (methodNumber != 0) 
			lineTableEncoder.append(",");

		CMethodJ* pMethodJ = (CMethodJ*)pMethod;
		CCodeAttribute* pCodeAttr = pMethodJ->GetCodeAttribute();
		if(pCodeAttr == NULL || pCodeAttr->GetLineNumbers() == NULL) {
			// no code attribute or no line number table
			// Emit a single zero as the "line number" for the
			// one and only (placeholder) executableUnit in this method.
			lineTableEncoder.appendLineNumber(0);
		}
		else {
			// This algorithm sees every line table entry as having
			// a range of IPs it applies to. There is a phantom
			// entry at the beginning that covers the range from zero
			// to the last ip before first true entry's ip.
			//
			// When we enter the range of a line table entry,
			// we put its line number in current_line_number.
			// Then we advance to the next line table entry
			// and set next_target_ip to the start of that NEXT range.
			// As long as the ip of the current eu is less than
			// that next range start ip, we're still in range
			// of the current line.
			//
			// When there are no more line number entries, the
			// next_target_ip becomes 0xffffffff so the last line
			// number applies to all eus to the end of the function.

			CLineNumberTableAttribute* plinenums = pCodeAttr->GetLineNumbers();
			CLineNumberTable& table = plinenums->GetLineNumberTable();
			u2 u2Leng = plinenums->GetTableLength();

			int current_line_number = 0;	// zero means "not available"
			int eu_number = 0;
			CLineNumberTable::iterator table_iter = table.begin();
			IP_t next_target_ip = (*table_iter)->GetStartPC();

			for (eu_number = 0; eu_number < num_eus; eu_number++) {
				IP_t this_eu_ip = ((*pInsBlocks)[eu_number])->GetOrigIP();
				if (this_eu_ip >= next_target_ip) {
					// This insblock is in a new line number's range
					current_line_number = (*table_iter)->GetLineNumber();
					table_iter++;
					if (table_iter != table.end()) {
						next_target_ip = (*table_iter)->GetStartPC();
					}
					else {
						// The current range is the last range.
						next_target_ip = pCodeAttr->GetCodeLength() + 1;
					}
				}

				// Now current_line_number is the proper line number for this eu.
				lineTableEncoder.appendLineNumber(current_line_number);
			} // end of "for each eu" loop
		} // end if "method has line info"
		// We've handled all the EUs for this method.
	} // end of "for each method" loop

	return lineTableEncoder;
}


//------------------------------------------------------------------------
// BuildMethodNamesString
//

string
CBCIEngProbe::BuildMethodNamesString(CModule* i_pmod)
{
	string str;
	CMethods* pmeths = i_pmod->GetMethods();
	int num_meths = pmeths->size();

	for (int i = 0; i < num_meths; i++) {
		CMethodJ* pmethj = (CMethodJ*)((*pmeths)[i]);
		CSTR name = pmethj->GetName();
		CSTR sig = pmethj->GetSignature();
		if (i > 0) {
			str.append("+");
		}
		str.append(name);
		str.append(sig);
	}
	return str;
}

//------------------------------------------------------------------------
// BlockInsertionStash helper class
//
// Use this class to hold on to new CInsBlocks you create that you
// are going to want to insert into a method, but that you don't
// want to insert right now because you don't want them to be seen
// by other instrumentation passes.
//
// It's used by callsite insertion for the "aftercall" part.
// The invoke instruction is at the end of an insertion block,
// and the "aftercall" insertion is performed in a new CInsBlock
// which has to follow the original. But if we link it in to the method
// too soon, it'll be seen by other parts of the insertion engine,
// and we don't want that.
//
// This is also used for entry insertion, to hold the new first block
// of the method.
//
// The Add method takes an old block pointer and a new block pointer.
// When you call PerformBlockInsertions, each new block will be 
// inserted AFTER the corresponding old block.
//
// If the "old block" pointer is NULL, it means you want to insert
// the new block at the top of the function, before the first original block.
//
 
class BlockInsertionStash : private vector<pair<CInsBlock*, CInsBlock* > >
{
public:
	// Record the fact that we want to insert newBlock after oldBlock
	void Add(CInsBlock* oldBlock, CInsBlock* newBlock) {
		push_back(pair<CInsBlock*, CInsBlock*>(oldBlock, newBlock));
	}

	// Perform the insertions recorded in this stash
	void PerformBlockInsertions(CMethod* pMeth)
	{
		for (BlockInsertionStash::iterator stashIter = begin();
			 stashIter != end();
			 stashIter++)
		{
			CInsBlock* oldBlock = (*stashIter).first;
			CInsBlock* newBlock = (*stashIter).second;

			if (oldBlock == NULL) {
				// Insert the new block at the top of the method
				CInsBlocks* methBlocks = pMeth->GetInsBlocks();
				methBlocks->insert(methBlocks->begin(), newBlock);
			}
			else {
				// Scan the method looking for the old block
				CInsBlocks* methBlocks = pMeth->GetInsBlocks();
				for (CInsBlocks::iterator methBlockIter = methBlocks->begin();
					 methBlockIter != methBlocks->end();
					 methBlockIter++)
				{
					CInsBlock* methBlock = (*methBlockIter);
					if (methBlock == oldBlock) {
						CInsBlocks::iterator tempMethBlockIter = methBlockIter;
						tempMethBlockIter++;
						methBlocks->insert(tempMethBlockIter, newBlock);
						break;	// don't need to keep looking
					}
				}
			}
		}
	}
};

//------------------------------------------------------------------------------
// HandleCallsiteInsertion
//
// This function does everything needed for callsite insertion.
//
// It takes a large number of parameters from its only caller,
// CBCIEngProbe::InstrumentMethod. It was split out of that function
// because it's so long and reasonably self-contained.
//

void
CBCIEngProbe::HandleCallsiteInsertion(CProbeInsertionContext& ctxt,
						CProbeFragmentList& beforeCallFrags,
						CProbeFragmentList& afterCallFrags,
						BlockInsertionStash& block_insertion_stash)
{
	cout << "CallSite" << endl;
	cout << "Instrumenting calls from " << ctxt.className << endl;
	const char* withinClass =  ctxt.className;
	const char* withinMethod =  ctxt.methodName;
	const char* withinSig =  ctxt.methodSignature;
	
	// walk every instruction of every block. At each invoke-type
	// instruction, see if it's a call to a function that matches
	// a callsite probe target. If so, perform callsite insertion on it.

	// These variables hold the local variable numbers for storing callsite args
	// and callsite "this" parameters.We only need to allocate one per function 
	// we do insertion on, no matter how many call sites get instrumented.
	int localVariableForCallsiteArgs = -1;
	int localVariableForCallsiteThis = -1;

	

	CInsBlocks* pinsb = ctxt.pMethodJ->GetInsBlocks();
	for (CInsBlocks::iterator itrBlk = pinsb->begin(); 
		 itrBlk != pinsb->end(); 
		 itrBlk++)
	{
		CInstructions* pins = (*itrBlk)->GetInstructions();
		for (CInstructions::iterator itrIns = pins->begin();
			 itrIns != pins->end();
			 itrIns++)
		{
			CInstruction* pi = *itrIns;
			if(pi->GetSemTag() == SEM_CALL) {
				// Call site: invokespecial, invokevirtual, invokestatic, invokeinterface
				// Get the package, class, method, and signature of the method being called
				CInstruction_InvokeJ* pInvokeInstr = (CInstruction_InvokeJ*)pi;
				unsigned cpIndex = pInvokeInstr->GetCpIndex();
				CCPUtf8Info* class8 = ctxt.pConstPool->GetMethodClass(cpIndex);
				string className = (string)(*class8);
				CCPUtf8Info* meth8 = ctxt.pConstPool->GetMethodName(cpIndex);
				string methodName = (string)(*meth8);
				CCPUtf8Info* sig8 = ctxt.pConstPool->GetMethodType(cpIndex);
				string methodSig = (string)(*sig8);

				// See if any callsite probes apply to this invoke instruction.
				// We have to check the fragments on both the BEFORECALL and AFTERCALL lists.
				// And while we're there, collect information about whether ANY of them want
				// the argument list and/or the "this" pointer.

				CProbeFragmentList beforeCalls;
				CProbeFragmentList afterCalls;
				bool any_callsite_probes_on_this_invoke = false;
				bool any_callsite_wants_this = false;
				bool any_callsite_wants_args = false;

				CProbeFragmentList::iterator itrfrag;
				for (itrfrag = beforeCallFrags.begin();
					 itrfrag != beforeCallFrags.end();
					 itrfrag++) 
				{
					CProbeFragment* pfrag = (*itrfrag);
					CProbe* parent = pfrag->GetParentProbe();
					cout << "class " << className.c_str() << endl;
					if (parent->Match(className.c_str(), methodName.c_str(), methodSig.c_str()) &&
						parent->MatchWithin(withinClass,withinMethod,withinSig )
					
					) {
						cout << "adding before" << endl;
						any_callsite_probes_on_this_invoke = true;
						beforeCalls.push_back(pfrag);
						if (pfrag->GetArgBits() & CProbeFragment::ARG_BITS_THISOBJ) {
							any_callsite_wants_this = true;
						}
						if (pfrag->GetArgBits() & CProbeFragment::ARG_BITS_ARGSLIST) {
							any_callsite_wants_args = true;
						}
					}
				}

				// Same loop as above, for AFTERCALL.
				for (itrfrag = afterCallFrags.begin();
					 itrfrag != afterCallFrags.end();
					 itrfrag++)
				{
					CProbeFragment* pfrag = (*itrfrag);
					CProbe* parent = pfrag->GetParentProbe();
					if (parent->Match(className.c_str(), methodName.c_str(), methodSig.c_str())&&
						parent->MatchWithin(withinClass,withinMethod,withinSig )
					
					) {
						cout << "adding after" << endl;
						any_callsite_probes_on_this_invoke = true;
						afterCalls.push_back(pfrag);
						if (pfrag->GetArgBits() & CProbeFragment::ARG_BITS_THISOBJ) {
							any_callsite_wants_this = true;
						}
						if (pfrag->GetArgBits() & CProbeFragment::ARG_BITS_ARGSLIST) {
							any_callsite_wants_args = true;
						}
					}
				}

				if (any_callsite_probes_on_this_invoke) {
					// Create an insertion context for the callsite insertion.
					// This doesn't get populated in the usual way, with the
					// constructor that takes parameters and pulls values from
					// the method and module we're doing insertion in.
					CProbeInsertionContext callsite_ctxt;
					callsite_ctxt.className = className.c_str();
					callsite_ctxt.pMethodJ = ctxt.pMethodJ;
					callsite_ctxt.methodName = methodName.c_str();
					callsite_ctxt.methodSignature = methodSig.c_str();
					callsite_ctxt.pConstPool = ctxt.pConstPool;

					// callsite probes don't have static fields, etc.
					callsite_ctxt.staticFieldRef = 0;
					callsite_ctxt.methodNumber = 0;
					callsite_ctxt.executableUnitNumber = 0;
					callsite_ctxt.classSourceFile = NULL;
					callsite_ctxt.methodNamesString = NULL;
					callsite_ctxt.lineTableString = NULL;

					callsite_ctxt.localVariableForThis = -1;
					callsite_ctxt.localVariableForArgs = -1;
					callsite_ctxt.itrIns = &itrIns;	// see caution at CProbeInsertionContext.itrIns decl
					callsite_ctxt.pIns = pins;

					// Set "hasThis" - means just one thing, that the method
					// actually has a "this" pointer. Doesn't mean a
					// fragment can access it. Constructors, for instance
					// have "this" but entry fragments can't access it -
					// it's uninitialized at that point.

					callsite_ctxt.hasThis = (pi->GetOpCode() != JVMI_invokestatic);

					CCodeAttribute* pcode = ctxt.pMethodJ->GetCodeAttribute();

					// We have to store args now if anybody wants them,
					// or if anybody wants "this" (because it's buried under the args)
					if (any_callsite_wants_args || any_callsite_wants_this) {
						// Allocate a local in the calling function to hold the args.
						// We only have to do this once per function we do insertion on,
						// no matter how many call sites get instrumentation.
						// TODO: fail here if MaxLocals becomes too big
						if (localVariableForCallsiteArgs == -1) {
							localVariableForCallsiteArgs = pcode->GetMaxLocals();
							pcode->SetMaxLocals(pcode->GetMaxLocals() + 1);
						}
						callsite_ctxt.localVariableForArgs = localVariableForCallsiteArgs;
						callsite_ctxt.CallsiteStoreArgs();
					}
					if (any_callsite_wants_this) {
						if (localVariableForCallsiteThis == -1) {
							// TODO: fail here if MaxLocals becomes too big
							localVariableForCallsiteThis = pcode->GetMaxLocals();
							pcode->SetMaxLocals(pcode->GetMaxLocals() + 1);
						}
						callsite_ctxt.localVariableForThis = localVariableForCallsiteThis;
						callsite_ctxt.CallsiteStoreThis();
					}

					// Now insert all the call-before probe calls
					
					for (itrfrag = beforeCalls.begin();
						 itrfrag != beforeCalls.end();
						 itrfrag++)
					{
						CProbeFragment* pfrag = (*itrfrag);
						pfrag->Instrument(callsite_ctxt);
					}

					// Now restore the stack so we can run the original invoke instruction
					callsite_ctxt.CallsiteReloadArgsAndThis();

					// That's the end of beforecall logic. Now do aftercall logic.

					if (afterCalls.size() != 0) {
						// We create a new insertion block, with label "-1" meaning "no label."
						// We will link it in after the one that we just did insertion on -
						// that is, the one that ended with the invoke instruction.
						//
						// This is predicated on the idea that the invoke instruction
						// was the end of the block it's in. If we change the code that slices
						// functions into insertion blocks, we'll have to change this too.

						// Assert that the "invoke" instruction is the last instruction in its block.
						CInstructions::iterator itrInsTemp = itrIns;
						itrInsTemp++;
						CBCIEngProbeException::Assert(itrInsTemp == pins->end(), 
							"Invoke instruction is not at the end of its insertion block");

						CInsBlock* pNewInsBlock = new CInsBlock((unsigned)-1);
						callsite_ctxt.pIns = pNewInsBlock->GetInstructions();
						CInstructions::iterator newInsBlockIter = callsite_ctxt.pIns->begin();
						callsite_ctxt.itrIns = &newInsBlockIter;
						callsite_ctxt.isExceptionExit = false;

						for (itrfrag = afterCalls.begin();
							itrfrag != afterCalls.end();
							itrfrag++)
						{
							CProbeFragment* pfrag = (*itrfrag);
							pfrag->Instrument(callsite_ctxt);
						}

						// Insert a goto now, after the aftercall insertion,
						// to jump around the catch block we're about to insert.
						// Note that the next original instruction after the invoke
						// is known to be the top of a CInsBlock because Parse()
						// always ends a CInsBlock at instructions like invoke.
						IP_t ipNext = pi->GetIP() + pi->GetSize();
						CLabels* plabels = ctxt.pMethodJ->GetLabels();
						plabels->AddLabel(ipNext);
						pNewInsBlock->AddInstruction(CInsSetJ::Create_goto(ipNext));

						// Build exception exit interceptor for the callsite
						// TODO: factor out to another function
						IP_t ipStart = pi->GetIP();
						IP_t ipEnd = pi->GetIP();
						IP_t ipCatchAll = callsite_ctxt.pMethodJ->CreateUniqueLabel();
						// Create a new block for exception handler and save it in the stash
						CInsBlock* pblkExceptionExit = new CInsBlock(ipCatchAll);
						CMethodException* pmtdex = new CMethodExceptionJ(0, ipStart, ipEnd, pblkExceptionExit);
						callsite_ctxt.pMethodJ->AddException(pmtdex, CMtdExTable::TOP);

						// Note: we add blocks to the stash in the inverse order that we
						// want them emitted in. This is a defect in the stash that
						// will be fixed soon. 
						// TODO: reorder these and remove this comment when it's fixed.
						block_insertion_stash.Add(*itrBlk, pblkExceptionExit);
						block_insertion_stash.Add(*itrBlk, pNewInsBlock);

						// Prepare the insertion context
						callsite_ctxt.pIns = pblkExceptionExit->GetInstructions();
						CInstructions::iterator itrInsCatchAll = callsite_ctxt.pIns->begin();
						callsite_ctxt.itrIns = &itrInsCatchAll;
						callsite_ctxt.isExceptionExit = true;
						for (itrfrag = afterCalls.begin(); itrfrag != afterCalls.end(); itrfrag++)
						{
							CProbeFragment* pfrag = (*itrfrag);
							pfrag->Instrument(callsite_ctxt);
							callsite_ctxt.Insert(CInsSetJ::Create_simple(JVMI_athrow));
						}
					}
				} // end if any_callsite_probes_on_this_invoke
			} // end if is SEM_CALL
		} // end for each instruction
	} // end for each block
}

//--------------------------------------------------------
// CProbeInsertionContext constructor
//
// Fills in the context structure with those values you can get
// from the method and its module, plus those you can get from "this."
//
// Called from two places:
// CBCIEngProbe::InstrumentMethod and CBCIEngProbe::HandleStaticInitializers
//
// This constructor is *not* called when creating a callsite context,
// because the information you want is about the CALLED method,
// not the method you're doing insertion into.
//

CProbeInsertionContext::CProbeInsertionContext(CBCIEngProbe* pEngProbe, CMethod* i_pmeth)
{
	CMethodJ* pmethj = (CMethodJ*)i_pmeth;
	CModuleJ* pmodj = (CModuleJ*)(pmethj->GetModule());

	className = pmodj->GetName();
	pMethodJ = pmethj;
	methodName = pmethj->GetName();
	methodSignature = pmethj->GetSignature();
	hasThis = pmethj->GetHasThis();

	staticFieldRef = 0;	// will fill in later
	methodNamesString = pEngProbe->GetMethodNamesString().c_str();
	lineTableString = pEngProbe->GetLineTableString().c_str();

	// Find out what method number this is in the module.
	// TODO: This mechanism works but seems kind of silly. Add a field to CMethod?
	// TODO: optimize this out if nobody wants methodNumber.
	// IMPORTANT: only works if adding a synthetic method puts it at the end,
	// or is always deferred until later.
	int mnum = 0;
	CMethods* pmeths = pmodj->GetMethods();
	for (mnum = 0; ; mnum++) {
		if ((*pmeths)[mnum] == pmethj) {
			methodNumber = mnum;
			break;
		}
	}

	// TODO: don't compute class source file unless somebody wants it.
	if (pmodj->GetSourceFileNames().size() > 0) {
		classSourceFile = (pmodj->GetSourceFileNames())[0].c_str();
	}
	else {
		classSourceFile = NULL;
	}

	// During function instrumentation, the local variable for "this" is always number zero.
	// During callsite instrumentation it'll be something else.
	localVariableForThis = 0;

	CJClassFile& jclass = pmodj->GetClass();
	pConstPool = jclass.GetConstPool();

	// Initialize localVariableForArgs to -1.
	// But if anybody actually wants it, we'll change it.
	localVariableForArgs = -1;
}

//------------------------------------------------------------------------------
// InstrumentMethod
// In:
//	i_pmeth - pointer to the method (CMethod*)
// Out:
//	-
// Throws:
//
// How this works:
//
// Make a callback to the driver that started us, to see if it wants to veto
// instrumentation on this method.
//
// Create a CProbeInsertionContext to hold pertinent information about
// this method, so the downstream functions don't have to look it all up.
//
// Run through the list of probes that might apply to this method, call match()
// to see which ones actually do. Add that probe's fragments to some lists,
// one list per type of fragment, with special handling for staticInitialization fragments.
//
// Handle callsite probes (separate function)
//
// Walk though every block and do "catch" insertion if it's the top of
// an exception handler, and do executableUnit insertion regardless.
//
// Perform entry insertion and exit insertion, plus exception-exit insertion
// which involves a new method-spanning try/catch block.
// 

void
CBCIEngProbe::InstrumentMethod(CMethod* i_pmeth)
{
	cout << "Instrumenting " << i_pmeth->GetName() << endl;
	// Functions with no code attribute don't get any farther.
	// They are placeholders for abstract functions, interfaces and native functions.
	// (Interfaces shouldn't have gotten this far.)
	CMethodJ* pmethj = (CMethodJ*)i_pmeth;
	CCodeAttribute* pcode = pmethj->GetCodeAttribute();
	if(NULL == pcode)
	{
		return;
	}

	// Allow the client that's driving us to veto instrumentation of this module
	if (NULL != m_pfnCallback &&
		(m_wCBFlags & BCIENGINTERFACE_CALLBACK_METHOD) &&
		!m_pfnCallback(i_pmeth->GetName(), strlen(i_pmeth->GetName()), BCIENGINTERFACE_CALLBACK_METHOD))
	{
		return;
	}

	// Pointers and iterators that get used repeatedly in this function
	CInstruction* pi;
	CInsBlocks::iterator itrBlk;
	CProbeFragmentList::iterator itrfrag;

	bool any_fn_probes_want_args_list = false;

	bool any_probefrags_match = false;
	bool any_callsite_probes = false;

	// This flag goes true if we encounter something that is going to
	// need function entry insertion.
	bool any_entry_insertion_needed = false;
	// or function exit insertion
	bool any_exit_insertion_needed = false;

	// This flag indicates whether the method has line information.
	bool method_has_line_info = (pmethj->GetLineNumbers()->size() != 0);

	// Holds the executable unit insertion mode for this method:
	// NONE if there are no executableUnit fragments that apply;
	// EVERY for methods with line info; FIRST for methods without line info.
	enum { EUMODE_NONE, EUMODE_FIRST, EUMODE_EVERY };
	int executable_unit_insertion_mode = EUMODE_NONE;

	// Create the insertion context for this method.
	CProbeInsertionContext ctxt(this, i_pmeth);

	// Populate the list of probes that apply to this method.
	//
	// All callsite probes apply to this method.
	// Non-callsite (function) probes only apply if they Match() the class/name/signature
	// of this class, or of a (direct) interface implemented by this class.
	// TODO: factor this loop into another function to make this one simpler.
	//
	// What's actually poplated is a one list per type of probe: probeFragments[nType]
	// where nType is the probe type: entry, exit, catch, callBefore, whatever.

	CProbeFragmentList   probeFrags[CProbeFragment::PROBE_LAST];

	CModuleJ* pModuleJ = (CModuleJ*)i_pmeth->GetModule();
	CJClassBuilder& cBuilder = pModuleJ->GetClassBuilder();

	for(CProbeList::iterator itrProbe = m_probelstMod.begin(); 
		itrProbe != m_probelstMod.end(); 
		itrProbe++)
	{
		CProbe* pProbe = (*itrProbe);
		bool match = probeAppliesToMethod(pProbe, 
					ctxt.className, 
					ctxt.methodName, 
					ctxt.methodSignature, 
					&cBuilder);

		if (match) {
			// This probe (pProbe) matched this method.
			cout << "Matched" << endl; 
			// Add this probe's ProbeFragments to the lists
			// of fragments that apply to this method. There is one list per type.
			// STATICINITIALIZER type fragments get special handling:
			// they will be processed later, in HandleStaticInitializers.
			//
			// Also add each fragment's external reference to the module,
			// so we can make calls to those functions.
			CProbeFragmentList& this_probes_frags = pProbe->GetProbeFragmentList();

			for(itrfrag = this_probes_frags.begin(); 
				itrfrag != this_probes_frags.end(); 
				itrfrag++)
			{
				CProbeFragment* pfrag = (*itrfrag);
				int nType = pfrag->GetType();
				CBCIEngProbeException::Assert(nType >= 0 && nType < CProbeFragment::PROBE_LAST,
					"Unknown probe reference type (not entry, catch, exit, etc.)");
				if (nType == CProbeFragment::PROBE_STATICINITIALIZER) {
					// Do nothing. We'll handle this fragment in HandleStaticInitializers
				}
				else {
					// It's a regular kind of fragment, to be inserted into this method
					probeFrags[nType].push_back(pfrag);
					i_pmeth->GetModule()->AddExtRef(*(pfrag->GetExtRef()));
					any_probefrags_match = true;
					if (!pProbe->IsCallsiteProbe() &&
						(pfrag->GetArgBits() & CProbeFragment::ARG_BITS_ARGSLIST)) 
					{
						any_fn_probes_want_args_list = true;
					}
					if (pProbe->IsCallsiteProbe()) {
						any_callsite_probes = true;
					}
					if (nType == CProbeFragment::PROBE_ONENTRY) {
						any_entry_insertion_needed = true;
					}
					if (nType == CProbeFragment::PROBE_ONEXIT) {
						any_exit_insertion_needed = true;
					}
					if (nType == CProbeFragment::PROBE_EXECUTABLEUNIT) {
						// OK, we know at least this executableUnit fragment
						// applies to this method. Decide what mode to use
						// based on whether there's line info available.
						// If FIRST mode, remember we need to do some entry insertion.
						if (method_has_line_info) {
							executable_unit_insertion_mode = EUMODE_EVERY;
						}
						else {
							executable_unit_insertion_mode = EUMODE_FIRST;
							any_entry_insertion_needed = true;
						}
					}
				}
			} // end "for each fragment in this (matching) probe"
		} // end if "this method matches the probe's targeting rules"
	} // end "for each probe in m_probelstMod"

	if (any_probefrags_match == false) {
		// No method-type fragments are interested in this method.
		// (Perhaps a staticInitializer matched, though.)
		return;
	}

	// Remember whether we're going to do any per-block insertion.
	// Yes if there are any "catch" fragments or 
	// (there are executableUnit fragments and this method has line info).
	bool any_per_block_insertion_needed = 
		(executable_unit_insertion_mode == EUMODE_EVERY) || 
		(probeFrags[CProbeFragment::PROBE_ONCATCH].size() != 0);

	// If any of the probes that apply to this method wants the argument list, 
	// allocate the local variable number for it now.
	if (any_fn_probes_want_args_list) {
		// TODO: fail here if MaxLocals becomes too big
		CCodeAttribute* pcode = pmethj->GetCodeAttribute(); 
		ctxt.localVariableForArgs = pcode->GetMaxLocals();
		pcode->SetMaxLocals(pcode->GetMaxLocals() + 1);

		// Flag the fact that we have some entry insertion to do
		any_entry_insertion_needed = true;
	}

	// Declare the block insertion stash. This is a memory bucket that stores
	// new CInsBlocks that you are going to want to insert into this function later,
	// after all our instrumentation is complete.

	BlockInsertionStash block_insertion_stash;

	// Now it's time to instrument the code

	CInsBlocks* pinsb = pmethj->GetInsBlocks();

	//==================================================================

	// If any of the matching probes are callsite probes,
	// call the callsite handler function now.
	if (any_callsite_probes) {
		HandleCallsiteInsertion(ctxt,
							probeFrags[CProbeFragment::PROBE_BEFORECALL],
							probeFrags[CProbeFragment::PROBE_AFTERCALL],
							block_insertion_stash);
	}

	// Now, do per-CInsBlock insertion, if necessary
	//
	// if (any per-block insertion is necessary)
	//	For each block,
	//		Do "catch" insertion if it's an exception handler:
	//			For each exception handler,
	//				If this block is a handler,
	//					For each catch-type probe fragment
	//						Call "Insert" on this fragment.
	//		Do "executableUnit" insertion:
	//			If executableUnit insertion mode is "every" and the block is not synthetic,
	//				For each executableUnit-type probe fragment
	//					Call "Insert" on this fragment.
	//
	// TODO: optimize. If there's more than one catch/finally in the
	// function, we're causing bloat. Instead, insert a JSR to a
	// common "top of catch/finally" clause. Place the isFinally
	// flag value in a local or something, so it can be passed to
	// the fragment.
	//
	// TODO: factor this into a separate function to make this one shorter.

	if (any_per_block_insertion_needed) {
		CExTable& extable = pcode->GetExTable();
		for(itrBlk = pinsb->begin(); itrBlk != pinsb->end(); itrBlk++)
		{
			// Set up the "instruction to insert at" iterator reference here.
			// All insertion will appear before the first instruction of the block,
			// in order - first every probe's catch insertion, then every probe's
			// executableUnit insertion.
			ctxt.pIns = (*itrBlk)->GetInstructions();
			CInstructions::iterator itrIns = ctxt.pIns->begin();
			ctxt.itrIns = &itrIns;
			ctxt.executableUnitNumber = (*itrBlk)->GetOriginalBlockNumber();

			// Insert "catch" fragments if this block is the start of an exception handler
			// Optimization: do this loop only if the list of "catch" probes nonempty.
			// TODO: optimize so we don't loop through ex table on each insn

			if (probeFrags[CProbeFragment::PROBE_ONCATCH].size() != 0) {
				for(CExTable::iterator itrEx = extable.begin(); itrEx != extable.end(); itrEx++)
				{
					if(itrEx->GetHandlerPC() == (*itrBlk)->GetLabel())
					{	
						ctxt.isFinally = (itrEx->GetCatchtype() == 0);
						for (itrfrag = probeFrags[CProbeFragment::PROBE_ONCATCH].begin();
							 itrfrag != probeFrags[CProbeFragment::PROBE_ONCATCH].end();
							 itrfrag++)
						{
							// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
							ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
							(*itrfrag)->Instrument(ctxt);
						}
						// Having found that this is a handler there's no need to
						// look for more exception table entries with matching HandlerPCs,
						// so break.
						break;
					}
				}
			}

			// Insert executableUnit probes.
			// Do this only if this block is original, not synthetic.
			// Synthetic blocks have an "original block number" of -1.
			if (executable_unit_insertion_mode == EUMODE_EVERY && (*itrBlk)->GetOriginalBlockNumber() != -1) {
				for (itrfrag = probeFrags[CProbeFragment::PROBE_EXECUTABLEUNIT].begin();
					 itrfrag != probeFrags[CProbeFragment::PROBE_EXECUTABLEUNIT].end();
					 itrfrag++)
				{
					// insert any executableUnit probes here, before the first
					// original instruction but after any catch insertion.
					// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
					ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
					(*itrfrag)->Instrument(ctxt);
				}
			}
		} // end "for each block"
	} // end "if any per-block insertion needed"

	// TODO: factor exit wrapping and return instruction
	// instrumentation into a separate function.

	// Create a finally wrapper for all return instructions,
	// and one grand "catch all" exception handler to call exit
	// fragments upon exception exit.
	if(any_exit_insertion_needed)
	{
		CreateExitWrap(ctxt, probeFrags[CProbeFragment::PROBE_ONEXIT]);
		// Walk every instruction of every block. Before each "return"
		// instruction, insert a JSR to the finally block created
		// by CreateExitWrap. Its location was stored in m_ipFinally.
		for(itrBlk = pinsb->begin(); itrBlk != pinsb->end(); itrBlk++)
		{
			CInstructions* pins = (*itrBlk)->GetInstructions();
			for (CInstructions::iterator itrIns = pins->begin();
				 itrIns != pins->end();
				 itrIns++)
			{
				pi = *itrIns;
				if(pi->GetSemTag() == SEM_RET)
				{
					pins->insert(itrIns, CInsSetJ::Create_jsr(m_ipFinally));
				}
			}
		}
	}

	// Perform top-of-method insertion, if any_entry_insertion_needed.
	// Right now three situations can require entry insertion:
	//		1. A PROBE_ONENTRY that applies to this method,
	//		2. Any function probes want the args list
	//		3. ExecutableUnit insertion on a method with no source info
	//
	// We create a new insertion block, with label "-1" meaning "no label"
	// and add it to the insertion stash, saying we want it inserted
	// before the first block of the method.
	//
	// Here's why this isn't done as simple insertion before the first instruction
	// of the first original CInsBlock: if any part of the method branches back
	// to that first instruction (e.g. the method starts with a "do" loop), our insertion
	// would be executed all over again. This way it'll only be executed once.
	// 
	// TODO: factor this into a separate function.
	// TODO: this logic can move anywhere in the function thanks to the stash.

	if (any_entry_insertion_needed) {
		CInsBlock* pNewInsBlock = new CInsBlock((unsigned)-1);
		CInstructions* pinsList = pNewInsBlock->GetInstructions();
		CInstructions::iterator itrIns = pinsList->begin();

		ctxt.pIns = pinsList;
		ctxt.itrIns = &itrIns;

		if (any_fn_probes_want_args_list) {
			// Insert instructions to allocate and populate the Object[] array for args.
			// We do this just once and re-use it for every probe fragment that wants it.
			ctxt.EmitArgsList();
			ctxt.Insert(CInsSetJ::Create_astore(ctxt.localVariableForArgs));
		}

		for (itrfrag = probeFrags[CProbeFragment::PROBE_ONENTRY].begin();
			 itrfrag != probeFrags[CProbeFragment::PROBE_ONENTRY].end();
			 itrfrag++)
		{
			// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
			ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
			(*itrfrag)->Instrument(ctxt);
		}

		if (executable_unit_insertion_mode == EUMODE_FIRST) {
			for (itrfrag = probeFrags[CProbeFragment::PROBE_EXECUTABLEUNIT].begin();
				itrfrag != probeFrags[CProbeFragment::PROBE_EXECUTABLEUNIT].end();
				itrfrag++)
			{
				ctxt.executableUnitNumber = 0;
				ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
				(*itrfrag)->Instrument(ctxt);
			}
		}

		// Record that we want the new block inserted at the top of the function.
		block_insertion_stash.Add(NULL, pNewInsBlock);
	}

	// Process the block insertion stash now that we're finished.
	block_insertion_stash.PerformBlockInsertions(pmethj);
}

//------------------------------------------------------------------------------
// CreateExitWrap
// In:
//	ctxt - CProbeInsertionContext - the context for the insertion
// Out:
//	-
// Throws:
//
// This function creates the ordinary exit and exception exit wrappers for a function. 
// This adds two blocks to the function: a "catch any" exception handler block for
// exception exit, and a block which serves as a "finally" block.
//
// The "catch any" exception handler calls the "exit" fragment of every probe 
// that applies to this function. The argument list passed to the fragments
// can include the exception object that caused this function to exit.
// The last thing the "catch any" handler does is rethrow the exception that got us there.
//
// The "finally" block contains a call to the "exit" fragment of every probe 
// that applies to this function. It makes available the return value (if there
// is one) as a possible argument to each fragment function.
// Then it returns to the place it was called from - which will be
// a JSR instruction inserted just before every "return" instruction
// in the original function.
//
void	
CBCIEngProbe::CreateExitWrap(CProbeInsertionContext& ctxt, 
                             CProbeFragmentList& exit_probe_frags)
{
	CCodeAttribute* pcode = ctxt.pMethodJ->GetCodeAttribute(); 
	CExTable& extab = pcode->GetExTable();
	CInsBlocks* pinsblks = ctxt.pMethodJ->GetInsBlocks();
	CInsBlock* pinsblkLast = *(pinsblks->end() - 1);
	CInstructions* pLastBlockInstructions = pinsblkLast->GetInstructions();
	CInstruction* pLastInstruction;

	// Set pLastInstruction to the last instruction in the function.
	CInstructions::reverse_iterator ritrIns;
	ritrIns = pLastBlockInstructions->rbegin();
	pLastInstruction = *ritrIns;

	// Compute labels
	IP_t ipStart = (*pinsblks->begin())->GetLabel();
	IP_t ipEnd = pLastInstruction->GetIP();
	m_ipCatchAll = ctxt.pMethodJ->CreateUniqueLabel();
	m_ipFinally = ctxt.pMethodJ->CreateUniqueLabel();

	// Create new blocks for the finally clause
	// Catch All block
	CInsBlock* pinsblkCatchAll = new CInsBlock(m_ipCatchAll);
	CInstructions* pinsCatchAll = pinsblkCatchAll->GetInstructions();
	// Finally block
	CInsBlock* pinsblkFinally = new CInsBlock(m_ipFinally);
	CInstructions* pinsFinally = pinsblkFinally->GetInstructions();

	// Create the exception descriptor and add it to the exception table
	CMethodException* pmtdex = new CMethodExceptionJ(0, ipStart, ipEnd, pinsblkCatchAll);
	ctxt.pMethodJ->AddException(pmtdex);

	// Generate code
	//
	// CatchAll block
	// The catchall block starts execution with the exception object
	// on the top of the stack. We call Instrument
	// for each ONEXIT fragment, and those which want to pass the exception
	// object to the fragment will dup it and pass it appropriately.
	//
	// Last, we emit "athrow" to rethrow this exception object.

	// This iterator local variable is used twice in this function
	// for different loops.
	CInstructions::iterator itrIns;
	CProbeFragmentList::iterator itrfrag;

	// Insert exit probes here
	for (itrfrag = exit_probe_frags.begin();
		 itrfrag != exit_probe_frags.end();
		 itrfrag++)
	{
		ctxt.pIns = pinsCatchAll;
		itrIns = pinsCatchAll->end();
		ctxt.itrIns = &itrIns;
		ctxt.isExceptionExit = true;
		ctxt.isFinally = false;
		// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
		ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
		(*itrfrag)->Instrument(ctxt);
	}
	pinsCatchAll->push_back(CInsSetJ::Create_simple(JVMI_athrow));

	// Finally block
	//
	// The finally block starts execution with the return address at the
	// top of the stack, and after that is the return value, if any.
	//
	// First we store the return address in a new local variable - that leaves
	// the return value at the top of the stack. PushArguments for a probe
	// fragment which wants the return object will correctly dup that value
	// and leave the original on the stack. The last thing we do is emit
	// a return through the new local.
	//
	// TODO: fail here if MaxLocals becomes too big

	u2 u2Ret = pcode->GetMaxLocals();
	pcode->SetMaxLocals(u2Ret + 1);	// Create a local variable for the ret
	CInstruction* pi = CInsSetJ::Create_astore(u2Ret);
	pinsFinally->push_back(pi);

	// Insert exit probes here
	for (itrfrag = exit_probe_frags.begin();
		 itrfrag != exit_probe_frags.end();
		 itrfrag++)
	{
		ctxt.pIns = pinsFinally;
		itrIns = pinsFinally->end();
		ctxt.itrIns = &itrIns;
		ctxt.isExceptionExit = false;
		ctxt.isFinally = false;
		// Set ctxt.staticFieldRef. Must set it on a per-fragment basis!
		ctxt.staticFieldRef = (*itrfrag)->GetParentProbe()->GetStaticFieldRef();
		(*itrfrag)->Instrument(ctxt);
	}
	pinsFinally->push_back(CInsSetJ::Create_ret(u2Ret));

	// Add catch and finally blocks to the method
	pinsblks->push_back(pinsblkCatchAll);
	pinsblks->push_back(pinsblkFinally);
}

//==============================================================================
// CBCIEngProbeException
// Probe engine exception
// ToDo: figure out how to hand out other exceptions without exposing 
//       the library internals
//
CBCIEngProbeException::CBCIEngProbeException(unsigned i_uReason)
:CBCIEngException(i_uReason)
{
	m_szReason[0] = '\0';
}

CBCIEngProbeException::CBCIEngProbeException(const char* i_szReason)
:CBCIEngException(REASON_Unknown)
{
	strncpy(m_szReason, i_szReason, 128);
	m_szReason[127] = '\0';
}

//= End of BCIEngProbe.cpp =====================================================

