#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: BCIEngProbe.mak,v 1.6 2006/03/24 21:58:56 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################


# Microsoft Developer Studio Generated NMAKE File, Based on BCIEngProbe.dsp
!IF "$(CFG)" == ""
CFG=BCIEngProbe - Win32 Debug
!MESSAGE No configuration specified. Defaulting to BCIEngProbe - Win32 Debug.
!ENDIF

!IF "$(CFG)" != "BCIEngProbe - Win32 Release" && "$(CFG)" != "BCIEngProbe - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE
!MESSAGE NMAKE /f "BCIEngProbe.mak" CFG="BCIEngProbe - Win32 Debug"
!MESSAGE
!MESSAGE Possible choices for configuration are:
!MESSAGE
!MESSAGE "BCIEngProbe - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "BCIEngProbe - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE
!ERROR An invalid configuration is specified.
!ENDIF

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE
NULL=nul
!ENDIF

!IF  "$(CFG)" == "BCIEngProbe - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

!IF "$(RECURSE)" == "0"

ALL : "$(OUTDIR)\BCIEngProbe.dll"

!ELSE

ALL : "BCIEngJ - Win32 Release" "$(OUTDIR)\BCIEngProbe.dll"

!ENDIF

!IF "$(RECURSE)" == "1"
CLEAN :"BCIEngJ - Win32 ReleaseCLEAN"
!ELSE
CLEAN :
!ENDIF
	-@erase "$(INTDIR)\BCIEngProbe.obj"
	-@erase "$(INTDIR)\BCIEngProbeInterface.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEngProbe.dll"
	-@erase "$(OUTDIR)\BCIEngProbe.exp"
	-@erase "$(OUTDIR)\BCIEngProbe.lib"
	-@erase "$(OUTDIR)\BCIEngProbe.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MT /W3 /GX /Zi /O2 /I "..\..\BCIEng\\" /I "..\..\BCIEng\BCIEngJ" /I "..\..\JClass" /I "..\..\common" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /Fp"$(INTDIR)\BCIEngProbe.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c

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

MTL=midl.exe
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEngProbe.bsc"
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib bciengj.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\BCIEngProbe.pdb" /debug /machine:I386 /out:"$(OUTDIR)\BCIEngProbe.dll" /implib:"$(OUTDIR)\BCIEngProbe.lib" /libpath:"..\..\jclass\release" /libpath:"..\..\bcieng\bciengj\release"
LINK32_OBJS= \
	"$(INTDIR)\BCIEngProbe.obj" \
	"$(INTDIR)\BCIEngProbeInterface.obj" \
	"..\BCIEngJ\Release\BCIEngJ.lib"

"$(OUTDIR)\BCIEngProbe.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "BCIEngProbe - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

!IF "$(RECURSE)" == "0"

ALL : "$(OUTDIR)\BCIEngProbe.dll" "$(OUTDIR)\BCIEngProbe.bsc"

!ELSE

ALL : "BCIEngJ - Win32 Debug" "$(OUTDIR)\BCIEngProbe.dll" "$(OUTDIR)\BCIEngProbe.bsc"

!ENDIF

!IF "$(RECURSE)" == "1"
CLEAN :"BCIEngJ - Win32 DebugCLEAN"
!ELSE
CLEAN :
!ENDIF
	-@erase "$(INTDIR)\BCIEngProbe.obj"
	-@erase "$(INTDIR)\BCIEngProbe.sbr"
	-@erase "$(INTDIR)\BCIEngProbeInterface.obj"
	-@erase "$(INTDIR)\BCIEngProbeInterface.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEngProbe.bsc"
	-@erase "$(OUTDIR)\BCIEngProbe.dll"
	-@erase "$(OUTDIR)\BCIEngProbe.exp"
	-@erase "$(OUTDIR)\BCIEngProbe.lib"
	-@erase "$(OUTDIR)\BCIEngProbe.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\..\BCIEng\\" /I "..\..\BCIEng\BCIEngJ" /I "..\..\JClass" /I "..\..\common" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\BCIEngProbe.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c

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

MTL=midl.exe
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEngProbe.bsc"
BSC32_SBRS= \
	"$(INTDIR)\BCIEngProbe.sbr" \
	"$(INTDIR)\BCIEngProbeInterface.sbr"

"$(OUTDIR)\BCIEngProbe.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=bciengj.lib kernel32.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\BCIEngProbe.pdb" /debug /machine:I386 /out:"$(OUTDIR)\BCIEngProbe.dll" /implib:"$(OUTDIR)\BCIEngProbe.lib" /pdbtype:sept /libpath:"..\..\jclass\debug" /libpath:"..\..\bcieng\bciengj\debug"
LINK32_OBJS= \
	"$(INTDIR)\BCIEngProbe.obj" \
	"$(INTDIR)\BCIEngProbeInterface.obj" \
	"..\BCIEngJ\Debug\BCIEngJ.lib"

"$(OUTDIR)\BCIEngProbe.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("BCIEngProbe.dep")
!INCLUDE "BCIEngProbe.dep"
!ELSE
!MESSAGE Warning: cannot find "BCIEngProbe.dep"
!ENDIF
!ENDIF


!IF "$(CFG)" == "BCIEngProbe - Win32 Release" || "$(CFG)" == "BCIEngProbe - Win32 Debug"
SOURCE=.\BCIEngProbe.cpp

!IF  "$(CFG)" == "BCIEngProbe - Win32 Release"


"$(INTDIR)\BCIEngProbe.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngProbe - Win32 Debug"


"$(INTDIR)\BCIEngProbe.obj"	"$(INTDIR)\BCIEngProbe.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF

SOURCE=.\BCIEngProbeInterface.cpp

!IF  "$(CFG)" == "BCIEngProbe - Win32 Release"


"$(INTDIR)\BCIEngProbeInterface.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngProbe - Win32 Debug"


"$(INTDIR)\BCIEngProbeInterface.obj"	"$(INTDIR)\BCIEngProbeInterface.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF

!IF  "$(CFG)" == "BCIEngProbe - Win32 Release"

"BCIEngJ - Win32 Release" :
   cd "..\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Release"
   cd "..\BCIEngProbe"

"BCIEngJ - Win32 ReleaseCLEAN" :
   cd "..\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Release" RECURSE=1 CLEAN
   cd "..\BCIEngProbe"

!ELSEIF  "$(CFG)" == "BCIEngProbe - Win32 Debug"

"BCIEngJ - Win32 Debug" :
   cd "..\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Debug"
   cd "..\BCIEngProbe"

"BCIEngJ - Win32 DebugCLEAN" :
   cd "..\BCIEngJ"
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEngJ.mak CFG="BCIEngJ - Win32 Debug" RECURSE=1 CLEAN
   cd "..\BCIEngProbe"

!ENDIF


!ENDIF

