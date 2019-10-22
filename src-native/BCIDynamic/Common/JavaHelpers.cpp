 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JavaHelpers.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// Java helpers
//------------------------------------------------------------------------------
// JavaHelpers.cpp
//==============================================================================
#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "JavaHelpers.h"

//==============================================================================
// CJavaFlags - java flags
//

//------------------------------------------------------------------------------
// External notation strings for Java flags
//
CSTR CJavaFlags::AccFlagStr[] = 
{
	"public",			// 0x0001
	"private",			// 0x0002
	"protected",		// 0x0004
	"static",			// 0x0008
	"final",			// 0x0010
	"synchronized",		// 0x0020	("super" for interfaces)
	"volotile",			// 0x0040
	"transient",		// 0x0080
	"native",			// 0x0100
	"interface",		// 0x0200
	"abstract",			// 0x0400
};

const int CJavaFlags::MAX_JAVA_FLAGS = SIZE_OF(AccFlagStr);


//------------------------------------------------------------------------------
// ToString
// Convert flags to a string 
//
// In:
//	isClass - Should be true if the flags defined on a class
// Out:
//	-
// Returns: string
//
string
CJavaFlags::ToString(bool isClass) const
{
	unsigned mask = 1;
	string strRet = "";
	for(int i=0; i < MAX_JAVA_FLAGS; i++)
	{
		if(m_u2Flags & mask)
		{
			if(isClass && i == 5)	// thank you, Sun!!!
			{
				strRet += "super";
			}
			else
			{
				strRet += AccFlagStr[i];
			}
			strRet += " ";
		}
		mask <<= 1;
	}
	return strRet;
}

//==============================================================================
// CJavaType
//
static	const char Sig[] = "BCDFIJLSZV?";

//------------------------------------------------------------------------------
// Constructor
//
// In:
//	i_jtype - type enumerator
//	i_nDim  - number of dimentions or 0 (default) if not an array
//	i_szClass - Class name (NULL for the primitives)
//
CJavaType::CJavaType(jtype_t i_jtype, int i_nDim, CSTR i_szClass)
{
	m_jtype = i_jtype;
	if(NULL != i_szClass)
		m_strClass = i_szClass;
	m_nDim = i_nDim;
}

//------------------------------------------------------------------------------
// Parse
// Parse a string representing Java type in the internal notation
//
// In:
//	i_szTypeSymbol - type signature in the internal notation.
void	
CJavaType::Parse(CSTR i_szTypeSymbol)
{
	m_nDim = 0;						// Assume not an array
	m_jtype = CJavaType::J_UNKNOWN;	// Assume the worst
	m_strClass = "";				// Assume primitive
	char* ch = (char*)i_szTypeSymbol;
	while(*ch == '[')
	{	// Count the array dimensions
		m_nDim++;
		ch++;
	}
	for(int n = 0; n < SIZE_OF(Sig); n++)
	{	// Find the type
		if(*ch == Sig[n])
		{
			m_jtype = (jtype_t)n;
			break;
		}
	}
	if(m_jtype == CJavaType::J_CLASS)
	{	// If this is a class
		ch++;	// Skip the 'L'
		while(*ch != ';' && *ch != 0)
		{	// Remember the name
			m_strClass += *ch++;
		}
	}
}

//------------------------------------------------------------------------------
// ToString
// Convert into the external notation
//
string
CJavaType::ToString() const
{
	string strRet;
	static const char* szTypes[] = 
	{
		"byte",
		"char",
		"double",
		"float",
		"int",
		"long",
		"",
		"short",
		"boolean",
		"void",
		"[]",
		"???"
	};

	CJavaType::jtype_t jtype = m_jtype;
	if(jtype >= CJavaType::J_LAST)
	{
		jtype = CJavaType::J_UNKNOWN;
	}
	if(IsClass())
	{
		strRet = GetClassName();
	}
	else
	{
		strRet = szTypes[jtype];
	}
	for(int i = 0; i < GetDim(); i++)
	{
		strRet += szTypes[CJavaType::J_ARRAY];
	}
	return strRet.c_str();
}

//------------------------------------------------------------------------------
// GetTypeString()
// Recreates type signature from the type information and returns it as string
//
string  
CJavaType::GetTypeString() const
{
	string strRet;
	for(int i = 0; i < GetDim(); i++)
	{
		strRet += "[";
	}
	if(IsClass())
	{
		strRet += 'L';
		strRet += GetClassName();
		strRet += ';';
	}
	else
	{
		strRet += Sig[m_jtype];
	}
	return strRet;
}

//------------------------------------------------------------------------------
// IsPrimitive()
// Returns true if type is primitive.
// Note that type is primitive unless it is a class or array
//
bool
CJavaType::IsPrimitive() const
{
	bool bRet = true;
	if(IsClass() || GetDim() != 0)
		bRet = false;
	return bRet;
}

//------------------------------------------------------------------------------
// GetCategory
// Get type category
//
// Returns:
//	1 - category 1	
//	2 - category 2
//	0 - unknown or void
//
int		
CJavaType::GetCategory() const
{
	switch(m_jtype)
	{
	case CJavaType::J_VOID:
	case CJavaType::J_UNKNOWN:
		return 0;
	case CJavaType::J_DOUBLE:
	case CJavaType::J_LONG:
		return 2;
	default:
		return 1;
	}
}

