 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JMemStream.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//*
//* JMemStream.h
//*

//==============================================================================
// Copyright .....
//
//------------------------------------------------------------------------------
// JMemStream.h
//
// Java Memory Bsed Stream. 
// Implementation of abstract class CJBaseStream
// This implementation uses a preallocated memory buffer for input/output
//
//==============================================================================
#ifndef _JMEMSTREAM_H
#define _JMEMSTREAM_H

#ifndef _COMMONDEF_H
#	include "CommonDef.h"
#endif
#ifndef _JAVADEF_H
#	include "JavaDef.h"
#endif
#ifndef _JBASESTREAM_H
#	include "JBaseStream.h"
#endif

class _EXT_REF CJMemStream;
class _EXT_REF CJMemStreamException;

class _EXT_REF CJMemStream : public CJBaseStream
{
public:
	CJMemStream();
	virtual ~CJMemStream();

	virtual long GetPos();
	virtual void Read(void* i_pBuffer, long i_Size);
	virtual void Write(void* i_pData, long i_Size);

	void	Open(void* i_pMem, long i_lSize, bool i_fFreeMem = false);
	void	Close();
	bool	IsOpen() const;

protected:
private:
	bool	m_fIsOpen;
	bool	m_fFreeMem;
	long	m_lPos;
	void*	m_pMem;
	long	m_lSize;
};

//------------------------------------------------------------------------------
// CJFileStreamException
//
class _EXT_REF CJMemStreamException
{
public:
	typedef enum {
		X_UNKNOWN,
		X_NOT_OPENED,
		X_ALREADY_OPENED,
		X_OPEN_ERROR,
		X_DATA_OVERRUN,
		X_LAST
	} Reason_t;

public:
	CJMemStreamException(Reason_t i_Reason = X_UNKNOWN)
	{
		m_Reason = i_Reason;
	}
	unsigned GetReason()const {return m_Reason;}

private:
	Reason_t m_Reason;
};


#endif // _JMEMSTREAM_H
//==============================================================================
