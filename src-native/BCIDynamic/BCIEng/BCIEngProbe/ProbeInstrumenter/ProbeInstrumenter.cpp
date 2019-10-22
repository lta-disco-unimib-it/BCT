 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: ProbeInstrumenter.cpp,v 1.1.2.2 2008-02-18 17:07:09 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// ProbeInstrumenter.cpp
//
// This is the "main" that drives the BCIEngProbe libary to instrument
// class files on disk.
// 
// USAGE: ProbeInstrumenter engine_script class_file [ class_file ... ]
//
// Instruments the named class files. The output file name for each
// is the original name plus ".bci"
//

#ifdef WIN32
#pragma warning(disable:4786)
#endif

#include "CommonDef.h"
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include "BCIEngProbeInterface.h"

USE_NAMESPACE(std);

//------------------------------------------------------------------------------
class CBCIEngProbeInstrumenter
{
public:
	CBCIEngProbeInstrumenter();
	~CBCIEngProbeInstrumenter();
	void InstrumentClassFile(CSTR i_szName);
	void ParseProbeDescriptors(CSTR i_szName);
	unsigned int InterpretArgs(const char* szArgs);

private:
	CBCIEngInterface*	m_peng;
};


//------------------------------------------------------------------------------
CBCIEngProbeInstrumenter::CBCIEngProbeInstrumenter()
{
	CreateBCIEngine((void**)&m_peng);
	m_peng->SetAllocator(malloc);
}

//------------------------------------------------------------------------------
CBCIEngProbeInstrumenter::~CBCIEngProbeInstrumenter()
{
	DestroyBCIEngine(m_peng);
}

enum {
	LOG_LEVEL_FINEST = 1,
	LOG_LEVEL_FINER,
	LOG_LEVEL_FINE,
	LOG_LEVEL_CONFIG,
	LOG_LEVEL_INFO,
	LOG_LEVEL_WARNING,
	LOG_LEVEL_SEVERE,
	LOG_LEVEL_NONE
};

const char* logLevelStrings[] = {
	"(zero)",
	"finest",
	"finer",
	"fine",
	"config",
	"info",
	"warning",
	"severe",
	"none",
	NULL
};

static int logLevel = LOG_LEVEL_SEVERE;

//------------------------------------------------------------------------------
int 
callback(CSTR i_clName, size_t len, unsigned flags)
{
	switch (flags) {
		case BCIENGINTERFACE_CALLBACK_MODULE:
			// Query: do you want to permit instrumentation of this module? (false will veto)
			return true;
		case BCIENGINTERFACE_CALLBACK_METHOD:
			// Query: false will veto instr of this method (false will veto)
			return true;
		case BCIENGINTERFACE_CALLBACK_MODULE_INSTR:
			// Notice: I instrumented this module, really made changes in it
			if (logLevel <= LOG_LEVEL_FINE) {
				fprintf(stderr, "[Probekit: class %s has been instrumented]\n", i_clName);
			}
			break;
		case BCIENGINTERFACE_CALLBACK_METHOD_INSTR:
			// Don't log individual methods that are instrumented
			break;
		default:
			// We don't know of any other messages
			break;
	}
	return true;
}

//------------------------------------------------------------------------------
void 
CBCIEngProbeInstrumenter::ParseProbeDescriptors(CSTR i_szName)
{
	char* pchProbe = NULL;
	size_t cbProbe = 0;
	FILE* pfProbe = fopen(i_szName, "r");

	if (NULL == pfProbe)
		throw "Can't open probe descriptors file";
	fseek(pfProbe, 0, SEEK_END);
	cbProbe = ftell(pfProbe) + 2;
	pchProbe = (char*)malloc(cbProbe + 1);
	fseek(pfProbe, 0, SEEK_SET);
	cbProbe = fread(pchProbe, 1, cbProbe, pfProbe);
	pchProbe[cbProbe] = '\0';

	if (logLevel <= LOG_LEVEL_FINER) {
		// Dump the script file we got
		// Dump every line separately
		// Remember that lines might be null-separated.
		// (We made sure the whole thing is null terminated.)
		fprintf(stderr, "[Probekit: start of script file]\n");
		fprintf(stderr, "%s\n", pchProbe);
		fprintf(stderr, "[Probekit: end of script file]\n");
	}

	// Initialize the BCI engine with this script file
	m_peng->Initialize(pchProbe, cbProbe);

	// Request all callbacks
	m_peng->SetCallback(callback, -1);
	free(pchProbe);
	fclose(pfProbe);
}

