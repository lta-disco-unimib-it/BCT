/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: JClassFile.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// JClassFile.h
//
// Java class file
//
// Based on JVM spec from:
// Tim Lindholm and Frank Yellin "JVM Spec." [1]
//
//------------------------------------------------------------------------------
// Remarks:
// 
// Things to do:
// 1. CCPUtf8Info   should work with a class CUtf8Info (not with char*)
// 5. Implement Cleanup method for all elements of JClassFile
// 6. Cache most used cp entries (common attribute names etc.)
//==============================================================================
#ifndef _JCLASSFILE_H
#define _JCLASSFILE_H
#include <vector>
#ifndef _COMMONDEF_H
#   include "CommonDef.h"
#endif
#ifndef _JAVADEF_H
#   include "JavaDef.h"
#endif
#ifndef	_JSTREAM_H
#   include "JStream.h"
#endif
//#ifndef _CONTAINER_H
//#include "container.h"
//#endif

USE_NAMESPACE(std);

class _EXT_REF CConstPool;
class _EXT_REF CCPInfo;
class _EXT_REF CCPUtf8Info;
class _EXT_REF CCPIntegerInfo;
class _EXT_REF CCPLongInfo;
class _EXT_REF CCPFloatInfo;
class _EXT_REF CCPDoubleInfo;
class _EXT_REF CCPStringInfo;
class _EXT_REF CCPNameAndTypeInfo;
class _EXT_REF CCPMethodrefInfo;
class _EXT_REF CCPInterfaceMethodrefInfo;
class _EXT_REF CInterfaceInfo;
class _EXT_REF CCPClassInfo;
class _EXT_REF CCPFieldrefInfo;

class _EXT_REF CJInterfaces;
class _EXT_REF CJFields;
class _EXT_REF CJMethods;	
class _EXT_REF CJAttribs;
class _EXT_REF CJException;
class _EXT_REF CJClassFile;
class _EXT_REF CJClassFileException;

class _EXT_REF CFieldInfo;
class _EXT_REF CJMethodInfo;
class _EXT_REF CExTable;
class _EXT_REF CSourceFileAttribute;
class _EXT_REF CSourceDirAttribute;

class _EXT_REF CAttributeInfo;
class _EXT_REF CCodeAttribute;
class _EXT_REF CStringAttribute;

class CConstantValueAttribute;
class CExceptionsAttribute;
class CLineNumberTableAttribute;
class CLocalVariableTableAttribute;
class CLocalVariableTypeTableAttribute;

//------------------------------------------------------------------------------
// Container classes
//
typedef _EXT_REF vector <class CCPInfo *> contCP_t;
typedef _EXT_REF vector <class CInterfaceInfo *> contInterfs_t;
typedef _EXT_REF vector <class CFieldInfo *> contFields_t;
typedef _EXT_REF vector <class CJMethodInfo *> contMethods_t;
typedef _EXT_REF vector <class CAttributeInfo *> contAttribs_t;
typedef _EXT_REF vector <class CJException> contExceptions_t;

typedef	union
{
	struct{u4 l; u4 h;} bytes;
	int64_t i64;
	double d;
} u64_t;

//------------------------------------------------------------------------------
// Constant Pool Info
//
//TODO: implement SafeCopy
//TODO: implement operator = and copy constructor
class CCPInfo
{
	friend class CConstPool;
public:

	CCPInfo(u1 i_u1Tag = CONSTANT_Unknown);
	CCPInfo(CCPInfo& i_cpinfo);
	virtual ~CCPInfo();

	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const {return false;}
	virtual u1*		GetInfo();
	u1		GetTag()	const
	{
		return m_u1Tag;
	}
	u2		GetCpIndex() const	
	{
		return m_u2CpIndex;
	}

	// Type control:
	operator CCPUtf8Info* ();
	operator CCPIntegerInfo* ();
	operator CCPFloatInfo* ();
	operator CCPLongInfo* ();
	operator CCPDoubleInfo* ();
	operator CCPClassInfo* ();
	operator CCPStringInfo* ();
	operator CCPMethodrefInfo* ();
	operator CCPInterfaceMethodrefInfo* ();
	operator CCPNameAndTypeInfo* ();
	operator CCPFieldrefInfo* ();

