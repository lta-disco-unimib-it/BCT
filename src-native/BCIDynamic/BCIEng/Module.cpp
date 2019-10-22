 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: Module.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//*
//* Module.cpp
//*

//==============================================================================
// Module.cpp
// 8/2/99
//------------------------------------------------------------------------------
// The instrumentable module implementation
//
//==============================================================================
#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#define BCI_MODULE_DLL

#ifdef WIN32
#	pragma warning(disable:4786)
#endif

#ifdef HPUX
#	include <iostream.h>
#else
#	include <iostream>
#endif

#ifdef WIN32
#pragma warning(default:4786)
#endif

#include <stdlib.h>
#include "CommonDef.h"
#include "Module.h"
#include "InsSet.h"
#include "BCIEng.h"	// for CBCIEngException, DEBUG_PRINTF

USE_NAMESPACE(std);

#ifndef __max
#define __max(a,b) ((a) >= (b) ? (a) : (b))
#endif


//==============================================================================
// class CModule
//
//------------------------------------------------------------------------------
CModule::CModule(CInsSet* i_pInsSet)
{
	m_pMethods = new CMethods;
	m_pInsSet = i_pInsSet;
	m_pExtRefs = new CExtRefs;
}

CModule::~CModule()
{
	delete m_pMethods;

	// m_pExtRefs is a collection of pointers, and it owns the
	// memory that's pointed to. Must delete the pointed-to ExtRefs.

	for (CExtRefs::iterator iter = m_pExtRefs->begin();
		iter != m_pExtRefs->end();
		iter++) {
			delete *iter;
	}
	delete m_pExtRefs;
}

//
// Implementors of CModule are expected to have their override of
// AddExtRef call the inherited one, just to get the external reference
// onto the list.
//
// NOTE: The ExtRefs collection keeps a pointer to the objects that you
// add to it, and deletes them when it is destroyed. So don't pass in
// pointers to non-heap objects.
//

void
CModule::AddExtRef(CExtRef& i_ref)
{
	m_pExtRefs->push_back(&i_ref);
}

//------------------------------------------------------------------------------
//
// Emit: call Emit on every method.
//
// In the end, all the method bodies of the methods in this module
// will have their bodies replaced with the new, post-insertion content.
//
void
CModule::Emit()
{
	for(CMethods::iterator iter = m_pMethods->begin(); 
	    iter < m_pMethods->end(); iter++)
	{
			(*iter)->Emit();
	}
}

//------------------------------------------------------------------------------
// Dump
//
void
CModule::Dump(ostream &i_os) const
{
	i_os << "====== Module dump ==========" << "\n"
		 << "Name       : " << GetName() << "\n"
	     << "Language   : " << GetLanguage() << "\n"
		 << "Description: " << GetDescription() << endl;
	m_pMethods->Dump(i_os);
}

