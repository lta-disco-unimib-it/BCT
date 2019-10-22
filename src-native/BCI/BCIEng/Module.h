/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Module.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// Module.h
// Started 9/28/99
//
//------------------------------------------------------------------------------
// Description:
// See BCI.red for the UML diagrams.
// Module is a minimal instrumentation unit (usually binary file or execution 
// unit loaded into the memory.
//
//==============================================================================
#ifndef _MODULE_H
#define _MODULE_H

#include "CommonDef.h"

#ifdef WIN32
#pragma warning(disable:4786)	// Because we use STL
#endif

//------------------------------------------------------------------------------
// STL
#include <vector>
#include <string>
#include <list>
#include <map>

USE_NAMESPACE(std);

//------------------------------------------------------------------------------
typedef unsigned		LineNum_t;
typedef unsigned		IP_t;
typedef unsigned char	Code_t;

static const IP_t IP_UNDEFINED = (IP_t)-1;

#include "InsSet.h"
//==============================================================================
// Exports
//
class CModule;
class CExtRef;
class CInsSet;
class CInsDescr;
class CMethod;
class CMethods;
class CMethodBody;
class CMethodException;
class CMethodExceptionTable;
class CBBlock;
class CBBlocks;
class CInstruction;
class CLineNumbers;
class CLabels;
class CModuleException;	
class CMtdExTable;	

struct MapLabelsEntry {
	IP_t newInstructionIP;
	IP_t newBlockIP;
	MapLabelsEntry(IP_t a, IP_t b) : newInstructionIP(a), newBlockIP(b) { }
};

//==============================================================================
// Definitions and declarations 
//
typedef vector <class CMethod*>		VectMethods_t;
typedef vector <class CInsBlock*>		ListInsBlocks_t;
typedef list   <class CInstruction*>	ListInstructions_t;
typedef map    <IP_t, IP_t>		MapLineNums_t;
typedef map    <IP_t, MapLabelsEntry>		MapLabels_t;
typedef vector <class CExtRef *>		CExtRefs;
typedef MapLineNums_t::value_type LineNumsEntry_t;
typedef MapLabels_t::value_type LabelsEntry_t;

// Declared in InsSet.h, but have a circular dependency, so redef here
typedef unsigned BrTarget_t;

//==============================================================================
// CModule
// Module is the minimal instrumentation unit. 
// In Java, it's like a class or a class file.
// Abstract class. 
//
// A module holds a collection of external references - whenever
// you call AddExtRef, it gets added to the collection, in order.
// You can index the collection (it's a vector by default).
//


class CModule
{
public:
	CModule(CInsSet* i_pInsSet);
	virtual ~CModule();

	virtual CSTR	GetName() const = 0;
	virtual CSTR	GetLanguage() const =0;
	virtual CSTR	GetDescription() const =0;
	virtual bool	IsInstrumented() const =0;

	// GetInterfaces returns a vector of
	// strings telling what interfaces this class implements.
	// It's a vector<string> to prevent any difficulty with memory 
	// ownership and destruction.
	virtual vector<string> GetInterfaces() const =0;

	virtual void	Open(CSTR i_szName)=0;

	// Implementors of AddExtRef are expected to call the inherited one
	// just to add the new reference to the collection.
	virtual void		AddExtRef(CExtRef& i_ExtRef);
	virtual CExtRefs*	GetExtRefs(){return m_pExtRefs;}

	virtual void	AddStringAttrib(CSTR i_szName, CSTR i_szValue)=0;

	// GetSourceFileNames should return a vector of strings naming
	// files that contributed to this module. Java will have just
	// one string in the vector.

	virtual const vector<string>& GetSourceFileNames() = 0;

	virtual void	Parse()=0;
	virtual void	Emit();

	CMethods*		GetMethods(){return m_pMethods;}
	CInsSet*		GetInsSet()	{return m_pInsSet;}


	virtual void	Dump(ostream& i_os) const;

protected:
	CMethods*		m_pMethods;
	CInsSet*		m_pInsSet;
	CExtRefs*		m_pExtRefs;

private:

};

//==============================================================================
// External reference.
// External reference is inserted into the module (see CModule::AddRef)
// at the instrumentation time. CExtRef carries all the necessary information
// about the reference we are going to add to the module.
class CExtRef
{
public:
	virtual ~CExtRef(){;}

