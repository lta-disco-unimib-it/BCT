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
package regressionTestManager.tcData.handlers;


import java.util.Properties;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.tcData.MethodInfo;
import conf.SettingsException;

public class TcInfoHandlerFileTest extends TestCase {
		private TcInfoHandlerFile tcInfoHandler;
		private static final String artifactsDir = "tests/unit/regressionTestManager/tcData/handlers/TcInforHandlerFileTest/";
		private static final String methOne = "pA.A.m()";
		
		TcInfoHandlerFileTest(){
			reloadInfoHandler();
		}
		



		private void reloadInfoHandler() {
			// TODO Auto-generated method stub
			tcInfoHandler = new TcInfoHandlerFile();

			Properties p = new Properties();
			p.put(RegressionTestManagerHandlerSettings.Options.testCaseInfoHandler, "");
			p.put(RegressionTestManagerHandlerSettings.Options.linerAlgorithm, "branchbound");
			p.put(TcInfoHandlerFile.PropertiesKeys.DIR, artifactsDir);

			RegressionTestManagerHandlerSettings s = new RegressionTestManagerHandlerSettings();
			try {
				s.init(p);
			} catch (SettingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}


	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMethodInfo() throws TcInfoHandlerException {
		MethodInfo mInfo = tcInfoHandler.getMethodInfo(methOne);
		
		assertEquals(mInfo.getId(),"0");
		
		tcInfoHandler.save();
		
		reloadInfoHandler();
		
		MethodInfo mInfoL = tcInfoHandler.getMethodInfo(methOne);
		
		assertEquals(mInfo.getId(),"0");
		
		
	}

	@Test
	public void testGetTestCaseInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTestCaseInfoFromId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgramPointInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateProgramPointInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableInfoStringStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableInfoString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveTestCaseInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModifiedTcInfoEntity() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModifiedMethodInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModifiedVariableInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetModifiedTestCaseInfo() {
		fail("Not yet implemented");
	}

	@Test
	public void testSave() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMethodName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMethodsIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTestCasesIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProgramPointIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableFromId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVariableProgramPoint() {
		fail("Not yet implemented");
	}

}
