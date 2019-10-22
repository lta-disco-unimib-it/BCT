 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: ExtRefJ_StatMethod.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// ExtRefJ_StatMethod.h
// Started 03/12/99
//
//==============================================================================
#ifndef _EXTRFJ_STATMETHOD_H
#define _EXTRFJ_STATMETHOD_H

#include "ModuleJ.h"

//==============================================================================
// CExtRefJ_StatMethod	-- Interface Method
// 
// ToDo:
// The following types of external references can be inherited:
// CExtRefJ_Class		-- Generic class method
// CExtRefJ_Native		-- Native Method
//

class CExtRefJ_StatMethod : public CExtRef
{
public:
	CExtRefJ_StatMethod(CSTR i_szClass, CSTR i_szMethod, CSTR i_szSignature);
	virtual ~CExtRefJ_StatMethod();	

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
	u2		m_u2ClassMethodRef;
};


#endif //defined _EXTRFJ_STATMETHOD_H
//= ExtRefJ_StatMethod.h =======================================================
