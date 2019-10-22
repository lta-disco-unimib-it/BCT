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
package recorders;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import traceReaders.TraceReaderException;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TracesReader;
import traceReaders.raw.IoTrace.LineIterator;
import conf.BctSettingsException;
import conf.EnvironmentalSetter;
import dfmaker.core.DaikonTraceProcessor;
import dfmaker.core.DaikonTraceProcessor.DTraceListener;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import flattener.core.Handler;
import flattener.handlers.RawHandler;

public abstract class TestConcurrentRecording {

	enum RecordingType {interaction,ioInteraction, ioInteractionManyMethods}

	private static int NUMBER_OF_METHODS = 35;
	
//	private int[] threadIterations = {
//			30000,
//			40000,
//			2000,
//			30000,
//			10000,
//			30001,
//			30200,
//			77000,
//			10,
//			300,
//			30000,
//			30020,
//			10000,
//			30001,
//			30200,
//			77002,
//			10,
//			300,
//			30200,
//			30000,};
	
	private int[] threadsIterations = {
			3000,
			4000,
			200,
			3000,
			1000,
			3001,
			3020,
			7700,
			1,
			30,
			3000,
			3002,
			1000,
			3000,
			3020,
			2700,
			10,
			300,
			3020,
			3000,
	};
	
	
	private int[] threadIterationsManyMethods = {
			37770,
			41300,
			20009,
	};
	
	private HashMap<String,Integer> invocations = new HashMap<String, Integer>();
	
	public static class InteractionTraceGeneratorThread extends GeneratorThread {