//==============================================================================
// class CMethod implementation
//
//------------------------------------------------------------------------------
// Parse
// Parses method into components
// (Insertion blocks, instructions etc.)
//
void
CMethod::Parse()
{
	typedef vector<CInstruction*> InstrStore_t;
	InstrStore_t Instructions;
	CInsBlock* pBlock = NULL;
	if(NULL == m_pBody)
		return;
	CInsDescr* pinsdsc = m_pBody->GetInsDescr();
	// 1-st pass: Collect branch targets and save them in the labels table
	while(NULL != pinsdsc)
	{
		CInstruction* pins = pinsdsc->CreateInstruction(*this);
		Instructions.push_back(pins);
		switch (pins->GetSemTag())
		{
			case SEM_INVALID:
			{
#ifdef _DEBUG
				cerr << "Error parsing " << m_pModule->GetName()
					 << "::" << GetName()
					 << "\nInvalid OpCode " << pins->GetOpCode()
					 << " at " << m_pBody->GetIP() << endl;
#endif
				throw CBCIEngException(CBCIEngException::REASON_InvalidOpcode);
				break; // not reached...
			}

			case SEM_BR:
			case SEM_BRC:
			case SEM_JSR:
			{
				// Add the branch target to the label list
				unsigned uBrTarget = ((CInstruction_Branch*)pins)->GetBranchTarget();
				m_Labels.AddLabel(uBrTarget);
				break;
			}

			case SEM_SWITCH:
			{
				// Add each target to the label map
				CInstruction_Switch* pinsSwitch = (CInstruction_Switch*)pins;
				for (CInstruction_Switch::iterator iter = pinsSwitch->begin(); 
					 iter != pinsSwitch->end(); 
					 iter++) {
						unsigned t = *iter;
						m_Labels.AddLabel(t);
				}
				break;
			}

			default:
			{
				// do nothing for legal instructions that don't affect the label list
				break;
			}
		}
		pinsdsc = m_pBody->Advance();
	}

	m_pBody->ResetIP(); // Not really necessary. We're finished with the MethodBody.

	// Second pass: now that the label list is populated, fill basic blocks
	IP_t ip = 0;
	int blockNum = 0;
	pBlock = new CInsBlock(m_pBody->GetIP(), blockNum);
	bool empty = true;

	for(InstrStore_t::iterator iterIns = Instructions.begin(); 
		iterIns < Instructions.end(); iterIns++)
	{
		(*iterIns)->SetIP(ip);

		// If this IP has a label on it, start a new basic
		// block. But not if the current basic block is empty:
		// that happens when we just finished one because of a
		// branch instruction, and it also happens the first
		// time through this loop.
		//
		// TODO: It's way inefficient to do the map lookup for
		// every IP. There ought to be a better way to say,
		// "Does the current instruction have a label on it?"
		// Sorting the label table would let us walk through it
		// linearly. Using a bit vector (one bit per byte of the
		// original function) might do it. Or something.

		if(m_Labels.IsLabel(ip) && !empty)
		{
			// Start basic block
			m_Blocks.push_back(pBlock);
			pBlock = new CInsBlock(ip, ++blockNum);
			empty = true;	// note: will immediately go false below
		}

		// Add this instruction to the current basic block.
		pBlock->AddInstruction(*iterIns);
		empty = false;

		ip += (*iterIns)->GetSize(ip);

		// Test the semantic tag to see if this is one of the
		// types of instructions that marks the end of an
		// insertion unit (aka Basic Block). Any instruction
		// which causes a change in control flow qualifies.
		//
		// You might think we don't need to test for
		// unconditional branch,  because there will of
		// necessity be a label on the next instruction,  and
		// *that* will be the start of the new BB. But if the
		// compiler emits branches followed by dead code, we
		// still want the branch to be the last instruction in
		// its basic block.
		//
		// All semantic tags *except* SEM_GEN qualify as the
		// ends of basic blocks, so that's what we test here.
		// (Note: we don't expect any SEM_INVALID or SEM_PLACEHOLDER)
		//
		// Final note: we don't have to wonder if the current bb
		// is empty, because we know it's not: we just added
		// this instruction to it!

		SemTag_t tag = (*iterIns)->GetSemTag();
		if(tag != SEM_GEN)
		{
			// End this insertion block, start new one
			m_Blocks.push_back(pBlock);
			pBlock = new CInsBlock(ip, ++blockNum);
			empty = true;
		}
	}

	// Usually the last instruction of a method is an unconditional
	// branch, which caused us to end the previous pBlock and start
	// a new one. That new one will be empty when we get here after
	// falling off the end of the method. We want to delete that block.
	//
	// However, some Java compilers are sloppy and generate
	// unreachable code that doesn't end with a branch at the end
	// of the function. We've seen it in j2db.jar from
	// WAS 5.1 for instance. So detect both cases and deal with them.
	// 
	// If we did dead code analysis we'd see that the code was dead,
	// but we don't. And we can't just assume it's unreachable and
	// delete it, because the cases we've seen are actually the target
	// of a branch that is in some OTHER unreachable code. Bizarre.
	//
	// The previous code that always threw an exception on methods
	// that ended without an unconditional branch was reported as
	// Eclipse Bugzilla bug 68435.

	if (pBlock->GetLength() != 0)
	{
		m_Blocks.push_back(pBlock);
	}
	else {
		delete pBlock;
	}

	// Set "nextUniqueIP" to an IP that isn't being used.
	// It's a little unsafe to set it to "ip" even though no insn
	// has that as its orig_ip ... that first unused IP actually can be
	// used in an exception table as the end of a protected (try) block.
	m_nextUniqueIP = ip + 1;
}

