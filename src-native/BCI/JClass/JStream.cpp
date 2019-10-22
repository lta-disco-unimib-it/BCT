 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JStream.cpp,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//------------------------------------------------------------------------------
// JStream.cpp
// 
// Implementation of CJStream class
//
// CJStream uses abstract class CBaseStream for atomic input/output operations
// Actual implementation of base stream is passed in the class constructor
//
// ?? Do we need SetStream to change basic stream on the fly?
//------------------------------------------------------------------------------

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "CommonDef.h"
#include "JStream.h"


//------------------------------------------------------------------------------
// CJStream
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
CJStream::CJStream(CJBaseStream* i_pBaseStream)
{
	m_pBaseStream = i_pBaseStream;
}

//------------------------------------------------------------------------------
CJStream::~CJStream()
{

}

//------------------------------------------------------------------------------
u4
CJStream::GetPos()
{
	return u4(m_pBaseStream->GetPos());
}

//------------------------------------------------------------------------------
void	
CJStream::Read(void* i_pBuffer, long i_Size)
{
	m_pBaseStream->Read(i_pBuffer, i_Size);
}

//------------------------------------------------------------------------------
void	
CJStream::Write(void* i_pData, long i_Size)
{
	m_pBaseStream->Write(i_pData, i_Size);
}

//------------------------------------------------------------------------------
void	
CJStream::ReadUtf8(void* i_pBuffer, long i_Size)
{
	//ToDo : UTF8 conversion
	m_pBaseStream->Read(i_pBuffer, i_Size);
}

//------------------------------------------------------------------------------
void	
CJStream::WriteUtf8(void* i_pData, long i_Size)
{
	//ToDo : UTF8 Conversion
	m_pBaseStream->Write(i_pData, i_Size);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator >> (u1& i_u1)
{
	m_pBaseStream->Read((void*)&i_u1, sizeof(u1));
	return (*this);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator << (u1 i_u1)
{
	m_pBaseStream->Write((void*)&i_u1, sizeof(u1));
	return (*this);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator >> (u2& i_u2)
{
	m_pBaseStream->Read((void*)&i_u2, sizeof(u2));
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_u2, sizeof(u2));
#endif
	return (*this);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator << (u2 i_u2)
{
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_u2, sizeof(u2));
#endif
	m_pBaseStream->Write((void*)&i_u2, sizeof(u2));
	return (*this);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator >> (u4& i_u4)
{
	m_pBaseStream->Read((void*)&i_u4, sizeof(u4));
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_u4, sizeof(u4));
#endif
	return (*this);
}

//------------------------------------------------------------------------------
CJStream& 
CJStream::operator << (u4 i_u4)
{
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_u4, sizeof(u4));
#endif
	m_pBaseStream->Write((void*)&i_u4, sizeof(u4));
	return (*this);
}

//= End of JStream.cpp =========================================================
