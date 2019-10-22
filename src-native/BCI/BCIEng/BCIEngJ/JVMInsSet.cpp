/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JVMInsSet.cpp,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// JVMInsSet.cpp
// 
//------------------------------------------------------------------------------
// Description 
// JVM Instruction Set.
// (see JVMInsSet.h)
// 
//==============================================================================
#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif
#include "ModuleJ.h"
#include "JVMInsSet.h"
#include "JavaDef.h"
#include "JavaHelpers.h"


// Calculate padding bytes for switch instructions
inline static unsigned CalcPad(IP_t ip)	
{
	return ++ip % 4 ? 4 - ip % 4 : 0;
}


// These descriptors are returned by the CInsDescr_WideJ factory method.
static CInsDescr_WideLoadStoreJ	wide_load_store_descr (JVMI_wide, "wide load/store", 4, SEM_GEN);
static CInsDescr_WideIIncJ		wide_iinc_descr (JVMI_wide, "wide iinc", 6, SEM_GEN);
static CInsDescr_WideRetJ		wide_ret_descr (JVMI_wide, "wide ret", 4, SEM_RETSR);

// Mnemonics used for WideLoadStoreJ instructions
static struct {
	BYTE opcode;
	const char* mnemonic;
} wide_load_store_mnemonic[] = {
	{ JVMI_iload, "wide iload" },
	{ JVMI_aload, "wide aload" },
	{ JVMI_dload, "wide dload" },
	{ JVMI_fload, "wide fload" },
	{ JVMI_lload, "wide lload" },
	{ JVMI_istore, "wide istore", },
	{ JVMI_astore, "wide astore", },
	{ JVMI_dstore, "wide dstore", },
	{ JVMI_fstore, "wide fstore", },
	{ JVMI_lstore, "wide lstore", }
};

//------------------------------------------------------------------------------
// Java instruction list

static CInsDescr JInsList[] = {
	#include "JVMIns.def"
};

const unsigned JInsListSize = sizeof(JInsList)/sizeof(*JInsList);