//------------------------------------------------------------------------------
// CreateUniqueBlockLabel
//
// Creates a label in the label table for an IP that isn't already used.
// Returns the IP so the caller can use it as the "old IP" for a block
// or instruction that will later need to be patched.
//
// You can only use this after Parse() and before Emit() ... After Parse()
// because that's when we know what IPs are already in use, and before Emit() 
// because all IPs are reassigned during Emit.
//

IP_t
CMethod::CreateUniqueLabel()
{
	m_Labels.AddLabel(m_nextUniqueIP);
	return m_nextUniqueIP++;
}

//------------------------------------------------------------------------------
void			
CMethod::AddException(CMethodException* i_pmtdex, CMtdExTable::Order_enu i_enuOrder)
{
	IP_t ipStart = i_pmtdex->GetStart();
	IP_t ipEnd   = i_pmtdex->GetEnd();
	CInsBlock* pblkHandler = i_pmtdex->GetHandler();
	IP_t ipHandler = pblkHandler->GetLabel();
	m_pMtdExTable->AddException(i_pmtdex, i_enuOrder);
	m_Labels.AddLabel(ipStart);
	m_Labels.AddLabel(ipEnd);
	m_Labels.AddLabel(ipHandler);
}

//------------------------------------------------------------------------------
// Emit
//
// This function changes a lot of state; only call it once.
// For example, it calculates the forwarding address of each label
// and updates the label table.
//
 
void
CMethod::Emit()
{
	CInsBlocks::iterator iter;
	IP_t IP = 0;
	if(NULL == m_pBody)
	{ // No code. Skip it
		return;
	}

	for(iter = m_Blocks.begin(); iter < m_Blocks.end(); iter++)
	{
		// First, set the target of the label for this BB to its
		// new starting IP. This line might be redundant: it's
		// a holdover from when labels only pointed to blocks,
		// never to individual instructions.
		m_Labels.SetLabelTargets((*iter)->GetLabel(), IP, IP);

		// Set this block's label to its starting IP, and
		// compute the ending IP for this block (hence the starting
		// IP for the next)
		IP = (*iter)->UpdateIP(IP);
	}

	// Update line number mapping info
	IP_t ipNew = 0;
	for(iter = m_Blocks.begin(); iter < m_Blocks.end(); iter++)
	{
		CInsBlock* pInsBlock = (*iter);
		IP_t ipNewForBlock = pInsBlock->GetLabel();
		CInstructions* pins = pInsBlock->GetInstructions();
		CInstructions::iterator iterIns;
		for(iterIns = pins->begin(); iterIns != pins->end(); iterIns++)
		{
			CInstruction* pi = *iterIns;
			IP_t ipOld = pi->GetIP();

			// Patch the label table entry for this instruction,
			// if any, so its target "instruction" IP is the 
			// new IP for this instruction, and its new "block" IP
			// is the new IP for the start of this block.
			// (The "block" IP is used when targeting branches;
			// the "instruction" IP is used when patching exception tables.)
			// SetLabelTargets is a no-op if there is no label for ipOld.
			m_Labels.SetLabelTargets(ipOld, ipNew, ipNewForBlock);

			// Patch the line number table entry for this instruction,
			// if any, to refer to the first
			// instruction of this block. That way all our insertion
			// on the first instruction of this line is logically
			// part of the same line.
			// SetNewTarget is a no-op if ipOld isn't a line number marker.
			m_LineNumbers.SetNewTarget(ipOld, ipNewForBlock);

			ipNew += pi->GetSize(ipNew);
		}
	}
	// At this point, the label table is finished: we know the forwarding address
	// of every label in the original function.


	// Replace method body
	// At this point, IP is also the size of the method in bytes.
	m_pBody->NewCode(NULL, IP);
	for(iter = m_Blocks.begin(); iter < m_Blocks.end(); iter++)
	{
		(*iter)->Emit(*this);
	}
}