	//TODO: implement other type casting operators here

protected:
    u1		m_u1Tag;
    u1*		m_pu1Info;

private:
	u2		m_u2CpIndex;	// Index in the constant pool
};

//------------------------------------------------------------------------------
// Constant pool: UTF8 Character Stream Info
//

class CCPUtf8Info : public CCPInfo
{
public:
	CCPUtf8Info();
	CCPUtf8Info(const CCPUtf8Info& i_utf8Info);
	CCPUtf8Info(const char* i_szString);
	virtual ~CCPUtf8Info();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pcpinfo) const;

	u2		GetLength() const
	{
		return m_u2Length;
	}

	u1*		GetBytes() const	//TODO: This is not quite legal!!!
	{
		return m_pu1Bytes;
	}
	bool IsEmpty()const {return GetLength() == 0;}

	bool operator == (CSTR i_str) const;
	const CCPUtf8Info& operator = (const CCPUtf8Info& i_utf8)
	{
		m_u2Length = i_utf8.GetLength();
		m_pu1Bytes = new u1[m_u2Length];
		memcpy(m_pu1Bytes, i_utf8.GetBytes(), m_u2Length);
		return *this;
	}
	operator string ()
	{
		// This is expensive, use at your own risk!
		return string((CSTR)GetBytes(), GetLength());
	}

protected:
private:
    u2		m_u2Length;				// Number of bytes in the string
    u1*		m_pu1Bytes;				// bytes[length] stored in big-endian format
};

