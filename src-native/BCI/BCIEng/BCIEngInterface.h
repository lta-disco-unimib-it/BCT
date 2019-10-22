 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngInterface.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

/*
//==============================================================================
// BCIEngInterface.h
//------------------------------------------------------------------------------
// BCI Engine interface for profiling agents.
// This interface defines the way profiling agents interact 
// with instrumentation engines.
//==============================================================================
*/

#ifndef _BCIENGINTERFACE_H
#define _BCIENGINTERFACE_H

#ifdef _BCIENGINTERFACE_DLL_EXPORT
#	ifdef WIN32
#		define	_BCIENGINTERFACE_EXT_REF __declspec(dllexport)
#	else 
#		define	_BCIENGINTERFACE_EXT_REF extern
#	endif
#elif defined _BCIENGINTERFACE_DLL_IMPORT 
#	ifdef WIN32
#		define _BCIENGINTERFACE_EXT_REF __declspec(dllimport)
#	else 
#		define	_BCIENGINTERFACE_EXT_REF extern
#	endif
#else
#	define _BCIENGINTERFACE_EXT_REF 
#endif

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */
  typedef void* (*pfnMalloc_t)(size_t);
  typedef int   (*pfnCallback_t)(const char* i_pInfo, size_t i_cbInfo, unsigned i_wFlags);
#ifdef __cplusplus
};
#endif /* __cplusplus */

#ifdef __cplusplus
class CBCIEngInterface
{
public:
	virtual ~CBCIEngInterface(){;}
	virtual void GetEngVersion(unsigned& o_uVersion) = 0;
	virtual void GetEngDescription(const char *& o_szDescription) = 0;
	virtual void Initialize(const char * i_pchOptions, size_t i_cbOptions) = 0;
	virtual void Instrument(void* i_pInClass, size_t i_cbInClass, 
		                    void** o_ppOutClass, size_t* o_pcbOutClass) = 0;
	virtual void SetAllocator(pfnMalloc_t i_pfnMalloc) = 0;
	virtual void SetCallback(pfnCallback_t i_pfnCallback, unsigned i_uFlags) = 0;
};


class CBCIEngInterfaceException 
{
private:
	enum { MSG_BUF_LEN = 256 };

public:
	CBCIEngInterfaceException(unsigned short i_uSource = 0, unsigned short i_uCode = 0)
	{
		m_uSource = i_uSource;
		m_uCode = i_uCode;
		m_szMessage[0] = '\0';
	}

	CBCIEngInterfaceException(unsigned short i_uSource, unsigned short i_uCode, const char* i_szReason)
	{
		m_uSource = i_uSource;
		m_uCode = i_uCode;
		if (i_szReason != NULL)
		{
			strncpy(m_szMessage, i_szReason, MSG_BUF_LEN);
			m_szMessage[MSG_BUF_LEN - 1] = '\0';
		}
		else
		{
			m_szMessage[0] = '\0';
		}
	}

	virtual unsigned short GetSource() const {return m_uSource;}
	virtual unsigned short GetReason() const {return m_uCode;}

	// FormatMessage returns the number of bytes in the message string, including the null terminator.
	// If the message is longer than the buffer, only bufSize-1 bytes are placed there followed by a null.
	// A return value of zero means no message is available.
	virtual int FormatMessage(char* buffer, size_t bufSize) const {
		if (m_szMessage == NULL) {
			return 0;
		}
		else {
			size_t len_required = strlen(m_szMessage) + 1;
			if (len_required <= bufSize) {
				strcpy(buffer, m_szMessage);
			}
			else {
				if (bufSize > 0) {
					strncpy(buffer, m_szMessage, bufSize-1);
					buffer[bufSize-1] = '\0';
				}
			}
			return len_required;
		}
	}

private:
	unsigned short m_uSource;
	unsigned short m_uCode;
	char m_szMessage[MSG_BUF_LEN];
};
#endif

/*------------------------------------------------------------------------------*/
typedef void* pbcieng_t;

/*------------------------------------------------------------------------------*/
/* Plain vanilla C functions for the probe BCI engine
*/

/* Values for the callback queries/notifications
 * 01 query: want to stop me from instrumenting this module?
 * 02 query: want to stop me from instrumenting this method?
 * 03 notice: I actually did instrument this module.
 * 04 notice: I actually did instrument this method.
 */
#	define BCIENGINTERFACE_CALLBACK_MODULE 0x01
#	define BCIENGINTERFACE_CALLBACK_METHOD 0x02
#	define BCIENGINTERFACE_CALLBACK_MODULE_INSTR 0x03
#	define BCIENGINTERFACE_CALLBACK_METHOD_INSTR 0x04

#ifdef __cplusplus
	extern "C" {
#endif

	_BCIENGINTERFACE_EXT_REF unsigned CreateBCIEngine(pbcieng_t* o_ppeng);
	_BCIENGINTERFACE_EXT_REF unsigned DestroyBCIEngine(pbcieng_t i_peng);
	_BCIENGINTERFACE_EXT_REF unsigned GetEngVersion(pbcieng_t i_pbcieng, unsigned* o_puVersion);
	_BCIENGINTERFACE_EXT_REF unsigned GetEngDescription(pbcieng_t i_pbcieng, char** o_szDescription);
	_BCIENGINTERFACE_EXT_REF unsigned Initialize(pbcieng_t i_pbcieng, const char* i_pchOptions, size_t i_cbOptions);
	_BCIENGINTERFACE_EXT_REF unsigned Instrument(pbcieng_t i_pbcieng, void* i_pInClass, size_t i_cbInClass, 
							          void** o_ppOutClass, size_t* o_pcbOutClass);
	_BCIENGINTERFACE_EXT_REF unsigned SetAllocator(pbcieng_t i_pbcieng, pfnMalloc_t i_pfnMalloc);
	_BCIENGINTERFACE_EXT_REF unsigned SetCallback(pbcieng_t i_pbcieng, pfnCallback_t i_pfnCallback, unsigned i_uFlags);

#ifdef __cplusplus
	}
#endif

#endif /* defined _BCIENGINTERFACE_H */
/*==============================================================================*/

