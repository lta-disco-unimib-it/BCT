 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEng.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// BCIEng.h
// Started 9/28/99
//
//------------------------------------------------------------------------------
// Description 
// BCI Engine definition.
// 
//==============================================================================

#ifndef _BCIENG_H
#define _BCIENG_H

#include "Module.h"

extern "C"
{
typedef void* (*pfnMalloc_t)(size_t);
typedef int   (*pfnCallback_t)(const char* i_pInfo, size_t i_cbInfo, unsigned i_wFlags);
}

class CBCIEng;
class CBCIEngException;

//------------------------------------------------------------------------------
// BCI Engine
//
class CBCIEng
{
public:
	CBCIEng(){m_pfnMalloc = NULL; m_pfnCallback = NULL; m_wCBFlags = 0;}
	virtual ~CBCIEng(){;}
	virtual void Instrument(CModule* i_pModule) = 0;
	virtual void SetAllocator(pfnMalloc_t i_pfnMalloc){m_pfnMalloc = i_pfnMalloc;}
	virtual void SetCallback(pfnCallback_t i_pfnCallback, WORD i_wCBFlags)
	{
		m_pfnCallback = i_pfnCallback;
		m_wCBFlags = i_wCBFlags;
	}
	
protected:
	pfnMalloc_t		m_pfnMalloc;
	pfnCallback_t	m_pfnCallback;
	WORD			m_wCBFlags;

};

//------------------------------------------------------------------------------
// BCI Engine Exception
//
// ToDo: discuss exceptions
// possible additions to the exception class may be:
// Exception name, Exception message (may be in the debug version only)
//			
class CBCIEngException
{
public:
	enum
	{
		REASON_OK,
		REASON_Unknown,
		REASON_InvalidOpcode,
		REASON_IsInstrumented,
		REASON_CantInstrument,
		REASON_InternalError,
		REASON_LAST
	};

	unsigned GetReason() const {return m_Reason;}
public:
	CBCIEngException(int i_Reason) {m_Reason = i_Reason;}

private:
	int			m_Reason;

};

//------------------------------------------------------------
//
// Misc helper functions
//

#if defined(_DEBUG)
#define DEBUG_PRINTF(arg) printf arg
#else
#define DEBUG_PRINTF(arg) /* nothing */
#endif // defined(_DEBUG)

#endif //defined BCIENG_H

//= End of BCIEng.h ============================================================
