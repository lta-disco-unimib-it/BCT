 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JClassBuilder.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 
#include "JClassBuilder.h"

//==============================================================================
// CJClassBuilder.cpp
//
// Java class builder.
//
//------------------------------------------------------------------------------

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

//------------------------------------------------------------------------------
// Default constructor.
// No special construction so far
CJClassBuilder::CJClassBuilder()
:CJClassFile()
{
}

//------------------------------------------------------------------------------
// Constructor
// In:
//	i_u2AccessFlags - access flags
//	i_szClass - Class name
//	i_szSuper - Super class name
//
// This constructor will result in a brand new class file
//
CJClassBuilder::CJClassBuilder(u2 i_u2AccessFlags, CSTR i_szClass, CSTR i_szSuper)
:CJClassFile()
{

	m_u2AccessFlags = i_u2AccessFlags;
	u2 u2ThisName = m_pConstPool->Add(new CCPUtf8Info(i_szClass));
	m_u2ThisClass = m_pConstPool->Add(new CCPClassInfo(u2ThisName));
	SetSuperClass(i_szSuper);
}

//------------------------------------------------------------------------------
// Virtual destructor
//
CJClassBuilder::~CJClassBuilder()
{
}


//------------------------------------------------------------------------------
// GetThisClassName
//
// Returns:
//	string for this class name in UTF8
//
string
CJClassBuilder::GetThisClassName()
{
	CCPClassInfo* pcpClass = (CCPClassInfo*)((*m_pConstPool)[GetThisClass()]);
	CCPUtf8Info* pcpUtf8 = (CCPUtf8Info*)((*m_pConstPool)[pcpClass->GetClassInd()]);
	string strThisClass = (string)*pcpUtf8;
	return strThisClass;
}

//------------------------------------------------------------------------------
// GetSuperClassName
// Returns:
//	string for super class name in UTF8
//
string
CJClassBuilder::GetSuperClassName()
{
	CCPUtf8Info* pcpUtf8 = (CCPUtf8Info*)((*m_pConstPool)[GetSuperClass()]);
	string strSuperClass = (string)*pcpUtf8;
	return strSuperClass;
}

string
CJClassBuilder::GetNameFromInterfaceIndex(u2 index)
{
	CCPClassInfo* interfaceRef = (CCPClassInfo*)((*m_pConstPool)[index]);
	CCPUtf8Info* pcpUtf8 = (CCPUtf8Info*)((*m_pConstPool)[interfaceRef->GetClassInd()]);
	string strThisInterface = (string)*pcpUtf8;
	return strThisInterface;
}

//------------------------------------------------------------------------------
// FindNameAndType
// In:
//	i_szName - name to find
//  i_szType - type to find (NULL - any type)
// Returns:
//	CCPNameAndTypeInfo - Constant pool name and type info pointer if found
//                       NULL otherwise
//
CCPNameAndTypeInfo*
CJClassBuilder::FindNameAndType(CSTR i_szName, CSTR i_szType) const
{
	CCPNameAndTypeInfo* pcpntiRet = NULL;	// return value
	CConstPool::iterator itr;				// CP iterator
	CCPInfo* pcpi = NULL;					// CP info
	
	for(itr = m_pConstPool->begin(),itr++; itr != m_pConstPool->end(); itr++)
	{
		pcpi = *itr;
		if(NULL != pcpi && pcpi->GetTag() == CONSTANT_NameAndType)
		{
			CCPNameAndTypeInfo* pcpnti = (CCPNameAndTypeInfo*)pcpi;
			u2 u2Name = pcpnti->GetNameInd();
			CCPUtf8Info* pcputf8Name = (CCPUtf8Info*)((*m_pConstPool)[u2Name]);
			if(*pcputf8Name == i_szName)
			{
				if(NULL != i_szType) // Do we check type?
				{
					u2 u2Descr = pcpnti->GetDescriptorInd();
					CCPUtf8Info* pcputf8Type = (CCPUtf8Info*)((*m_pConstPool)[u2Descr]);
					if(!(*pcputf8Type == i_szType))
						continue;	// Bad type - continue
				}
				pcpntiRet = pcpnti; // Found!
				break;
			}
		}

	}
	return pcpntiRet;
}

