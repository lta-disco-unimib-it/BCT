 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngProbeInterface.cpp,v 1.1.2.6 2008-03-31 13:01:12 pastore Exp $ 
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
	CProbe* pprobec = NULL;
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
	CProbeFragment::fragmentType_t cfragType;
	size_t  cbScan = 0;
	char*	szScan = (char*)i_pchOptions;
	int n = 0;
	int probes=-1;
	char* cMethod;
	char* cSignature;
	char* cArgs;
	bool bctProbe = false;
	
	//char* methodProbesLog[2] = { "bctLP0$Probe_1","bctLP1$Probe_1" };
	//char* callProbesLog[2] = { "bctLP0$Probe_0","bctLP1$Probe_0" };
	//char* methodProbesCheck[2] = { "bctCP0$Probe_1","bctCP1$Probe_1" };
	//char* callProbesCheck[2] = { "bctCP0$Probe_0","bctCP1$Probe_0" };
	
	CFilterRuleList* commonRules = new CFilterRuleList();
	CFilterRuleList* commonRulesWithin = new CFilterRuleList();
	
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
			probes++;
			// Start of new probe.
			// Flush out the old one being read, if any
			if(NULL != pprobe)
			{
				probelist.push_back(pprobe);
				if ( bctProbe ){
					probelist.push_back(pprobec);
				}
				
				pprobe->SetPrefilter(pprefilter);
				pprobe->SetPrefilterWithin(pprefilterWithin);
				
				if ( bctProbe ){
					pprobec->SetPrefilter(new CFilterRuleList());
					pprobec->SetPrefilterWithin(new CFilterRuleList());
					this->ruleWithin( pprobec, pprobe );
				}
				
			
				m_peng.AddProbe(pprobe);
				if ( bctProbe ){
					m_peng.AddProbe(pprobec);
				}
				
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
			pprobec = m_peng.CreateProbe();
			
			//by default exclude all calls to bctProbes
			pprobe->AddFilterRule("*", "bct*", "*", "*", CFilterRule::ACTION_EXCLUDE);
			pprobec->AddFilterRule("*", "bct*", "*", "*", CFilterRule::ACTION_EXCLUDE);
			pprobec->AddFilterRuleWithin("*", "bct*", "*", "*", CFilterRule::ACTION_EXCLUDE);
			
	
			
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
			if (STRICMP(szRecType, "FILTER") == 0)
				// This is the probe targeting rule
			{	// Add rule to the current probe
				CFilterRule fr(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
				commonRules->push_back( fr ); 
			}
			else if (STRICMP(szRecType, "FILTERWITHIN") == 0)
			{
				CFilterRule fr(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
				commonRulesWithin->push_back( fr );
				//pprobe->AddFilterRuleWithin(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
				//pprobec->AddFilterRuleWithin(szProbePackage, szProbeClass, szProbeMethod, szProbeSignature, action);
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
				
				if (STRICMP(szRecType, "RULE") == 0)
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

			if(STRICMP(szRefType, "ONENTRY") == 0){
				fragType = CProbeFragment::PROBE_ONENTRY;
				cfragType = CProbeFragment::PROBE_BEFORECALL;
				cMethod = "_beforeCall";
				//cSignature = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V";
				cSignature = szRefSignature;
				//cArgs = "className,methodName,methodSig,args";
				cArgs = szRefArgs;
			}
			else if(STRICMP(szRefType, "ONEXIT") == 0){
				fragType = CProbeFragment::PROBE_ONEXIT;
				cfragType = CProbeFragment::PROBE_AFTERCALL;
				cMethod = "_afterCall";
				cSignature = szRefSignature;
				//cSignature = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V";
				//cArgs = "returnedObject,className,methodName,methodSig,args";
				cArgs = szRefArgs; 
			}
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
			
			string probeName( szRefClass );
			//cout << szRefClass << endl;
			if (  probeName.find( bctProbePrefix ) != string::npos ){
				//cout << "Logging" << endl;
				pprobe->AddFragment(fragType, getMethodProbe(probeName,probes), szRefMethod, szRefSignature, "static", szRefArgs);
				pprobec->AddFragment( cfragType, getCallProbe(probeName,probes), cMethod, cSignature, "static", cArgs);
				bctProbe = true;
			} else {
				bctProbe = false;
				pprobe->AddFragment(fragType, probeName.c_str(), szRefMethod, szRefSignature, "static", szRefArgs);
				//pprobec->AddFragment( cfragType, probeName.c_str(), cMethod, cSignature, "static", cArgs);
			}
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
		cout << "BCT?" << bctProbe << endl;
		probelist.push_back(pprobe);
		if ( bctProbe ){
			probelist.push_back(pprobec);
		}
		
		
		pprobe->SetPrefilter(pprefilter);
		pprobe->SetPrefilterWithin(pprefilterWithin);
		if ( bctProbe ){
			pprobec->SetPrefilter(new CFilterRuleList());
			pprobec->SetPrefilterWithin(new CFilterRuleList());
			this->ruleWithin( pprobec, pprobe );
		}
		
		
		m_peng.AddProbe(pprobe);
		
		if ( bctProbe ) {
			m_peng.AddProbe(pprobec);
		}
		
		
		pprobe = NULL;
		pprobec = NULL;
		  	
	}
	
	list<CProbe*> probelistC;
	list<CProbe*> probelistM;
	CFilterRuleList filterlistE;
	
	for(list<CProbe*>::iterator itr = probelist.begin(); itr != probelist.end(); itr++)
	{
		//Add common rules
		for ( list<CFilterRule>::iterator itc = commonRules->begin(); itc != commonRules->end(); itc++)
			(*itr)->AddFilterRule((*itc));
	
		//Add common rules
		for ( list<CFilterRule>::iterator itc = commonRulesWithin->begin(); itc != commonRulesWithin->end(); itc++)
			(*itr)->AddFilterRuleWithin((*itc));
		
		if ( (*itr)->IsCallsiteProbe() ){
			probelistC.push_back( (*itr) );
		}	else {
			probelistM.push_back( (*itr) );
		}
		
	}
	
	
	
	
	list<CProbe*>::iterator itrCall;
	list<CProbe*>::iterator itrM;
	
	//Extract the include rules that cannot be set to include in outgoing calls
	for(itrM = probelistM.begin(); itrM != probelistM.end(); itrM++)
	{		
			CFilterRuleList lst = (*itrM)->GetPrefilterRuleList();
			list<CFilterRule>::iterator iter;
			
			for (iter=lst.begin(); iter != lst.end(); iter++)
  			{
  				if ( (*iter).m_action == CFilterRule::ACTION_INCLUDE )
  					filterlistE.push_back(*iter);		
  			}
	}
	
	//callsite probes must not monitor calls to already monitored methods (method side) 
	for(itrCall = probelistC.begin(); itrCall != probelistC.end(); itrCall++)
	{
		for(itrM = probelistM.begin(); itrM != probelistM.end(); itrM++)
		{		
				this->ruleToCall( (*itrCall), (*itrM), filterlistE );
		}
	}
	
	cerr << "INSTRUMENTATION RULES:" << endl;
	for(list<CProbe*>::iterator itr = probelist.begin(); itr != probelist.end(); itr++)
	{
		printProbe(*itr);
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

const char* CBCIEngProbeInterface::getCallProbe( const string& probeName, int componentNumber ){
	return getProbe(probeName,componentNumber,"$Probe_0");
}

const char* CBCIEngProbeInterface::getMethodProbe( const string& probeName, int componentNumber ){
	return getProbe(probeName,componentNumber,"$Probe_1");
}


const char* CBCIEngProbeInterface::getProbe( const string& probeName, const int componentNumber, const char* postfix ){
	string* probe = new string ( probeName.substr(0,probeName.find("_")));
	cout << "Prefix " << *probe << endl;
	//string* probe = new string(prefix);
	stringstream ss;
	ss << componentNumber;
	probe->append(ss.str());
	probe->append(postfix);
	return probe->c_str();
}

void CBCIEngProbeInterface::printProbe( CProbe* pprobe ){
	char* action;
	cerr << "PROBE" << endl;
	char* callsite;
	if ( pprobe->IsCallsiteProbe() )
		cerr << "CALLSITE" << endl;
	else
		cerr << "METHODSITE" << endl;
		
	CFilterRuleList lst = pprobe->GetFilterRuleList();
	list<CFilterRule>::iterator iter;
	for (iter=lst.begin(); iter != lst.end(); iter++)
  	{
  		if ( (*iter).m_action == CFilterRule::ACTION_EXCLUDE )
  			action = "EXCLUDE";
  		else
  			action = "INCLUDE";
  		cerr << "RULE" << " ";
  		cerr << (*iter).m_strPackageName << " "<< (*iter).m_strClassName << " "<< (*iter).m_strMethodName << " " << (*iter).m_strMethodSig << " "<< action << endl;
  	}
  	
  	lst = pprobe->GetFilterRuleWithinList();
	
	for (iter=lst.begin(); iter != lst.end(); iter++)
  	{
  		if ( (*iter).m_action == CFilterRule::ACTION_EXCLUDE )
  			action = "EXCLUDE";
  		else
  			action = "INCLUDE";
  		cerr << "RULEWITHIN" << " ";
  		cerr << (*iter).m_strPackageName << " "<< (*iter).m_strClassName << " "<< (*iter).m_strMethodName << " "<< (*iter).m_strMethodSig << " " << action << endl; 
  	}
  	
  	lst = pprobe->GetPrefilterRuleList();
	for (iter=lst.begin(); iter != lst.end(); iter++)
  	{
  		if ( (*iter).m_action == CFilterRule::ACTION_EXCLUDE )
  			action = "EXCLUDE";
  		else
  			action = "INCLUDE";
  		cerr << "FILTER" << " ";
  		cerr << (*iter).m_strPackageName << " "<< (*iter).m_strClassName << " "<< (*iter).m_strMethodName << " "<< (*iter).m_strMethodSig << " " << action << endl;
  	}	
  	
  	 
	
  	CFilterRuleList lstw = pprobe->GetPrefilterRuleWithinList();
  	list<CFilterRule>::iterator iterw;
	for (iterw=lstw.begin(); iterw != lstw.end(); iterw++)
  	{
  		if ( (*iterw).m_action == CFilterRule::ACTION_EXCLUDE )
  			action = "EXCLUDE";
  		else
  			action = "INCLUDE";
  		cerr << "FILTERWITHIN" << " ";
  		cerr << (*iterw).m_strPackageName << " "<< (*iterw).m_strClassName << " "<< (*iterw).m_strMethodName << " "<< (*iterw).m_strMethodSig << " " << action << endl;
  	}
  	
}

void CBCIEngProbeInterface::ruleWithin( CProbe* pprobeCall, CProbe* pprobeExec )
{
	//cout << "BEFORE W" << endl;
	//cout << "CALL" << endl;
	//printProbe(pprobeCall);
	//cout << "EXEC" << endl;
	//printProbe(pprobeExec);
	
	//CProbeFragment::fragmentType_t fragType;
	//if ( fragType == 	CProbeFragment::PROBE_AFTERCALL || fragType == CProbeFragment::PROBE_BEFORECALL ){
			CFilterRuleList lst = pprobeExec->GetPrefilterRuleList();
			list<CFilterRule>::iterator iter;
			
			for (iter=lst.begin(); iter != lst.end(); iter++)
  			{
  				
  				 pprobeCall->AddPrefilterRuleWithin((*iter).m_strPackageName,
  				 								 (*iter).m_strClassName, 
  				 								 (*iter).m_strMethodName, 
  				 								 (*iter).m_strMethodSig,
  				 								 (*iter).m_action);
  				//cout << "added " << (*iter).m_strPackageName << " " << (*iter).m_strClassName << eol;  								 
  			}
		
			pprobeCall->AddPrefilterRuleWithin("*",
  				 								 "*", 
  				 								 "*",
  				 								 "*", 
  				 								 CFilterRule::ACTION_EXCLUDE );
		//}
		//cout << "AFTER W" << endl;
		//printProbe(pprobeCall);
}

/**
 * This method add to a CallSite probe filtering rules by negation of method site probe. It also do not add include rules
 * taht are inside the exclude list.
 * 
 * 
 * 
 */ 
void CBCIEngProbeInterface::ruleToCall( CProbe* pprobeCall, CProbe* pprobeExec, CFilterRuleList& exclude )
{
	
	
	//cout << "BEFORE ADDFILTER" << endl;
	//printProbe(pprobeCall);
	//printProbe(pprobeExec);
	
	//CProbeFragment::fragmentType_t fragType;
	//if ( fragType == 	CProbeFragment::PROBE_AFTERCALL || fragType == CProbeFragment::PROBE_BEFORECALL ){
			CFilterRuleList lst = pprobeExec->GetPrefilterRuleList();
			list<CFilterRule>::iterator iter;
			
			for (iter=lst.begin(); iter != lst.end(); iter++)
  			{
  				bool accept = true;
  				CFilterRule::action_t action;
  				
  				if ( (*iter).m_action == CFilterRule::ACTION_EXCLUDE ){
  					
  					action = CFilterRule::ACTION_INCLUDE;
  					
  					//an action can be included only if it is not excluded by other
					list<CFilterRule>::iterator iterE;
					//cerr << "CHECK " << endl;
					//cerr << (*iter).m_strPackageName << endl;			
					for (iterE=exclude.begin(); iterE != exclude.end(); iterE++)
  					{  						
  						//cerr << (*iterE).m_strPackageName << endl;
  						if ( (*iterE).m_action == CFilterRule::ACTION_INCLUDE && 
  				 			STRICMP((*iter).m_strPackageName, (*iterE).m_strPackageName )  == 0 &&
  				 			STRICMP((*iter).m_strClassName, (*iterE).m_strClassName )  == 0 && 
  				 			STRICMP((*iter).m_strMethodName, (*iterE).m_strMethodName ) == 0  )
								accept = false;  				 			
  					}  					
  				
  				
  				}
  				else
  					action = CFilterRule::ACTION_EXCLUDE;

  				 if ( accept && 
  				 		( ! ( action == CFilterRule::ACTION_INCLUDE && 
  				 			STRICMP((*iter).m_strPackageName, "*")  == 0 &&
  				 			STRICMP((*iter).m_strClassName, "*")  == 0 && 
  				 			STRICMP((*iter).m_strMethodName, "*") == 0 ) ) 
  				 	) {
  				 	
  				 	pprobeCall->AddPrefilterRule((*iter).m_strPackageName,
  				 								 (*iter).m_strClassName, 
  				 								 (*iter).m_strMethodName, 
  				 								 (*iter).m_strMethodSig,
  				 								 action);
  				 }
  			}
		//cout << "AFTER" << endl;
		//printProbe(pprobeCall);
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

const char* CBCIEngProbeInterface::bctProbePrefix = "bct";

//= End of BCIEngProbeInterface.cpp ========================================================
