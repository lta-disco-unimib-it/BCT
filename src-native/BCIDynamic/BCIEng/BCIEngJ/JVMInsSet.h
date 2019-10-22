/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JVMInsSet.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// JVMInsSet.h
// 
//------------------------------------------------------------------------------
// Description 
// JVM Instruction Set.
// This instruction set is used for parsing Java instruction stream into 
// the list of insertion units (normally basic blocks) for subsequent 
// instrumentation. 
// 
//==============================================================================

#ifndef _JVMINSSET_H
#define _JVMINSSET_H

#include "Module.h"
#include "InsSet.h"

//= Dfinitions ==================================================================
// Here we define how to interpret the instruction definition from
// JVMIns.def
// In this case it maps directly into the CInsDescr constructor
//
#define DEF_JVMI(op, mnem, size, arg, stack, semt) \
		CInsDescr(op, #mnem, size, stack, semt),

typedef enum tagJVMI
{
	JVMI_nop, JVMI_aconst_null,	JVMI_iconst_m1,	JVMI_iconst_0, JVMI_iconst_1, 
	JVMI_iconst_2, JVMI_iconst_3, JVMI_iconst_4, JVMI_iconst_5,	JVMI_lconst_0,			
	JVMI_lconst_1, JVMI_fconst_0, JVMI_fconst_1, JVMI_fconst_2,	JVMI_dconst_0,			
	JVMI_dconst_1, JVMI_bipush, JVMI_sipush, JVMI_ldc, JVMI_ldc_w, JVMI_ldc2_w,			
	JVMI_iload,	JVMI_lload,	JVMI_fload,	JVMI_dload,	JVMI_aload,	JVMI_iload_0,			
	JVMI_iload_1, JVMI_iload_2, JVMI_iload_3, JVMI_lload_0, JVMI_lload_1, 
	JVMI_lload_2, JVMI_lload_3, JVMI_fload_0, JVMI_fload_1, JVMI_fload_2, 
	JVMI_fload_3, JVMI_dload_0, JVMI_dload_1, JVMI_dload_2, JVMI_dload_3, 
	JVMI_aload_0, JVMI_aload_1, JVMI_aload_2, JVMI_aload_3, JVMI_iaload,			
	JVMI_laload, JVMI_faload, JVMI_daload, JVMI_aaload, JVMI_baload, JVMI_caload,			
	JVMI_saload, JVMI_istore, JVMI_lstore, JVMI_fstore, JVMI_dstore, JVMI_astore,			
	JVMI_istore_0, JVMI_istore_1, JVMI_istore_2, JVMI_istore_3, JVMI_lstore_0,			
	JVMI_lstore_1, JVMI_lstore_2, JVMI_lstore_3, JVMI_fstore_0, JVMI_fstore_1,			
	JVMI_fstore_2, JVMI_fstore_3, JVMI_dstore_0, JVMI_dstore_1, JVMI_dstore_2,			
	JVMI_dstore_3, JVMI_astore_0, JVMI_astore_1, JVMI_astore_2, JVMI_astore_3,			
	JVMI_iastore, JVMI_lastore, JVMI_fastore, JVMI_dastore, JVMI_aastore, 
	JVMI_bastore, JVMI_castore, JVMI_sastore, JVMI_pop,	JVMI_pop2, JVMI_dup,
	JVMI_dup_x1, JVMI_dup_x2, JVMI_dup2, JVMI_dup2_x1, JVMI_dup2_x2, JVMI_swap,				
	JVMI_iadd, JVMI_ladd, JVMI_fadd, JVMI_dadd, JVMI_isub, JVMI_lsub, JVMI_fsub,				
	JVMI_dsub, JVMI_imul, JVMI_lmul, JVMI_fmul, JVMI_dmul, JVMI_idiv, JVMI_ldiv,				
	JVMI_fdiv, JVMI_ddiv, JVMI_irem, JVMI_lrem, JVMI_frem, JVMI_drem, JVMI_ineg,				
	JVMI_lneg, JVMI_fneg, JVMI_dneg, JVMI_ishl, JVMI_lshl, JVMI_ishr, JVMI_lshr,				
	JVMI_iushr, JVMI_lushr, JVMI_iand, JVMI_land, JVMI_ior, JVMI_lor, JVMI_ixor,				
	JVMI_lxor, JVMI_iinc, JVMI_i2l, JVMI_i2f, JVMI_i2d, JVMI_l2i, JVMI_l2f, 
	JVMI_l2d, JVMI_f2i, JVMI_f2l, JVMI_f2d, JVMI_d2i, JVMI_d2l, JVMI_d2f, 
	JVMI_i2b, JVMI_i2c, JVMI_i2s, JVMI_lcmp, JVMI_fcmpl, JVMI_fcmpg, JVMI_dcmpl,			
	JVMI_dcmpg, JVMI_ifeq, JVMI_ifne, JVMI_iflt, JVMI_ifge, JVMI_ifgt, JVMI_ifle,				
	JVMI_if_icmpeq, JVMI_if_icmpne, JVMI_if_icmplt, JVMI_if_icmpge, JVMI_if_icmpgt,		
	JVMI_if_icmple, JVMI_if_acmpeq, JVMI_if_acmpne, JVMI_goto, JVMI_jsr,				
	JVMI_ret, JVMI_tableswitch, JVMI_lookupswitch, JVMI_ireturn, JVMI_lreturn,			
	JVMI_freturn, JVMI_dreturn,	JVMI_areturn, JVMI_return, JVMI_getstatic, 
	JVMI_putstatic,	JVMI_getfield, JVMI_putfield, JVMI_invokevirtual,
	JVMI_invokespecial,	JVMI_invokestatic, JVMI_invokeinterface, 
	JVMI_xxxunusedxxx, JVMI_new, JVMI_newarray, JVMI_anewarray, JVMI_arraylength,
	JVMI_athrow, JVMI_checkcast, JVMI_instanceof, JVMI_monitorenter, 
	JVMI_monitorexit, JVMI_wide, JVMI_multianewarray, JVMI_ifnull, JVMI_ifnonnull,
	JVMI_goto_w, JVMI_jsr_w
} JVMI_t;

//==============================================================================
// CInsSetJ
// Java instruction set
//
class CInsSetJ : public CInsSet
{
public:
	CInsSetJ();

	static CInstruction* Create_simple(JVMI_t i_JVMI);

	static CInstruction* Create_getstatic(unsigned short i_Ref);
	static CInstruction* Create_ldc(unsigned i_CpInd);
	static CInstruction* Create_newarray(BYTE i_Type);
	static CInstruction* Create_push(char  i_Byte);
	static CInstruction* Create_push(short i_Short);
	static CInstruction* Create_push_constant(int i_Const);
	static CInstruction* Create_putstatic(unsigned short i_Ref);
	static CInstruction* Create_astore(int i_slotNum);
	static CInstruction* Create_istore(int i_slotNum);
	static CInstruction* Create_lstore(int i_slotNum);
	static CInstruction* Create_aload(int i_slotNum);
	static CInstruction* Create_iload(int i_slotNum);
	static CInstruction* Create_fload(int i_slotNum);
	static CInstruction* Create_dload(int i_slotNum);
	static CInstruction* Create_lload(int i_slotNum);
	static CInstruction* Create_iinc(int i_slotNum, int amount);
	static CInstruction* Create_anewarray(unsigned i_classRef);
	static CInstruction* Create_new(unsigned i_CpInd);
	static CInstruction* Create_checkcast(unsigned i_CpInd);
	static CInstruction* Create_invokespecial(unsigned i_CpInd);
	static CInstruction* Create_jsr(unsigned short i_target);
	static CInstruction* Create_ret(unsigned short i_slotNum);
	static CInstruction* Create_invokevirtual(unsigned i_CpInd);
	static CInstruction* Create_invokestatic(unsigned i_CpInd);
	static CInstruction* Create_goto(unsigned short i_target);
	static CInstruction* Create_placeholder(IP_t i_OrigIP);
    static CInstruction* Create_tableswitch(unsigned i_Low, unsigned i_High, 
                                            const vector<BrTarget_t> &i_BranchTable);
    static CInstruction* Create_lookupswitch(const vector<BrTarget_t> &i_BranchTable,
                                             const vector<unsigned> &i_LookupTable);
    static CInstruction* Create_multianewarray(unsigned i_CpInd, BYTE i_Dimensions);
    static CInstruction* Create_invokeinterface(unsigned i_CpInd, BYTE i_Count);
	static CInstruction* Create_if(JVMI_t opcode, unsigned short i_target);
};

//==============================================================================
// CInstruction_InvokeJ
//
// Differs from standard CInstruction by offering you the chance
// to read the constant pool index, to find out what method is being invoked.
//
// Four instructions belong to this subclass. All are SEM_CALL instructions:
//		invokeinterface
//		invokespecial
//		invokestatic
//		invokevirtual
//

class CInstruction_InvokeJ : public CInstruction
{
public:
	CInstruction_InvokeJ(CSTR i_Mnem, const BYTE* i_pCode, unsigned i_Size, int i_Stack);
	virtual int	GetStack(const CMethod& i_meth);
	virtual unsigned short GetCpIndex() const;
};

//==============================================================================
// CInstruction_GetPutJ
//
// Enables to get the CP index and implements stack depth calculation
// based on the referenced type
//
// Four instructions belong to this subclass. All are SEM_GEN instructions:
//		getstatic
//		putstatic
//		getfield
//		putfield
//
class CInstruction_GetPutJ : public CInstruction
{
public:
	CInstruction_GetPutJ(CSTR i_Mnem, const BYTE* i_pCode, unsigned i_Size, int i_Stack);
	virtual int	GetStack(const CMethod& i_meth);
	virtual unsigned short GetCpIndex() const;
};

//==============================================================================
// CInstruction_BranchJ
// 
// This class implements both branch and conditional branch group of 
// instructions. So far the only difference is the semantic tag (SEM_BR or SEM_BRC)
// If we find more semantic diferences than that we probably have to create
// the whole new class of the conditional branch instructions.
//
class CInstruction_BranchJ : public CInstruction_Branch
{
public:
	CInstruction_BranchJ(CSTR i_Mnem, SemTag_t i_SemTag, const BYTE* i_pCode,
						unsigned i_Size, int i_Stack, unsigned i_BranchTarget);

	void Emit(CMethod& i_Method) const;

protected:

private:

};

//==============================================================================
// CInstruction_Branch_wJ
// Wide branching (more than 128 bytes in either direction)
//
class CInstruction_Branch_wJ : public CInstruction_Branch
{
public:
	CInstruction_Branch_wJ(CSTR i_Mnem, SemTag_t i_SemTag, const BYTE* i_pCode,
						unsigned i_Size, int i_Stack, unsigned i_BranchTarget);

	void Emit(CMethod& i_Method) const;
};

//==============================================================================
class CInstruction_TableSwitchJ : public CInstruction_Switch
{
public:
	typedef vector<BrTarget_t> BranchTable_t;

public:
    CInstruction_TableSwitchJ();
	CInstruction_TableSwitchJ(const CMethodBody& i_meth);

	virtual void	Emit(CMethod& i_Method) const;
	virtual void	Dump(ostream& i_os, CMethod& i_Method) const;
	virtual unsigned GetSize(IP_t i_IP = -1) const;
	virtual CInstruction_Switch::iterator begin() const
	{
		return m_BranchTable.begin();
	}
	virtual CInstruction_Switch::iterator end() const
	{
		return m_BranchTable.end();
	}
    virtual unsigned GetLow() const 
    {
        return m_Low;
    }
    virtual void SetLow(unsigned i_Low) { m_Low = i_Low; }
    virtual unsigned GetHigh() const 
    {
        return m_High;
    }
    virtual void SetHigh(unsigned i_High) { m_High = i_High; }
    virtual BranchTable_t& GetBranchTable() { return m_BranchTable; }

    // returns the number of targets in the table, including the default
    // (stored as the first entry)
    virtual unsigned int GetTargetsCount() const
    {
        return m_BranchTable.size();
    }

private:
	// TargetIP is saved during first pass, and used to calculate the
	// number of pad bytes needed, and therefore to compute GetSize.
	unsigned		m_TargetIP;
	unsigned		m_Low;
	unsigned		m_High;
	BranchTable_t	m_BranchTable;
};

//==============================================================================
class CInstruction_LookupSwitchJ : public CInstruction_Switch
{
public:
	typedef vector<unsigned> LookupTable_t;
	typedef vector<BrTarget_t> BranchTable_t;

public:
	CInstruction_LookupSwitchJ();
	CInstruction_LookupSwitchJ(const CMethodBody& i_meth);

	virtual void	Emit(CMethod& i_Method) const;
	virtual void	Dump(ostream& i_os, CMethod& i_Method) const;
	virtual unsigned GetSize(IP_t i_IP = -1) const;
	virtual CInstruction_Switch::iterator begin() const
	{
		return m_BranchTable.begin();
	}
	virtual CInstruction_Switch::iterator end() const
	{
		return m_BranchTable.end();
	}
    virtual LookupTable_t& GetLookupTable()
    {
        return m_LookupTable;
    }
    virtual BranchTable_t& GetBranchTable() { return m_BranchTable; }
    virtual void SetPairs(unsigned i_Pairs) { m_Pairs = i_Pairs; }

    // returns the number of targets in the table, including the default
    // (stored as the first entry)
    virtual unsigned int GetTargetsCount() const
    {
        return m_BranchTable.size();
    }

private:
	// TargetIP is saved during first pass, and used to calculate the
	// number of pad bytes needed, and therefore to compute GetSize.
	unsigned		m_TargetIP;		// Target IP
	unsigned		m_Pairs;		// Number of pairs

	// The LookupTable and BranchTable are parallel arrays:
	// for a given index, the {LookupTable[i],BranchTable[i]} pair is what
	// you got from the original instruction.

	LookupTable_t	m_LookupTable;
	BranchTable_t	m_BranchTable;
};


//==============================================================================
// Java non-trivial instuction descriptors go here ...

//==============================================================================
// CInsDescr_BranchJ
// Java branch
//
class CInsDescr_BranchJ : public CInsDescr_Branch
{
public:
	CInsDescr_BranchJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		              int i_Stack, SemTag_t i_SemTag);

	virtual CInstruction* CreateInstruction(const CMethod &meth) const;
};

