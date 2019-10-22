/**********************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ModuleJ.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/

//==============================================================================
// ModuleJ.cpp
// 9/28/99
//
//------------------------------------------------------------------------------
// Description
// Java-specific module implementation
//
//==============================================================================

#if defined(__OS400__)
#pragma convert(819)	/* see comment in CommonDef.h about this */
#endif

#include "ModuleJ.h"
#include "JVMInsSet.h"
#include "JFileStream.h"

#ifdef HPUX
#include <iostream.h>
#else
#include <iostream>
#endif

static CInsSetJ InsSet;

//==============================================================================
// CModuleJ implementation
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
// Constructor
//

CModuleJ::CModuleJ()
:CModule(&InsSet)
{
	m_pClass = NULL;
	m_fDestroyClass = false;
	m_fAccessFlags = 0;
	m_sourceFileNamesPopulated = false;
}

//------------------------------------------------------------------------------
// Destructor
//

CModuleJ::~CModuleJ()
{
	if(m_fDestroyClass)
	{
		delete m_pClass;
	}
}

//------------------------------------------------------------------------------
// Open module as a file
//
void	
CModuleJ::Open(CSTR i_szName)
{
	CJClassBuilder*	pJClass;						// Java Class
	CJFileStream	FileStreamIn;					// File stream
	CJStream		InStream(&FileStreamIn);		// Java input stream

	m_strName = i_szName;
	pJClass = new CJClassBuilder;
	FileStreamIn.Open(i_szName, CJFileStream::ACCESS_READ);
	pJClass->Read(InStream);
	m_pClass = pJClass;
	m_fDestroyClass = true;
	FileStreamIn.Close();
	Verify();
}

//------------------------------------------------------------------------------
// IsInstrumented
// Is the module instrumented?
// The module is considered to be instrumented if it has the "Instrumented" field
//
// Returns:
//		true if module is instrumented
//		false otherwise
//
bool
CModuleJ::IsInstrumented() const
{
	CJAttribs* pAttribs = m_pClass->GetAttribs();
	for(CJAttribs::iterator iter = pAttribs->begin(); iter < pAttribs->end(); iter++)
	{
		string strName = *(*iter)->GetName();
		if(strName.compare("Instrumented") == 0)
		{
			return true;
		}
	}
	return false;
}

//------------------------------------------------------------------------------
void	
CModuleJ::SetAccessFlags(unsigned long f)
{
	m_fAccessFlags = f;
}

//------------------------------------------------------------------------------
// Open
// Open module given classfile
// Note: the classfile must be read or otherwise constructed
//       prior to passing into this routine
//
void	
CModuleJ::Open(CJClassBuilder* io_pClass, bool i_fDestroyClass)
{
	m_pClass = io_pClass;
	m_fDestroyClass = i_fDestroyClass;
	Verify();
}

//------------------------------------------------------------------------------
void	
CModuleJ::AddExtRef(CExtRef& i_ExtRef)
{
	// Add external reference to the module
	// ToDo? Verify that it is a valid Java reference?
	i_ExtRef.InjectMetaData(*this);	

}

//------------------------------------------------------------------------------
void	
CModuleJ::AddStringAttrib(CSTR i_szName, CSTR i_szValue)
{
	CJAttribs* pAttribs = m_pClass->GetAttribs();
	CStringAttribute* pStringAttrib = new CStringAttribute(m_pClass,
		                                                   CCPUtf8Info(i_szName),
		                                                   CCPUtf8Info(i_szValue));
	pAttribs->Add(pStringAttrib);
}

//------------------------------------------------------------------------------
// Module modification methods

//------------------------------------------------------------------------------
CCPFieldrefInfo*	
CModuleJ::CreateFieldRef(u2 i_u2AccFlags, CSTR i_szName, CJavaType i_jtype)
{
	CFieldInfo* pfi = m_pClass->CreateField(i_u2AccFlags, i_szName, i_jtype);
	CCPFieldrefInfo* pfri = m_pClass->CreateFieldRef(pfi);
	return pfri;
}

//------------------------------------------------------------------------------
CMethodJ*			
CModuleJ::CreateMethod(u2 i_u2AccFlags, CSTR i_szName, CSTR i_szSignature)
{
	CJMethodInfo* pmi = m_pClass->CreateMethod(i_u2AccFlags, i_szName, i_szSignature);

	CMethodJ* pmeth = new CMethodJ(this, i_szName, i_szSignature, pmi->GetCode(), i_u2AccFlags);
	CCodeAttribute* pCodeAttribute = pmi->GetCode();
	if(NULL != pCodeAttribute)
	{
		u1* pcodeBytes = pCodeAttribute->GetCode();
		u2  u2CodeLength = pCodeAttribute->GetCodeLength();
		CMethodBody *pBody = new CMethodBody(this, pcodeBytes, u2CodeLength);
		pmeth->SetBody(pBody);
	}
	pmeth->Parse();
	m_pMethods->push_back(pmeth);
	return pmeth;
}

