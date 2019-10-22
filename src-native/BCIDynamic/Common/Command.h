 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: Command.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// Command.h
//------------------------------------------------------------------------------
// Command line parser
//==============================================================================
#include "CommonDef.h"
#include <vector>
#include <string>

USE_NAMESPACE(std);

class CValue;
class CFileInfo;
class COption;
class CFiles;
class COptions;
class CCommand;


//------------------------------------------------------------------------------
// File Information
//
class CFileInfo
{
public:
	CFileInfo(CSTR i_szTitle = NULL)
	{
		if(NULL != i_szTitle)	 
		{
			SetTitle(i_szTitle);
		}
	}
	CSTR	GetName(){return m_strName.c_str();}
	CSTR	GetExt(){return m_strExt.c_str();}
	CSTR	GetPath(){return m_strPath.c_str();}
	CSTR	GetTitle(){return m_strTitle.c_str();}
	void	SetName(CSTR i_szName);
	void	SetExt(CSTR i_szExt);
	void	SetPath(CSTR i_szPath);
	void	SetTitle(CSTR i_szTitle);

private:
	void	MakeTitle();
	
	string	m_strExt;
	string	m_strName;
	string	m_strPath;
	string	m_strTitle;
};

//------------------------------------------------------------------------------
// Command Option Value
//
class CValue
{
public:
	typedef enum
	{
		TYPE_UNDEF,
		TYPE_INT,
		TYPE_BOOL,
		TYPE_STR
	} ValType_t;

public:
	CValue() {m_type = TYPE_UNDEF;}
	CValue(CSTR i_szVal);
	CValue(int	i_nVal);
	CValue(bool i_fVal);
	CValue(const CValue& i_val);
	~CValue();

	ValType_t GetType() const { return m_type;}
	operator int () const;
	operator bool () const;
	operator CSTR () const;
	CValue& operator = (const CValue& i_val);
	CValue& operator = (int i_nVal);
	CValue& operator = (bool i_fVal);
	CValue& operator = (CSTR i_szVal);

private:
	ValType_t	m_type;
	union
	{
		char*	sz;
		int		n;
		bool	f;
	} m_val;
};

//------------------------------------------------------------------------------
// Command option
//
class COption
{
	friend class CCommand;
public:
	COption(CSTR i_szName, const CValue& i_val, bool i_isDefined = false);
	~COption();
	bool	IsDefined() { return m_isDefined;}
	CSTR	GetName() const {return m_strName.c_str();}
	CValue& GetValue() {return m_val;}
	void	SetValue(const CValue& i_val) { m_val = i_val;}
	bool operator == (CSTR i_szName) {return m_strName == i_szName;}

private:
	string	m_strName;
	CValue	m_val;
	bool	m_isDefined;
};

//------------------------------------------------------------------------------
// Files container
//
class CFiles : public vector<CFileInfo*>
{
public:
	CFiles();
	~CFiles();

	void Add(CFileInfo* i_pFile)
	{
		push_back(i_pFile);
	}
};

//------------------------------------------------------------------------------
// Options container
//
class COptions : public vector<COption*>
{
public:
	COptions();
	~COptions();

	void Add(COption* i_pOption)
	{
		push_back(i_pOption);
	}
};

//------------------------------------------------------------------------------
// Command
//
class CCommand
{
public:
	CCommand();
	void		Parse(CSTR i_szCmd);
	CFiles&		GetFiles()		{return m_files;}
	COptions&	GetOptions()	{return m_options;}  
	COption&	GetOption(CSTR i_szName);

private:
	CFiles		m_files;
	COptions	m_options;
	
	//- Command parsing wiring
	enum
	{
		STAT_START,
		STAT_WS,
		STAT_OPTION,
		STAT_FILE,
		STAT_END,
		STAT_ERROR,
		STAT_LAST
	};
	string		m_strCmd;
	string		m_strTok;
	int			m_Ind;
	unsigned	m_Stat;
	int			GetCh();
	int			UngetCh();
	void		ParseExe();
	void		SkipWS();
	void		ParseOption();
	void		ParseValue(COption& i_opt);
	void		ParseFile();
};

//------------------------------------------------------------------------------
// Command Exception
//
class CCommandException
{
public:
	enum {
		X_UNKNOWN,		// Unknown exception
		X_BAD_OPTION,	// Bad option
		X_BAD_VALUE,	// Bad Value
		X_BAD_FILE,		// Bad file name
		X_LAST			// Sentinel
	};
	CCommandException(unsigned i_reason, CSTR i_szTok = NULL) 
	{
		m_reason = i_reason;
		m_strTok = i_szTok ? i_szTok : "";
	}
	unsigned	GetReason() {return m_reason;}
	CSTR		GetToken()  {return m_strTok.c_str();}

private:
	unsigned	m_reason;
	string		m_strTok;

};

//= End of command.h ===========================================================
