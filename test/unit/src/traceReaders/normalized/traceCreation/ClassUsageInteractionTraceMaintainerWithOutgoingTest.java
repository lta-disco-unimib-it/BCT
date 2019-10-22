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
package traceReaders.normalized.traceCreation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tools.NormalizedInteractionTraceHandlerMemory;
import traceReaders.metaData.ExecutionTokenMetaData;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.traceCreation.ClassUsageInteractionTraceMaintainer;
import traceReaders.raw.Token;
import util.componentsDeclaration.ComponentsDefinitionException;
import util.componentsDeclaration.JavaSignatureParser;

@Ignore("TODO: Class under test not used, no time to repair test/code")
public class ClassUsageInteractionTraceMaintainerWithOutgoingTest {
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the sequence
	 * 
	 * A.m
	 * 
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testSingleCall() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		
		String firstObjectId = "1";
		ExecutionTokenMetaData md;
		Token t;
		String method1 = "myPack.A.m()"; 
		
		t = new Token(method1+"B");
		
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(firstObjectId);
		md.setTimestamp(0);
		
		t.setTokenMetaData(md);
		
		
		tm.programPointBegin(null, t);
		
		t = new Token(method1+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(firstObjectId);
		md.setTimestamp(0);
		t.setTokenMetaData(md);
		
		tm.programPointEnd(null, t);
		
		tm.analysisEnd();
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(method1);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		assertEquals(1, traces.size());
		
		
		
	}
	
	
	/**
	 * Test the sequence
	 * 
	 * A.m	(a1)
	 * 	B.b	(b1)
	 * 	B.c (b1)
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testMultiCall1() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.m()";
		String bB = "myPack.B.b()";
		String cC = "myPack.C.c()";
		String cD = "myPack.C.d()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedA1);
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(bC);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		

		
	}
	
	/**
	 * Test the sequence
	 * 
	 * A.m	(a1)
	 * 	B.b	(b1)
	 * 		C.c	(c1)
	 * 		C.d	(c1)
	 * 	B.c (b1)
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testMultiCall2() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.m()";
		String bB = "myPack.B.b()";
		String cC = "myPack.C.c()";
		String cD = "myPack.C.d()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		

		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedA1);
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		expectedB1.add(bC);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class C
		//
		
		List<String> expectedC1 = new ArrayList<String>();
		expectedC1.add(cC);
		expectedC1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedC1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(cC);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
	}
	
	
	/**
	 * Test the sequence
	 * 
	 * A.<init>	(a1)
	 * 	B.<init> (b1)
	 * 		C.<init> (c1)
	 * 		C.d	(c1)
	 * 	B.c (b1)
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testConstructorCall() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.<init>()";
		String bB = "myPack.B.<init>()";
		String cC = "myPack.C.<init>()";
		String cD = "myPack.C.d()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		//md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		//md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		//md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		

		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedA1);
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		expectedB1.add(bC);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class C
		//
		
		List<String> expectedC1 = new ArrayList<String>();
		expectedC1.add(cC);
		expectedC1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedC1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(cC);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
	}

	
	
	/**
	 * Test the sequence
	 * 
	 * A.m	(a1)
	 * 	B.b	(b1)
	 * 		C.c	(c1)
	 * 		C.d	(c1)
	 * 	B.c (b1)
	 * A.m	(a1)
	 *	B.b	(b1)
	 * 		C.c	(c1)
	 * 		C.d	(c1)
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testMultiCall3() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.m()";
		String bB = "myPack.B.b()";
		String cC = "myPack.C.c()";
		String cD = "myPack.C.d()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		expectedA1.add(aM);
		expectedA1.add(bB);
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedA1);
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		expectedB1.add(bC);
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class C
		//
		
		List<String> expectedC1 = new ArrayList<String>();
		expectedC1.add(cC);
		expectedC1.add(cD);
		expectedC1.add(cC);
		expectedC1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedC1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(cC);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
	}
	
	
	
	/**
	 * Test the sequence
	 * 
	 * A.m	(a1)
	 * 	A.m	(a2)
	 * 		B.b	(b2)
	 * 			C.c	(c2)
	 * 			C.d	(c2)
	 * 		B.c (b2)
	 * 	A.m	(a2)
	 *		B.b	(b2)
	 * 			C.c	(c2)
	 * 			C.d	(c2)
	 * 	B.b	(b1)
	 * 		C.c	(c1)
	 * 		C.d	(c1)
	 * 	B.c (b1)
	 * A.m	(a1)
	 *	B.b	(b1)
	 * 		C.c	(c1)
	 * 		C.d	(c1)
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testMultiCallNested() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.m()";
		String bB = "myPack.B.b()";
		String cC = "myPack.C.c()";
		String cD = "myPack.C.d()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		String idA2 = "a2";
		String idB2 = "b2";
		String idC2 = "c2";	
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		//Internal call
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA2);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		//End internal call
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(cD+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(cD+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
				
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(aM);
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		expectedA1.add(aM);
		expectedA1.add(bB);
		
		expectedClass.add(expectedA1);
		
		expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		expectedA1.add(aM);
		expectedA1.add(bB);
		
		expectedClass.add(expectedA1);
		
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		expectedB1.add(bC);
		expectedB1.add(bB);
		expectedB1.add(cC);
		expectedB1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		
		//
		//Class C
		//
		
		List<String> expectedC1 = new ArrayList<String>();
		expectedC1.add(cC);
		expectedC1.add(cD);
		expectedC1.add(cC);
		expectedC1.add(cD);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedC1);
		expectedClass.add(expectedC1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(cC);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
	}
	
	
	/**
	 * Test the sequence
	 * 
	 * A.m	(a1) ts=40
	 * 	B.b	(b1) ts=50
	 * 	B.c (b1) ts=60
	 * C.m	(c1) ts=1 
	 * 	B.b	(b1) ts=2
	 * 	B.c (b1) ts=3
	 *  B.c (b1) ts=4
	 * @throws ComponentsDefinitionException
	 */
	@Test
	public void testMultiCallOrder() throws ComponentsDefinitionException {
		NormalizedInteractionTraceHandlerMemory th = new NormalizedInteractionTraceHandlerMemory();
		ClassUsageInteractionTraceMaintainer tm = new ClassUsageInteractionTraceMaintainerWithOutgoing();
		
		tm.init(null, th);
		long ts = 0;
		
		ExecutionTokenMetaData md;
		Token t;
		String aM = "myPack.A.m()";
		String bB = "myPack.B.b()";
		String cM = "myPack.C.m()";
		String bC = "myPack.B.c()";
		
		String idA1 = "a1";
		String idB1 = "b1";
		String idC1 = "c1";
		
		
		//This first sequence happened later
		ts = 100;
		
		t = new Token(aM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(aM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idA1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		//This sequence happened before
		
		ts = 0;
		
		t = new Token(cM+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bB+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		
		t = new Token(bB+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		t = new Token(bC+"B");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointBegin(null, t);
		
		t = new Token(bC+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idB1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		t = new Token(cM+"E");
		md = new ExecutionTokenMetaData();
		md.setCalledObjectId(idC1);
		md.setTimestamp(ts++);
		t.setTokenMetaData(md);
		tm.programPointEnd(null, t);
		
		
		
		
		tm.analysisEnd();
		
		//
		//Class A
		//
		
		List<String> expectedA1 = new ArrayList<String>();
		expectedA1.add(aM);
		expectedA1.add(bB);
		expectedA1.add(bC);
		
		ArrayList<List<String>> expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedA1);
		
		String clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(aM);
		List<List<Token>> traces = th.getTracesForMethod(clazz);
		
		List<List<String>> actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		//
		//Class C
		//
		
		List<String> expectedC1 = new ArrayList<String>();
		expectedC1.add(cM);
		expectedC1.add(bB);
		expectedC1.add(bC);
		expectedC1.add(bC);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedC1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(cM);
		traces = th.getTracesForMethod(clazz);
		
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		//
		//Class B
		//
		
		List<String> expectedB1 = new ArrayList<String>();
		expectedB1.add(bB);
		expectedB1.add(bC);
		expectedB1.add(bC);
		expectedB1.add(bB);
		expectedB1.add(bC);
		
		expectedClass = new ArrayList<List<String>>();
		expectedClass.add(expectedB1);
		
		clazz = JavaSignatureParser.getCanonicalClassNameFromCompleteMethodSignature(bB);
		traces = th.getTracesForMethod(clazz);
		actualClass = getMethodLists(traces);
		
		assertEquals(expectedClass, actualClass);
		
		

		
	}
	

	private List<List<String>> getMethodLists(List<List<Token>> traces) {
		List<List<String>> result = new ArrayList<List<String>>();
		
		Collections.sort(traces,new Comparator<List<Token>>(){

			public int compare(List<Token> o1, List<Token> o2) {
				if ( o1.size() == 0 ){
					return -1;
				}
				if ( o2.size() == 0 ){
					return +1;
				}
				return (int) (o1.get(0).getTokenMetaData().getTimestamp() - o2.get(0).getTokenMetaData().getTimestamp()); 
			}
			
		});
		
		for ( List<Token> trace : traces ){
			List<String> methodsList = new ArrayList<String>();
			for ( Token t : trace ){
				methodsList.add(t.getMethodSignature());
			}
			result.add(methodsList);
		}
		
		return result;
	}

}
