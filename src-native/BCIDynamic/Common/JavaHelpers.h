 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JavaHelpers.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// Java helpers
//------------------------------------------------------------------------------
// JavaHelpers.h
//==============================================================================
#ifndef _JAVAHELPERS_H
#define _JAVAHELPERS_H

#include <string>
#include <vector>
#include "CommonDef.h"
#include "JavaDef.h"

USE_NAMESPACE(std);

//==============================================================================
// CJavaFlags
//
// Java Access Flags
// Handling and conversion to string
//
class CJavaFlags
{
public:
	CJavaFlags(u2 i_u2Flags)
	{
		m_u2Flags = i_u2Flags;
	}
	u2 Get() const {return m_u2Flags;}
	void Set() {m_u2Flags = m_u2Flags;}
	string ToString(bool isClass = false) const;

private:
	static CSTR AccFlagStr[];
	static const int MAX_JAVA_FLAGS;
	u2 m_u2Flags;
};


//==============================================================================
// CJavaType
//------------------------------------------------------------------------------
// Java Type
// 
// Used by CJavaMethodName and the class builder features
//
// Construct primitive types using the J_xxx enum.
// Construct reference types using J_CLASS, and supply the
// internal form ("Ljava/lang/String;") as the third
// argument to the constructor.
// Construct array types by using a nonzero value for i_nDim.
//

class CJavaType
{
public:
	typedef enum
	{
		J_BYTE,
		J_CHAR,
		J_DOUBLE,
		J_FLOAT,
		J_INT,
		J_LONG,
		J_CLASS,
		J_SHORT,
		J_BOOLEAN,
		J_VOID,
		J_ARRAY,
		J_UNKNOWN,
		J_LAST
	} jtype_t;

public:
	CJavaType(jtype_t i_jtype = CJavaType::J_UNKNOWN, int i_nDim = 0, CSTR i_szType = NULL);
	CJavaType(const CJavaType& i_type){*this = i_type;}
	void	Parse(CSTR i_szTypeSymbol);
	jtype_t GetType() const {return m_jtype;}
	string	GetTypeString() const;
	bool	IsClass() const {return m_jtype == J_CLASS;}
	bool	IsPrimitive() const;
	bool	IsArray() const {return m_nDim != 0;}
	CSTR	GetClassName() const {return m_strClass.c_str();}
	int		GetDim() const{return m_nDim;}
	int		GetCategory() const;
	int		GetStackSize() const;
	string  ToString() const;

	CJavaType& operator = (const CJavaType& i_type);

private:
	string		m_strClass;	// Class Name (for class type only)
	jtype_t		m_jtype;	// Type ID
	int			m_nDim;		// Number of dimensions
};

//==============================================================================
// CJavaMethodName
//------------------------------------------------------------------------------
// Java Method Name
//
// Handles method names represented in the Java internal notation.
// Converts into the external notation and provides the type information 
// about arguments and the return type.
//
// 
class CJavaMethodName
{
public:
	typedef vector<CJavaType> args_t;

public:
	CJavaMethodName(CSTR i_szNameAndSig = NULL, bool i_bAutoParse=true);
	void SetName(CSTR i_szName);
	void SetSignature(CSTR i_szSig);
	CSTR GetName() const;
	CSTR GetSignature() const;
	const CJavaType& GetRetType() const;
	const args_t& GetArgs() const {return m_args;}
	int	GetArgCount() const;
	int GetStackSize() const;
	
	void Parse();
	string ToString() const;

private:
	bool		m_bAutoParse;	// Parse automatically when signature changes
	string		m_strName;		// Method name
	string		m_strSignature;	// Method signature (in Java notation)

	CJavaType	m_jtRet;		// Return type
	args_t		m_args;			// Contatiner of the argument types
};

#endif
//= End of JavaHelpers.h =======================================================
