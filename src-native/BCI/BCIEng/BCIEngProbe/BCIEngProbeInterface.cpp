 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngProbeInterface.cpp,v 1.1.2.4 2006-12-02 16:47:54 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// BCIEngProbeInterface
//------------------------------------------------------------------------------
// External representation for the probe kit.
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#ifdef MVS 
#include <unistd.h> /* for __atoe */
#endif

#include "CommonDef.h"

#define _BCIENGINTERFACE_DLL_EXPORT // Make the "C" interface exported

#ifdef WIN32
#pragma warning(disable:4786)
#endif

#include "BCIEngProbe.h"
#include "BCIEngProbeInterface.h"

#define TOK_WHITESPACE " \t"

//------------------------------------------------------------------------------
inline static unsigned ExceptionToError(CBCIEngProbeInterfaceException& ex)
{
	unsigned uErr = ex.GetReason();
	uErr <<= 16;
	uErr |= ex.GetSource();
	return uErr;
}

//==============================================================================
// Implementation of BCI Engine interface for the Probe Kit
//


//------------------------------------------------------------------------------
void 
CBCIEngProbeInterface::GetEngVersion(unsigned& o_uVersion)
{
	o_uVersion = BCI_ENG_PROBE_1_0;
}
	
//------------------------------------------------------------------------------
void
CBCIEngProbeInterface::GetEngDescription(const char*& o_szDescription)
{
	o_szDescription = "BCI Engine for Probe Kit. Version 1.0";
}

//------------------------------------------------------------------------------

// Helper func for CBCIEngProbeInterface::Initialize
//
// Armed with a buffer full of text, we want to split it along the
// newline boundaries by stomping on the newlines. Sounds simple, except 
// the text may have been generated on one OS and parsed on another, so
// you can readily tell where the line terminators are. For example, on
// Windows, it is CR/LF, on most Unix systems it is CR, on Mac it is LF.
//
// This code assumes the incoming text is ASCII, and therefore
// that CR is 13 and LF is 10.
//
// When we find either a CR or LF (\n or \r, on ASCII systems), we replace
// them with a null byte. This is OK because we control the input stream
// enough to know there won't be random occurences of these chars. If they're
// there at all, they're the line terminators. Also, the calling function,
// CBCIEngProbeInterface::Initialize, treats extra null bytes as if they're 
// blank lines in the input file.

static void 
break_into_lines(char *buf, size_t len)
{
	size_t i;

	if ( buf == NULL ) {
		return;
	}

        for (i = 0; i < len; i++) {
		// 13 and 10 are known ASCII values for CR and LF
		// even if this source compiles in a non-ASCII domain.
		if ( buf[i] == 13 || buf[i] == 10 ) {
			buf[i] = '\0';
		}
	}
}

#if defined(MVS) || defined(__OS400__)

/*
 * Function to do a stricmp on two ascii strings, even though
 * strcasecmp wants EBCDIC.
 *
 * This code first checks to see if the input strings are identical,
 * returns zero if so. If not, this code converts each string to EBCDIC
 * and calls the native strcasecmp function.
 */

#if defined(__OS400__)
// Explicitly declare this extern, because getting it from
// /QIBM/include/strings.h is too hard.
extern "C" {
	int strcasecmp(const char*, const char*);
};
#endif

static int ascii_stricmp(char* left, char* right)
{
	int result;

	if (strcmp(left, right) == 0) return 0;	/* easy case first */
	char* left_e = strdup(left);
	__atoe(left_e);
	char* right_e = strdup(right);
	__atoe(right_e);
#if defined(__OS400__)
	result = strcasecmp(left_e, right_e);
#else
	result = stricmp(left_e, right_e);
#endif
	free(left_e);
	free(right_e);
	return result;
}
#define STRICMP ascii_stricmp
#else
#define STRICMP stricmp
#endif