//------------------------------------------------------------------------------
void 
CBCIEngProbeInstrumenter::InstrumentClassFile(CSTR i_szName)
{
	string strOutName = i_szName;
	size_t cbIn, cbOut;
	void *pIn, *pOut;

	if (logLevel <= LOG_LEVEL_FINEST) {
		fprintf(stderr, "[Probekit processing class file %s]\n", i_szName);
	}

	// Open and read class file
	FILE* pfIn = fopen(i_szName, "rb");
	if (NULL == pfIn)
		throw "Can't open class file";
	fseek(pfIn, 0, SEEK_END);
	cbIn = ftell(pfIn);
	pIn = malloc(cbIn);
	fseek(pfIn, 0, SEEK_SET);
	fread(pIn, 1, cbIn, pfIn);
	fclose(pfIn);

	// Instrument
	m_peng->Instrument(pIn, cbIn, &pOut, &cbOut);

	// Write the output. Notice that maybe pOut == pIn
	// if the instrumentation engine decided not to do anything.
	// TODO: flag this somehow to our caller, who might care.
	// TODO: let the caller send us a flag saying not to bother writing *.class.bci in this case.

	if (logLevel <= LOG_LEVEL_FINEST) {
		if (pOut == pIn) {
			fprintf(stderr, "[Probekit did not change this class file]\n");
		}
	}

	strOutName += ".bci";
	FILE* pfOut = fopen(strOutName.c_str(), "wb");
	fwrite(pOut, 1, cbOut, pfOut);
	fclose(pfOut);

	free(pIn);	
	if (pOut != pIn) {
		// Only free pOut if it's not the same as pIn (which we just freed).
		// It will be the same if the instrumenation engine decided not to do anything.
		free(pOut);
	}
}


//------------------------------------------------------------------------------
static void PrintHelp()
{
	fprintf(stderr, "Usage: ProbeInstrumenter [ opts ] engine-script-file class-file [ class-file ... ]\n");
	fprintf(stderr, "Options are:\n");
	fprintf(stderr, "\t-loglevel xxx\t(xxx = none, little, some, most, all)\n");
}

//------------------------------------------------------------------------------
//
// Subroutines to turn error codes in to messages.
//
// The getErrorMessage function returns a string which the caller must free!
//
const char* source_strings[] = {
		"(unknown)",
		"driver-to-engine interface",
		"class file manager",
		"Generic module manager",
		"Java module manager",
		"Generic insertion engine",
		"Java insertion engine",
		"Probekit insertion engine",
		NULL
	};

struct MessageCatalogEntry {
  int src;			// src == -1 means "end of list"
  int code;
  const char* msg;
};

MessageCatalogEntry message_catalog[] = {
	{ 1, 1, "unspecified error" }, 
	{ 1, 2, "bad arguments in probescript" }, 
	{ 1, 3, "bad filter type in probescript" }, 
	{ 2, 1, "invalid method called (internal error)" }, 
	{ 2, 2, "an internal error occurred" }, 
	{ 2, 3, "class file has a bad magic number" }, 
	{ 2, 4, "class file has a bad version" }, 
	{ 2, 5, "class file has a bad entry in the constant pool" }, 
	{ 2, 6, "class file has a bad field reference" }, 
	{ 2, 7, "class file has a bad interface reference" }, 
	{ 2, 8, "class file has a bad method reference" }, 
	{ 2, 9, "class file has a bad value for a constant" }, 
	{ 2, 10, "internal error regarding constant pool type management" }, 
	{ 2, 11, "index out of bounds" }, 
	{ 3, 1, "unknown error" }, 
	{ 3, 2, "internal error" }, 
	{ 3, 3, "invalid module" }, 
	{ 3, 4, "parse error" }, 
	{ 3, 5, "invalid method called (internal error)" }, 
	{ 3, 6, "ouput code size too large (code overrun)" }, 
	{ 5, 1, "unknown error" }, 
	{ 5, 2, "invalid opcode encountered" }, 
	{ 5, 3, "class/module is already instrumented" }, 
	{ 5, 4, "can not instrument this class/module" }, 
	{ 5, 5, "internal error" }, 
	{ 7, 1, "internal error" }, 
	{ -1, -1, NULL }, 
};

