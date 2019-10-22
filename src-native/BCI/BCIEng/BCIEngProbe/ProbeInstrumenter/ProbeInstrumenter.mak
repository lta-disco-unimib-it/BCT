#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: ProbeInstrumenter.mak,v 1.6 2006/03/24 21:58:56 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################


# Microsoft Developer Studio Generated NMAKE File, Based on ProbeInstrumenter.dsp
!IF "$(CFG)" == ""
CFG=ProbeInstrumenter - Win32 Debug
!MESSAGE No configuration specified. Defaulting to ProbeInstrumenter - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "ProbeInstrumenter - Win32 Release" && "$(CFG)" != "ProbeInstrumenter - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "ProbeInstrumenter.mak" CFG="ProbeInstrumenter - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "ProbeInstrumenter - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "ProbeInstrumenter - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "ProbeInstrumenter - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "..\Release\ProbeInstrumenter.exe" "$(OUTDIR)\ProbeInstrumenter.bsc"

!ELSE 

ALL : "BCIEngProbe - Win32 Release" "..\Release\ProbeInstrumenter.exe" "$(OUTDIR)\ProbeInstrumenter.bsc"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"BCIEngProbe - Win32 ReleaseCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\ProbeInstrumenter.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\ProbeInstrumenter.bsc"
	-@erase "..\Release\ProbeInstrumenter.exe"
	-@erase "..\Release\ProbeInstrumenter.sbr"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /ML /W3 /GX /Zi /O2 /I "..\..\..\Common" /I "..\..\..\jclass" /I "..\..\..\bcieng" /I "..\..\..\bcieng\bciengj" /I ".." /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /FR"..\Release\\" /Fp"$(INTDIR)\ProbeInstrumenter.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\ProbeInstrumenter.bsc" 
BSC32_SBRS= \
	"..\Release\ProbeInstrumenter.sbr"

"$(OUTDIR)\ProbeInstrumenter.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\ProbeInstrumenter.pdb" /machine:I386 /out:"../Release/ProbeInstrumenter.exe" /libpath:"..\Release" 
LINK32_OBJS= \
	"$(INTDIR)\ProbeInstrumenter.obj" \
	"..\Release\BCIEngProbe.lib"

"..\Release\ProbeInstrumenter.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "ProbeInstrumenter - Win32 Debug"

OUTDIR=.\..\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\..\Debug
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "$(OUTDIR)\ProbeInstrumenter.exe" "$(OUTDIR)\ProbeInstrumenter.bsc"

!ELSE 

ALL : "BCIEngProbe - Win32 Debug" "$(OUTDIR)\ProbeInstrumenter.exe" "$(OUTDIR)\ProbeInstrumenter.bsc"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"BCIEngProbe - Win32 DebugCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\ProbeInstrumenter.obj"
	-@erase "$(INTDIR)\ProbeInstrumenter.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\ProbeInstrumenter.bsc"
	-@erase "$(OUTDIR)\ProbeInstrumenter.exe"
	-@erase "$(OUTDIR)\ProbeInstrumenter.ilk"
	-@erase "$(OUTDIR)\ProbeInstrumenter.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

"$(INTDIR)" :
    if not exist "$(INTDIR)/$(NULL)" mkdir "$(INTDIR)"

CPP_PROJ=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "..\..\..\Common" /I "..\..\..\jclass" /I "..\..\..\bcieng" /I "..\..\..\bcieng\bciengj" /I ".." /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\ProbeInstrumenter.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\ProbeInstrumenter.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\ProbeInstrumenter.sbr"

"$(OUTDIR)\ProbeInstrumenter.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=kernel32.lib bciengprobe.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\ProbeInstrumenter.pdb" /debug /machine:I386 /out:"$(OUTDIR)\ProbeInstrumenter.exe" /pdbtype:sept /libpath:"..\debug" 
LINK32_OBJS= \
	"$(INTDIR)\ProbeInstrumenter.obj" \
	"$(OUTDIR)\BCIEngProbe.lib"

"$(OUTDIR)\ProbeInstrumenter.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

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


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("ProbeInstrumenter.dep")
!INCLUDE "ProbeInstrumenter.dep"
!ELSE 
!MESSAGE Warning: cannot find "ProbeInstrumenter.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "ProbeInstrumenter - Win32 Release" || "$(CFG)" == "ProbeInstrumenter - Win32 Debug"
SOURCE=.\ProbeInstrumenter.cpp

!IF  "$(CFG)" == "ProbeInstrumenter - Win32 Release"


"$(INTDIR)\ProbeInstrumenter.obj"	"..\Release\ProbeInstrumenter.sbr" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "ProbeInstrumenter - Win32 Debug"


"$(INTDIR)\ProbeInstrumenter.obj"	"$(INTDIR)\ProbeInstrumenter.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

!IF  "$(CFG)" == "ProbeInstrumenter - Win32 Release"

"BCIEngProbe - Win32 Release" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngProbe.mak CFG="BCIEngProbe - Win32 Release" 
   cd ".\ProbeInstrumenter"

"BCIEngProbe - Win32 ReleaseCLEAN" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngProbe.mak CFG="BCIEngProbe - Win32 Release" RECURSE=1 CLEAN 
   cd ".\ProbeInstrumenter"

!ELSEIF  "$(CFG)" == "ProbeInstrumenter - Win32 Debug"

"BCIEngProbe - Win32 Debug" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngProbe.mak CFG="BCIEngProbe - Win32 Debug" 
   cd ".\ProbeInstrumenter"

"BCIEngProbe - Win32 DebugCLEAN" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngProbe.mak CFG="BCIEngProbe - Win32 Debug" RECURSE=1 CLEAN 
   cd ".\ProbeInstrumenter"

!ENDIF 


!ENDIF 

