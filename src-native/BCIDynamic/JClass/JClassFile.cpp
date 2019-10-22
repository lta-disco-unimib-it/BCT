/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JClassFile.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// JClassFile.cpp: implementation of the CJClassFile class.
//
//
// When using CConstantPool::Add, remember that it takes ownership of
// the memory you pass in as the CPPInfo pointer. You don't need to
// delete the object you passed in - the CConstantPool destructor will
// do that. Also, if the thing you're "adding" was already there, the
// Add call will immediately delete the object you passed in! So don't
// use that pointer any more. Instead, use the index returned by Add
// to access the thing you added.
//
// The typical pattern for adding a new constant is:
//
// CConstPool* pcp = class_file.GetConstPool();
// CCPInfo* pcpi = new ... CCPInfo derivative ...;
// u2 u2Index = pcp->Add(pcpi);
// /* at this point pcpi might have been freed! */
// ...
// CCPInfo* added_item = pcp->at(u2Index);
// UseTheIndex(added_item->GetCpIndex());
//
// And the absolute WRONG thing to use after Add would be pcpi->GetCpIndex()
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include <string>

#ifdef HPUX
#  include <iostream.h>
#else
#  include <iostream>
#endif

#include "CommonDef.h"
#include "JClassFile.h"

USE_NAMESPACE(std);

//==============================================================================
// CCPInfo implementation
//

//------------------------------------------------------------------------------
CCPInfo::CCPInfo(u1 i_u1Tag)
{
	m_u1Tag = i_u1Tag;
	m_pu1Info = NULL;
	m_u2CpIndex = 0;
}

//------------------------------------------------------------------------------
CCPInfo::CCPInfo(CCPInfo& i_cpinfo)
{
	m_u1Tag = i_cpinfo.GetTag();
	u1* pu1Info = i_cpinfo.GetInfo();
	u2 u2Size = *(u2*)pu1Info;
	m_pu1Info = new u1[u2Size];
	memcpy(m_pu1Info, pu1Info, u2Size);
	m_u2CpIndex = i_cpinfo.GetCpIndex();
}

//------------------------------------------------------------------------------
CCPInfo::~CCPInfo()
{
	if(NULL != m_pu1Info)
	{
		delete m_pu1Info;
		m_pu1Info = NULL;
	}
}

//------------------------------------------------------------------------------
u1*		
CCPInfo::GetInfo()
{
	//Stub
	// TODO: implement
	throw CJClassFileException(CJClassFileException::X_NOT_IMPLEMENTED);
	return m_pu1Info;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPUtf8Info* ()
{
	if(GetTag() != CONSTANT_Utf8)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPUtf8Info*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPIntegerInfo* ()
{
	if(GetTag() != CONSTANT_Integer)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPIntegerInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPFloatInfo* ()
{
	if(GetTag() != CONSTANT_Float)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPFloatInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPLongInfo* ()
{
	if(GetTag() != CONSTANT_Long)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPLongInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPDoubleInfo* ()
{
	if(GetTag() != CONSTANT_Double)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPDoubleInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPClassInfo* ()
{
	if(GetTag() != CONSTANT_Class)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPClassInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPStringInfo* ()
{
	if(GetTag() != CONSTANT_String)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPStringInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPMethodrefInfo* ()
{
	if(GetTag() != CONSTANT_Methodref)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPMethodrefInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPInterfaceMethodrefInfo* ()
{
	if(GetTag() != CONSTANT_InterfaceMethodref)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPInterfaceMethodrefInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPNameAndTypeInfo* ()
{
	if(GetTag() != CONSTANT_NameAndType)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPNameAndTypeInfo*)this;
}

//------------------------------------------------------------------------------
CCPInfo::operator CCPFieldrefInfo* ()
{
	if(GetTag() != CONSTANT_Fieldref)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	return (CCPFieldrefInfo*)this;
}

//------------------------------------------------------------------------------
void
CCPInfo::Read(CJStream& i_jstream)
{
	throw CJClassFileException(CJClassFileException::X_NOT_IMPLEMENTED);
}

//------------------------------------------------------------------------------
void
CCPInfo::Write(CJStream& i_jstream) const
{
	throw CJClassFileException(CJClassFileException::X_NOT_IMPLEMENTED);
}

//------------------------------------------------------------------------------
u4
CCPInfo::GetSize() const
{
	throw CJClassFileException(CJClassFileException::X_NOT_IMPLEMENTED);
	return 0;
}


//==============================================================================
// CCPUtf8 implementation
//


//------------------------------------------------------------------------------
CCPUtf8Info::CCPUtf8Info()
:CCPInfo(CONSTANT_Utf8)
{
	m_u2Length = 0;
	m_pu1Bytes = NULL;
}

//------------------------------------------------------------------------------
CCPUtf8Info::CCPUtf8Info(const CCPUtf8Info& i_utf8Info)
:CCPInfo(CONSTANT_Utf8)
{
	m_u2Length = i_utf8Info.GetLength();
	m_pu1Bytes = new u1[m_u2Length];
	memcpy(m_pu1Bytes, i_utf8Info.GetBytes(), m_u2Length);
}

//------------------------------------------------------------------------------
CCPUtf8Info::CCPUtf8Info(const char* i_szString)
:CCPInfo(CONSTANT_Utf8)
{
	// TODO: insert UTF8 scanner here
	if(i_szString == NULL)
		throw CJClassFileException(CJClassFileException::X_BAD_VALUE);

	// Throw an exception if the string is longer than we can represent
	if(strlen(i_szString) > 0xffff)
		throw CJClassFileException(CJClassFileException::X_BAD_VALUE);

	m_u2Length = strlen(i_szString);
	m_pu1Bytes = new u1[m_u2Length];
	memcpy(m_pu1Bytes, i_szString, m_u2Length);
}

//------------------------------------------------------------------------------
CCPUtf8Info::~CCPUtf8Info()
{
	if(NULL != m_pu1Bytes)
	{
		delete m_pu1Bytes;
		m_pu1Bytes = NULL;
	}
}

//------------------------------------------------------------------------------
void
CCPUtf8Info::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2Length;
	m_pu1Bytes = new u1[m_u2Length];
	i_jstream.ReadUtf8((void*)m_pu1Bytes, m_u2Length);
}

//------------------------------------------------------------------------------
void
CCPUtf8Info::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2Length;
	i_jstream.WriteUtf8((void*)m_pu1Bytes, m_u2Length);
}

//------------------------------------------------------------------------------
u4
CCPUtf8Info::GetSize() const
{
	u4 u4Size = sizeof(m_u2Length);
	u4Size += m_u2Length;
	return u4Size;
}

//------------------------------------------------------------------------------
bool
CCPUtf8Info::Equals (CCPInfo* i_pcpinfo) const
{
	if(CONSTANT_Utf8 != i_pcpinfo->GetTag())
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);

	CCPUtf8Info* putf8info = reinterpret_cast<CCPUtf8Info*>(i_pcpinfo);
	bool sameleng = m_u2Length == putf8info->GetLength();
	bool identical;
	if(sameleng)
		identical = 0 == memcmp((void*)m_pu1Bytes, (void*)putf8info->GetBytes(), m_u2Length);
	return sameleng && identical;
}

//------------------------------------------------------------------------------
bool
CCPUtf8Info::operator == (CSTR i_str) const
{
	bool Equal =  strlen(i_str) == m_u2Length
	           && 0 == memcmp((void*)m_pu1Bytes, (void*)i_str, m_u2Length);
	return Equal;
}

//==============================================================================
// CCPIntegerInfo implementation

//------------------------------------------------------------------------------
CCPIntegerInfo::CCPIntegerInfo(u4 i_u4Bytes)
:CCPInfo(CONSTANT_Integer)
{
	m_u4Bytes = i_u4Bytes;
}

//------------------------------------------------------------------------------
bool
CCPIntegerInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Integer)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPIntegerInfo* pccpinteger = reinterpret_cast<CCPIntegerInfo*>(i_pinfo);
	return *this == *pccpinteger;

}

//------------------------------------------------------------------------------
void
CCPIntegerInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u4Bytes;
}

//------------------------------------------------------------------------------
void
CCPIntegerInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u4Bytes;
}

//------------------------------------------------------------------------------
u4
CCPIntegerInfo::GetSize() const
{
	return sizeof(m_u4Bytes);
}

//------------------------------------------------------------------------------
bool
CCPIntegerInfo::operator == (const CCPIntegerInfo& i_ccpinteger) const
{
	return i_ccpinteger.GetBytes() == m_u4Bytes;
}


//==============================================================================
// CCPFloatInfo implementation

//------------------------------------------------------------------------------
CCPFloatInfo::CCPFloatInfo(float i_float)
:CCPInfo(CONSTANT_Float)
{
	m_float = i_float;
}

//------------------------------------------------------------------------------
bool
CCPFloatInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Float)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPFloatInfo* pccpfloat = reinterpret_cast<CCPFloatInfo*>(i_pinfo);
	return *this == *pccpfloat;

}