//------------------------------------------------------------------------------
void	
CModuleJ::Parse()
{
	CJMethods* pMethods	= m_pClass->GetMethods();
	CJMethods::iterator iterjm;
	// GetClassName
	CConstPool& cp = *(m_pClass->GetConstPool());
	CCPClassInfo* pClassInfo = (CCPClassInfo*)cp[m_pClass->GetThisClass()];
	CCPUtf8Info* putf8Name = (CCPUtf8Info*)cp[pClassInfo->GetClassInd()];
	m_strName = (string)*putf8Name;
	
	// Get list of interfaces this class implements,
	// populate m_interfaceNames.

	CJInterfaces* pInterfaces = m_pClass->GetInterfaces();
	for (CJInterfaces::iterator iter = pInterfaces->begin();
		 iter != pInterfaces->end();
		 iter++)
	{
		CInterfaceInfo* this_interface = (*iter);
		u2 this_inter_index = this_interface->GetIndex();
		CCPClassInfo* this_inter_class_info = (CCPClassInfo*)cp[this_inter_index];
		u2 this_inter_name_index = this_inter_class_info->GetClassInd();
		CCPUtf8Info* this_inter_name_utf8 = (CCPUtf8Info*)cp[this_inter_name_index];
		string this_inter_name_string = (string)(*this_inter_name_utf8);
		m_interfaceNames.push_back(this_inter_name_string);
	}

	// Setup methods
	for(iterjm = pMethods->begin(); iterjm < pMethods->end(); iterjm++)
	{
		string strName = (string)(*(*iterjm)->GetName());
		string strSign = (string)(*(*iterjm)->GetDescriptor());
		u2 u2AccessFlags = (*iterjm)->GetAccessFlags();
		CCodeAttribute* pCodeAttr = (*iterjm)->GetCode();
		CMethod* pMtd = new CMethodJ(this, strName.c_str(), strSign.c_str(), pCodeAttr, u2AccessFlags);
		if(NULL != pCodeAttr)
		{
			CMethodBody *pBody = new CMethodBody(this, pCodeAttr->GetCode(), pCodeAttr->GetCodeLength());
			pMtd->SetBody(pBody);
		}
		m_pMethods->push_back(pMtd);
	}
	
	CMethods::iterator iterm;
	for(iterm =  m_pMethods->begin(); iterm < m_pMethods->end(); iterm++)
	{
		(*iterm)->Parse();
	}
}

//------------------------------------------------------------------------------
void	
CModuleJ::Emit()
{
	CModule::Emit();
}

//------------------------------------------------------------------------------
void	
CModuleJ::Emit(CJStream& i_jstream)
{
	CModule::Emit();
	m_pClass->Write(i_jstream);
}

//------------------------------------------------------------------------------
CJClassFile&
CModuleJ::GetClass()
{
	if(NULL == m_pClass)
		throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR, "No Java Class");
	return *m_pClass;
}

//------------------------------------------------------------------------------
CJClassBuilder&
CModuleJ::GetClassBuilder()
{
	if(NULL == m_pClass)
		throw CModuleException(CModuleException::X_REASON_INTERNAL_ERROR, "No Java Class");
	return *m_pClass;
}


//- Private methods ------------------------------------------------------------
void
CModuleJ::Verify()
{
	if(NULL == m_pClass)
	{
		throw CModuleException(CModuleException::X_REASON_INVALID_MODULE, "No Java Class");
	}
	//m_pClass->Verify();
	//ToDo: module verification ?
}

const vector<string>&
CModuleJ::GetSourceFileNames()
{
	// If this is the first time through here, populate m_sourceFileNames
	if (!m_sourceFileNamesPopulated) {
		CJClassFile& classFile = GetClass();
		CJAttribs* pAttribs = classFile.GetAttribs();
		for (int i = 0; i < pAttribs->size(); i++) {
			CAttributeInfo* pAttrib = (*pAttribs)[i];
			CCPUtf8Info* pNameInfo = pAttrib->GetName();
			if (((string)(*pNameInfo)).compare("SourceFile") == 0) {
				// Found the SourceFile attribute
				CSourceFileAttribute* psfa = (CSourceFileAttribute*)(pAttrib);
				CCPUtf8Info *putf8 = psfa->GetValue();
				m_sourceFileNames.push_back((string)(*putf8));
				break;
			}
		}
		m_sourceFileNamesPopulated = true;
	}

	return m_sourceFileNames;
}

//==============================================================================
// CMethodJ implementation
//

