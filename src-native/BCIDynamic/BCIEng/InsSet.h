/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InsSet.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// InsSet.h
// Started 7/28/99
//------------------------------------------------------------------------------
// Instruction Set
// 
//==============================================================================
#ifndef _INSSET_H
#define _INSSET_H

//------------------------------------------------------------------------------
// STL
#include <vector>
#ifdef WIN32
#pragma warning(disable:4786)
#endif

//------------------------------------------------------------------------------
// Project definitions
#include "CommonDef.h"
#include "Module.h"
USE_NAMESPACE(std);

//==============================================================================
//Declarations 
class CInstruction;
class CInstruction_Branch;
class CInsDescr;
class CInsDescr_Branch;
class CInsDescr_Switch;
class CInsSet;

//==============================================================================
//  Refs
//
class CModule;
class CMethod;
class CMethodBody;

typedef unsigned BrTarget_t;
typedef vector<BrTarget_t> CBranchTargets;

//==============================================================================
// Semantic tags for instructions.
// These semantic tags are used by the code parser to slice a method body
// to insertion blocks (see CMethod::Parse)
// Normaly all instructions having SEM_GEN tag will form a single insertion block
// Other tags can break an insertion block since they result in changing the
// control flow. SEM_RETSR is a special case. This instruction is a branch 
// without an explicit target. It's for the Java "ret" instruction. The target is 
// taken from a local. In java the JSR - RET couple is usually used for finally 
// block processing.
//
typedef enum tagSemTag
{
	SEM_GEN,			// Generic
	SEM_BR,				// Branch
	SEM_BRC,			// Conditional branch
	SEM_SWITCH,			// Switch
	SEM_CALL,			// Call
	SEM_JSR,			// Jump to local subroutine
	SEM_RET,			// Return
	SEM_RETSR,			// Return from local subroutine 
	SEM_THROW,			// Throw
	SEM_INVALID,		// Invalid byte code (causes an exception)
	SEM_PLACEHOLDER,	// Synthetic, placeholder instruction for the end of the function (zero size)
	SEM_LAST			// Sentinel
} SemTag_t;



//==============================================================================
// CInstruction
// Generic Instruction
//
// The basic functionality is to copy some number of bytes from
// the instruction stream pointer (i_pCode) and emit them back later.
//
// If you have a strange instruction (like .Net branches, which can be
// long or short), you can just override GetSize and Emit, and you're
// finished as far as this class is concerned: the fact that the
// constructor gets a nonsense value for i_Size is harmless. You do have
// to pass "1" for i_Size and a good i_pCode value, however, because the
// constructor will malloc the code and also save the first byte as
// m_OpCode. This is bad and TODO should be fixed.
//
// IMPORTANT: CInstruction::GetSize is used during the "emit" phase, so
// the size that's wanted is the size of the instruction you will emit,
// not the size of the instruction you originally parsed. For the
// original parse, CInsDescr::GetSize is used.
//
// OPTIMIZATION TODO: use a union of four bytes and a pointer as the
// memory of the original instruction. If i_Size is four or less,
// use the bytes instead of allocating a tiny little block to hold
// the bytes. This will be the high-runner case by far.
//

class CInstruction
{
public:
	CInstruction(CSTR i_Mnem, SemTag_t i_SemTag, const BYTE* i_pCode, 
		         int i_Size, int i_Stack);
	virtual ~CInstruction();
	
	SemTag_t	GetSemTag() const {return m_SemTag;}	// use to determine how to downcast

	CSTR		GetMnem() const {return m_szMnem;}		// use for dumping, debugging
	unsigned	GetOpCode() const {return m_OpCode;}	// use for dumping, debugging
	IP_t		GetIP() const {return m_IP;}			// get original IP
	void		SetIP(IP_t i_IP) {m_IP = i_IP;}
    BYTE*       GetCode() const { return m_pCode; }     // get raw code

	virtual unsigned	GetSize(unsigned i_IP = (unsigned)-1) const {return m_Size;}
	virtual int			GetStack(const CMethod& i_Method)  {return m_Stack;}

	// SetMnem is used rarely: only for Java wide load/store instructions
	virtual void	SetMnem(const char* mnem) { m_szMnem = mnem; }

	virtual void	Emit(CMethod& i_Method) const;
	virtual void	Dump(ostream& i_os, CMethod& i_Method) const;

protected:
	unsigned		m_OpCode;	// Operation code
	BYTE*			m_pCode;	// Instruction raw code (for generic instructions)
	int				m_Size;		// Code size
	int				m_Stack;	// Stack size
	IP_t			m_IP;		// Original IP (-1 - undefined)

private:
	CSTR			m_szMnem;	// Mnemonic (for debugging)
	const SemTag_t  m_SemTag;	// Semantic tag
};