//------------------------------------------------------------------------------
void
CCPFloatInfo::Read(CJStream& i_jstream)
{
	u4 u4Value;
	i_jstream >> u4Value;
	*((u4*)&m_float) = u4Value;
}

//------------------------------------------------------------------------------
void
CCPFloatInfo::Write(CJStream& i_jstream) const
{
	u4 u4Value;
	u4Value = *((u4*)&m_float);
	i_jstream << u4Value;
}

//------------------------------------------------------------------------------
u4
CCPFloatInfo::GetSize() const
{
	return sizeof(u4);
}


//------------------------------------------------------------------------------
bool
CCPFloatInfo::operator == (const CCPFloatInfo& i_ccpfloat) const
{
	return i_ccpfloat.GetFloat() == m_float;
}

//==============================================================================
// CCPLongInfo implementation

//------------------------------------------------------------------------------
CCPLongInfo::CCPLongInfo(int64_t i_arg)
:CCPInfo(CONSTANT_Long)
{
	m_int64 = i_arg;
	
}

//------------------------------------------------------------------------------
bool
CCPLongInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Long)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPLongInfo* pccplong = reinterpret_cast<CCPLongInfo*>(i_pinfo);
	return *this == *pccplong;

}

//------------------------------------------------------------------------------
void
CCPLongInfo::Read(CJStream& i_jstream)
{
	u64_t u64;
#ifdef BIG_ENDIAN_HW
	i_jstream >> u64.bytes.h >> u64.bytes.l;
#else
	i_jstream >> u64.bytes.l >> u64.bytes.h;
#endif
	m_int64 = u64.i64;
}

//------------------------------------------------------------------------------
void
CCPLongInfo::Write(CJStream& i_jstream) const
{
	u64_t u64 = {0};
	u64.i64 = m_int64;
#ifdef BIG_ENDIAN_HW
	i_jstream << u64.bytes.h << u64.bytes.l;
#else
	i_jstream << u64.bytes.l << u64.bytes.h;
#endif
}

//------------------------------------------------------------------------------
u4
CCPLongInfo::GetSize() const
{
	return sizeof(m_int64);
}

//------------------------------------------------------------------------------
bool
CCPLongInfo::operator == (const CCPLongInfo& i_ccplong) const
{
	return i_ccplong.GetLong() == m_int64;
}

//------------------------------------------------------------------------------
ostream&
operator << (ostream& s, int64_t l)
{
	char buffer[25];
	int base;
	if (s.flags() & ios::hex)
		base = 16;
	else
		base = 10;
#ifdef _UNIX_
// TODO anandi
	// s << l64a(l, buffer, base);
	s << "missing logic to output int64_t";
#else
	s << _i64toa(l, buffer, base);
#endif
	return s;
}


//==============================================================================
// CCPDoubleInfo implementation

//------------------------------------------------------------------------------
CCPDoubleInfo::CCPDoubleInfo(double i_double)
:CCPInfo(CONSTANT_Double)
{
	m_double = i_double;
}

//------------------------------------------------------------------------------
bool
CCPDoubleInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Double)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	CCPDoubleInfo* pccpdouble = reinterpret_cast<CCPDoubleInfo*>(i_pinfo);
	return *this == *pccpdouble;
}

//------------------------------------------------------------------------------
void
CCPDoubleInfo::Read(CJStream& i_jstream)
{
	u64_t u64;
#ifdef BIG_ENDIAN_HW
	i_jstream >> u64.bytes.h >> u64.bytes.l;
#else
	i_jstream >> u64.bytes.l >> u64.bytes.h;
#endif
	m_double = u64.d;
}

//------------------------------------------------------------------------------
void
CCPDoubleInfo::Write(CJStream& i_jstream) const
{
	u64_t u64;
	u64.d = m_double;
#ifdef BIG_ENDIAN_HW
	i_jstream << u64.bytes.h << u64.bytes.l;
#else
	i_jstream << u64.bytes.l << u64.bytes.h;
#endif
}

//------------------------------------------------------------------------------
u4
CCPDoubleInfo::GetSize() const
{
	return sizeof(m_double);
}

//------------------------------------------------------------------------------
bool
CCPDoubleInfo::operator == (const CCPDoubleInfo& i_ccpdouble) const
{
	return i_ccpdouble.GetDouble() == m_double;
}

//==============================================================================
// CCPClassInfo Implementation

//------------------------------------------------------------------------------
CCPClassInfo::CCPClassInfo(u2 i_u2ClassInd)
:CCPInfo(CONSTANT_Class)
{
	m_u2ClassInd = i_u2ClassInd;
}

//------------------------------------------------------------------------------
bool
CCPClassInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Class)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPClassInfo* pccpclass = reinterpret_cast<CCPClassInfo*>(i_pinfo);
	return *this == *pccpclass;
}

//------------------------------------------------------------------------------
void
CCPClassInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2ClassInd;
}

//------------------------------------------------------------------------------
void
CCPClassInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2ClassInd;
}

//------------------------------------------------------------------------------
u4
CCPClassInfo::GetSize() const
{
	return sizeof(m_u2ClassInd);
}

//------------------------------------------------------------------------------
bool
CCPClassInfo::operator == (const CCPClassInfo& i_ccpclass) const
{
	return i_ccpclass.GetClassInd() == m_u2ClassInd;
}

//==============================================================================
// CCPStringInfo Implemrntation

//------------------------------------------------------------------------------
CCPStringInfo::CCPStringInfo(u2 i_u2StringInd)
:CCPInfo(CONSTANT_String)
{
	m_u2StringInd = i_u2StringInd;
}

//------------------------------------------------------------------------------
void
CCPStringInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2StringInd;
}

//------------------------------------------------------------------------------
void
CCPStringInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2StringInd;
}

//------------------------------------------------------------------------------
u4
CCPStringInfo::GetSize() const
{
	return sizeof(m_u2StringInd);
}

//------------------------------------------------------------------------------
bool
CCPStringInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_String)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	CCPStringInfo* pccpstr = reinterpret_cast<CCPStringInfo*>(i_pinfo);
	return *this == *pccpstr;
}

//------------------------------------------------------------------------------
bool
CCPStringInfo::operator == (const CCPStringInfo& i_ccpstr) const
{
	return i_ccpstr.GetStringInd() == m_u2StringInd;
}

//==============================================================================
// CCPFieldrefInfo implementation

//------------------------------------------------------------------------------
CCPFieldrefInfo::CCPFieldrefInfo(u2 i_u2Classind, u2 i_u2NameAndTypeInd)
:CCPInfo(CONSTANT_Fieldref)
{
	m_u2ClassInd = i_u2Classind;
	m_u2NameAndTypeInd = i_u2NameAndTypeInd;
}

//------------------------------------------------------------------------------
bool
CCPFieldrefInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Fieldref)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	CCPFieldrefInfo* pccpfieldref = reinterpret_cast<CCPFieldrefInfo*>(i_pinfo);
	return *this == *pccpfieldref;
}

//------------------------------------------------------------------------------
void
CCPFieldrefInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2ClassInd
		      >> m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
void
CCPFieldrefInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2ClassInd
		      << m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
u4
CCPFieldrefInfo::GetSize() const
{
	return sizeof(m_u2ClassInd) + sizeof(m_u2NameAndTypeInd);	
}


//------------------------------------------------------------------------------
bool
CCPFieldrefInfo::operator == (const CCPFieldrefInfo& i_ccpfieldref) const
{
	return i_ccpfieldref.GetClassInd() == m_u2ClassInd
		&& i_ccpfieldref.GetNameAndTypeInd() == m_u2NameAndTypeInd;
}

//==============================================================================
// CCPMethodrefInfo implementation

//------------------------------------------------------------------------------
CCPMethodrefInfo::CCPMethodrefInfo(u2 i_u2ClassInd, u2 i_u2NameAndTypeInd)
:CCPInfo(CONSTANT_Methodref)
{
	m_u2ClassInd = i_u2ClassInd;
	m_u2NameAndTypeInd = i_u2NameAndTypeInd;
}

