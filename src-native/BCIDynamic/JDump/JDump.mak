#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: JDump.mak,v 1.5 2006/03/24 22:01:33 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################

# Microsoft Developer Studio Generated NMAKE File, Based on JDump.dsp
!IF "$(CFG)" == ""
CFG=JDump - Win32 Debug
!MESSAGE No configuration specified. Defaulting to JDump - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "JDump - Win32 Release" && "$(CFG)" != "JDump - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "JDump.mak" CFG="JDump - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "JDump - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "JDump - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "JDump - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "$(OUTDIR)\JDump.exe" "$(OUTDIR)\JDump.bsc"

!ELSE 

ALL : "BCIEngJ - Win32 Release" "JClassStat - Win32 Release" "$(OUTDIR)\JDump.exe" "$(OUTDIR)\JDump.bsc"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"JClassStat - Win32 ReleaseCLEAN" "BCIEngJ - Win32 ReleaseCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\Command.obj"
	-@erase "$(INTDIR)\Command.sbr"
	-@erase "$(INTDIR)\JDump.obj"
	-@erase "$(INTDIR)\JDump.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\JDump.bsc"
	-@erase "$(OUTDIR)\JDump.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MD /W3 /GX /O2 /I "..\common" /I "..\JClass" /I "..\BciEng" /I "..\BciEng\BciEngJ" /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\JDump.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\JDump.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\Command.sbr" \
	"$(INTDIR)\JDump.sbr"

"$(OUTDIR)\JDump.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib JclassStat.lib ..\bcieng\release\bcieng.lib ..\bcieng\bciengj\release\bciengj.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\JDump.pdb" /machine:I386 /out:"$(OUTDIR)\JDump.exe" /libpath:"..\JClass\Release" 
LINK32_OBJS= \
	"$(INTDIR)\Command.obj" \
	"$(INTDIR)\JDump.obj" \
	"..\JClass\Release\JClassStat.lib" \
	"..\BCIEng\BCIEngJ\Release\BCIEngJ.lib"

"$(OUTDIR)\JDump.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "JDump - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "$(OUTDIR)\JDump.exe" "$(OUTDIR)\JDump.bsc"

!ELSE 

ALL : "BCIEngJ - Win32 Debug" "JClassStat - Win32 Debug" "$(OUTDIR)\JDump.exe" "$(OUTDIR)\JDump.bsc"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"JClassStat - Win32 DebugCLEAN" "BCIEngJ - Win32 DebugCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\Command.obj"
	-@erase "$(INTDIR)\Command.sbr"
	-@erase "$(INTDIR)\JDump.obj"
	-@erase "$(INTDIR)\JDump.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\JDump.bsc"
	-@erase "$(OUTDIR)\JDump.exe"
	-@erase "$(OUTDIR)\JDump.map"
	-@erase "$(OUTDIR)\JDump.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\common" /I "..\JClass" /I "..\BciEng" /I "..\BciEng\BciEngJ" /I "..\Common" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\JDump.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\JDump.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\Command.sbr" \
	"$(INTDIR)\JDump.sbr"

"$(OUTDIR)\JDump.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib JClassStat.lib ..\bcieng\debug\bcieng.lib ..\bcieng\bciengj\debug\bciengj.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\JDump.pdb" /map:"$(INTDIR)\JDump.map" /debug /machine:I386 /out:"$(OUTDIR)\JDump.exe" /libpath:"..\JClass\debug\\" /fixed:no 
LINK32_OBJS= \
	"$(INTDIR)\Command.obj" \
	"$(INTDIR)\JDump.obj" \
	"..\JClass\Debug\JClassStat.lib" \
	"..\BCIEng\BCIEngJ\Debug\BCIEngJ.lib"

"$(OUTDIR)\JDump.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

SOURCE="$(InputPath)"
DS_POSTBUILD_DEP=$(INTDIR)\postbld.dep

ALL : $(DS_POSTBUILD_DEP)

# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

$(DS_POSTBUILD_DEP) : "BCIEngJ - Win32 Debug" "JClassStat - Win32 Debug" "$(OUTDIR)\JDump.exe" "$(OUTDIR)\JDump.bsc"
   copy Debug\jdump.exe c:\tools\bin
	echo Helper for Post-build step > "$(DS_POSTBUILD_DEP)"

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("JDump.dep")
!INCLUDE "JDump.dep"
!ELSE 
!MESSAGE Warning: cannot find "JDump.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "JDump - Win32 Release" || "$(CFG)" == "JDump - Win32 Debug"

!IF  "$(CFG)" == "JDump - Win32 Release"

"JClassStat - Win32 Release" : 
   cd "..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Release" 
   cd "..\JDump"

"JClassStat - Win32 ReleaseCLEAN" : 
   cd "..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Release" RECURSE=1 CLEAN 
   cd "..\JDump"

!ELSEIF  "$(CFG)" == "JDump - Win32 Debug"

"JClassStat - Win32 Debug" : 
   cd "..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Debug" 
   cd "..\JDump"

"JClassStat - Win32 DebugCLEAN" : 
   cd "..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Debug" RECURSE=1 CLEAN 
   cd "..\JDump"

!ENDIF 

!IF  "$(CFG)" == "JDump - Win32 Release"

"BCIEngJ - Win32 Release" : 
   cd "..\BCIEng\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Release" 
   cd "..\..\JDump"

"BCIEngJ - Win32 ReleaseCLEAN" : 
   cd "..\BCIEng\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Release" RECURSE=1 CLEAN 
   cd "..\..\JDump"

!ELSEIF  "$(CFG)" == "JDump - Win32 Debug"

"BCIEngJ - Win32 Debug" : 
   cd "..\BCIEng\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Debug" 
   cd "..\..\JDump"

"BCIEngJ - Win32 DebugCLEAN" : 
   cd "..\BCIEng\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Debug" RECURSE=1 CLEAN 
   cd "..\..\JDump"

!ENDIF 

SOURCE=..\Common\Command.cpp

"$(INTDIR)\Command.obj"	"$(INTDIR)\Command.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=.\JDump.cpp

"$(INTDIR)\JDump.obj"	"$(INTDIR)\JDump.sbr" : $(SOURCE) "$(INTDIR)"



!ENDIF 

