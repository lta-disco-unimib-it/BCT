 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JDump.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 


//==============================================================================
// JDump.cpp
//------------------------------------------------------------------------------
// Java class file dump utility V 1.0 by VLH
//
// Uses JClass.dll
//------------------------------------------------------------------------------
//
//------------------------------------------------------------------------------
// Notes:
//   It would be much better to inherit JClassDump from JClassFile and 
//   implement it there.
//------------------------------------------------------------------------------
// Usage:
/*
		JDump [options] <file>
		Options
		-h      - print help
		-n:x    - hex. output
		-n:d    - dec. output
		-hd[-]  - class header
		-cp     - constant pool
		-cp:r   - resolve constant pool
		-if     - interfaces
		-if:r   - resolve interfaces
		-fl     - fields
		-fl:r   - resolve fields
		-mt     - methods
		-mt:r   - resolve methods
		-mt:t	- dump throws
		-mt:b	- dump bytecode
		-mt:g	- dump debug information
		-mt:e	- dump exceptions
		-at		- attributes
		-all	- dump all
		-all:r	- dump all resolved
*/
//==============================================================================

#include "CommonDef.h"
#include "JClassFile.h"
#include "JFileStream.h"
#include "InsSet.h"
#include "JVMInsSet.h"
#include "Command.h"

#include <iostream>


static CSTR JDUMP_VERSION_STR	= "1.0";		// JDump version

static CSTR CommandExceptionMsg[] =
{
	"Unknown exception",
	"Bad option",
	"Bad Value",
	"Bad file name",
	"?!"
};

static CSTR AccFlagStr[] = 
{
	"public",			// 0x0001
	"private",			// 0x0002
	"protected",		// 0x0004
	"static",			// 0x0008
	"final",			// 0x0010
	"synchronized",		// 0x0020	("super" for interfaces)
	"volatile",			// 0x0040
	"transient",		// 0x0080
	"native",			// 0x0100
	"interface",		// 0x0200
	"abstract",			// 0x0400
};

static int MAX_JAVA_FLAGS = sizeof(AccFlagStr)/sizeof(CSTR);

enum {
	FLAG_RESOLVE		= 0x0001,
	FLAG_DUMP_CODE		= 0x0002,
	FLAG_DUMP_DEBUG		= 0x0004,
	FLAG_DUMP_EXCEPT	= 0x0008,
	FLAG_DUMP_THROWS	= 0x0010,
	FLAG_DUMP_ATTRIB	= 0x0020
};

enum
{
	RET_OK				= 0,
	RET_CMD_ERROR		= 1,
	RET_FILE_ERROR		= 2,
	RET_CLASS_ERROR		= 3,
	RET_INTERNAL_ERROR	= 4,
	RET_LAST
};

static ostream& os = cout;	//TODO: may be os must be an option?

static CInsSetJ JVMInsSet;


#define LONG_FROM_4_BYTES(i)    \
		( (long)pBytes[i]<<24 | (long)pBytes[i+1]<<16 |      \
			(long)pBytes[i+2]<<8 | (long)pBytes[i+3] )

//------------------------------------------------------------------------------
// Print help
//
static void PrintHelp()
{
	cout << "Usage: JDump [options] <file>" << "\n";
	cout << "Options\n";
	cout << "\t-h      - print help\n";
	cout << "\t-n:x    - hex. output\n";
	cout << "\t-n:d    - dec. output\n";
	cout << "\t-hd[-]  - class header (default +)\n";
	cout << "\t-cp     - constant pool\n";
	cout << "\t-cp:r   - resolve constant pool\n";
	cout << "\t-if     - interfaces\n";
	cout << "\t-if:r   - resolve interfaces\n";
	cout << "\t-fl     - fields\n";
	cout << "\t-fl:a   - fields with attributes\n";
	cout << "\t-fl:r   - resolve fields\n";
	cout << "\t-mt     - methods\n";
	cout << "\t-mt:b   - dump byte code\n";
	cout << "\t-mt:e   - dump exceptions\n";
	cout << "\t-mt:g   - dump debug info\n";
	cout << "\t-mt:r   - resolve methods\n";
	cout << "\t-mt:t   - dump throws\n";
	cout << "\t-at     - attributes\n";
	cout << "\t-all    - dump all\n";
	cout << "\t-all:r  - dump all resolved\n";
	cout << "\n";
	cout << "Example jdump -all:r -mt:gbt test.class\n";
	cout << "(Dump all resolved, methods with debug info, byte code and throws)\n";

}

//------------------------------------------------------------------------------
// Format Class Access flags to string
//
static void AccToString(u2 i_u2AccFlags, string& o_strFlags, bool isClass = false)
{
	unsigned mask = 1;
	o_strFlags = "";
	for(int i=0; i < MAX_JAVA_FLAGS; i++)
	{
		if(i_u2AccFlags & mask)
		{
			if(isClass && i == 5)	// thank you, Sun!!!
			{
				o_strFlags += "super";
			}
			else
			{
				o_strFlags += AccFlagStr[i];
			}
			o_strFlags += " ";
		}
		mask <<= 1;
	}
}