//------------------------------------------------------------------------------
// Parse
void
CMethodJ::Parse()
{

	// Java-specific parsing: make a label at every line number.
	// Also populate m_LineNumbers for mapping old to new IPs.

	if(NULL != m_pCodeAttr)
	{
		CLineNumberTableAttribute* plinenums = m_pCodeAttr->GetLineNumbers();
		if(NULL != plinenums)
		{
			CLineNumberTable& table = plinenums->GetLineNumberTable();
			u2 u2Leng = plinenums->GetTableLength();
			for(CLineNumberTable::iterator iter = table.begin(); iter != table.end(); iter++)
			{
				IP_t ip = (*iter)->GetStartPC();
				m_LineNumbers.insert(LineNumsEntry_t(ip, ip));
				// NOTE:
				// We create a 'fake' label for every line here
				// to force the parser building additional Insertion Block
				// for every line of code. This will automatically result in
				// slicing a new CInsBlock at the beginning of every line.
				m_Labels.AddLabel(ip);
			}
		}
	}

	// Generic parsing
	CMethod::Parse();

	// Exception handler table parsing
	if(NULL != m_pCodeAttr)
	{
		// Create and parse the exception table.
		m_pMtdExTable = new CMtdExTableJ(this);
		m_pMtdExTable->Parse();

		// Record the original method length for use in local variable table parsing
		m_origCodeLength = m_pCodeAttr->GetCodeLength();
	}
}

//------------------------------------------------------------------------------
// Emit
void
CMethodJ::Emit()
{
	// Generic emission
	CMethod::Emit();
	if(NULL == m_pCodeAttr)
 		return;
	// Update the line number information --------------------------------------
	CLineNumberTableAttribute* plinenums = m_pCodeAttr->GetLineNumbers();
	if(NULL != plinenums)
	{
		CLineNumberTable& table = plinenums->GetLineNumberTable();
		CLineNumberTable::iterator itrTable;
		CLineNumbers::iterator itrLines;

		for(itrTable = table.begin(); itrTable != table.end(); itrTable++)
		{
			
			IP_t ip = (*itrTable)->GetStartPC();
			itrLines = m_LineNumbers.find(ip);
			if(itrLines != m_LineNumbers.end())
				(*itrTable)->SetStartPC(itrLines->second);
		}
	}

	// Update exceptions and local variable mapping ----------------------------
	m_pMtdExTable->Emit();

	//-------
	// Loop over instructions and patch the local variable info table
	//

	// Local variable table LVT
	CLocalVariableTableAttribute* pLclAttr = m_pCodeAttr->GetLocalVariables();
	CLocalVariableTable& LclTable = pLclAttr->GetLocalVariableTable();
	CLocalVariableTable  LclTableSav;
	CLocalVariableTable::iterator itrLcl, itrLclSav;	// LVT iterator
	
	// Local Variable Types Table LVTT /New in Java5/
	// The lvttNull variable is here for the sake of the reference
	// initialization in the case where the method doesn't have an LVTT.
	CLocalVariableTypeTable lvttNull;
	CLocalVariableTypeTableAttribute*  pLclTypeAttr = m_pCodeAttr->GetLocalVariableTypes();
	CLocalVariableTypeTable& LclTypeTable = pLclTypeAttr?pLclTypeAttr->GetLocalVariableTypeTable():lvttNull;
	CLocalVariableTypeTable  LclTypeTableSav;
	CLocalVariableTypeTable::iterator itrLclType, itrLclTypeSav; // LVTT Iterator

	CInsBlocks::iterator iterBlocks;
	IP_t ip = 0;

	if(NULL != pLclAttr)
	{
		LclTableSav = LclTable;		// Save the original LVT
	}
	if (NULL != pLclTypeAttr)
	{
		LclTypeTableSav = LclTypeTable;	// Save the original LVTT
	}
	for(iterBlocks = m_Blocks.begin(); iterBlocks < m_Blocks.end(); iterBlocks++)
	{
		CInstructions* pins = (*iterBlocks)->GetInstructions();
		CInstructions::iterator iterIns;
		IP_t ipOrigBlk = (*iterBlocks)->GetOrigIP();

		for(iterIns = pins->begin(); iterIns != pins->end(); iterIns++)
		{
			IP_t ipOrig = (*iterIns)->GetIP();

			// Scan the local var table. Any entry that refers to the old IP of
			// this instruction gets rewritten to refer to the new IP of this instruction.
			// TODO: worry that this is slow, looping over the local var table for each insn.
			if(NULL != pLclAttr)
			{
				itrLclSav = LclTableSav.begin();
				for(itrLcl = LclTable.begin(); itrLcl != LclTable.end(); itrLcl++, itrLclSav++)
				{
					if(ipOrig == (*itrLclSav)->GetStartPC())
					{	// Original start ip found
						(*itrLcl)->SetStartPC(ip);
					}
					if(ipOrig == (*itrLclSav)->GetStartPC() + (*itrLclSav)->GetLength())
					{	// Original end ip found
						(*itrLcl)->SetLength(ip - (*itrLcl)->GetStartPC());
					}
				}
			}
			// Scan for the local variable type table
			if (NULL != pLclTypeAttr)
			{
				itrLclTypeSav = LclTypeTableSav.begin();
				for(itrLclType = LclTypeTable.begin(); itrLclType != LclTypeTable.end(); itrLclType++, itrLclTypeSav++)
				{
					if(ipOrig == (*itrLclTypeSav)->GetStartPC())
					{	// Original start ip found
						(*itrLclType)->SetStartPC(ip);
					}
					if(ipOrig == (*itrLclTypeSav)->GetStartPC() + (*itrLclTypeSav)->GetLength())
					{	// Original end ip found
						(*itrLclType)->SetLength(ip - (*itrLclType)->GetStartPC());
					}
				}
			}

			// Advance ip
			ip += (*iterIns)->GetSize(ip);
			if(ip >= 0x0000FFFF)
			{ // Code overflow
				string strProcName = m_pModule->GetName();
				strProcName += ".";
				strProcName += GetName();
				throw CModuleException(CModuleException::X_REASON_CODE_OVERRUN, strProcName.c_str());
			}
		}
	}

	// Final variable table handling: need to patch the "ends."
	// That is, those entries whose original PC+length equals the 
	// original size of the method. Patch their length values
	// so they still span to the end of the method.
	//
	// At this point in this function, ip is the new end ip of this method.
	// m_origCodeLength was recorded in the CMethod just for use right here.

	IP_t endOrig = m_origCodeLength;
	if (NULL != pLclAttr) {
		itrLclSav = LclTableSav.begin();
		for(itrLcl = LclTable.begin(); itrLcl != LclTable.end(); itrLcl++, itrLclSav++)
		{
			if(endOrig == (*itrLclSav)->GetStartPC() + (*itrLclSav)->GetLength())
			{	// end ip of this variable region is the original end ip of the method
				(*itrLcl)->SetLength(ip - (*itrLcl)->GetStartPC());
			}
		}
	}
	
	// Do the same for the LVTT
	if(NULL != pLclTypeAttr)
	{
		itrLclTypeSav = LclTypeTableSav.begin();
		for(itrLclType = LclTypeTable.begin(); itrLclType != LclTypeTable.end(); itrLclType++, itrLclTypeSav++)
		{
			if(endOrig  == (*itrLclTypeSav)->GetStartPC() + (*itrLclTypeSav)->GetLength())
			{
					(*itrLclType)->SetLength(ip - (*itrLclType)->GetStartPC());
			}
		}
	}

	CalcStackDepth();

	// Replace the method body
	m_pCodeAttr->SetCode(m_pBody->GetCodeSize(), m_pBody->GiveAvayCode());

}

