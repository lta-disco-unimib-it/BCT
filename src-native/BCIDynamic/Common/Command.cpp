 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: Command.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// Command.cpp
//------------------------------------------------------------------------------
// Command line parser
//==============================================================================
#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "Command.h"


//==============================================================================
// CFileInfo implementation
//
// TODO: add input verification for valid characters.
//------------------------------------------------------------------------------
void	
CFileInfo::SetName(CSTR i_szName)
{
	m_strName = i_szName;
}

//------------------------------------------------------------------------------
void	
CFileInfo::SetExt(CSTR i_szExt)
{
	m_strExt = i_szExt;
}

//------------------------------------------------------------------------------
void	
CFileInfo::SetPath(CSTR i_szPath)
{
	m_strPath = i_szPath;
}

//------------------------------------------------------------------------------
void	
CFileInfo::SetTitle(CSTR i_szTitle)
{
	// TODO: parse title
	m_strTitle = i_szTitle;
}


//------------------------------------------------------------------------------
void	
CFileInfo::MakeTitle()
{
	m_strTitle = m_strPath + m_strName + m_strExt;
}


//==============================================================================
// CValue implementation
//

//------------------------------------------------------------------------------
CValue::CValue(CSTR i_szVal)
{
	m_val.sz = strdup(i_szVal);
	m_type = CValue::TYPE_STR;
}

//------------------------------------------------------------------------------
CValue::CValue(int	i_nVal)
{
	m_val.n = i_nVal;
	m_type = CValue::TYPE_INT;
}

//------------------------------------------------------------------------------
CValue::CValue(bool i_fVal)
{
	m_val.f = i_fVal;
	m_type = CValue::TYPE_BOOL;
}

//------------------------------------------------------------------------------
CValue::CValue(const CValue& i_val)
{
	*this = i_val;
}

//------------------------------------------------------------------------------
CValue::~CValue()
{
	if(CValue::TYPE_STR == m_type)
	{
		free(m_val.sz);
	}
}

//------------------------------------------------------------------------------
CValue::operator int () const
{
	if(TYPE_UNDEF != m_type && TYPE_INT != m_type)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	return m_val.n;
}

//------------------------------------------------------------------------------
CValue::operator bool () const
{
	if(TYPE_UNDEF != m_type && TYPE_BOOL != m_type)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	return m_val.f;
}

//------------------------------------------------------------------------------
CValue::operator CSTR () const
{
	if(TYPE_UNDEF != m_type && TYPE_STR != m_type)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	return m_val.sz;
}

//------------------------------------------------------------------------------
CValue& 
CValue::operator = (const CValue& i_val)
{
	if(m_type != TYPE_UNDEF && i_val.GetType() != m_type)
	{
		throw CCommandException(CCommandException::X_BAD_VALUE);
	}
	switch(i_val.GetType())
	{
	case TYPE_INT:
		m_val.n = (int)i_val;
		break;
	case TYPE_BOOL:
		m_val.f = (bool)i_val;
		break;
	case TYPE_STR:
		m_val.sz = strdup((CSTR)i_val);
		break;
	default:
		throw CCommandException(CCommandException::X_BAD_VALUE);
	}
	if(m_type == TYPE_UNDEF)
		m_type = i_val.GetType();
	return *this;
}

//------------------------------------------------------------------------------
CValue& 
CValue::operator = (int i_nVal)
{
	if(m_type != CValue::TYPE_INT)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	m_val.n = i_nVal;
	return *this;
}

//------------------------------------------------------------------------------
CValue& 
CValue::operator = (bool i_fVal)
{
	if(m_type != CValue::TYPE_BOOL)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	m_val.f = i_fVal;
	return *this;
}

//------------------------------------------------------------------------------
CValue& 
CValue::operator = (CSTR i_szVal)
{
	if(m_type != CValue::TYPE_STR)
		throw CCommandException(CCommandException::X_BAD_VALUE);
	free(m_val.sz);
	m_val.sz = strdup(i_szVal);
	return *this;
}

//==============================================================================
// CFiles implementation
//
CFiles::CFiles()
{
}

CFiles::~CFiles()
{
	for(iterator iter = begin(); iter < end(); iter++)
	{
		delete *iter;
	}
}

//==============================================================================
// COptions implementation
//
COptions::COptions()
{
}

COptions::~COptions()
{
	int nOpts = size();
	for(iterator iter = begin(); iter < end(); iter++)
	{
		delete *iter;
	}
}

//==============================================================================
// CCommand implementation
//

//------------------------------------------------------------------------------
// Get option by name
// Throws CCommandException
//
COption::COption(CSTR i_szName, const CValue& i_val, bool i_isDefined)
{
	m_strName = i_szName;
	m_val = i_val;
	m_isDefined = i_isDefined;
}

COption::~COption()
{

}

//------------------------------------------------------------------------------
inline int CCommand::GetCh()
{
	if(m_Ind <= m_strCmd.size())
		return m_strCmd[m_Ind++];
	else
		return 0;
}