//------------------------------------------------------------------------------
// Dump Class Header
//
static void DumpClassHeader(CJClassFile& i_Class)
{
	CConstPool& cp = *(i_Class.GetConstPool());
	CCPUtf8Info* pUtf8;
	CCPClassInfo* pClassInfo;
	// Get this class string
	pClassInfo = (CCPClassInfo*)cp[i_Class.GetThisClass()];
	pUtf8 = (CCPUtf8Info*)cp[pClassInfo->GetClassInd()];
	string strThisClass((CSTR)pUtf8->GetBytes(), pUtf8->GetLength());
	// Get super class string
	u2 u2SuperInd = i_Class.GetSuperClass();
	string strSuperClass;
	if(u2SuperInd > 0)
	{
		pClassInfo = (CCPClassInfo*)cp[u2SuperInd];
		pUtf8 = (CCPUtf8Info*)cp[pClassInfo->GetClassInd()];
		strSuperClass = string((CSTR)pUtf8->GetBytes(), pUtf8->GetLength());
	}
	// Get access flags
	string strFlags;
	AccToString(i_Class.GetAccessFlags(), strFlags, true);
	CJAttribs* pattribs = i_Class.GetAttribs();
	// Get known attributes
	string strSourceFile;
	string strSourceDir("<undefined>");
	for(CJAttribs::iterator iter = pattribs->begin(); iter < pattribs->end(); iter++)
	{
		if(*(*iter)->GetName() == "SourceFile")
		{
			CSourceFileAttribute* psfa = (CSourceFileAttribute*)*iter;
			CCPUtf8Info *putf8 = psfa->GetValue();
			strSourceFile = (string)*putf8;
		}
		else if(*(*iter)->GetName() == "SourceDir")
		{
			CSourceDirAttribute* psda = (CSourceDirAttribute*)*iter;
			CCPUtf8Info *putf8 = psda->GetValue();
			strSourceDir = (string)*putf8;
		}
		// No other attributes are documented for the header.
	}

	// Output everything
	os	<< "Version: " << i_Class.GetMajorVersion() << "." 
		<< i_Class.GetMinorVersion() << "\n";
	os	<< "Access: " << i_Class.GetAccessFlags() << " (" << strFlags << ")\n";
	os	<< "This  Class: " << strThisClass << "\n";
	os	<< "Super Class: " << strSuperClass << "\n";
	os	<< "Source File: " << strSourceFile << "\n";
	os	<< "Source Dir : " << strSourceDir << "\n";
}

