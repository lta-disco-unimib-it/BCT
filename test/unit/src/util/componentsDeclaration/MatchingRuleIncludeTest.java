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
package util.componentsDeclaration;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for matching rule
 * 
 * We derive tests with a combinatorial approach, for every signature element we check case in which it is not matched, when RE is expressed with or without special chars
 * 
 * 
 * Method rule
 * precise [SINGLE]
 * wild char
 * 
 * Class rule
 * precise [SINGLE]
 * wild char
 * 
 * Package rule
 * precise [SINGLE]
 * wild char
 * 
 * Match package
 * Y
 * N
 * 
 * Match class
 * Y
 * N
 * 
 * Match meth
 * Y
 * N		
 * 
 * 
 * T1 	precise	wild	wild		ALL RESULTS CHECKED
 * T2	wild	precise	wild		ALL RESULTS CHECKED
 * T3	wild	wild	precise		ALL RESULTS CHECKED
 * T4	wild	wild 	wild		ALL RESULTS CHECKED
 * 			
 * @author Fabrizio Pastore
 *
 */
public class MatchingRuleIncludeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		MatchingRuleInclude rule = new MatchingRuleInclude("org.apache.catalina.core","C.*","m.*");
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core", "Clazz", "meth(()V)"));
		
		assertFalse(rule.acceptMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		
		assertTrue(rule.acceptClass("org.apache.catalina.core", "Clazz") );
		assertFalse(rule.rejectClass("org.apache.catalina.core", "Clazz") );
		
		assertFalse(rule.acceptClass("org.apache.catalina", "Clazz") );
		assertFalse(rule.rejectClass("org.apache.catalina", "Clazz") );
	}
	
	@Test
	public void test2() {
		MatchingRuleInclude rule = new MatchingRuleInclude("org.apache.catalina.core.*","Clazz","m.*");
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		
		assertFalse(rule.acceptMethod("org.apache.catalina.core.u", "AClazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "AClazz", "meth(()V)"));
		
		assertTrue(rule.acceptClass("org.apache.catalina.core", "Clazz") );
		assertFalse(rule.rejectClass("org.apache.catalina.core", "Clazz") );
		
		assertFalse(rule.acceptClass("org.apache.catalina.core.u", "AClazz") );
		assertFalse(rule.rejectClass("org.apache.catalina.core.u", "AClazz") );
	}
	
	@Test
	public void test3() {
		MatchingRuleInclude rule = new MatchingRuleInclude("org.apache.catalina.core.*","Clazz.*","meth\\(\\(\\)V\\)");
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		
		assertFalse(rule.acceptMethod("org.apache.catalina.core.u", "Clazz", "ameth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "Clazz", "ameth(()V)"));
		
		assertTrue(rule.acceptClass("org.apache.catalina.core", "Clazze") );
		assertFalse(rule.rejectClass("org.apache.catalina.core", "Clazze") );
		
		assertFalse(rule.acceptClass("org.apache.catalina.core.u", "Claz") );
		assertFalse(rule.rejectClass("org.apache.catalina.core.u", "Claz") );
	}
	
	@Test
	public void test4() {
		MatchingRuleInclude rule = new MatchingRuleInclude("org.apache.catalina.core.*","Clazz.*","met.*");
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core.u", "Clazz", "meth(()V)"));
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core", "Clazzes", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core", "Clazzes", "meth(()V)"));
		
		assertTrue(rule.acceptMethod("org.apache.catalina.core", "Clazzes", "method((II)V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core", "Clazzes", "method((II)V)"));
		
		
		assertFalse(rule.acceptMethod("org.apache.catalina.cor.u", "Clazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.cor.u", "Clazz", "meth(()V)"));
		
		assertFalse(rule.acceptMethod("org.apache.catalina.core", "AClazz", "meth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core", "AClazz", "meth(()V)"));
		
		assertFalse(rule.acceptMethod("org.apache.catalina.core", "Clazz", "ameth(()V)"));
		assertFalse(rule.rejectMethod("org.apache.catalina.core", "Clazz", "ameth(()V)"));
		
		
		assertTrue(rule.acceptClass("org.apache.catalina.core", "Clazze") );
		assertFalse(rule.rejectClass("org.apache.catalina.core", "Clazze") );
		
		assertFalse(rule.acceptClass("org.apache.catalina.cor", "Clazz") );
		assertFalse(rule.rejectClass("org.apache.catalina.cor", "Clazz") );
		
		assertFalse(rule.acceptClass("org.apache.catalina.core", "Clazs") );
		assertFalse(rule.rejectClass("org.apache.catalina.core", "Clazs") );
		
	}
	
}
