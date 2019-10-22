 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JavaDef.h,v 1.1.2.2 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

//==============================================================================
// JavaDef.h
//
// Some common Java definitions
//
//==============================================================================

//??? NameSpace ???
#ifndef _JAVADEF_H
#define _JAVADEF_H

#include "CommonDef.h"

#define JAVA_MAGIC					0xCAFEBABE;

// class_file.access_flags and field_info.access_flags values
#define ACC_PUBLIC					0x0001	// Public access
#define ACC_PRIVATE					0x0002	// Private access
#define ACC_PROTECTED				0x0004	// Protected access
#define ACC_STATIC					0x0008	// Static
#define ACC_FINAL					0x0010	// Final access
#define ACC_SUPER					0x0020	// Super access
#define ACC_SYNCHRONIZED			0x0020  // Synchronized (for methods)
#define ACC_TRANSIENT				0x0080	// Transient (for fields)
#define ACC_INTERFACE				0x0200	// Interface acess
#define ACC_ABSTRACT				0x0400	// Abstract access

// cp_info.tag values
#define CONSTANT_Utf8				1		// UTF8 constant
#define CONSTANT_Integer			3		// Integer constant
#define CONSTANT_Float				4		// Float constant
#define CONSTANT_Long				5		// Long constant
#define CONSTANT_Double				6		// Double constant
#define CONSTANT_Class				7		// Class constant
#define CONSTANT_String				8		// String constant
#define CONSTANT_Fieldref			9		// Field reference constant
#define CONSTANT_Methodref			10		// Method refernce constant
#define CONSTANT_InterfaceMethodref	11		// Interface method refernce constant 
#define CONSTANT_NameAndType		12		// Name and type constant

// Array types
#define T_BOOLEAN					4		// bool
#define T_CHAR						5		// char
#define T_FLOAT						6		// float
#define T_DOUBLE					7		// double
#define T_BYTE						8		// byte
#define T_SHORT						9		// short
#define T_INT						10		// int
#define T_LONG						11		// long

#define CONSTANT_Unknown			0

// ToDo: other Java specific definitions
typedef unsigned char				u1;		// 1 byte
typedef unsigned short				u2;		// 2 byte unsigned integer
typedef	unsigned int				u4;		// 4 byte unsigned integer
typedef uint64_t					u8;		// 8 byte unsigned integer

// TODO: This section may be hardware dependent and should be ifdeffed properly
typedef int							JINTEGER;
typedef int64_t						JLONG;
typedef float						JFLOAT;
typedef double						JDOUBLE;

#endif	// _JAVADEF_H

//= End of JavaDef.h ===========================================================
