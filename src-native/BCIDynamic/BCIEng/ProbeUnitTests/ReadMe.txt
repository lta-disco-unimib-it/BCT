#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: ReadMe.txt,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################


========================================================================
       CONSOLE APPLICATION : ProbeUnitTests
========================================================================

Created by Allan Pratt, October 7, 2003

This is a program that we can use to drive unit tests in BCIEngProbe and friends.
I needed to make a separate projet for this, and then insert source files from
BCIEngProbe into it, because even public member functions are not exported from
BCIEngProbe.dll. In particular, to test the CFilterRule class I wanted
to call CFilterRule::WildcardStringMatch directly.

USAGE:

	-wildcardtest
		Tests the wildcard pattern matcher in CFilterRule.

(Currently those are the only tests incorporated in this test driver program.)

========================================================================

To create this project so it would build, I had to set up the project settings
to be sure the include directory path was like this:
	..\BCIEngProbe,..\BCIEngJ,..,..\..\JClass,..\..\common

And the library list on the link command was like this:
	kernel32.lib ..\..\jclass\debug\jclassstat.lib ..\debug\bcieng.lib ..\bciengj\debug\bciengj.lib

And the C++ Code Generation settings included the right DLLs
	Debug Multithreaded DLL