//------------------------------------------------------------------------------
// FindFieldRef
// In:
//	i_szName  - name of the field to find
//	i_szClass - name of the class (NULL == this class)
// Returns:
//	CCPFieldRefInfo - Constant pool field reference info pointer if found
//                    NULL otherwise
//
CCPFieldrefInfo*
CJClassBuilder::FindFieldRef(CSTR i_szName, CSTR i_szClass) const
{
	CCPFieldrefInfo* pcpfriRet = NULL;	// Field ref info to return
	CConstPool::iterator itr;			// CP iterator
	CCPInfo* pcpi = NULL;				// CP info

	CCPNameAndTypeInfo* pcpnti = FindNameAndType(i_szName);
	for(itr = m_pConstPool->begin(), itr++; NULL != pcpnti && itr != m_pConstPool->end(); itr++)
	{
		pcpi = *itr;
		if(NULL != pcpi && pcpi->GetTag() == CONSTANT_Fieldref)
		{
			CCPFieldrefInfo* pcpfri = (CCPFieldrefInfo*)pcpi;	// Field ref
			u2 u2Class = pcpfri->GetClassInd();					// Class info index
			if(i_szClass == NULL)
			{
				if(u2Class != m_u2ThisClass)
					continue;	// Not this class!
			}
			else
			{
				CCPClassInfo* pcpci = (CCPClassInfo*)((*m_pConstPool)[u2Class]);
				CCPUtf8Info* pcputf8ClassName = (CCPUtf8Info*)((*m_pConstPool)[pcpci->GetClassInd()]);
				if(!(*pcputf8ClassName == i_szClass))
					continue; // Not the specified class!
			}
			if(pcpfri->GetNameAndTypeInd() == pcpnti->GetCpIndex())
			{
				pcpfriRet = pcpfri;
				break;	// Found!
			}
		}
	}
	return pcpfriRet;
}

//------------------------------------------------------------------------------
// FindMethodRef
// In:
//	i_szName - method name
//	i_szSignature - method signature in Java internal format
//	i_szClass - name of the class (NULL == this class)
// Returns:
//	CCPMethodRefInfo* - pointer to the method ref info if found
//	                    NULL otherwise
//
CCPMethodrefInfo*
CJClassBuilder::FindMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass) const
{
	CCPMethodrefInfo* pcpmriRet = NULL;	// Method Ref Info to return
	CConstPool::iterator itr;			// CP iterator
	CCPInfo* pcpi = NULL;				// CP info


	CCPNameAndTypeInfo* pcpnti = FindNameAndType(i_szName, i_szSignature);
	for(itr = m_pConstPool->begin(), itr++; NULL != pcpnti && itr != m_pConstPool->end(); itr++)
	{
		pcpi = *itr;
		if(NULL != pcpi && pcpi->GetTag() == CONSTANT_Methodref)
		{
			CCPMethodrefInfo* pcpmri = (CCPMethodrefInfo*)pcpi;
			u2 u2Class = pcpmri->GetClassInd();					// Class info index
			if(i_szClass == NULL)
			{
				if(u2Class != m_u2ThisClass)
					continue;	// Not this class!
			}
			else
			{
				CCPClassInfo* pcpci = (CCPClassInfo*)((*m_pConstPool)[u2Class]);
				CCPUtf8Info* pcputf8ClassName = (CCPUtf8Info*)((*m_pConstPool)[pcpci->GetClassInd()]);
				if(!(*pcputf8ClassName == i_szClass))
					continue; // Not the specified class!
			}
			if(pcpmri->GetNameAndTypeInd() == pcpnti->GetCpIndex())
			{
				pcpmriRet = pcpmri;
				break;	// Found
			}
		}
	}
	return pcpmriRet;
}

