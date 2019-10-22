 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: BCIEngProbe.h,v 1.1.2.3 2007-07-18 02:48:40 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// BCIEngProbe.h
//------------------------------------------------------------------------------
// BCI Engine for the probe kit
//==============================================================================

#ifndef _BCIENGPROBE_H_
#define _BCIENGPROBE_H_

#include <vector>
#include <string>
#include <list>
#include <set>
#include "BCIEng.h"
#include "ModuleJ.h"
#include "CommonDef.h"
#include "JavaHelpers.h"			// Java helpers
USE_NAMESPACE(std);

//- Forward refs ---------------------------------------------------------------
class CBCIEngProbe;
class CProbe;

//- Some helper classes are not exposed here -----------------------------------
class BlockInsertionStash;

//- Types and defs -------------------------------------------------------------

//------------------------------------------------------------------------------
// Probe insertion engine exception
//
class CBCIEngProbeException : public CBCIEngException
{
public:
	enum
	{
		EX_OK,
		EX_INTERNAL,
		EX_LAST
	};

public:
	CBCIEngProbeException(unsigned i_uReason);
	CBCIEngProbeException(const char* i_szReason);

	virtual const char* GetStringReason() { return m_szReason; };

	static void Assert(bool cond, const char* string) {
		if (!cond) throw CBCIEngProbeException(string);
	}

private:
	char m_szReason[128];
};

// Forward-declare this becuase of circularity between
// CProbeInsertionContext and CProbeFragment
struct CProbeInsertionContext;

//------------------------------------------------------------------------------
// CProbeFragment
// 
// This class represents a single probe fragment of any type.
//

class CProbeFragment
{
public:
	typedef enum {
		PROBE_ONENTRY,		// On method entry
		PROBE_ONEXIT,		// On method exit
		PROBE_ONCATCH,		// On catch
		PROBE_BEFORECALL,	// Call site (before)
		PROBE_AFTERCALL,	// Call site (after)
		PROBE_STATICINITIALIZER,	// static initializer
		PROBE_EXECUTABLEUNIT, // executable unit
		PROBE_LAST			// Guard
	} fragmentType_t;

	// ---- HACK ALERT ---- HACK ALERT ---- HACK ALERT ---
	// The ordering of these is the the canonical order, and must match
	// the ordering used by the probe compiler when it wrote the Java code.
	// 
	// In addition, RETURNEDOBJ and EXOBJ have to be the first and second parameters
	// in the canonical order. See comments at CProbeRef::PushArguments
	// 
	typedef enum ProbeArgBits_tag {
		ARG_BITS_RETURNEDOBJ = 1,
		ARG_BITS_EXOBJ = 2,
		ARG_BITS_CLASSNAME = 4,
		ARG_BITS_METHODNAME = 8,
		ARG_BITS_METHODSIG = 16,
		ARG_BITS_THISOBJ = 32,
		ARG_BITS_ARGSLIST = 64,
		ARG_BITS_ISFINALLY = 128,
		ARG_BITS_STATICFIELD = 256,
		ARG_BITS_CLASSSOURCEFILE = 512,
		ARG_BITS_METHODNAMES = 1024,
		ARG_BITS_METHODLINETABLES = 2048,
		ARG_BITS_METHODNUMBER = 4096,
		ARG_BITS_EXECUTABLEUNITNUMBER = 8192,
		ARG_BITS_LAST = 16384
	} ProbeArgBits;

public:
	CProbeFragment(CProbe* parent, fragmentType_t i_fragType, CSTR i_szClass, CSTR i_szMethod, CSTR i_szSig, CSTR i_szArgList);
	~CProbeFragment();

	fragmentType_t GetType() { return m_fragType; };
	CExtRef*		GetExtRef() { return m_pextref; };
	unsigned int	GetArgBits() { return m_argBits; };
	CProbe*			GetParentProbe() { return m_parent; };