//------------------------------------------------------------------------------
// CalcStackDepth
//
int
CMethodJ::CalcStackDepth()
{
	int nStack = CMethod::CalcStackDepth();
	m_pCodeAttr->SetMaxStack(nStack);
	return nStack;
}

//==============================================================================
// CMtdExtableJ
//------------------------------------------------------------------------------
// For historical reasons the end of an exception block in a Java exception
// table points to the next instrucion after the protected block.
// Since the abstract exception table assumes the end of the exception block
// points to the start of the last instruction of the block, we need to recalculate
// the protected block as we parse the Java exception table.
// (We will undo this operation when we emit the table.)
//
//------------------------------------------------------------------------------
void	
CMtdExTableJ::Parse()
{
	CMethodJ* pmtd = (CMethodJ*)m_pmtd;
	CExTable& extblj = pmtd->GetCodeAttribute()->GetExTable();
	CExTable::iterator itrexj = extblj.begin();

	if(itrexj == extblj.end())
		return;		// No exceptions in this method!
	// Get exception table
	CMtdExTable* pMtdExTable = pmtd->GetExTable();
	// Patch the end of exception block addresses
	CInsBlocks* pblks = pmtd->GetInsBlocks();
	CInsBlocks::iterator itrBlks;
	CInstructions::iterator itrIns;

	for(itrBlks = pblks->begin(); itrBlks != pblks->end(); itrBlks++)
	{
		CInsBlock* pblk = *itrBlks;
		CInstructions* pins = pblk->GetInstructions();
		for(itrIns = pins->begin(); itrIns != pins->end(); itrIns++)
		{
			CInstruction* pi = *itrIns;
			IP_t ipIns = pi->GetIP();
			for(itrexj = extblj.begin(); itrexj != extblj.end(); itrexj++)
			{
				unsigned uType = itrexj->GetCatchtype();
				IP_t ipStart = itrexj->GetStartPC();
				IP_t ipEnd = itrexj->GetEndPC();
				if(ipEnd == pi->GetIP() + pi->GetSize())
				{
					ipEnd = pi->GetIP();
					CInsBlock* pblkHandler = pmtd->FindBlock(itrexj->GetHandlerPC());
					pmtd->AddException(new CMethodExceptionJ(uType, ipStart, ipEnd, pblkHandler));
				}
			}
		}
	}
}