//------------------------------------------------------------------------------
inline int CCommand::UngetCh()
{
	if(m_Ind > 0)
		return m_strCmd[--m_Ind];
	else
		return 0;
}

COption&	
CCommand::GetOption(CSTR i_szName)
{
	bool found = false;
	COptions::iterator iter;

	for(iter = m_options.begin(); 
	    iter < m_options.end() && !found; iter++)
	{
		if(**iter == i_szName)
			found = true;
	}
	iter--;
	if(!found)
		throw CCommandException(CCommandException::X_BAD_OPTION);
	return **iter;
}

//------------------------------------------------------------------------------
// Parse the command line
//
CCommand::CCommand()
{
}

void
CCommand::Parse(CSTR i_szCmd)
{
	m_Ind = 0;
	m_Stat = STAT_START;
	m_strCmd = i_szCmd;
	while(m_Stat != STAT_ERROR && m_Stat != STAT_END)
	{
		switch(m_Stat)
		{
			case STAT_START:
				ParseExe();
				break;
			case STAT_WS:
				SkipWS();
				break;
			case STAT_OPTION:
				ParseOption();
				break;
			case STAT_FILE:
				ParseFile();
				break;
			default:
				break;
		}
	}
}

//------------------------------------------------------------------------------
// Parse executable name (1-st parameter of the command line)
void		
CCommand::ParseExe()
{
	int ch;
	if('"' == GetCh())
	{
		while((ch = GetCh()) != '"')
			m_strTok += ch;
	}
	else
	{
		while((ch = GetCh()) != ' ' && ch != '\t' && ch != '\0')
			m_strTok += ch;
		UngetCh();
	}
	m_strTok = "";
	m_Stat = STAT_WS;
}

//------------------------------------------------------------------------------
// Skip white space
void		
CCommand::SkipWS()
{
	int ch;
	m_strTok = "";
	while((ch = GetCh()) == ' ' || ch == '\t');
	if(ch == '/' || ch == '-')
	{
		m_Stat = STAT_OPTION;
	}
	else if (ch == 0)
	{
		m_Stat = STAT_END;
	}
	else
	{
		m_Stat = STAT_FILE;
		UngetCh();
	}

}

//------------------------------------------------------------------------------
void		
CCommand::ParseOption()
{
	int ch;
	bool isDone = false;
	m_strTok = "";
	
	while(!isDone)
	{
		switch(ch = GetCh())
		{
		case ':':
			ParseValue(GetOption(m_strTok.c_str()));
			isDone = true;
			break;
		case '+':
		case '-':
			UngetCh();
			ParseValue(GetOption(m_strTok.c_str()));
			isDone = true;
			break;
		case ' ':
		case '\t':
		case '\0':
		{
			COption& opt = GetOption(m_strTok.c_str());
			opt.m_isDefined = true;
			if(opt.GetValue().GetType() == CValue::TYPE_BOOL)
			{
				opt.SetValue(CValue(true));
			}
			UngetCh();
			m_Stat = STAT_WS;
			isDone = true;
			break;
		}
		default:
			m_strTok += ch;
		}
	}
}

//------------------------------------------------------------------------------
void		
CCommand::ParseValue(COption& i_opt)
{
	int ch;
	int nVal;
	m_strTok = "";

	i_opt.m_isDefined = true;
	while((ch = GetCh()) != 0 && ch != '\t' && ch != ' ')
	{
		m_strTok += ch;
	}
	switch(i_opt.GetValue().GetType())
	{
	case CValue::TYPE_INT:
		nVal = atoi(m_strTok.c_str());
		i_opt.SetValue(CValue(nVal));
		break;
	case CValue::TYPE_BOOL:
		if(m_strTok == "+")
			i_opt.SetValue(CValue(true));
		else if(m_strTok == "-")
			i_opt.SetValue(CValue(false));
		else
			throw CCommandException(CCommandException::X_BAD_VALUE, m_strTok.c_str());
		break;
	case CValue::TYPE_STR:
		i_opt.SetValue(CValue(m_strTok.c_str()));
		break;
	default:
		break;
	}
	UngetCh();
	m_Stat = STAT_WS;
}

//------------------------------------------------------------------------------
void		
CCommand::ParseFile()
{
	int ch = GetCh();
	if('"' == ch)
	{
		while((ch = GetCh()) != '"')
		{
			m_strTok += ch;
			if(0 == ch)
				throw CCommandException(CCommandException::X_BAD_FILE, m_strTok.c_str());

		}
	}
	else
	{
		while(ch != ' ' && ch != '\t' && ch != 0)
		{
			m_strTok += ch;
			ch = GetCh();
		}
		UngetCh();
	}
	m_Stat = STAT_WS;
	m_files.Add(new CFileInfo(m_strTok.c_str()));
}

//= End of Command.cpp =========================================================