	void		 PushArguments(CProbeInsertionContext& ctxt);
	virtual void Instrument(CProbeInsertionContext& ctxt);
	static unsigned int ComputeArgBits(CSTR i_szArgsList);


protected:
	CExtRef*		m_pextref;
	unsigned int	m_argBits;
	fragmentType_t	m_fragType;
	CProbe*			m_parent;
};


//------------------------------------------------------------------------------
typedef vector<CProbeFragment*> CProbeFragmentList;

//------------------------------------------------------------------------------
//
// CLineTableEncoder
//
// This class implements the string encoder for line tables.
//
// Create one, then feed it numbers, then read out the answer from the string.
//
// You can also simply append characters to the string, like ',' to separate methods.
//

class CLineTableEncoder : public string
{
public:
	CLineTableEncoder() : 
		string(),
		m_increment_mode(false),
		m_previous_number(0) 
	{ };

	void appendLineNumber(int i);

private:
	bool m_increment_mode;
	int m_previous_number;
};

//------------------------------------------------------------------------------
// CProbeInsertionContext
// 
// This is a parameter block that contains everything you need to know 
// to do insertion for a probe fragment. It was created because the
// PushArguments function was starting to sprout a truly amazing number 
// of parameters.
//
// NOTICE FOR MAINTAINERS:
//
// When this class was created, we tried to comment the meaning and 
// implications of each value that is placed here. If we don't keep
// that up, this will degenerate into an even bigger ball of mud
// than it is now.
//
// If you need to know what one of these is for, read the comment here.
// If you can't figure it out from the comment, then update the comment
// when you do figure it out. If you add a new use for one of these
// items, come back and comment it here. PLEASE.
// 
// This struct doesn't "own" any of the things it points to. It just
// refers to them. Think of it as an extention to the parameter list for 
// the functions it's passed to.
//
// The default constructor doesn't initialize anything.
// The other one pulls knowable values from the method, the module,
// and the CBCIEngProbe instance.
//

struct CProbeInsertionContext {
	CProbeInsertionContext() { ; };
	CProbeInsertionContext(CBCIEngProbe* pEngProbe, CMethod* i_pmeth);

	//----------------------------------------------------------------------
	// SECTION 1: VALUES WHICH ARE KNOWN AND CONSTANT FOR A WHOLE METHOD

	// MEANING: Strings for the class name, method name, method signature.
	const char* className;
	const char* methodName;
	const char* methodSignature;
	const char* classSourceFile;	// null if none available

	// MEANING: a string pointer to CBCIEngProbe::m_methodNamesString for this class
	const char* methodNamesString;

	// MEANING: a string pointer to CBCIEngProbe::m_lineTableString for this class
	const char* lineTableString;

	// MEANING: Tells whether the method has a "this" argument.
	// USES: In function insertion, tells whether the first (non-"this") parameter is in slot 0 or 1.
	// NOTE: When true, it's still possible that a given proberef can't access "this" for a given function.
	// See proberef_can_access_this() for an explanation.
	bool hasThis;

	// MEANING: A pointer to the constant pool object for the method the insertion is being done in.
	// USES: strings and function references are added to this constant pool.
	CConstPool* pConstPool;

	// MEANING: A pointer to the method that the insertion is going into.
	// NOTE: For callsite probes, this is *not* the method being called!
	CMethodJ* pMethodJ;

	// MEANING: the method number of this method in the module in the canonical ordering
	// (which is the ordering of CMethods in the CModule)
	unsigned methodNumber;

	//----------------------------------------------------------------------
	// SECTION 2: VALUES WHICH ARE NOT KNOWN AT CONTEXT CONSTRUCTOR TIME,
	// BUT REMAIN THE SAME FOR THE WHOLE METHOD ONCE THEY'RE FILLED IN

	// MEANING: The local variable number to use for holding the "this" argument. Value -1 means "none."
	// USES: The "this" argument can be found in this location. For function probes it's zero.
	// For callsite probes it's the local that "this" was saved in, if any.
	int localVariableForThis;

	// MEANING: The local variable number to use for holding the argument list Object array. Value -1 means "none."
	// USES: In all probes, the argument list Object[] array can be found in this location.
	int localVariableForArgs;

