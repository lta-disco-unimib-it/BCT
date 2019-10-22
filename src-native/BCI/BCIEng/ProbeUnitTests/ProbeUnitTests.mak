#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: ProbeUnitTests.mak,v 1.6 2006/03/24 21:59:29 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################


# Microsoft Developer Studio Generated NMAKE File, Based on ProbeUnitTests.dsp
# Hand-edited by apratt to remove absolute paths and other silliness
!IF "$(CFG)" == ""
CFG=ProbeUnitTests - Win32 Debug
!MESSAGE No configuration specified. Defaulting to ProbeUnitTests - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "ProbeUnitTests - Win32 Release" && "$(CFG)" != "ProbeUnitTests - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "ProbeUnitTests.mak" CFG="ProbeUnitTests - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "ProbeUnitTests - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "ProbeUnitTests - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "ProbeUnitTests - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\ProbeUnitTests.exe" "$(OUTDIR)\ProbeUnitTests.pch"


CLEAN :
	-@erase "$(INTDIR)\BCIEngProbe.obj"
	-@erase "$(INTDIR)\ProbeUnitTests.obj"
	-@erase "$(INTDIR)\ProbeUnitTests.pch"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\ProbeUnitTests.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MD /W3 /GX /O2 /I "..\BCIEngProbe" /I "..\BCIEngJ" /I ".." /I "..\..\JClass" /I "..\..\common" /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\ProbeUnitTests.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\ProbeUnitTests.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib ..\..\jclass\release\jclassstat.lib ..\release\bcieng.lib ..\bciengj\release\bciengj.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\ProbeUnitTests.pdb" /machine:I386 /out:"$(OUTDIR)\ProbeUnitTests.exe" 
LINK32_OBJS= \
	"$(INTDIR)\BCIEngProbe.obj" \
	"$(INTDIR)\ProbeUnitTests.obj" \
	"$(INTDIR)\StdAfx.obj"

"$(OUTDIR)\ProbeUnitTests.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "ProbeUnitTests - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\ProbeUnitTests.exe" "$(OUTDIR)\ProbeUnitTests.pch" "$(OUTDIR)\ProbeUnitTests.bsc"


CLEAN :
	-@erase "$(INTDIR)\BCIEngProbe.obj"
	-@erase "$(INTDIR)\BCIEngProbe.sbr"
	-@erase "$(INTDIR)\ProbeUnitTests.obj"
	-@erase "$(INTDIR)\ProbeUnitTests.pch"
	-@erase "$(INTDIR)\ProbeUnitTests.sbr"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\StdAfx.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\ProbeUnitTests.bsc"
	-@erase "$(OUTDIR)\ProbeUnitTests.exe"
	-@erase "$(OUTDIR)\ProbeUnitTests.ilk"
	-@erase "$(OUTDIR)\ProbeUnitTests.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "..\BCIEngProbe" /I "..\BCIEngJ" /I ".." /I "..\..\JClass" /I "..\..\common" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\ProbeUnitTests.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\ProbeUnitTests.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\BCIEngProbe.sbr" \
	"$(INTDIR)\ProbeUnitTests.sbr" \
	"$(INTDIR)\StdAfx.sbr"

"$(OUTDIR)\ProbeUnitTests.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib ..\..\jclass\debug\jclassstat.lib ..\debug\bcieng.lib ..\bciengj\debug\bciengj.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\ProbeUnitTests.pdb" /debug /machine:I386 /out:"$(OUTDIR)\ProbeUnitTests.exe" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\BCIEngProbe.obj" \
	"$(INTDIR)\ProbeUnitTests.obj" \
	"$(INTDIR)\StdAfx.obj"

"$(OUTDIR)\ProbeUnitTests.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("ProbeUnitTests.dep")
!INCLUDE "ProbeUnitTests.dep"
!ELSE 
!MESSAGE Warning: cannot find "ProbeUnitTests.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "ProbeUnitTests - Win32 Release" || "$(CFG)" == "ProbeUnitTests - Win32 Debug"
SOURCE=..\BCIEngProbe\BCIEngProbe.cpp

!IF  "$(CFG)" == "ProbeUnitTests - Win32 Release"


"$(INTDIR)\BCIEngProbe.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


!ELSEIF  "$(CFG)" == "ProbeUnitTests - Win32 Debug"


"$(INTDIR)\BCIEngProbe.obj"	"$(INTDIR)\BCIEngProbe.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


!ENDIF 

SOURCE=.\ProbeUnitTests.cpp

!IF  "$(CFG)" == "ProbeUnitTests - Win32 Release"


"$(INTDIR)\ProbeUnitTests.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "ProbeUnitTests - Win32 Debug"


"$(INTDIR)\ProbeUnitTests.obj"	"$(INTDIR)\ProbeUnitTests.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\StdAfx.cpp

!IF  "$(CFG)" == "ProbeUnitTests - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "..\BCIEngProbe" /I "..\BCIEngJ" /I ".." /I "..\..\JClass" /I "..\..\common" /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\ProbeUnitTests.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\ProbeUnitTests.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "ProbeUnitTests - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "..\BCIEngProbe" /I "..\BCIEngJ" /I ".." /I "..\..\JClass" /I "..\..\common" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\ProbeUnitTests.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\StdAfx.sbr"	"$(INTDIR)\ProbeUnitTests.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 


!ENDIF 