// IMPORTANT!
// The getErrorMessage function returns a string which the caller must free!

char* getErrorMessage(int code, int src) 
{
	if (src < 8) {
		const char* srcmsg = source_strings[src];
		for (int i = 0; message_catalog[i].src != -1; i++) {
			MessageCatalogEntry& mce = message_catalog[i];
			if (mce.src == src && mce.code == code) {
				const char* errmsg = mce.msg;
				const char *fmt = "source: %s, message: %s";
				char* returnmsg = (char*)malloc(strlen(fmt) + strlen(srcmsg) + strlen(mce.msg) + 1);
				sprintf(returnmsg, fmt, srcmsg, mce.msg);
				return returnmsg;
			}
		}
		
		// Known source, unknown error code
		const char* fmt = "source: %s, error code: %d";
		char* returnmsg = (char*)malloc(strlen(fmt) + strlen(srcmsg) + 15);
		sprintf(returnmsg, fmt, srcmsg, code);
		return returnmsg;
	}
	
	// Unknown source
	const char* fmt = "source location: %d, error code: %d";
	char *returnmsg = (char*)malloc(strlen(fmt) + 15 + 15);
	sprintf(returnmsg, fmt, src, code);
	return returnmsg;
}

//------------------------------------------------------------------------------
int main(int argc, char* argv[])
{
	int errorStatus = 0;

	int argScan = 1;

	if (argScan >= argc) {
		PrintHelp();
		return 2;
	}
	if (strcmp(argv[argScan], "-loglevel") == 0) {
		// parse the next arg as the log level
		++argScan;
		if (argScan >= argc) {
			PrintHelp();
			return 2;
		}
		int match;
		for (match = 0; logLevelStrings[match] != NULL; match++ ){
			if (stricmp(argv[argScan], logLevelStrings[match]) == 0) {
				logLevel = match;
				break;
			}
		}
		if (logLevelStrings[match] == NULL) {
			PrintHelp();
			return 2;
		}
		++argScan;
	}

	CBCIEngProbeInstrumenter eng;

	try {
		eng.ParseProbeDescriptors(argv[argScan]);
	}
	catch (...) {
		fprintf(stderr, "Problem creating instrumentation engine using engine script %s\n", argv[argScan]);
		return 1;
	}

	++argScan;

	// Operate on each input file.
	// A failure in one doesn't stop us operating on others.
	for (int i = argScan; i < argc; i++) {
		try
		{
			eng.InstrumentClassFile(argv[i]);
		}
		catch (CBCIEngProbeInterfaceException& ex)
		{
			unsigned src=ex.GetSource(); 
			unsigned code=ex.GetReason();
			int msgBufSize = 1024;
			char* msgBuf = (char*)malloc(msgBufSize);
			ex.FormatMessage(msgBuf, msgBufSize);
			char* errorMessage = getErrorMessage(code, src);	// we must free this string
			fprintf(stderr, "Probekit error while processing %s:\n%s\n%s\n", argv[i], errorMessage, msgBuf);
			free(errorMessage);
			free(msgBuf);
			errorStatus = 2;
		}
		catch (char* szMsg)
		{
			fprintf(stderr, "Probe test exception: %s\n", szMsg);
			errorStatus = 2;
		}
	}
	return errorStatus;
}

//= End of ProbeInstrumenter.cpp =======================================================
