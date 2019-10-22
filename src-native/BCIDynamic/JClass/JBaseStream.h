 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JBaseStream.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// JBaseStream.h
//
// Java Basic Input/Output Stream
//
//==============================================================================

//------------------------------------------------------------------------------
// JBaseStream
// Java Basic Stream. 
// Abstract class
// Defines low - level operations on Java input/output data stream
//
#ifndef _JBASESTREAM_H
#define _JBASESTREAM_H
#ifndef _COMMONDEF_H
#	include "CommonDef.h"
#endif

class _EXT_REF CJBaseStream
{
public:
	virtual ~CJBaseStream() {;}

	virtual long GetPos() = 0;
	virtual void Read(void* i_pbuffer, long i_size) = 0;
	virtual void Write(void* i_pdata, long i_size) = 0;
};

#endif //_JBASESTREAM_H
//= End of JBaseStream.hxx =====================================================
