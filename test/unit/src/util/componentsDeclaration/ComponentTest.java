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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class defines the test cases for the Component class
 * 
 * 
 * AcceptMethodSignature
 * 
 * Test cases are derived considering the following categories 
 * 
 * Package elements
 * 0	[SINGLE][Property NoPackage]
 * 1	[SINGLE]
 * >1
 * 
 * Rule matching
 * all match	
 * package not match
 * class not match
 * methodName not match
 * methodPar not match [IF EM|REMP]
 * 
 * Rule types
 * all exact matching (EM) [P EM]
 * package generic, class generic, method name generic (RE) [P RE]
 * package generic, class generic, method name precise, method parameters generic (REMP) [P REMP]
 * 
 * InnerClass
 * yes	[SINGLE]
 * no	
 * 
 * Signature
 * aspects
 * probe (java bytecode)
 * 
 * Parameters
 * 0	
 * 1
 * >1
 * 
 * 
 * <table>
 * <tr><td>Package</td><td>Rule type</td><td>Inner Class</td><td>Signature</td><td>Parameters</td><td>Test</td></tr>
 * <tr><td>0</td><td>EM</td><td>No</td><td>Probe</td><td>>1</td><td>0</td><</tr>
 * <tr><td>1</td><td>EM</td><td>No</td><td>Probe</td><td>>1</td><td>1</td></tr>
 * <tr><td>>1</td><td>EM</td><td>No</td><td>Probe</td><td>0</td><td>2</td></tr>
 * <tr><td>>1</td><td>EM</td><td>No</td><td>Probe</td><td>1</td><td>3</td></tr>
 * <tr><td>>1</td><td>EM</td><td>No</td><td>Probe</td><td>>1</td><td>4</td></tr>
 * <tr><td>>1</td><td>EM</td><td>No</td><td>Aspects</td><td>>1</td><td>5</td></tr>
 * <tr><td>>1</td><td>EM</td><td>Yes</td><td>Probe</td><td>>1</td><td>6</td></tr>
 * <tr><td>0</td><td>RE</td><td>No</td><td>Probe</td><td>>1</td><td>7</td></tr>
 * <tr><td>1</td><td>RE</td><td>No</td><td>Probe</td><td>>1</td><td>8</td></tr>
 * <tr><td>>1</td><td>REMP</td><td>No</td><td>Probe</td><td>0</td><td>9</td></tr>
 * <tr><td>>1</td><td>REMP</td><td>No</td><td>Probe</td><td>1</td><td>10</td></tr>
 * <tr><td>>1</td><td>REMP</td><td>No</td><td>Probe</td><td>>1</td><td>11</td></tr>
 * <tr><td>>1</td><td>RE</td><td>No</td><td>Aspects</td><td>0</td><td>12</td></tr>
 * <tr><td>>1</td><td>RE</td><td>Yes</td><td>Aspects</td><td>>1</td><td>13</td></tr>
 * <tr><td>>1</td><td>RE</td><td>No</td><td>Probe</td><td>>1</td><td>14</td></tr>
 * </table>
 * 
 * 
 * 
 * AcceptBytecodeMethodSignature
 * 
 * Tests that bytecode signatures are corrrectly managed
 * 
 * At this time components do not permit to distinguish method considering their return type
 * We derived tests considering the following categories
 * 
 * Parameters
 * 0	
 * 1
 * >1
 * 
 * Return value
 * V		
 * Object
 * 
 * Parameter	Return	Test
 * 0			Object	B1
 * 1			Object	B2
 * >1			Object	B3
 * 0			V		B4
 * 1			V		B5
 * >1			V		B6
 * 
 * @author Fabrizio Pastore
 *
 */
public class ComponentTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test0() {
		
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("aMyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("apack.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("MyClass.amyMethod(I;[java.lang.Object)"));
		
		assertTrue( c.acceptPackage("") );
		