//==============================================================================
// CInstruction_Branch
// Branch Instruction
//
// This actually has enough functionality for Java, but not .Net.
// .Net branch instruction sizes are not constant, so they'll override
// GetSize too.
//
class CInstruction_Branch : public CInstruction
{
public:
	CInstruction_Branch(CSTR i_Mnem, SemTag_t i_SemTag, const BYTE* i_pCode,
		                unsigned i_Size, int i_Stack, unsigned i_BranchTarget);
	BrTarget_t GetBranchTarget() const {return m_BranchTarget;}

	virtual void	Emit(CMethod& i_Method) const;
	virtual void	Dump(ostream& i_os, CMethod& i_Method) const;

protected:
	BrTarget_t		m_BranchTarget;
private:
};

//==============================================================================
// CInstruction_Switch
// Switch Instruction
//
// Note that the default Switch implementation is not really functional,
// since this instuction strongly depends on the underlying language
// semantics. This is almost a pure abstract class exposed to the instrumentation
// engine. The instrumentation engine relies on the branch target iterator
// returned by this instruction.
//
// Today, the iterator is a vector iterator, but a change to this typedef
// should let us use another data structure without changing the engine.
// (We'd still have to recompile, but we remain source compatible.) 
//
// GetSize() on this abstraction level throws an exception. Only concrete
// implementations of this class know how to calculate it's size.
// 
class CInstruction_Switch : public CInstruction
{
public:
	// iterator
	typedef vector<BrTarget_t>::const_iterator iterator;

public:
	virtual void		Emit(CMethod& i_Method) const;
	virtual void		Dump(ostream& i_os, CMethod& i_Method) const;
	// virtual unsigned	GetSize()const = 0;

	// Get the branch target iterator (STL way)
	virtual iterator	begin() const = 0;
	virtual iterator	end() const = 0;

protected:
	CInstruction_Switch(CSTR i_Mnem);	// Assure there is a derived class

private:

};


//==============================================================================
// Instruction descriptor.
class CInsDescr
{
public:
	//- Constructor
	CInsDescr(unsigned i_OpCode, CSTR i_szMnem, 
			  unsigned i_Size, int i_stack,
		      SemTag_t i_SemTag):
	m_OpCode(i_OpCode),
	m_szMnem(i_szMnem),
	m_Size(i_Size),
	m_Stack(i_stack),
	m_SemTag(i_SemTag)
	{;}

	//- Info methods
	unsigned	GetOpCode() const {return m_OpCode;}
	CSTR		GetMnem() const	{return m_szMnem;}
	SemTag_t	GetSemTag() const {return m_SemTag;}
	// This GetSize just returnes value from the instruction descr. table
	unsigned	GetSize() const {return m_Size;}
	int			GetStack() const {return m_Stack;}

	//- Virtual methods
	// Default implementations: not special, error to call factory, size is fixed.
	virtual bool		IsSpecial() const {return false;}
	virtual CInsDescr*  InsDescrFactory(const CMethodBody &meth) const;
	virtual	int			GetStack(const CMethodBody &meth) const { return m_Stack;}
	virtual unsigned	GetSize(const CMethodBody &meth) const { return m_Size;}

	virtual CInstruction* CreateInstruction(const CMethod& meth) const;

protected:
	const unsigned	m_OpCode;
	CSTR			m_szMnem;
	const unsigned	m_Size;
	const int		m_Stack;
	const SemTag_t	m_SemTag;
};

//==============================================================================
// Branch Instruction  
// Inherited from CInsDescr. Knows how to calculate the branch target
//
// All language implementors should subclass this to parse the bytes and
// create the switch instruction appropriately.
// Hence the protected constructor.
//
class CInsDescr_Branch : public CInsDescr
{
protected:
	CInsDescr_Branch(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		             int i_Stack, SemTag_t i_SemTag)
	:CInsDescr(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
	{;}
};

//==============================================================================
// Switch Instruction  
// Inherited from CInsDescr. Knows how to calculate the branch target list
//
// All language implementors should subclass this to parse the bytes and
// create the switch instruction appropriately.
// Hence the protected constructor.
//
class CInsDescr_Switch : public CInsDescr
{
protected:
	CInsDescr_Switch(unsigned i_OpCode, CSTR i_szMnem, unsigned i_Size, 
		             int i_Stack, SemTag_t i_SemTag)
	:CInsDescr(i_OpCode, i_szMnem, i_Size, i_Stack, i_SemTag)
	{;}
};

//==============================================================================
// Instruction set 
class CInsSet : public vector <CInsDescr *>
{
public:
	CInsSet(CInsDescr* i_InstructionList, unsigned i_uSize) 
	{
		resize(i_uSize);
		for(unsigned i = 0; i < i_uSize; i++)
		{
			(*this)[i] = &i_InstructionList[i];
		}
	}
	CInsDescr* GetDescr(unsigned i_OpCode) const {return (*this)[i_OpCode];}

private:

};

#endif
//= End Of InsSet.h ============================================================