//------------------------------------------------------------------------------
CInsBlock*		
CMethod::FindBlock(IP_t i_ip)
{
	CInsBlock* pblkRet = NULL;
	for(CInsBlocks::iterator itr = m_Blocks.begin(); itr != m_Blocks.end(); itr++)
	{
		if((*itr)->GetLabel() == i_ip)
		{
			pblkRet = *itr;
			break;
		}
	}
	return pblkRet;
}

//------------------------------------------------------------------------------
int 
CMethod::CalcStackDepth()
{
	int nMaxStack = 0;
	int nOut = 0;
	CInsBlocks::iterator itr;
	
	// Calculate stack depth starting from the first block
	nMaxStack = CalcStackDepth(*m_Blocks.begin(), 0);

	// Now calculate stack depth for all unvisited blocks
	// pointed by the label table.
	// This will take care of the exception handlers
	// As well as other potential corner cases where a
	// 'live' block was ignored by the recursive descent in the
	// previous call.
	//
	// We think these unvisited blocks are exception handlers.
	// That means their "in" stack depth is 1, for the exception object.
	// If there are other reasons for an unvisited block to exist,
	// we should figure out what it is and handle it explicitly.

	for(itr = m_Blocks.begin(); itr != m_Blocks.end(); itr++)
	{
		if(m_Labels.IsLabel((*itr)->GetOrigIP()))
		{
			if(!(*itr)->IsVisited())
			{
				int nStack;
				nStack = CalcStackDepth(*itr, 1);
				nMaxStack = __max(nStack, nMaxStack);
			}
		}
	}
	
	return nMaxStack;
}

//------------------------------------------------------------------------------
// Dump
// In:
//		i_os	-- Output stream
// 
void
CMethod::Dump(ostream& i_os)
{
	i_os << "Method " << GetName() << endl;
	m_Blocks.Dump(i_os, *this);
	m_Labels.Dump(i_os);
	if (m_pMtdExTable != NULL)
		m_pMtdExTable->Dump(i_os);
	m_LineNumbers.Dump(i_os);
}

//------------------------------------------------------------------------------
// Private methods

//------------------------------------------------------------------------------
// CalcStackDepth
// Calculates the maximum stack depth by recursively following all branch
// targets.
//
// Note: To simplify this code an assumption was made that JSR target code
// doesn't change the net stack depth (i.e. stack on entry and exit is 
// exacly the same). Although this was not clearly stated in the JVM 
// specification, it appears to be true. If we ever find Java byte code
// proving othervise the JSR branch following should be changed.
//
// Each block only needs to be visited once, because of the limits Java
// places on byte codes for verification.
// The initial stack depth, max depth, and final
// stack depth are all known after you've seen a block once, and they can't
// ever be different.
//
// In: 
//	i_pBlk - pointer to the first insertion block.
//  i_nIn  - Net stack depth on the entry.
// Returns:
//	int    - Max. stack depth calculated for the branch begining with i_pBlk
//

