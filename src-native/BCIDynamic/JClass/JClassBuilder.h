 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JClassBuilder.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// JClassBuilder.h
//
// Java class builder
//
//------------------------------------------------------------------------------
#ifndef _JCLASSBUILDER_H
#define _JCLASSBUILDER_H

#include "CommonDef.h"
#include "JavaHelpers.h"
#include "JClassFile.h"


class CJClassFile;
class CJClassBuilder;

//==============================================================================
// CJClassBuilder
//
//------------------------------------------------------------------------------
// Extends CJClass functionality with methods for adding class components:
// fields, methods, attributes and constants in the constant pool.
// Methods in this class may throw CJClassException.
// 
// Remarks:
// 
// Most of these operations return a pointer to the CPPInfo object
// that was created (of the appropriate subclass) rather than just
// the constant pool index of the newly-created component. If you
// want the index, you can get it from CCPInfo::GetCpIndex().
//
//------------------------------------------------------------------------------
class CJClassBuilder : public CJClassFile
{
public:
	CJClassBuilder();
	CJClassBuilder(u2 i_u2AccessFlags, CSTR i_szClass, CSTR i_szSuper);
	virtual ~CJClassBuilder();

	// Class inforamtion
	string GetThisClassName();
	string GetSuperClassName();
	string GetNameFromInterfaceIndex(u2 index);

	CCPNameAndTypeInfo* FindNameAndType(CSTR i_szName, CSTR i_szType = NULL) const;
	CCPFieldrefInfo* FindFieldRef(CSTR i_szName, CSTR i_szClass = NULL) const ;
	CCPMethodrefInfo* FindMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass = NULL) const;
	CCPClassInfo*	FindClass(CSTR i_szName) const;

	// Constant pool manipulation
	CCPStringInfo* CreateStringConstant(CSTR i_szString);
	CCPIntegerInfo* CreateIntegerConstant (JINTEGER i_int);
	CCPLongInfo* CreateLongConstant(JLONG  i_long);
	CCPFloatInfo* CreateFloatConstant(JFLOAT i_float);
	CCPDoubleInfo* CreateDoubleConstant(JDOUBLE i_double);

	// Class manipulation
	void SetSuperClass(CSTR i_szBase);
	void SetAccessFlags(u2 u2i_AcessFlags);
	u2   AddClassAttribute(CAttributeInfo* i_pattributenfo);
	u2   AddFieldAttribute(CAttributeInfo* i_pattributeinfo, CFieldInfo* i_pfieldinfo);
	u2   AddMethodAttribute(CAttributeInfo* i_pattributeinfo, CJMethodInfo* i_pjmethodinfo);
	CInterfaceInfo* AddInterface(CSTR i_szInterface);
	CFieldInfo* CreateField(u2 i_u2Access, CSTR i_szName, CJavaType& i_jtype);
	CJMethodInfo* CreateMethod(u2 i_u2Access, CSTR i_szName, CSTR i_szSignature);
	CJMethodInfo* CreateMethod(u2 i_u2Access, CJavaMethodName& i_jmn);
	CCPFieldrefInfo* CreateFieldRef(CFieldInfo* i_pfi);
	CCPMethodrefInfo* CreateMethodRef(CJMethodInfo* i_pmi);
	CCPInterfaceMethodrefInfo* CreateInterfaceMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass);
	CCPInterfaceMethodrefInfo* CreateInterfaceMethodRef(CJavaMethodName& i_jmn, CSTR i_szClass);
	CCPFieldrefInfo* CreateExtFieldRef(CSTR i_szName, CJavaType& i_jtype, CSTR i_szClass);
	CCPMethodrefInfo* CreateExtMethodRef(CSTR i_szName, CSTR i_szSignature, CSTR i_szClass);
	CCPMethodrefInfo* CreateExtMethodRef(CJavaMethodName& i_jmn, CSTR i_szClass);

	// Helpers
	CJMethodInfo* AddConstructor(CSTR i_szSignature);
	CJMethodInfo* AddDefaultConstructor();
	CJMethodInfo* AddStaticConstructor();

};

#endif // defined _JCLASSBUILDER_H
//= End of JClassBuilder.h =====================================================