	virtual void			InjectMetaData(CModule& i_Module) = 0;
	virtual CInstruction*	CreateInstruction() const = 0;
	virtual string			ToString() const = 0;

};

//==============================================================================
// CMethodBody
// Method body has functionality for navigating method code and 
// accessing Instruction Descriptor based on the current IP position.
// Method body navigates it's code by consulting the instruction set
// table (CInsSet) attached to the hosting Module.
//
// Note: it would be great to always copy code bytes, but unfortunately
// this might be expensive. That's why CMethodBody has GiveAwayCode.
// This method returns pointer to the actual code and promises not to 
// delete it in the future.
// 
class CMethodBody
{
public:
	CMethodBody(CModule* i_pModule, Code_t* i_pCode, size_t i_CodeSize);
	virtual		~CMethodBody();

	IP_t		GetIP() const {return m_IP;}
	void		ResetIP();
	unsigned	GetCodeByte() const {return (unsigned)m_pCode[m_IP];}
	Code_t*		GetCode() {return m_pCode;}				// fast and dangerous
	const Code_t* GetCode() const { return m_pCode; }	// const version
	size_t		GetCodeSize() const {return m_CodeSize;}
	void		NewCode(Code_t* i_pCode, size_t i_CodeSize);
	void		Inject(BYTE* i_pCode, size_t i_Size);

	Code_t*		GiveAvayCode() {m_DisposeCode = false; return m_pCode;}

	CInsDescr*	GetInsDescr();
	CInsDescr*	Advance();

protected:
	CModule*		m_pModule;
	IP_t			m_IP;
	Code_t*			m_pCode;
	size_t			m_CodeSize;
	IP_t			m_StartIP;
	CInsDescr*		m_pCurrInsDescr;

private:
	bool		m_DisposeCode;
};

//==============================================================================
// CInstructions
class CInstructions : public ListInstructions_t
{
public:
	CInstructions(){}
	~CInstructions();
	virtual void	Dump(ostream& i_os, CMethod& i_Method);
};

//==============================================================================
// CInsBlock
// Means "Insertion Block" or "Instruction Block" - holds a list of instructions.
// All functions are broken down into a list of these by Parse().
// Blocks are split at basic block boundaries: branch instructions, labels, calls, etc.
// They're also split at line numbers, so technically multiple CInsBlocks
// can form a single "basic block."
//
class CInsBlock
{
public:
	CInsBlock(IP_t i_Label, int blockNum = -1) {
		m_ipLabel = i_Label;
		m_ipOrig = i_Label;
		m_origBlockNum = blockNum;
		m_bVisited = false;
	};

	IP_t	GetLabel() const {return m_ipLabel;}
	IP_t	GetOrigIP() const {return m_ipOrig;}
	size_t	GetLength() const;
	IP_t	UpdateIP(IP_t i_ip);
	void	SetLabel(IP_t i_ip){m_ipLabel = i_ip;}
	void	AddInstruction(CInstruction* i_pInstr){m_Instrs.push_back(i_pInstr);}

	// "Original block number" tracking:
	// The value -1 means "this is not an original block."
	// Any other value is a block's original position in the
	// initial parse of the module.
	int		GetOriginalBlockNumber() const { return m_origBlockNum; };

	void AddCallBefore(const CExtRef& i_ExtRef){;}
	void AddCallAfter(const CExtRef& i_ExtRef){;}

	CInstructions *GetInstructions() {return &m_Instrs;}

	void	Emit(CMethod& o_Method);
	void	Dump(ostream& i_os, CMethod& i_Method);
	bool	IsVisited() const {return m_bVisited;}
	void	SetVisited() {m_bVisited = true;}

protected:
	IP_t			m_ipLabel;			// Block label: the starting address
	IP_t			m_ipOrig;			// Original Label
	CInstructions	m_Instrs;			// Instructions inside the block
	int				m_origBlockNum;		// original block number, or -1 if synthetic.

private:
	bool			m_bVisited;			// True if the block was visited
										// by the follow branch routine
};


//==============================================================================
// CInsBLocks
// Collection of insertion blocks
//
class CInsBlocks : public ListInsBlocks_t
{
public:
	CInsBlocks();
	~CInsBlocks();

	virtual void	Dump(ostream& i_os, CMethod& i_Method);

protected:

private:

};

