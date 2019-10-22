 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JFileStream.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//------------------------------------------------------------------------------
// JFileStream.h
//
// Java File Stream. 
// Implementation of abstract class CJBaseStream
// This implementation uses actual file stream (filebuf) for input/output
//
//==============================================================================
#include <stdio.h>

#ifndef _JAVADEF_H
#	include "JavaDef.h"
#endif

#ifndef _JBASESTREAM_H
#	include "JBaseStream.h"
#endif

class _EXT_REF CJFileStream;

class CJFileStream : public CJBaseStream
{
public:
	typedef enum
	{
		ACCESS_READ,
		ACCESS_WRITE,
		ACCESS_LAST
	} Access_t;

public:
	CJFileStream(CSTR i_Name=NULL, Access_t i_Access=ACCESS_READ);
	virtual ~CJFileStream();

	virtual long GetPos();
	virtual void Read(void* i_pBuffer, long i_Size);
	virtual void Write(void* i_pData, long i_Size);

	void	Open(CSTR i_Name, Access_t i_Access);
	void	Close();
	bool	IsOpen() const;

private:
	FILE*		m_file;
	Access_t	m_Access;
	bool		m_IsOpen;
};

class CJFileStreamException
{
public:
	enum
	{
		X_UNKNOWN,
		X_BAD_MODE,
		X_NOT_OPENED,
		X_ALREADY_OPENED,
		X_OPEN_ERROR,
		X_FILE_ERROR,
		X_DATA_OVERRUN,
		X_LAST
	};

	CJFileStreamException(unsigned i_reason = X_UNKNOWN, unsigned i_error = 0)
	{
		m_reason = i_reason;
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

//= end of JFileStream.hxx =====================================================