//------------------------------------------------------------------------------
// Dump Constant Pool
// Dump the whichOne'th constant, or all if whichOne==0.
//
static void DumpConstantPool(CJClassFile& i_Class, unsigned i_flags = 0, int whichOne =0)
{
	CConstPool* pcp = i_Class.GetConstPool();
	int n = 0;
	if (whichOne == 0)
	{
		// Dumping all constants, we need a header line.
		os << "\nConstant pool\n";
	}
	CConstPool::iterator iter = pcp->begin();
	while(++iter < pcp->end())
	{
		if (whichOne)
		{
			if (whichOne > ++n)
				continue;
		}
		else
			os << ++n << ". ";
		switch((*iter)->GetTag())
		{
		case CONSTANT_Utf8:					// UTF8 constant info
			{
				CCPUtf8Info* pcputf8 = (CCPUtf8Info*)*iter;
				os << "Utf8: " << string(*pcputf8);
			}
			break;
		case CONSTANT_Integer:				// integer constant info
			{
				CCPIntegerInfo* pcpint = (CCPIntegerInfo*)*iter;
				os << "Integer: " << pcpint->GetBytes();
			}
			break;
		case CONSTANT_Float:				// float constant info
			{
				CCPFloatInfo* pcpfloat = (CCPFloatInfo*)*iter;
				os << "Float: " << pcpfloat->GetFloat();
			}
			break;
		case CONSTANT_Long:					// long constant info
			{
				CCPLongInfo* pcplong = (CCPLongInfo*)*iter;
				os << "Long: " << (long)pcplong->GetLong();
			}
			iter++, n++;
			break;
		case CONSTANT_Double:				// double constant info
			{
				CCPDoubleInfo* pcpdouble = (CCPDoubleInfo*)*iter;
				os << "Double: " << pcpdouble->GetDouble();
			}
			iter++, n++;
			break;
		case CONSTANT_Class:				// class constant info
			{
				CCPClassInfo* pcpclass = (CCPClassInfo*)*iter;
				u2 u2ClassInd = pcpclass->GetClassInd();
				os << "Class: ";
				if(i_flags & FLAG_RESOLVE)
				{
					os << string(*pcp->GetClass(n));
				}
				else
				{
					 os << "[" << u2ClassInd << "]";
				}
			}
			break;
		case CONSTANT_String:				// string constant info
			{
				CCPStringInfo* pcpstring = (CCPStringInfo*)*iter;
				os << "String: ";
				if(i_flags & FLAG_RESOLVE)
				{
					CCPUtf8Info* pcputf8 = pcp->GetString(n);
					os << string(*pcputf8);
				}
				else
				{
					os << "[" << pcpstring->GetStringInd() << "]";
				}
			}
			break;
		case CONSTANT_Fieldref:				// field ref constant info
			{
				CCPFieldrefInfo* pcpfield = (CCPFieldrefInfo*)*iter;
				u2 u2ClassInd = pcpfield->GetClassInd();
				u2 u2NameTypeInd = pcpfield->GetNameAndTypeInd();
				os	<< "FieldRef: " ;
				if(i_flags & FLAG_RESOLVE)
				{
					string strClass = *(pcp->GetClass(u2ClassInd));
					string strName = *(pcp->GetName(u2NameTypeInd));
					string strType = *(pcp->GetType(u2NameTypeInd));
					os << strClass << " : " << strName << " : " << strType;
				}
				else
				{
					os	<< "Class[" << u2ClassInd << "] "
						<< "NameAndType[" << u2NameTypeInd << "] ";
				}
			}
			break;
		case CONSTANT_Methodref:			// method ref constant info
			{
				CCPInterfaceMethodrefInfo* pcpmethod = (CCPInterfaceMethodrefInfo*)*iter;
				u2 u2ClassInd = pcpmethod->GetClassInd();
				u2 u2NameAndTypeInd = pcpmethod->GetNameAndTypeInd();

				os	<< "MethodRef: "; 
				if(i_flags & FLAG_RESOLVE)
				{
					os	<< string(*pcp->GetClass(u2ClassInd)) << " : "
						<< string(*pcp->GetName(u2NameAndTypeInd)) << " : "
						<< string(*pcp->GetType(u2NameAndTypeInd));
				}
				else
				{
					os	<< "Class[" << u2ClassInd << "] "
						<< "NameAndType[" << u2NameAndTypeInd << "] ";
				}
			}
			break;
		case CONSTANT_InterfaceMethodref:	// interface method ref constant info
			{
				CCPInterfaceMethodrefInfo* pcpmethod = (CCPInterfaceMethodrefInfo*)*iter;
				u2 u2ClassInd = pcpmethod->GetClassInd();
				u2 u2NameAndTypeInd = pcpmethod->GetNameAndTypeInd();

				os	<< "InterfaceMethodRef: " ;
				if(i_flags & FLAG_RESOLVE)
				{
					os	<< string(*pcp->GetClass(u2ClassInd)) << " : "
						<< string(*pcp->GetName(u2NameAndTypeInd)) << " : "
						<< string(*pcp->GetType(u2NameAndTypeInd));
				}
				else
				{
					os	<< "Class[" << u2ClassInd << "] "
						<< "NameAndType[" << u2NameAndTypeInd << "] ";
				}
			}
			break;
		case CONSTANT_NameAndType:			// name and type constant info
			{
				CCPNameAndTypeInfo* pcpnt = (CCPNameAndTypeInfo*)*iter;

				os	<< "NameAndType: ";
				if(i_flags & FLAG_RESOLVE)
				{
					os	<< string(*pcp->GetName(n)) << " "
						<< string(*pcp->GetType(n));
				}
				else
				{
					os	<< "Name[" << pcpnt->GetNameInd() << "] "
						<< "Descriptor[" << pcpnt->GetDescriptorInd() << "] ";
				}
			}
			break;
		default:
			os << "Unknown constant type";
			break;
		}
		if (whichOne != 0)
		{
			return;	// Done reporting on this constant
		}
		os << "\n";
	}
}

//------------------------------------------------------------------------------
// Dump Interfaces
//
static void DumpInterfaces(CJClassFile& i_Class, unsigned i_flags = 0)
{
	CJInterfaces* pinterf = i_Class.GetInterfaces();
	int n = 0;
	os << "\nInterfaces:\n";
	for(CJInterfaces::iterator iter = pinterf->begin(); iter < pinterf->end(); iter++)
	{
		int ind = (*iter)->GetIndex();
		os << n++ << ".";
		if(i_flags & FLAG_RESOLVE)
		{
			CConstPool* pcp = i_Class.GetConstPool();
			os << string(*pcp->GetClass(ind));
		}
		else
		{
			os << "Name[" << ind << "]\n";
		}
	}
}

//------------------------------------------------------------------------------
// Dump Field Attributes
//
static void DumpAttributes(CJAttribs& i_Attr, unsigned i_flags)
{
	CJAttribs::iterator iter;
	int n = 0;
	if(i_Attr.size() > 0)
	{
		os << "\tAttributes:\n";
	}
	for (iter = i_Attr.begin(); iter < i_Attr.end(); iter++)
	{
		string strName = *(*iter)->GetName();
		os	<< "\t" << n++ << "."
			<< " Name: " << strName 
			<< " Size: " << (*iter)->GetLength() - 6;
		if(strName == "ConstantValue")
		{
			os << " Index: " << ((CConstantValueAttribute*)*iter)->GetConstantInd();
		}

		 os	<< "\n";
	}
}

