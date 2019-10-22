 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JMemStream.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// JMemStream.cpp
//
// Java Memory Bsed Stream. 
// Implementation of abstract class CJBaseStream
// This implementation uses a preallocated memory buffer for input/output
//
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include <stdlib.h>
#include <string.h>
//#define _DLL_EXPORT
#include "JMemStream.h"

//------------------------------------------------------------------------------
CJMemStream::CJMemStream()
: m_lPos(0)
, m_fIsOpen(false)
, m_fFreeMem(false)
, m_pMem(NULL)
, m_lSize(0)
{
	;
}

//------------------------------------------------------------------------------
CJMemStream::~CJMemStream()
{
	if(IsOpen())
	{
		Close();
	}
}


//------------------------------------------------------------------------------
long 
CJMemStream::GetPos()
{
	return m_lPos;
}

//------------------------------------------------------------------------------
void 
CJMemStream::Read(void* i_pBuffer, long i_Size)
{
	if(!IsOpen())
	{
		throw CJMemStreamException(CJMemStreamException::X_NOT_OPENED);
	}
	if(m_lPos + i_Size > m_lSize)
	{
		throw CJMemStreamException(CJMemStreamException::X_DATA_OVERRUN);
	}
	memcpy(i_pBuffer, (BYTE*)m_pMem + m_lPos, i_Size);
	m_lPos += i_Size;
}

//------------------------------------------------------------------------------
void 
CJMemStream::Write(void* i_pData, long i_Size)
{
	if(!IsOpen())
	{
		throw CJMemStreamException(CJMemStreamException::X_NOT_OPENED);
	}
	if(m_lPos + i_Size > m_lSize)
	{
		throw CJMemStreamException(CJMemStreamException::X_DATA_OVERRUN);
	}
	memcpy((BYTE*)m_pMem + m_lPos, i_pData, i_Size);
	m_lPos += i_Size;
}

//------------------------------------------------------------------------------
void	
CJMemStream::Open(void* i_pMem, long i_lSize, bool i_fFreeMem)
{
	if(IsOpen())
	{
		throw CJMemStreamException(CJMemStreamException::X_ALREADY_OPENED);
	}
	m_pMem = i_pMem;
	m_lPos = 0;
	m_lSize = i_lSize;
	m_fIsOpen = true;
	m_fFreeMem = i_fFreeMem;
}

//------------------------------------------------------------------------------
void	
CJMemStream::Close()
{
	if(!IsOpen())
	{
		throw CJMemStreamException(CJMemStreamException::X_NOT_OPENED);
	}
	m_fIsOpen = false;
	if(m_fFreeMem)
	{
		free(m_pMem);
	}
}

//------------------------------------------------------------------------------
bool	
CJMemStream::IsOpen() const
{
	return m_fIsOpen;
}

//= End of JMemStream.cpp ======================================================