//------------------------------------------------------------------------------
bool
CCPMethodrefInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_Methodref)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPMethodrefInfo* pmethodref = reinterpret_cast<CCPMethodrefInfo*>(i_pinfo);
	return *this == *pmethodref;
}

//------------------------------------------------------------------------------
void
CCPMethodrefInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2ClassInd
		      >> m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
void
CCPMethodrefInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2ClassInd
		      << m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
u4
CCPMethodrefInfo::GetSize() const
{
	return sizeof(m_u2ClassInd) + sizeof(m_u2NameAndTypeInd);	
}

//------------------------------------------------------------------------------
bool
CCPMethodrefInfo::operator == (const CCPMethodrefInfo& i_methodref) const
{
	return i_methodref.GetClassInd() == m_u2ClassInd
		&& i_methodref.GetNameAndTypeInd() == m_u2NameAndTypeInd;
}

//==============================================================================
// CCPInterfaceMethodrefInfo implementation

//------------------------------------------------------------------------------
CCPInterfaceMethodrefInfo::CCPInterfaceMethodrefInfo()
:CCPInfo(CONSTANT_InterfaceMethodref)
{
	m_u2ClassInd = 0;
	m_u2NameAndTypeInd = 0;
}

//------------------------------------------------------------------------------
CCPInterfaceMethodrefInfo::CCPInterfaceMethodrefInfo(u2 i_u2ClassInd, u2 i_u2NameAndTypeInd)
:CCPInfo(CONSTANT_InterfaceMethodref)
{
	m_u2ClassInd = i_u2ClassInd;
	m_u2NameAndTypeInd = i_u2NameAndTypeInd;
}

//------------------------------------------------------------------------------
bool
CCPInterfaceMethodrefInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_InterfaceMethodref)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	}
	CCPInterfaceMethodrefInfo* pintmethodref = reinterpret_cast<CCPInterfaceMethodrefInfo*>(i_pinfo);
	return *this == *pintmethodref;
}

//------------------------------------------------------------------------------
void
CCPInterfaceMethodrefInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2ClassInd
		      >> m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
void
CCPInterfaceMethodrefInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2ClassInd
		      << m_u2NameAndTypeInd;	
}

//------------------------------------------------------------------------------
u4
CCPInterfaceMethodrefInfo::GetSize() const
{
	return sizeof(m_u2ClassInd) + sizeof(m_u2NameAndTypeInd);	
}

//------------------------------------------------------------------------------
bool
CCPInterfaceMethodrefInfo::operator == (const CCPInterfaceMethodrefInfo& i_intmethodref) const
{
	return i_intmethodref.GetClassInd() == m_u2ClassInd
		&& i_intmethodref.GetNameAndTypeInd() == m_u2NameAndTypeInd;
}

//==============================================================================
// CCPNameAndTypeInfo implementation

//------------------------------------------------------------------------------
CCPNameAndTypeInfo::CCPNameAndTypeInfo(u2 i_u2NameInd, u2 i_u2DescriptorInd)
:CCPInfo(CONSTANT_NameAndType)
{
	m_u2NameInd = i_u2NameInd;
	m_u2DescriptorInd = i_u2DescriptorInd;
}

//------------------------------------------------------------------------------
CCPNameAndTypeInfo::CCPNameAndTypeInfo()
:CCPInfo(CONSTANT_NameAndType)
{
	m_u2NameInd = 0;
	m_u2DescriptorInd = 0;
}
//------------------------------------------------------------------------------
void
CCPNameAndTypeInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2NameInd
		      >> m_u2DescriptorInd;	
}

//------------------------------------------------------------------------------
void
CCPNameAndTypeInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2NameInd
		      << m_u2DescriptorInd;	
}

//------------------------------------------------------------------------------
u4
CCPNameAndTypeInfo::GetSize() const
{
	return sizeof(m_u2NameInd) + sizeof(m_u2DescriptorInd);	
}

//------------------------------------------------------------------------------
bool
CCPNameAndTypeInfo::Equals(CCPInfo* i_pinfo) const
{
	if(i_pinfo->GetTag() != CONSTANT_NameAndType)
		throw CJClassFileException(CJClassFileException::X_BAD_TYPECAST);
	CCPNameAndTypeInfo* pinfo = reinterpret_cast<CCPNameAndTypeInfo*>(i_pinfo);
	return *this == *pinfo;
}

//------------------------------------------------------------------------------
bool
CCPNameAndTypeInfo::operator == (const CCPNameAndTypeInfo& i_info) const
{
	return (m_u2NameInd == i_info.GetNameInd()
		    && m_u2DescriptorInd == i_info.GetDescriptorInd());
}

//==============================================================================
// CConstPool implementation

//------------------------------------------------------------------------------
CConstPool::CConstPool()
{
	resize(1);
	(*this)[0] = NULL;	// cp[0] is reserved for JVM!!!
}

//------------------------------------------------------------------------------
CConstPool::~CConstPool()
{
	for(iterator iter = begin(); iter < end(); iter++)	
	{
		delete *iter;
		*iter = 0;
	}
}

//------------------------------------------------------------------------------
// Note:
// Double and Long constants take 2 elements of constant pool but use 1.
// This is undocumented Java feature.
void
CConstPool::Read(CJStream& i_jstream)
{
	u2 Size;				// Constant Pool size
	u1 Tag;					// Next constant tag
	CCPInfo* pCurrent;

	clear();
	i_jstream	>>	Size;					// Get size from the input stream
	resize(Size);
	(*this)[0] = NULL;                                              // Zero Element (never used)
	for(u2 Ind = 1;  Ind < Size; Ind++)
	{
		i_jstream >> Tag;
		switch(Tag)
		{
		case CONSTANT_Utf8:					// Get UTF8 constant info
			pCurrent = new CCPUtf8Info;		
			break;
		case CONSTANT_Integer:				// Get integer constant info
			pCurrent = new CCPIntegerInfo;	
			break;
		case CONSTANT_Float:				// Get float constant info
			pCurrent = new CCPFloatInfo;
			break;
		case CONSTANT_Long:					// Get long constant info
			pCurrent = new CCPLongInfo;
			break;
		case CONSTANT_Double:				// Get double constant info
			pCurrent = new CCPDoubleInfo;
			break;
		case CONSTANT_Class:				// Get class constant info
			pCurrent = new CCPClassInfo;
			break;
		case CONSTANT_String:				// Get string constant info
			pCurrent = new CCPStringInfo;
			break;
		case CONSTANT_Fieldref:				// Get field ref constant info
			pCurrent = new CCPFieldrefInfo;
			break;
		case CONSTANT_Methodref:			// Get method ref constant info
			pCurrent = new CCPMethodrefInfo;
			break;
		case CONSTANT_InterfaceMethodref:	// Get interface method ref constant info
			pCurrent = new CCPInterfaceMethodrefInfo;
			break;
		case CONSTANT_NameAndType:			// Get name and type constant info
			pCurrent = new CCPNameAndTypeInfo;
			break;
		default:							// Bad constant. Throw exception
			throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);
			break;
		} // end switch
		pCurrent->Read(i_jstream);
		pCurrent->m_u2CpIndex = Ind;
		(*this)[Ind] = pCurrent;
		if(CONSTANT_Long == Tag || CONSTANT_Double == Tag)
		{// See comment in the method header
			Ind++;
			(*this)[Ind] = NULL;
		}
	} // end for
}

//------------------------------------------------------------------------------
void
CConstPool::Write(CJStream& i_jstream) const
{
	i_jstream	<<	(u2)size();
	// For each element of the constant pool write the content.
	for(int i = 1; i < size(); i++)
	{
		CCPInfo* pcpinfo = (*this)[i];
		u1 tag = pcpinfo->GetTag();
		i_jstream << tag;
		pcpinfo->Write(i_jstream);
		if(CONSTANT_Long == tag || CONSTANT_Double == tag)
		{
			i++;
		}
	}
}

//------------------------------------------------------------------------------
u4
CConstPool::GetSize() const
{
	u4 u4Size = sizeof(u2);
	for(int i = 1; i < size(); i++)
	{
		CCPInfo* pcpinfo = (*this)[i];
		u1 tag = pcpinfo->GetTag();
		u4Size += sizeof(tag) + pcpinfo->GetSize();
		if(CONSTANT_Long == tag || CONSTANT_Double == tag)
		{
			i++;
		}
	}
	return u4Size;
}