		public InteractionTraceGeneratorThread(
				DataRecorder recorder,
				int iterations, int threadId) {
			super(recorder, iterations, threadId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void recordData(String method,Handler[] inputEnter, Handler[] inputExit, Handler returnValue) {
			try {
				recorder.recordInteractionEnter(method, threadId);
				try {
					Thread.sleep((long) (Math.random()*30));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recorder.recordInteractionExit(method, threadId);
			} catch (RecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public static class IoInteractionTraceGeneratorThread extends GeneratorThread {

		public IoInteractionTraceGeneratorThread(
				DataRecorder recorder,
				int iterations, int threadId) {
			super(recorder, iterations, threadId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void recordData(String method,Handler[] inputEnter, Handler[] inputExit, Handler returnValue) {
			try {
				recorder.recordIoInteractionEnter( method, inputEnter, threadId);
				
				try {
					Thread.sleep((long) (Math.random()*30));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if ( returnValue == null ){
					recorder.recordIoInteractionExit(method, inputExit, threadId);
				} else {
					recorder.recordIoInteractionExit(method, inputExit, returnValue, threadId);
				}
			} catch (RecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		
	}
	
	public static abstract class GeneratorThread extends Thread{
		
		protected DataRecorder recorder;
		private int iterations;
		protected int threadId;
		private boolean running = true;
		
		public GeneratorThread(DataRecorder recorder,int iterations, int threadId){
			this.recorder = recorder;
			this.iterations = iterations;
			this.threadId = threadId;
		}
		
		public void run(){
			for( int i = 0; i < iterations; i++ ){
				
				
				String value = "MyClass.myMeth"+getKeyForIteration(i)+"()";
				
				System.out.println("Thread "+threadId+" recording enter/exit "+value);
				
				
				Handler[] handlersEnter= getInputDataEnter(i);
				

				Handler[] handlersExit= getInputDataExit(i);
				Handler handlerReturn= getIoDataReturn(i);
				
				
				recordData(value,handlersEnter,handlersExit,handlerReturn);
			}

			System.out.println(GeneratorThread.class.getCanonicalName()+" "+threadId+" terminated.");
			
			running = false;
		}
		
		

		private Handler[] getInputDataEnter(int i) {
			RawHandler handler = new RawHandler("parameter[0]");
			handler.addNodeValue(".name", "\"Name\"");
			handler.addNodeValue(".counter", ""+i);
			
			return new RawHandler[]{handler}; 
		}
		
		private Handler[] getInputDataExit(int i) {
			RawHandler handler = new RawHandler("parameter[0]");
			handler.addNodeValue(".name", "\"Name\"");
			handler.addNodeValue(".counter", ""+(i+7));
			
			return new RawHandler[]{handler}; 
		}
		
		private Handler getIoDataReturn(int i ) {
			if ( ( i % 5 ) == 0 ){
				return null;
			}
			RawHandler handler = new RawHandler("parameter[0]");
			handler.addNodeValue("name", "\"Name\"");
			handler.addNodeValue("counter", "\"Name\"");
			
			return handler; 
		}

		public abstract void recordData(String method,Handler[] inputEnter, Handler[] inputExit, Handler returnValue);

		public boolean isRunning() {
			// TODO Auto-generated method stub
			return running;
		}
		
	}


	
	
	public abstract DataRecorder getRecorder(RecordingType recordingType) throws Exception;
	
	public abstract File getBctHome(RecordingType recordingType) throws Exception;
	
	public abstract TracesReader getReader(RecordingType recordingType) throws Exception;
	
	public abstract void recordingEnd() throws Exception;
	
	public abstract void recordingStart() throws Exception;
	
	public abstract void testEnd() throws Exception;
	
	private List<GeneratorThread> threads = new ArrayList();
	
	@Test
	public void testInteractionConcurrentRecordingFirstSession() throws Exception{
		testConcurrentRecording(1,RecordingType.interaction);
	}
	
	@Test
	public void testInteractionConcurrentRecordingSecondSession() throws Exception{
		testConcurrentRecording(2,RecordingType.interaction);
	}
	
	@Test
	public void testIoInteractionConcurrentRecordingFirstSession() throws Exception{
		testConcurrentRecording(1,RecordingType.ioInteraction);
	}

	@Test
	public void testIoInteractionConcurrentRecordingSecondSession() throws Exception{
		testConcurrentRecording(2,RecordingType.ioInteraction);
	}
	
	@Test
	public void testIoInteractionManyMethodsConcurrentRecordingFirstSession() throws Exception{
		int old = NUMBER_OF_METHODS;
		NUMBER_OF_METHODS = Integer.MAX_VALUE;
		int[] oldIterations = threadsIterations;
		threadsIterations = threadIterationsManyMethods;
		testConcurrentRecording(1,RecordingType.ioInteractionManyMethods);
		NUMBER_OF_METHODS = old;
		threadsIterations = oldIterations;
	}
	

	
	private static int getKeyForIteration(int i) {
		return i%NUMBER_OF_METHODS;
	}
	
	public void testConcurrentRecording(int sessions, RecordingType recordingType) throws Exception{
		
		
		//tracesDir.mkdirs();
		
		EnvironmentalSetter.setBctHome(getBctHome(recordingType).getAbsolutePath());
		
		DataRecorder recorder = getRecorder(recordingType);
		
		long startTime = System.currentTimeMillis();
		
		
		recordingStart();
		
		
		for ( int i = 0; i < threadsIterations.length; i++ ){
			GeneratorThread t = null;
			switch ( recordingType ){
			case interaction:
				t = new InteractionTraceGeneratorThread(recorder, threadsIterations[i], i);
				break;
			case ioInteraction:
				t = new IoInteractionTraceGeneratorThread(recorder, threadsIterations[i], i);
				break;
			case ioInteractionManyMethods:
				t = new IoInteractionTraceGeneratorThread(recorder, threadsIterations[i], i);
				break;	
			}

			t.start();
			threads.add(t);

		}
		
		long time = System.currentTimeMillis();
		
		synchronized (this) {

			while ( threadsRunning() ){
				this.wait(3000);
			}

		}
		
		System.out.println("Recording ended");
		recordingEnd();
		
		
		System.out.println("Execution time "+(System.currentTimeMillis()-time));
		
		long executionTime = System.currentTimeMillis() - startTime;
		
		TracesReader reader = getReader(recordingType);
		
		for ( int i = 0; i < threadsIterations.length; i++ ){
			checkTrace( reader, recordingType, i, sessions );
			
		}
		
		if ( recordingType == RecordingType.ioInteraction ){
			checkIoTraces( reader, recordingType, sessions );
		}
		
		
		
		testEnd();
		
	}


	private void checkIoTraces(TracesReader reader,
			RecordingType recordingType, int sessions) throws TraceReaderException, TraceException, DTraceListenerException {
		//Check the total number of distinct method that we have
		int totalMethods = 0;
		for ( int threadIterations : threadsIterations){
			int methodsPerThread = getKeyForIteration(threadIterations);
			if ( methodsPerThread > totalMethods ){
				totalMethods = methodsPerThread;
			}
		}
		
		
		//count how many times each distinct method is invoked
		int totalInvocationsPerMethod[] = new int[totalMethods]; 
		
		for ( int method = 0; method < totalMethods; method++ ){
			int allInvocations = 0;
			for ( int threadIterations : threadsIterations){
				int invocationsWithinThread = threadIterations/NUMBER_OF_METHODS;
				if ( ( threadIterations%NUMBER_OF_METHODS ) >= method ){
					invocationsWithinThread+=1;
				}
				allInvocations+=invocationsWithinThread;
			}
			totalInvocationsPerMethod[method] = allInvocations;
		}
		
		for ( int method = 0; method < totalMethods; method++ ){
			String methodName = "MyClass.myMeth"+method+"()";
			IoTrace trace = reader.getIoTrace(methodName);
			
			TestDaikonProcessor myCommandExecutor = new TestDaikonProcessor();
			DaikonTraceProcessor dtp = new DaikonTraceProcessor(myCommandExecutor);
			dtp.process(trace.getLineIterator());
			
			int expectedInvocations = getInvocationsPerMethod(methodName);
			
			assertEquals(expectedInvocations,myCommandExecutor.entryPoints);
			assertEquals(expectedInvocations,myCommandExecutor.exitPoints);
			
			Map<String, Integer> values = myCommandExecutor.vars.get("parameter[0].name");
			
			assertEquals( expectedInvocations*2, values.get("\"Name\"").intValue() );
		}
		
	}
	
	private static class TestDaikonProcessor implements DTraceListener{

		private int entryPoints;
		int exitPoints;
		public HashMap<String,Map<String,Integer>> vars = new HashMap<String,Map<String,Integer>>();
		
		@Override
		public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
			entryPoints++;
		}

		@Override
		public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
			
			// TODO Auto-generated method stub
			exitPoints++;
		}
		
		

		@Override
		public void newProgramVar(String varName, String varValue,
				String varModifier) throws DTraceListenerException {
			// TODO Auto-generated method stub
			Map<String, Integer> varMap = vars.get(varName);
			
			if ( varMap == null ){
				varMap = new HashMap<String,Integer>();
				vars.put(varName, varMap);
			}
			
			Integer count = varMap.get(varValue);
			if ( count == null ){
				varMap.put(varValue, 1);
			} else {
				varMap.put(varValue, 1+count);
			}
		}

		@Override
		public void traceEnd() throws DTraceListenerException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void genericProgramPoint(long beginOffset, String line)
				throws DTraceListenerException {
			// TODO Auto-generated method stub
			
		}
		
	}

	private boolean threadsRunning() {
		for ( GeneratorThread t : threads ){
			if ( t.isRunning() ){
				return true;
			}
		}
		return false;
	}

	private void checkTrace(TracesReader reader, RecordingType recordingType, int threadId, int sessions ) throws BctSettingsException, TraceException, IOException {
		System.out.println("Checking traces for thread "+threadId);
			
			//new FileTracesReader(  tracesDir, FileDataRecorder.DEFAULT_IO_DIR, FileDataRecorder.DEFAULT_INTERACTION_DIR, false);
		Iterator<InteractionTrace> traces = reader.getInteractionTracesForThreadId(String.valueOf(threadId));
		
		
		for (int s = 0; s < sessions; s++ ){

			assertTrue ( traces.hasNext() );
			InteractionTrace trace = traces.next();
			
			for( int i = 0; i < threadsIterations[threadId]; i++ ){
				Token token = trace.getNextToken();
				assertEquals( "MyClass.myMeth"+getKeyForIteration(i)+"()B", token.getTokenValue() ) ;
				assertEquals( "MyClass.myMeth"+getKeyForIteration(i)+"()", token.getMethodSignature() );

				token = trace.getNextToken();
				assertEquals ( "MyClass.myMeth"+getKeyForIteration(i)+"()E" , token.getTokenValue());
				assertEquals ( "MyClass.myMeth"+getKeyForIteration(i)+"()" , token.getMethodSignature() );
				
				String method = "MyClass.myMeth"+getKeyForIteration(i)+"()";
				addInvocationPerMethod(method); 
			}

			Token unexpectedToken = trace.getNextToken();
			assertEquals( "Trace for thread "+threadId+" should be ended at this point", null,  unexpectedToken );




			trace.close();
		}
		assertFalse ( "Since we have only "+sessions+" recording session(s) we expect to have just "+sessions+" trace(s) for thread "+threadId, traces.hasNext() );
		
	}

	private void addInvocationPerMethod(String method) {
		Integer count = invocations.get(method);
		if ( count == null ){
			invocations.put(method, 1);
		} else {
			invocations.put(method, 1+count);
		}
	}
	
	private int getInvocationsPerMethod(String method){
		return invocations.get(method);
	}
}
