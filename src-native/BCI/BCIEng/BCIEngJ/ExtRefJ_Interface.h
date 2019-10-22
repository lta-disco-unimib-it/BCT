 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: ExtRefJ_Interface.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// ExtRefJ_Interface.h
// Started 03/12/99
//
//==============================================================================

#ifndef _EXTRFJ_INTERFACE_H
#define _EXTRFJ_INTERFACE_H

#include "ModuleJ.h"

//==============================================================================
// CExtRefJ_Interface	-- Interface Method
// 
// ToDo:
// The following types of external references can be inherited:
// CExtRefJ_Class		-- Generic class method
// CExtRefJ_Native		-- Native Method
// etc.

class CExtRefJ_Interface : public CExtRef
{
public:
	CExtRefJ_Interface(CSTR i_szClass, CSTR i_szMethod, CSTR i_szSignature);

	virtual void			InjectMetaData(CModule& i_Module);
	virtual CInstruction*	CreateInstruction() const;
	virtual string			ToString() const;

private:
	string	m_strClass;
	string	m_strMethod;
	string	m_strSignature;

	//- Java class internal references
	u2		m_u2ClassName;
	u2		m_u2MethodName;
	u2		m_u2Signature;
	u2		m_u2ClassRef;
	u2		m_u2NameAndType;
	u2		m_u2InterfaceMethodRef;
};

#endif
//= End of ExtRefJ_Interface.h =================================================
