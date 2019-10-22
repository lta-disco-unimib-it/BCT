/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package regressionTestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import junit.framework.TestCase;
import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import regressionTestManager.tcData.handlers.TcInfoHandlerFile;
import conf.EnvironmentalSetter;

public class TcInfoHandlerFileTest extends TestCase {
	private TcInfoHandlerFile handler = null;
	
	private String packageName = "myPackage";
	private String methodOneName = "methodOne()";
	private String programPointName = packageName+"."+methodOneName;
	private String enter = ":::ENTER";
	private String exit = ":::EXIT1";
	private String programPointEnter = programPointName+enter;
	private String methodTwoName = "methodTwoName()";
	private String variableOneName = "parameter[0].name.toString()";
	private String variableOneValue = "45";
	private String testCaseOne = "myTestPackage.myTestClass.testCaseOne()";
	private String testCaseTwo = "myTestPackage.myTestClass.testCaseOne()";
	
	private String variableTwoName = "parameter[0].myInt.intValue()";
	private String variableTwoValue = "7";
	
	 public static boolean deleteDir(File dir) {
	        if(!dir.exists()) {
	            return true;
	        }
	        boolean res = true;
	        if(dir.isDirectory()) {
	            File[] files = dir.listFiles();
	            for(int i = 0; i < files.length; i++) {
	                res &= deleteDir(files[i]);
	            }
	            res = dir.delete();//Delete dir itself
	        } else {
	            res = dir.delete();
	        }
	        return res;
	 }
	
	
	public TcInfoHandlerFileTest(String name) {
		super(name);
				
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		File tmp = new File("tmp");
		deleteDir ( tmp );
		
		Properties p = new Properties();
		p.put("type","tools.InvariantGenerator");
		p.put("temporaryDir", "tmp");
		p.put("FSAEngine","KBehavior");
		p.put( "daikonConfig","essentials");
		p.put( "traceReader.type","traceReaders.raw.FileTracesReader");

		p.put( "traceReader.tracesPath","tmp/01-logs");

		p.put( "traceReader.ioTracesDirName","ioInvariantLogs");

		p.put( "traceReader.interactionTracesDirName","interactionInvariantLogs");


		

		p.put( "normalizedTraceHandler.type","traceReaders.normalized.NormalizedTraceHandlerFile");

		p.put( "normalizedTraceHandlerFile.declsDir","tmp/normalized/decls");
		p.put( "normalizedTraceHandlerFile.dtraceDir","tmp/normalized/dtrace");
		p.put( "normalizedTraceHandlerFile.interactionDir","tmp/normalized/interaction");


		
		p.put( "metaDataHandlerSettingsType",RegressionTestManagerHandlerSettings.class.getCanonicalName());
		p.put( RegressionTestManagerHandlerSettings.class.getCanonicalName()+".testCaseInfoHandler.type",TcInfoHandlerFile.class.getCanonicalName());
		p.put( RegressionTestManagerHandlerSettings.class.getCanonicalName()+".testCaseInfoHandler.dir","tmp/infoHandler");
		p.put( RegressionTestManagerHandlerSettings.class.getCanonicalName()+".linearAlgorithm", "greedy" );


		EnvironmentalSetter.setInvariantGeneratorSettings(p);

		
		
		handler = new TcInfoHandlerFile();
		handler.init((RegressionTestManagerHandlerSettings) EnvironmentalSetter.getInvariantGeneratorSettings().getMetaDataHandlerSettings());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		
	}



	
	public void testGetMethodInfo() throws TcInfoHandlerException {
		MethodInfo infoOne = handler.getMethodInfo( methodOneName );
		String id = infoOne.getId();
		
		handler.save();
		
		MethodInfo infoTwo = handler.getMethodInfo( methodTwoName  );
		String idTwo = infoTwo.getId();
		
		infoOne = handler.getMethodInfo( methodOneName );
		
		assertEquals( id, infoOne.getId() );
		assertFalse( id.equals(idTwo));
		
	}

	public void testTestCaseInfo() throws TcInfoHandlerException {
		TestCaseInfo tcInfo = handler.getTestCaseInfo(testCaseOne);
		String tcid = tcInfo.getId();
		
		
		
		ProgramPointInfo ppInfo = handler.createProgramPointInfo();
		VariableInfo varOne  = handler.getVariableInfo( programPointEnter, variableOneName, variableOneValue );
		ppInfo.addVariable( varOne );
		tcInfo.addProgramPoint(ppInfo);
		
		
		handler.save();
		
		assertEquals ( 1, handler.getTestCasesIds().size() );
		
		TestCaseInfo tcInfoGet = handler.getTestCaseInfoFromId(tcid);
		
		assertEquals( tcInfo, tcInfoGet );
		
		
	}


