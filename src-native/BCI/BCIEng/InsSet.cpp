/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: InsSet.cpp,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// InsSet.cpp
// Started 8/2/99
//------------------------------------------------------------------------------
// The instrumentable module implementation
//
//==============================================================================
#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#define BCI_MODULE_DLL
#include "BCIEng.h"
#include "InsSet.h"

#ifdef HPUX
#include <iostream.h>
#else
#include <iostream>
#endif

USE_NAMESPACE(std);

//==============================================================================
// class CInsDescr
//
//------------------------------------------------------------------------------
CInstruction* 
CInsDescr::CreateInstruction(const CMethod& i_meth) const
{
	CMethodBody* pmb = i_meth.GetBody();
	const BYTE* pCode = pmb->GetCode() + pmb->GetIP();
	CSTR szMnem = GetMnem();
	int Stack = GetStack(*pmb);
	unsigned Size = GetSize(*pmb);
	CInstruction* pins = new CInstruction(szMnem, GetSemTag(), pCode, Size, Stack);
	return pins;
};

//------------------------------------------------------------------------------
CInsDescr*  
CInsDescr::InsDescrFactory(const CMethodBody &meth) const
{ 
	throw CModuleException(CModuleException::X_REASON_UNKNOWN, "Ins Descriptor Factory"); 
	return NULL;	// satisfy HPUX compiler, doesn't see throw as unconditional exit
};

//==============================================================================
// class CInstruction
//
//------------------------------------------------------------------------------
CInstruction::CInstruction(CSTR i_Mnem, SemTag_t i_SemTag, const BYTE* i_pCode, 
						   int i_Size, int i_Stack)
:m_szMnem(i_Mnem)
,m_SemTag(i_SemTag)
,m_Size(i_Size)
,m_Stack(i_Stack)
{
	if (i_Size > 0)
	{
		m_pCode = new BYTE[i_Size];
		memcpy(m_pCode, i_pCode, i_Size);
		m_OpCode = *m_pCode;
	}
	else 
	{	// Incomplete data: most likely came here from default constructor
		// of an inherited instruction. This should be initialized later.
		m_pCode = NULL;
		m_OpCode = (unsigned)-1; // Undefined 
	}
	m_IP = (IP_t) -1; //Undefined
}

//------------------------------------------------------------------------------
CInstruction::~CInstruction()
{
	if(m_pCode != NULL)
		delete[] m_pCode;
}

//------------------------------------------------------------------------------
void	
CInstruction::Emit(CMethod& i_Method) const
{
	CMethodBody* pBody = i_Method.GetBody();
	pBody->Inject(m_pCode, m_Size);
}

static void
hex_into_buffer(char *dest, int val)
{
	int c = val >> 4;
	if (c > 0x9) dest[0] = (c - 0xa) + 'A';
	else dest[0] = c + '0';

	c = val & 0xf;
	if (c > 0x9) dest[1] = (c - 0xa) + 'A';
	else dest[1] = c + '0';
}

//------------------------------------------------------------------------------
void	
CInstruction::Dump(ostream& i_os, CMethod& /* i_Method */) const
{
	//
	// Dump up to the first five bytes of the instruction, then the
	// opcode. Line up the columns.
	//

	char buf[4];	// tab, two digits, null

	buf[0] = '\t';
	buf[3] = '\0';
	hex_into_buffer(&buf[1], GetOpCode());
	int char_count = 2;

	i_os << (unsigned char*)buf;

	buf[0] = ' ';
	for (int i = 1; i < 5 && i < m_Size; i++) {
		hex_into_buffer(&buf[1], m_pCode[i]);
		i_os << buf;
		char_count += 3;
	}

	for ( ; char_count < 16; char_count++) {
		i_os << ' ';
	}

	i_os << GetMnem() << endl;
}

//==============================================================================
// class CInstruction_Branch
//
//------------------------------------------------------------------------------
CInstruction_Branch::CInstruction_Branch(CSTR i_Mnem, SemTag_t i_SemTag,
										 const BYTE* i_pCode, 
										 unsigned i_Size, int i_Stack,
										 unsigned i_BranchTarget)
:CInstruction(i_Mnem, i_SemTag, i_pCode, i_Size, i_Stack)
,m_BranchTarget(i_BranchTarget)
{
}

//------------------------------------------------------------------------------
void
CInstruction_Branch::Emit(CMethod& i_Method) const
{
	// This should not be called. Emitting a branch instruction is always
	// language-specific.
	throw CBCIEngException(CBCIEngException::REASON_Unknown);
}

//------------------------------------------------------------------------------
void
CInstruction_Branch::Dump(ostream& i_os, CMethod& i_Method) const
{
	// Look up the branch target in the label table and dump that,
	// not the original IP of the branch target.
	IP_t orig_target = GetBranchTarget();
	IP_t new_target = i_Method.GetLabels()->GetLabelBlockTarget(orig_target);

	i_os << "\t" << GetOpCode() << "\t\t    " << GetMnem() 
		 << "\t" << new_target << endl;
}

//==============================================================================
// class CInstruction_Switch
//
//------------------------------------------------------------------------------
CInstruction_Switch::CInstruction_Switch(CSTR i_Mnem)
:CInstruction(i_Mnem, SEM_SWITCH, NULL, 0, 0)
{
}

//------------------------------------------------------------------------------
void	
CInstruction_Switch::Emit(CMethod& i_Method) const
{
	// This should not be called. Would be pure virtual except
	// for the possibility of a language with no switch instruction.
	throw CBCIEngException(CBCIEngException::REASON_Unknown);
}

//------------------------------------------------------------------------------
void	
CInstruction_Switch::Dump(ostream& i_os, CMethod& i_Method) const
{
	CLabels *labels = i_Method.GetLabels();

	i_os << "\t" << GetOpCode() << "\t\t     " << GetMnem();
	iterator iter;
	for(iter = begin(); iter < end(); iter++)
	{
		i_os << " " << labels->GetLabelBlockTarget(*iter);
	}
	i_os << endl;
}

//= End of InsSet.cpp ==========================================================