//------------------------------------------------------------------------------
// Emit the exception table
// ToDo: Exception tables entries may be hashed to accelerate the process
void	
CMtdExTableJ::Emit()
{
	CMethodJ* pmtd = (CMethodJ*)m_pmtd;
	CLabels* plabels = pmtd->GetLabels();
	CMtdExTable* pMtdExTable = pmtd->GetExTable();
	CMtdExTable::iterator itrEx;
	CInsBlocks* pblks = pmtd->GetInsBlocks();
	CInsBlocks::iterator itrBlks;
	CInstructions::iterator itrIns;

	// Scrap the old exception table
	CExTable& extblj = pmtd->GetCodeAttribute()->GetExTable();
	extblj.clear();
	// Create new exception table
	for(itrEx = pMtdExTable->begin(); itrEx != pMtdExTable->end(); itrEx++)
	{
		bool bDone = false;
		CMethodExceptionJ* pmtdexj = (CMethodExceptionJ*)*itrEx;
		unsigned uType = pmtdexj->GetType();
		IP_t ipStart = plabels->GetLabelInstructionTarget(pmtdexj->GetStart());
		IP_t ipEnd = plabels->GetLabelInstructionTarget(pmtdexj->GetEnd());
		IP_t ipHandler = pmtdexj->GetHandler()->GetLabel();
		IP_t ipRealEnd = ipEnd;

		// Java has the end of exception block marked by the IP of
		// instruction following the last instruction in the block.
		// To folow this awkward requirement we have to scan all 
		// instructions for every exception table entry and recalculate
		// the end address.
		// Note: this implementation takes a brute force approach for simplicity.
		// The performance may be significantly improved with cashing.
		for(itrBlks = pblks->begin(); !bDone && itrBlks != pblks->end(); itrBlks++)
		{
			CInsBlock* pblk = *itrBlks;
			CInstructions* pins = pblk->GetInstructions();
			for(itrIns = pins->begin(); itrIns != pins->end(); itrIns++)
			{
				CInstruction* pi = *itrIns;
				IP_t ipIns = pi->GetIP();
				if(plabels->IsLabel(ipIns))
				{
					ipIns = plabels->GetLabelInstructionTarget(ipIns);
					if(ipIns == ipEnd)
					{
						ipRealEnd = ipIns + pi->GetSize();
						bDone = true;
						break;
					}
				}
			}
		}
		CJException jex = CJException(uType, ipStart, ipRealEnd, ipHandler); 
		extblj.push_back(jex);
	}
}

void
CMtdExTableJ::Dump(ostream &i_os) const
{
	i_os << "Exception Table (end refers to start of last instruction in region):" << endl;
	i_os << "Type\tStart\tEnd\tHandler" << endl;

	CMtdExTable::const_iterator itrEx;
	for(itrEx = begin(); itrEx != end(); itrEx++)
	{
		CMethodExceptionJ* pmtdexj = (CMethodExceptionJ*)*itrEx;
		unsigned uType = pmtdexj->GetType();
		IP_t ipStart = pmtdexj->GetStart();
		IP_t ipEnd = pmtdexj->GetEnd();
		IP_t ipHandler = pmtdexj->GetHandler()->GetLabel();

		i_os << uType << "\t" << ipStart << "\t" << ipEnd << "\t" << ipHandler << endl;
	}
}

//==============================================================================
// CSerialVersionUIDHelper class
//
// This class is useful in computing the serialVersionUID value for a Java class.
//
// Its public API should be to take a class as input and produce 
// the serialVersionUID as output. Unfortunately, we haven't worked out
// the legal implications of implementing SHA, so instead it returns
// a byte array that contains the bytes you need to compute SHA over.
//
// [All areas of code related to serialVersionUID can be found by looking
// for that word in the comments.]
//

CSerialVersionUIDHelper::CSerialVersionUIDHelper(CModuleJ* pModule)
{
	// Initialize to an empty state
	data = (u1*)malloc(CHUNK_SIZE);
	used_length = 0;
	allocated_length = CHUNK_SIZE;

	// Call the builder
	BuildSUIDByteArray(pModule);
}