//------------------------------------------------------------------------------
// Dump Fields
//
static void DumpFields(CJClassFile& i_Class, unsigned i_flags = 0)
{
	CJFields* pfields = i_Class.GetFields();
	int n = 0;
	os << "\nFields:\n";
	for(CJFields::iterator iter = pfields->begin(); iter < pfields->end(); iter++)
	{
		CFieldInfo* pfld = *iter;
		os	<< n++ << ".";
		if(i_flags & FLAG_RESOLVE)
		{
			string strFlags;
			CConstPool& cp = *(i_Class.GetConstPool());
			CCPUtf8Info* putf8Name = (CCPUtf8Info*)cp[pfld->GetNameInd()];
			CCPUtf8Info* putf8Type = (CCPUtf8Info*)cp[pfld->GetDescriptorInd()];
			AccToString(pfld->GetAccessFlags(), strFlags);

			os	<< strFlags
				<< string(*putf8Name) << " "
				<< string(*putf8Type) << "\n";
		}
		else
		{
			os	<< "Access["   << pfld->GetAccessFlags() <<"] "
				<< "Name["	   << pfld->GetNameInd() <<"] "
				<< "Descriptor[" << pfld->GetDescriptorInd() << "]\n";
		}
		if(i_flags & FLAG_DUMP_ATTRIB)
			DumpAttributes((*iter)->GetAttribs(), i_flags);
	}
}

//------------------------------------------------------------------------------
// Dump Line Numbers
//
static void DumpLineNumbers(CLineNumberTableAttribute* i_pln)
{
	if(NULL != i_pln)
	{
		os << "\tLine Numbers:\n";
		CLineNumberTable& lnt = i_pln->GetLineNumberTable();
		u2 u2Len = i_pln->GetTableLength();
		for(u2 u2Ind = 0; u2Ind < u2Len; u2Ind++)
		{
			os	<< "\tPC: " << lnt[u2Ind]->GetStartPC() 
				<< "\tLine: " << lnt[u2Ind]->GetLineNumber()
				<< "\n";
		}
	}
}


//------------------------------------------------------------------------------
// Dump local variables
//

static void DumpLocalVariables(CJClassFile& i_Class, CLocalVariableTableAttribute* i_plv,
						unsigned i_flags)
{
	if(NULL != i_plv)
	{
		os << "\tLocal Variables:\n";
		CConstPool& cp = *(i_Class.GetConstPool());
		CLocalVariableTable& lvt = i_plv->GetLocalVariableTable();
		u2 u2Len = i_plv->GetTableLength();
		for(u2 u2Ind = 0; u2Ind < u2Len; u2Ind++)
		{
			os	<< "\t" << lvt[u2Ind]->GetIndex() << ". "
				<< "PC: "		<< lvt[u2Ind]->GetStartPC() << "; "
				<< "Length: "	<< lvt[u2Ind]->GetLength()   << "; ";
			if(i_flags & FLAG_RESOLVE)
			{
				string strName = *(CCPUtf8Info*)cp[lvt[u2Ind]->GetNameIndex()];
				string strType = *(CCPUtf8Info*)cp[lvt[u2Ind]->GetDescriptorIndex()];
				os << strName << " " << strType;

			}
			else
			{
				os << "Name["		<< lvt[u2Ind]->GetNameIndex() << "] ";
				os << "Type["		<< lvt[u2Ind]->GetDescriptorIndex() << "]";
			}
			os << "\n";
		}
	}
}

//------------------------------------------------------------------------------
// Dump Exceptions
//

static void DumpThrows(CJClassFile& i_Class, CExceptionsAttribute* i_pex)
{
	if(NULL != i_pex)
	{
		os << "\tThrows:\n";
		CConstPool* pcp = i_Class.GetConstPool();
		u2 u2Len = i_pex->GetTableLength();
		for(u2 u2Ind = 0; u2Ind < u2Len; u2Ind++)
		{
			os	<< "\t" << u2Ind << ". [" 
				<< i_pex->GetIndex(u2Ind) << "] "
				<< (string)*pcp->GetClass(i_pex->GetIndex(u2Ind)) << "\n";
		}
	}
}

//------------------------------------------------------------------------------
// Find label for instruction operand.
//