int 
CMethod::CalcStackDepth(CInsBlock* i_pblk, int i_nIn)
{
	int nNet = i_nIn;							// Net stack depth 
	int nRet = i_nIn;							// Total max depth (the returned value)
	int nHold;									// holder for recursive calc return values
	bool bFallThrough;							// True as long as we should fall through to the next block
	CInsBlock* pblkTarget = NULL;				// The target block for branches
	CInsBlock* pblkNext = i_pblk;
	CInstructions* pins;
	CInstructions::iterator itr;

	if(i_pblk->IsVisited())
	{
		// This block was already visited, so it and its successors have
		// already had their effects on the overall max calculation. Just return.
		return 0;
	}

	// Loop over blocks as long as we are falling through
	for (bFallThrough = true ; bFallThrough ; pblkNext = GetNextBlock(pblkNext))
	{
		if(NULL == pblkNext)
		{	// Being defensive, should never happen
			string  strLocation = m_pModule->GetName();
			strLocation += ".";
			strLocation += m_strName;
			throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR, strLocation.c_str());
		}
		pblkNext->SetVisited();
		pins = pblkNext->GetInstructions();
		for(itr = pins->begin(); itr != pins->end(); itr++)
		{
			CInstruction* pi = *itr;				// Current instruction pointer
			CInstruction_Branch* piBranch;			// Current branch
			int nStack = (*itr)->GetStack(*this);	// Net stack reported by the instruction
			nNet += nStack;							
			nRet = __max(nNet, nRet);
			switch(pi->GetSemTag())
			{
				case SEM_GEN:
				case SEM_CALL:
					// The trivial fall through case
					bFallThrough = true;
					break;
				case SEM_BR:
					// Recursively follow at the branch target, then don't fall through
					piBranch = (CInstruction_Branch*)pi;
					pblkTarget = GetTargetBlock(piBranch->GetBranchTarget());
					nHold = CalcStackDepth(pblkTarget, nNet);
					nRet = __max(nHold, nRet);
					bFallThrough = false;
					break;
				case SEM_JSR:
					// The JSR instruction has two successors, and the stack
					// is different for each. At its target, the stack is one element
					// deeper (for the return address). Then when we fall through,
					// the stack is the same as when we started - its net effect is zero.
					// So in the recursion we pass nNet+1 as the initial depth.
					piBranch = (CInstruction_Branch*)pi;
					pblkTarget = GetTargetBlock(piBranch->GetBranchTarget());
					nHold = CalcStackDepth(pblkTarget, nNet + 1);
					nRet = __max(nHold, nRet);
					bFallThrough = true;
					break;
				case SEM_BRC:
					// Recursively follow at the branch target, then fall through.
					piBranch = (CInstruction_Branch*)pi;
					pblkTarget = GetTargetBlock(piBranch->GetBranchTarget());
					nHold = CalcStackDepth(pblkTarget, nNet);
					nRet = __max(nHold, nRet);
					bFallThrough = true;
					break;
				case SEM_SWITCH:
				{	// The switch instruction has multiple branch targets
					CInstruction_Switch* piSwitch = (CInstruction_Switch*)pi;
					CInstruction_Switch::iterator itrSwitch;
					for(itrSwitch = piSwitch->begin(); itrSwitch != piSwitch->end(); itrSwitch++)
					{
						pblkTarget = GetTargetBlock(*itrSwitch);
						nHold = CalcStackDepth(pblkTarget, nNet);
						nRet = __max(nHold, nRet);
					}
					bFallThrough = false;
					break;
				}
				case SEM_RET:
				case SEM_RETSR:
				case SEM_THROW:
					// Dead end
					bFallThrough = false;
					break;
				case SEM_PLACEHOLDER:
					// This is a zero-length placeholder pseudo-instruction.
					// Don't change the value of bFallThrough
					break;
				default:
					// If we are here, something is wrong with the parsed instruction.
					// Retport an internal error.
					string  strLocation = m_pModule->GetName();
					strLocation += ".";
					strLocation += m_strName;
					throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR, strLocation.c_str());
			} // end switch
		} // end for
	} // end "for each block if we fall through"
	return nRet;
}