//------------------------------------------------------------------------------
u2
CConstPool::Find(CCPInfo* i_pinfo) const
{
	bool	found = false;
	u2		u2Ind = 1;

	//TODO: implement some smart caching?
	while(u2Ind < size() && !found)
	{
		CCPInfo* pinfo = (*this)[u2Ind];
		if( NULL != pinfo
		&&	i_pinfo->GetTag() == pinfo->GetTag()
		&&	i_pinfo->Equals(pinfo))
			found = true;	
		else
			u2Ind++;
	}
	return found ? u2Ind : CONSTANT_Unknown;
}

//------------------------------------------------------------------------------
u2
CConstPool::Add(CCPInfo* i_pinfo)
{
	u2 u2Index = CONSTANT_Unknown;
	u2 u2Tag = i_pinfo->GetTag();
	
	u2Index = Find(i_pinfo);
	if(CONSTANT_Unknown == u2Index)
	{
		u2Index = size();
		i_pinfo->m_u2CpIndex = u2Index;
		push_back(i_pinfo);
		if(u2Tag == CONSTANT_Long || u2Tag == CONSTANT_Double)
		{
			push_back(NULL);
		}
	}
	else
	{
		delete i_pinfo;
	}
	return u2Index;
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetString(u2 i_u2StringInd)
{
	u2 u2StringInd = ((CCPStringInfo*)(*this)[i_u2StringInd])->GetStringInd();
	if(CONSTANT_Utf8 != (*this)[u2StringInd]->GetTag())
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);
	return (CCPUtf8Info*)(*this)[u2StringInd];
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetClass(u2 i_u2ClassInd)
{
	CCPClassInfo* ccpclass = (CCPClassInfo*)(*this)[i_u2ClassInd];
	u2 u2NameInd = ccpclass->GetClassInd();
	if(CONSTANT_Utf8 != (*this)[u2NameInd]->GetTag())
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);

	return (CCPUtf8Info*)(*this)[u2NameInd];
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetName(u2 i_u2NameAndTypeInd)
{
	CCPNameAndTypeInfo* pnt = (CCPNameAndTypeInfo*)(*this)[i_u2NameAndTypeInd];
	u2 u2NameInd = pnt->GetNameInd();
	return (CCPUtf8Info*)(*this)[u2NameInd];
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetType(u2 i_u2NameAndTypeInd)
{
	if(CONSTANT_NameAndType != (*this)[i_u2NameAndTypeInd]->GetTag())
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);
	CCPNameAndTypeInfo* pnt = (CCPNameAndTypeInfo*)(*this)[i_u2NameAndTypeInd];
	u2 u2TypeInd = pnt->GetDescriptorInd();
	return (CCPUtf8Info*)(*this)[u2TypeInd];
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetMethodClass(u2 i_u2MethodInd)
{
	CCPInfo* pcpinfo = (*this)[i_u2MethodInd];
	u2 tag = pcpinfo->GetTag();
	if(CONSTANT_Methodref != tag
	&& CONSTANT_InterfaceMethodref != tag)
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);

	u2 u2ClassInd = CONSTANT_Methodref == tag ?
		((CCPMethodrefInfo*)pcpinfo)->GetClassInd():
		((CCPInterfaceMethodrefInfo*)pcpinfo)->GetClassInd();
	return GetClass(u2ClassInd);
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetMethodName(u2 i_u2MethodInd)
{
	CCPInfo* pcpinfo = (*this)[i_u2MethodInd];
	u2 tag = pcpinfo->GetTag();
	if(CONSTANT_Methodref != tag
	&& CONSTANT_InterfaceMethodref != tag)
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);

	u2 u2NameAndTypeInd = CONSTANT_Methodref == tag ?
		((CCPMethodrefInfo*)pcpinfo)->GetNameAndTypeInd():
		((CCPInterfaceMethodrefInfo*)pcpinfo)->GetNameAndTypeInd();
	return GetName(u2NameAndTypeInd);
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CConstPool::GetMethodType(u2 i_u2MethodInd)
{
	CCPInfo* pcpinfo = (*this)[i_u2MethodInd];
	u2 tag = pcpinfo->GetTag();
	if(CONSTANT_Methodref != tag
	&& CONSTANT_InterfaceMethodref != tag)
		throw CJClassFileException(CJClassFileException::X_BAD_CONSTANT);

	u2 u2NameAndTypeInd = CONSTANT_Methodref == tag ?
		((CCPMethodrefInfo*)pcpinfo)->GetNameAndTypeInd():
		((CCPInterfaceMethodrefInfo*)pcpinfo)->GetNameAndTypeInd();
	return GetType(u2NameAndTypeInd);
}

//==============================================================================
// CInterfaceInfo Implementation
//

//------------------------------------------------------------------------------
void
CInterfaceInfo::Read(CJStream& i_jstream)
{
	i_jstream >> m_u2Index;
}

//------------------------------------------------------------------------------
void
CInterfaceInfo::Write(CJStream& i_jstream) const
{
	i_jstream << m_u2Index;
}

//------------------------------------------------------------------------------
u4
CInterfaceInfo::GetSize() const
{
	return sizeof(m_u2Index);
}

//==============================================================================
// CJInterfaces Implementation
//

//------------------------------------------------------------------------------
CJInterfaces::CJInterfaces()
{
	;
}

//------------------------------------------------------------------------------
CJInterfaces::~CJInterfaces()
{
	CJInterfaces& interfs = (CJInterfaces&)*this;
	FOR_EACH(iter)
	{
		delete (*iter);
	}
}

//------------------------------------------------------------------------------
void
CJInterfaces::Read(CJStream& i_jstream)
{
	u2 Size;

	i_jstream	>>	Size;
	resize(Size);
	for(u2 Ind = 0;  Ind < Size; Ind++)
	{
		(*this)[Ind] = new CInterfaceInfo();
		(*this)[Ind]->Read(i_jstream);
	}
}

//------------------------------------------------------------------------------
void
CJInterfaces::Write(CJStream& i_jstream) const
{
	i_jstream	<<	(u2)size();
	// For each element of the interface container write the content.
	for(int i = 0; i < size(); i++)
		(*this)[i]->Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CJInterfaces::GetSize() const
{
	u4 u4Size = sizeof(u2);
	// For each element of the interface container write the content.
	for(int i = 0; i < size(); i++)
		u4Size += (*this)[i]->GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
u2
CJInterfaces::Find(CInterfaceInfo* i_pinterface) const
{
	bool	found = false;
	u2		u2Ind;

	for(u2Ind = 0; u2Ind < size() && !found; u2Ind++)
	{
		CInterfaceInfo* pii = (*this)[u2Ind];
		if(*i_pinterface == *pii)
		{
			found = true;	
		}
	}
	return found ? u2Ind : CONSTANT_Unknown;

}

//------------------------------------------------------------------------------
u2
CJInterfaces::Add(CInterfaceInfo* i_pinterface)
{
	u2 u2Index = Find(i_pinterface);
	if(CONSTANT_Unknown == u2Index)
	{
		push_back(i_pinterface);
		u2Index = size() - 1;
	}
	else
	{
		delete i_pinterface;
	}
	return u2Index;
}

//==============================================================================
// CFieldInfo Implementation
//

//------------------------------------------------------------------------------
// Constructors
//

CFieldInfo::CFieldInfo(CJClassFile* i_pClassfile)
: m_pClassFile(i_pClassfile),
  m_Attribs(i_pClassfile)
{
}

//------------------------------------------------------------------------------
// ToDo: Validate input values, throw exception if not valid
CFieldInfo::CFieldInfo(CJClassFile*  i_pClassfile, u2 i_u2NameInd,
                       u2 i_u2DescriptorInd, u2 i_u2AccessFlags)
: m_pClassFile(i_pClassfile),
  m_Attribs(i_pClassfile)
{
	m_u2AccessFlags = i_u2AccessFlags;
	m_u2NameInd = i_u2NameInd;
	m_u2DescriptorInd = i_u2DescriptorInd;
}


//------------------------------------------------------------------------------
void
CFieldInfo::Read(CJStream& i_jstream)
{
	i_jstream	>>	m_u2AccessFlags
				>>	m_u2NameInd
				>>	m_u2DescriptorInd;
	m_Attribs.Read(i_jstream);
}

//------------------------------------------------------------------------------
void
CFieldInfo::Write(CJStream& i_jstream) const
{
	i_jstream	<<	m_u2AccessFlags
				<<	m_u2NameInd
				<<	m_u2DescriptorInd;
	m_Attribs.Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CFieldInfo::GetSize() const
{
	u4 u4Size = sizeof(m_u2AccessFlags)
			  +	sizeof(m_u2NameInd)
			  +	sizeof(m_u2DescriptorInd);
	u4Size += m_Attribs.GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CFieldInfo::GetName()
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	CCPInfo& info = *(cp[m_u2NameInd]);
	return (CCPUtf8Info*)info;	// invoke the cast operator on this info
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CFieldInfo::GetDescriptor()
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	return (CCPUtf8Info*)(cp[m_u2DescriptorInd]);
}


//==============================================================================
// CJFields Implementation
//

//------------------------------------------------------------------------------
CJFields::CJFields(CJClassFile* i_pClassfile)
: m_pClassFile(i_pClassfile)
{
}

//------------------------------------------------------------------------------
CJFields::~CJFields()
{
	FOR_EACH(iter)
		delete (*iter);
}

//------------------------------------------------------------------------------
void
CJFields::Read(CJStream& i_jstream)
{
	u2 Size;
	CFieldInfo* pCurrent;

	i_jstream	>>	Size;
	resize(Size);
	for(u2 Ind = 0;  Ind < Size; Ind++)
	{
		pCurrent = new CFieldInfo(m_pClassFile);
		pCurrent->Read(i_jstream);
		(*this)[Ind] = pCurrent;
	}
}

//------------------------------------------------------------------------------
void
CJFields::Write(CJStream& i_jstream) const
{
	u2 Size = size();
	i_jstream	<<	Size;
	for(int i = 0; i < size(); i++)
		(*this)[i]->Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CJFields::GetSize() const
{
	u4 u4Size = sizeof(u2);
	for(int i = 0; i < size(); i++)
		u4Size += (*this)[i]->GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
u2
CJFields::Add(CFieldInfo* i_pFieldInfo)
{
	push_back(i_pFieldInfo);
	return size() - 1;
}

//==============================================================================
// CJMethodInfo Implementation
//

//------------------------------------------------------------------------------
CJMethodInfo::CJMethodInfo(CJClassFile* i_pClassFile)
: m_pClassFile(i_pClassFile)
, m_Attribs(i_pClassFile)
{
}

//------------------------------------------------------------------------------
CJMethodInfo::CJMethodInfo(CJClassFile*  i_pClassFile, CSTR i_Name,
		                 CSTR i_Descript, u2 i_u2Access)
: m_pClassFile(i_pClassFile)
, m_Attribs(i_pClassFile)
{
	CConstPool* pcp = m_pClassFile->GetConstPool();

	m_u2AccessFlags = i_u2Access;
	m_u2NameInd = pcp->Add(new CCPUtf8Info(i_Name));
	if(NULL != i_Descript)
		m_u2DescriptorInd = pcp->Add(new CCPUtf8Info(i_Descript));
	else
		m_u2DescriptorInd = 0;
}

//------------------------------------------------------------------------------
void
CJMethodInfo::Read(CJStream& i_jstream)
{
	i_jstream	>>	m_u2AccessFlags
				>>	m_u2NameInd
				>>	m_u2DescriptorInd;
	m_Attribs.Read(i_jstream);
}

//------------------------------------------------------------------------------
void
CJMethodInfo::Write(CJStream& i_jstream) const
{
	i_jstream	<<	m_u2AccessFlags
				<<	m_u2NameInd
				<<	m_u2DescriptorInd;
	m_Attribs.Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CJMethodInfo::GetSize() const
{
	u4 u4Size = sizeof(m_u2AccessFlags)
			  + sizeof(m_u2NameInd)
			  + sizeof(m_u2DescriptorInd);
	u4Size += m_Attribs.GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CJMethodInfo::GetName()
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	return (CCPUtf8Info*)(cp[m_u2NameInd]);
}

//------------------------------------------------------------------------------
CCPUtf8Info*	
CJMethodInfo::GetDescriptor()
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	return (CCPUtf8Info*)(cp[m_u2DescriptorInd]);
}

//------------------------------------------------------------------------------
CCodeAttribute*
CJMethodInfo::GetCode()
{
	for(int i = 0; i < m_Attribs.size(); i++)
	{
		if(*(m_Attribs[i]->GetName()) == "Code")
		{
			return (CCodeAttribute*)m_Attribs[i];
		}
	}
	return NULL;
}

//------------------------------------------------------------------------------
void			
CJMethodInfo::SetCode(CCodeAttribute* i_pCode)
{
	bool hasCode = false;

	for(int i = 0; i < m_Attribs.size() && !hasCode; i++)
	{
		if(*(m_Attribs[i]->GetName()) == "Code")
		{
			delete(m_Attribs[i]);
			m_Attribs[i] = i_pCode;
			hasCode = true;
		}
	}
	if(!hasCode)
	{
		m_Attribs.Add(i_pCode);
	}
}

//------------------------------------------------------------------------------
CExceptionsAttribute*
CJMethodInfo::GetExceptions()
{
	for(CJAttribs::iterator iter = m_Attribs.begin(); iter < m_Attribs.end(); iter++)
	{
		if(*(*iter)->GetName() == "Exceptions")
			return (CExceptionsAttribute*)*iter;
	}
	return NULL;
}

//==============================================================================
// CJMethods Implementation
//
CJMethods::~CJMethods()
{
	FOR_EACH(iter)
		delete (*iter);
}

//------------------------------------------------------------------------------
void
CJMethods::Read(CJStream& i_jstream)
{
	u2 Size;
	CJMethodInfo* pCurrent;

	i_jstream	>>	Size;
	resize(Size);
	for(u2 Ind = 0;  Ind < Size; Ind++)
	{
		pCurrent = new CJMethodInfo(m_pClassFile);
		pCurrent->Read(i_jstream);
		(*this)[Ind] = pCurrent;
	}
}

//------------------------------------------------------------------------------
void
CJMethods::Write(CJStream& i_jstream) const
{
	i_jstream	<<	(u2)size();
	for(int i=0; i < size(); i++)
		(*this)[i]->Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CJMethods::GetSize() const
{
	u4 u4Size = sizeof(u2);
	for(int i=0; i < size(); i++)
		u4Size += (*this)[i]->GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
u2
CJMethods::Add(CJMethodInfo* i_pmethod)
{
	// TODO: make sure the method is not ambiguous
	push_back(i_pmethod);
	return size() - 1;
}

//==============================================================================
// CJAttributeInfo Implementation
//

//------------------------------------------------------------------------------
CAttributeInfo::CAttributeInfo(CJClassFile* i_pClassFile)
: m_pClassFile(i_pClassFile)
{	
	m_u2NameInd	= 0;
    m_u4Length	= 0;
    m_pu1Info	= NULL;
}

CAttributeInfo::~CAttributeInfo()
{
	if(NULL != m_pu1Info)
	{
		delete m_pu1Info;
	}
}

CCPUtf8Info*	
CAttributeInfo::GetName()
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	return (CCPUtf8Info*)(cp[m_u2NameInd]);
}


//------------------------------------------------------------------------------
void
CAttributeInfo::Read(CJStream& i_jstream)
{
	// m_u2NameInd is read by CJAttribs.Read
	i_jstream	>>	m_u4Length;
}

//------------------------------------------------------------------------------
void
CAttributeInfo::Write(CJStream& i_jstream) const
{
	i_jstream	<<	m_u2NameInd
				<<	m_u4Length;
}

//------------------------------------------------------------------------------
u4
CAttributeInfo::GetSize() const
{
	return sizeof(m_u2NameInd) + sizeof(m_u4Length);
}

//==============================================================================
// CStringAttribute implementation
//
CStringAttribute::CStringAttribute(CJClassFile* i_pClassFile,
		                           const CCPUtf8Info& i_utf8Name, const CCPUtf8Info& i_utf8Value)
:CAttributeInfo(i_pClassFile)
{
	m_u2NameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info(i_utf8Name));
	if(i_utf8Value.IsEmpty())
	{
		m_u4Length = 0;
	}
	else
	{
		m_u2Ind = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info(i_utf8Value));
		m_u4Length = sizeof(m_u2Ind);
	}
}

//------------------------------------------------------------------------------
CCPUtf8Info*
CStringAttribute::GetValue() const
{
	CConstPool& cp = *(m_pClassFile->GetConstPool());
	return (CCPUtf8Info*)(cp[m_u2Ind]);
}

//------------------------------------------------------------------------------
void
CStringAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2Ind;
	m_u4Length = sizeof(m_u2Ind);
}

//------------------------------------------------------------------------------
void
CStringAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2Ind;
}

//------------------------------------------------------------------------------
u4
CStringAttribute::GetSize() const
{
	u4 u4Size = CAttributeInfo::GetSize();
	u4Size += sizeof(m_u2Ind);
	return u4Size;
}

//==============================================================================
// CJAttribs Implementation
//
// Note: this class is a subclass of std::vector, and the implementation
// uses a lot of patterns like: "(*this)[x] = y;"
// even though "at(x) = y;" is more readable and safer (because at() does
// array-bounds checking). However, GCC 2.9x does not have vector::at().
//
// TODO: rewrite "(*this)[x]" as "at(x)" when we are able to dump GCC 2.9x support.
//

CJAttribs::CJAttribs(CJClassFile*	i_pClassfile)
: m_pClassFile(i_pClassfile)
{
	;
}

//------------------------------------------------------------------------------
CJAttribs::~CJAttribs()
{
	FOR_EACH(iter)
		delete *iter;
}

//------------------------------------------------------------------------------
void
CJAttribs::Read(CJStream& i_jstream)
{
	u2 Size;
	u2 NameInd;
	CAttributeInfo* pCurrent;
	CCPInfo*		pcpi;
	CCPUtf8Info*	pcpUtf8;
	CConstPool& cp = *(m_pClassFile->GetConstPool());

	i_jstream	>>	Size;
	resize(Size);
	for(u2 Ind = 0;  Ind < Size; Ind++)
	{
		i_jstream >> NameInd;
		pcpi = cp[NameInd];
		pcpUtf8 = (CCPUtf8Info*)pcpi;
		if(*pcpUtf8 == "Code")
		{
			pCurrent = new CCodeAttribute(m_pClassFile);
		}
		else if(*pcpUtf8 == "LineNumberTable")
		{
			pCurrent = new CLineNumberTableAttribute(m_pClassFile);
		}
		else if(*pcpUtf8 == "LocalVariableTable")
		{
			pCurrent = new CLocalVariableTableAttribute(m_pClassFile);
		}
 		else if(*pcpUtf8 == "LocalVariableTypeTable")
		{
			pCurrent = new CLocalVariableTypeTableAttribute(m_pClassFile);
		}
		else if(*pcpUtf8 == "Exceptions")
		{
			pCurrent = new CExceptionsAttribute(m_pClassFile);
		}
		else if(*pcpUtf8 == "SourceFile")
		{
			pCurrent = new CSourceFileAttribute(m_pClassFile, CCPUtf8Info());
		}
		else if(*pcpUtf8 == "SourceDir")
		{
			pCurrent = new CSourceDirAttribute(m_pClassFile, CCPUtf8Info());
		}
		else if(*pcpUtf8 == "ConstantValue")
		{
			pCurrent = new CConstantValueAttribute(m_pClassFile);
		}
		else if(false)
		{
			// other known attributes attribute
		}
		else
		{
			pCurrent = new CUnknownAttribute(m_pClassFile, NameInd);
		}
		pCurrent->Read(i_jstream);
		(*this)[Ind] = pCurrent;
	}
}

//------------------------------------------------------------------------------
void
CJAttribs::Write(CJStream& i_jstream) const
{
	u2 Size = (u2)size();
	i_jstream	<<	Size;
	for(int i=0; i < size(); i++)
		(*this)[i]->Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CJAttribs::GetSize() const
{
	u4 u4Size = sizeof(u2);
	for(int i=0; i < size(); i++)
		u4Size += (*this)[i]->GetSize();
	return u4Size;
}

//------------------------------------------------------------------------------
u2		
CJAttribs::Add(CAttributeInfo* i_pAttrib)
{
	push_back(i_pAttrib);
	return size() - 1;
}

//==============================================================================
// CUnknownAttribute implementation
//
//------------------------------------------------------------------------------
CUnknownAttribute::CUnknownAttribute(CJClassFile* i_pClassFile, CSTR i_szName)
: CAttributeInfo(i_pClassFile)
{
	m_u2NameInd = m_pClassFile->GetConstPool()->Add(new CCPUtf8Info(i_szName));
}

//------------------------------------------------------------------------------
void
CUnknownAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	m_pu1Info = new u1[m_u4Length];
	i_jstream.Read((void*)m_pu1Info, m_u4Length);
}

//------------------------------------------------------------------------------
void
CUnknownAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream.Write((void*)m_pu1Info, m_u4Length);
}

//------------------------------------------------------------------------------
u4
CUnknownAttribute::GetSize() const
{
	u4 u4Size = CAttributeInfo::GetSize();
	u4Size += m_u4Length;
	return u4Size;
}

//==============================================================================
// CJException implementation
//
CJException::CJException(u2 i_u2CatchType, u2 i_u2StartPC,
						 u2 i_u2EndPC, u2 i_u2HandlerPC)
{
	m_u2Catchtype	= i_u2CatchType;
	m_u2StartPC		= i_u2StartPC;
	m_u2EndPC		= i_u2EndPC;
	m_u2HandlerPC	= i_u2HandlerPC;
}

//------------------------------------------------------------------------------
void
CJException::Read(CJStream& i_jstream)
{
	i_jstream	>>	m_u2StartPC
				>>	m_u2EndPC
				>>	m_u2HandlerPC
				>>	m_u2Catchtype;	
}

//------------------------------------------------------------------------------
void
CJException::Write(CJStream& i_jstream) const
{
	i_jstream	<<	m_u2StartPC
				<<	m_u2EndPC
				<<	m_u2HandlerPC
				<<	m_u2Catchtype;	
}

//------------------------------------------------------------------------------
u4
CJException::GetSize() const
{
	u4 u4Size = sizeof(m_u2StartPC)
			  + sizeof(m_u2EndPC)
			  + sizeof(m_u2HandlerPC)
			  + sizeof(m_u2Catchtype);	
	return u4Size;
}

//==============================================================================
// CExTable implementation
//

//------------------------------------------------------------------------------
void
CExTable::Read(CJStream& i_jstream)
{
	u2 Size;

	i_jstream >> Size;
	if(Size > 0)
		resize(Size);
	for(u2 Ind = 0; Ind < Size; Ind++)
	{
		(*this)[Ind].Read(i_jstream);
	}

}

//------------------------------------------------------------------------------
void
CExTable::Write(CJStream& i_jstream) const
{
	u2 u2Size = size();
	i_jstream << u2Size;
	for(u2 Ind = 0; Ind < u2Size; Ind++)
	{
		(*this)[Ind].Write(i_jstream);
	}
}

//------------------------------------------------------------------------------
u4
CExTable::GetSize() const
{
	u4 u4Size = sizeof(u2);
	for(u2 Ind = 0; Ind < size(); Ind++)
	{
		u4Size += (*this)[Ind].GetSize();
	}
	return u4Size;
}

//==============================================================================
// CConstantValueAttribute implementation
//

//------------------------------------------------------------------------------
CConstantValueAttribute::CConstantValueAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
{
	u2 u2CodeNameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("ConstantValue"));
	m_u2NameInd = u2CodeNameInd;
	m_u2ConstantInd = 0;
}

CConstantValueAttribute::~CConstantValueAttribute()
{
}

//------------------------------------------------------------------------------
void
CConstantValueAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2ConstantInd;
}

//------------------------------------------------------------------------------
void
CConstantValueAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2ConstantInd;
}