static bool DoWeResolveOperand(CJClassFile& i_Class, CInsDescr* pins, u1* pBytes, int ip)
{
	string s;

	// If we have a branch label then we will report it.
	if (pins->GetSemTag() == SEM_BR || pins->GetSemTag() == SEM_BRC)
	{
		return true;
	}

	// If we have a switch then we will report it.
	if (pins->GetSemTag() == SEM_SWITCH)
	{
		// Tableswitch (170) and Lookupswitch(171).
		return true;
	}

	switch(pins->GetOpCode())
	{
	// Instructions with 1 byte index into constant pool.
	case JVMI_ldc:				// 18

		return true;
	// Instructions with 2 byte index into constant pool.
	case JVMI_ldc_w:			// 19
	case JVMI_ldc2_w:			// 20
	case JVMI_getstatic:		// 178
	case JVMI_putstatic:		// 179
	case JVMI_getfield:			// 180
	case JVMI_putfield:			// 181
	case JVMI_invokevirtual:	// 182
	case JVMI_invokespecial:	// 183
	case JVMI_invokestatic:		// 184
	case JVMI_invokeinterface:	// 185
	case JVMI_new:				// 187
	case JVMI_anewarray:		// 189
	case JVMI_checkcast:		// 192
	case JVMI_instanceof:		// 193
	case JVMI_multianewarray:	// 197
		return true;
	default:
		break;
	}

	return false;
}

//------------------------------------------------------------------------------
// Output label for instruction operand.
// Note that in this context ip points to the instruction beginning,
// ipOperand points to the operand within the instruction.
//

static int InstOperand(CJClassFile& i_Class, CInsDescr* pins, u1* pBytes, int ip, int ipOperand)
{
	string s;
	static string	noLabel = "";

	// If we have a label save it for the next line.
	if ((pins->GetSemTag() == SEM_BR) ||
		(pins->GetSemTag() == SEM_BRC))
	{
		// For branches, operand is byte address.
		long addr;
		if (pins->GetOpCode() == JVMI_goto_w /*200*/  ||
		    pins->GetOpCode() == JVMI_jsr_w /*201*/ )
		{
			addr = LONG_FROM_4_BYTES(ip+1);
		}
		else {
			addr = (long) (int)pBytes[ip+1]<<8 | (int)pBytes[ip+2];
		}
		addr = 0xFFFF & (addr + ip);	// addr is ip-relative.
		os << "Addr:" << addr;
		return 0; // all done
	}

	if (pins->GetSemTag() == SEM_SWITCH)
	{
		// Tableswitch (170) and Lookupswitch(171).
		static long	lowValue;		// For tableswitch only
		static long	highValue;		// For tableswitch only
		static long	jumpOffsetCount = 0;	// For tableswitch only
		static long	matchOffsetPairCount;	// For lookupswitch only
		long	matchValue;
		long	addr;

		if (ipOperand <= ip+1)
		{
			// First encounter with this switch.
			// Skip op-code and byte pad.
			ipOperand = 4 * (((ip+1) + 3) / 4);
			addr = LONG_FROM_4_BYTES(ipOperand);
			addr = 0xFFFF & (addr + ip);	// addr is ip-relative.
			os << "Default Addr:" << addr;
			return ipOperand + 4;
		}
		if (pins->GetOpCode() == JVMI_tableswitch /*170*/ )
		{
			if (ipOperand >= ip+5 && ipOperand <= ip+8)
			{
				// low value
				lowValue = LONG_FROM_4_BYTES(ipOperand);
				os << "Low value:" << lowValue;
				return ipOperand + 4;
			}
			if (ipOperand >= ip+9 && ipOperand <= ip+12)
			{
				// high value
				highValue = LONG_FROM_4_BYTES(ipOperand);
				os << "High value:" << highValue;
				jumpOffsetCount = (highValue - lowValue) + 1;
				return ipOperand + 4;
			}
			addr = LONG_FROM_4_BYTES(ipOperand);
			addr = 0xFFFF & (addr + ip);	// addr is ip-relative.
			os << "Jump Addr:" << addr;
			if (--jumpOffsetCount <= 0)
				return 0;	// done with tableswitch
			return ipOperand + 4;	// Dump next value when we get to (ip+4)
		}

		if (pins->GetOpCode() == JVMI_lookupswitch /*171*/ )
		{
			if (ipOperand >= ip+5 && ipOperand <= ip+8)
			{
				// low value
				matchOffsetPairCount = LONG_FROM_4_BYTES(ipOperand);
				os << "Npairs:" << matchOffsetPairCount;
				return ipOperand + 4;
			}

			matchValue = LONG_FROM_4_BYTES(ipOperand);
			ipOperand += 4;	// skip match value, look at address
			addr = LONG_FROM_4_BYTES(ipOperand);
			addr = 0xFFFF & (addr + ip);	// addr is ip-relative.
			os << "Match:" << matchValue << "  Addr:" << addr;
			if (--matchOffsetPairCount <= 0)
				return 0;	// done with lookupswitch
			return ipOperand + 4;	// Dump when at next pair
		}

		return 0;  // not reached
	}

	switch(pins->GetOpCode())
	{
	// Instructions with 1 byte index into constant pool.
	case JVMI_ldc:				// 18
		DumpConstantPool(i_Class, FLAG_RESOLVE /*flags*/ , (int)pBytes[ip+1]);
		return 0;

	// Instructions with 2 byte index into constant pool.
	case JVMI_ldc_w:			// 19
	case JVMI_ldc2_w:			// 20
	case JVMI_getstatic:		// 178
	case JVMI_putstatic:		// 179
	case JVMI_getfield:			// 180
	case JVMI_putfield:			// 181
	case JVMI_invokevirtual:	// 182
	case JVMI_invokespecial:	// 183
	case JVMI_invokestatic:		// 184
	case JVMI_invokeinterface:	// 185
	case JVMI_new:				// 187
	case JVMI_anewarray:		// 189
	case JVMI_checkcast:		// 192
	case JVMI_instanceof:		// 193
	case JVMI_multianewarray:	// 197
		DumpConstantPool(i_Class, FLAG_RESOLVE /*flags*/ , (int)pBytes[ip+1]<<8 | (int)pBytes[ip+2]);
		return 0;

	default:
		return 0; // all done
	}

	return 0; // all done
}

