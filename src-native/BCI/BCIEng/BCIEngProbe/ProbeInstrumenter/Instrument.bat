@echo off
rem
rem usage: instrument classname [ args ... ]
rem Instruments the class file, runs it.
rem 
rem Uses the Probes.jar and probes.txt files found at your %TEMP% directory
rem
echo Instrumenting %1
setlocal
PATH ..\debug;%PATH%
ProbeInstrumenter %TEMP%\probekit.txt %1.class 
endlocal
echo %1.class instrumented.
echo Renaming original to BAK, and BCI to original
ren %1.class %1.class.bak
ren %1.class.bci %1.class
echo Executing %1 %2 %3 %4 %5 %6 %7 %8
java -cp ".;%CLASSPATH%;%TEMP%\probekit.jar" %1 %2 %3 %4 %5 %6 %7 %8
echo Restoring plain/BCI versions of class files
ren %1.class %1.class.bci
ren %1.class.bak %1.class
echo Finished
