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
package bctFaults;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import check.ioInvariantParser.InvariantParseException;
import check.ioInvariantParser.IoInvariantParser;

public class Bug156 {
	
	public static class AClass {
		int field = 0;
		
		public int getField(){
			return field;
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	
	@Test
	public void testConstraintOnGetter_of_NotExistingParameter() throws InvariantParseException {
		String expression = "parameter[0].getField().intValue() == 0";
		Object parameters[] = new Object[]{};
		Object returnValue = null;
		
		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		//parameter does not exist but something is expected so it is correct to return error ==> this is not true, if it does not exists we cannot say anything
		assertEquals(true,result); 

	}
	
	@Test
	public void testConstraintOnField_of_NotExistingParameter() throws InvariantParseException {
		String expression = "parameter[0].field.intValue() == 0";
		Object parameters[] = new Object[]{};
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		//parameter does not exist but something is expected so it is correct to return error ==> this is not true, if it does not exists we cannot say anything
		assertEquals(true,result);

	}
	
	
	@Test
	public void testConstraintOnGetter_of_NullParameter() throws InvariantParseException {
		String expression = "parameter[0].getField().intValue() == 0";
		Object parameters[] = new Object[]{null};
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(true,result);

	}
	
	@Test
	public void testConstraintOnField_of_NullParameter() throws InvariantParseException {
		String expression = "parameter[0].field.intValue() == 0";
		Object parameters[] = new Object[]{null};
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(true,result);

	}
	
	@Test
	public void testConstraintOnGetter_of_NullReturnValue() throws InvariantParseException {
		String expression = "returnValue.getField().intValue() == 0";
		Object parameters[] = new Object[0];
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(true,result);

	}
	
	@Test
	public void testConstraintOnField_of_NullReturnValue() throws InvariantParseException {
		String expression = "returnValue.field.intValue() == 0";
		Object parameters[] = new Object[0];
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(true,result);

	}
	
	@Test
	public void testValidConstraintsOnNullReturnValue() throws InvariantParseException {
		String expression = "returnValue == null";
		Object parameters[] = new Object[0];
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(true,result);
	}
	
	@Test
	public void testNotValidConstraintsOnNullReturnValue() throws InvariantParseException {
		String expression = "returnValue != null";
		Object parameters[] = new Object[0];
		Object returnValue = null;

		boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
		assertEquals(false,result);

	}
	
	@Test
	public void testValidConstraintOnGetter_of_NonNullReturnValue() {
		String expression = "returnValue.getField().intValue() == 0";
		Object parameters[] = new Object[0];
		Object returnValue = new AClass();
		try {
			boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
			assertEquals(true,result);
		} catch (InvariantParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testNotValidConstraintOnGetter_of_NonNullReturnValue() {
		String expression = "returnValue.getField().intValue() == 5";
		Object parameters[] = new Object[0];
		Object returnValue = new AClass();
		try {
			boolean result = IoInvariantParser.evaluateExpression(expression, parameters, returnValue, null);
			assertEquals(false,result);
		} catch (InvariantParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