//------------------------------------------------------------------------------
u4
CConstantValueAttribute::GetSize() const
{
	u4 u4Size = CAttributeInfo::GetSize();
	u4Size += sizeof(m_u2ConstantInd);
	return u4Size;
}


//==============================================================================
// CCodeAttribute implementation
//

//------------------------------------------------------------------------------
CCodeAttribute::CCodeAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
, m_Attribs(i_pClassFile)
{
	m_u2MaxStack	= 0;
	m_u2MaxLocals	= 0;
	m_u4CodeLength	= 0;

		//TODO: The next line must be changed!!! see TODO #6 in the header file.
	u2 u2CodeNameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("Code"));
	m_u2NameInd = u2CodeNameInd;
	m_pu1Code = NULL;
}

CCodeAttribute::~CCodeAttribute()
{
	delete m_pu1Code;
}

//------------------------------------------------------------------------------
void
CCodeAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream	>>	m_u2MaxStack
				>>	m_u2MaxLocals
				>>	m_u4CodeLength;
	
	m_pu1Code = new u1[m_u4CodeLength];
	i_jstream.Read((void*)m_pu1Code, m_u4CodeLength);

	m_ExTable.Read(i_jstream);
	m_Attribs.Read(i_jstream);
}

//------------------------------------------------------------------------------
void
CCodeAttribute::Write(CJStream& i_jstream) const
{
	//Calculate length
	const u4 u4Length = GetLength() - CAttributeInfo::SizeOf();
	i_jstream	<<	m_u2NameInd
				<<	u4Length;
	i_jstream	<<	m_u2MaxStack
				<<	m_u2MaxLocals
				<<	m_u4CodeLength;
	
	i_jstream.Write((void*)m_pu1Code, m_u4CodeLength);

	m_ExTable.Write(i_jstream);
	m_Attribs.Write(i_jstream);
}