//------------------------------------------------------------------------------
// Constant pool: Integer Info
//
class CCPIntegerInfo : public CCPInfo
{
public:
	CCPIntegerInfo(u4 i_u4Bytes = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u4		GetBytes()	const
	{
		return m_u4Bytes;
	}
	bool operator == (const CCPIntegerInfo& i_ccpinteger) const;
protected:
private:
	u4		m_u4Bytes;
};

//------------------------------------------------------------------------------
// Constant pool: float info
//
class CCPFloatInfo : public CCPInfo
{
public:
	CCPFloatInfo(float i_float = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u4		GetBytes()
	{
		return (u4)m_float;
	}
	float	GetFloat() const
	{
		return m_float;
	}
	bool operator == (const CCPFloatInfo& i_ccpfloat) const;
	
protected:

private:
	float m_float;
};

//------------------------------------------------------------------------------
// Constant pool: Long info
//

class CCPLongInfo : public CCPInfo
{
public:
	CCPLongInfo(int64_t arg = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo)const;
	int64_t GetLong() const
	{
		return m_int64;
	}
	bool operator == (const CCPLongInfo& i_ccplong) const;
protected:

private:
	int64_t m_int64;
};

// Stream-out operator for a Long
_EXT_REF ostream& operator<<(ostream& s, int64_t l);

//------------------------------------------------------------------------------
// Constant pool: Double info
//
class CCPDoubleInfo : public CCPInfo
{
public:
	CCPDoubleInfo(double i_double = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	double GetDouble() const
	{
		return m_double;
	}
	bool operator == (const CCPDoubleInfo& i_ccpdouble) const;

protected:

private:
	double m_double;
};

//------------------------------------------------------------------------------
// Constant pool: Class info
//
class CCPClassInfo : public CCPInfo
{
public:
	CCPClassInfo(u2 i_u2ClassInd = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetClassInd() const { return m_u2ClassInd;}
	bool operator == (const CCPClassInfo& i_ccpclass) const;
protected:

private:
	u2	m_u2ClassInd;
};

//------------------------------------------------------------------------------
// Constant pool: String info
//
class CCPStringInfo : public CCPInfo
{
public:
	CCPStringInfo(u2 i_u2String = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetStringInd() const
	{
		return	m_u2StringInd;
	}
	bool operator == (const CCPStringInfo& i_ccpstr) const;

protected:

private:
	u2	m_u2StringInd;
};

//------------------------------------------------------------------------------
// Constant pool: Field reference info
//
class CCPFieldrefInfo : public CCPInfo
{
public:
	CCPFieldrefInfo(u2 i_u2Classind = 0, u2 i_u2NameAndTypeInd = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetClassInd() const
	{
		return	m_u2ClassInd;
	}
	u2	GetNameAndTypeInd() const
	{
		return	m_u2NameAndTypeInd;
	}
	bool operator == (const CCPFieldrefInfo& i_ccpfieldref) const;
protected:

private:
    u2		m_u2ClassInd;			// Class Index in the class_file.constant_pool
    u2		m_u2NameAndTypeInd;		// Name Index in the class_file.constant_pool

};

//------------------------------------------------------------------------------
// Constant pool: Method reference info
// 
class CCPMethodrefInfo : public CCPInfo
{
public:
	CCPMethodrefInfo(u2 i_u2ClassInd = 0, u2 i_u2NameAndTypeInd = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetClassInd() const
	{
		return	m_u2ClassInd;
	}
	u2	GetNameAndTypeInd() const
	{
		return	m_u2NameAndTypeInd;
	}
	bool operator == (const CCPMethodrefInfo& i_methodref) const;

protected:

private:
    u2		m_u2ClassInd;			// Class Index in the class_file.constant_pool
    u2		m_u2NameAndTypeInd;		// Name Index in the class_file.constant_pool
};

//------------------------------------------------------------------------------
// Constant pool: Interface method reference info
//
class CCPInterfaceMethodrefInfo : public CCPInfo
{
public:
	CCPInterfaceMethodrefInfo();
	CCPInterfaceMethodrefInfo(u2 i_u2ClassInd, u2 i_u2NameAndTypeInd);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetClassInd() const
	{
		return	m_u2ClassInd;
	}
	u2	GetNameAndTypeInd() const
	{
		return	m_u2NameAndTypeInd;
	}
	bool operator == (const CCPInterfaceMethodrefInfo& i_intmethodref) const;
protected:

private:
    u2		m_u2ClassInd;			// Class Index in the class_file.constant_pool
    u2		m_u2NameAndTypeInd;		// Name Index in the class_file.constant_pool

};

//------------------------------------------------------------------------------
// Constant pool: Name and Type info
//
class CCPNameAndTypeInfo : public CCPInfo
{
public:
	CCPNameAndTypeInfo();
	CCPNameAndTypeInfo(u2 i_u2NameInd, u2 i_u2DescriptorInd);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual bool Equals(CCPInfo* i_pinfo) const;
	u2	GetNameInd() const
	{
		return	m_u2NameInd;
	}
	u2	GetDescriptorInd() const
	{
		return	m_u2DescriptorInd;
	}
	bool operator == (const CCPNameAndTypeInfo& i_info) const;

protected:

private:
    u2		m_u2NameInd;			// Name Index in the class_file.constant_pool
    u2		m_u2DescriptorInd;		// Field or method descriptor
};

//------------------------------------------------------------------------------
// Attribute Info
// [1] 4.7
class CAttributeInfo
{
public:
	CAttributeInfo(CJClassFile* i_pClassFile);
	virtual ~CAttributeInfo();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	virtual u4	 GetLength() const
	{	// m_u4Length doesn't include m_u2NameInd
		// and m_u4Length
		return	m_u4Length + sizeof(m_u2NameInd) + sizeof(m_u4Length);	
	}
	u2 GetNameInd()
	{
		return m_u2NameInd;
	}
	u1*	GetInfo()
	{
		return m_pu1Info;
	}

	CCPUtf8Info* GetName();
	
	// This is the base size of CAttributeInfo.
	// It must be taken into account for all embedded attributes
	// when calculating the total attribute length. It is excluded from
	// top level attributes when size is stored in the ClassFile
	// See [1] 4.7.4
	u2	SizeOf() const {return sizeof(m_u2NameInd) + sizeof(m_u4Length);}

protected:
    u2			m_u2NameInd;
    u4			m_u4Length;
    u1*			m_pu1Info;