// ---------------------------------------------------------------------
// BuildSUIDByteArray
//
// This function builds a byte array in the exact format required
// for computing the serialVersionUID for the class.
//
// When you call this function, the module should be in its uninstrumented
// state - or at least with no changes that affect the byte array being built.
//
// The specs for how to build the byte array can be found at Sun, in
// a document called "Java(TM) Object Serialization Specification."
// For release 1.4.2 the pertinent section is numbered 4.6, "Stream Unique 
// Identifiers." I don't want to paste the spec into this comment because Sun 
// holds the copyright.
//
// Beware of the following clarifications of the Sun spec:
//
// All ints are written as 32-bit big-endian.
//
// All strings are written as a two-byte length (big-endian), followed
// by the modified UTF8 encoding with no trailing null.
//
// Sorting UTF8's can be done with a byte-wise unsigned-char sort. As long
// as the strings have no internal nulls, the result is the same as a numerical
// sort by the char values in the un-encoded Unicode string.
//
// The class name itself and the interfaces it implements are written as
// fully-qualified strings with dots as separators: java.lang.Object
//
// The class modifiers to be written include only public, final, interface,
// and abstract. Not "super."
//
// The class modifiers for arrays should have the "abstract" modifier bit set.
//
// The class modifiers for interfaces which are not public should 
// have "abstract" unset.
//
// When writing the list of interfaces that are implemented, array classes should
// appear as if they do not implement any interfaces, even if the JVM makes it
// look like they implement Serializable or other interfaces.
//
// When writing the list of interfaces that are implemented, use the
// fully-qualified name with dots, not slashes: java.io.Serializable
//
// When writing the fields, the field descriptor is written in internal
// format with slashes as separators: Ljava/lang/Object;
//
// Method descriptors (arguments and return type) are written in internal
// format, with dots as separators: ([Ljava.lang.String;)V
//
// (This function doesn't bother with some of the caveats above, since it will
// never be called upon to compute the serialVersionUID for array classes or
// interfaces.)
//

void
CSerialVersionUIDHelper::BuildSUIDByteArray(CModuleJ* pModule)
{
	CJClassFile& cf = pModule->GetClass();
	CConstPool* cp = cf.GetConstPool();
	CCPUtf8Info* pUTF8;
	u4 value;
	int i;

	// Write the class name, using dots not slashes

	u2 thisClass = cf.GetThisClass();
	pUTF8 = cp->GetClass(thisClass);
	this->ConvertWithDotsAndAppend(pUTF8);

	// Write the class modifiers as an int, but only certain ones

	value = cf.GetAccessFlags();
	value &= (ACC_PUBLIC | ACC_FINAL | ACC_INTERFACE | ACC_ABSTRACT);
	if ((value & ACC_INTERFACE) && !(value & ACC_PUBLIC))
		value &= ~ACC_ABSTRACT;	// caveat: clear "abstract" for non-public interfaces
	this->Append(value);

	// Write the interfaces, sorted by name.

	CJInterfaces* ifaces = cf.GetInterfaces();
	int ifaceCount = ifaces->size();
	if (ifaceCount > 0) {
		// Note: Sorting the UTF8-encoded names by byte values is identical to
		// sorting the corresponding Unicode strings as long as no string
		// contains a null byte inside.

		CCPUtf8Info** ifaceArray = new CCPUtf8Info*[ifaceCount];
		for (i = 0 ; i < ifaceCount; i++) {
			u2 ifaceClassIndex = (*ifaces)[i]->GetIndex();
			CCPInfo* inf = (*cp)[ifaceClassIndex];
			CCPClassInfo* pClassInf = (CCPClassInfo*)inf;
			u2 ifaceNameIndex = pClassInf->GetClassInd();
			inf = (*cp)[ifaceNameIndex];
			CCPUtf8Info* ifaceNameUTF = (CCPUtf8Info*)(*inf);
			ifaceArray[i] = ifaceNameUTF;
		}

		// Sort the interface name array and append its contents
		// (This gives a warning on Solaris but appears to work.)
		::qsort(ifaceArray, 
				ifaceCount, 
				sizeof(CCPUtf8Info*), 
				CSerialVersionUIDHelper::qsort_compare_CCPUtf8Info);
		for (i = 0; i < ifaceCount; i++) {
			this->ConvertWithDotsAndAppend(ifaceArray[i]);
		}
		delete[] ifaceArray;
	}

	// Write the fields, sorted by name, as field, modifier, and descriptor
	CJFields* fields = cf.GetFields();
	int fieldCount = fields->size();
	if (fieldCount > 0) {
		// Allocate the array big enough to hold 'em all,
		// then populate only with those that qualify.
		CFieldInfo** fieldArray = new CFieldInfo*[fieldCount];
		int qualifyingFieldCount = 0;
		for (i = 0; i < fields->size(); i++) {
			CFieldInfo* pField = (*fields)[i];
			u2 flags = pField->GetAccessFlags();
			bool isPrivateTransient = (flags & ACC_PRIVATE) && (flags & ACC_TRANSIENT);
			bool isPrivateStatic = (flags & ACC_PRIVATE) && (flags & ACC_STATIC);
			if (!(isPrivateTransient || isPrivateStatic)) {
				fieldArray[qualifyingFieldCount++] = pField;
			}
		}

		// Sort the field array and append the fields
		// (This gives a warning on Solaris but appears to work.)
		::qsort(fieldArray, 
			qualifyingFieldCount, 
			sizeof(CFieldInfo*), 
			CSerialVersionUIDHelper::qsort_compare_CFieldInfo);

		for (i = 0; i < qualifyingFieldCount; i++) {
			this->Append(fieldArray[i]->GetName());
			this->Append(fieldArray[i]->GetAccessFlags());
			this->ConvertWithSlashesAndAppend(fieldArray[i]->GetDescriptor());
		}
		delete[] fieldArray;
	}

	// Build and sort a method table
	CJMethods* methods = cf.GetMethods();
	int methodCount = methods->size();

	CJMethodInfo** methodArray = new CJMethodInfo*[methodCount];
	for (i = 0; i < methodCount; i++) {
		methodArray[i] = (*methods)[i];
	}
	// (This gives a warning on Solaris but appears to work.)
	::qsort(methodArray, 
			methodCount, 
			sizeof(CJMethodInfo*), 
			CSerialVersionUIDHelper::qsort_compare_CJMethodInfo);

	// If <clinit> is present, write it as name/mod/sig
	// and remove it from the now-sorted array (by setting its entry to NULL).
	for (i = 0; i < methodCount; i++) {
		CJMethodInfo* pMethod = methodArray[i];
		CCPUtf8Info* pMethodName = pMethod->GetName();
		if ((pMethodName->GetLength() == 8) && 
			(memcmp(pMethodName->GetBytes(), "<clinit>", 8) == 0)) 
		{
			// yes, Virginia, there is a <clinit> method
			this->Append(pMethodName);
			this->Append(ACC_STATIC);
			this->AppendUTF((u1*)"()V", 3);
			methodArray[i] = NULL;	// Clear this out so we don't see it again
			break;					// There can only be one.
		}
	}

	// Write non-private <init> methods, sorted by signature, as name/mod/sig
	// and remove them from the array (by setting the entry to NULL).
	// While we're at it, NULL out all private methods so we don't see them again.
	for (i = 0; i < methodCount; i++) {
		if (methodArray[i] != NULL) {
			CJMethodInfo* pMethod = methodArray[i];
			CCPUtf8Info* pMethodName = pMethod->GetName();
			u2 modifiers = pMethod->GetAccessFlags();
			if (!(modifiers & ACC_PRIVATE)) {
				if ((pMethodName->GetLength() == 6) &&
					(memcmp(pMethodName->GetBytes(), "<init>", 6) == 0)) 
				{
					this->Append(pMethodName);
					this->Append(modifiers);
					this->ConvertWithDotsAndAppend(pMethod->GetDescriptor());
					// Clear out this non-private constructor
					methodArray[i] = NULL;
				}
				// else do nothing: this is a non-private, non-constructor
			}
			else {
				// Private method. Clear out its entry.
				methodArray[i] = NULL;
			}
		}
	}

	// All that's left in methodArray is non-private, non-constructors.
	// Write them out, as name/mod/sig
	for (i = 0; i < methodCount; i++) {
		if (methodArray[i] != NULL) {
			CJMethodInfo* pMethod = methodArray[i];
			CCPUtf8Info* pMethodName = pMethod->GetName();
			this->Append(pMethodName);
			this->Append(pMethod->GetAccessFlags());
			CCPUtf8Info* pDesc = pMethod->GetDescriptor();
			this->ConvertWithDotsAndAppend(pDesc);
		}
	}

	delete[] methodArray;
}

