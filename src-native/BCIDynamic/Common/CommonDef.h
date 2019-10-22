 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: CommonDef.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// CommonDef.h
//
// Some common definitions. Among other things, this file serves
// as a portability header. It should be included first in all sources 
// files (and probably most of the headers!). In particular, it is
// crucial that you include it before using any platform-specific
// conditionals, such as _WINDOWS_ or _UNIX_.
//
// For this header to work, you need to define one of these ARCH symbols:
//
//	WIN32 (means x86)
//  IPF_ARCH (means Itanium Processor Family)
//  EM64T_ARCH (means Intel/AMD 64-bit processors)
//	IA32_ARCH (means x86)
//	SPARC_ARCH
//	HPPA_ARCH
//	ALPHA_ARCH
//	PPC_ARCH
//  LINUX_PPC_ARCH  (LINUX PPC64)
//	OS390_ARCH (means zSeries)
//	AS400_ARCH (means iSeries)
//
//==============================================================================

#ifndef _COMMONDEF_H
#define _COMMONDEF_H

#ifdef HPUX
#	define NO_NAMESPACES
#endif

/************************************************************************
 *
 * Note about __OS400__ and pragma convert(819):
 *
 * We want every literal string in the BCIEngProbe sources to be ASCII.
 * The reason is that there are far more interactions with strings and
 * chars that come from class files (which are UTF8) than there are
 * interactions with the operating system and libraries (like printf),
 * which want EBCDIC strings.
 *
 * To achieve this on iSeries / AS400 / OS400, we have to use pragma
 * #convert(819) at the top of every single source file. Can't put it in
 * #an #include file; the compiler knows the difference.
 *
 * Then, when you really do want an EBCDIC string literal (e.g. as the
 * format string argument to sprintf), you can precede it with #pragma
 * convert(0) to engage EBCDIC mode, then follow it with #pragma
 * convert(819) to continue.
 *
 * Note: #pragma convert(x) calls nest, so if you have two
 * convert(819)s, you'll need two convert(0) to get back to EBCDIC. 
 */

///////
// Required platform-specific includes 
///////
#if defined(WIN32) || defined(WIN64)
    // This brings _WINDOWS_ into scope. Use _WINDOWS_ to wrap Windows code.
#   include <windows.h>           /* for lots o' stuff, but esp. _WINDOWS_ */
#   include <malloc.h>            /* for alloca */
#elif defined(IPF_ARCH) || (EM64T_ARCH) || (LINUX_PPC_ARCH)
#   include <stdint.h>         /* for int64_t and uint64_t */
#endif

/* Only on OS400, we need this to get malloc/free */
#ifdef OS400
#include <stdlib.h>
#endif

//------------------------------------------------------------------------------
#define STR		char*
#define CSTR	const char*

typedef unsigned char BYTE, *PBYTE;
typedef unsigned short WORD, *PWORD;

#define SIZE_OF(x) sizeof(x)/sizeof(*x)

#define FOR_EACH(__iter) for(iterator __iter = begin(); __iter < end(); __iter++)

#if !defined(_WINDOWS_)
#	define _EXT_REF
#else
// Windows specific stuff
#	ifdef  _DLL_EXPORT
#		define _EXT_REF	__declspec(dllexport) 
#	elif defined _DLL_IMPORT
#		define _EXT_REF	__declspec(dllimport) 
#	elif  defined _LIB
#		define _EXT_REF
#	else
#		define _EXT_REF
#	endif
#endif // _WINDOWS_

// Define int64_t and uint64_t.  Note that int64_t is already defined in types.h for LinuxPPC64

#if defined(OS390_ARCH) && defined(LINUX)
	typedef signed long long int int64_t;
	typedef unsigned long long int uint64_t;
#else
#if defined(_WINDOWS_)
		typedef __int64 int64_t;
		typedef unsigned __int64 uint64_t;
#else
#       if !defined(IPF_ARCH) && !defined(EM64T_ARCH) && !defined(LINUX_PPC_ARCH) // Already being declared in stdint.h
		typedef long long int64_t;
		typedef unsigned long long uint64_t;
#       endif
#endif
#endif

//------------------------------------------------------------------------------
/////
// Windows-isms that pervade the code
/////
#if !defined(_WINDOWS_)
  typedef unsigned long       DWORD;
  typedef int                 BOOL;