//------------------------------------------------------------------------------
// Dump Byte code
//
static void DumpByteCode(CJClassFile& i_Class, CCodeAttribute* i_pcode, unsigned i_flags = 0)
{
	// See if we have line numbers
	CLineNumberTableAttribute* pln = i_pcode->GetLineNumbers();
	// Get code
	u1* pBytes = i_pcode->GetCode();
	u4  CodeLeng = i_pcode->GetCodeLength();
	int ipNext = 0;
	int operandNext = 0;
	CInsDescr* pinsDescLast;
	int ipLast;

	os	<< "\tStack[" << i_pcode->GetMaxStack() << "]\n"
		<< "\tLocals[" << i_pcode->GetMaxLocals() << "]\n"
		<< "\tCode Size[" << CodeLeng << "]\n";
	for(int ip = 0; ip < CodeLeng; ip++)
	{
		// Dump byte code
		os	<< "\t" << ip << ":\t" << (int)pBytes[ip];
		// Dump mnemonic if any
		if(ip == ipNext)
		{
			CInsDescr* pinsDesc;
			pinsDesc = JVMInsSet[(u2)pBytes[ip]];
			if (pinsDesc->IsSpecial()) {
				// The only "special" Java instruction is "wide."
				// Downcast to CInsDescr_WideJ and call the special
				// version of the factory method that doesn't require a CMethod.
				if (pBytes[ip] == JVMI_wide) {
					CInsDescr_WideJ* pWideDesc = (CInsDescr_WideJ*)pinsDesc;
					pinsDesc = pWideDesc->InsDescrFactory(&pBytes[ip]);
				}
				else {
					os << "\tJDUMP ERROR: got IsSpecial from an instruction besides \"wide\"?!" << endl;
				}
			}
			os << "\t" << pinsDesc->GetMnem() << "\t";
			if(strlen(pinsDesc->GetMnem()) < 8)
				os << "\t";
			ipNext += pinsDesc->GetSize();
			if(i_flags & FLAG_RESOLVE)
			{
				if (DoWeResolveOperand(i_Class, pinsDesc, pBytes, ip))
				{
					// At (ip+1) start dumping operand(s)
					operandNext = ip + 1;
				}
			}
			ipLast = ip;
			pinsDescLast = pinsDesc;
		}
		else if(ip == operandNext)
		{
			// Resolve the operand for the previous opcode.
			os << "\t\t";
			operandNext = InstOperand(i_Class, pinsDescLast, pBytes, ipLast, ip);
		}
		// Dump line number if any
		if(NULL !=pln)
		{
			CLineNumberTable& lnt = pln->GetLineNumberTable();
			for(CLineNumberTable::iterator itr = lnt.begin(); itr != lnt.end(); itr++)
			{
				if(ip == (*itr)->GetStartPC())
				{
					os << "\t; line " << (*itr)->GetLineNumber();
				}
			}
		}
		os	<< "\n";
	}
}

//------------------------------------------------------------------------------
// Dump Exception Table
//
static void DumpExceptionTable(CJClassFile& i_Class, CExTable& i_ExTable)
{
	CExTable::iterator iter;
	int n = 0;
	if(i_ExTable.size() > 0)
	{
		os << "\tException table:\n";
	}
	for(iter = i_ExTable.begin(); iter < i_ExTable.end(); iter++)
	{
		os	<< "\t" << n++ << "." 
			<< " Start: "   << (*iter).GetStartPC()
			<< " End: "     << (*iter).GetEndPC()
			<< " Handler: " << (*iter).GetHandlerPC()
			<< " Type: "    << (*iter).GetCatchtype()
			<< "\n";
	}
}

