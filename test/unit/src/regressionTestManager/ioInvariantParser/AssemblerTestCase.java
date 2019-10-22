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
package regressionTestManager.ioInvariantParser;

import junit.framework.TestCase;

public class AssemblerTestCase extends TestCase {
	protected String programPointEnter;
	protected String parameter0;
	protected String parameter1;
	protected String stringVal;
	protected Double intVal;
	protected Double intVal2;
	protected Double doubleVal;
	protected Variable var0Enter;
	protected Variable var0Exit;
	protected Variable var1;
	protected String stringValExp;
	protected String programPointExit;

	public AssemblerTestCase( String name ){
		super( name );
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		programPointEnter  = "testPakage.testClass.testMethod():::ENTER";
		programPointExit  = "testPakage.testClass.testMethod():::EXIT1";
		
		parameter0 = "parameter[0].x";
		parameter1 =  "parameter[1].y.m()";
		
		var0Enter = new Variable( programPointEnter, parameter0 );
		var0Exit = new Variable( programPointExit, parameter0 );
		var1 = new Variable ( programPointEnter, parameter1 );
		
		stringVal = "\"a string Value\"";
		stringValExp = "a string Value";
		
		intVal = new Double(2);
		intVal2 = new Double(4);
		doubleVal = new Double(2.345);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


}