//------------------------------------------------------------------------------
// FindClass
// In:
//	i_szName - Class name
// Returns:
//	CCPClassInfo* -- pointer to the class info
//                   NULL if class was not found
//
CCPClassInfo*	
CJClassBuilder::FindClass(CSTR i_szName) const
{
	CCPClassInfo* pcpClassInfo = NULL;
	CConstPool::iterator itr = m_pConstPool->begin();
	while(itr != m_pConstPool->end())
	{
		if((*itr)->GetTag() == CONSTANT_Class)
		{
			CCPClassInfo* pcpci = (CCPClassInfo*)*itr;
			CCPUtf8Info* pcputf8ClassName = (CCPUtf8Info*)((*m_pConstPool)[pcpci->GetClassInd()]);
			if(*pcputf8ClassName == i_szName)
			{
				pcpClassInfo = pcpci;
				break;
			}
			itr++;
		}
	}
	return pcpClassInfo;
}

//------------------------------------------------------------------------------
// CreateStringConstant
// In:
//	i_szString - the constant value
// Returns:
//	CCPStringInfo* - pointer to the string info in the constant pool
//
CCPStringInfo*
CJClassBuilder::CreateStringConstant(CSTR i_szString)
{
	u2 u2Index;
	u2Index = m_pConstPool->Add(new CCPUtf8Info(i_szString));
	u2Index = m_pConstPool->Add(new CCPStringInfo(u2Index));
	CCPStringInfo* pcpString = (CCPStringInfo*)((*m_pConstPool)[u2Index]);
	return pcpString;
}

//------------------------------------------------------------------------------
// CreateIntegerConstant
// In:
//	i_int - Integer value to add
// Returns:
//	CCPIntegerInfo* - pointer to the integer info in the constant pool
//
CCPIntegerInfo*
CJClassBuilder::CreateIntegerConstant (JINTEGER i_int)
{
	u2 u2Index = 0;
	u2Index = m_pConstPool->Add(new CCPIntegerInfo(i_int));
	CCPIntegerInfo* pcpInt = (CCPIntegerInfo*)((*m_pConstPool)[u2Index]);
	return pcpInt;
}

//------------------------------------------------------------------------------
// CreateLongConstant
// In:
//	i_long - long value to add
// Returns:
//	CCPLongInfo* - pointer to the long info in the constant pool
//
CCPLongInfo*
CJClassBuilder::CreateLongConstant(JLONG i_long)
{
	u2 u2Index = 0;
	u2Index = m_pConstPool->Add(new CCPLongInfo(i_long));
	CCPLongInfo* pcpLong = (CCPLongInfo*)((*m_pConstPool)[u2Index]);
	return pcpLong;
}

//------------------------------------------------------------------------------
// CreateFloatConstant
// In:
//	i_float - float value to add
// Returns:
//	CCPLongInfo* - pointer to the float info in the constant pool
//
CCPFloatInfo*
CJClassBuilder::CreateFloatConstant(JFLOAT i_float)
{
	u2 u2Index = 0;
	u2Index = m_pConstPool->Add(new CCPFloatInfo(i_float));
	CCPFloatInfo* pcpFloat = (CCPFloatInfo*)((*m_pConstPool)[u2Index]);
	return pcpFloat;
}

//------------------------------------------------------------------------------
// CreateDoubleConstant
// In:
//	i_double - double value to add
// Returns:
//	CCPLongInfo* - pointer to the double info in the constant pool
//
CCPDoubleInfo*
CJClassBuilder::CreateDoubleConstant(JDOUBLE i_double)
{
	u2 u2Index = 0;
	u2Index = m_pConstPool->Add(new CCPDoubleInfo(i_double));
	CCPDoubleInfo* pcpDouble = (CCPDoubleInfo*)((*m_pConstPool)[u2Index]);
	return pcpDouble;
}