//==============================================================================
// CInsDescr_Branch_wJ
// Java wide branch
//
class CInsDescr_Branch_wJ : public CInsDescr_Branch
{
public:
	CInsDescr_Branch_wJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		                int i_Stack, SemTag_t i_SemTag);

	virtual CInstruction* CreateInstruction(const CMethod &meth) const;
};

//==============================================================================
// CInsDescrSwitchJ
// Java switch
//
class CInsDescr_SwitchJ : public CInsDescr_Switch
{
public:
	CInsDescr_SwitchJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		               int i_Stack, SemTag_t i_SemTag);

	virtual CInstruction*   CreateInstruction(const CMethod &meth) const;
	virtual unsigned	    GetSize(const CMethodBody &meth) const;
};

//==============================================================================
class CInsDescr_GetPutJ : public CInsDescr
{
public:
	CInsDescr_GetPutJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
		: CInsDescr(i_OpCode, i_szMnem, i_Size, 0, i_SemTag) { };
	virtual CInstruction*  CreateInstruction(const CMethod &i_meth) const;
};

//==============================================================================
// CInsDescr_MultiANewArrayJ
// The ovverriden CreateInstruction calculates stack from the given code
//
class CInsDescr_MultiANewArrayJ : public CInsDescr
{
public:
	CInsDescr_MultiANewArrayJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
		: CInsDescr(i_OpCode, i_szMnem, i_Size, 0, i_SemTag) { };

