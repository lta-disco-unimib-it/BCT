 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngJTest.cpp,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

// BCIEngJTest.cpp
//
// This program tests the basic framework functionality - it parses and
// then emits modules without doing any BCI, to make sure we don't mess them up.
//
// USAGE: BCIEngJTest class_file
//
// Reports an error on stderr if there is a problem.
// See the function VerifyResults to extend the kinds of problems
// we check for. Currently we check that the size of the method didn't change,
// and that the "max stack" attribute is the same.
// This could be extended to check that the byte codes themselves are identical.
//
//==============================================================================
#pragma warning(disable:4786)
#include <iostream>
#include <string>
#include <vector>

#include "CommonDef.h"
#include "ModuleJ.h"
#include "JFileStream.h"

USE_NAMESPACE(std);

class CMethodInfo;
class CModuleInfo;
class CBCIEngJTest;
typedef vector<CMethodInfo> mtds_t;


//==============================================================================
class CMethodInfo
{
public:
	CMethodInfo(CMethodJ* i_pmeth)
	{
		m_strName = i_pmeth->GetName();
		CCodeAttribute* pcode = i_pmeth->GetCodeAttribute();
		if(pcode)
		{
			m_nStack = pcode->GetMaxStack();
			m_nSize = pcode->GetCodeLength();
		}
		else
		{
			m_nStack = m_nSize = 0;
		}
	}

	bool operator == (const CMethodInfo& i_mtdinf) const
	{
		bool bRet = m_strName == i_mtdinf.m_strName
				 && m_nStack  == i_mtdinf.m_nStack 
				 && m_nSize   == i_mtdinf.m_nSize;
		return bRet;
	}

	bool operator != (const CMethodInfo& i_mtdinf) const
	{
		return !(*this == i_mtdinf);
	}

	const char* GetName()const{ return m_strName.c_str();}
	const int GetStack()const {return m_nStack;}
	const int GetSize()const {return m_nSize;}

private:
	string	m_strName;
	int		m_nStack;
	int		m_nSize;
};




//==============================================================================
class CModuleInfo
{
public:
	CModuleInfo(CModuleJ* i_pmod)
	{
		m_strName = i_pmod->GetName();
		CMethods* pmethods = i_pmod->GetMethods();
		CMethods::iterator itr;
		for(itr = pmethods->begin(); itr != pmethods->end(); itr++)
		{
			CMethodInfo mtdinf((CMethodJ*)(*itr));
			m_Methods.push_back(mtdinf);
		}
	}
	const char* GetName(){return m_strName.c_str();}
	mtds_t& GetMethods(){return m_Methods;}

private:
	mtds_t m_Methods;
	string m_strName;
};


//==============================================================================
class CBCIEngJTest
{
public:
	CBCIEngJTest()
	{
		m_pmod = NULL;
		m_pinfBefore = NULL;
		m_pinfAfter = NULL;
	}

	~CBCIEngJTest()
	{
		delete m_pmod;
		delete m_pinfBefore;
		delete m_pinfAfter;
	}

	void OpenClass(const char* i_szClassName);
	void Instrument();
	void VerifyResults();
	void WriteClass(const char* i_szClassName);

private:
	CModuleJ*	m_pmod;
	CModuleInfo* m_pinfBefore;
	CModuleInfo* m_pinfAfter;
};


//------------------------------------------------------------------------------
void 
CBCIEngJTest::OpenClass(const char* i_szClassName)
{
	CJFileStream	InStream(i_szClassName);	// File stream
	CJStream		JStreamIn(&InStream);		// Java input stream
	CModuleJ*		pModuleJ = new CModuleJ;
	CJClassFile* pClass = new CJClassFile;      // Will be deleted in the module destructor
	pClass->Read(JStreamIn);
	pModuleJ->Open(pClass, true);
	pModuleJ->SetAccessFlags(pClass->GetAccessFlags());
	pModuleJ->Parse();
	m_pmod = pModuleJ;
	m_pinfBefore = new CModuleInfo(m_pmod);
}

//------------------------------------------------------------------------------
void 
CBCIEngJTest::Instrument()
{
	m_pmod->Emit();
	m_pinfAfter = new CModuleInfo(m_pmod);
	//m_pmod->Dump(cout);
}

//------------------------------------------------------------------------------
void 
CBCIEngJTest::VerifyResults()
{
	bool bPass = true;
	mtds_t mtdsBefore = m_pinfBefore->GetMethods();
	mtds_t mtdsAfter = m_pinfAfter->GetMethods();
	mtds_t::iterator itrBefore = mtdsBefore.begin();
	mtds_t::iterator itrAfter = mtdsAfter.begin();
	while(itrBefore != mtdsBefore.end() && itrAfter != mtdsAfter.end())
	{
		if(*itrBefore != *itrAfter)
		{
			bPass = false;
			cerr << "Verification error in "  
				 << m_pmod->GetName() << "." << itrBefore->GetName() << endl;
			cerr << "\tBefore: size:" << itrBefore->GetSize() 
				 << " stack: " << itrBefore->GetStack() << "\n"
				 << "\tAfter: size:" << itrAfter->GetSize() 
				 << " stack: " << itrAfter->GetStack() 
				 << endl;
		}
		itrBefore++;
		itrAfter++;
	}
	cout << "Module " << m_pinfBefore->GetName();
	if(bPass)
	{
		cout << " OK." << endl;
	}
	else
	{
		cout << " Failure." << endl;
	}

}

//==============================================================================
enum
{
	EXIT_OK = 0,
	EXIT_BAD_COMMAND = 1,
	EXIT_ERROR = 2,
	EXIT_LAST
};

int main(int argc, char* argv[])
{
	try
	{
		CBCIEngJTest eng;

		if(argc == 2)
		{
			string strInClass = argv[1];
			string strOutClass = strInClass;
			strOutClass += ".bci";
			eng.OpenClass(argv[1]);
			eng.Instrument();
			eng.VerifyResults();
			return EXIT_OK;
		}
		else
		{
			return EXIT_BAD_COMMAND;
		}
	}
	catch(const char* i_szError)
	{
		cerr << i_szError << endl;
		return EXIT_ERROR;
	}
	return 0;
}

//==============================================================================