	CJClassFile*	m_pClassFile;
};

//------------------------------------------------------------------------------
// Java Attributes
// [1] 4.7
class CJAttribs : public contAttribs_t
{
public:
	CJAttribs(CJClassFile* 	i_pClassfile);
	~CJAttribs();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	u2		Add(CAttributeInfo* i_pAttrib);

protected:
	CJClassFile* 	m_pClassFile;
};

//------------------------------------------------------------------------------
// TODO: define attributes in a separate file?
//

//------------------------------------------------------------------------------
// Unknown attribute
// In case we encounter some custom-defined attribute in a class
//
class CUnknownAttribute : public CAttributeInfo
{
public:
	CUnknownAttribute(CJClassFile* i_pClassFile, u2 i_u2NameInd)
		: CAttributeInfo(i_pClassFile){m_u2NameInd = i_u2NameInd;}
	CUnknownAttribute(CJClassFile* i_pClassFile, CSTR i_szName);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
};

//------------------------------------------------------------------------------
// Exception
// [1] 
//

class CJException
{
public:
	CJException(u2 i_u2CatchType = 0, u2 i_u2StartPC = 0, u2 i_u2EndPC = 0, u2 i_u2HandlerPC = 0);
	void		Read(CJStream& i_jstream);
	void		Write(CJStream& i_jstream) const;
	u4			GetSize() const;

	u2 GetStartPC()		const	{return m_u2StartPC;}
	u2 GetEndPC()		const	{return m_u2EndPC;}
	u2 GetHandlerPC()	const	{return m_u2HandlerPC;}
	u2 GetCatchtype()	const	{return m_u2Catchtype;}
	void SetStartPC(u2 val)		{m_u2StartPC = val;}
	void SetEndPC(u2 val)		{m_u2EndPC = val;}
	void SetHandlerPC(u2 val)	{m_u2HandlerPC = val;}

	// Operators < and == to make STL vector happy.
	bool operator < (const CJException& i_exception) const
	{
		return m_u2StartPC < i_exception.GetStartPC();
	}
	bool operator == (const CJException& i_exception) const
	{
		return m_u2StartPC == i_exception.GetStartPC();
	}

private:
	u2			m_u2StartPC;
	u2			m_u2EndPC;
	u2			m_u2HandlerPC;
	u2			m_u2Catchtype;
};


//------------------------------------------------------------------------------
// Exception table (part of code)
//
class CExTable : public contExceptions_t
{
public:
	void		Read(CJStream& i_jstream);
	void		Write(CJStream& i_jstream) const;
	u4			GetSize() const;
};

//------------------------------------------------------------------------------
// String Attribute
// You can construct and put this attribute into a class file,
// but you should know that it will be treated as an Unknown attribute
// when you read a class file. To have as special treatment for
// your custom defined attribute you should inherit a new CJClassFile
// class and overload the attribute reading method.
//
// ToDo: inherit CSourceFileAttribute and  CSourceDirAttribute from
//               CStringAttribute
class CStringAttribute : public CAttributeInfo
{
public:	
	CStringAttribute(CJClassFile* i_pClassFile, 
		             const CCPUtf8Info& i_utf8Name, const CCPUtf8Info& i_utf8Value);
	
	u2			 GetInd() const { return m_u2Ind;}
	CCPUtf8Info* GetValue() const;
	
	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual u4		GetSize() const;

protected:
	u2 m_u2Ind;		
};

//------------------------------------------------------------------------------
// SourceFile Attribute 
// [1] 4.7.2
class CSourceFileAttribute : public CStringAttribute
{
public:
	CSourceFileAttribute(CJClassFile* i_pClassFile, const CCPUtf8Info& i_Utf8Info);
};

//------------------------------------------------------------------------------
// SourceDir Attribute 
// (Not defined in spec, but is widely used)
class CSourceDirAttribute : public CStringAttribute
{
public:
	CSourceDirAttribute(CJClassFile* i_pClassFile, const CCPUtf8Info& i_Utf8Info);
};

//------------------------------------------------------------------------------
// Constant value attribute
//
class CConstantValueAttribute : public CAttributeInfo
{
public:
	CConstantValueAttribute(CJClassFile* i_pClassFile);
	virtual ~CConstantValueAttribute();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	virtual u4   GetLength() const {return CAttributeInfo::GetLength() + sizeof(u2);}

	u2	GetConstantInd()	const	{return m_u2ConstantInd;}


protected:
	u2		m_u2ConstantInd;
};

//------------------------------------------------------------------------------
// Code attribute
//
class CCodeAttribute : public CAttributeInfo
{
public:
	CCodeAttribute(CJClassFile* i_pClassFile);
	virtual ~CCodeAttribute();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	virtual u4   GetLength() const;

