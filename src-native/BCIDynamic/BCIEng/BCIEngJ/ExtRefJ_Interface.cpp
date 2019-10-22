 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: ExtRefJ_Interface.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// ExtRefJ_Interface.cpp
// Started 03/12/99
//
//------------------------------------------------------------------------------
// Description:
// Java external reference for an interface
//
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "ExtRefJ_Interface.h"

//==============================================================================
// CExtRefJ implementation

//------------------------------------------------------------------------------
// Construction/destruction
//
CExtRefJ_Interface::CExtRefJ_Interface(
	CSTR i_szClass, CSTR i_szMethod, CSTR i_szSignature
)
: m_strClass(i_szClass)
, m_strMethod(i_szMethod)
, m_strSignature(i_szSignature)
{
	m_u2ClassName		= 0;
	m_u2MethodName		= 0;
	m_u2Signature		= 0;
	m_u2ClassRef		= 0;
	m_u2NameAndType		= 0;
	m_u2InterfaceMethodRef = 0;
}


//------------------------------------------------------------------------------
// InjectMetaData
// In:
//	i_Module		- the hosting module reference
// Out:
// Returns:
// Throws:
//
// Modify the module metadata
// (in this case constant pool) to make the reference visible to 
// the module.
//
void			
CExtRefJ_Interface::InjectMetaData(CModule& i_Module)
{
	// ToDo: type check somehow?
	CModuleJ&		module = (CModuleJ&)i_Module;
	CJClassFile&	jclass = module.GetClass();
	CConstPool*		pconst = jclass.GetConstPool();
	
	// Add class reference
	m_u2ClassName = pconst->Add(new CCPUtf8Info(m_strClass.c_str()));
	m_u2ClassRef  = pconst->Add(new CCPClassInfo(m_u2ClassName));

	//
	m_u2Signature   = pconst->Add(new CCPUtf8Info(m_strSignature.c_str()));
	m_u2MethodName	= pconst->Add(new CCPUtf8Info(m_strMethod.c_str()));
	m_u2NameAndType = pconst->Add(new CCPNameAndTypeInfo(m_u2MethodName, m_u2Signature));

	// Add Interface method ref
	m_u2InterfaceMethodRef = pconst->Add(new CCPInterfaceMethodrefInfo(m_u2ClassRef, m_u2NameAndType));
}

//------------------------------------------------------------------------------
// CreateInstruction
// In:
// Out:
// Returns:
//	CInstruction*	- pointer to the instruction to injecto into the 
//					  instrumented code
// Throws:
//	
// This method returns pointer to an instruction that can be inserted into
// the instruction list as a call instruction for this reference. 
// In the most common case this method will be used by an instrumentetion
// engine to insert runtime calls into the instrumented code.
//
// ToDo: implement
CInstruction* 
CExtRefJ_Interface::CreateInstruction() const 
{
	return NULL;
}

//------------------------------------------------------------------------------
string
CExtRefJ_Interface::ToString() const
{
	string strRet = m_strClass;
	strRet += ".";
	strRet += m_strMethod; 
	strRet += m_strSignature;
	return strRet;
}

//= End of ExtRefJ_Interface.cpp ===============================================
