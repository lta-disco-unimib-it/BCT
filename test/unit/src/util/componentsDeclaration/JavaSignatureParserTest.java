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
 * Tests derived with category partition
 * 
 * Package elements
 * 
 * 0	[SINGLE]
 * 1	[SINGLE]
 * >1
 * 
 * Classname len
 * 1	[SINGLE]
 * >1
 * 
 * InnerClass
 * Yes	[SINGLE]
 * No
 * 
 * Parameters
 * 0	[SINGLE]
 * 1	[SINGLE]
 * >1
 * 
 * Parameters 
 * primitive	[SINGLE]
 * object		[SINGLE]
 * 
 * Parameters lang
 * java syntax	
 * bytecode syntax
 * 
 * Return value
 * void
 * Object
 * 
 * 
 * PEls	CLen	IC	Pars	PLang	PType	RV	Test
 * 0	>1		N	>1		J					0
 * 1	>1		N	>1		J		Prim		1
 * 1	>1		N	>1		J		Obj			2
 * >1	>1		N	>1		J		-			3				
 * >1	1		N	>1		J					4
 * >1	>1		N	0		J					5
 * >1	>1		N	1		J					6
 * >1	>1		N	0		B					7
 * >1	>1		N	1		B					8
 * >1	>1		N	>1		B					9
 * >1	>1		Y	>1		B					10
 * 
 * 
 * @author Fabrizio Pastore
 *
 */
public class JavaSignatureParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * 
	 */
	@Test
	public void test1_SinglePackage_MethodWithParameters() {
		
		String packageName = "myPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(Object,Object)";

		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test2_SinglePackage_MethodWithComplexParameters() {
		
		String packageName = "myPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(java.lang.Object,Integer)";

		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test5_SubPackage_MethodWithoutParameters() {
		
		String packageName = "myPackage.sub";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "()";
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test6_SubPackage_MethodWithOneParameter() {
		
		String packageName = "myPackage.sub";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(java.lang.Object)";
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test3_SubPackage_MethodWithComplexParameters() {
		
		String packageName = "myPackage.subPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(java.lang.Object,Integer)";
		
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		
		
		
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
		
		
		
	}
	
	
	private void verify(String packageName, String className,
			String methodName, String parameters, 
			String returnSimple, String returnObject ) {
		
		
		
		String methodSignature = methodName+parameters;
		
		String canonicalClassName;
		if ( packageName.length() > 0 ){
		canonicalClassName = packageName+"."+className;
		} else {
			canonicalClassName = className;
		}
		
		String completeMethodSignature = canonicalClassName+"."+methodSignature;
		
		String completeMethodName = canonicalClassName+"."+methodName;
		
		String byteCodeMethodSignatureSimple = canonicalClassName+"."+methodName+"("+parameters+returnSimple+")";
		String byteCodeMethodSignatureObject = canonicalClassName+"."+methodName+"("+parameters+returnObject+")";
		
		assertEquals ( canonicalClassName, JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature) );
		
		JavaSignatureParser signatureParser = new JavaSignatureParser();
		assertEquals ( className, signatureParser.getClassNameFromCompleteMethodSignature(completeMethodSignature) );
		
		assertEquals( completeMethodName, signatureParser.getCompleteMethodNameFromCompleteMethodSignature(completeMethodSignature) );
		
		assertEquals( completeMethodSignature, signatureParser.getCompleteMethodSignatureFromBytecodeMethodSignature(byteCodeMethodSignatureSimple));
		assertEquals( completeMethodSignature, signatureParser.getCompleteMethodSignatureFromBytecodeMethodSignature(byteCodeMethodSignatureObject));
		
		assertEquals ( methodName, signatureParser.getMethodNameFromCompleteMethodSignature(completeMethodSignature) );
		
		assertEquals ( methodSignature, signatureParser.getMethodSignatureFromCompleteMethodSignature(completeMethodSignature) );
		
		assertEquals ( packageName, signatureParser.getPackageNameFromCanonicalClassName(canonicalClassName));
		
		assertEquals( packageName, signatureParser.getPackageNameFromCompleteMethodSignature(completeMethodSignature) );
		
		
	}

	@Test
	public void test7_SubPackage_BytecodeMethodWithoutParameters() {
		
		String packageName = "myPackage.subPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "()";
		
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	
	@Test
	public void test8_SubPackage_BytecodeMethodWithOneParameter() {
		
		String packageName = "myPackage.subPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "([Ljava.lang.Object;)";
		
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test9_SubPackage_BytecodeMethodWithComplexParameters() {
		
		String packageName = "myPackage.subPackage";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(Ljava.lang.Object;I[Ljava.lang.Object)";
		
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	
	@Test
	public void test4_SubPackage_ClassSingleName() {
		
		String packageName = "myPackage.subPackage";
		String className = "C";
		String methodName = "myMethod";
		String parameters = "(java.lang.Object,Integer)";
		
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test0_DefaultPackage_MethodWithComplexParameters() {
		
		String packageName = "";
		String className = "TheClass";
		String methodName = "myMethod";
		String parameters = "(java.lang.Object,Integer)";
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}
	
	@Test
	public void test10_SubPackage_InnerClass_MethodWithComplexParameters() {
		
		String packageName = "myPackage.subPackage";
		String className = "TheClass$Inner";
		String methodName = "myMethod";
		String parameters = "(Ljava.lang.Object;I)";
		
		
		String returnSimple = "V";
		String returnObject = "Ljava.lang.Object";
		
		verify(packageName,className,methodName,parameters,returnSimple,returnObject);
	}

}
