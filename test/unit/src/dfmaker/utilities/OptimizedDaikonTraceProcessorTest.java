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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testSupport.IoTraceCreatorSimple;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import traceReaders.raw.IoTrace.LineIterator;
import dfmaker.core.OptimizedDaikonTraceProcessor;
import dfmaker.core.ProgramPointHash;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import dfmaker.core.OptimizedDaikonTraceProcessor.OptimizedDTraceListener;

@Ignore("FIXME")
public class OptimizedDaikonTraceProcessorTest {

	public static class OptimizedListenerStub implements OptimizedDTraceListener{
		private HashSet<Integer> seenEntry = new HashSet<Integer>();
		private HashSet<Integer> seenExit = new HashSet<Integer>();
		private boolean added;
		private int varAddedEnter=0;
		private int varAddedExit=0;
		
		
		private boolean mustAddEnter=false;
		private boolean mustAddExit=false;
		
		private int expectedVarEnter;
		private int expectedVarExit;
		private boolean expectedEnter;
		private boolean traceEnded=false;
		private int state=-1;
		private int processedEntryPoints=0;
		private int processedExitPoints=0;
		
		public OptimizedListenerStub(int expectedVarEnter, int expectedVarExit) {
			this.expectedVarEnter = expectedVarEnter;
			this.expectedVarExit = expectedVarExit;
		}

		public boolean entryPoint(int hashcode) throws DTraceListenerException {
			state=0;
			++processedEntryPoints;
			expectedEnter=true;
			if ( mustAddEnter == true && varAddedEnter < expectedVarEnter){
				fail("Not all variables have been added : "+varAddedEnter+"/"+expectedVarEnter);
			}
			
			if ( seenEntry.size() > 0 && ( ! seenEntry.contains(hashcode) ) ){
				fail("Seen different hashes");
			}
			
			varAddedEnter=0;
			if ( ! seenEntry.contains(hashcode) ){
				seenEntry.add(hashcode);
				mustAddEnter = true;
				return false;
			}
			mustAddEnter=false;
			return true;
		}

		public boolean exitPoint(int hashcode) throws DTraceListenerException {
			++processedExitPoints;
			state=0;
			expectedEnter=false;
			added=false;
			
			if ( mustAddExit == true && varAddedExit < expectedVarExit){
				fail("Added only "+varAddedExit+" expecting "+expectedVarExit);
			}
			
			if ( seenExit.size() > 0 && ( ! seenExit.contains(hashcode) ) ){
				fail("Seen different hashes");
			}
			
			varAddedExit=0;
			if ( ! seenExit.contains(hashcode) ){
				seenExit.add(hashcode);
				mustAddExit = true;
				return false;
			}
			mustAddExit=false;
			return true;
		}

		public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
			if ( state != 0 )
				fail("Unexpected entry call, must call entry hash");
			state = 1;
			if ( ! expectedEnter )
				fail("Unexpected entry call");
			
			if ( ! mustAddEnter )
				fail("Unexpected add");
			assertTrue(line.endsWith(":::ENTER"));
		}