//------------------------------------------------------------------------------
// Get the block that follows this one.
// TODO: optimize or something. Chasing through the block list seems like a waste.
CInsBlock*		
CMethod::GetNextBlock(CInsBlock* i_pblk)
{
	CInsBlock* pblkRet = NULL;
	CInsBlocks::iterator itr = m_Blocks.begin();
	while(itr != m_Blocks.end() && NULL == pblkRet)
	{
		bool bFound = i_pblk == *itr;
		itr++;
		if(bFound && itr != m_Blocks.end())
		{
			pblkRet = *itr;
		}
	}
	return pblkRet;
}

//------------------------------------------------------------------------------
// Get the block corresponding to a branch target.
// TODO: optimize or something. Chasing through the block list seems like a waste.
CInsBlock*		
CMethod::GetTargetBlock(BrTarget_t i_target)
{
	CInsBlock* pblkRet = NULL;
	CInsBlocks::iterator itr = m_Blocks.begin();
	while(itr != m_Blocks.end() && NULL == pblkRet)
	{
		IP_t ipLabel = (*itr)->GetOrigIP();
		if(ipLabel == i_target)
		{
			pblkRet = *itr;
		}
		itr++;
	}
	return pblkRet;
}


//==============================================================================
// class CMethodBody
//

//------------------------------------------------------------------------------
CMethodBody::CMethodBody(CModule* i_pModule, Code_t* i_pCode, size_t i_CodeSize)
: m_pModule(i_pModule)
{
	m_pCode = NULL;
	m_DisposeCode = false;
	NewCode(i_pCode, i_CodeSize);
	m_StartIP = 0;
	ResetIP();
}

//------------------------------------------------------------------------------
CMethodBody::~CMethodBody()
{
	if(m_DisposeCode)
	{
		delete m_pCode;
	}
}

//------------------------------------------------------------------------------
void		
CMethodBody::NewCode(Code_t* i_pCode, size_t i_CodeSize)
{
	delete[] m_pCode;		//delete old one
	m_pCode = new Code_t[i_CodeSize];
	m_DisposeCode = true;
	m_CodeSize = i_CodeSize;
	if(NULL == i_pCode)
	{
		memset(m_pCode, 0, m_CodeSize);
	}
	else
	{
		memcpy(m_pCode, i_pCode, i_CodeSize);
	}
}

//------------------------------------------------------------------------------
// CMethodBody::GetInsDescr()
// Get the instruction descriptor
// In:
// Out:
// Returns: CInsDescr* - Instruction descriptor pointer
//
// Read the instruction descriptor pointer out of the array.
// Then ask that one if it's special; if so, call its factory method.
//
CInsDescr*
CMethodBody::GetInsDescr()
{
	CInsDescr* pInsDesc = NULL;
	if(m_CodeSize > 0)
	{
		unsigned InsCode = m_pCode[m_IP];
		pInsDesc = (*(m_pModule->GetInsSet()))[InsCode];
		if(pInsDesc->IsSpecial())
		{
			pInsDesc = pInsDesc->InsDescrFactory(*this);
		}
	}
	return pInsDesc;
}

//------------------------------------------------------------------------------
// CMethodBody::Advance()
// Advance to the next instruction in the code
// In:
// Out:
// Returns: CInsDescr* - Instruction descriptor pointer
//
// Uses m_CurrInsDescr, a pointer to the "current" instruction
// descriptor. This is the descriptor that corresponds to the "current"
// instruction, as represented by m_IP.
//
// Passes *this (as a reference) so the descriptor can ask for the
// method base pointer and opcode.
//
// Do not call this method after it has returned NULL. When it
// returns NULL once, m_IP points past the end of the instructions.
// Doesn't do checking because that would slow the common case.
//

