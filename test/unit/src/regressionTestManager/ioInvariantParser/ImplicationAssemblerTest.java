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
import regressionTestManager.tcSpecifications.TcSpecificationAnd;
import regressionTestManager.tcSpecifications.TcSpecificationEquals;
import regressionTestManager.tcSpecifications.TcSpecificationEqualsNull;
import regressionTestManager.tcSpecifications.TcSpecificationGreaterThan;
import regressionTestManager.tcSpecifications.TcSpecificationLessThan;
import regressionTestManager.tcSpecifications.TcSpecificationNot;
import regressionTestManager.tcSpecifications.TcSpecificationOr;

public class ImplicationAssemblerTest extends AssemblerTestCase {

	public ImplicationAssemblerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEqNull_EqInt() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( 
				" ( "+parameter0+" == null ) ==> ( "+parameter1+" == "+intVal+" ) ", 
				programPointEnter);
		
		assertEquals ( 3, result.size() );
		
		assertTrue ( result.contains( new TcSpecificationOr(
				new TcSpecificationEqualsNull( var0Enter )
				) ) );
		
		System.out.println( result.get(0) );
		System.out.println( result.get(1) );
		System.out.println( result.get(2) );
		
		
		
		assertTrue ( result.contains( 
				new TcSpecificationOr ( 
				new TcSpecificationAnd( 
						new TcSpecificationNot( new TcSpecificationEqualsNull ( var0Enter ) ), 
						new TcSpecificationNot( new TcSpecificationEquals ( var1, intVal ) ) )
				)));
		
		assertTrue ( result.contains( 
				new TcSpecificationOr (
				new TcSpecificationAnd( new TcSpecificationNot( new TcSpecificationEqualsNull ( var0Enter ) ), 
						new TcSpecificationEquals ( var1, intVal ) )
				)));
		
	}
	
	public void testEqNull_GtInt() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( 
				" ( "+parameter0+" == null ) ==> ( "+parameter1+" > "+intVal+" ) ", 
				programPointEnter);
		
		assertEquals ( 3, result.size() );
		
		TcSpecificationEqualsNull A = new TcSpecificationEqualsNull( var0Enter );
		TcSpecificationEquals B1 = new TcSpecificationEquals ( var1, intVal + 1 );
		TcSpecificationGreaterThan B2 = new TcSpecificationGreaterThan ( var1, intVal + 1 );

		//A
		assertTrue ( result.contains( new TcSpecificationOr(
					A
				) ) );
		
		
		
		
		//!A and !B
		TcSpecificationOr or = new TcSpecificationOr ( 
				new TcSpecificationAnd( 
						new TcSpecificationNot( A ), 
						new TcSpecificationNot( B1 ) )
				);
		
		or.addElement(
				new TcSpecificationAnd( 
						new TcSpecificationNot( A ), 
						new TcSpecificationNot( B2 ) )
				
				);
		
		assertTrue ( result.contains( or ));
		
		
		// !A and B
		
		or = new TcSpecificationOr (
				new TcSpecificationAnd(	new TcSpecificationNot( A ), B1 )
				);
		or.addElement( 
				new TcSpecificationAnd(	new TcSpecificationNot( A ), B1 )
				);		
		assertTrue ( result.contains( or ) );
		
	}
	
	public void testLTEInt_EqInt() throws InvariantParseException{
		ArrayList result = IoInvariantParser.getTestCaseSpecifications( 
				" ( "+parameter0+" <= "+intVal+" ) ==> ( "+parameter1+" == "+intVal2+" ) ", programPointEnter );
	
		TcSpecification A1 = new TcSpecificationEquals( var0Enter, intVal );
		
		TcSpecification A2 = new TcSpecificationEquals( var0Enter, intVal -1  );
		
		TcSpecification A3 = new TcSpecificationLessThan( var0Enter, intVal -1 );
		
		TcSpecification B = new TcSpecificationEquals( var1 , intVal2 );
		
		System.out.println(result);
//		A
		TcSpecificationOr or = new TcSpecificationOr(
				A1
			);
		
		or.addElement( A2 );
		
		or.addElement( A3 );
		
		
		assertTrue ( result.contains( or ) );
		
		
//		!A and !B
		or = new TcSpecificationOr ( 
				new TcSpecificationAnd( 
						new TcSpecificationNot( A1 ), 
						new TcSpecificationNot( B ) )
				);
		
		or.addElement(
				new TcSpecificationAnd( 
						new TcSpecificationNot( A2 ), 
						new TcSpecificationNot( B ) )
				
				);
		
		or.addElement(
				new TcSpecificationAnd( 
						new TcSpecificationNot( A3 ), 
						new TcSpecificationNot( B ) )
				
				);
		
		assertTrue ( result.contains( or ) );
		
//		!A and B
		or = new TcSpecificationOr ( 
				new TcSpecificationAnd( 
						new TcSpecificationNot( A1 ), 
						B )
				);
		
		or.addElement(
				new TcSpecificationAnd( 
						new TcSpecificationNot( A2 ), 
						B ) 
				
				);
		
		or.addElement(
				new TcSpecificationAnd( 
						new TcSpecificationNot( A3 ), 
						B ) 
				
				);
		
		assertTrue ( result.contains( or ) );
		
	}
	
}