#  ifndef CONST
#    define CONST               const
#  endif

#  ifndef FALSE
#    define FALSE               0
#  endif

#  ifndef TRUE
#    define TRUE                1
#  endif

#  ifndef LONG
#    define LONG                long
#  endif

  typedef char CHAR;
  typedef CHAR *PCHAR;
  typedef CHAR *LPCH, *PCH;

  typedef CONST CHAR *LPCCH, *PCCH;
  typedef CHAR *NPSTR;
  typedef CHAR *LPSTR, *PSTR;
  typedef CONST CHAR *LPCSTR, *PCSTR;

  //
  // Neutral ANSI/UNICODE types and macros
  //
#  ifdef  UNICODE                     // r_winnt

#    ifndef _TCHAR_DEFINED
       typedef WCHAR TCHAR, *PTCHAR;
       typedef WCHAR TBYTE , *PTBYTE ;
#      define _TCHAR_DEFINED
#    endif /* !_TCHAR_DEFINED */

    typedef LPWSTR LPTCH, PTCH;
    typedef LPWSTR PTSTR, LPTSTR;
    typedef LPCWSTR LPCTSTR;
    typedef LPWSTR LP;

#  else   /* not Windows, not UNICODE */               // r_winnt

#    ifndef _TCHAR_DEFINED
       typedef char TCHAR, *PTCHAR;
       typedef unsigned char TBYTE , *PTBYTE ;
#      define _TCHAR_DEFINED
#    endif /* !_TCHAR_DEFINED */

     typedef LPSTR LPTCH, PTCH;
     typedef LPSTR PTSTR, LPTSTR;
     typedef LPCSTR LPCTSTR;
#  endif /* UNICODE */                // r_winnt

   typedef void * HMODULE;

#  define APIENTRY
#endif // not Windows

//------------------------------------------------------------------------------
///////
// Specify endian-ness for your platform.
///////
#if defined(WIN32) || defined(IA32_ARCH) || defined(IPF_ARCH) || defined(EM64T_ARCH) || defined(ALPHA_ARCH)
#   define BIG_ENDIAN_HW
#elif defined(SPARC_ARCH) || defined(HPPA_ARCH) || defined(PPC_ARCH) || defined(AS400_ARCH) || defined(OS390_ARCH) || defined(LINUX_PPC_ARCH)
#   define LITTLE_ENDIAN_HW
#else
#   error "Platform-specific configuration required"
#endif

/////
// Byte swapping inline code to deal with endianess.
/////
typedef union
{
	unsigned char uc[4];
	unsigned int  ui;
} uc4_t;
// SwapBytes
// Convert buffer to little/big endian format
inline static void SwapBytes(void* i_pBuffer, long i_Size)
{
	uc4_t u;
	long c = 0;	// counter 

	unsigned char* ptr;

	while(c < i_Size - 2)
	{
		ptr = (BYTE*)i_pBuffer + c;
		u.ui = *(unsigned*)(ptr);

		for(int i = 0; i < 4; i++)
		{
			ptr[i] = u.uc[3-i];
		}
		c +=4;
	}
	if(i_Size - c >= 2)
	{
		ptr = (BYTE*)i_pBuffer + c;

		u.ui = *(unsigned short*)ptr;
		ptr[0] = u.uc[1];
		ptr[1] = u.uc[0];
	}
}

inline unsigned short LE_WORD(unsigned short i_word)
{
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_word, sizeof(i_word));
#endif
	return i_word;
}

inline unsigned LE_DWORD(unsigned i_dword)
{
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_dword, sizeof(i_dword));
#endif
	return i_dword;
}

inline int64_t LE_QWORD(int64_t i_qword)
{
#ifdef BIG_ENDIAN_HW
	SwapBytes((void*)&i_qword, sizeof(i_qword));
#endif
	return i_qword;
}

//------------------------------------------------------------------------------
/* On UNIX, use macros instead of calling memcpy as this is faster. 
 * On Windows, use regular memcpy() as this inlined.
 */
#ifdef _WINDOWS_
#  define MEMCPY2(x, y) memcpy(x, y, 2) 
#  define MEMCPY4(x, y) memcpy(x, y, 4) 
#elif defined(_UNIX_)  
#  define MEMCPY2(x, y) \
        { \
	    char *_tx = (char *)(x); \
	    char *_ty = (char *)(y); \
	    (*_tx) = (*_ty); \
	    *(_tx + 1) = *(_ty + 1); \
	}

