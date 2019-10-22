#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: BciEngJTest.mak,v 1.6 2006/03/24 21:58:18 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################

# Microsoft Developer Studio Generated NMAKE File, Based on BciEngJTest.dsp
# Hand-edited by apratt to remove absolute paths and other silliness
!IF "$(CFG)" == ""
CFG=BciEngJTest - Win32 Debug
!MESSAGE No configuration specified. Defaulting to BciEngJTest - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "BciEngJTest - Win32 Release" && "$(CFG)" != "BciEngJTest - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "BciEngJTest.mak" CFG="BciEngJTest - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "BciEngJTest - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "BciEngJTest - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "BciEngJTest - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\BciEngJTest.exe"


CLEAN :
	-@erase "$(INTDIR)\BCIEngJTest.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\BciEngJTest.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MD /W3 /GX /O2 /I "..\\" /I "..\..\\" /I "..\..\..\commmon" /I "..\..\..\JClass" /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\BciEngJTest.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BciEngJTest.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib JClassStat.lib BCIEng.lib BCIEngJ.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\BciEngJTest.pdb" /machine:I386 /out:"$(OUTDIR)\BciEngJTest.exe" /libpath:"..\..\..\JClass\Release" /libpath:"..\..\Release" /libpath:"..\Release" 
LINK32_OBJS= \
	"$(INTDIR)\BCIEngJTest.obj"

"$(OUTDIR)\BciEngJTest.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "BciEngJTest - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\BciEngJTest.exe"


CLEAN :
	-@erase "$(INTDIR)\BCIEngJTest.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BciEngJTest.exe"
	-@erase "$(OUTDIR)\BciEngJTest.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\\" /I "..\..\\" /I "..\..\..\commmon" /I "..\..\..\JClass" /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\BciEngJTest.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

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
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BciEngJTest.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=BCIEng.lib BCIEngJ.lib kernel32.lib JClassStat.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\BciEngJTest.pdb" /debug /machine:I386 /out:"$(OUTDIR)\BciEngJTest.exe" /pdbtype:sept /libpath:"..\..\..\JClass\Debug" /libpath:"..\..\Debug" /libpath:"..\Debug" /fixed:no 
LINK32_OBJS= \
	"$(INTDIR)\BCIEngJTest.obj"

"$(OUTDIR)\BciEngJTest.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("BciEngJTest.dep")
!INCLUDE "BciEngJTest.dep"
!ELSE 
!MESSAGE Warning: cannot find "BciEngJTest.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "BciEngJTest - Win32 Release" || "$(CFG)" == "BciEngJTest - Win32 Debug"
SOURCE=.\BCIEngJTest.cpp

"$(INTDIR)\BCIEngJTest.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

