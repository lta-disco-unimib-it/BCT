 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JFileStream.cpp,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
//
//------------------------------------------------------------------------------
// JFileStream.cpp
//
// Java File Stream implementation
//
// This implementation is system-specific.
// It should implement conversion to Big Endian format as necessary
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "CommonDef.h"
#include "JFileStream.h"

//------------------------------------------------------------------------------
// Helper functions
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// CJFileStream implementation
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// Constructor
//
CJFileStream::CJFileStream(CSTR i_Name, Access_t i_Access)
{
	m_IsOpen = false;
	if(NULL != i_Name)
	{
		Open(i_Name, i_Access);
	}
}

//------------------------------------------------------------------------------
// Destructor
CJFileStream::~CJFileStream()
{
	if(IsOpen())
	{
		Close();
	}
}

//------------------------------------------------------------------------------
// Get position in the stream
// Abstract method implementation
long 
CJFileStream::GetPos()
{
	if(IsOpen())
		return ftell(m_file);
	else
		return -1;
}

//------------------------------------------------------------------------------
// Read from the stream
// Abstract method implementation
void 
CJFileStream::Read(void* i_pBuffer, long i_Size)
{
	long bytes;

	if(!m_IsOpen)
		throw CJFileStreamException(CJFileStreamException::X_NOT_OPENED);
	bytes = fread(i_pBuffer, 1, i_Size, m_file);
	if(bytes != i_Size)
	{
		throw CJFileStreamException(CJFileStreamException::X_FILE_ERROR, LAST_ERROR);
	}
}

//------------------------------------------------------------------------------
// Write to the stream
// Abstract method implementation
void 
CJFileStream::Write(void* i_pData, long i_Size)
{
	long bytes;

	if(!m_IsOpen)
		throw CJFileStreamException(CJFileStreamException::X_NOT_OPENED);
	bytes = fwrite(i_pData, 1, i_Size, m_file);
	if(bytes != i_Size)
	{
		throw CJFileStreamException(CJFileStreamException::X_FILE_ERROR, LAST_ERROR);
	}
}

//------------------------------------------------------------------------------
// Open file stream
void 
CJFileStream::Open(CSTR i_Name, Access_t i_Access)
{
	const char* mode;

	if(m_IsOpen)
		throw CJFileStreamException(CJFileStreamException::X_ALREADY_OPENED);

	switch(i_Access)
	{
	case ACCESS_READ:
		mode = "rb";
		break;
	case ACCESS_WRITE:
		mode = "wb";
		break;
	default:
		throw CJFileStreamException(CJFileStreamException::X_BAD_MODE);
		break;
	}
	m_file = fopen(i_Name, mode);

	if (NULL == m_file)
	{
		m_IsOpen = false;
		throw CJFileStreamException(CJFileStreamException::X_OPEN_ERROR, LAST_ERROR);
	}
	else
	{
		m_IsOpen = true;
	}
}

//------------------------------------------------------------------------------
// Close file stream
void 
CJFileStream::Close()
{
	if(IsOpen())
	{
		fclose(m_file);
	}
}

//------------------------------------------------------------------------------
// Is file stream opened?
bool 
CJFileStream::IsOpen() const
{
	return m_IsOpen;
}

//= End of JClassFile.cxx ======================================================