CInsDescr*
CMethodBody::Advance()
{
	CInsDescr* pid = NULL;

	// Advance IP. If the result is in range, get new ins descr
	m_IP += m_pCurrInsDescr->GetSize(*this);

	// If we haven't fallen off the end, get descr for new insn 
	if(m_IP < m_StartIP + m_CodeSize)
	{
		pid = GetInsDescr();
	}

	m_pCurrInsDescr = pid;	// remember the current descr (or NULL)
	return pid;
}

//------------------------------------------------------------------------------
void
CMethodBody::ResetIP()
{
	m_IP = 0;
	m_pCurrInsDescr = GetInsDescr();
}

//------------------------------------------------------------------------------
// Inject
// Inject code bytes at current IP
//
void		
CMethodBody::Inject(BYTE* i_pCode, size_t i_Size)
{
	if(m_IP + i_Size > m_CodeSize)
	{
		throw CModuleException(CModuleException::X_REASON_CODE_OVERRUN, m_pModule->GetName());
	}
	else
	{
		memcpy(m_pCode + m_IP, i_pCode, i_Size);
		m_IP += i_Size;
	}
}

//==============================================================================
// class CLineNumbers
//

//------------------------------------------------------------------------------
// FindAddress
// In:
//		i_IP	- IP to find
//
// Returns:
//		IP_t	- New IP it is mapped to or -1 if not found
//
IP_t 
CLineNumbers::FindAddress(IP_t i_IP)
{
	IP_t ipRet = (IP_t)-1;

	CLineNumbers::iterator iter = find(i_IP);
	if(iter != end())
	{
		ipRet = iter->second;
	}
	return ipRet;
}

//------------------------------------------------------------------------------
// SetNewTarget
// In:
//		i_oldIP		- original IP from the line number table
//		i_newIP		- new IP for the line
//
// Stores new IP for an address referenced in the line number table
// If the original address can't be found, does nothing
void 
CLineNumbers::SetNewTarget(IP_t i_oldIP, IP_t i_newIP)
{
	CLineNumbers::iterator iter = find(i_oldIP);
	if(iter == end())
	{
		// Silently ignore calls on IPs that don't match an entry.
		// This way the caller can call SetNewTarget without
		// first calling FindAddress.
	}
	else
	{
		iter->second = i_newIP;
	}
}

//------------------------------------------------------------------------------
// Dump
void
CLineNumbers::Dump(ostream& i_os)
{
	i_os << "Line numbers map\n";
	for(CLineNumbers::iterator iter = begin(); iter != end(); iter++)
	{
		i_os << '\t' << iter->first << " -> " << iter->second << endl;
	}
}

//==============================================================================
void			
CMtdExTable::AddException(CMethodException* i_pmtdex, Order_enu i_enuOrder)
{
	switch(i_enuOrder)
	{
		case CMtdExTable::AUTO:
		{
			//TODO: add the exception range search here
			// meanwhile throw an exception
			CModuleException ex(CModuleException::X_REASON_INTERNAL_ERROR);
			throw ex;
			break;
		}
		case CMtdExTable::TOP:
			insert(this->begin(), i_pmtdex);
			break;
		case CMtdExTable::BOTTOM:
			push_back(i_pmtdex);
			break;
		default:
			CModuleException ex(CModuleException::X_REASON_INTERNAL_ERROR);
			throw ex;
	}
}

//==============================================================================
// class CLabels
//
unsigned 
CLabels::GetLabelInstructionTarget(IP_t i_IP)
{
	CLabels::iterator iterLabels;
	iterLabels = find(i_IP);
	if(iterLabels == end())
	{
		return (unsigned) -1;
	}
	else
	{
		return (*iterLabels).second.newInstructionIP;
	}
}