//------------------------------------------------------------------------------
u4
CCodeAttribute::GetSize() const
{
	return GetLength();
}

//------------------------------------------------------------------------------
u4
CCodeAttribute::GetLength() const
{
	u4 u4Length	= CAttributeInfo::SizeOf()
				+ sizeof(m_u2MaxStack)		// max_stack counter
				+ sizeof(m_u2MaxLocals)		// max_locals counter
				+ sizeof(m_u4CodeLength)	// code_length counter
				+ sizeof(u2)				// exception_table_length counter
				+ sizeof(u2);				// attributes_count counter
	u4Length += m_ExTable.size() * 8;
	u4Length += m_u4CodeLength;
	for(int i = 0; i < m_Attribs.size(); i++)
	{
		u4Length += m_Attribs[i]->GetLength();
	}
	return u4Length;
}

void
CCodeAttribute::SetMaxLocals(int i_MaxLocals)
{
	if (i_MaxLocals > 65535) {
		// You exceeded the max number of locals
		throw CJClassFileException(CJClassFileException::X_BAD_VALUE);
	}

	m_u2MaxLocals = i_MaxLocals;
}

void
CCodeAttribute::SetMaxStack(int i_MaxStack)
{
	if (i_MaxStack > 65535) {
		// You exceeded the max amount of stack
		throw CJClassFileException(CJClassFileException::X_BAD_VALUE);
	}
	m_u2MaxStack = i_MaxStack;
}