	u2	GetMaxStack()	const	{return m_u2MaxStack;}
	u2	GetMaxLocals()	const	{return m_u2MaxLocals;}
	u4	GetCodeLength()	const	{return m_u4CodeLength;}

	u1*	GetCode()				{return m_pu1Code;}
	CExTable&	 GetExTable()	{return m_ExTable;}
	CJAttribs&	 GetAttribs()	{return m_Attribs;}

	void SetMaxStack(int i_MaxStack);
	void SetMaxLocals(int i_MaxLocals);
	void SetCode(u4 i_u4CodeLength, u1* i_pu1Code);

	CLineNumberTableAttribute* GetLineNumbers();
	CLocalVariableTableAttribute* GetLocalVariables();
	CLocalVariableTypeTableAttribute* GetLocalVariableTypes();

protected:
	u2			m_u2MaxStack;
	u2			m_u2MaxLocals;
	u4			m_u4CodeLength;
	u1*			m_pu1Code;
	CExTable	m_ExTable;
	CJAttribs	m_Attribs;
};

//------------------------------------------------------------------------------
// Exceptions attribute
//
typedef vector<u2> CIndexTable;

class CExceptionsAttribute : public CAttributeInfo
{
public:
	CExceptionsAttribute(CJClassFile* i_pClassFile);
	virtual ~CExceptionsAttribute();
	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual u4		GetSize() const;
	virtual u4		GetLength() const;

	u2	GetTableLength() { return m_u2Number; }
	u2	GetIndex(u4 i_u4Off) { return m_IndexTable[i_u4Off];}
	CCPClassInfo*	GetClassInfo(u2 i_u2Ind);
	void			Add(u2 i_u2ClassInd);

protected:
	u2			m_u2Number;				// Number of exceptions
	CIndexTable	m_IndexTable;			// Exceptions index table
};

//------------------------------------------------------------------------------
// LineNumberTable attribute
//
class CLineNumberInfo
{
public:
	CLineNumberInfo(u2 i_u2StartPC = 0, u2 i_u2LineNumber = 0)
	{
		m_u2StartPC = i_u2StartPC;
		m_u2LineNumber = i_u2LineNumber;
	}
	u2 GetStartPC() {return m_u2StartPC;}
	u2 GetLineNumber() {return m_u2LineNumber;}
	void SetStartPC(u2 pc) {m_u2StartPC = pc;}
	void SetLineNumber(u2 li) {m_u2LineNumber = li;}

protected:
	u2			m_u2StartPC;
	u2			m_u2LineNumber;
};

typedef vector<CLineNumberInfo*> CLineNumberTable;

class CLineNumberTableAttribute : public CAttributeInfo
{
public:
	CLineNumberTableAttribute(CJClassFile* i_pClassFile);
	~CLineNumberTableAttribute();
	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual u4	    GetSize() const;
	virtual u4		GetLength() const;
	u2 GetTableLength() const {return m_u2TableLength;}

	CLineNumberTable& GetLineNumberTable() {return m_LineNumberTable;}

protected:
	u2				 m_u2TableLength;
	CLineNumberTable m_LineNumberTable;
};

//------------------------------------------------------------------------------
// Local Variable Information
//
class CLocalVariableInfo
{
public:
	CLocalVariableInfo(u2 i_u2StartPC = 0, u2 i_u2Length = 0, u2 i_u2NameIndex = 0, 
		               u2 i_u2DescriptorIndex = 0, u2 i_u2Index = 0)
	{
		m_u2StartPC = i_u2StartPC;
		m_u2Length = i_u2Length;
		m_u2NameIndex = i_u2NameIndex;
		m_u2DescriptorIndex = i_u2DescriptorIndex;
		m_u2Index = i_u2Index;
	}

	CLocalVariableInfo(const CLocalVariableInfo& i_Info)
	{
		*this = i_Info;
	}

	u2	GetStartPC(){return m_u2StartPC;}
	u2	GetLength(){return m_u2Length;}
	u2	GetNameIndex(){return m_u2NameIndex;}
	u2	GetDescriptorIndex(){return m_u2DescriptorIndex;}
	u2	GetIndex(){return m_u2Index;}

