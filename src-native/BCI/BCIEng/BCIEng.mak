# Microsoft Developer Studio Generated NMAKE File, Based on BCIEng.dsp
!IF "$(CFG)" == ""
CFG=BCIEng - Win32 Debug
!MESSAGE No configuration specified. Defaulting to BCIEng - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "BCIEng - Win32 Release" && "$(CFG)" != "BCIEng - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "BCIEng.mak" CFG="BCIEng - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "BCIEng - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "BCIEng - Win32 Debug" (based on "Win32 (x86) Static Library")
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

!IF  "$(CFG)" == "BCIEng - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\BCIEng.lib"


CLEAN :
	-@erase "$(INTDIR)\InsSet.obj"
	-@erase "$(INTDIR)\Module.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEng.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /Zi /O2 /I "..\Common" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Fp"$(INTDIR)\BCIEng.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEng.bsc" 
BSC32_SBRS= \
	
LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\BCIEng.lib" 
LIB32_OBJS= \
	"$(INTDIR)\InsSet.obj" \
	"$(INTDIR)\Module.obj"

"$(OUTDIR)\BCIEng.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
    $(LIB32) @<<
  $(LIB32_FLAGS) $(DEF_FLAGS) $(LIB32_OBJS)
<<

!ELSEIF  "$(CFG)" == "BCIEng - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\BCIEng.lib"


CLEAN :
	-@erase "$(INTDIR)\InsSet.obj"
	-@erase "$(INTDIR)\Module.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\BCIEng.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MDd /W3 /Gm /GX /Zi /Od /I "..\Common" /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /Fp"$(INTDIR)\BCIEng.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\BCIEng.bsc" 
BSC32_SBRS= \
	
LIB32=link.exe -lib
LIB32_FLAGS=/nologo /out:"$(OUTDIR)\BCIEng.lib" 
LIB32_OBJS= \
	"$(INTDIR)\InsSet.obj" \
	"$(INTDIR)\Module.obj"

"$(OUTDIR)\BCIEng.lib" : "$(OUTDIR)" $(DEF_FILE) $(LIB32_OBJS)
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
!IF EXISTS("BCIEng.dep")
!INCLUDE "BCIEng.dep"
!ELSE 
!MESSAGE Warning: cannot find "BCIEng.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "BCIEng - Win32 Release" || "$(CFG)" == "BCIEng - Win32 Debug"
SOURCE=.\InsSet.cpp

"$(INTDIR)\InsSet.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\Module.cpp

"$(INTDIR)\Module.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