//------------------------------------------------------------------------------
void 
CBCIEngProbeInterface::Initialize(const char* i_pchOptions, size_t i_cbOptions)
{
	CProbe* pprobe = NULL;
	CFilterRuleList* pprefilter = NULL;
	CFilterRuleList* pprefilterWithin = NULL;	//added for within check for callsite
	list<CProbe*> probelist;
	char* szRecType;
	char* szProbePackage;
	char* szProbeClass;
	char* szProbeMethod;
	char* szProbeSignature;
	char* szProbeAction;
	char* szRefType;
	char* szRefClass;
	char* szRefMethod;
	char* szRefSignature;
	char* szRefArgs;
	char* szDummy;
	CProbeFragment::fragmentType_t fragType;
	size_t  cbScan = 0;
	char*	szScan = (char*)i_pchOptions;
	int n = 0;

	break_into_lines(szScan, i_cbOptions);
	for(;cbScan < i_cbOptions; szScan += n + 1, cbScan += n + 1)
	{
		n = strlen(szScan);
		if (n == 0) {
			// blank line? continue
			continue;
		}
		szRecType = strtok(szScan, TOK_WHITESPACE);
		if (szRecType == NULL) {
			// blank line, continue
			continue;
		}

		if(STRICMP(szRecType, "REM") == 0)
		{
			// Comment. Continue the line-reading loop
			continue;
		}
		else if(STRICMP(szRecType, "PROBE") == 0)
		{
			// Start of new probe.
			// Flush out the old one being read, if any
			if(NULL != pprobe)
			{
				probelist.push_back(pprobe);
				pprobe->SetPrefilter(pprefilter);
				pprobe->SetPrefilterWithin(pprefilterWithin);
				m_peng.AddProbe(pprobe);
				
				pprobe = NULL;
				pprefilter = NULL;
				pprefilterWithin = NULL;
			}

			szDummy = strtok(NULL, TOK_WHITESPACE);
			if (szDummy != NULL) {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex; //"Bad PROBE line, too many parameters";
			}

			pprobe = m_peng.CreateProbe();
		}
		else if (STRICMP(szRecType, "RULE") == 0
			 ||  STRICMP(szRecType, "FILTER") == 0
			 ||  STRICMP(szRecType, "RULEWITHIN") == 0
			 ||  STRICMP(szRecType, "FILTERWITHIN") == 0
			 )
		{
			// Probe RULE or FILTER
			// The important difference between filters and rules: Rules are specified per probe;
			// filters are specified per set of probes.
			// Filters do not come from the XML probe description. In fact XML doesn't have 
			// appropriate element. Filters usually come from UI and specify the first tier of
			// filtering (aka prefiltering) used to narrow down the targeting for probes from a probe set.
			szProbePackage = strtok(NULL, TOK_WHITESPACE);
			szProbeClass = strtok(NULL, TOK_WHITESPACE);
			szProbeMethod = strtok(NULL, TOK_WHITESPACE);
			szProbeSignature = strtok(NULL, TOK_WHITESPACE);
			szProbeAction = strtok(NULL, TOK_WHITESPACE);
			szDummy = strtok(NULL, TOK_WHITESPACE);

			if (szProbeAction == NULL) {
				// string ended before reading the action token
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex; //"Bad RULE line, not enough parameters";
			}
			if (szDummy != NULL) {
				throw "Bad RULE line, too many parameters";
			}

			CFilterRule::action_t action;
			if (STRICMP(szProbeAction, "include") == 0) {
				action = CFilterRule::ACTION_INCLUDE;
			}
			else if (STRICMP(szProbeAction, "exclude") == 0) {
				action = CFilterRule::ACTION_EXCLUDE;
			}
			else {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_INVALID_FILTER_TYPE);
				throw ex; //"Bad action string in RULE line, must be include or exclude";
			}
			if (STRICMP(szRecType, "RULE") == 0)
				// This is the probe targeting rule
			{	// Add rule to the current probe 
				pprobe->AddFilterRule(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
			}
			else if (STRICMP(szRecType, "RULEWITHIN") == 0)
			{
				pprobe->AddFilterRuleWithin(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
			}
			else
			{
				// This is a prefilter ("FILTER" statement)
				// Add to the prefilters
				if(pprefilter == NULL)
				{
					pprefilter = new CFilterRuleList();
				}
				if(pprefilterWithin == NULL)
				{
					pprefilterWithin = new CFilterRuleList();
				} 
				CFilterRule fr(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
				
				if (STRICMP(szRecType, "FILTER") == 0)
					pprefilter->push_back(fr);
				else
					pprefilterWithin->push_back(fr);
			}
		}
		else if(NULL != pprobe && STRICMP(szRecType, "REF") == 0)
		{
			// Pick off the type (entry, exit, catch) and the class, method, and signature,
			// plus the argument list - which will be NULL if it's missing
			szRefType = strtok(NULL, TOK_WHITESPACE);
			szRefClass = strtok(NULL, TOK_WHITESPACE);
			szRefMethod = strtok(NULL, TOK_WHITESPACE);
			szRefSignature = strtok(NULL, TOK_WHITESPACE);
			szRefArgs = strtok(NULL, TOK_WHITESPACE);		// might be NULL if this ref takes no args
			szDummy = strtok(NULL, TOK_WHITESPACE);

			if (szRefArgs == NULL) szRefArgs = "";
			if (szRefSignature == NULL) {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex; //"Bad REF line, not enough parameters";
			}
			if (szDummy != NULL) {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex; //"Bad REF line, too many parameters";
			}

			if(STRICMP(szRefType, "ONENTRY") == 0)
				fragType = CProbeFragment::PROBE_ONENTRY;
			else if(STRICMP(szRefType, "ONEXIT") == 0)
				fragType = CProbeFragment::PROBE_ONEXIT;
			else if(STRICMP(szRefType, "ONCATCH") == 0)
				fragType = CProbeFragment::PROBE_ONCATCH;
			else if(STRICMP(szRefType, "BEFORECALL") == 0)
				fragType = CProbeFragment::PROBE_BEFORECALL;
			else if(STRICMP(szRefType, "AFTERCALL") == 0)
				fragType = CProbeFragment::PROBE_AFTERCALL;
			else if(STRICMP(szRefType, "STATICINITIALIZER") == 0)
				fragType = CProbeFragment::PROBE_STATICINITIALIZER;
			else if(STRICMP(szRefType, "EXECUTABLEUNIT") == 0)
				fragType = CProbeFragment::PROBE_EXECUTABLEUNIT;
			else {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
													CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex;
			}
			pprobe->AddFragment(fragType, szRefClass, szRefMethod, szRefSignature, "static", szRefArgs);
		}
		else if (NULL != pprobe && STRICMP(szRecType, "STATICFIELD") == 0)
		{
			// Pick off the static field's type
			szRefClass = strtok(NULL, TOK_WHITESPACE);
			if (szRefClass == NULL) {
				CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_INTERFACE,
					                              CBCIEngProbeInterfaceException::EX_BAD_PROBE_ARGS);
				throw ex; //"Bad STATICFIELD line, not enough parameters";
			}
			pprobe->AddStaticField(szRefClass);
		}
			

		// else some unknown opcode or something, just ignore
	}

	// End of file - write out the last probe we were scanning, if any
	if (pprobe != NULL) {
		probelist.push_back(pprobe);
		pprobe->SetPrefilter(pprefilter);
		pprobe->SetPrefilterWithin(pprefilterWithin);
		m_peng.AddProbe(pprobe);
				
		pprobe = NULL;
		
				
	}

	//if(pprefilter != NULL)
	//{
	//	m_peng.AddPrefilter(pprefilter);		// Save prefilter in the engine
	//}
	
	//if(pprefilterWithin != NULL)
	//{
	//	m_peng.AddPrefilterWithin(pprefilterWithin);		// Save prefilter in the engine
	//}
	
	// Set prefilter for each probe in this set
	// In this implementation all probes delivered in a single set obtain identical prefilters.
	//for(list<CProbe*>::iterator itr = probelist.begin(); itr != probelist.end(); itr++)
	//{
	//	(*itr)->SetPrefilter(pprefilter);
	//	(*itr)->SetPrefilterWithin(pprefilterWithin);
	//	m_peng.AddProbe(*itr);
	//}

}


//------------------------------------------------------------------------------
void 
CBCIEngProbeInterface::Instrument(void* i_pInClass, size_t i_cbInClass, 
		                          void** o_ppOutClass, size_t* o_pcbOutClass)
{
	try
	{
		m_peng.Instrument(i_pInClass, i_cbInClass, o_ppOutClass, o_pcbOutClass);
	}
	catch (CJClassFileException& i_ex)
	{
		CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_JCLASSFILE,
					                      i_ex.GetReason());
		throw ex;
	}
	catch (CBCIEngProbeException& i_ex) 
	{
		CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_BCIENGPROBE,
					                      i_ex.GetReason(),
										  i_ex.GetStringReason());
		throw ex;
	}
	catch (CBCIEngException& i_ex)
	{
		CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_BCIENG,
					                      i_ex.GetReason());
		throw ex;
	}
	catch (CModuleException& i_ex)
	{
		CBCIEngProbeInterfaceException ex(CBCIEngProbeInterfaceException::EXSRC_MODULE,
					                      i_ex.GetReason());
		throw ex;
	}
}

