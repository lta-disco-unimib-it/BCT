 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: JavaDef.h,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
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

#define JAVA_MAGIC					0xCAFEBABE;

// class_file.access_flags and field_info.access_flags values
#define ACC_PUBLIC					0x0001	// Public access
#define ACC_PRIVATE					0x0002	// Private access
#define ACC_PROTECTED				0x0004	// Protected access
#define ACC_STATIC					0x0008	// Static
#define ACC_FINAL					0x0010	// Final access
#define ACC_SUPER					0x0020	// Super access
#define ACC_SYNCHRONIZED			0x0020  // Synchronized (for methods)
#define ACC_INTERFACE				0x0200	// Interface acess
#define ACC_ABSTRACT				0x0400	// Abstract access

// cp_info.tag values
#define CONSTANT_Utf8				1		//
#define CONSTANT_Integer			3		//
#define CONSTANT_Float				4		//
#define CONSTANT_Long				5		//	
#define CONSTANT_Double				6		//
#define CONSTANT_Class				7		//	
#define CONSTANT_String				8		//
#define CONSTANT_Fieldref			9		//	
#define CONSTANT_Methodref			10		//
#define CONSTANT_InterfaceMethodref	11		//	
#define CONSTANT_NameAndType		12		//

#define CONSTANT_Unknown			0

// array types
#define T_BOOLEAN					4		// bool
#define T_CHAR						5		// char
#define T_FLOAT						6		// float
#define T_DOUBLE					7		// double
#define T_BYTE						8		// byte
#define T_SHORT						9		// short
#define T_INT						10		// int
#define T_LONG						11		// long

// ToDo: other Java specific definitions
typedef unsigned char				u1;		// 1 byte
typedef unsigned short				u2;		// 2 byte unsigned integer
typedef	unsigned int				u4;		// 4 byte unsigned integer

#endif	// _JAVADEF_H

//= End of JavaDef.h ===========================================================
