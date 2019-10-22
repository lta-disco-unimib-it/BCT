 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JStream.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//*
//* JStream.h
//*

//==============================================================================
// Copyright .....
//
//------------------------------------------------------------------------------
// JStream.h
//
// Java Stream Class
//
//==============================================================================

#ifndef _JSTREAM_H
#define _JSTREAM_H

#ifndef _JAVADEF_H
#include "JavaDef.h"
#endif
#ifndef _BASESTREAM_H
#include "JBaseStream.h"
#endif

class _EXT_REF CJStream;
class _EXT_REF CJStreamException;

//------------------------------------------------------------------------------
// CJStream
// This class actually performs input / output
// using existing implementation of abstract CJBaseStream.
// You have to supply CJBaseStream implementation in the constructor
//
class CJStream
{
public:
	CJStream(CJBaseStream* i_pBaseStream);
	~CJStream();
	u4		GetPos();
	void	Read(void* i_pBuffer, long i_Size);
	void	Write(void* i_pData, long i_Size);
	void	ReadUtf8(void* i_pBuffer, long i_Size);
	void	WriteUtf8(void* i_pData, long i_Size);
	CJStream& operator >> (u1& i_u1);
	CJStream& operator << (u1 i_u1);
	CJStream& operator >> (u2& i_u2);
	CJStream& operator << (u2 i_u2);
	CJStream& operator >> (u4& i_u4);
	CJStream& operator << (u4 i_u4);

protected:

private:
	CJBaseStream*	m_pBaseStream;		// Base stream implementation ptr.
};

//------------------------------------------------------------------------------
// CJStreamException
// Exception thrown on CJStream operation failure
class CJStreamException
{
public:
	enum {
		X_UNKNOWN,
		X_OPEN_ERROR,
		X_READ_ERROR,
		X_WRITE_ERROR,
		X_LAST
	};

	CJStreamException()
	{
		m_reason = X_UNKNOWN;
		m_error = 0;
	}

	unsigned GetReason()
	{
		return m_reason;
	}

	unsigned GetError()
	{
		return m_error;
	}

private:
	unsigned m_reason;
	unsigned m_error;
};

#endif //!defined(_JSTREAM_H)