//------------------------------------------------------------------------------
// SetSuperClass
// In:
//	i_szSuper - sets the super class
// Returns:
//	-
// Sets the super class for this class
//
void
CJClassBuilder::SetSuperClass(CSTR i_szSuper)
{
	u2 u2ClassName = m_pConstPool->Add(new CCPUtf8Info(i_szSuper));
	u2 u2ClassInfo = m_pConstPool->Add(new CCPClassInfo(u2ClassName));
	m_u2SuperClass = u2ClassInfo;
}

//------------------------------------------------------------------------------
// SetAccessFlags
// In:
//	i_u2AccessFlags
//
// Sets access flags of this class
//
void
CJClassBuilder::SetAccessFlags(u2 i_u2AccessFlags)
{
	m_u2AccessFlags = i_u2AccessFlags;
}

//------------------------------------------------------------------------------
// AddInterface
// In:
//	i_szInterface - interface name
//
CInterfaceInfo*
CJClassBuilder::AddInterface(CSTR i_szInterface)
{
	u2 u2Index = m_pConstPool->Add(new CCPUtf8Info(i_szInterface));
	CInterfaceInfo* pinterface = new CInterfaceInfo(u2Index);
	GetInterfaces()->Add(pinterface);
	return pinterface;
}

//------------------------------------------------------------------------------
// AddClassAttribute
// In:
//	i_pattribute - pointer to attribute info
// Returns:
//	u2 - number of attributes in the class (including this one)
// Sets a new attribute for this class
//
u2
CJClassBuilder::AddClassAttribute(CAttributeInfo* i_pattributeinfo)
{
	CJAttribs* pattribs = this->GetAttribs();
	pattribs->push_back(i_pattributeinfo);
	u2 u2Attribs = pattribs->size();
	return u2Attribs;
}

//------------------------------------------------------------------------------
// AddFieldAttribute
// In:
//	i_pattributeinfo - attribute to add
//	i_pfieldinfo - field to which the attribute is added
// Returns:
//	u2 - number of attributes in the class (including this one)
//
u2
CJClassBuilder::AddFieldAttribute(CAttributeInfo* i_pattributeinfo, CFieldInfo* i_pfieldinfo)
{
	CJAttribs& attribs = i_pfieldinfo->GetAttribs();
	attribs.push_back(i_pattributeinfo);
	u2 u2Attribs = attribs.size();
	return u2Attribs;
}

//------------------------------------------------------------------------------
// AddMethodAttribute
// In:
//	i_pattributeinfo - attribute to add
//	i_pmethoddinfo - method to which the attribute is added
// Returns:
//	u2 - number of attributes in the class (including this one)
//
u2
CJClassBuilder::AddMethodAttribute(CAttributeInfo* i_pattributeinfo, CJMethodInfo* i_pjmethodinfo)
{
	CJAttribs& attribs = i_pjmethodinfo->GetAttribs();
	attribs.push_back(i_pattributeinfo);
	u2 u2Attribs = attribs.size();
	return u2Attribs;
}

//------------------------------------------------------------------------------
// CreateField
// In:
//	i_u2Access	- Access flags (see javadef.h)
//	i_szName	- field name
//	i_jtype		- Java type (see JavaHelpers.h)
// Returns:
//	CFieldInfo* - pointer to the new field info
//
// Creates a new field and adds it to the list of fields
//
CFieldInfo*
CJClassBuilder::CreateField(u2 i_u2Access, CSTR i_szName, CJavaType& i_jtype)
{
	u2 u2Name = m_pConstPool->Add(new CCPUtf8Info(i_szName));
	u2 u2Type = m_pConstPool->Add(new CCPUtf8Info(i_jtype.GetTypeString().c_str()));
	CFieldInfo* pfield = new CFieldInfo(this, u2Name, u2Type, i_u2Access);
	u2 u2Ref = m_pFields->Add(pfield);
	return pfield;
}