//------------------------------------------------------------------------------
void
CCodeAttribute::SetCode(u4 i_u4CodeLength, u1* i_pu1Code)
{
	if (i_u4CodeLength > 65536) {
		// You exceeded the max amount of code
		throw CJClassFileException(CJClassFileException::X_BAD_VALUE);
	}

	if(NULL != m_pu1Code)
	{
		delete m_pu1Code;
	}
	m_u4CodeLength = i_u4CodeLength;
	m_pu1Code = i_pu1Code;
}

//------------------------------------------------------------------------------
CLineNumberTableAttribute*
CCodeAttribute::GetLineNumbers()
{
	for(CJAttribs::iterator iter = m_Attribs.begin(); iter < m_Attribs.end(); iter++)
	{
		if(*(*iter)->GetName() == "LineNumberTable")
			return (CLineNumberTableAttribute*)*iter;
	}
	return NULL;
}

//------------------------------------------------------------------------------
CLocalVariableTableAttribute*
CCodeAttribute::GetLocalVariables()
{
	for(CJAttribs::iterator iter = m_Attribs.begin(); iter < m_Attribs.end(); iter++)
	{
		if(*(*iter)->GetName() == "LocalVariableTable")
			return (CLocalVariableTableAttribute*)*iter;
	}
	return NULL;
}

//------------------------------------------------------------------------------
CLocalVariableTypeTableAttribute* 
CCodeAttribute::GetLocalVariableTypes()
{
	for(CJAttribs::iterator iter = m_Attribs.begin(); iter < m_Attribs.end(); iter++)
	{
		if(*(*iter)->GetName() == "LocalVariableTypeTable")
			return (CLocalVariableTypeTableAttribute*)*iter;
	}
	return NULL;
}


//==============================================================================
// CExceptionsAttribute implementation
// [1] 4.7.5

//------------------------------------------------------------------------------
CExceptionsAttribute::CExceptionsAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
{
	//TODO: The next line must be changed!!! see TODO #6 in the header file.
	u2 u2NameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("Exceptions"));
	m_u2NameInd = u2NameInd;
}

//------------------------------------------------------------------------------
CExceptionsAttribute::~CExceptionsAttribute()
{
	;
}

//------------------------------------------------------------------------------
void	
CExceptionsAttribute::Read(CJStream& i_jstream)
{
	u2 ExceptionInd;
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2Number;
	m_IndexTable.resize(m_u2Number);
	for(u2 Ind = 0; Ind < m_u2Number; Ind++)
	{
		i_jstream >> ExceptionInd;
		m_IndexTable[Ind] = ExceptionInd;
	}
}

//------------------------------------------------------------------------------
void	
CExceptionsAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2Number;
	for(u2 Ind = 0; Ind < m_u2Number; Ind++)
	{
		i_jstream << m_IndexTable[Ind];
	}
}

//------------------------------------------------------------------------------
u4		
CExceptionsAttribute::GetSize() const
{
	return GetLength();
}

//------------------------------------------------------------------------------
u4		
CExceptionsAttribute::GetLength() const
{
	u4 u4Length = CAttributeInfo::SizeOf()		// AttributeInfo (always 6)
				+ sizeof(u2)					// Counter size
				+ sizeof(u2) * (m_u2Number);	// All table entries
	return u4Length;
}

//------------------------------------------------------------------------------
CCPClassInfo*	
CExceptionsAttribute::GetClassInfo(u2 i_u2Ind)
{
	CConstPool* pcp = m_pClassFile->GetConstPool();
	return (CCPClassInfo*)(*pcp)[i_u2Ind];
}

//------------------------------------------------------------------------------
void			
CExceptionsAttribute::Add(u2 i_u2ClassInd)
{
	m_IndexTable.push_back(i_u2ClassInd);
	m_u2Number++;
}

//==============================================================================
// CLineNumberAttribute implementation
//

//------------------------------------------------------------------------------
CLineNumberTableAttribute::CLineNumberTableAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
{
	u2 u2NameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("LineNumberTable"));
	m_u2NameInd = u2NameInd;
	m_u2TableLength = 0;
}

CLineNumberTableAttribute::~CLineNumberTableAttribute()
{
	for(CLineNumberTable::iterator iter = m_LineNumberTable.begin();
		iter < m_LineNumberTable.end(); iter++)
	{
		delete *iter;
	}
}

//------------------------------------------------------------------------------
void	
CLineNumberTableAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2TableLength;
	m_LineNumberTable.resize(m_u2TableLength);
	for(int i = 0; i < m_u2TableLength; i++)
	{
		u2	u2StartPC;
		u2	u2LineNumber;
		i_jstream >> u2StartPC >> u2LineNumber;
		m_LineNumberTable[i] = new CLineNumberInfo(u2StartPC, u2LineNumber);
	}
}

//------------------------------------------------------------------------------
void	
CLineNumberTableAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2TableLength;
	for(int i = 0; i < m_u2TableLength; i++)
	{
		i_jstream << m_LineNumberTable[i]->GetStartPC()
			      << m_LineNumberTable[i]->GetLineNumber();
	}
}

//------------------------------------------------------------------------------
u4		
CLineNumberTableAttribute::GetSize() const
{
	return GetLength();
}

//------------------------------------------------------------------------------
u4		
CLineNumberTableAttribute::GetLength() const
{
	u4 u4Length = CAttributeInfo::SizeOf()
		        + sizeof(u2)
				+ m_u2TableLength * sizeof(u2) * 2;
	return u4Length;
}


//==============================================================================
// CLocalVariableTableAttribute implementation
//
CLocalVariableTable::CLocalVariableTable(const CLocalVariableTable& i_LocalVars)
{
	*this = i_LocalVars;
}

CLocalVariableTable::~CLocalVariableTable()
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		delete *iter;
	}
}

CLocalVariableTable&
CLocalVariableTable::operator = (const CLocalVariableTable& i_LocalVars)
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		delete *iter;
	}
	if(!i_LocalVars.empty())
	{
		const_iterator iterIn;
		clear();
		for(iterIn = i_LocalVars.begin(); iterIn != i_LocalVars.end(); iterIn++)
		{
			push_back(new CLocalVariableInfo(**iterIn));
		}
	}
	return *this;
}

//------------------------------------------------------------------------------
CLocalVariableTableAttribute::CLocalVariableTableAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
{
	//TODO: The next line must be changed!!! see TODO #6 in the header file.
	u2 u2NameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("LocalVariableTable"));
	m_u2NameInd = u2NameInd;
	m_u2TableLength = 0;
	m_LocalVariableTable.clear();
}

//------------------------------------------------------------------------------
CLocalVariableTableAttribute::~CLocalVariableTableAttribute()
{
}

