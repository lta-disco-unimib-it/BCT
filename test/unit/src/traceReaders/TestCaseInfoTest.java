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
package traceReaders;

import junit.framework.TestCase;
import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.VariableInfo;
import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.TestCaseInfo;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import regressionTestManager.tcData.handlers.TcInfoHandlerFile;
import conf.EnvironmentalSetter;

public class TestCaseInfoTest extends TestCase {

	public TestCaseInfoTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddRem(){
		TcInfoHandler tcih = new TcInfoHandlerFile();
		tcih.init( (RegressionTestManagerHandlerSettings) EnvironmentalSetter.getInvariantGeneratorSettings().getMetaDataHandlerSettings() );
		TestCaseInfo tci = tcih.getTestCaseInfo("TestTC");
		MethodInfo mi = tcih.getMethodInfo("testMethod");
		VariableInfo vi = tcih.getVariableInfo("testVar","aa","aa");
		tci.addMethod(mi);
		System.out.println(tci);
		try {
			tcih.save();
		} catch (TcInfoHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tci = tcih.getTestCaseInfo("TestTC");
		System.out.println(tci);
		assertEquals( new Integer(1), tci.getMethodOccurrencies(mi.getId()) );
		
	}
}