//------------------------------------------------------------------------------
// CreateMethod
// In:
//	i_u2Access	- Access flags (see javadef.h)
//	i_szName	- field name
//	i_szSignature - Method signature (in Java internal format)
// Returns:
//	CJMethodInfo* - pointer to the created method info
//
// Creates a new method and adds it to the list of methods.
// Note that this call just creates a method without a reference.
// In order to call the method from Java byte code it is necessarey to create
// a method reference (see CreateMethodRef and CreateExtMethodRef).
// In some cases method may be created and never referenced within the class.
// This is usually done to expose functionality to other classes. In this case
// you don't have to create a local method reference.
// Also note that a method is created with an empty code attribute. To make the method
// usefull one will have to generate byte code for the method. However, the
// code generation is beyond the scope of this class. To generate byte code
// use CJavaModule and CJavaMethod classes (see ModuleJ.h)
//
CJMethodInfo*
CJClassBuilder::CreateMethod(u2 i_u2Access, CSTR i_szName, CSTR i_szSignature)
{

	u2 u2Ref = 0;

	CJMethodInfo* pMethodInfo = NULL;
	CCodeAttribute* pCodeAttribute = new CCodeAttribute(this);
	pMethodInfo = new CJMethodInfo(this, i_szName, i_szSignature, i_u2Access);
	pMethodInfo->SetCode(pCodeAttribute);
	u2Ref = m_pMethods->Add(pMethodInfo);

	return pMethodInfo;
}

//------------------------------------------------------------------------------
// CreateMethod
// In:
//	i_u2Access	- acess flags
//	i_jmn		- Java method name descriptor (see JavaHelpers.h)
// Returns:
//	CJMethodInfo* - pointer to the created method info
//
// A diffrent form of CreateMethod
CJMethodInfo*
CJClassBuilder::CreateMethod(u2 i_u2Access, CJavaMethodName& i_jmn)
{
	return CreateMethod(i_u2Access, i_jmn.GetName(), i_jmn.GetSignature());
}

//------------------------------------------------------------------------------
// CreateFieldRef
// In:
//	i_pfi - pointer to the existing field info
// Returns:
//	CCPFieldrefInfo* - new field ref info
//
// Creates reference to an internal field in the constant pool
// To use this operation first create a local field by calling CreateField
// and then use it as the argument.
// CreateExtFieldRef includes the field information creation because
// External field descriptor without a reference doesn't make sence.
//
CCPFieldrefInfo*
CJClassBuilder::CreateFieldRef(CFieldInfo* i_pfi)
{
	u2 u2Index;

	CCPNameAndTypeInfo* pcpnti = new CCPNameAndTypeInfo(i_pfi->GetNameInd(),
		                                                i_pfi->GetDescriptorInd());
	u2Index = m_pConstPool->Add(pcpnti);
	CCPFieldrefInfo* pcpfri = new CCPFieldrefInfo(m_u2ThisClass, u2Index);
	u2Index = m_pConstPool->Add(pcpfri);
	pcpfri = (CCPFieldrefInfo*)((*m_pConstPool)[u2Index]);
	return pcpfri;
}