//------------------------------------------------------------------------------
void	
CLocalVariableTableAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2TableLength;
	for(int i = 0; i < m_u2TableLength; i++)
	{
		u2	u2StartPC;
		u2	u2Length;
		u2	u2NameIndex;
		u2	u2DescriptorIndex;
		u2	u2Index;
		i_jstream	>> u2StartPC
					>> u2Length
					>> u2NameIndex
					>> u2DescriptorIndex
					>> u2Index;
		m_LocalVariableTable.push_back(new CLocalVariableInfo(u2StartPC, u2Length,
			                               u2NameIndex, u2DescriptorIndex, u2Index));
	}
}

//------------------------------------------------------------------------------
void	
CLocalVariableTableAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2TableLength;
	for(int i = 0; i < m_u2TableLength; i++)
	{
		i_jstream	<< m_LocalVariableTable[i]->GetStartPC()
					<< m_LocalVariableTable[i]->GetLength()
					<< m_LocalVariableTable[i]->GetNameIndex()
					<< m_LocalVariableTable[i]->GetDescriptorIndex()
					<< m_LocalVariableTable[i]->GetIndex();
	}
}
//------------------------------------------------------------------------------
u4		
CLocalVariableTableAttribute::GetSize() const
{
	return GetLength();
}

//------------------------------------------------------------------------------
u4		
CLocalVariableTableAttribute::GetLength() const
{
	u4 u4Length = CAttributeInfo::SizeOf()
		        + sizeof(u2)
				+ m_u2TableLength * sizeof(u2) * 5;
	return u4Length;
}

//==============================================================================
// CLocalVariableTypeTableAttribute implementation
//

CLocalVariableTypeTable::CLocalVariableTypeTable(const CLocalVariableTypeTable& i_LocalVars)
{
	*this = i_LocalVars;
}

CLocalVariableTypeTable::~CLocalVariableTypeTable()
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		delete *iter;
	}
}

CLocalVariableTypeTable& 
CLocalVariableTypeTable::operator = (const CLocalVariableTypeTable& i_LocalVars)
{
	for(iterator iter = begin(); iter != end(); iter++)
	{
		delete *iter;
	}
	if(!i_LocalVars.empty())
	{
		const_iterator iterIn;
		clear();
		for(iterIn = i_LocalVars.begin(); iterIn != i_LocalVars.end(); iterIn++)
		{
			push_back(new CLocalVariableTypeInfo(**iterIn));
		}
	}
	return *this;
}

//------------------------------------------------------------------------------
CLocalVariableTypeTableAttribute::CLocalVariableTypeTableAttribute(CJClassFile* i_pClassFile)
: CAttributeInfo(i_pClassFile)
{
	//TODO: The next line must be changed!!! see TODO #6 in the header file.
	u2 u2NameInd = i_pClassFile->GetConstPool()->Add(new CCPUtf8Info("LocalVariableTypeTable"));
	m_u2NameInd = u2NameInd;
	m_u2TableLength = 0;
	m_LocalVariableTypeTable.clear();
}

//------------------------------------------------------------------------------
CLocalVariableTypeTableAttribute::~CLocalVariableTypeTableAttribute()
{
}

//------------------------------------------------------------------------------
void	
CLocalVariableTypeTableAttribute::Read(CJStream& i_jstream)
{
	CAttributeInfo::Read(i_jstream);
	i_jstream >> m_u2TableLength;
	for(int i = 0; i < m_u2TableLength; i++)
	{
		u2	u2StartPC;
		u2	u2Length;
		u2	u2NameIndex;
		u2	u2SignatureIndex;
		u2	u2Index;
		i_jstream	>> u2StartPC
					>> u2Length
					>> u2NameIndex
					>> u2SignatureIndex
					>> u2Index;
		m_LocalVariableTypeTable.push_back(new CLocalVariableTypeInfo(u2StartPC, u2Length,
			                               u2NameIndex, u2SignatureIndex, u2Index));
	}
}

//------------------------------------------------------------------------------
void	
CLocalVariableTypeTableAttribute::Write(CJStream& i_jstream) const
{
	CAttributeInfo::Write(i_jstream);
	i_jstream << m_u2TableLength;
	for(int i = 0; i < m_u2TableLength; i++)
	{
		i_jstream	<< m_LocalVariableTypeTable[i]->GetStartPC()
					<< m_LocalVariableTypeTable[i]->GetLength()
					<< m_LocalVariableTypeTable[i]->GetNameIndex()
					<< m_LocalVariableTypeTable[i]->GetSignatureIndex()
					<< m_LocalVariableTypeTable[i]->GetIndex();
	}
}
//------------------------------------------------------------------------------
u4		
CLocalVariableTypeTableAttribute::GetSize() const
{
	return GetLength();
}

//------------------------------------------------------------------------------
u4		
CLocalVariableTypeTableAttribute::GetLength() const
  {
	u4 u4Length = CAttributeInfo::SizeOf()
		        + sizeof(u2)
				+ m_u2TableLength * sizeof(u2) * 5;
	return u4Length;
}

//==============================================================================
// CSourceFileAttribute implementation
//

//------------------------------------------------------------------------------
CSourceFileAttribute::CSourceFileAttribute(CJClassFile* i_pClassFile, const CCPUtf8Info& i_Utf8Info)
: CStringAttribute(i_pClassFile, CCPUtf8Info("SourceFile"), i_Utf8Info)
{}

//==============================================================================
// CSourceFileAttribute implementation
//

//------------------------------------------------------------------------------
CSourceDirAttribute::CSourceDirAttribute(CJClassFile* i_pClassFile, const CCPUtf8Info& i_Utf8Info)
: CStringAttribute(i_pClassFile, CCPUtf8Info("SourceDir"), i_Utf8Info)
{}

//==============================================================================
// CJClassFile implementation
//

//------------------------------------------------------------------------------
// Construction/Destruction
//------------------------------------------------------------------------------
//
CJClassFile::CJClassFile()
{
	m_u4Magic = JAVA_MAGIC;
	m_u2MinorVersion	= CJClassFile::MinorVersion;
	m_u2MajorVersion	= CJClassFile::MajorVersion;
	m_u2AccessFlags		= 0;
	m_u2ThisClass		= 0;	
	m_u2SuperClass		= 0;	
	m_pConstPool		= new CConstPool;
    m_pInterfaces		= new CJInterfaces;
	m_pMethods			= new CJMethods(this);
	m_pAttribs			= new CJAttribs(this);
	m_pFields			= new CJFields(this);

}

//------------------------------------------------------------------------------
//
CJClassFile::~CJClassFile()
{
	delete m_pConstPool;
	delete m_pInterfaces;
	delete m_pMethods;
	delete m_pAttribs;
	delete m_pFields;
}

//------------------------------------------------------------------------------
//
void
CJClassFile::Read(CJStream& i_jstream)
{
	i_jstream	>>	m_u4Magic;
	if(0xCAFEBABE != m_u4Magic)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_MAGIC);
	}
	i_jstream	>>	m_u2MajorVersion;
	if(CJClassFile::MajorVersion < m_u2MajorVersion)
	{
		throw CJClassFileException(CJClassFileException::X_BAD_VERSION);
	}
	i_jstream	>>	m_u2MinorVersion;
	m_pConstPool->Read(i_jstream);
	i_jstream	>>	m_u2AccessFlags
				>>	m_u2ThisClass
				>>	m_u2SuperClass;	
	m_pInterfaces->Read(i_jstream);
	m_pFields->Read(i_jstream);
	m_pMethods->Read(i_jstream);
	m_pAttribs->Read(i_jstream);
}

//------------------------------------------------------------------------------
void
CJClassFile::Write(CJStream& i_jstream) const
{
	i_jstream	<<	m_u4Magic
				<<	m_u2MajorVersion
				<<	m_u2MinorVersion;
	m_pConstPool->Write(i_jstream);
	i_jstream	<<	m_u2AccessFlags
				<<	m_u2ThisClass
				<<	m_u2SuperClass;	
	m_pInterfaces->Write(i_jstream);
	m_pFields->Write(i_jstream);
	m_pMethods->Write(i_jstream);
	m_pAttribs->Write(i_jstream);
}

//------------------------------------------------------------------------------
u4		
CJClassFile::GetSize() const
{
	u4 u4Size   = sizeof(m_u4Magic)
				+ sizeof(m_u2MajorVersion)
				+ sizeof(m_u2MinorVersion)
				+ sizeof(m_u2AccessFlags)
				+ sizeof(m_u2ThisClass)
				+ sizeof(m_u2SuperClass);
	u4Size += m_pConstPool->GetSize();
	u4Size += m_pInterfaces->GetSize();	
	u4Size += m_pFields->GetSize();	
	u4Size += m_pMethods->GetSize();
	u4Size += m_pAttribs->GetSize();

	return u4Size;
}

//= End of JClassFileCPP =======================================================