	void SetStartPC(u2 i_u2StartPC){m_u2StartPC = i_u2StartPC;}
	void SetLength(u2 i_u2Length){m_u2Length = i_u2Length;}

private:
	u2	m_u2StartPC;
	u2	m_u2Length;
	u2	m_u2NameIndex;
	u2	m_u2DescriptorIndex;
	u2	m_u2Index;
};

//------------------------------------------------------------------------------
// Local Variable Table
class CLocalVariableTable : public vector<CLocalVariableInfo*>
{
public:
	CLocalVariableTable(){clear();}
	CLocalVariableTable(const CLocalVariableTable& i_LocalVars);
	~CLocalVariableTable();
	CLocalVariableTable& operator = (const CLocalVariableTable& i_LocalVars);
private:

};

//------------------------------------------------------------------------------
// Local Variable Table Attribute
//
class CLocalVariableTableAttribute  : public CAttributeInfo
{
public:
	CLocalVariableTableAttribute(CJClassFile* i_pClassFile);
	CLocalVariableTableAttribute(const CLocalVariableTableAttribute& i_Locals);
	~CLocalVariableTableAttribute();
	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual u4	    GetSize() const;
	virtual u4		GetLength() const;

	u2 GetTableLength() {return m_u2TableLength;}
	CLocalVariableTable&	GetLocalVariableTable(){return m_LocalVariableTable;}

	CLocalVariableTableAttribute& operator = (const CLocalVariableTableAttribute& i_Locals);

private:
	u2		m_u2TableLength;
	CLocalVariableTable m_LocalVariableTable;
};

//------------------------------------------------------------------------------
// Local Variable Type Information /New in Java 5/
//
class CLocalVariableTypeInfo
{
public:
	CLocalVariableTypeInfo(u2 i_u2StartPC = 0, u2 i_u2Length = 0, u2 i_u2NameIndex = 0, 
		               u2 i_u2SignatureIndex = 0, u2 i_u2Index = 0)
	{
		m_u2StartPC = i_u2StartPC;
		m_u2Length = i_u2Length;
		m_u2NameIndex = i_u2NameIndex;
		m_u2SignatureIndex = i_u2SignatureIndex;
		m_u2Index = i_u2Index;
	}

	CLocalVariableTypeInfo(const CLocalVariableInfo& i_Info)
	{
		*this = i_Info;
	}

	u2	GetStartPC(){return m_u2StartPC;}
	u2	GetLength(){return m_u2Length;}
	u2	GetNameIndex(){return m_u2NameIndex;}
	u2	GetSignatureIndex(){return m_u2SignatureIndex;}
	u2	GetIndex(){return m_u2Index;}

	void SetStartPC(u2 i_u2StartPC){m_u2StartPC = i_u2StartPC;}
	void SetLength(u2 i_u2Length){m_u2Length = i_u2Length;}

private:
	u2	m_u2StartPC;
	u2	m_u2Length;
	u2	m_u2NameIndex;
	u2	m_u2SignatureIndex;
	u2	m_u2Index;
};

//------------------------------------------------------------------------------
// Local Variable Type Table /New in Java 5/
class CLocalVariableTypeTable : public vector<CLocalVariableTypeInfo*>
{
public:
	CLocalVariableTypeTable(){clear();}
	CLocalVariableTypeTable(const CLocalVariableTypeTable& i_LocalVars);
	~CLocalVariableTypeTable();
	CLocalVariableTypeTable& operator = (const CLocalVariableTypeTable& i_LocalVars);
private:

};

//------------------------------------------------------------------------------
// Local Variable Type Table (LVTT) /New in Java 5/
//
class CLocalVariableTypeTableAttribute  : public CAttributeInfo
{
public:
	CLocalVariableTypeTableAttribute(CJClassFile* i_pClassFile);
	CLocalVariableTypeTableAttribute(const CLocalVariableTableAttribute& i_Locals);
	~CLocalVariableTypeTableAttribute();
	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual u4	    GetSize() const;
	virtual u4		GetLength() const;