//------------------------------------------------------------------------------
// CreateMethodRef
// In:
//	i_pmi - pointer to the method info
// Returns:
//	CCPMethodrefInfo - pointer to the method ref info	
//
// Creates a constant pool reference to a method.
// Takes same approach as a field creation.
// Internal method creation is split to two atomic operations:
// Method creation and Reference to the Method creation (as we can have a method
// without internal reference).
// The external reference creation is a singleton.
//
CCPMethodrefInfo*
CJClassBuilder::CreateMethodRef(CJMethodInfo* i_pmi)
{
	CCPNameAndTypeInfo* pcpnti = new CCPNameAndTypeInfo(i_pmi->GetNameInd(),
		                                                i_pmi->GetDescriptorInd());
	u2 u2NameAndType = m_pConstPool->Add(pcpnti);
	CCPMethodrefInfo* pcpmri = new CCPMethodrefInfo(m_u2ThisClass, pcpnti->GetCpIndex());
	u2 u2MethodRef = m_pConstPool->Add(pcpmri);
	pcpmri = (CCPMethodrefInfo*)((*m_pConstPool)[u2MethodRef]);
	return pcpmri;
}

//------------------------------------------------------------------------------
// CreateInterfaceMethodRef
// In:
//	i_szName - method name
//	i_szSignature - method signature (in Java internal format)
//	i_szClass - Interface class name
// Returns:
//	CCPInterfaceMethodrefInfo* - pointer to the interface method ref in the
//                               const. pool
// Creates a constant pool reference to an interface method
//
CCPInterfaceMethodrefInfo*
CJClassBuilder::CreateInterfaceMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass)
{
	CCPInterfaceMethodrefInfo* pcpMethodRef = NULL;

	// Add class info
	u2 u2ClassName = m_pConstPool->Add(new CCPUtf8Info(i_szClass));
	u2 u2ClassRef  = m_pConstPool->Add(new CCPClassInfo(u2ClassName));

	// Add name and type ref
	u2 u2Signature   = m_pConstPool->Add(new CCPUtf8Info(i_szSignature));
	u2 u2MethodName	= m_pConstPool->Add(new CCPUtf8Info(i_szName));
	u2 u2NameAndType = m_pConstPool->Add(new CCPNameAndTypeInfo(u2MethodName, u2Signature));

	// Add Interface method ref
	pcpMethodRef = new CCPInterfaceMethodrefInfo(u2ClassRef, u2NameAndType);
	u2 u2MethodRef = m_pConstPool->Add(pcpMethodRef);
	pcpMethodRef = (CCPInterfaceMethodrefInfo*)((*m_pConstPool)[u2MethodRef]);

	return pcpMethodRef;
}

//------------------------------------------------------------------------------
// CreateInterfaceMethodRef
// In:
//	i_jmn - CJavaMethodName (see JavaHelpers.h)
//	i_szClass - Interface class name
// Returns:
//	CCPInterfaceMethodrefInfo* - pointer to the interface method ref in the
//                               const. pool
// A different form of the interface method reference creation
//
CCPInterfaceMethodrefInfo*
CJClassBuilder::CreateInterfaceMethodRef(CJavaMethodName& i_jmn, CSTR i_szClass)
{
	return CreateInterfaceMethodRef(i_jmn.GetName(), i_jmn.GetSignature(), i_szClass);
}

//------------------------------------------------------------------------------
// CreateExtFieldRef
// In:
//	i_szName - Field name
//	i_jtype  - Field type (see JAvaHelpers.h)
// Returns:
//	CCPFieldRefInfo* - pointer to the field ref info in the constant pool
//
// Create extrnal field reference.
// Note that the Class Builder does not attempt to resolve the external field
// reference. It just creates a reference with the specified name and type.
// If the field properties are not correct the JVM will throw a runtime
// exception.
//
CCPFieldrefInfo*
CJClassBuilder::CreateExtFieldRef(CSTR i_szName, CJavaType& i_jtype, CSTR i_szClass)
{
	// Add Name and type info
	u2 u2Name = m_pConstPool->Add(new CCPUtf8Info(i_szName));
	u2 u2Type = m_pConstPool->Add(new CCPUtf8Info(i_jtype.GetTypeString().c_str()));
	CCPNameAndTypeInfo* pcpnti = new CCPNameAndTypeInfo(u2Name, u2Type);
	u2 u2NameAndType = m_pConstPool->Add(pcpnti);
	// Add class info
	u2 u2ClassName = m_pConstPool->Add(new CCPUtf8Info(i_szClass));
	u2 u2ClassRef  = m_pConstPool->Add(new CCPClassInfo(u2ClassName));

	CCPFieldrefInfo* pcpfri = new CCPFieldrefInfo(u2ClassRef, u2NameAndType);
	u2 u2FieldRef = m_pConstPool->Add(pcpfri);
	pcpfri = (CCPFieldrefInfo*)((*m_pConstPool)[u2FieldRef]);
	return pcpfri;
}