	//----------------------------------------------------------------------
	// SECTION 3: VALUES WHICH CHANGE FROM ONE INSERTION POINT TO ANOTHER

	// MEANING: A pointer to the CInstructions list that insertion should be done on.
	// USES: see Insert function below
	CInstructions* pIns;

	// MEANING: The iterator representing the location before which instructions should be inserted.
	// USES: see Insert function below.
	// TODO: don't make this an iterator*, make it an iterator. Does that work? Simpler, less odd.
	// Watch out - you can't safely say ctxt.itrIns = &(block->begin()) because the lifetime
	// of the iterator returned by begin() in that expression isn't long enough.
	CInstructions::iterator* itrIns;

	// MEANING: the executable unit number of the executable unit within its method.
	// Available to executableUnit and catch fragments only.
	unsigned executableUnitNumber;

	// MEANING: True for exception exit insertion.
	// False for regular exit insertion, or insertion that isn't exit insertion at all.
	// USES: ignored unless the fragment wants the exception object, in which case
	// this flag being true tells you there is one; otherwise you pass null.
	bool isExceptionExit;

	// MEANING: True for insertion in a finally clause that's entered by exception,
	// false for all other kinds of insertion.
	// USES: passed as the isFinally flag parameter value. Only for catch-type fragments.
	bool isFinally;

	//----------------------------------------------------------------------
	// SECTION 4: VALUES WHICH CHANGE FOR EACH DIFFERENT FRAGMENT INSERTED AT A GIVEN SPOT

	// MEANING: a reference into the constant pool of the static field
	// created by the <staticField> element of the probe description.
	// A value of zero means none exists, but any probe that uses <staticField>
	// should have a nonzero value for this.
	// During insertion, this changes rapidly, because fragments from different
	// probes will have different staticFieldRefs (assuming they both
	// have <staticField> elements.)
	// 
	// USES: the staticField data item pushes the value of this field as the argument.
	unsigned staticFieldRef;

	// MEANING: Which type of fragment this is: entry, exit, catch, callsite entry, callsite exit.
	CProbeFragment::fragmentType_t fragmentType;

	// MEANING: What arguments this probe reference wants us to pass it.
	// TODO: change the type to ProbeArgBits_enu and define that type in CProbeRef when ClearCase works again.
	unsigned int argBits;

	//----------------------------------------------------------------------
	// METHODS: for emitting one instruction or several at the indicated point

	void Insert(CInstruction* ins) {
		// Quickie inline for a single instruction
		pIns->insert(*itrIns, ins);
	}

	void BoxStackedValue(CJavaType::jtype_t jtype, bool keep_on_stack);
	void BoxLocalValue(CJavaType::jtype_t jtype, int local_num);
	int  UnboxStackedValue(CJavaType::jtype_t jtype);
	void CallsiteStoreArgs();
	void CallsiteStoreThis();
	void CallsiteReloadArgsAndThis();
	void EmitArgsList();
	bool CanAccessThis();
};

//------------------------------------------------------------------------------
// CFilterRule, CFilterRuleList

class CFilterRule {

public:

	// These members are public so we don't have a useless constructor that
	// just copies values into fields.

	char*		m_strPackageName;
	char*		m_strClassName;
	char*		m_strMethodName;
	char*		m_strMethodSig;
	enum action_t {
		ACTION_INCLUDE,
		ACTION_EXCLUDE
	} m_action;

	CFilterRule(CSTR i_szPackageName, 
				  CSTR i_szClassName, 
				  CSTR i_szMethodName, 
				  CSTR i_szMethodSig, 
				  action_t i_action);

	CFilterRule(const CFilterRule& other);
	CFilterRule& operator=(const CFilterRule& other);
	~CFilterRule();