#  define MEMCPY4(x, y) \
        { \
	    char *_tx = (char *)(x); \
	    char *_ty = (char *)(y); \
	    (*_tx) = (*_ty); \
	    *(_tx + 1) = *(_ty + 1); \
	    *(_tx + 2) = *(_ty + 2); \
	    *(_tx + 3) = *(_ty + 3); \
	}
#else
#  error "Platform-specific configuration required"
   // Pick the method most effective for your platform.
#endif 

/////
// Does your platform have stricmp or strcasecmp?
/////
#include <string.h>
#if defined(__OS400__)
	// Don't include anything. stricmp is open-coded in BCIEngProbeInterface.cpp
	// The reason: if I include <strings.h> on torascgm, it doesn't have strcasecmp.
	// On torascgm I have to include /usr/include/strings.h. (The one you get
	// from <strings.h> is /QIBM/include I think).
	// On toras3rm, /usr/include/strings.h won't compile; it #includes
	// standards.h which is a symlink to a missing file. And on that machine,
	// <strings.h> doesn't define strcasecmp unless you have three
	// other defines: __EXTENDED__, __POSIX_LOCALE__, and __OS400__TGTVRM__.
#elif defined(AIX) || defined(OS390)
	// zOS and AIX have strcasecmp in strings.h
	#include <strings.h>
#endif

#ifdef _UNIX_
#  define stricmp strcasecmp
#elif defined(_WINDOWS_)
   // stricmp comes from string.h
#else
#  error "Platform-specific configuration required"
#endif

/////
// How do you get the last error num?
/////
#ifdef _WINDOWS_
#  define LAST_ERROR GetLastError()
#elif defined(_UNIX_)
#  include <errno.h>
#  define LAST_ERROR errno
#else
#  error "Platform-specific configuration required"
#endif

/////
// Does your compiler support namespaces? Most should by now, so the
// default is to assume namespace support. If your compiler does not,
// define NO_NAMESPACES on the compiler command line. Usage:
//      USE_NAMESPACE(std);
/////
#ifdef NO_NAMESPACES
#  define USE_NAMESPACE(x)
#else
#  define USE_NAMESPACE(x) using namespace x
#endif // NO_NAMESPACES

/*
 *  OS/400 code page conversion functions
 */
#ifdef __OS400__

#include <iconv.h>

/*
 * @param - buffer - the buffer where the converted string can be written. If no
 *       buffer is passed in, then one is created. 
 */ 

char* as400_convert_tobuffer(char* from_str, char* from_code, char* to_code, 
							 char *buffer) {
	iconv_t cd;
	char* to_str;
	char* to_str_base;
	size_t from_len;
	size_t to_len;

	from_len = strlen(from_str);
	if(from_len > 16773104) /* max for AS/400 */
		return NULL;

	to_len = from_len + 1;
	if (buffer) {
		to_str = buffer; 
	}
	else {
		to_str = (char*)malloc(to_len);
	}
	to_str_base = to_str;
	memset(to_str, 0, to_len);

	cd = iconv_open(to_code, from_code);
	iconv(cd, &from_str, &from_len, &to_str, &to_len);
	iconv_close(cd);

	return to_str_base;
}

char* as400_convert(char* from_str, char* from_code, char* to_code) {
	return as400_convert_tobuffer(from_str,from_code,to_code,0); 
}

/* Turn of conversion so the format strings ("IBM...") are EBCDIC */
#pragma convert(0)

/* Overwrites the string in place */
static void __atoe(char* from_str) {
	char* p = as400_convert_tobuffer(from_str, "IBMCCSID0081900000000000000000000", "IBMCCSID0000000000000000000000000", 0); /* ASCII to EBCDIC */
	strcpy(from_str, p);
	free(p);
}

/* Overwrites the string in place */
static void __etoa(char* from_str) {
	char* p = as400_convert_tobuffer(from_str, "IBMCCSID0000000000000000000000000", "IBMCCSID0081900000000000000000000", 0); /* EBCDIC to ASCII */
	strcpy(from_str, p);
	free(p);
}

/* Resume conversion */
#pragma convert(819)

#endif /* __OS400__ */

#endif // defined _COMMONDEF_H

//==============================================================================
