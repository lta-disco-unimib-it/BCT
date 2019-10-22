#############################################################
# Copyright (c) 2005, 2006 IBM Corporation and others. 
# All rights reserved.   This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html         
# $Id: JClassStat.mak,v 1.5 2006/03/24 22:00:44 hleung Exp $ 
#  
# Contributors: 
# IBM - Initial contribution
#############################################################

# Microsoft Developer Studio Generated NMAKE File, Based on JClassStat.dsp
!IF "$(CFG)" == ""
CFG=JClassStat - Win32 Debug
!MESSAGE No configuration specified. Defaulting to JClassStat - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "JClassStat - Win32 Release" && "$(CFG)" != "JClassStat - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "JClassStat.mak" CFG="JClassStat - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "JClassStat - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "JClassStat - Win32 Debug" (based on "Win32 (x86) Static Library")
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

!IF  "$(CFG)" == "JClassStat - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\JClassStat.lib"


CLEAN :
	-@erase "$(INTDIR)\JavaHelpers.obj"
	-@erase "$(INTDIR)\JClassBuilder.obj"
	-@erase "$(INTDIR)\JClassFile.obj"
	-@erase "$(INTDIR)\JFileStream.obj"
	-@erase "$(INTDIR)\JMemStream.obj"
	-@erase "$(INTDIR)\JStream.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\JClassStat.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /O2 /I "..\common" /D "NDEBUG" /D "WIN32" /D "_MBCS" /D "_LIB" /Fp"$(INTDIR)\JClassStat.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\JClassStat.bsc" 
BSC32_SBRS= \
	
LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\JClassStat.lib" 
LIB32_OBJS= \
	"$(INTDIR)\JavaHelpers.obj" \
	"$(INTDIR)\JClassBuilder.obj" \
	"$(INTDIR)\JClassFile.obj" \
	"$(INTDIR)\JFileStream.obj" \
	"$(INTDIR)\JMemStream.obj" \
	"$(INTDIR)\JStream.obj"

"$(OUTDIR)\JClassStat.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
    $(LIB32) @<<
  $(LIB32_FLAGS) $(DEF_FLAGS) $(LIB32_OBJS)
<<

!ELSEIF  "$(CFG)" == "JClassStat - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\JClassStat.lib"


CLEAN :
	-@erase "$(INTDIR)\JavaHelpers.obj"
	-@erase "$(INTDIR)\JClassBuilder.obj"
	-@erase "$(INTDIR)\JClassFile.obj"
	-@erase "$(INTDIR)\JFileStream.obj"
	-@erase "$(INTDIR)\JMemStream.obj"
	-@erase "$(INTDIR)\JStream.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\JClassStat.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\common" /I "..\Common" /D "_DEBUG" /D "WIN32" /D "_MBCS" /D "_LIB" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\JClassStat.bsc" 
BSC32_SBRS= \
	
LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\JClassStat.lib" 
LIB32_OBJS= \
	"$(INTDIR)\JavaHelpers.obj" \
	"$(INTDIR)\JClassBuilder.obj" \
	"$(INTDIR)\JClassFile.obj" \
	"$(INTDIR)\JFileStream.obj" \
	"$(INTDIR)\JMemStream.obj" \
	"$(INTDIR)\JStream.obj"

"$(OUTDIR)\JClassStat.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
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
!IF EXISTS("JClassStat.dep")
!INCLUDE "JClassStat.dep"
!ELSE 
!MESSAGE Warning: cannot find "JClassStat.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "JClassStat - Win32 Release" || "$(CFG)" == "JClassStat - Win32 Debug"
SOURCE=..\Common\JavaHelpers.cpp

"$(INTDIR)\JavaHelpers.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=.\JClassBuilder.cpp

"$(INTDIR)\JClassBuilder.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\JClassFile.cpp

"$(INTDIR)\JClassFile.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\JFileStream.cpp

"$(INTDIR)\JFileStream.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\JMemStream.cpp

"$(INTDIR)\JMemStream.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\JStream.cpp

"$(INTDIR)\JStream.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