	u2 GetTableLength() {return m_u2TableLength;}
	CLocalVariableTypeTable&	GetLocalVariableTypeTable(){return m_LocalVariableTypeTable;}

	CLocalVariableTypeTableAttribute& operator = (const CLocalVariableTypeTableAttribute& i_Locals);

private:
	u2		m_u2TableLength;
	CLocalVariableTypeTable m_LocalVariableTypeTable;
};

//------------------------------------------------------------------------------
// Interface information
//
class CInterfaceInfo
{
public:
	CInterfaceInfo(u2 i_u2Index = 0)
	{
		m_u2Index = i_u2Index;
	}
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;

	u2		GetIndex() const	{return m_u2Index;}
	bool operator == (const CInterfaceInfo i_interface) const
	{
		return(m_u2Index == i_interface.GetIndex());
	}

private:
	u2		m_u2Index;				// Inerface name index in the constant pool
};

//------------------------------------------------------------------------------
// Java Interfaces
//
class CJInterfaces : public	contInterfs_t
{
public:
	CJInterfaces();
	~CJInterfaces();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	u2 Find(CInterfaceInfo* i_pinterface) const;
	u2 Add(CInterfaceInfo* i_pinterface);
};


//------------------------------------------------------------------------------
// Field information
//
class CFieldInfo
{
public:
	CFieldInfo(CJClassFile*  i_pClassfile);
	CFieldInfo(CJClassFile*  i_pClassfile, u2 i_u2NameInd, 
		       u2 i_u2DescriptorInd, u2 i_u2AccessFlags = 0);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	u2	GetAccessFlags() {return m_u2AccessFlags;}
	u2	GetNameInd() const {return m_u2NameInd;}
	u2	GetDescriptorInd() const {return m_u2DescriptorInd;}
	CJAttribs& GetAttribs()	{return m_Attribs;}

    // Helpers
	CCPUtf8Info*	GetName();
	CCPUtf8Info*	GetDescriptor();

protected:
	u2				m_u2AccessFlags;		
    u2				m_u2NameInd;
    u2				m_u2DescriptorInd;
	CJAttribs		m_Attribs;				// Attributes container

	CJClassFile* 	m_pClassFile;
};

//------------------------------------------------------------------------------
// Java Fields
//
class CJFields : public	contFields_t
{
public:
	CJFields(CJClassFile*  i_pClassfile);
	~CJFields();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;
	u2	Add(CFieldInfo* i_pFieldInfo);

protected:
	CJClassFile* 	m_pClassFile;
};

//------------------------------------------------------------------------------
// Method information
//
class CJMethodInfo
{
public:
	CJMethodInfo(CJClassFile*  i_pClassfile);
	CJMethodInfo(CJClassFile*  i_pClassfile, CSTR i_Name, 
		        CSTR i_Descript, u2 i_u2Access);
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4	 GetSize() const;

	u2			GetAccessFlags() {return m_u2AccessFlags;}
	u2			GetNameInd() const {return m_u2NameInd;}
	u2			GetDescriptorInd() const {return m_u2DescriptorInd;}
	CJAttribs&	GetAttribs()	{return m_Attribs;}
	CJClassFile* GetClassFile() {return m_pClassFile;}

    // Helpers
	CCPUtf8Info*	GetName();
	CCPUtf8Info*	GetDescriptor();
	CCodeAttribute*	GetCode();
	void			SetCode(CCodeAttribute* i_pCode);
	CExceptionsAttribute* GetExceptions();

protected:
	u2				m_u2AccessFlags;		
    u2				m_u2NameInd;
    u2				m_u2DescriptorInd;
	CJAttribs		m_Attribs;				// Attributes container

	CJClassFile*  m_pClassFile;
};

//------------------------------------------------------------------------------
// Java Methods
// [1] 4.6
//
class CJMethods : public contMethods_t
{
public:
	CJMethods(CJClassFile*  i_pClassfile) : m_pClassFile(i_pClassfile){;}
	~CJMethods();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	u2			 Add(CJMethodInfo* i_pmethod);
private:
	CJClassFile* 	m_pClassFile;
};

//------------------------------------------------------------------------------
// Java Constant Pool
// [1] 4.4
//
class CConstPool : public contCP_t
{
public:
	// TODO: Copy constructor and assignment operator
	CConstPool();
	~CConstPool();
	virtual void Read(CJStream& i_jstream);
	virtual void Write(CJStream& i_jstream) const;
	virtual u4   GetSize() const;
	u2 Find(CCPInfo* i_pccpinfo) const;
	u2 Add(CCPInfo*	i_pinfo);