//------------------------------------------------------------------------------
// CreateExtMethodRef
// In:
//	i_szName - method name
//	i_szSignature - method signature
//	i_szClass - Class package and name (in the internal slash separated format).
// Returns:
//	CCPMethodRefInfo* - pointer to the method ref onformation in the constant pool
// Creates an external method reference
//
CCPMethodrefInfo*
CJClassBuilder::CreateExtMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass)
{
	CCPMethodrefInfo* pcpMethodRef = NULL;

	u2 u2ClassName = m_pConstPool->Add(new CCPUtf8Info(i_szClass));
	u2 u2ClassRef  = m_pConstPool->Add(new CCPClassInfo(u2ClassName));

	//
	u2 u2Signature   = m_pConstPool->Add(new CCPUtf8Info(i_szSignature));
	u2 u2MethodName	= m_pConstPool->Add(new CCPUtf8Info(i_szName));
	u2 u2NameAndType = m_pConstPool->Add(new CCPNameAndTypeInfo(u2MethodName, u2Signature));

	// Add Interface method ref
	pcpMethodRef = new CCPMethodrefInfo(u2ClassRef, u2NameAndType);
	u2 u2MethodRef = m_pConstPool->Add(pcpMethodRef);
	pcpMethodRef = (CCPMethodrefInfo*)((*m_pConstPool)[u2MethodRef]);

	return pcpMethodRef;
}

//------------------------------------------------------------------------------
// CreateExtMethodRef
// In:
//	i_jmn - CJavaMethodName (See JavaHelpers.h)
//	i_szClass - Class package and name (in the internal slash separated format).
// Returns:
//	CCPMethodRefInfo* - pointer to the method ref onformation in the constant pool
//
// Another form of CreateExtMethodRef
//
CCPMethodrefInfo*
CJClassBuilder::CreateExtMethodRef(CJavaMethodName& i_jmn, CSTR i_szClass)
{
	return CreateExtMethodRef(i_jmn.GetName(), i_jmn.GetSignature(), i_szClass);
}

//------------------------------------------------------------------------------
// AddConstructor
// In:
//	i_szSignature - constructor signature
// Returns:
//	CJMethodInfo* - pointer to the method info in the constant pool.
// Helper method to create a constructor with Java constructor name and the
// appropriate signature.
// Note in order to path verification constructor must call the super constructor.
//
//
CJMethodInfo*
CJClassBuilder::AddConstructor(CSTR i_szSignature)
{
	return CreateMethod(ACC_PRIVATE, "<init>", i_szSignature);
}

//------------------------------------------------------------------------------
// AddDefaultConstructor
// Returns:
//	CJMethodInfo* - pointer to the method info in the constant pool.
//
// Helper method. Creates default constructor.
//
CJMethodInfo*
CJClassBuilder::AddDefaultConstructor()
{
	return CreateMethod(ACC_PRIVATE, "<init>", "()V");
}

//------------------------------------------------------------------------------
// AddStaticConstructor
// Returns:
//	CJMethodInfo* - pointer to the method info in the constant pool.
//
// Helper method. Creates static constructor.
//
CJMethodInfo*
CJClassBuilder::AddStaticConstructor()
{
	return CreateMethod(ACC_STATIC|ACC_PRIVATE, "<clinit>", "()V");
}

//= CJClassBuilder.cpp ==========================================================
