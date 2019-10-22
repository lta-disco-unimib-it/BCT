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
package dfmaker.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.IoTraceCreatorSimple;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import traceReaders.raw.FileIoTrace.FileLineIterator;
import dfmaker.core.ProgramPointHash;

public class ProgramPointHashExtractorTest {
	private static final String methodName = "a.B.m(int i)";
	private static final int iterations = 100000;
	private static final String tracePath = "test/unit/artifacts/dfmaker/utilities/ProgramPointHashExtractorTest/trace.dtrace";

	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetProgramPointsHashesFirstVoid() throws IOException, TraceException {
		
		File trace = new File(tracePath );
		IoTraceCreatorFirstVoid creator = new IoTraceCreatorFirstVoid(methodName,iterations);
		
		IoTrace iotrace = new FileIoTrace(methodName,trace);
		creator.createTrace(trace);
		
		int hashEnter = creator.getHashEnter();
		int hashExit = creator.getHashExit();
		
		System.out.println("Trace Created");
		
		BufferedReader br = new BufferedReader(new FileReader(trace));
		System.out.println("Line Iterator");
		FileLineIterator f = new FileLineIterator(br);
		long startTime = System.currentTimeMillis();
		while(f.hasNext()){
			f.next();
		}
		long endTime = System.currentTimeMillis();
		long lineIteratorTime = endTime-startTime;
		System.out.println("Line Iterator Finished in "+lineIteratorTime);
		
		startTime = System.currentTimeMillis();
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		long pphTime = endTime-startTime;
		
		assertTrue(pphTime<lineIteratorTime*2);
		
		assertEquals( iterations*2, ppHashes.size());
		
		Iterator<ProgramPointHash> it = ppHashes.iterator();
		int c=0;
		while( it.hasNext() ){
			ProgramPointHash pphEnter = it.next();
			assertEquals(hashEnter, pphEnter.getHash());
			assertEquals((long)1,pphEnter.getLength());
			
			ProgramPointHash pphExit = it.next();
			assertEquals(hashExit, pphExit.getHash());
			assertEquals("Iteration "+c,(long)4,pphExit.getLength());
			++c;
		}
	
		assertTrue( creator.deleteTrace(trace));
	}
	
	
	@Test
	public void testGetProgramPointsHashes() throws IOException, TraceException {
		
		File trace = new File(tracePath );
		IoTraceCreatorSimple creator = new IoTraceCreatorSimple(methodName,iterations);
		
		IoTrace iotrace = new FileIoTrace(methodName,trace);
		creator.createTrace(trace);
		
		int hashEnter = creator.getHashEnter();
		int hashExit = creator.getHashExit();
		
		System.out.println("Trace Created");
		
		BufferedReader br = new BufferedReader(new FileReader(trace));
		System.out.println("Line Iterator");
		FileLineIterator f = new FileLineIterator(br);
		long startTime = System.currentTimeMillis();
		while(f.hasNext()){
			f.next();
		}
		long endTime = System.currentTimeMillis();
		long lineIteratorTime = endTime-startTime;
		System.out.println("Line Iterator Finished in "+lineIteratorTime);
		
		startTime = System.currentTimeMillis();
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		long pphTime = endTime-startTime;
		
		assertTrue(pphTime<lineIteratorTime*2);
		
		assertEquals( iterations*2, ppHashes.size());
		
		Iterator<ProgramPointHash> it = ppHashes.iterator();
		int c=0;
		while( it.hasNext() ){
			ProgramPointHash pphEnter = it.next();
			assertEquals(hashEnter, pphEnter.getHash());
			assertEquals((long)4,pphEnter.getLength());
			
			ProgramPointHash pphExit = it.next();
			assertEquals(hashExit, pphExit.getHash());
			assertEquals("Iteration "+c,(long)7,pphExit.getLength());
			++c;
		}
	
		assertTrue( creator.deleteTrace(trace));
	}

	@Test
	public void testGetProgramPointsHashesNL() throws IOException, TraceException {
		
		File trace = new File(tracePath );
		IoTraceCreatorSimple creator = new IoTraceCreatorSimple(methodName,iterations);
		
		IoTrace iotrace = new FileIoTrace(methodName,trace);
		creator.setNewLineAtEOF(true);
		creator.createTrace(trace);
		
		int hashEnter = creator.getHashEnter();
		int hashExit = creator.getHashExit();
		
		System.out.println("Trace Created");
		
		BufferedReader br = new BufferedReader(new FileReader(trace));
		System.out.println("Line Iterator");
		FileLineIterator f = new FileLineIterator(br);
		long startTime = System.currentTimeMillis();
		while(f.hasNext()){
			f.next();
		}
		long endTime = System.currentTimeMillis();
		long lineIteratorTime = endTime-startTime;
		System.out.println("Line Iterator Finished in "+lineIteratorTime);
		
		startTime = System.currentTimeMillis();
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		long pphTime = endTime-startTime;
		
		assertTrue(pphTime<lineIteratorTime*2);
		
		assertEquals( iterations*2, ppHashes.size());
		
		Iterator<ProgramPointHash> it = ppHashes.iterator();
		int c=0;
		while( it.hasNext() ){
			ProgramPointHash pphEnter = it.next();
			assertEquals(hashEnter, pphEnter.getHash());
			assertEquals((long)4,pphEnter.getLength());
			
			ProgramPointHash pphExit = it.next();
			assertEquals(hashExit, pphExit.getHash());
			assertEquals("Iteration "+c,(long)7,pphExit.getLength());
			++c;
		}
	
		assertTrue( creator.deleteTrace(trace));
	}	
		


}
