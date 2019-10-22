#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: BCIEngJ.mak,v 1.6 2006/03/24 21:57:56 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################

# Microsoft Developer Studio Generated NMAKE File, Based on BCIEngJ.dsp
!IF "$(CFG)" == ""
CFG=BCIEngJ - Win32 Debug
!MESSAGE No configuration specified. Defaulting to BCIEngJ - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "BCIEngJ - Win32 Release" && "$(CFG)" != "BCIEngJ - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "BCIEngJ.mak" CFG="BCIEngJ - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "BCIEngJ - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "BCIEngJ - Win32 Debug" (based on "Win32 (x86) Static Library")
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

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "$(OUTDIR)\BCIEngJ.lib"

!ELSE 

ALL : "JClassStat - Win32 Release" "BCIEng - Win32 Release" "$(OUTDIR)\BCIEngJ.lib"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"BCIEng - Win32 ReleaseCLEAN" "JClassStat - Win32 ReleaseCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\ExtRefJ_Interface.obj"
	-@erase "$(INTDIR)\ExtRefJ_StatMethod.obj"
	-@erase "$(INTDIR)\JVMInsSet.obj"
	-@erase "$(INTDIR)\ModuleJ.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEngJ.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /Zi /O2 /I "..\\" /I "..\..\jclass" /I "..\..\..\sun" /I "..\..\common" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Fp"$(INTDIR)\BCIEngJ.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEngJ.bsc" 
BSC32_SBRS= \
	
LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\BCIEngJ.lib" 
LIB32_OBJS= \
	"$(INTDIR)\ExtRefJ_Interface.obj" \
	"$(INTDIR)\ExtRefJ_StatMethod.obj" \
	"$(INTDIR)\JVMInsSet.obj" \
	"$(INTDIR)\ModuleJ.obj" \
	"..\Release\BCIEng.lib" \
	"..\..\JClass\Release\JClassStat.lib"

"$(OUTDIR)\BCIEngJ.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
    $(LIB32) @<<
  $(LIB32_FLAGS) $(DEF_FLAGS) $(LIB32_OBJS)
<<

!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

!IF "$(RECURSE)" == "0" 

ALL : "$(OUTDIR)\BCIEngJ.lib" "$(OUTDIR)\BCIEngJ.bsc"

!ELSE 

ALL : "JClassStat - Win32 Debug" "BCIEng - Win32 Debug" "$(OUTDIR)\BCIEngJ.lib" "$(OUTDIR)\BCIEngJ.bsc"

!ENDIF 

!IF "$(RECURSE)" == "1" 
CLEAN :"BCIEng - Win32 DebugCLEAN" "JClassStat - Win32 DebugCLEAN" 
!ELSE 
CLEAN :
!ENDIF 
	-@erase "$(INTDIR)\ExtRefJ_Interface.obj"
	-@erase "$(INTDIR)\ExtRefJ_Interface.sbr"
	-@erase "$(INTDIR)\ExtRefJ_StatMethod.obj"
	-@erase "$(INTDIR)\ExtRefJ_StatMethod.sbr"
	-@erase "$(INTDIR)\JVMInsSet.obj"
	-@erase "$(INTDIR)\JVMInsSet.sbr"
	-@erase "$(INTDIR)\ModuleJ.obj"
	-@erase "$(INTDIR)\ModuleJ.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEngJ.bsc"
	-@erase "$(OUTDIR)\BCIEngJ.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\..\Common" /I "..\\" /I "..\..\jclass" /I "..\..\..\sun" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\BCIEngJ.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEngJ.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\ExtRefJ_Interface.sbr" \
	"$(INTDIR)\ExtRefJ_StatMethod.sbr" \
	"$(INTDIR)\JVMInsSet.sbr" \
	"$(INTDIR)\ModuleJ.sbr"

"$(OUTDIR)\BCIEngJ.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\BCIEngJ.lib" 
LIB32_OBJS= \
	"$(INTDIR)\ExtRefJ_Interface.obj" \
	"$(INTDIR)\ExtRefJ_StatMethod.obj" \
	"$(INTDIR)\JVMInsSet.obj" \
	"$(INTDIR)\ModuleJ.obj" \
	"..\Debug\BCIEng.lib" \
	"..\..\JClass\Debug\JClassStat.lib"

"$(OUTDIR)\BCIEngJ.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
    $(LIB32) @<<
  $(LIB32_FLAGS) $(DEF_FLAGS) $(LIB32_OBJS)
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
!IF EXISTS("BCIEngJ.dep")
!INCLUDE "BCIEngJ.dep"
!ELSE 
!MESSAGE Warning: cannot find "BCIEngJ.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "BCIEngJ - Win32 Release" || "$(CFG)" == "BCIEngJ - Win32 Debug"
SOURCE=.\ExtRefJ_Interface.cpp

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"


"$(INTDIR)\ExtRefJ_Interface.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"


"$(INTDIR)\ExtRefJ_Interface.obj"	"$(INTDIR)\ExtRefJ_Interface.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\ExtRefJ_StatMethod.cpp

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"


"$(INTDIR)\ExtRefJ_StatMethod.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"


"$(INTDIR)\ExtRefJ_StatMethod.obj"	"$(INTDIR)\ExtRefJ_StatMethod.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\JVMInsSet.cpp

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"


"$(INTDIR)\JVMInsSet.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"


"$(INTDIR)\JVMInsSet.obj"	"$(INTDIR)\JVMInsSet.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\ModuleJ.cpp

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"


"$(INTDIR)\ModuleJ.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"


"$(INTDIR)\ModuleJ.obj"	"$(INTDIR)\ModuleJ.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"

"BCIEng - Win32 Release" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEng.mak CFG="BCIEng - Win32 Release" 
   cd ".\BCIEngJ"

"BCIEng - Win32 ReleaseCLEAN" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEng.mak CFG="BCIEng - Win32 Release" RECURSE=1 CLEAN 
   cd ".\BCIEngJ"

!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"

"BCIEng - Win32 Debug" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEng.mak CFG="BCIEng - Win32 Debug" 
   cd ".\BCIEngJ"

"BCIEng - Win32 DebugCLEAN" : 
   cd ".."
   $(MAKE) /$(MAKEFLAGS) /F .\BCIEng.mak CFG="BCIEng - Win32 Debug" RECURSE=1 CLEAN 
   cd ".\BCIEngJ"

!ENDIF 

!IF  "$(CFG)" == "BCIEngJ - Win32 Release"

"JClassStat - Win32 Release" : 
   cd "..\..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Release" 
   cd "..\BCIEng\BCIEngJ"

"JClassStat - Win32 ReleaseCLEAN" : 
   cd "\VLH\vlh_bluerat_sh_tp\BlueRat\runtime\probes\BCI\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Release" RECURSE=1 CLEAN 
   cd "..\BCIEng\BCIEngJ"

!ELSEIF  "$(CFG)" == "BCIEngJ - Win32 Debug"

"JClassStat - Win32 Debug" : 
   cd "..\..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Debug" 
   cd "..\BCIEng\BCIEngJ"

"JClassStat - Win32 DebugCLEAN" : 
   cd "..\..\JClass"
   $(MAKE) /$(MAKEFLAGS) /F .\JClassStat.mak CFG="JClassStat - Win32 Debug" RECURSE=1 CLEAN 
   cd "..\BCIEng\BCIEngJ"

!ENDIF 


!ENDIF 

