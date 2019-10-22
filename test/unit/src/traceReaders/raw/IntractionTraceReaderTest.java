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
package traceReaders.raw;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;


/**
 * Trace len
 * default TD
 * 256	T0
 * 255	T1
 * 257	T2
 * 512	T3
 * 499	T4
 * 511	T5
 * 513	T6
 * 0	T7
 * 1	T8
 * 2	T9
 * 
 * 
 * Token len
 * no	T7
 * 0	T8
 * 1	T9
 * 2	T10
 * 254	T11
 * 255	T12
 * 256	T13
 * 510	T14
 * 511	T15
 * 512	T16
 * 512 + Meth	T17 
 * 513 + Meth	T18
 * 514 + Meth	T19
 * 
 * @author Fabrizio Pastore
 *
 */
public class IntractionTraceReaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test0() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/256.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test1() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/255.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method12()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	
	@Test
	public void test2() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/257.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1234()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test3() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/512.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1234()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}

	@Test
	public void test4() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/499.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}

	@Test
	public void test5() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/511.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test6() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/513.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method12345()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test7() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/0.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test8() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/1.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test9() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/2.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("A", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test10() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t10.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("AB", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	

	@Test
	public void test11() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t11.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 254; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test12() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t12.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 255; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test13() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t13.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 256; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test14() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t14.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;
		
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 510; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test15() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t15.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 511; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test16() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t16.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 512; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void test17() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t17.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 512; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals("METH", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}

	public void test18() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t18.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 513; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals("METH", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	public void test19() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/t19.itrace"));
		InteractionTraceReader itr = new InteractionTraceReader(256,fr);
		String token;

		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < 514; i++ ){
			sb.append("A");
		}
		
		token = itr.getNextToken();
		assertEquals(sb.toString(), token);
		
		token = itr.getNextToken();
		assertEquals("METH", token);
		
		token = itr.getNextToken();
		assertEquals("AMETHA", token);
		
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
	
	@Test
	public void testDefault() throws IOException {
		FileReader fr = new FileReader(TestArtifactsManager.getUnitTestFile("interactionTraceReader/512.itrace"));
		//Default constructor uses buffer size of 512
		InteractionTraceReader itr = new InteractionTraceReader(fr);
		String token;
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method123()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method5()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method6()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method7()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method8()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method9()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method2()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method3()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method4()B", token);
		
		token = itr.getNextToken();
		assertEquals("Method1234()B", token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
		
		token = itr.getNextToken();
		assertEquals(null, token);
	}
}