//==============================================================================
// CLineNumbers
// Line numbers map
//------------------------------------------------------------------------------
// This class contains a map of address used in a line number table
// to a new address in the instrumented module.
// Procedure emission should use this map to regenerate the line number table. 
//
class CLineNumbers : public MapLineNums_t
{
public:
	CLineNumbers(){;}
	~CLineNumbers(){;}

	IP_t FindAddress(IP_t i_IP);
	void SetNewTarget(IP_t i_oldIP, IP_t i_newIP);
	
	virtual void	Dump(ostream& i_os);

private:

};

//==============================================================================
class CMethodException 
{
public:
	CMethodException(IP_t i_ipStart, IP_t i_ipEnd, CInsBlock* i_pblkHandler)
	:m_ipStart(i_ipStart), m_ipEnd(i_ipEnd), m_pblkHandler(i_pblkHandler){}
	IP_t		GetStart()const {return m_ipStart;}
	IP_t		GetEnd()const {return m_ipEnd;}
	CInsBlock*	GetHandler()const {return m_pblkHandler;}
	void		SetStart(IP_t i_ip) {m_ipStart = i_ip;}
	void		SetEnd(IP_t i_ip) {m_ipEnd = i_ip;}
	void		SetHandler(CInsBlock* i_pblkHandler) {m_pblkHandler = i_pblkHandler;}
private:
	IP_t		m_ipStart;
	IP_t		m_ipEnd;
	CInsBlock*	m_pblkHandler;
};

//==============================================================================
class CMtdExTable : public vector <CMethodException*>
{
public:
	typedef enum
	{
		AUTO,
		TOP,
		BOTTOM
	} Order_enu;

public:
	CMtdExTable(CMethod* i_pmtd){m_pmtd = i_pmtd;}
	virtual ~CMtdExTable()
	{
		for(CMtdExTable::iterator itr = begin(); itr != end(); itr++)
			delete *itr;
	}
	void			AddException(CMethodException* i_pmtdex, Order_enu i_enuOrder);
	virtual void	Parse()=0;
	virtual void	Emit()=0;
	virtual void	Dump(ostream& i_os) const = 0;

protected:
	CMethod*		m_pmtd;
};

//==============================================================================
// CLabels
// Collection of labels
//
// The label table is a map whose key is the old IP of an instruction of interest.
// An instruction of interest is one whose new IP you're going to want to know during Emit().
//
// There are TWO forwarding addresses for each label. Which one you use depends on what
// you're using it for. The "instruction target" is the new IP of the actual instruction
// that the label was created for. This is used for exception table patching.
//
// The "block target" is the new IP of the top of the CInsBlock that the instruction
// is in. That is the forwarding address uesd for branch instructions. 
//
// Here's why the "block target" is the one to use for branch instructions:
// Any instruction which is the target of a branch instruction is going to be at the
// top of a CInsBlock, because during Parse we sliced a new block at branch targets.
// But we might do insertion before this instruction, like if it's an "invoke" and
// a callsite probe applies to it, or if we're doing insertion on every CInsBlock
// (every executableUnit in probekit terms). In that case, the insertion should be the target
// of any branch that jumps to this instruction.
//
// Back to the "instruction target" type, and the reason we can't use the block target
// as the proper target in all cases: in callsite insertion, we create a new try/catch block
// around the original invoke instruction. This instruction will always be at the end of
// and original CInsBlock, but it might not have been at the start of one. We don't want
// the try block to extend back to the start of the CInsBlock that this invoke instruction
// is in, so we have to patch the exception table based on the new IP of the actual invoke
// instruction, not the top of the CInsBlock.
//

class CLabels : private MapLabels_t
{
public:
	bool IsLabel(IP_t i_IP);
	CLabels(){;}
	~CLabels(){;}
	IP_t GetLabelInstructionTarget(IP_t i_IP);
	IP_t GetLabelBlockTarget(IP_t i_IP);
	void SetLabelTargets(IP_t i_oldIP, IP_t i_newInstructionIP, IP_t i_newBlockIP);
	void Dump(ostream &i_os) const;
	void AddLabel(IP_t oldIP) { insert(LabelsEntry_t(oldIP, MapLabelsEntry(oldIP, oldIP))); };
    using MapLabels_t::clear;       // publicize clear()
};

