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

import grammarInference.Record.Trace;
import grammarInference.Record.kbhParser;

//import it.unimib.disco.lta.alfa.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import traceReaders.normalized.NormalizedInteractionTrace;
import traceReaders.normalized.NormalizedInteractionTraceHandlerFile;
import traceReaders.normalized.NormalizedInteractionTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.NormalizedTraceHandlerFile;
import traceReaders.raw.Token;
import util.FileUtil;

public class Bug155 {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1InteractionTracesGeneration() throws NormalizedTraceHandlerException, FileNotFoundException {
		File interactionTracesFolder = TestArtifactsManager.getBugFile("155/test1InteractionTracesGenerationDir");
		
		FileUtil.deleteRecursively(interactionTracesFolder);
		assertTrue(interactionTracesFolder.mkdir());
		
		NormalizedInteractionTraceHandlerFile interactionTracesHandler = new NormalizedInteractionTraceHandlerFile(interactionTracesFolder);
	
		//empty trace
		String method0 = "aPack.AClass.aMethod0()B";
		String threadId0 = "0";
		List<Token> trace0 = new ArrayList<Token>();
		
		
		interactionTracesHandler.addInteractionTrace(method0, trace0, threadId0);

		//
		//single trace for a method
		//trace with one element
		String method1 = "aPack.AClass.aMethod1()B";
		String threadId1 = "11";
		List<Token> trace1 = new ArrayList<Token>();
		trace1.add(new Token("p.Class1.meth()B"));
		
		interactionTracesHandler.addInteractionTrace(method1, trace1, threadId1);
		
		//
		//single trace for a method
		//trace with more than one elements
		String method2 = "aPack.AClass.aMethod2()B";
		String threadId2 = "3";
		List<Token> trace2 = new ArrayList<Token>();
		trace2.add(new Token("p.Class2.meth0()B"));
		trace2.add(new Token("p.Class2.meth1()B"));
		trace2.add(new Token("p.Class2.meth2()B"));
		
		interactionTracesHandler.addInteractionTrace(method2, trace2, threadId2);
		
		//
		//many traces for a method
		//trace with more than one element
		String method3 = "aPack.AClass.aMethod3()B";
		String threadId3_1 = "3";
		List<Token> trace3_1 = new ArrayList<Token>();
		trace3_1.add(new Token("p.Class31.meth1()B"));
		trace3_1.add(new Token("p.Class31.meth2()B"));
		trace3_1.add(new Token("p.Class31.meth3()B"));
		
		interactionTracesHandler.addInteractionTrace(method3, trace3_1, threadId3_1);
		
		String threadId3_2 = "3";
		List<Token> trace3_2 = new ArrayList<Token>();
		trace3_2.add(new Token("p.Class32.meth1()B"));
		
		
		interactionTracesHandler.addInteractionTrace(method3, trace3_2, threadId3_2);
		
		String threadId3_3 = "34";
		List<Token> trace3_3 = new ArrayList<Token>();
		trace3_3.add(new Token("p.Class33.meth1()B"));
		trace3_3.add(new Token("p.Class33.meth1()B"));
		
		interactionTracesHandler.addInteractionTrace(method3, trace3_3, threadId3_3);
		
		
		//
		//Test if working
		//
		NormalizedInteractionTraceHandlerFile interactionTracesHandlerReader = new NormalizedInteractionTraceHandlerFile(interactionTracesFolder);
		NormalizedInteractionTraceIterator tracesIt = interactionTracesHandlerReader.getInteractionTracesIterator();
		
		
		HashMap<String,NormalizedInteractionTrace> traces = new HashMap<String, NormalizedInteractionTrace>();
		while ( tracesIt.hasNext() ){
		
			NormalizedInteractionTrace trace = tracesIt.next();
			
			String method = trace.getMethodName();
			traces.put(method, trace);
			
		}
		
		assertEquals(4,traces.size());
		
		NormalizedInteractionTrace trace;
		
		trace = traces.get(method0);
		assertEquals( method0, trace.getMethodName());
		checkTrace(trace,new List[]{trace0});
		
		trace = traces.get(method1);
		assertEquals( method1, trace.getMethodName());
		checkTrace(trace,new List[]{trace1});
		
		trace = traces.get(method2);
		assertEquals( method2, trace.getMethodName());
		checkTrace(trace,new List[]{trace2});
		
		trace = traces.get(method3);
		assertEquals( method3, trace.getMethodName());
		checkTrace(trace,new List[]{trace3_1,trace3_2,trace3_3});
		
	}

	private void checkTrace(NormalizedInteractionTrace trace,
			List<Token>[] lists) throws FileNotFoundException {
		kbhParser parser = new kbhParser(trace.getTraceFile().getAbsolutePath());
		
		Iterator<Trace> tit = parser.getTraceIterator();
		int pos = 0;
		
		while ( tit.hasNext() ){
			pos++;
			
			Trace ktrace = tit.next();
			assertTrue( "Trace has more or "+pos+" elements, while "+lists.length+" are expected.",pos <= lists.length );
			
			if ( pos > lists.length ){
				fail("More elements in written trace than expected");
			}
		
			List<Token> ktokens = lists[pos-1];
			
			assertEquals(ktokens.size(), ktrace.getLength());
			
			for ( int i = 0; i <  ktokens.size(); ++i ){
				assertEquals(ktokens.get(i).getMethodSignature(), ktrace.getSymbol(i));
			}
			
			
		}
	}
}
