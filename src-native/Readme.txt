This folder contains C/C++ sources for building the modified Byte Code Instrumentation Engine library.
These are modified versions of that available in the eclipse package org.eclipse.hyades.probekit.

In folder BCIDynamic there is the version modified in november 2006 that permits to collect informations on all calls done from a method
using callsite probes.

In folder BCI there is the version with added functionalities that permits to check whath is the method receiving the callsite 
instrumentation, that is, it permits to filter on the caller and the callee. This version works only with static instrumentation 
because it need a probescript compiler that support extended grammar and this is not ready yet.

These two verions are not merged because we need a working dynamic methodology for experiments on the fly.

To work with these it is intended that you run "make" booth in BCI and BCIDynamic and then:

1) copy BCIDynamic/Release/BCIEngProbe.so to your /path/to/NewAgentController/plugins/org.eclipse.hyades.probekit/lib/ 
	To not overwrite original BCIEngProbe.so you can rename the BCT one and refers to this in 
	/path/to/NewAgentController/plugins/org.eclipse.hyades.probekit/config/pluginconfig.xml.

2) use BCI/Release/probeinstrumenter for static instrumentation:
	add BCI/Release/ to your PATH environment variable


This code duplication permit us to not modify ALL makefiles..

=================================================================================================================
Dynamic instrumentation ( INCONSISTENT WITH CURRENT VERSION )

BCT Dynamic Instrumentation Engine behavior is different from Tptp original BCIEngine.

Enter and Exit probe fragments monitor all calls coming to an element and are inserted inside the method being called(as in TPTP).

Callsite fragments instead monitors calls coming from Enter Exit probes. They are inserted in the method doing the call. Filter rules of Callsite fragments apply to
elements called by a Enter/Exit probe.

All callsite fragments apply to all enter/exit probes.
If you do not want to monitor calls coming from a component monitored with Enter/Exit fragments you have to add
NOTINSTRUMENTCALL inside its probe.script.


Examples:

1) You want to monitor all calls to p.A and all call coming from p.A to all world except java.lang:

Add to the probe that monitors incoming calls, filter rules :
	"INCLUDE p A * *"
	"EXCLUDE * * * *"
	
Add to the probe that monitors outgoing calls, filter rules :
	"EXCLUDE java.lang* * * *"
	"INCLUDE * * * *"

2) Tou want to use regression test probes, so you want:
	bctTcLoggingProbeIncoming attached to all calls coming to firstPackage* and secondPackage*
	bctTcLoggingProbeOutgoing attached to all calls coming from the above packages to all classes except java* 
	testCaseActivatorProbe attached to testpackage.TestClass.test* but you do not want to monitor call coming from these methods 

	Add to the probe that monitors incoming calls, filter rules :
		"INCLUDE firstPackage * * *"
		"INCLUDE secondPackage * * *"
		"EXCLUDE * * * *"
	
	Add to the probe that monitors outgoing calls, filter rules :
		"EXCLUDE java* * * *"
		"INCLUDE * * * *"
	
	Add to the probe script for testCaseActivatorProbe lines
		NOTINSTRUMENTCALLS
	
=================================================================================================================
Static instrumentation

As the original, also with these modified version is possible to instrument probes in a static way.

In order to do this you have to:

1) set your PATH to point to probeinstrumenter (the one made in folder BCI/Release)

	export PATH=$PATH:/path/to/src-native/BCI/Release/
	
2) launch ProbeInstrumenter on classes/jar you want to instrument. In order to do this ProbeInstrumenter must be on your classpath
	
	export CLASSPATH=$CLASSPATH:/path/to/eclipse/plugins/org.eclipse.hyades.probekit_xxx/probekit.jar
	
	java ProbeInstrumenter probe.script TARGET
	
	where probe.script is the script with probe instrumenting rules  and TARGET is the class/jar you want to instrument
	
	
	this comand instruments one class/jar at time, to instrument more file you can run something like this
	
	for x in `find ./ -name "*.class"`; do java ProbeInstrumenter probe.script $x;done
	
	
	
	

=======================================================================================================================
(Extended) Probe script description:
BCIENgProbeIterface.Initialize :160

REM 			comment
PROBE			start of new probe, flush new one
RULE			filter rule on the target (called method) for probes
RULEWITHIN		filter rule on the caller for probes
FILTER			filter rule for set of probes on the target(called method) (these are the ones set from GUI)
FILTERWITHIN	filter rule for set of probes on the caller



=========================================================================================================================
Static instrumentation example:
We want to collect data for the component called second threating it as a black box. So we have to monitor all method calls
from the world to second* and all call from second to the world (except for java.* subworld).

this is the probe.script that we can do


PROBE
RULE probes* * * * exclude
RULE log* * * * exclude
RULE * bct* * * exclude
RULEWITHIN * bct* * * exclude
REF AFTERCALL bctLoggingProbeOutgoing_probe$Probe_0 _afterCall (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V returnedObject,className,methodName,methodSig,args
REF BEFORECALL bctLoggingProbeOutgoing_probe$Probe_0 _beforeCall (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V className,methodName,methodSig,args
FILTERWITHIN  second* * * * INCLUDE
FILTERWITHIN  * * * * EXCLUDE
FILTER  java* * * * EXCLUDE
FILTER  second* * * * EXCLUDE
PROBE
RULE probes* * * * exclude
RULE log* * * * exclude
RULE * bct* * * exclude
RULEWITHIN * bct* * * exclude
REF AFTERCALL bctLoggingProbeOutgoing_probe$Probe_0 _afterCall (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V returnedObject,className,methodName,methodSig,args
REF BEFORECALL bctLoggingProbeOutgoing_probe$Probe_0 _beforeCall (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V className,methodName,methodSig,args
FILTERWITHIN  second* * * * EXCLUDE
FILTERWITHIN  * * * * INCLUDE
FILTER  second* * * * INCLUDE
FILTER  * * * * EXCLUDE


Pay Attention: OUR modief version of BCIEngine does not work like the IBM one. 
In our version filter rules refers to the probe defined above them. In IBM version all filter rules are applied to all probes.



===========================================================================
Compile under Windows

Eclipse:
	
	
	
	0) We tryed it with Visual Studio v8 (2005)
		add to environment PATH path\to\Visual Studio 8\VC\bin;path\to\Visual Studio 8\Common7\IDE
		the first is for nmake and cl, the second is for mspdb80.dll
		
	1) Create a new C++ project (for BCI or BCIDynamic):
		set the folllowing environmental variables into it:
		INCLUDE: path\to\Visual Studio 8\VC\include;path\to\Visual Studio 8\VC\platformSDK\include
		LIB: path\to\Visual Studio 8\VC\lib;path\to\Visual Studio 8\VC\platformSDK\lib
		
	2) Import sources (BCI or BCIDynamic) into it:
		include the following files:
			*.mak
			*.def
			*.h
			*.cpp
			*.c++
			*.iso
			*.rc
			
	3) Modify ProbeInstrumenter.mak	and set default configuration to Release
	
	4) right click on /BCIEng/BCIEngProbe/ProbeInstrumenter and select "create make target":
		set compiler to nmake
		set target ProbeInstrumenter.mak
		
	5) Build the make target