	public void testCreateAndGetProgramPointInfo() throws TcInfoHandlerException {
		ProgramPointInfo pp = handler.createProgramPointInfo();
		
		
		String id = pp.getId();
		
		
		HashSet<String> ids = new HashSet<String>();
		ids.add(id);
		
		handler.save();
		
		assertEquals( 1, handler.getProgramPointIds().size() );
		
		assertTrue( handler.getProgramPointIds().contains(id) );

		pp = handler.createProgramPointInfo();
		pp.addVariable( handler.getVariableInfo(programPointEnter, variableOneName, variableOneValue) );
		ProgramPointInfo ppStore = pp;
		
		id = pp.getId();

		ids.add(id);

		handler.save();
		assertEquals( ids, handler.getProgramPointIds() );
		
		
		
		ProgramPointInfo ppInfo = handler.getProgramPointInfo(id);
		assertEquals ( ppStore, ppInfo );
		
	}

	public void testGetVariableInfo() throws TcInfoHandlerException {
		VariableInfo varOneInfo = handler.getVariableInfo(programPointEnter, variableOneName, variableOneValue);
		String idOne = varOneInfo.getId();
		HashSet<String> ids = new HashSet<String>();
		ids.add(idOne);
		VariableInfo v = handler.getVariableInfo(programPointEnter, variableOneName, variableOneValue);
		assertTrue( v == varOneInfo );
		
		handler.save();
		
		VariableInfo varTwoInfo = handler.getVariableInfo(programPointEnter, variableTwoName, variableTwoValue);
		String idTwo = varTwoInfo.getId();
		ids.add( idTwo );
		v = handler.getVariableInfo(programPointEnter, variableOneName, variableOneValue);
		
		assertEquals( v, varOneInfo );
		assertEquals( ids, handler.getVariableIds() );
		
		VariableInfo var = handler.getVariableFromId(idOne);
		assertTrue ( v == var );
		
		handler.save();
		
		//Variable value
		
		String onevalue = handler.getVariableValue(idOne);
		assertEquals( variableOneValue, onevalue );
		
		String twovalue = handler.getVariableValue(idTwo);
		assertEquals( variableTwoValue, twovalue );
		
		
		//Variable name
		
		String oneName = handler.getVariableName(idOne);
		assertEquals( variableOneName, oneName );
	}


	public void testSetModified__TcInfo() {
		//
	}

	public void testSetModified__MethodInfo() {
		//
	}

	public void testSetModified__VariableInfo() {
		//
	}

	public void testSetModified__TestCaseInfo() {
		//
	}

	
	
	
	public void testSave() {
		//tested in other methods
	}

	public void testGetMethodName() throws TcInfoHandlerException {
		MethodInfo infoOne = handler.getMethodInfo( methodOneName );
		String id = infoOne.getId();
		
		assertEquals( methodOneName, handler.getMethodName(id) );
		
		handler.save();
		
		assertEquals( methodOneName, handler.getMethodName(id) );
		
		MethodInfo infoTwo = handler.getMethodInfo( methodTwoName  );
		String idTwo = infoTwo.getId();
		
		infoOne = handler.getMethodInfo( methodOneName );
		
		assertEquals( methodOneName, handler.getMethodName(id) );
		
		
	}

	public void testGetMethodsIds() {
		HashSet<String> ids = new HashSet<String>();
		
		MethodInfo infoOne = handler.getMethodInfo( methodOneName );
		String id = infoOne.getId();
		ids.add(id);
		
		MethodInfo infoTwo = handler.getMethodInfo( methodTwoName );
		id = infoTwo.getId();
		ids.add(id);
		
		assertEquals( 2, handler.getMethodsIds().size() );
		assertEquals( ids, handler.getMethodsIds() );
	}

	public void testGetTestCasesIds() {
		
	}

	public void testGetVariableIds() {
		//
	}

	public void testGetProgramPointIds() {
		//
	}


	public void testGetVariableValue() {
		//
	}

	public void testGetVariableName() {
		//
	}


}