	// Wildcard rules:
	// The "pattern" is the wildcard pattern, like "javax*"
	// The "candidate" is the name we're checking, like "javax.net.SocketFactory"
	//
	// As a special case, if the candidate is "*" we return TRUE.
	// Otherwise it's usual wildcard rules: a star matches anything (or nothing)
	// between non-star substrings.
	//
	// This wildcard string match function is public and static so we can
	// write a test for it from outside this class.
	static bool WildcardStringMatch(const char* pattern, const char* candidate);

	// Match this rule against a candidate
	bool Match(const char* i_strPackage, 
			   const char* i_strClass, 
			   const char* i_strMethod, 
			   const char* i_strMethodSig);
};

typedef list<CFilterRule> CFilterRuleList;


//------------------------------------------------------------------------------
// CPrefilters
// Prefilters (aka first tier filters).
// This list represent a set of rules that apply as the first tier of filtering.
// Rules from the original probe script will apply only if a class passed the
// first tier filtering. These rules are stored in the engine. Each probe 
// Has references to a filter list from this list. The list is populated from the 
// "FILTER" statements that may accompany a combined set of probes delivered in
// the Initialize(...) call. The "FILTER" statements are created by the BCI
// engine host from information obtained from the UI or from the configuration.
//
class CPrefilters : public vector<CFilterRuleList*>
{
public:
	~CPrefilters();
};

//------------------------------------------------------------------------------
// CProbe
// Probe descriptor
//
class CProbe
{
public:
	CProbe() { m_staticFieldRef = 0; m_plstPrefilter = NULL; m_plstPrefilterWithin = NULL;};
	~CProbe();

	void		AddFilterRule( CFilterRule& rule );
	
	void		AddFilterRuleWithin( CFilterRule& rule );

	void		AddFilterRule(CSTR i_szPackageName, 
							  CSTR i_szClassName, 
							  CSTR i_szMethodName, 
							  CSTR i_szMethodSig, 
							  CFilterRule::action_t i_action);
							  
	void		AddFilterRuleWithin(CSTR i_szPackageName, 
							  CSTR i_szClassName, 
							  CSTR i_szMethodName, 
							  CSTR i_szMethodSig, 
							  CFilterRule::action_t i_action);						  

	void		AddPrefilterRule(CSTR i_szPackageName, 
							  CSTR i_szClassName, 
							  CSTR i_szMethodName, 
							  CSTR i_szMethodSig, 
							  CFilterRule::action_t i_action);
							  
	void		AddPrefilterRuleWithin(CSTR i_szPackageName, 
							  CSTR i_szClassName, 
							  CSTR i_szMethodName, 
							  CSTR i_szMethodSig, 
							  CFilterRule::action_t i_action);

	void		SetPrefilter(CFilterRuleList* i_plstPrefilter){m_plstPrefilter = i_plstPrefilter;}
	
	void		SetPrefilterWithin(CFilterRuleList* i_plstPrefilter){m_plstPrefilterWithin = i_plstPrefilter;}

	// External reference descriptor
	void		AddFragment(CProbeFragment::fragmentType_t i_fragType, 
		                    CSTR i_szClass, CSTR i_szMethod, CSTR i_szMethodSig, 
							CSTR i_szRefType = "static", CSTR i_szArgList = "");

	CProbeFragmentList&	GetProbeFragmentList();

	void		AddStaticField(CSTR i_szTypeString);

	bool		IsCallsiteProbe();

	// Match
	// The arguments being passed are the package, class, method, and signature strings
	// of the candidate method for the match. Sometimes this is called with just
	// a package and class name, to see if any method in that class could possibly
	// match. In that case the method and signature strings are just "*"
	bool		Match(CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szMethodSig, bool* pIsExplicit = NULL);
	
	//Same method but used to see if it applies on within filters/rules
	bool		MatchWithin(CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szMethodSig, bool* pIsExplicit = NULL);

	// StaticFieldType getter.
	// When this returns NULL, the probe doesn't declare a static field.
	// When not null, it's the string telling what type the static field should be.
	CSTR		GetStaticFieldType() { if (m_staticFieldType.length() == 0) return NULL;
										else return m_staticFieldType.c_str(); };