//==============================================================================
// CMethod
// Method is the functional unit of a module. It is also the primary 
// instrumentation target. First we parse each method into components:
// insertion units, labels, line numbers etc. Then we instrument the method
// and it's insertion unit. After the insertion is done, the method can be re-emited.
// Note, that each method has a body that holds the actual code. The body
// is parsed into the instruction groups, that form insertion units. After
// the insertion is done, the method body is recreated by emiting individual 
// instructions forming the insertion units back into the body.
//
class CMethod
{
public:
	CMethod(CModule* i_pModule, CSTR i_szName)
	{
		m_pModule = i_pModule;
		m_strName = i_szName;
		m_fDestroyBody = false;
		m_pBody = NULL;
		m_pMtdExTable = NULL;
		m_fHasThis = false;
		m_nextUniqueIP = 0;
	}

	virtual ~CMethod()
	{
		if(m_fDestroyBody)
			delete m_pBody;
		delete m_pMtdExTable;
	}

	virtual void	Parse();
	virtual void	Emit();
	virtual	int		CalcStackDepth();

	CSTR			GetName() const {return m_strName.c_str();}	
	CMethodBody*	GetBody() const {return m_pBody;}
	CModule*		GetModule() const {return m_pModule;}
	CInsBlocks*		GetInsBlocks() { return &m_Blocks; }
	CLineNumbers*	GetLineNumbers() {return &m_LineNumbers;}
	CLabels*		GetLabels() { return &m_Labels; }
	CMtdExTable*	GetExTable() { return m_pMtdExTable;}
	CInsBlock*		FindBlock(IP_t i_ip);
	bool			GetHasThis() { return m_fHasThis; }
	void			SetHasThis(bool f) { m_fHasThis = f; }

	// Method to create a label for an IP that doesn't already exist.
	// You can only use this after Parse() and before Emit().
	IP_t			CreateUniqueLabel();
	void			AddException(CMethodException* i_pmtdex, 
						CMtdExTable::Order_enu i_enuOrder = CMtdExTable::BOTTOM);

	void			SetBody(CMethodBody* i_pBody, bool i_fDestroy = true)
	{
		m_pBody = i_pBody;
		m_fDestroyBody = i_fDestroy;
	}

	virtual void	Dump(ostream& i_os);

protected:
	string			m_strName;			// Method name
	CModule*		m_pModule;			// Hosting module
	CMethodBody*	m_pBody;			// Method Body
	CInsBlocks		m_Blocks;			// Insertion blocks
	CLineNumbers	m_LineNumbers;		// Line number info
	CMtdExTable*	m_pMtdExTable;		// Method Exception table			
	CLabels			m_Labels;			// Labels
	bool			m_fDestroyBody;		// Need to destroy body on destruction?
	bool			m_fHasThis;			// Set true if this is an instance method.
	IP_t			m_nextUniqueIP;		// For creating unique labels; works only after Parse()

private:
	int				CalcStackDepth(CInsBlock* i_pblk, int i_nIn);
	CInsBlock*		GetNextBlock(CInsBlock* i_pblk);
	CInsBlock*		GetTargetBlock(BrTarget_t);
};

//==============================================================================
// CMethods
// Vector of methods in a module
//
class CMethods : public VectMethods_t
{
public:
	CMethods(){;}
	~CMethods()
	{
		for(CMethods::iterator iter = begin(); iter < end(); iter++)
		{
			delete *iter;
		}
	}

	virtual void	Dump(ostream& i_os)
	{
		for(iterator iter = begin(); iter < end(); iter++)
			(*iter)->Dump(i_os);
	}

protected:

private:

};

//==============================================================================
// CModuleException
// This exception is thrown by classes in this module
//
class CModuleException
{
public:
	enum
	{
		X_REASON_OK,
		X_REASON_UNKNOWN,				// I don't know what it is
		X_REASON_INTERNAL_ERROR,		// Internal error
		X_REASON_INVALID_MODULE,		// Invalid module structure
		X_REASON_PARSE_ERROR,			// Parsing error
		X_REASON_INVALID_CALL,			// Invalid method call (e.g. abstract method)
		X_REASON_CODE_OVERRUN,			// Code overrun during emission
		X_REASON_LAST
	};

	CModuleException(unsigned i_Reason = X_REASON_OK, CSTR i_szMessage = "<?>")
	{
		m_Reason = i_Reason;
		m_strMessage = i_szMessage;
	}
	unsigned GetReason() const {return m_Reason;}

	CSTR GetMessage() const
	{
		return m_strMessage.c_str();
	}

protected:

private:
	string		m_strMessage;
	unsigned	m_Reason;
};

#endif // _MODULE_H
//= End Of Module.h ============================================================
