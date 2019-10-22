This folder contains a suite of programs that can be adopted to identify 
regression faults in C/C++ programs using BCT.
These programs work in four steps:
	1) record the behavior of a correct version of the software,
	2) record the behavior of the new software version affected by a fault during a failing execution
	3) infer the models
	4) identify anomalies
	 
These programs depend on:
	bash
	gdb	- the GNU debugger
	python
	
	

	
Tutorial:
	
	1)	set the BCT_C_HOME environment variable to point to this folder
		export BCT_C_HOME=/home/user/gdbMonitoring
		
	2)	cd to a folder where you want to keep recorded data (can b ethe current 
		folder. or the program folder)
	
		cd /home/user/bctMonitoring		
		
	2)	identify the functions modified in the new software by running 
	bctIdentifyModifiedMethods.sh
		
		${BCT_C_HOME}/bctIdentifyModifiedMethods.sh \ 
			<path/to/stableVersionSources> <path/to/newVersionSources>
			
			
		
	3)	specify eventual methods/functions that you consider useful for the monitoring 
		(e.g. method not modified but that implements features that are 
		relevant for the application)
		
		add the C/C++ signature of these methods to file 
		bct.relevantFunctions.txt in the current directory (one method signature
		 for each line)
		
			e.g. you can add the following line
			
				MyClass::myMeth(int x)
		
			
		if you just add a prefix of the method signature all the methods that 
		begin with the same prefix will be monitored
		
			by adding the line 
				MyClass::myMeth
				
			all the methods with a signature starting with MyClass::myMeth will 
			be monitored, e.g.
				MyClass::myMeth(long x)
				MyClass::myMeth(int x, double y)
				MyClass::myMethodology(long x)
			
			
			by adding the line 
				MyClass::
			all the methods declared within class MyClass will be monitored
			
			
			by adding the line 
				MyClass
			all the methods declared within classes that start with MyClass will
			be monitored (pay attention also eventual functions  that contain 
			the string MyClass will be monitored)
			
			
			
	4)	monitor the execution of the original software during valid executions
		by running
			bctMonitorValidExecutionsWithGDB.sh <executableToRun> <args>
			
		e.g.
			bctMonitorValidExecutionsWithGDB.sh ./xmltest
			
		After the execution of the test the file bct.monitor.valid.txt will 
		be generated
		
	5)	monitor the execution of the modified software during failing executions
		by running
			bctMonitorFailingExecutionsWithGDB.sh <executableToRun> <args>
			
		e.g.
			bctMonitorFailingExecutionsWithGDB.sh ./xmltest
			
		After the execution of the test the file bct.monitor.failing.txt will 
		be generated	
		
	6)	infer models with BCT
	
		java -cp path/to/bct-standalone.jar tools.gdbTraceParser.GdbTraceParser \
			bct.monitor.valid.txt				 
			
			
	7) identify anomalies
		
		java -cp path/to/bct-standalone.jar tools.gdbTraceParser.GdbTraceParser -check \
			bct.monitor.failing.txt		
			
			