		public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
			if ( state != 0 )
				fail("Unexpected entry call, must call entry hash");
			state = 1;
			if ( expectedEnter )
				fail("Unexpected exit call");
			if ( ! mustAddEnter )
				fail("Unexpected add");
			assertTrue(line.endsWith(":::EXIT1"));
		}

		public void newProgramVar(String varName, String varValue, String varModifier) throws DTraceListenerException {
			if ( expectedEnter ){
			if ( varAddedEnter == 0 && state != 1 )
				fail("Unexpected newProgramVar call");
			if ( varAddedEnter > 0 && state != 2 )
				fail("Unexpected newProgramVar call");
			} else {
				if ( varAddedExit == 0 && state != 1 )
					fail("Unexpected newProgramVar call");
				if ( varAddedExit > 0 && state != 2 )
					fail("Unexpected newProgramVar call");
			}
			
			state = 2;
			if ( expectedEnter ){
				++varAddedEnter;
			} else {
				++varAddedExit;
			}
		}

		public void traceEnd() {
			traceEnded=true;
		}

		public boolean isAdded() {
			return added;
		}

		public HashSet<Integer> getSeenEntry() {
			return seenEntry;
		}

		public HashSet<Integer> getSeenExit() {
			return seenExit;
		}


		public boolean isTraceEnded() {
			return traceEnded;
		}

		public boolean isExpectedEnter() {
			return expectedEnter;
		}

		public int getExpectedVarEnter() {
			return expectedVarEnter;
		}

		public int getExpectedVarExit() {
			return expectedVarExit;
		}

		public boolean isMustAddEnter() {
			return mustAddEnter;
		}

		public boolean isMustAddExit() {
			return mustAddExit;
		}

		public int getProcessedEntryPoints() {
			return processedEntryPoints;
		}

		public int getProcessedExitPoints() {
			return processedExitPoints;
		}

		public int getState() {
			return state;
		}

		public int getVarAddedEnter() {
			return varAddedEnter;
		}

		public int getVarAddedExit() {
			return varAddedExit;
		}

		@Override
		public void genericProgramPoint(long beginOffset, String line)
				throws DTraceListenerException {
			// TODO Auto-generated method stub
			
		}
		
	}

	private static final String tracePath = "test/unit/artifacts/dfmaker/utilities/OptimizedDaikonTraceProcessorTest/trace.dtrace";;
	private static final String methodName = "a.B.m(int i)";
	private static final int iterations = 100000;
	private int expectedVarExit=2;
	private int expectedVarEnter=1;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testProcessSimmectricTraceSingle() throws TraceException, IOException, DTraceListenerException {
		
		OptimizedListenerStub listener = new OptimizedListenerStub(1,1); 
		File traceFile = new File(tracePath );
		
		IoTraceCreatorSimmetric creator = new IoTraceCreatorSimmetric(methodName,1);
		creator.createSimpleTrace(traceFile);
		IoTrace iotrace = new FileIoTrace(methodName,traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
		
		assertTrue( listener.isTraceEnded() );
	
		assertEquals(1, listener.getProcessedEntryPoints());
		
		assertEquals(1, listener.getProcessedExitPoints());
		
		assertEquals(1,listener.getSeenEntry().size());
		Iterator<Integer> hashIt = listener.getSeenEntry().iterator();
		int hash = hashIt.next();
		
		assertEquals(creator.getHashEnter(),hash);
		
		assertEquals(1,listener.getSeenExit().size());
		Iterator<Integer> hashItExit = listener.getSeenExit().iterator();
		int hashExit = hashItExit.next();
		
		assertEquals(creator.getHashExit(),hashExit);
		
		assertTrue(traceFile.delete());
	}
	
	
	@Test
	public void testProcessSimmectricTrace() throws TraceException, IOException, DTraceListenerException {
		
		OptimizedListenerStub listener = new OptimizedListenerStub(1,1); 
		File traceFile = new File(tracePath );
		
		IoTraceCreatorSimmetric creator = new IoTraceCreatorSimmetric(methodName,iterations);
		creator.createSimpleTrace(traceFile);
		IoTrace iotrace = new FileIoTrace(methodName,traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
		
		assertTrue( listener.isTraceEnded() );
	
		assertEquals(iterations, listener.getProcessedEntryPoints());
		
		assertEquals(iterations, listener.getProcessedExitPoints());
		
		assertEquals(1,listener.getSeenEntry().size());
		Iterator<Integer> hashIt = listener.getSeenEntry().iterator();
		int hash = hashIt.next();
		
		assertEquals(creator.getHashEnter(),hash);
		
		assertEquals(1,listener.getSeenExit().size());
		Iterator<Integer> hashItExit = listener.getSeenExit().iterator();
		int hashExit = hashItExit.next();
		
		assertEquals(creator.getHashExit(),hashExit);
		
		assertTrue(traceFile.delete());
	}
	
	/**
	 * Test if it is able to process a trace that ends with a void line
	 * 
	 * @throws TraceException
	 * @throws IOException
	 * @throws DTraceListenerException
	 */
	@Test
	public void testProcessSimmectricTraceNL() throws TraceException, IOException, DTraceListenerException {
		
		OptimizedListenerStub listener = new OptimizedListenerStub(1,1); 
		File traceFile = new File(tracePath );
		
		IoTraceCreatorSimmetric creator = new IoTraceCreatorSimmetric(methodName,iterations);
		creator.setNewLineAtEOF(true);
		creator.createSimpleTrace(traceFile);
		IoTrace iotrace = new FileIoTrace(methodName,traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
		
		assertTrue( listener.isTraceEnded() );
	
		assertEquals(iterations, listener.getProcessedEntryPoints());
		
		assertEquals(iterations, listener.getProcessedExitPoints());
		
		assertEquals(1,listener.getSeenEntry().size());
		Iterator<Integer> hashIt = listener.getSeenEntry().iterator();
		int hash = hashIt.next();
		
		assertEquals(creator.getHashEnter(),hash);
		
		assertEquals(1,listener.getSeenExit().size());
		Iterator<Integer> hashItExit = listener.getSeenExit().iterator();
		int hashExit = hashItExit.next();
		
		assertEquals(creator.getHashExit(),hashExit);
		
		assertTrue(traceFile.delete());
	}
	
	
	@Test
	public void testProcessOnlyReturn() throws TraceException, IOException, DTraceListenerException {
		
		OptimizedListenerStub listener = new OptimizedListenerStub(0,1); 
		File traceFile = new File(tracePath );
		
		IoTraceCreatorFirstVoid creator = new IoTraceCreatorFirstVoid(methodName,iterations);
		
		creator.createTrace(traceFile);
		IoTrace iotrace = new FileIoTrace(methodName,traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
		
		assertTrue( listener.isTraceEnded() );
	
		assertEquals(iterations, listener.getProcessedEntryPoints());
		
		assertEquals(iterations, listener.getProcessedExitPoints());
		
		assertEquals(1,listener.getSeenEntry().size());
		Iterator<Integer> hashIt = listener.getSeenEntry().iterator();
		int hash = hashIt.next();
		
		assertEquals(creator.getHashEnter(),hash);
		
		assertEquals(1,listener.getSeenExit().size());
		Iterator<Integer> hashItExit = listener.getSeenExit().iterator();
		int hashExit = hashItExit.next();
		
		assertEquals(creator.getHashExit(),hashExit);
		assertTrue(traceFile.delete());
	}
	
	
	@Test
	public void testProcess() throws TraceException, IOException, DTraceListenerException {
		IoTraceCreatorSimple creator = new IoTraceCreatorSimple(methodName,iterations);
		
		
		File traceFile = new File(tracePath );
		creator.createTrace(traceFile);
		

		OptimizedListenerStub listener = new OptimizedListenerStub(0,1); 
		
		
		IoTrace iotrace = new FileIoTrace(methodName,traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
		
		assertTrue( listener.isTraceEnded() );
	
		assertEquals(iterations, listener.getProcessedEntryPoints());
		
		assertEquals(iterations, listener.getProcessedExitPoints());
		
		assertEquals(1,listener.getSeenEntry().size());
		Iterator<Integer> hashIt = listener.getSeenEntry().iterator();
		int hash = hashIt.next();
		
		assertEquals(creator.getHashEnter(),hash);
		
		assertEquals(1,listener.getSeenExit().size());
		Iterator<Integer> hashItExit = listener.getSeenExit().iterator();
		int hashExit = hashItExit.next();
		
		assertEquals(creator.getHashExit(),hashExit);
		assertTrue(traceFile.delete());
	}

}