//------------------------------------------------------------------------------
// GetStackSize
// Calculate stack size necessary for passing an argument of this type
//
int		
CJavaType::GetStackSize() const
{
	int nStack;
	if(m_nDim == 0)
		nStack = GetCategory();
	else
		nStack = 1;
	return nStack;
}

//------------------------------------------------------------------------------
// Assignment operator
//
CJavaType&
CJavaType::operator = (const CJavaType& i_type)
{
	m_nDim = i_type.m_nDim;
	m_strClass = i_type.m_strClass;
	m_jtype = i_type.m_jtype;
	return *this;
}

//==============================================================================
// CJavaMethodName
//

//------------------------------------------------------------------------------
// Constructor
// ToDo: Recognize input string format and do the appropriate parsing
// for internal and external notations.
// e.g. "main(Ljava/lang/String;)V" versus "void main(java.lang.String)"
//
CJavaMethodName::CJavaMethodName(CSTR i_szNameAndSig, bool i_bAutoParse)
{
	m_bAutoParse = i_bAutoParse;
	if(NULL != i_szNameAndSig)
	{
		char* szScan = (char*)i_szNameAndSig;
		while(*szScan != '\0' && *szScan != '(')
		{
			m_strName += *szScan++;
		}
		m_strSignature = szScan;
	}
	if(m_bAutoParse)
	{
		Parse();
	}
}

//------------------------------------------------------------------------------
// SetName
//
void 
CJavaMethodName::SetName(CSTR i_szName)
{
	m_strName = i_szName;
}

//------------------------------------------------------------------------------
// SetSignature
//
// Calling this method will cause parsing if i_bAutoparse in 
// the costructor was true
//
void 
CJavaMethodName::SetSignature(CSTR i_szSignature)
{
	m_strSignature = i_szSignature;
	if(m_bAutoParse)
	{
		Parse();
	}
}

//------------------------------------------------------------------------------
// GetName
//
CSTR 
CJavaMethodName::GetName() const
{
	return m_strName.c_str();
}

//------------------------------------------------------------------------------
// GetSignature
//
CSTR 
CJavaMethodName::GetSignature() const
{
	return m_strSignature.c_str();
}

//------------------------------------------------------------------------------
// GetReturnType
//
const CJavaType&  
CJavaMethodName::GetRetType() const
{
	return m_jtRet;
}

//------------------------------------------------------------------------------
// GetArgCount
//
int	
CJavaMethodName::GetArgCount() const
{
	return m_args.size();
}

//------------------------------------------------------------------------------
// GetStackSize
// Calculate stack size contribution for the method based on its signature.
// The stack depth is calculated as the stack required for the return type
// minus stack for all arguments.
//
int 
CJavaMethodName::GetStackSize() const
{
	int nRet = m_jtRet.GetStackSize();
	CJavaMethodName::args_t::const_iterator itr;
	for(itr = m_args.begin(); itr != m_args.end(); itr++)
	{
		nRet -= itr->GetStackSize();
	}

	return nRet;
}

//------------------------------------------------------------------------------
// Parse()
// Parse the stored method name into the array of argument types and the
// return type.
//
// TODO: right now there is a precondition associated with this routine.
// It assumes the method name is well formed and correct (that is usually 
// true if the name was extracted from a compiled Java class).
// However we need a more universal parsing routine that verifies 
// the correctnes of the name and signature and reports an error (if any)
//
void 
CJavaMethodName::Parse()
{
	bool isArg   = false;
	bool isArray = false;
	string strToken;

	string::iterator itr = m_strSignature.begin();
	CJavaType jtype;

	while(itr != m_strSignature.end())
	{
		switch(*itr)
		{
		case ' ':
		case '\t':
			break;
		case '(':
			isArg = true;
			itr++;
			continue;
		case ')':
			isArg = false;
			itr++;
			continue;
		case 'L':
			while(*itr != ';')
				strToken += *itr++;
			break;
		case '[':
			while(*itr == '[')
				strToken += *itr++;
			continue;
		default:
			strToken += *itr;
			break;
		}
		itr++;
		jtype.Parse(strToken.c_str());
		strToken = "";
		if(isArg)
		{
			m_args.push_back(jtype);
		}
		else
		{
			m_jtRet = jtype;
		}
	}
}

//------------------------------------------------------------------------------
// ToString()
// Builds a string from the stored type information.
// The resulting string will contain a name and signature in the 'external'
// Java format. E.g. int MyMethod(int, Java.lang.String[])
//
string
CJavaMethodName::ToString() const
{
	string strRet = GetRetType().ToString();
	strRet += ' ';
	strRet += m_strName;
	strRet += '(';
	int n = GetArgCount();
	CJavaMethodName::args_t::const_iterator itr;
	for(itr = m_args.begin(); itr != m_args.end(); itr++, n--)
	{
		string strTemp;
		strRet += itr->ToString();
		if(n > 1)
		{
			strRet += ", ";
		}
	}
	strRet += ')';
	return strRet;
}

//= End of JavaHelpers.cpp =====================================================