		assertFalse( c.acceptPackage("apack") );
	}

	@Test
	public void test1() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.aMyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMeth(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod()"));
		
		assertTrue( c.acceptPackage("myPackage") );
		
		assertFalse( c.acceptPackage("apack") );
		
		assertFalse( c.acceptPackage("myPackageA") );
		
		assertFalse( c.acceptPackage("myPackage.sub") );
	}
	
	@Test
	public void test2() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPack.sub.MyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMeth()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod(L)"));
		
		
		assertTrue( c.acceptPackage("myPackage.sub") );
		
		assertFalse( c.acceptPackage("apack") );
		
		assertFalse( c.acceptPackage("myPackageA") );
		
		assertFalse( c.acceptPackage("myPackage") );
	}
	
	@Test
	public void test3() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.sub.MyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMeth(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod([java.lang.Object)"));
	}
	
	@Test
	public void test4() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMeth(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod()"));
	}
	
	@Test
	public void test5() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(int,Java.lang.Object\\[\\]\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(int,Java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPack.sub.MyClass.myMethod(int,Java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod(int,Java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMeth(int,Java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod()"));
	}
	
	@Test
	public void test6() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass\\$Inner","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.sub.MyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.InnermyMeth(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass$Inner.myMethod()"));
	}

	
	
	//Tests with regular expressions in rule
	//
	
	@Test
	public void test7() {
		
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude(".*","My.*","my.*");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("aMyClass.myMethod(I;[java.lang.Object)"));
		
		//not to consider
		//assertFalse(c.acceptMethodSignature("apack.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("MyClass.amyMethod(I;[java.lang.Object)"));
		
		assertTrue( c.acceptPackage("") );
		
		assertTrue( c.acceptPackage("aPack") );
	}

	@Test
	public void test8() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myP.*","MyC.*","myM.*");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("amyPack.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.aMyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myAMeth(I;[java.lang.Object)"));
		
		assertFalse( c.acceptPackage("") );
		
		assertTrue( c.acceptPackage("myPacka") );
		
		assertFalse( c.acceptPackage("my.Packa") );
		
		assertFalse( c.acceptPackage("myAP") );
	}
	
	@Test
	public void test9() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage\\.s.*","MyC.*","myMethod\\(.*\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.util.MyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myAMeth()"));
		
		//this is an error
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod"));
		
		assertFalse( c.acceptPackage("") );
		
		assertTrue( c.acceptPackage("myPackage.sub") );
		
		assertTrue( c.acceptPackage("myPackage.s") );
		
		assertFalse( c.acceptPackage("myPackage.") );
		
		assertFalse( c.acceptPackage("myPackage.a") );
	}
	
	@Test
	public void test10() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage\\.s.*","MyC.*","myMethod\\(java.lang\\..*\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.MyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.aMyClass.myMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.amyMethod(java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(java.language.Item)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod()"));
		
	}
	
	@Test
	public void test11() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage","MyClass","myMethod\\(I;\\[java.lang\\..*\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.MyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.aMyClass.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMeth(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.sub.MyClass.myMethod(java.language.Item)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod()"));
	}
	
	@Test
	public void test12() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage","MyClass","myMethod\\(int,java.lang\\..*\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.MyClass.myMethod(int,java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPack.MyClass.myMethod(int,java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.aMyClass.myMethod(int,Java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMeth(int,java.lang.Object[])"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.myMethod(int,java.languge.Object[])"));
	}
	
	@Test
	public void test13() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage","MyClass\\$Inner","myMethod\\(I;\\[java.lang\\..*\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptMethodSignature("myPackage.MyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPack.MyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.aMyClass$Inner.myMethod(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass.InnermyMeth(I;[java.lang.Object)"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass$Inner.myMethod()"));
		
		assertFalse(c.acceptMethodSignature("myPackage.MyClass$Inner.myMethod(I;[java.language.Item)"));
	}


	
	//Bytecode signature tests
	//
	
	@Test
	public void testB0() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPack.sub.MyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.MyClass.myMethod((L)Ljava.lang.Object)"));
	}
	
	@Test
	public void testB1() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPack.sub.MyClass.myMethod((java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod((java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth((java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(([java.lang.Object)Ljava.lang.Object)"));
	}
	
	@Test
	public void testB2() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((I;[java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.MyClass.myMethod((I;[java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod((I;[java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth((I;[java.lang.Object)Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(()Ljava.lang.Object)"));
	}
	
	@Test
	public void testB3() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPack.sub.MyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth(()Ljava.lang.Object)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.MyClass.myMethod((L)Ljava.lang.Object)"));
	}
	
	@Test
	public void testB4() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPack.sub.MyClass.myMethod((java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod((java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth((java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(([java.lang.Object)V)"));
	}
	
	@Test
	public void testB5() {
		Component c = new Component("testComponent");
		
		List<MatchingRule> mrList = new ArrayList<MatchingRule>();
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		mrList.add(mr);
		
		c.addRule(mr);
		
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.MyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(()V)"));
	}
	
	@Test
	public void testMultiRules() {
		Component c = new Component("testComponent");
		
		
		MatchingRule mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMethod\\(I;\\[java.lang.Object\\)");
		c.addRule(mr);
		mr = new MatchingRuleExclude("myPackage.sub","MyClass","myMeth.*");
		c.addRule(mr);
		mr = new MatchingRuleInclude("myPackage.sub","MyClass","myMet.*");
		c.addRule(mr);
		
		
		
		
		//First
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.MyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.aMyClass.myMethod((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeth((I;[java.lang.Object)V)"));
		
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod(()V)"));
		
		
		//second
		assertFalse(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMethod((II)V)"));
		
		//third
		assertTrue(c.acceptBytecodeMethodSignature("myPackage.sub.MyClass.myMeta(()V)"));
	}
	
}