unsigned 
CLabels::GetLabelBlockTarget(IP_t i_IP)
{
	CLabels::iterator iterLabels;
	iterLabels = find(i_IP);
	if(iterLabels == end())
	{
		return (unsigned) -1;
	}
	else
	{
		return (*iterLabels).second.newBlockIP;
	}
}

//
// The i_newInstructionIP argument is the value that will be returned for GetLabelInstructionIP;
// that function is used when patching exception tables.
//
// The i_newBlockIP argument is the value that will be returned for GetLabelBlockIP;
// that function is used when patching "goto" instructions.
//
// Note: this is a no-op if old_IP isn't in the label table. That way
// the caller can call SetLabelTargets without first calling IsLabel.
//
void
CLabels::SetLabelTargets(IP_t i_oldIP, IP_t i_newInstructionIP, IP_t i_newBlockIP)
{
	CLabels::iterator iterLabels;
	iterLabels = find(i_oldIP);
	if(iterLabels != end())
	{
		(*iterLabels).second.newInstructionIP = i_newInstructionIP;
		(*iterLabels).second.newBlockIP = i_newBlockIP;
	}
	else {
		// No label with this oldIP.
		// No harm done - return silently.
	}
}

bool CLabels::IsLabel(IP_t i_IP)
{
	return (GetLabelInstructionTarget(i_IP) != -1);
}

void
CLabels::Dump(ostream &i_os) const
{
	i_os << "Label Table:" << endl;
	const_iterator it = begin();
	const_iterator it_end = end();
	for (; it != it_end; it++) {
		i_os << '\t' << it->first << " -> " << 
			it->second.newInstructionIP <<
			" , " <<
			it->second.newBlockIP << endl;
	}
}

//==============================================================================
// class CInstructions
//

//------------------------------------------------------------------------------
CInstructions::~CInstructions()
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		CInstruction *pins = *iter;
		delete pins;
	}
}

//------------------------------------------------------------------------------
void
CInstructions::Dump(ostream& i_os, CMethod& i_Method)
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		(*iter)->Dump(i_os, i_Method);	
	}
}

//==============================================================================
// class CInsBlock
//

//------------------------------------------------------------------------------
// UpdateLabelAndSize: set the m_Label field to the argument,
// and compute the size based on GetSize of the component instructions.
//

IP_t
CInsBlock::UpdateIP(IP_t i_IP)
{
	IP_t IP = i_IP;
	m_ipLabel = IP;
	CInstructions::iterator iter;
	for(iter = m_Instrs.begin(); iter != m_Instrs.end(); iter++)
	{
		CInstruction* pins = *iter;
		IP = IP + pins->GetSize(IP);
	}
	
	return IP;
}

//------------------------------------------------------------------------------
void 
CInsBlock::Emit(CMethod &o_Method)
{
	CInstructions::iterator iter;
	for(iter = m_Instrs.begin(); iter != m_Instrs.end(); iter++)
	{
		(*iter)->Emit(o_Method);
	}
}

//------------------------------------------------------------------------------
void 
CInsBlock::Dump(ostream& i_os, CMethod& i_Method)
{
	i_os << GetLabel() << ":" << endl;
	m_Instrs.Dump(i_os, i_Method);
}

//------------------------------------------------------------------------------
// GetLength
// Returns number of instructions in the block;
size_t CInsBlock::GetLength() const
{
	return m_Instrs.size();
}

//==============================================================================
// CInsBLocks

//------------------------------------------------------------------------------
CInsBlocks::CInsBlocks()
{
	;
}

//------------------------------------------------------------------------------
CInsBlocks::~CInsBlocks()
{
	for (iterator iter = begin(); iter < end(); iter++)
	{
		delete *iter;
	}
}

//------------------------------------------------------------------------------
void	
CInsBlocks::Dump(ostream& i_os, CMethod& i_Method)
{
	for (iterator iter = begin(); iter < end(); iter++)
	{
		(*iter)->Dump(i_os, i_Method);
	}
}

//= End Of Module.cpp ==========================================================