//------------------------------------------------------------------------------
// Dump Methods
//
static void DumpMethods(CJClassFile& i_Class, unsigned i_flags = 0)
{
	int n = 0;
	CJMethods* pmethods = i_Class.GetMethods();
	CJMethods::iterator iter;
	os << "\nMethods\n";
	for(iter = pmethods->begin(); iter < pmethods->end(); iter++)
	{
		CJMethodInfo* pMethod = *iter;
		CCPUtf8Info* putf8Name = pMethod->GetName();
		string strName((char*)putf8Name->GetBytes(), putf8Name->GetLength());
		os	<< n++ << ".";
		if(i_flags & FLAG_RESOLVE)
		{
			string strFlags;
			CConstPool& cp = *(i_Class.GetConstPool());
			CCPUtf8Info* putf8Type = (CCPUtf8Info*)cp[pMethod->GetDescriptorInd()];
			AccToString(pMethod->GetAccessFlags(), strFlags);

			os	<< strFlags
				<< string(*pMethod->GetName()) << " "
				<< string(*putf8Type) << "\n";
		}
		else
		{
			os	<< "Access[" << pMethod->GetAccessFlags() << "] "
				<< "Name[" << pMethod->GetNameInd() << "] "
				<< "Descriptor[" << pMethod->GetDescriptorInd() << "]\n";
		}
		if(i_flags & FLAG_DUMP_THROWS)
		{
			CExceptionsAttribute* pex = pMethod->GetExceptions();
			DumpThrows(i_Class, pex);
		}
		CCodeAttribute* pcode  = pMethod->GetCode();
		if(NULL != pcode)
		{
			if(i_flags & FLAG_DUMP_CODE)
			{
				DumpByteCode(i_Class, pcode, i_flags);
			}
			if(i_flags & FLAG_DUMP_DEBUG)
			{
				CLineNumberTableAttribute* pln = pcode->GetLineNumbers();
				DumpLineNumbers(pln);
				CLocalVariableTableAttribute* plv = pcode->GetLocalVariables();
				DumpLocalVariables(i_Class, plv, i_flags);
			}
			if(i_flags & FLAG_DUMP_EXCEPT)
			{
				CExTable& ExTable = pcode->GetExTable();
				DumpExceptionTable(i_Class, ExTable);
			}
		}
		else
		{
			os << "\t<No Code>\n";
		}
	}
}

//------------------------------------------------------------------------------
// Dump Class
//
static void DumpClass(CJClassFile& i_Class, CCommand& i_Command)
{
	unsigned	flagsAll = 0;		// Common flags for all options
	bool		fAll = false;		// dump all
	// -all option processing --------------------------------------------------
	COption& optAll = i_Command.GetOption("all");
	if(optAll.IsDefined())
	{
		CSTR szVal = optAll.GetValue();
		for(int n = 0 ; szVal[n] != 0; n++)
		{
			switch(szVal[n])
			{
			case 'r': flagsAll |= FLAG_RESOLVE; break;
			default: cout << "Bad -all value: " << szVal[n] << "\n";
			}
		}
		fAll = true;
	}
	// -hd option processing -----------------------------------------------
	COption& optHd = i_Command.GetOption("hd");
	if(fAll || (bool)optHd.GetValue())
	{
		DumpClassHeader(i_Class);
	}
	// -at option processing ---------------------------------------------------
	COption& optAt = i_Command.GetOption("at");
	if(fAll || optAt.IsDefined())
	{
		DumpAttributes(*i_Class.GetAttribs(), flagsAll);
	}
	// -cp[:r] option processing -----------------------------------------------
	COption& optCp = i_Command.GetOption("cp");
	if(fAll || optCp.IsDefined())
	{
		CSTR szVal = optCp.GetValue();
		unsigned flags = flagsAll;
		for(int n = 0 ; szVal[n] != 0; n++)
		{
			switch(szVal[n])
			{
			case 'r': flags |= FLAG_RESOLVE; break;
			default: cout << "Bad -cp value: " << szVal[n] << "\n";
			}
		}
		DumpConstantPool(i_Class, flags, 0 /*dump all*/ );
	}
	// -if[:r] option processing -----------------------------------------------
	COption& optIf = i_Command.GetOption("if");
	if(fAll || optIf.IsDefined())
	{
		CSTR szVal = optIf.GetValue();
		unsigned flags = flagsAll;
		for(int n = 0 ; szVal[n] != 0; n++)
		{
			switch(szVal[n])
			{
			case 'r': flags |= FLAG_RESOLVE; break;
			default: cout << "Bad -cp value: " << szVal[n] << "\n";
			}
		}
		DumpInterfaces(i_Class, flags);
	}
	// -fl[:r] option processing -----------------------------------------------
	COption& optFl = i_Command.GetOption("fl");
	if(fAll || optFl.IsDefined())
	{
		CSTR szVal = optFl.GetValue();
		unsigned flags = flagsAll;
		for(int n = 0 ; szVal[n] != 0; n++)
		{
			switch(szVal[n])
			{
			case 'r': flags |= FLAG_RESOLVE; break;
			case 'a': flags |= FLAG_DUMP_ATTRIB; break;
			default: cout << "Bad -cp value: " << szVal[n] << "\n";
			}
		}
		DumpFields(i_Class, flags);
	}
	// -mt[:rgb] option processing ---------------------------------------------
	COption& optMt = i_Command.GetOption("mt");
	if(fAll || optMt.IsDefined())
	{
		CSTR szVal = optMt.GetValue();
		unsigned flags = flagsAll;
		for(int n = 0 ; szVal[n] != 0; n++)
		{
			switch(szVal[n])
			{
			case 'r': flags |= FLAG_RESOLVE; break;
			case 'g': flags |= FLAG_DUMP_DEBUG; break;
			case 'b': flags |= FLAG_DUMP_CODE; break;
			case 'e': flags |= FLAG_DUMP_EXCEPT; break;
			case 't': flags |= FLAG_DUMP_THROWS; break;
			default: cout << "Bad -mt value: " << szVal[n] << "\n";
			}
		}
		DumpMethods(i_Class, flags);
	}
}

