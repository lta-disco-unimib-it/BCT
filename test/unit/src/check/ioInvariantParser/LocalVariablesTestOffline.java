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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;

import testSupport.check.ioInvariantParser.MyClass;

public class LocalVariablesTestOffline extends LocalVariablesAbstract {

	public LocalVariablesTestOffline() {
		super(true);
	}
	
	@Test
	public void testPointerComparison() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("&p", Long.decode("0x806a569")) ;
		locals.put("&end", Long.decode("0x8068000")) ;
		
		
		boolean result;
		
		result = IoInvariantParser.evaluateExpression("&p <= &end", parameters, null, locals );
		assertFalse(result);
		
		result = IoInvariantParser.evaluateExpression("&p >= &end", parameters, null, locals );
		assertTrue(result);
	}
	
	@Test
	public void testMissingPrimitiveVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x", 6);

		boolean result = IoInvariantParser.evaluateExpression("a == 6", parameters, null, locals );
		assertTrue(result);
	}
	
	@Test
	public void testObjectVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x", new MyClass());

		boolean result = IoInvariantParser.evaluateExpression("x.intValue == 4", parameters, null, locals );
		assertTrue(result);
		
		//This test fails because x.intValue is not parsed as WORD . FIELD but as WORD
		//So the parser looks for a local named x.intValue
		result = IoInvariantParser.evaluateExpression("x.intValue == 5", parameters, null, locals );
		assertFalse(result);

	}
	
	
	@Test
	public void testMissingObjectVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x", new MyClass());

		boolean result = IoInvariantParser.evaluateExpression("a.intValue == 4", parameters, null, locals );
		assertTrue(result);
		

	}
	
	@Test
	public void testNameWithDottVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x.intValue", 4);

		boolean result;
		result= IoInvariantParser.evaluateExpression("x.intValue == 4", parameters, null, locals );
		assertTrue(result);
		
		//This test fails because x.intValue is not parsed as WORD . FIELD but as WORD
		//So the parser looks for a local named x.intValue
		result = IoInvariantParser.evaluateExpression("x.intValue == 5", parameters, null, locals );
		assertFalse(result);

	}
	
	
	@Test
	public void testMissingNameWithDottVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x.intValue", 4);

		boolean result = IoInvariantParser.evaluateExpression("a.intValue == 4", parameters, null, locals );
		assertTrue(result);
		

	}
	
	@Test
	public void testNotNull() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x.intValue", 4);
		locals.put("x.j", "!NULL");
		locals.put("y", 4);
		locals.put("nx.intValue", null);
		locals.put("ny", null);
		locals.put("pnodes[3]", null);

		assertTrue( IoInvariantParser.evaluateExpression("x.intValue != null", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("y != null", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("x != null", parameters, null, locals ) );
		
		
		assertFalse( IoInvariantParser.evaluateExpression("nx.intValue != null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("ny != null", parameters, null, locals ) );
		
		assertFalse( IoInvariantParser.evaluateExpression("x == null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("y == null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("x.intValue == null", parameters, null, locals ) );
		
		assertTrue( IoInvariantParser.evaluateExpression("x.j != null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("x.j == null", parameters, null, locals ) );
		
		assertFalse( IoInvariantParser.evaluateExpression("pnodes[3] != null", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("pnodes[3] == null", parameters, null, locals ) );

	}
	
	@Test
	public void testPrimitiveVariables() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x", 6);

		boolean result = IoInvariantParser.evaluateExpression("x == 6", parameters, null, locals );
		assertTrue(result);
		
		
		result = IoInvariantParser.evaluateExpression("x one of { 5, 6 }", parameters, null, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("x == 5", parameters, null, locals );
		assertFalse(result);
		
		result = IoInvariantParser.evaluateExpression("x one of { 5, 4 }", parameters, null, locals );
		assertFalse(result);
		
	}
	
	@Test
	public void testStringsNull() throws InvariantParseException{
		Object parameters[] = new Object[1];
		//	parameters[0] =	new MyClass();

		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("a", "AAA");
		locals.put("b", "BBB");
		locals.put("x", null);

		boolean result = IoInvariantParser.evaluateExpression("a < b", parameters, null, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("b > a", parameters, null, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("a < x", parameters, null, locals );
		assertTrue(result);
	
		result = IoInvariantParser.evaluateExpression("b > x", parameters, null, locals );
		assertTrue(result);
		
		
	}

	@Test
	public void testAbsentString() throws InvariantParseException{
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();


		boolean result = IoInvariantParser.evaluateExpression("a == \"NAME\"", parameters, null, locals );
		assertTrue(result);
		
		
		result = IoInvariantParser.evaluateExpression("a one of {\"\",\"ABC\"}", parameters, null, locals );
		assertTrue(result);
	}
	
	@Test
	public void testStarThis() throws InvariantParseException{
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("*this.counter", 0);
		
//		locals.put("x", null);

		boolean result = IoInvariantParser.evaluateExpression("*this.counter == 0", parameters, null, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("*this.counter == 1", parameters, null, locals );
		assertFalse(result);
	}
	
	@Ignore("FIXME: double check why is so hard to make it pass")
	@Test
	public void testStarStarThis() throws InvariantParseException{
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("**this.p.counter", 0);
		
//		locals.put("x", null);

		boolean result = IoInvariantParser.evaluateExpression("**this.p.counter == 0", parameters, null, locals );
		assertTrue(result);
		
		result = IoInvariantParser.evaluateExpression("**this.p.counter == 1", parameters, null, locals );
		assertFalse(result);
	}
	
	@Test
	public void testStringsEscaped() throws InvariantParseException{
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("a", "/home");
		locals.put("b", "\nmain");
//		locals.put("x", null);

		boolean result = IoInvariantParser.evaluateExpression("a > b", parameters, null, locals );
		assertTrue(result);
	}

	@Test
	public void testEscapedStrings_F_Bug() throws InvariantParseException {
		String model = "base one of { \"\001b\", \"BROKEN\", \"LINK\", \"Z\213f$\211\004$\213D$\004\302f\" }";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("base", "Z\213f$\211\004$\213D$\004\302f");
		
		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
		locals = new HashMap<String, Object>();
		locals.put("base", "Z\213f$\211\004$\213D$\004\302ft");
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);
		
		
		
		model = "base one of { \"\001b\", \"BROKEN\", \"LINK\", \"Z\213\\f$\211\004$\213D$\004\302\\f\" }";
		locals = new HashMap<String, Object>();
		locals.put("base", "Z\213\\f$\211\004$\213D$\004\302\\f" );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
	}
	
	@Test
	public void testDifferent() throws InvariantParseException {
		String model = "direction != 0";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("direction", Long.valueOf("3221221656") );
		
		
		
				
		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
		locals = new HashMap<String, Object>();
		locals.put("direction", new BigInteger("13835040360016904192") );
		
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
	
	}

	@Test
	public void testNaN() throws InvariantParseException{
		String model = "x >= 1.0";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("x", Double.NaN );

		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);


	}
	
	@Test
	public void testArrayField() throws InvariantParseException{
		String model = "nodes[0].value == 3";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("nodes[0].value", 5 );

		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);

		locals = new HashMap<String, Object>();
		locals.put("nodes[0].value", 3 );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);

	}
	
	@Test
	public void testArrayImplication() throws InvariantParseException{
		String model = "(nodes[0] != null)  ==>  (nodes[0].value == 3)";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("nodes[0].value", 5 );

		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);

		locals = new HashMap<String, Object>();
		locals.put("nodes[0].value", 3 );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);


		locals = new HashMap<String, Object>();
		locals.put("nodes[0]", null );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);

	}	
			
	@Test
	public void testImplication() throws InvariantParseException{
		String model = "(node != null)  ==>  (node.value == 3)";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("node", "!NULL" );
		locals.put("node.value", 5 );
		
		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);
		
		locals = new HashMap<String, Object>();
		locals.put("node", "!NULL" );
		locals.put("node.value", 3 );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);

		
		locals = new HashMap<String, Object>();
		locals.put("node", null );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
	}
	
	@Test
	public void testImplicationNoParent() throws InvariantParseException{
		String model = "(node != null)  ==>  (node.value == 3)";
		Object parameters[] = new Object[1];
		HashMap<String, Object> locals = new HashMap<String, Object>();
		locals.put("node.value", 5 );
		
		boolean result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertFalse(result);
		
		locals = new HashMap<String, Object>();
		locals.put("node.value", 3 );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);

		
		locals = new HashMap<String, Object>();
		locals.put("node", null );
		result = IoInvariantParser.evaluateExpression(model, parameters, null, locals );
		assertTrue(result);
		
	}

}