//------------------------------------------------------------------------------


//==============================================================================
// Plain vanilla C implementation of the probe kit interface
//

//------------------------------------------------------------------------------
unsigned GetEngVersion(pbcieng_t i_pbcieng, unsigned* o_puVersion)
{
	unsigned uErr = 0;
	try
	{
		CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
		peng->GetEngVersion(*o_puVersion);
	}
	catch(...)
	{
	}
	return uErr;
}

//------------------------------------------------------------------------------
unsigned GetEngDescription(pbcieng_t i_pbcieng, char** o_szDescription)
{
	unsigned uErr = 0;
	try
	{
		CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
		peng->GetEngDescription(*(const char**)o_szDescription);
	}
	catch(...)
	{
	}
	return uErr;
}

//------------------------------------------------------------------------------
unsigned Initialize(pbcieng_t i_pbcieng, CSTR i_pchOptions, size_t i_cbOptions)
{
	unsigned uErr = 0;
	try
	{
		CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
		peng->Initialize(i_pchOptions, i_cbOptions);
	}
	catch (CBCIEngProbeInterfaceException& ex)
	{
		uErr = ExceptionToError(ex);
	}
	return uErr;
}

//------------------------------------------------------------------------------
unsigned Instrument(pbcieng_t i_pbcieng, void* i_pInClass, size_t i_cbInClass, 
		            void** o_ppOutClass, size_t* o_pcbOutClass)
{
	unsigned uErr = 0;
	try
	{
		CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
		peng->Instrument(i_pInClass, i_cbInClass, o_ppOutClass, o_pcbOutClass);
	}
	catch (CBCIEngProbeInterfaceException& ex)
	{
		uErr = ExceptionToError(ex);
	}
	return uErr;
}

//------------------------------------------------------------------------------
unsigned SetAllocator(pbcieng_t i_pbcieng, pfnMalloc_t i_pfnMalloc)
{
	unsigned uErr = 0;
	CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
	peng->SetAllocator(i_pfnMalloc);
	return uErr;
}

//------------------------------------------------------------------------------
unsigned SetCallback(pbcieng_t i_pbcieng, pfnCallback_t i_pfnCallback, unsigned i_uFlags)
{
	unsigned uErr = 0;
	CBCIEngProbeInterface* peng = (CBCIEngProbeInterface*)i_pbcieng;
	peng->SetCallback(i_pfnCallback, i_uFlags);
	return uErr;
}

//------------------------------------------------------------------------------
unsigned CreateBCIEngine(pbcieng_t* o_eng)
{
	unsigned uErr = 0;
	*o_eng = (pbcieng_t)new CBCIEngProbeInterface;
	return uErr;
}

//------------------------------------------------------------------------------
unsigned DestroyBCIEngine(pbcieng_t i_eng)
{
	delete (CBCIEngProbeInterface*)i_eng;
	return 0;
}
//= End of BCIEngProbeInterface.cpp ========================================================