	virtual CInstruction*  CreateInstruction(const CMethod &i_meth) const;
};

//==============================================================================
// CInsDescr_Wide
// This is a meta-instruction: when you create one from the method body,
// you get back a CInstruction_xloadW or _xstoreW (where 'x' is the load type)
// or _retW or iincW.
//
// They don't even have the same semantic type: "wide ret" is SEM_RETSR.
//
class CInsDescr_WideJ : public CInsDescr
{
public:
	CInsDescr_WideJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		            int i_Stack, SemTag_t i_SemTag);
	virtual unsigned GetSize(const CMethodBody& mb) const { 
		return InsDescrFactory(mb)->GetSize(mb); 
	};
	virtual CInstruction* CreateInstruction(const CMethod& meth) const {
		CMethodBody* pmb = meth.GetBody();
		return InsDescrFactory(*pmb)->CreateInstruction(meth);
	};
	virtual bool IsSpecial() const { 
		return true; 
	};

	// The meat is here: the descr factory decides which wide instruction
	// you're really looking at, and returns the proper ins descr.
	virtual CInsDescr*  InsDescrFactory(const CMethodBody &meth) const;
	virtual CInsDescr*	InsDescrFactory(const BYTE* bytes) const;
};

// wide load or store: instruction size is 4, sem_tag is SEM_GEN
// Also, the CInstruction operation fills in the proper mnemonic string for the
// instruction: wide iload, wide astore, whatever.
//
// TODO: Implement separate descriptors for load and store to get the right stack depth
// 