//------------------------------------------------------------------------------
// Set up command
//
static void SetupCommand(CCommand& o_Command)
{
	COptions& opt = o_Command.GetOptions();
	opt.Add(new COption("h", CValue(false)));	// -h - print help (bool)
	opt.Add(new COption("n", CValue("x")));		// -n[x|d] - Hex or decimal (default hex)
	opt.Add(new COption("cl", CValue(false)));	// -cl - Dump class
	opt.Add(new COption("hd", CValue(true)));	// -hd - Dump the header (default:yes)
	opt.Add(new COption("cp", CValue("")));		// -cp:[r] - Constant pool [resolved]
	opt.Add(new COption("if", CValue("")));		// -if:[r] - Interfaces
	opt.Add(new COption("fl", CValue("")));		// -fl - Fields
	opt.Add(new COption("mt", CValue("")));		// -mt:[rg] - Methods [resolved, debug]
	opt.Add(new COption("at", CValue(false)));	// -at - Attributes
	opt.Add(new COption("all", CValue("")));	// -all - Dump all
}

//------------------------------------------------------------------------------
// Verify command
//
static bool VerifyCommand(CCommand& i_Command)
{
	if(i_Command.GetOption("h").IsDefined())
	{
		PrintHelp();
		return false;
	}
	if(i_Command.GetFiles().size() == 0)
	{
		cout << "No files specified\n";
		return false;
	}
	COption& optBase = i_Command.GetOption("n");
	if(optBase.IsDefined())
	{
		if(strcmp(optBase.GetValue(), "x") == 0)
		{
			uppercase(internal(hex(os)));
		}
		else if(strcmp(optBase.GetValue(), "d") == 0)
		{
			dec(os);
		}
		else
		{
			cout << "Bad -n option (must be x or d)\n";
			return false;
		}
	}
	// TODO: put other option verifyers here
	return true;
}

//------------------------------------------------------------------------------
// main
//
main(int argc, char* argv[])
{
	CCommand		Command;
	CJClassFile		JClass;							// Java Class
	CJFileStream	FileStreamIn;					// File stream
	CJStream		InStream(&FileStreamIn);		// Java input stream

	try
	{
		SetupCommand(Command);
	#ifdef _WINDOWS_
		Command.Parse(::GetCommandLine());
	#else
		{
			int i, str_len = 0;
			STR arg_str;
			for (i=0; i<argc; i++) {
				str_len += strlen(argv[i]);
			}
			str_len += argc;
			arg_str = (STR)malloc(str_len+1);
			arg_str[0] = '\0';
			for (i=0; i<argc; i++) {
				if (i) strcat(arg_str, " ");
				strcat(arg_str, argv[i]);
			}
			Command.Parse(arg_str);
		}
	#endif
		if(!VerifyCommand(Command))
		{
			cout << "Use JDump -h to get help\n";
			return RET_CMD_ERROR;
		}
	}
	catch(CCommandException e)
	{
		CSTR szToken = e.GetToken();
		cout	<< "CCommandException: " << CommandExceptionMsg[e.GetReason()]
				<< " " << szToken << "\n";
		PrintHelp();
		return RET_CMD_ERROR;
	}

	try
	{
		// TODO cycle through all files
		// Right now for one file only!
		CFileInfo* pfi = Command.GetFiles()[0];
		os << "JDump version:" << JDUMP_VERSION_STR 
		   << " Input file: " << pfi->GetTitle() << "\n";
		FileStreamIn.Open(pfi->GetTitle(), CJFileStream::ACCESS_READ);
		JClass.Read(InStream);
		DumpClass(JClass, Command);
		return RET_OK;
	}

	catch (CJClassFileException e)
	{
		unsigned Reason = e.GetReason();
		cout << "CJClassFileException: " << Reason << "\n";
		return RET_CLASS_ERROR;
	}

	catch (CJFileStreamException e)
	{
		unsigned Reason = e.GetReason();
		cout << "CJFileStreamException: " << Reason << "\n";
		return RET_FILE_ERROR;
	}

	return RET_INTERNAL_ERROR;
}

//= End of JDump.cpp ===========================================================
