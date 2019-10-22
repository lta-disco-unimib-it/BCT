This folder contains three test cases for the probekit instrumenter

They verify the correct monitoring of components in case of simple(one package per component) multiple component definition and complex (more packages with include and excludes) components definition

For the execution of the test we assume that you are using Eclipse.


SETUP
	0. Install the modified version of the TPTP instrumenter
	1. Create a Java project with the BCT source code in an eclipse workspace (stored for example in /home/userName/workspace/)
	2. Remove the "probes" folder from the source folder list (the test cases use stubs for bct probes)
	3. Open another eclipse workspace
	4. Create a new Java project (TestInstrumentation), select create project from existing source and 
		4.1 select the folder /home/userName/workspace/BCT/tests/system/Instrument/Program
		4.2 press next, in the libraries tab add a class folder, create a folder inthe current project that point to /home/userName/workspace/BCT/bin
	5. Create a new Java project (call it ProbeSimple) ,select create project from existing source and 
		select the folder /home/userName/workspace/BCT/tests/system/Instrument/Probes/SimpleComponentDefinition
	6. Do the same thing for the other two tests

	
Running the tests

These instructions work for all the three test cases, the string XXXX indicates one of Simple, Complex, Mixed.

	SETUP
	1. in ProbeXXXX Select bctTCLP.probe, right button -> static instrumentation -> select TestInstrumentation/bin
	2. Open bctTCLP.probescript, add the following lines
		FILTER java/lang* * * * exclude
		FILTER java/util* * * * exclude
	3. Project->Clean->select TestInstrumentation/bin
	4. in ProbeXXXX Select TCMonitoring.probe, right button -> static instrumentation -> select TestInstrumentation/bin
	5. in ProbeXXXX Select bctTCLP.probe, right button -> static instrumentation -> select TestInstrumentation/bin
	
	RUN
	1.	in TestInstrumentation select test.XXXXComponentsTest, risth button -> Run As -> JUnit Test
	
	OUTPUT
	1. No Failures in JUnit tests
	2. Look at the console wiew, all the printed lines must say that a test is PASSED
	
	