	// StaticFieldRef getter and setter.
	// This is reset each time a new module (class) is processed.
	// It's only used if this probe uses the staticField element.
	void		SetStaticFieldRef(unsigned ref) { m_staticFieldRef = ref; };
	unsigned	GetStaticFieldRef() const { return m_staticFieldRef; };

	//FIXME: expose internalelements
	CFilterRuleList		GetFilterRuleList() const { return m_ruleList; };
	CFilterRuleList		GetFilterRuleWithinList() const { return m_ruleListWithin; };

	CFilterRuleList&		GetPrefilterRuleList() const { return *m_plstPrefilter; };
	CFilterRuleList&		GetPrefilterRuleWithinList() const { return *m_plstPrefilterWithin; };	
	
private:
	// The filter spec: what method(s) this probe should be applied to
	CFilterRuleList		m_ruleList;
	CFilterRuleList		m_ruleListWithin;
	CFilterRuleList*	m_plstPrefilter;
	CFilterRuleList*	m_plstPrefilterWithin;

	CProbeFragmentList m_probefraglst;
	string m_staticFieldType;
	unsigned m_staticFieldRef;
	
	bool		MatchCommon( CFilterRuleList& i_ruleList, CFilterRuleList*	i_plstPrefilter, CSTR i_szPkgAndClass, CSTR i_szMethod, CSTR i_szMethodSig, bool* pIsExplicit = NULL);
};

//------------------------------------------------------------------------------
// CProbeList
// A list of pointers to probes.
//
class CProbeList:public vector <CProbe*>
{
public:
	CProbeList();

	virtual void	AddProbe(CProbe* i_pprobe);
};


//------------------------------------------------------------------------------
// CBCIEngProbe
// Probe insertion engine
//
class CBCIEngProbe : public CBCIEng
{
public:
	CBCIEngProbe();
	~CBCIEngProbe();

	bool			Instrument(void* i_pInClass, size_t i_cbInClass,
							   void** o_ppOutClass, size_t* o_pcbOutClass);
	void			AddProbe(CProbe* i_pprobe);
	CProbe*			CreateProbe() const;
	const CProbeList& GetProbes() const;

	int				AddPrefilter(CFilterRuleList* i_plstRules);
	int				AddPrefilterWithin(CFilterRuleList* i_plstRules);
	const string&	GetMethodNamesString() { return m_methodNamesString; };
	const string&	GetLineTableString() { return m_lineTableString; };

private:
	virtual void	Instrument(CModule* i_pmod);
	void			InstrumentMethod(CMethod* i_pmeth);
	void			CreateExitWrap(CProbeInsertionContext& ctxt,
						CProbeFragmentList& exit_probe_frags);

	string			BuildLineTableString(CModuleJ* i_pmodj);
	string			BuildMethodNamesString(CModule* i_pmod);

	void			HandleCallsiteInsertion(CProbeInsertionContext& ctxt,
						CProbeFragmentList& beforeCallFragments,
						CProbeFragmentList& afterCallFragments,
						BlockInsertionStash& block_insertion_stash);

	void			HandleStaticInitializers(CModuleJ* i_pmodj);
	void			EmitInitializerForSerialVersionUID(CMethod* pStaticCtorMethod, 
						CSerialVersionUIDHelper* pSerialVersionHelper,
						unsigned serialUIDFieldRef);
private:
	CProbeList		m_probelst;				// List of Probes
	CPrefilters		m_prefilters;			// First tier of filtering
	CPrefilters		m_prefiltersWithin;			// First tier of filtering
	

	// The following fields are reset and reused for each module
	CProbeList		m_probelstMod;			// Probes for the current module
	set<CProbeFragment*>	m_staticInitializerFragments;
	string			m_lineTableString;
	string			m_methodNamesString;

	// These fields are reset and reused for each instrumented method
	IP_t			m_ipFinally;				// Finally wrapper label
	IP_t			m_ipCatchAll;				// catch-all wrapper label
};

#endif // defined _BCIENGPROBE_H_

//= End of BCIEngProbe.h =======================================================
