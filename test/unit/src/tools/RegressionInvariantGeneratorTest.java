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
package tools;

import java.util.ArrayList;

import junit.framework.TestCase;
import regressionTestManager.detectionMatrix.InteractionTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;

public class RegressionInvariantGeneratorTest extends TestCase {
	public final String test1 = "test.TestClass.test1";
	public final String test2 = "test.TestClass.test2";
	public final String test3 = "test.TestClass.test3";
	
	public final String C1_1 = "packC.C.m1()";
	public final String C1_2 = "packC.C.m2()";
	public final String C1_3 = "packC.C.m3()";
	
	public final String C2_1 = "packD.D.m1()";
	public final String C2_2 = "packD.D.m2()";
	
	
	public RegressionInvariantGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCleanDefault_noDefaultTest(){
		InteractionTcDetectionMatrix m = new InteractionTcDetectionMatrix();
		
		ArrayList<String> elements = new ArrayList<String>();
		
		elements.add(C1_1);
		elements.add(C1_2);
		elements.add(C1_3);
		
		elements.add(C2_1);
		elements.add(C2_2);
		
		m.setElementsToCover(elements);
		
		TestCaseInfo tcInfo1 = new TestCaseInfo(test1,test1,null);
		TestCaseInfo tcInfo2 = new TestCaseInfo(test2,test2,null);
		TestCaseInfo tcInfo3 = new TestCaseInfo(test3,test3,null);

		
		m.addTestVector(tcInfo1, new Boolean[]{true,true,true,false,false});
		m.addTestVector(tcInfo2, new Boolean[]{false,false,true,true,true});
		m.addTestVector(tcInfo3, new Boolean[]{false,false,true,true,false});
		
		TcDetectionMatrix newM = RegressionInvariantGenerator.copyCoveredElements(m);
		
		assertEquals(m,newM);
		
	}
}
