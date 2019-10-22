/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ModuleJ.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//*
//* ModuleJ.h
//*

//==============================================================================
// ModuleJ.h
// 9/28/99
//
//------------------------------------------------------------------------------
// Description 
// Java-specific module implementation (see Module.h)
//==============================================================================
#ifndef _MODULEJ_H
#define _MODULEJ_H
#ifdef WIN32
#pragma warning(disable:4786)
#endif

#include "Module.h"
#include "JClassBuilder.h"

class CModuleJ;
class CMethodJ;

//==============================================================================
// CModuleJ
// Java module
// Java specific implementation for the abstract Module
//
class CModuleJ : public CModule
{
public:
	CModuleJ();
	virtual ~CModuleJ();

	virtual CSTR	GetName() const {return m_strName.c_str();}
	virtual CSTR	GetLanguage() const {return "Java";}
	virtual CSTR	GetDescription() const {return "Java Instrumentation Engine V1.1";}
	virtual bool	IsInstrumented() const;

	virtual vector<string> GetInterfaces() const { return m_interfaceNames; }
	virtual bool	IsAnInterface() const {return (this != 0) && ((m_fAccessFlags & ACC_INTERFACE) != 0);}
	virtual void	SetAccessFlags(unsigned long f);
	virtual void	Open(CSTR i_szName);
	virtual void	Open(CJClassBuilder* io_pClass, bool i_fDestroyClass = false);
	virtual void	AddExtRef(CExtRef& i_ExtRef);
	virtual void	AddStringAttrib(CSTR i_szName, CSTR i_szValue);
	virtual void	Parse();
	virtual void	Emit();

	void	Emit(CJStream& i_jstream);	// Java specific emission
	CJClassFile&	GetClass();
	CJClassBuilder& GetClassBuilder();

	virtual const vector<string>& GetSourceFileNames();

	// Module modification methods
	CCPFieldrefInfo*	CreateFieldRef(u2 i_u2AccFlags, CSTR i_szName, CJavaType i_jtype);
	CMethodJ*			CreateMethod(u2 i_u2AccFlags, CSTR i_szName, CSTR i_szSignature);

protected:

private:
	void	Verify();

private:
	string			m_strName;			// Module name
	CJClassBuilder*	m_pClass;			// Java class file builder (see JClassBuilder.h)
	bool			m_fDestroyClass;	// Need to destroy class?
	unsigned long		m_fAccessFlags;	// Access flags (to check for an interface)
	vector<string>	m_interfaceNames;	// List of interface names implemented by this class

	// Storage in support of GetSourceFileNames:
	vector<string>	m_sourceFileNames;
	bool			m_sourceFileNamesPopulated;
};

//==============================================================================
//
//
class CMethodJ : public CMethod
{
public:
	CMethodJ(CModule* i_pModule, CSTR i_szName, CSTR i_szSignature,
		     CCodeAttribute* i_pCodeAttr, u2 i_u2AccessFlags)
	:CMethod(i_pModule, i_szName)
	{
		m_strSignature = i_szSignature;
		m_pCodeAttr = i_pCodeAttr;
		m_u2AccessFlags = i_u2AccessFlags;

		// Set the "hasThis" attribute, which is accessible to all CMethod users
		SetHasThis(!(m_u2AccessFlags & ACC_STATIC));
	}
	virtual ~CMethodJ(){;}

	CCodeAttribute* GetCodeAttribute() {return m_pCodeAttr;}
	CSTR	GetSignature() const {return m_strSignature.c_str();}
	bool IsAbstract() { return ((m_u2AccessFlags & ACC_ABSTRACT) != 0); };
    u2 GetAccessFlags() const { return m_u2AccessFlags; }

	virtual void Parse();
	virtual void Emit();
	virtual int	 CalcStackDepth();

protected:
private:
	string				m_strSignature;		// Java method signature
	CCodeAttribute*		m_pCodeAttr;		// Code attribute from CJClassFile
	u2					m_u2AccessFlags;	// Method access flags
	IP_t				m_origCodeLength;	// Saved code length from Parse to Emit
};


//==============================================================================
// CMethodExceptionJ
class CMethodExceptionJ : public CMethodException
{
public:
	CMethodExceptionJ(unsigned i_uType, IP_t i_ipStart, IP_t i_ipEnd, CInsBlock* i_pblkHandler)
	:CMethodException(i_ipStart, i_ipEnd, i_pblkHandler)
	,m_uType(i_uType)
	{}
	unsigned GetType()const{return m_uType;}

private:
	unsigned m_uType;

};

//==============================================================================
// CMtdExtableJ
// Exception table for Java
//
class CMtdExTableJ : public CMtdExTable
{
public:
	CMtdExTableJ(CMethodJ* i_pmtd):CMtdExTable((CMethod*)i_pmtd){}
	virtual void	Parse();
	virtual void	Emit();
	virtual void	Dump(ostream& i_os) const;
};

//==============================================================================
// CSerialVersionUIDHelper
// A class containing logic for computing serialVersionUID for Java
//
// An object of this type should be created *before* you make any 
// changes to the class, if you want the computed ID to match the original.
// In particular, do it before creating new methods or new non-private statics.
//
// Computing a serialVersionUID means following a very specific set of rules.
// The rules are defined in some documents on java.sun.com, but there are
// some ambiguities in those rules that can only be filled by experience and
// a little trial and error. See the comments in BuildSUIDByteArray for those.
//
// This class contains an open-coded, not-platform-optimized 
// function for unsigned_memcmp because some UNIXes use signed byte compare,
// not unsigned, for memcmp.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//
class CSerialVersionUIDHelper 
{
public:
	CSerialVersionUIDHelper(CModuleJ* theClass);
	// u64 getSerialVersionUID(); // sigh... someday...
	u1* GetBytes() { return data; };
	u2	GetLength() { return used_length; };
	~CSerialVersionUIDHelper() { free (data); };

private:
	void BuildSUIDByteArray(CModuleJ* theClass);

	// Compare and sort functions for getting things in the right order.
	static int unsigned_memcmp(const unsigned char* left, const unsigned char* right, int len);
	static int qsort_compare_CCPUtf8Info(const void* vleft, const void* vright);
	static int qsort_compare_CFieldInfo(const void* vleft, const void* vright);
	static int qsort_compare_CJMethodInfo(const void* vleft, const void* vright);

	// Below are the data items and methods that let us build up a byte array
	// that we should eventually compute SHA on.
	enum { CHUNK_SIZE = 100 };
	u1* data;
	u2 used_length;
	u2 allocated_length;

	// Write the bytes explicitly - nothing but the bytes 
	void Append(const u1* bytes, u2 length);

	// Write the length and then the bytes from a memory buffer
	void AppendUTF(const u1* bytes, u2 length);

	// Write the length and then the string from a constant pool entry
	void Append(CCPUtf8Info* pUTF8);

	// Convert slashes to dots and append as above
	void ConvertWithDotsAndAppend(CCPUtf8Info* pUTF8);

	// Convert dots to slashes and append as above
	void ConvertWithSlashesAndAppend(CCPUtf8Info* pUTF8);

	// Write the value using the proper byte ordering
	void Append(int value);
};



#endif //defined MODULEJ_H

//= End of ModuleJ.h ===========================================================
