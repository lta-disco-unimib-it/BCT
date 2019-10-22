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
package check.ioInvariantParser;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import check.ioInvariantParser.ImplicationTest.Complex;

import conf.EnvironmentalSetter;
import junit.framework.TestCase;
import testSupport.check.ioInvariantParser.MyClass;

@RunWith(Parameterized.class)
public class ImplicationTest {
	
	 @Parameters
     public static Collection<Object[]> data() {
             return Arrays.asList(new Object[][] {
            	{ Boolean.TRUE } , { Boolean.FALSE }	 
             });
     }
	
	@BeforeClass
	public static void setupStatic(){
		File bctHome = new File("test/unit/artifacts/BCT_HOME");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
	}

	private boolean offline;
	
	public ImplicationTest(boolean offlineAnalysis ){
		this.offline = offlineAnalysis;
		IoInvariantParser.setOfflineAnalysis(offlineAnalysis);
	}
	
	@Test
	public void testParameters() throws InvariantParseException{
		Object parameters[] = new Object[1];
		parameters[0] =	new MyClass();
		
		checkImplication(false,"(parameter[0].intValue == 4)", "parameter[0]", parameters, null, null);
		
		checkImplication(false,"(parameter[0] != null)", "parameter[0]", parameters, null, null);
		
		
		parameters[0] = null;
		checkImplication(true, "(parameter[0] != null)", "parameter[0]", parameters, null, null);

		
		parameters = new Object[0];
		checkImplication(true,"(parameter[0] != null)", "parameter[0]", parameters, null, null);
	}
	
	public static class P {
		int precedence;
		
		public P ( int v ){
			precedence = v;
		}
	}
	
	@Test
	public void testBug() throws InvariantParseException{
		Object[] parameters = new Object[2];
		parameters[0] = new P(50);
		parameters[1] = Boolean.TRUE;
		
		boolean result = IoInvariantParser.evaluateExpression("(parameter[0] != null)  ==>  (parameter[0].precedence > parameter[1])", parameters, null, null );
		assertTrue(result);
	}
	
	@Test
	public void testLocals() throws InvariantParseException{
		

		HashMap<String, Object> locals = new HashMap<String,Object>();
		locals.put("input.intValue", 4);
		locals.put("input.m2.done", 6);
		locals.put("input.doubleValue", 4.0);
		locals.put("input.stringValue", "stringa");
		
		if ( ! offline ){
			return;
		}
		
		boolean result = IoInvariantParser.evaluateExpression("(input.intValue == 4) ==> (input.intValue == 6)", null, null, locals );
		assertFalse(result);
		
		checkImplication(false,"(input.intValue == 4)", "input", null, null, locals);
		
		checkImplication(false,"(input != null)", "input", null, null, locals);
		
		
		locals = new HashMap<String,Object>();
		locals.put("input", null);
		checkImplication(true, "(input != null)", "input", null, null, locals);

		
		locals = new HashMap<String,Object>();
		checkImplication(true,"(input != null)", "input", null, null, locals);
	}
	
	
	@Test
	public void testParametersAndLocals() throws InvariantParseException{
		Object parameters[] = new Object[1];
		parameters[0] =	new MyClass();
		
		HashMap<String, Object> locals = new HashMap<String,Object>();
		locals.put("input.intValue", 4);
		locals.put("input.m2.done", 6);
		locals.put("input.doubleValue", 4.0);
		locals.put("input.stringValue", "stringa");
		
		checkImplication(false,"(parameter[0].intValue == 4)", "parameter[0]", parameters, null, locals);
		
		checkImplication(false,"(parameter[0] != null)", "parameter[0]", parameters, null, locals);
		
		if ( offline )
			checkImplication(false,"(input.intValue == 4)", "input", parameters, null, locals);
		
		if ( offline )
			checkImplication(false,"(input != null)", "input", parameters, null, locals);
		
		
		locals = new HashMap<String,Object>();
		locals.put("input", null);
		parameters[0] = null;
		if ( offline )
			checkImplication(true, "(input != null)", "input", parameters, null, locals);
		checkImplication(true, "(parameter[0] != null)", "parameter[0]", parameters, null, locals);

		
		parameters = new Object[0];
		locals = new HashMap<String,Object>();
		if ( offline )
			checkImplication(true,"(input != null)", "input", parameters, null, locals);
		checkImplication(true,"(parameter[0] != null)", "parameter[0]", parameters, null, locals);
	}
	
	public static class Complex {
		private double real;
		private double imaginary;

		public Complex(double real, double imaginary) {
	        super();
	        this.real = real;
	        this.imaginary = imaginary;
	    }

