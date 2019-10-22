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

import regressionTestManager.tcSpecifications.TcSpecificationEquals;
import regressionTestManager.tcSpecifications.TcSpecificationGreaterThan;
import regressionTestManager.tcSpecifications.TcSpecificationLessThan;
import regressionTestManager.tcSpecifications.TcSpecificationNotEqualsNull;
import regressionTestManager.tcSpecifications.TcSpecificationPlusOne;

public class NonEqualsAssemblerTest extends AssemblerTestCase {

	public NonEqualsAssemblerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testVarNEVar() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" != "+parameter1, programPointEnter);
		
		assertEquals( 4, result.size() );
		
		assertTrue ( result.contains( new TcSpecificationEquals( var1, new TcSpecificationPlusOne( var0Enter ) ) ) );
		
		assertTrue ( result.contains( new TcSpecificationGreaterThan( var0Enter, new TcSpecificationPlusOne( var1 )) ));
		
		assertTrue ( result.contains( new TcSpecificationLessThan( var0Enter, var1 ) ));
		
		assertTrue ( result.contains( new TcSpecificationLessThan( var1, var0Enter ) ));
		
	}
	
	public void testVarNEInt() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" != "+intVal, programPointEnter);
		
		assertEquals( 4, result.size() );
		
		assertTrue ( result.contains( new TcSpecificationEquals( var0Enter, intVal + 1  ) ) );
		
		assertTrue ( result.contains( new TcSpecificationLessThan( var0Enter, intVal + 1 ) ));
		
		assertTrue ( result.contains( new TcSpecificationGreaterThan( var0Enter, intVal + 1 ) ));
		
		assertTrue ( result.contains( new TcSpecificationEquals( var0Enter, intVal - 1  ) ) );
		
	}
	
	public void testVarNENull() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( parameter0+" != null ", programPointEnter);
		
		assertEquals( 1, result.size() );
		
		assertTrue ( result.contains( new TcSpecificationNotEqualsNull( var0Enter ) ) );
		
	}
}