void CSerialVersionUIDHelper::Append(const u1* bytes, u2 length) 
{
	if (used_length + length > allocated_length) {
		allocated_length = used_length + length + CHUNK_SIZE;
		data = (u1*)realloc(data, allocated_length);
	}
	memcpy(data + used_length, bytes, length);
	used_length += length;
}

void CSerialVersionUIDHelper::Append(CCPUtf8Info* pUTF8) 
{
	u1* bytes = pUTF8->GetBytes();
	u2 length = pUTF8->GetLength();
	AppendUTF(bytes, length);
}

void CSerialVersionUIDHelper::Append(int value) 
{
	u1 buffer[4];
	buffer[0] = (value >> 24);
	buffer[1] = (value >> 16);
	buffer[2] = (value >> 8);
	buffer[3] = (value);
	Append(buffer, 4);
}

void CSerialVersionUIDHelper::AppendUTF(const u1* bytes, u2 length) 
{
	u1 prefix[2];
	prefix[0] = (length >> 8) & 0xff;
	prefix[1] = length & 0xff;
	Append(prefix, 2);
	Append(bytes, length);
}

// Convert slashes to dots and append
void CSerialVersionUIDHelper::ConvertWithDotsAndAppend(CCPUtf8Info* pUTF8) 
{
	u1* bytes = pUTF8->GetBytes();
	u2 len = pUTF8->GetLength();
	u1* newBytes = new u1[len];
	memcpy(newBytes, bytes, len);
	for (int j = 0; j < len; j++)
		if (newBytes[j] == '/')
			newBytes[j] = '.';
	AppendUTF(newBytes, len);
	delete[] newBytes;
}