		public static final Complex NaN = new Complex(Double.NaN, Double.NaN);
		public static final Complex I = new Complex(0.0, 1.0);
	}
	
	@Test
	public void testNaN() throws InvariantParseException{
		Object parameters[] = new Object[1];
		
		parameters[0] = new Complex(1, 2);
		
		HashMap<String, Object> locals = new HashMap<String,Object>();
		locals.put("parameter[0]", parameters[0]);
		boolean result;

		
		IoInvariantParser.setOfflineAnalysis(false);
		
		
		
		Complex returnValue = new Complex(Double.NaN, Double.NaN);
		
		result = IoInvariantParser.evaluateExpression("parameter[0].NaN.real != parameter[0].NaN.real", parameters, returnValue, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("parameter[0].NaN.real == parameter[0].NaN.real", parameters, returnValue, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("parameter[0].NaN.real == parameter[0].NaN.imaginary", parameters, returnValue, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(parameter[0].real == 0) ==> (returnValue.real == 0)", parameters, returnValue, locals);
		assertTrue(result);
		

		result = IoInvariantParser.evaluateExpression("(parameter[0].I != null and parameter[0].I.I != null)  ==>  (parameter[0].I.I.NaN == parameter[0].I.NaN)", parameters, null, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(parameter[0].I != null and parameter[0].I.I != null)  ==>  (parameter[0].I.I.NaN != parameter[0].I.NaN)", parameters, null, locals);
		assertFalse(result);
		
		result = IoInvariantParser.evaluateExpression("(parameter[0].I == null and parameter[0].I.I != null)  ==>  (parameter[0].I.I.NaN != parameter[0].I.NaN)", parameters, null, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(parameter[0].NaN != null and parameter[0].NaN.NaN != null)  ==>  (parameter[0].NaN.I == parameter[0].NaN.NaN.I)", parameters, null, locals);
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(parameter[0].NaN != null and parameter[0].NaN.NaN != null)  ==>  (parameter[0].NaN.I != parameter[0].NaN.NaN.I)", parameters, null, locals);
		assertFalse(result);
		
		
		
		IoInvariantParser.setOfflineAnalysis(true);
	}
	
	private void checkImplication(boolean variableNotDefined, String implication, String varToCheck, Object[] paremeters, Object returnValue, HashMap<String, Object> locals)
			throws InvariantParseException {
		boolean result;
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".m2.done == 6)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".doubleValue == 4.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 5)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".m2.done == 7)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".doubleValue == 8.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue == 4)", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue == 5)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue != 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue != 5)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue > 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue > 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue >= 7)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue >= 4)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue < 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue < 5)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue <= 4)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".intValue <= 3)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		//
		// STRING
		//
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue == \"stringa\")", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue > \"astringa\")", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue > \"tstringa\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue >= \"astringa\")", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue >= \"tstringa\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue < \"tstringa\")", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue < \"astringa\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue <= \"tstringa\")", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue <= \"astringa\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue <= \"stringa\")", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue >= \"stringa\")", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue == \"string\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue != \"string\")", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".stringValue != \"stringa\")", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		//
		// DOUBLE
		//
		
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue == 4)", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue == 5)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue != 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue != 5)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue > 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue > 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue >= 7)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue >= 4)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue < 4)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue < 5)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue <= 4)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue <= 3)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue == 4.0)", paremeters, returnValue, locals );
		assertTrue(result);

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue == 5.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue != 4.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue != 5.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue > 0.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue > 4.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue >= 7.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue >= 4.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue < 4.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue < 5.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue <= 4.0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".doubleValue <= 3.0)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		//
		//
		//
		
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done == 6)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done > 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done > 6)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );

		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done >= 7)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done >= 6)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done < 6)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done < 7)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done <= 6)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression(implication + " ==> (" + varToCheck + ".m2.done <= 5)", paremeters, returnValue, locals );
		assertFalseIfDefined( variableNotDefined, result );
		
		if ( !offline ){
			return;
		}

		//variable nonEx does not exist
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.done == true)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.done == false)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar == 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar != 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar > 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar < 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar >= 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.intVar <= 0)", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.done == \"ABC\")", paremeters, returnValue, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("(" + varToCheck + ".intValue == 4) ==> (" + varToCheck + ".m2.nonEx.done != \"ABC\")", paremeters, returnValue, locals );
		assertTrue(result);
		
	}

	private void assertFalseIfDefined(boolean variableNotDefined, boolean result) {
		if ( variableNotDefined ) { 
			assertTrue(result); 
		} else { 
			assertFalse(result); 
		};
	}
	
}