//==============================================================================
// CInsSetJ implementation
//------------------------------------------------------------------------------
// Constructor
CInsSetJ::CInsSetJ()
:CInsSet(JInsList, JInsListSize)
{
	//------------------------------------------------------------------------------
	// Note: The following tables are nested into CInsSetJ class on purpose.
	// This makes sure that the static construction order is correct.
	//
	// The following tables have to contain an instruction descriptor
	// for every instruction that is handled by a subclass of CInsDescr
	// (as opposed to generics that are handled by the base class).
	//
	// The JInsList array has an instruction descriptor per opcode value.
	// It is populated initially by #including the JVMIns.def file.
	// Then the CInsSetJ constructor replaces some of the entries 
	// with the subclass objects in these arrays below.
	//
	// We have multiple tables because C++ doesn't let you create an
	// array containing objects of different types, even when they're
	// all subclasses of a common base.
	//

	//------------------------------------------------------------------------------
	// Branch instructions
	//
	static CInsDescr_BranchJ branch_set[] =
	{
		CInsDescr_BranchJ(153, "ifeq",			3, -1, SEM_BRC),
		CInsDescr_BranchJ(154, "ifne",			3, -1, SEM_BRC), 
		CInsDescr_BranchJ(155, "iflt",			3, -1, SEM_BRC), 
		CInsDescr_BranchJ(156, "ifge",			3, -1, SEM_BRC),
		CInsDescr_BranchJ(157, "ifgt",			3, -1, SEM_BRC), 
		CInsDescr_BranchJ(158, "ifle",			3, -1, SEM_BRC), 
		CInsDescr_BranchJ(159, "if_icmpeq",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(160, "if_icmpne",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(161, "if_icmplt",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(162, "if_icmpge",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(163, "if_icmpgt",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(164, "if_icmple",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(165, "if_acmpeq",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(166, "if_acmpne",		3, -2, SEM_BRC), 
		CInsDescr_BranchJ(167, "goto",			3,  0, SEM_BR), 
		CInsDescr_BranchJ(168, "jsr",			3,  0, SEM_JSR),
		CInsDescr_BranchJ(198, "ifnull",		3, -1, SEM_BRC),
		CInsDescr_BranchJ(199, "ifnonnull",		3, -1, SEM_BRC)
	};

	//------------------------------------------------------------------------------
	// Switch instructions
	//
	static CInsDescr_SwitchJ switch_set[] =
	{
		CInsDescr_SwitchJ(170, "tableswitch",  (unsigned)-1, -1, SEM_SWITCH),
		CInsDescr_SwitchJ(171, "lookupswitch", (unsigned)-1, -1, SEM_SWITCH) 
	};


	//------------------------------------------------------------------------------
	// Wide branch instructions
	//
	static CInsDescr_Branch_wJ	brwide_set[] =
	{
		CInsDescr_Branch_wJ(JVMI_goto_w, "goto_w",	5, 0, SEM_BR),
		CInsDescr_Branch_wJ(JVMI_jsr_w,  "jsr_w",   5, 0, SEM_JSR)
	};

	//------------------------------------------------------------------------------
	// Java "Invoke" instructions
	//

	static CInsDescr_InvokeJ invoke_set[] = 
	{
		CInsDescr_InvokeJ(JVMI_invokeinterface, "invokeinterface", 5, SEM_CALL),
		CInsDescr_InvokeJ(JVMI_invokespecial, "invokespecial", 3, SEM_CALL),
		CInsDescr_InvokeJ(JVMI_invokestatic, "invokestatic", 3, SEM_CALL),
		CInsDescr_InvokeJ(JVMI_invokevirtual, "invokevirtual", 3, SEM_CALL),
	};

	//------------------------------------------------------------------------------
	// Get/Put instructions
	//
	static CInsDescr_GetPutJ getput_set[] =
	{
		CInsDescr_GetPutJ(JVMI_getstatic, "getstatic", 3, SEM_GEN),
		CInsDescr_GetPutJ(JVMI_putstatic, "putstatic", 3, SEM_GEN),
		CInsDescr_GetPutJ(JVMI_getfield,  "getfield",  3, SEM_GEN),
		CInsDescr_GetPutJ(JVMI_putfield,  "putfield",  3, SEM_GEN)
	};


	//------------------------------------------------------------------------------
	// Other non-trivial instructions
	//
	static CInsDescr_MultiANewArrayJ jvmi_multianewarray(JVMI_multianewarray, 
		                             "multianewarray", 4, SEM_GEN);
	static CInsDescr_WideJ		jvmi_wide  (JVMI_wide,   "wide",   (unsigned)-1, 0, SEM_GEN);

	//Replacing non-trivial instructions in the instruction set
	// This clumsy "(*this)[x] = y;" syntax is required because
	// GCC 2.9x on Linux has no "vector::at()" implementation.
	// TODO: change syntax to "at(x) = y" when we dump 2.9x compiler support
	int i;
	for(i = 0; i < SIZE_OF(branch_set); i++)
	{
		(*this)[branch_set[i].GetOpCode()] = &branch_set[i];
	}
	for(i = 0; i < SIZE_OF(switch_set); i++)
	{
		(*this)[switch_set[i].GetOpCode()] = &switch_set[i];
	}
	for(i = 0; i < SIZE_OF(brwide_set); i++)
	{
		(*this)[brwide_set[i].GetOpCode()] = &brwide_set[i];
	}
	for (i = 0; i < SIZE_OF(invoke_set); i++) 
	{
		(*this)[invoke_set[i].GetOpCode()] = &invoke_set[i];
	}
	for (i = 0; i < SIZE_OF(invoke_set); i++) 
	{
		(*this)[getput_set[i].GetOpCode()] = &getput_set[i];
	}
	(*this)[JVMI_multianewarray] = &jvmi_multianewarray;
	(*this)[JVMI_wide]   = &jvmi_wide;
}

//------------------------------------------------------------------------------
// Instruction creation helpers are going below
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_simple(JVMI_t i_JVMI)
{
		CInsDescr* pInsDescr = &JInsList[i_JVMI];
		if(pInsDescr->GetOpCode() != i_JVMI)
		{	// Instruction list is messed up?
			throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR);
		}
		if(pInsDescr->GetSize() != 1)
		{	// Oops, this is not a simple instruction!
			throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR);
		}
		static unsigned char Op[1];
		Op[0] = pInsDescr->GetOpCode();
		CInstruction* pins = new CInstruction(pInsDescr->GetMnem(), 
			                                  pInsDescr->GetSemTag(),
											  Op, 1, pInsDescr->GetStack());
		return pins;
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_ldc(unsigned i_CpInd)
{
	if(i_CpInd < 256)
	{
		CInsDescr* pDescr = &JInsList[JVMI_ldc];
		unsigned char code[2] = {JVMI_ldc};
		BYTE arg = (BYTE)i_CpInd;
		*(BYTE*)&code[1] = arg; // no need of memcpy as this is a byte
		return new CInstruction(pDescr->GetMnem(), 
			                    pDescr->GetSemTag(), 
								code, SIZE_OF(code), pDescr->GetStack());
	}
	else
	{
		CInsDescr* pDescr = &JInsList[JVMI_ldc_w];
		unsigned char code[3] = {JVMI_ldc_w};
		short arg = LE_WORD(i_CpInd);
		MEMCPY2(&code[1], &arg);
		return new CInstruction(pDescr->GetMnem(), 
			                    pDescr->GetSemTag(), code, SIZE_OF(code),
								pDescr->GetStack());
								
	}
}

//------------------------------------------------------------------------------
// Create_push_short_constant pushes a constant short 
// integer on the stack, using the most compact encoding
// available.
// If your constant doesn't fit in a short, you'll need to
// create a constant pool entry and use ldc.
//
// Assumes that enum values in JVMI_t are in the same
// order as the instruction set.

CInstruction*
CInsSetJ::Create_push_constant(int i_Const)
{
	// Short form for -1 through 5
	if (i_Const >= -1 && i_Const <= 5) {
		return Create_simple((JVMI_t)(JVMI_iconst_0 + i_Const));
	}
	else if (i_Const >= -128 && i_Const <= 127) {
		return Create_push((char)i_Const);
	}
	else if (i_Const >= -32768 && i_Const <= 32767) {
		return Create_push((short)i_Const);
	}
	else {
		// You called this with a constant that needs four bytes
		throw CModuleException(CModuleException::X_REASON_INVALID_CALL);
	}
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_getstatic(unsigned short i_Ref)
{
	BYTE code[3] = {JVMI_getstatic};
	short scratch = LE_WORD(i_Ref);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[JVMI_getstatic];
	return new CInstruction_GetPutJ(pDescr->GetMnem(), 
			                code, SIZE_OF(code),
							pDescr->GetStack());
}


//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_newarray(BYTE i_Type)
{
	BYTE code[2] = {JVMI_newarray};
	code[1] = i_Type;
	CInsDescr* pDescr = &JInsList[JVMI_newarray];
	return new CInstruction(pDescr->GetMnem(), 
			                pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_push(char  i_Byte)
{
	BYTE code[2] = {JVMI_bipush};
	code[1] = i_Byte;
	CInsDescr* pDescr = &JInsList[JVMI_bipush];
	return new CInstruction(pDescr->GetMnem(), 
			                pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_push(short i_Short)
{
	BYTE code[3] = {JVMI_sipush};
	short scratch = LE_WORD(i_Short);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[JVMI_sipush];
	return new CInstruction(pDescr->GetMnem(), 
			                pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_putstatic(unsigned short i_Ref)
{
	BYTE code[3] = {JVMI_putstatic};
	short scratch = LE_WORD(i_Ref);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[JVMI_putstatic];
	return new CInstruction_GetPutJ(pDescr->GetMnem(), 
			                code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
// TODO: reimplement with individual load/store instruction builders
//
static CInstruction*
create_load_store(const char* mnem, JVMI_t opcode, JVMI_t quick_opcode, int i_slotNum)
{
	BYTE code[4];
	int codeSize;
	CInsDescr* pDescr = &JInsList[opcode];
	if (i_slotNum > 255) {
		// Use wide form
		code[0] = JVMI_wide;
		code[1] = opcode;
		short scratch = LE_WORD(i_slotNum);
		MEMCPY2(&code[2], &scratch);

		// get the mnemonic from wide_load_store_mnemonic since it's wide
		for (int i = 0; ; i++) {
			if (wide_load_store_mnemonic[i].opcode == opcode) {
				mnem = wide_load_store_mnemonic[i].mnemonic;
				break;
			}
		}
		return new CInstruction(mnem, SEM_GEN, code, 4, pDescr->GetStack());
	}
	else if (i_slotNum <= 3) {
		// Use quick form
		code[0] = quick_opcode + i_slotNum;
		codeSize = 1;
	}
	else {
		// Use standard form
		code[0] = opcode;
		code[1] = i_slotNum;
		codeSize = 2;
	}
	return new CInstruction(mnem, SEM_GEN, code, codeSize, pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_astore(int i_slotNum)
{ 
	return create_load_store("astore", JVMI_astore, JVMI_astore_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_istore(int i_slotNum)
{ 
	return create_load_store("istore", JVMI_istore, JVMI_istore_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_lstore(int i_slotNum)
{ 
	return create_load_store("lstore", JVMI_lstore, JVMI_lstore_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_aload(int i_slotNum)
{ 
	return create_load_store("aload", JVMI_aload, JVMI_aload_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_iload(int i_slotNum)
{ 
	return create_load_store("iload", JVMI_iload, JVMI_iload_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_fload(int i_slotNum)
{ 
	return create_load_store("fload", JVMI_fload, JVMI_fload_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_dload(int i_slotNum)
{ 
	return create_load_store("dload", JVMI_dload, JVMI_dload_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_lload(int i_slotNum)
{ 
	return create_load_store("lload", JVMI_lload, JVMI_lload_0, i_slotNum);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_iinc(int i_slotNum, int amount)
{
	BYTE code[3];
	code[0] = JVMI_iinc;
	code[1] = i_slotNum;
	code[2] = amount;
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_anewarray(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_anewarray;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_multianewarray(unsigned i_CpInd, BYTE i_Dimensions)
{
	BYTE code[4];
	code[0] = JVMI_multianewarray;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
    code[3] = i_Dimensions;
	CInsDescr* pDescr = &JInsList[JVMI_multianewarray];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, SIZE_OF(code),
							1 - i_Dimensions);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_new(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_new;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_checkcast(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_checkcast;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, SIZE_OF(code),
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_invokespecial(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_invokespecial;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction_InvokeJ(pDescr->GetMnem(),
							code, SIZE_OF(code), 0);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_invokevirtual(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_invokevirtual;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction_InvokeJ(pDescr->GetMnem(),
									code, SIZE_OF(code), 0);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_invokeinterface(unsigned i_CpInd, BYTE i_Count)
{
	BYTE code[5];
	code[0] = JVMI_invokeinterface;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
    code[3] = i_Count;
    code[4] = 0;
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction_InvokeJ(pDescr->GetMnem(),
									code, SIZE_OF(code), 0);
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_invokestatic(unsigned i_CpInd)
{
	BYTE code[3];
	code[0] = JVMI_invokestatic;
	short scratch = LE_WORD(i_CpInd);
	MEMCPY2(&code[1], &scratch);
	CInsDescr* pDescr = &JInsList[code[0]];
	return new CInstruction_InvokeJ(pDescr->GetMnem(),
									code, SIZE_OF(code), 0);
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_jsr(unsigned short i_u2Target)
{
	const BYTE code[3] = {JVMI_jsr, 0, 0};
	CInsDescr* pDescr = &JInsList[code[0]];
	CInstruction_BranchJ* piJsr = new CInstruction_BranchJ(
		pDescr->GetMnem(), pDescr->GetSemTag(), 
		code, sizeof(code), pDescr->GetStack(),
		i_u2Target
	);
	return piJsr;
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_if(JVMI_t opcode, unsigned short i_u2Target)
{
	// opcode is (for example) JVMI_if_icmplt - the opcode itself
	const BYTE code[3] = {opcode, 0, 0};
	CInsDescr* pDescr = &JInsList[code[0]];
	CInstruction_BranchJ* piIf = new CInstruction_BranchJ(
		pDescr->GetMnem(), pDescr->GetSemTag(), 
		code, sizeof(code), pDescr->GetStack(),
		i_u2Target
	);
	return piIf;
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_ret(unsigned short i_slotNum)
{
	BYTE code[4];
	CInsDescr* pDescr;
	if (i_slotNum > 255) {
		// Emit wide form
		code[0] = JVMI_wide;
		code[1] = JVMI_ret;
		pDescr = &JInsList[code[0]];
		short scratch = LE_WORD(i_slotNum);
		MEMCPY2(&code[2], &scratch);
		return new CInstruction("wide ret", SEM_RETSR, code, 4, pDescr->GetStack());
	}

	code[0] = JVMI_ret;
	code[1] = (u1)i_slotNum;
	pDescr = &JInsList[code[0]];
	return new CInstruction(pDescr->GetMnem(),
							pDescr->GetSemTag(), code, 2,
							pDescr->GetStack());
}

//------------------------------------------------------------------------------
CInstruction* 
CInsSetJ::Create_goto(unsigned short i_target)
{
	const BYTE code[3] = {JVMI_goto, 0, 0};
	CInsDescr* pDescr = &JInsList[code[0]];
	CInstruction_BranchJ* piGoto = new CInstruction_BranchJ(
		pDescr->GetMnem(), pDescr->GetSemTag(), 
		code, sizeof(code), pDescr->GetStack(),
		i_target
	);
	return piGoto;
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_placeholder(IP_t i_OrigIP)
{
	CInstruction* ins = new CInstruction("<placeholder>", SEM_PLACEHOLDER, NULL, 0, 0);
	ins->SetIP(i_OrigIP);
	return ins;
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_tableswitch(unsigned i_Low, unsigned i_High, 
                             const vector<BrTarget_t> &i_BranchTable)
{
	CInstruction_TableSwitchJ* ins = new CInstruction_TableSwitchJ();
    ins->SetLow(i_Low);
    ins->SetHigh(i_High);
    vector<BrTarget_t> &insBranchTable = ins->GetBranchTable();
    CInstruction_Switch::iterator iter = i_BranchTable.begin();
    CInstruction_Switch::iterator iterEnd = i_BranchTable.end();
    for (; iter != iterEnd; ++iter)
    {
        insBranchTable.push_back(*iter);
    }
	return ins;
}

//------------------------------------------------------------------------------
CInstruction*
CInsSetJ::Create_lookupswitch(const vector<BrTarget_t> &i_BranchTable,
                              const vector<unsigned> &i_LookupTable)
{
	CInstruction_LookupSwitchJ* ins = new CInstruction_LookupSwitchJ();
    unsigned int uiPairs = i_BranchTable.size();
    ins->SetPairs(uiPairs - 1);
    vector<BrTarget_t> &insBranchTable = ins->GetBranchTable();
    vector<unsigned> &insLookupTable = ins->GetLookupTable();
    unsigned int i;
    for (i = 0; i < uiPairs; ++i)
    {
        insBranchTable.push_back(i_BranchTable[i]);
        insLookupTable.push_back(i_LookupTable[i]);
    }
	return ins;
}

//==============================================================================
// class CInstruction_InvokeJ
//
// Overrides GetStack(). 
// Implements a new getter, GetCpIndex() to get
// the constant pool index of the method being invoked.
//

//------------------------------------------------------------------------------
CInstruction_InvokeJ::CInstruction_InvokeJ(CSTR i_Mnem, const BYTE* i_pCode,
						unsigned i_Size, int i_Stack)
:CInstruction(i_Mnem, SEM_CALL, i_pCode, i_Size, i_Stack)
{
}

//------------------------------------------------------------------------------
int	
CInstruction_InvokeJ::GetStack(const CMethod& i_meth)
{
	if(m_Stack != 0)
		return m_Stack;
	CModuleJ& jmod = *(CModuleJ*)i_meth.GetModule();
	CJClassFile& jclass = jmod.GetClass();
	CConstPool& cp = *jclass.GetConstPool();
	CCPUtf8Info& utf8Sig = *cp.GetMethodType(GetCpIndex());
	CJavaMethodName jmtname;
	string strSig = (string)utf8Sig;
	jmtname.SetSignature(strSig.c_str());
	m_Stack = jmtname.GetStackSize();
	if(GetOpCode() != JVMI_invokestatic)
		m_Stack--; // Pop this pointer
	return m_Stack;
}

//------------------------------------------------------------------------------
unsigned short CInstruction_InvokeJ::GetCpIndex() const
{
	short high = m_pCode[1];
	short low = m_pCode[2];

	// Paranoia mode: made absolutely sure there was no sign extension
	high &= 0xff;
	low &= 0xff;

	return ((high << 8) | low);
}

//==============================================================================
// CInstruction_GetPutJ
//------------------------------------------------------------------------------
CInstruction_GetPutJ::CInstruction_GetPutJ(CSTR i_Mnem, const BYTE* i_pCode, 
										   unsigned i_Size, int i_Stack)
:CInstruction(i_Mnem, SEM_GEN, i_pCode, i_Size, i_Stack)
{}

//------------------------------------------------------------------------------
int	
CInstruction_GetPutJ::GetStack(const CMethod& i_meth)
{
	if(m_Stack != 0)
		return m_Stack;
	unsigned CpInd = GetCpIndex();
	CModuleJ& jmod = *(CModuleJ*)i_meth.GetModule();
	CJClassFile& jclass = jmod.GetClass();
	CConstPool& cp = *jclass.GetConstPool();
	CCPFieldrefInfo* pfield =  (CCPFieldrefInfo*)(cp[CpInd]);
	CCPUtf8Info& utf8Type = *cp.GetType(pfield->GetNameAndTypeInd());
	CJavaType jt;
	string strType = (string)utf8Type;
	jt.Parse(strType.c_str());
	int nStack = jt.GetStackSize();
	switch(m_OpCode)
	{
	case JVMI_getstatic:
		m_Stack = nStack;
		break;
	case JVMI_putstatic:
		m_Stack = -nStack;
		break;
	case JVMI_getfield:
		m_Stack = nStack - 1;
		break;
	case JVMI_putfield:
		m_Stack = -nStack - 1;
		break;
	}
	m_Stack;
	return m_Stack;
}

//------------------------------------------------------------------------------
unsigned short 
CInstruction_GetPutJ::GetCpIndex() const
{
	short high = m_pCode[1];
	short low = m_pCode[2];
	high &= 0xff;
	low &= 0xff;

	return ((high << 8) | low);
}

//==============================================================================
// CInstruction_BranchJ
//
//------------------------------------------------------------------------------
CInstruction_BranchJ::CInstruction_BranchJ(CSTR i_Mnem, SemTag_t i_SemTag,
				      const BYTE* i_pCode, unsigned i_Size, int i_Stack, 
					  unsigned i_BranchTarget)
:CInstruction_Branch(i_Mnem, i_SemTag, i_pCode, i_Size, i_Stack, i_BranchTarget)
{}

//------------------------------------------------------------------------------
void 
CInstruction_BranchJ::Emit(CMethod& i_Method) const
{
	CMethodBody* pbody = i_Method.GetBody();
	CLabels* plabels = i_Method.GetLabels();
	IP_t IP = pbody->GetIP();
	long longTarget = plabels->GetLabelBlockTarget(m_BranchTarget) - IP;
	short target = (short)longTarget;
	int intTarget = (int)longTarget;
	if ( intTarget > (int)0x00007FFF || intTarget < (int)0xFFFF8000) // Java code array is 4GB max
	{
		// Target was truncated when moved to a short; it cannot be
		// represented in 16 bits.  We could resolve this with branch
		// islands but that is more work than it's worth, don't
		// instrument this module.
		throw CModuleException(CModuleException::X_REASON_CODE_OVERRUN, i_Method.GetModule()->GetName());
	}
	target = LE_WORD(target);	// Possibly byte-swapped

	// m_OpCode is 4 bytes wide and the opcode is
	// in the first byte for WIN32 and the last byte for UNIX.
	// This assignment handles the difference.
	BYTE opCode = m_OpCode;

	pbody->Inject(&opCode, 1);
	pbody->Inject((BYTE*)&target, 2);
}

//==============================================================================
// class CInstruction_BranchJ
//
//------------------------------------------------------------------------------
CInstruction_Branch_wJ::CInstruction_Branch_wJ(CSTR i_Mnem, SemTag_t i_SemTag, 
                        const BYTE* i_pCode, unsigned i_Size, int i_Stack, 
                        unsigned i_BranchTarget)
:CInstruction_Branch(i_Mnem, i_SemTag, i_pCode, i_Size, i_Stack,i_BranchTarget)
{}

//------------------------------------------------------------------------------
void 
CInstruction_Branch_wJ::Emit(CMethod& i_Method) const
{
	CMethodBody* pbody = i_Method.GetBody();
	CLabels* plabels = i_Method.GetLabels();
	IP_t IP = pbody->GetIP();
	unsigned target = plabels->GetLabelBlockTarget(m_BranchTarget);
	target = LE_DWORD(target - IP);

	// m_OpCode is 4 bytes wide and the opcode is
	// in the first byte for WIN32 and the last byte for UNIX.
	// This assignment handles the difference.
	BYTE opCode = m_OpCode;

	pbody->Inject(&opCode, 1);
	pbody->Inject((BYTE*)&target, 4);
}

//==============================================================================
// CInstruction_TableSwitchJ implementation
//
//------------------------------------------------------------------------------
// Constructor
// Constructs the TableSwitch instruction based on the code from the Method Body
// 
// In: 
//		i_meth - Current method body
//
// Note: You may ask why the parsing/emission code for the TableSwitch instruction
// doesnt't use pointer to the instruction structure but instead is processing
// the code byte after byte. The unswer is: because of the allignment bytes.
// Then you may ask why not to use #pragma:pack(push, 4) and align the data 
// properly? Because I didn't want to make this project data alignment or
// compiler sensetive. Ater all, switch is a rare insruction so 
// the process-it-hard-way approach can't bring much of the overhead.
// 
CInstruction_TableSwitchJ::CInstruction_TableSwitchJ(const CMethodBody& i_meth)
:CInstruction_Switch(JInsList[JVMI_tableswitch].GetMnem())
{
	m_Stack = -1;
	unsigned ip = i_meth.GetIP();
	unsigned dflt = 0;
	unsigned ntargets = 0;
	unsigned ind = 0;
	const Code_t* pCode = i_meth.GetCode() + ip;
	if(*pCode != JInsList[JVMI_tableswitch].GetOpCode())
	{
		//ToDo: throw something
	}
    else
    {
        // update opcode
        m_OpCode = *pCode;
    }

	// I calculate all this offsets, because I didn't want this code
	// to be compiler allignment sensitive.
	ip++;
	ip += (ip % 4) ? 4 - ip % 4 : 0;
	MEMCPY4(&dflt, i_meth.GetCode() + ip);
	dflt = LE_DWORD(dflt);
	ip += 4;
	MEMCPY4(&m_Low, i_meth.GetCode() + ip);
	m_Low = LE_DWORD(m_Low);
	ip += 4;
	MEMCPY4(&m_High, i_meth.GetCode() + ip);
	m_High = LE_DWORD(m_High);
	ip += 4;
	ntargets = m_High - m_Low + 1;			// Num. of targets in the switch
	ntargets++;			     				// Plus default value
	m_BranchTable.resize(ntargets);

	m_BranchTable[0] = i_meth.GetIP() + dflt;	// Default is always 0 element
	for(ind = 1; ind < ntargets; ind++)
	{
	        int offset;
		MEMCPY4(&offset, i_meth.GetCode() + ip);
		offset = LE_DWORD(offset);
		m_BranchTable[ind] = i_meth.GetIP() + offset;
		ip += 4;
	}
}

CInstruction_TableSwitchJ::CInstruction_TableSwitchJ()
:CInstruction_Switch(JInsList[JVMI_tableswitch].GetMnem())
{
}

//------------------------------------------------------------------------------
void	
CInstruction_TableSwitchJ::Emit(CMethod& i_Method) const
{
	unsigned target; // New branch target
	BYTE Padding[5] = {JVMI_tableswitch, 0, 0, 0, 0};
	CMethodBody* pBody = i_Method.GetBody();
	CLabels* plabels = i_Method.GetLabels();
	IP_t StartIP = pBody->GetIP();
	unsigned PaddingSize = 1 + CalcPad(StartIP);
	pBody->Inject(Padding, PaddingSize);
	target = plabels->GetLabelBlockTarget(m_BranchTable[0]);
	unsigned dflt =  target - StartIP;
	dflt = LE_DWORD(dflt);
	pBody->Inject((BYTE*)&dflt, sizeof(dflt));
	unsigned low = LE_DWORD(m_Low);
	unsigned high = LE_DWORD(m_High);
	pBody->Inject((BYTE*)&low, sizeof(low));
	pBody->Inject((BYTE*)&high, sizeof(high));
	for(int ind = 1; ind <= m_High - m_Low + 1; ind++)
	{
		target = plabels->GetLabelBlockTarget(m_BranchTable[ind]);
		target = LE_DWORD(target - StartIP);
		pBody->Inject((BYTE*)&target, sizeof(target));
	}
}

//------------------------------------------------------------------------------
void	
CInstruction_TableSwitchJ::Dump(ostream& i_os, CMethod& i_Method) const
{
	CInstruction_Switch::Dump(i_os, i_Method);
}

//------------------------------------------------------------------------------
unsigned	
CInstruction_TableSwitchJ::GetSize(IP_t i_IP) const
{
	if(i_IP == -1)
	{
		//ToDo: throw an exception
	}
	int allign = CalcPad(i_IP);
	return 1 + allign + 12 + (m_High - m_Low + 1) * 4;
}

//==============================================================================
// CInstruction_LookupSwitchJ implementation
//
//------------------------------------------------------------------------------
// Constructor
CInstruction_LookupSwitchJ::CInstruction_LookupSwitchJ(const CMethodBody& i_meth)
:CInstruction_Switch(JInsList[JVMI_lookupswitch].GetMnem())
{
	m_Stack = -1;
	unsigned ip = i_meth.GetIP();
	unsigned dflt = 0;
	unsigned ind = 0;
	const Code_t* pCode = i_meth.GetCode() + ip;
	if(*pCode != JInsList[JVMI_lookupswitch].GetOpCode())
	{
		//ToDo: throw something
	}
    else
    {
        // update opcode
        m_OpCode = *pCode;
    }
	ip++;
	ip += (ip % 4) ? 4 - ip % 4 : 0;
	MEMCPY4(&dflt, i_meth.GetCode() + ip);
	dflt = LE_DWORD(dflt);
	ip += 4;
	MEMCPY4(&m_Pairs, i_meth.GetCode() + ip);
	m_Pairs = LE_DWORD(m_Pairs);
	ip += 4;
	m_BranchTable.resize(m_Pairs + 1);
	m_LookupTable.resize(m_Pairs + 1);
	m_BranchTable[0] = i_meth.GetIP() + dflt;	// Default always 0 element
	for(ind = 0; ind < m_Pairs; ind++)
	{
	        int value, offset;
		MEMCPY4(&value, i_meth.GetCode() + ip);
		value = LE_DWORD(value);
		m_LookupTable[ind + 1] = value;
		MEMCPY4(&offset, i_meth.GetCode() + ip + 4);
		offset = LE_DWORD(offset);
		m_BranchTable[ind + 1] = i_meth.GetIP() + offset;
		ip += 8;
	}
}

CInstruction_LookupSwitchJ::CInstruction_LookupSwitchJ()
:CInstruction_Switch(JInsList[JVMI_lookupswitch].GetMnem())
{
}

//------------------------------------------------------------------------------
// ToDo: can be accelerated drastically! (if necessary)
void	
CInstruction_LookupSwitchJ::Emit(CMethod& i_Method) const
{
	unsigned target;
	BYTE Padding[5] = {JVMI_lookupswitch, 0, 0, 0, 0};
	CMethodBody* pBody = i_Method.GetBody();
	CLabels* plabels = i_Method.GetLabels();
	IP_t StartIP = pBody->GetIP();
	unsigned PaddingSize = 1 + CalcPad(StartIP);
	pBody->Inject(Padding, PaddingSize);
	target = plabels->GetLabelBlockTarget(m_BranchTable[0]);
	unsigned dflt =  target - StartIP;
	dflt = LE_DWORD(dflt);
	pBody->Inject((BYTE*)&dflt, sizeof(dflt));
	unsigned pairs = LE_DWORD(m_Pairs);
	pBody->Inject((BYTE*)&pairs, sizeof(pairs));
	for(int ind = 1; ind <= m_Pairs; ind++)
	{
		unsigned value = LE_DWORD(m_LookupTable[ind]);
		target = plabels->GetLabelBlockTarget(m_BranchTable[ind]);
		target = LE_DWORD(target - StartIP);
	
		pBody->Inject((BYTE*)&value, sizeof(value));
		pBody->Inject((BYTE*)&target, sizeof(target));
	}
}

//------------------------------------------------------------------------------
void	
CInstruction_LookupSwitchJ::Dump(ostream& i_os, CMethod& i_Method) const
{
	CInstruction_Switch::Dump(i_os, i_Method);
}

//------------------------------------------------------------------------------
unsigned	
CInstruction_LookupSwitchJ::GetSize(IP_t i_IP) const
{
	if(i_IP == -1)
	{
		//ToDo: throw an exception
	}
	int allign = CalcPad(i_IP);
	return  1 + allign + 8 + m_Pairs * 8;
}

//==============================================================================
// CInsDescr_BranchJ implementation
//------------------------------------------------------------------------------
CInsDescr_BranchJ::CInsDescr_BranchJ(unsigned i_OpCode, CSTR i_szMnem, 
									 unsigned i_Size, int i_Stack, SemTag_t i_SemTag)
:CInsDescr_Branch(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
{
	;
}

//------------------------------------------------------------------------------
CInstruction* 
CInsDescr_BranchJ::CreateInstruction(const CMethod &meth) const
{
	CMethodBody* pmb = meth.GetBody();
	const BYTE *pCode = pmb->GetCode();
	unsigned IP = pmb->GetIP();
	short offset;
        MEMCPY2(&offset, (pCode + IP + 1));
	offset = LE_WORD(offset);
	BrTarget_t target = (BrTarget_t)(IP + offset);

	CInstruction_Branch* pins = new CInstruction_BranchJ(GetMnem(), GetSemTag(),
		                            pCode + IP, 
		                            GetSize(*pmb), GetStack(*pmb), target);
	return pins;
}

//==============================================================================
// CInsDescr_Branch_wJ implementation
//------------------------------------------------------------------------------
CInsDescr_Branch_wJ::CInsDescr_Branch_wJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
										 int i_Stack, SemTag_t i_SemTag)
:CInsDescr_Branch(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
{
	;
}

//------------------------------------------------------------------------------
CInstruction* 
CInsDescr_Branch_wJ::CreateInstruction(const CMethod &meth) const
{
	CMethodBody* pmb = meth.GetBody();
	const BYTE *pCode = pmb->GetCode();
	unsigned IP = pmb->GetIP();
	unsigned offset;
	MEMCPY4(&offset, pCode + IP + 1);
	offset = LE_DWORD(offset);
	BrTarget_t target = (BrTarget_t)(IP + offset);

	CInstruction_Branch* pins = new CInstruction_Branch_wJ(GetMnem(), GetSemTag(),
                                    pCode + IP, 
		                            GetSize(*pmb), GetStack(*pmb), target);
	return pins;
}


//==============================================================================
// CInsDescr_InvokeJ implementation
//------------------------------------------------------------------------------
// Constructor
CInsDescr_InvokeJ::CInsDescr_InvokeJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
:CInsDescr(i_OpCode, i_szMnem, i_Size, 0, i_SemTag)
{
	;
}

//------------------------------------------------------------------------------
CInstruction*	
CInsDescr_InvokeJ::CreateInstruction(const CMethod &i_meth) const
{
	CMethodBody* pmb = i_meth.GetBody();
	const BYTE *pCode = pmb->GetCode();
	unsigned IP = pmb->GetIP();
	return new CInstruction_InvokeJ(GetMnem(), pCode + IP, GetSize(*pmb), 0);
}

//==============================================================================
// CInsDescr_SwitchJ implementation
//------------------------------------------------------------------------------
// Constructor
CInsDescr_SwitchJ::CInsDescr_SwitchJ(unsigned i_OpCode, CSTR i_szMnem, 
									 unsigned i_Size, int i_Stack, SemTag_t i_SemTag)
:CInsDescr_Switch(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
{
}

//------------------------------------------------------------------------------
unsigned	    
CInsDescr_SwitchJ::GetSize(const CMethodBody &meth) const
{
	unsigned ip = meth.GetIP();
	unsigned dflt = 0;
	unsigned low = 0;
	unsigned high = 0;
	unsigned npairs = 0;

	ip += 1 + CalcPad(ip);						// Padding
	MEMCPY4(&dflt, meth.GetCode() + ip);	// Save default
	dflt = LE_DWORD(dflt);
	ip += 4;						// Skip default
	switch(*(meth.GetCode() + meth.GetIP())) // no need of memcpy as this is a BYTE
	{
		case JVMI_tableswitch:
		        MEMCPY4(&low, meth.GetCode() + ip);
			low = LE_DWORD(low);
			ip += 4;
			MEMCPY4(&high, meth.GetCode() + ip);
			high = LE_DWORD(high);
			ip += 4;
			ip += 4 * (high - low + 1);
			break;
		case JVMI_lookupswitch:
		        MEMCPY4(&npairs, meth.GetCode() + ip);
			npairs = LE_DWORD(npairs);
			ip += 4;
			ip += 8 * npairs;
			break;
		default:
			throw CModuleException(CModuleException::X_REASON_PARSE_ERROR, "Bad Switch");
	}
	return ip - meth.GetIP();
}

//------------------------------------------------------------------------------
// Create Instruction
// Input:
// Output:
// Returns:
//
CInstruction*   
CInsDescr_SwitchJ::CreateInstruction(const CMethod &meth) const
{
	CMethodBody* pmb = meth.GetBody();
	CInstruction_Switch* pinsSwitch;
	const BYTE* pCode = pmb->GetCode() + pmb->GetIP();

	switch(*pCode)
	{
		case JVMI_tableswitch:
			pinsSwitch = new CInstruction_TableSwitchJ(*pmb);
			break;
		case JVMI_lookupswitch:
			pinsSwitch = new CInstruction_LookupSwitchJ(*pmb);
			break;
		default:
			throw CModuleException(CModuleException::X_REASON_PARSE_ERROR, "Bad Switch");
	}
	return pinsSwitch;
}


//==============================================================================
CInstruction*	
CInsDescr_MultiANewArrayJ::CreateInstruction(const CMethod &i_meth) const
{
	CMethodBody* pmb = i_meth.GetBody();
	CInstruction* pins;
	const BYTE* pCode = pmb->GetCode() + pmb->GetIP();
	int nStack = 1 - pCode[3];
	pins = new CInstruction(m_szMnem, m_SemTag, pCode, m_Size, nStack);
	return pins;
}

//==============================================================================
CInstruction*	
CInsDescr_GetPutJ::CreateInstruction(const CMethod &i_meth) const
{
	CInstruction* pins;
	CMethodBody* pmb = i_meth.GetBody();
	const Code_t* pCode = pmb->GetCode() + pmb->GetIP();
	pins = new CInstruction_GetPutJ(m_szMnem, pCode, m_Size, 0);
	return pins;
}

//==============================================================================
// CInsDescr_Wide implementation
//------------------------------------------------------------------------------
// constructor
//
// The "Wide" instruction is sometimes a SEM_GEN instruction
// and sometimes a SEM_RETSR instruction, depending on the opcode
// it modifies. So you need to set the SemTag properly during construction.
//
CInsDescr_WideJ::CInsDescr_WideJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
								 int i_Stack, SemTag_t i_SemTag)
:CInsDescr(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
{
}

CInsDescr*
CInsDescr_WideJ::InsDescrFactory(const CMethodBody& meth) const
{
	unsigned ip = meth.GetIP();
	const Code_t* pCode = meth.GetCode();
	BYTE opcode = *(pCode + ip + 1);
	if (opcode == JVMI_iinc) {
		return &wide_iinc_descr;
	}
	else if (opcode == JVMI_ret) {
		return &wide_ret_descr;
	}
	else {
		return &wide_load_store_descr;
	}
}

CInsDescr*
CInsDescr_WideJ::InsDescrFactory(const BYTE* bytes) const
{
	BYTE opcode = *(bytes + 1);
	if (opcode == JVMI_iinc) {
		return &wide_iinc_descr;
	}
	else if (opcode == JVMI_ret) {
		return &wide_ret_descr;
	}
	else {
		return &wide_load_store_descr;
	}
}

CInstruction*
CInsDescr_WideLoadStoreJ::CreateInstruction(const CMethod& meth) const
{
	CMethodBody* pmb = meth.GetBody();
	CInstruction* pins = this->CInsDescr::CreateInstruction(meth);
	BYTE opcode = *(pmb->GetCode() + pmb->GetIP() + 1);

	for (int i = 0; ; i++) {
		if (wide_load_store_mnemonic[i].opcode == opcode) {
			pins->SetMnem(wide_load_store_mnemonic[i].mnemonic);
			break;
		}
	}
	return pins;
}

//= End of JVMInsSet.cpp =======================================================