// Convert slashes to dots and append
void CSerialVersionUIDHelper::ConvertWithSlashesAndAppend(CCPUtf8Info* pUTF8) 
{
	u1* bytes = pUTF8->GetBytes();
	u2 len = pUTF8->GetLength();
	u1* newBytes = new u1[len];
	memcpy(newBytes, bytes, len);
	for (int j = 0; j < len; j++)
		if (newBytes[j] == '.')
			newBytes[j] = '/';
	AppendUTF(newBytes, len);
	delete[] newBytes;
}

// Custom unsigned_memcmp implementation: some UNIXes improperly do *signed* compares!
int 
CSerialVersionUIDHelper::unsigned_memcmp(const unsigned char* left, const unsigned char* right, int len)
{
	while (len--) {
		if (*left++ != *right++) {
			if (*(--left) > *(--right)) return 1;
			else return -1;
		}
	}
	return 0;
}

//------------------------------------------------------------------------------
// qsort_compare_utf8: compare two UTF8's for qsort purposes.
//
// This function does a byte-wise compare of the UTF8 bytes.
// The result is the same as a short-wise compare of the numeric values
// of the corresponding Unicode characters, with just one exception: if 
// there is an interior null in a string, it will appear to be "greater than"
// a non-null character in the other string, if the character
// in the other string is repesented with a single UTF8 byte.
//
// Consider that restriction when sort order is critical. In the case
// of sorting interface names, field names, method names, and
// method signature strings, the restriction is irrelevant because
// those strings can't have nulls in them.
// 
// For performance reasons, this function does not type-check the pointers.
// 
// This function is used to sort an array of pointers to UTF8's. Therefore
// each argument is a pointer to a pointer to a UTF8.
//
// It is also used as a helper in other qsort_compare functions; those have
// to arrange for the arguments ALSO to be pointer-to-pointer-to-CCPUtf8Info.
//

int 
CSerialVersionUIDHelper::qsort_compare_CCPUtf8Info(const void* vleft, const void* vright)
{
	const CCPUtf8Info* left = *(const CCPUtf8Info**)vleft;
	const CCPUtf8Info* right = *(const CCPUtf8Info**)vright;
	u1* leftBytes = left->GetBytes();
	u2 leftLength = left->GetLength();
	u1* rightBytes = right->GetBytes();
	u2 rightLength = right->GetLength();

	int len_of_shorter = (leftLength < rightLength ? leftLength : rightLength);
	int result = unsigned_memcmp(leftBytes, rightBytes, len_of_shorter);
	if (result != 0) {
		// byte mismatch before end of shorter string: that's our answer.
		return result;
	}
	else {
		// they were the same up to the end of the shorter one
		if (leftLength < rightLength) 
			return -1;
		else if (leftLength > rightLength) 
			return 1;
		else 
			return 0;
	}
}


//------------------------------------------------------------------------------
// qsort_compare_CFieldInfo: compare for qsort purposes.
//
// Pull the UTF8s for the field names and call qsort_compare on them.
//
// See CCPUtf8Info::qsort_compare for details on comparing two UTF8's.
//
// This function is used to sort an array of pointers to CFieldInfo's.
// Therefore each argument is a pointer to a pointer to a CFieldInfo.
//

int
CSerialVersionUIDHelper::qsort_compare_CFieldInfo(const void* vleft, const void* vright)
{
	CFieldInfo* left = *(CFieldInfo**)vleft;
	CFieldInfo* right = *(CFieldInfo**)vright;

	CCPUtf8Info* leftName = left->GetName();
	CCPUtf8Info* rightName = right->GetName();
	int result = qsort_compare_CCPUtf8Info(&leftName, &rightName);
	return result;
}


//------------------------------------------------------------------------------
// qsort_compare_CJMethodInfo: compare CJMethodInfo's for qsort purposes.
//
// This implementation is based on the requirements of computing the
// serialVersionUID of a class. The sort order is based first on the
// method names, and then (if they are equal) on the signature strings.
//
// See CCPUtf8Info::qsort_compare for details on comparing two UTF8's.
//
// This function is used to sort an array of pointers to CJMethodInfo
// objects. Therefore each argument is a pointer to a pointer to a CJMethodInfo.
//

int
CSerialVersionUIDHelper::qsort_compare_CJMethodInfo(const void* vleft, const void* vright)
{
	CJMethodInfo* left = *(CJMethodInfo**)vleft;
	CJMethodInfo* right = *(CJMethodInfo**)vright;

	CCPUtf8Info* leftName = left->GetName();
	CCPUtf8Info* rightName = right->GetName();
	int result = qsort_compare_CCPUtf8Info(&leftName, &rightName);
	if (result != 0)
		return result;

	// Names are the same, compare descriptors (signatures)
	CCPUtf8Info* leftSig = left->GetDescriptor();
	CCPUtf8Info* rightSig = left->GetDescriptor();
	result = qsort_compare_CCPUtf8Info(&leftSig, &rightSig);
	return result;
}


//= End of modulej.cpp =========================================================