class CInsDescr_WideLoadStoreJ : public CInsDescr
{
public:
	CInsDescr_WideLoadStoreJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
		: CInsDescr(i_OpCode, i_szMnem, i_Size, 2, i_SemTag) { };
	virtual CInstruction* CreateInstruction(const CMethod& meth) const;
};

// wide ret: instruction size is 4, sem_tag is SEM_RETSR
class CInsDescr_WideRetJ : public CInsDescr
{
public:
	CInsDescr_WideRetJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
		: CInsDescr(i_OpCode, i_szMnem, i_Size, -2, i_SemTag) { };
};

// wide iinc: instruction size is 6, sem_tag is SEM_GEN
class CInsDescr_WideIIncJ : public CInsDescr
{
public:
	CInsDescr_WideIIncJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag)
		: CInsDescr(i_OpCode, i_szMnem, i_Size, 0, i_SemTag) { };
};

//==============================================================================
// CInsDescr_InvokeJ
//
// The overridden CreateInstruction in this class returns a CInstruction_InvokeJ
// object.
//
// CInstruction_InvokeJ is identical to generic CInstructions,
// except that it exports another method: GetCpIndex returns the
// constant pool index of the function being called.
//
class CInsDescr_InvokeJ : public CInsDescr
{
public:
	CInsDescr_InvokeJ(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, SemTag_t i_SemTag);
	virtual CInstruction*	CreateInstruction(const CMethod &meth) const;
};


#endif // defined _JVMINSSET_H

//= End of JVMInsSet.h =========================================================
