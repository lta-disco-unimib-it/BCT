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
package cpp.gdb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

public class FunctionMonitoringDataTest {

	@Test
	public void testIsStatic(){
		
		assertTrue ( testIsStaticInternal("_Z12assertEqualsll") );
		assertFalse ( testIsStaticInternal("_ZN10WorkersMap9addWorkerESsl") );
		assertFalse ( testIsStaticInternal("_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE") );
		
		assertTrue ( testIsStaticInternal("_ZN10WorkersMapC1Ev") );
		assertTrue ( testIsStaticInternal("_ZN10WorkersMapD2Ev") );
		
		
		
		
		
	}

	@Ignore("Cases that are static but cannot be identified")
	@Test
	public void testIsStatic_cannotBeIdentifiedWithSIgnatureOnly(){
		//our implementation cannot detect that the following isStatic using only the parsing of signature
		assertTrue ( testIsStaticInternal("_ZN6ClassA12doStaticLineEi") );
		assertTrue ( testIsStaticInternal("_ZN6ClassA8doStaticEi") );
	}
	
	@Test
	public void testIsStatic_WithFunctionsFinderFile() throws FileNotFoundException, IOException{
		
		File file = new File ( "test/unit/artifacts/cpp/functionNamesAnalysis/staticFunctions.properties");
		StaticFunctionsFinderFile finder = new StaticFunctionsFinderFile(file);
		StaticFunctionsFinderFactory.setInstance(finder);
		
		assertTrue ( testIsStaticInternal("_Z12assertEqualsll") );
		assertFalse ( testIsStaticInternal("_ZN10WorkersMap9addWorkerESsl") );
		assertFalse ( testIsStaticInternal("_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE") );
		
		assertTrue ( testIsStaticInternal("_ZN10WorkersMapC1Ev") );
		assertTrue ( testIsStaticInternal("_ZN10WorkersMapD2Ev") );
		
		assertTrue ( testIsStaticInternal("_ZN6ClassA12doStaticLineEi") );
		
		assertTrue ( testIsStaticInternal("_ZN6ClassA8doStaticEi") );
		
		
		
	}

	@Test
	public void testConstructor(){
		
		assertTrue ( testConstructorInternal("_ZNSsC1EPKcRKSaIcE" ) );
		
		assertTrue ( testConstructorInternal("_ZNSaIcEC1Ev@plt" ) );
		
		
		assertTrue ( testConstructorInternal("_ZNSsC1EPKcRKSaIcE@plt" ) );
		
		assertTrue ( testConstructorInternal("_ZN10WorkersMapC1Ev") );
		
		assertFalse ( testConstructorInternal("_ZN6ClassA12doSomethingCEv") );
	}
	
	@Test
	public void testDestructor(){
		assertTrue ( testDestructorInternal("_ZN10WorkersMapD2Ev") );
	}
	
	private boolean testIsStaticInternal(String functionName) {
		FunctionMonitoringData f = new FunctionMonitoringData(functionName);
		return f.isStatic();
	}
	
	private boolean testConstructorInternal(String functionName) {
		FunctionMonitoringData f = new FunctionMonitoringData(functionName);
		return f.isConsructor();
	}
	
	private boolean testDestructorInternal(String functionName) {
		FunctionMonitoringData f = new FunctionMonitoringData(functionName);
		return f.isDestructor();
	}
}
