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

import java.util.ArrayList;

import regressionTestManager.tcSpecifications.TcSpecification;
import regressionTestManager.tcSpecifications.TcSpecificationEquals;
import regressionTestManager.tcSpecifications.TcSpecificationEqualsNull;

public class EqualsAssemblerTest extends AssemblerTestCase {
	
	public EqualsAssemblerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testVarEqualsvar() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" == "+parameter1, programPointEnter);
		assertEquals( 1, result.size() );
		
		TcSpecificationEquals expected = new TcSpecificationEquals( var0Enter, var1 );
		
		TcSpecification actual = (TcSpecification) result.get(0);
		
		assertEquals( expected, actual );
	}
	
	public void testVarEqualsInt() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" == "+intVal, programPointEnter);
		assertEquals( 1, result.size() );
		
		TcSpecificationEquals expected = new TcSpecificationEquals( var0Enter, intVal );
		
		TcSpecification actual = (TcSpecification) result.get(0);
		
		assertEquals( expected, actual );
	}
	
	public void testVarEqualString() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" == "+stringVal, programPointEnter);
		assertEquals( 1, result.size() );
		
		TcSpecificationEquals expected = new TcSpecificationEquals( var0Enter, stringValExp );
		
		TcSpecification actual = (TcSpecification) result.get(0);
		
		assertEquals( expected, actual );
	}
	
	public void testVarEqualNull() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" == null", programPointEnter);
		
		assertEquals( 1, result.size() );
		
		TcSpecification expected = new TcSpecificationEqualsNull( var0Enter );
		
		assertTrue( result.contains(expected) );
	}
}
