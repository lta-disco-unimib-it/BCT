 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngProbeInterface.h,v 1.1.2.3 2007-10-25 15:39:44 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// BCIEngProbeInterface.h
//------------------------------------------------------------------------------
// External representation for the probe kit.
// This header file is part of the redistributable Probe Kit package
// and it may be compiled with both C and C++ compilers.
//
// Here are some details.
// CBCIEngProbe and underlying CBCIEng, CJClassFile etc. are designed and 
// implemented as C++ libraries. You can tweak these projects to build as both
// DLL or static library depending on your needs. Most of the current projects
// are built around staic version of these libraries.
//
// To simplify integration with instrumentation engine hosts (such as JVMPI agent
// or static instrumentation utility) we created this additional interface
// layer that simplifies the external representation of the instrumentation
// engine(s). This interface can be used as C++ library for OO projects as 
// well as plain C library for standard C and other language environments 
// that cn't resolve MS C++ name mangling.
//
// Both interfaces are using CreateBCIEngine and DestroyBCIEngine calls. 
// CreateBCIEngine returns a void* pointer that in fact is pointing 
// to a CBCIEnineInterface class. C++ program should just use this interface
// pointer after appropriate typecasting. Regular C program should supply
// this pointer as the first argument af all subsequent calls.
//
// There is a difference between C++ and standard C interfaces in the exception 
// handling. CBCIEngineInterface methods may throw CBCIEngInterfaceException.
// This exception class has two virtual methods:
// GetExceptionSource() and GetReason().
// Since the exception class has only virtual methods, it is not necessary to 
// export it from the DLL.
//
// Since it is hard to handle C++ exceptions in a standard C program, the
// standard C functions return unsigned integer that contains same information
// as CBCIEngInterfaceException:
// The low order word contains Exception Code while the high order word contains
// Exception Source. 
//
//==============================================================================
#ifndef _BCIENGPROBEINTERFACE_H
#define _BCIENGPROBEINTERFACE_H
 // Make the "C" interface exported
#include "CommonDef.h"
#include "BCIEngProbe.h"
#include "BCIEngInterface.h"

#include <iostream>
#include <sstream>
USE_NAMESPACE(std);


#define BCI_ENG_PROBE_1_0	0x00010000

//------------------------------------------------------------------------------
class CBCIEngProbeInterface : public CBCIEngInterface
{
public:

	virtual void GetEngVersion(unsigned& o_uVersion);
	
	virtual void GetEngDescription(const char*& o_szDescription);

	virtual void Initialize(const char* i_pchOptions, size_t i_cbOptions);

	virtual void Instrument(void* i_pInClass, size_t i_cbInClass, 
		                    void** o_ppOutClass, size_t* o_pcbOutClass);

	virtual void SetAllocator(pfnMalloc_t i_pfnMalloc)
	{
		m_peng.SetAllocator(i_pfnMalloc);
	}

	virtual void SetCallback(pfnCallback_t i_pfnCallback, unsigned i_uFlags)
	{
		m_peng.SetCallback(i_pfnCallback, i_uFlags);
	}

private:
	virtual void printProbe( CProbe* pprobe );
	virtual void ruleToCall( CProbe* probeCall, CProbe* probeExec, CFilterRuleList& exclude );
	virtual void ruleWithin( CProbe* probeCall, CProbe* probeExec );  
	CBCIEngProbe m_peng;
	static const  char* bctProbePrefix;	
	
	virtual const char* getCallProbe( const string& probeName, int componentNumber );

	virtual const char* getMethodProbe( const string& probeName, int componentNumber );

	virtual const char* getProbe( const string& prefix, const int componentNumber, const char* postfix );
	
};

 
//------------------------------------------------------------------------------
class CBCIEngProbeInterfaceException : public CBCIEngInterfaceException
{
public:
	enum ex_source_tag
	{
		EXSRC_UNKNOWN,			// Unknown source
		EXSRC_INTERFACE,		// The interface implementation
		EXSRC_JCLASSFILE,		// CJClassFileException
		EXSRC_MODULE,			// CModuleException
		EXSRC_MODULEJ,			// CModuleJException
		EXSRC_BCIENG,			// CBCIEngine
		EXSRC_BCIENGJ,			// CBCIEngJ
		EXSRC_BCIENGPROBE,		// CBCIEngProbe
		EXSRC_LAST				// 
	};

	enum ex_code_tag
	{
		EX_OK,
		EX_UNKNOWN,
		EX_BAD_PROBE_ARGS,
		EX_INVALID_FILTER_TYPE,
		EX_LAST
	};

	CBCIEngProbeInterfaceException(unsigned short i_uSource = 0, unsigned short i_uCode = 0)
	:CBCIEngInterfaceException(i_uSource, i_uCode){}
	CBCIEngProbeInterfaceException(unsigned short i_uSource, unsigned short i_uCode, const char* i_szReason)
	:CBCIEngInterfaceException(i_uSource, i_uCode, i_szReason){}
};

#endif // defined _BCIENGPROBEINTERFACE_H

//= End of BCIEngProbeInterface.h ==============================================