	// Helpers:
	CCPUtf8Info*	GetString(u2 i_u2StringInd);
	CCPUtf8Info*	GetClass(u2 i_u2ClassInd);
	CCPUtf8Info*	GetName(u2 i_u2NameAndTypeInd);
	CCPUtf8Info*	GetType(u2 i_u2NameAndTypeInd);
	CCPUtf8Info*	GetMethodClass(u2 i_u2MethodInd);
	CCPUtf8Info*	GetMethodName(u2 i_u2MethodInd);
	CCPUtf8Info*	GetMethodType(u2 i_u2MethodInd);
};

//------------------------------------------------------------------------------
// Java Class File
// [1] 4.1
//
class CJClassFile  
{
public:
	enum {MajorVersion = 3, MinorVersion = 46};	// TODO: comment on version 
	CJClassFile();
	virtual ~CJClassFile();

	virtual void	Read(CJStream& i_jstream);
	virtual void	Write(CJStream& i_jstream) const;
	virtual	u4		GetSize() const;

	u2				GetMinorVersion() const		{return m_u2MinorVersion;}
	u2				GetMajorVersion() const		{return m_u2MajorVersion;}
	u2				GetAccessFlags()  const		{return m_u2AccessFlags;}
	u2				GetThisClass()	  const		{return m_u2ThisClass;}
	u2				GetSuperClass()	  const		{return m_u2SuperClass;}
												
	CConstPool*		GetConstPool()				{return m_pConstPool;}
	CJInterfaces*	GetInterfaces()				{return m_pInterfaces;}
	CJFields*		GetFields()					{return m_pFields;}
	CJMethods*		GetMethods()				{return m_pMethods;}
	CJAttribs*		GetAttribs()				{return m_pAttribs;}

	void			Verify();

protected:
	u4				m_u4Magic;				// Magic number (0xBABECAFE)
	u2				m_u2MinorVersion;		// Minor version number
	u2				m_u2MajorVersion;		// Major version number
	CConstPool*		m_pConstPool;			// Constant pool
	u2				m_u2AccessFlags;		// Access flags
	u2				m_u2ThisClass;			// This class offset in const. pool
	u2				m_u2SuperClass;			// Super class offset in const. pool
	CJInterfaces*	m_pInterfaces;			// Interfaces container
	CJFields*		m_pFields;				// Fields container
	CJMethods*		m_pMethods;				// Methods container
	CJAttribs*		m_pAttribs;				// Attributes container

private:
};

//------------------------------------------------------------------------------
// Java Class File Exception
//
class CJClassFileException
{
public:
	enum
	{
		X_UNKNOWN,			// I don't know what it is, but it doesn't look good
		X_NOT_IMPLEMENTED,	// Unimplemented method call 
							//		(shouldn't happen when the project is done
		X_INTERNAL_ERROR,	// Internal error (kind of like unknown)
		X_BAD_MAGIC,		// Bad magic number in the header
		X_BAD_VERSION,		// Unsupported Java version
		X_BAD_CONSTANT,		// Bad constant tag
		X_BAD_FIELD,		// Field references constant out of CP range
		X_BAD_INTERFACE,	// Same as Bad field for interfaces
		X_BAD_METHOD,		// Same as Bsd interface for methods
		X_BAD_VALUE,		// Trying to assign incompatible value to a constant
		X_BAD_TYPECAST,		// Bad typecasting (e.g. assigning CCPInfo* having
							//		            IntegerInfo to Utf8Info)
		X_BAD_INDEX,		// Index out of bounds
		X_LAST				// Sentinel
	};

	CJClassFileException(unsigned i_reason)
	{
		m_reason = i_reason;
	}

	unsigned GetReason()
	{
		return m_reason;
	}

private:
	unsigned m_reason;
};

#endif // !defined(_JCLASSFILE_H)

//= End of JClassFile.h ========================================================
