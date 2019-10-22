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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import junit.framework.TestCase;
import testSupport.TestArtifactsManager;
import traceReaders.TraceReaderException;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.MetaDataIterator;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.IoTrace.LineIterator;
import util.FileUtil;
import util.RuntimeContextualDataUtil;
import conf.BctSettingsException;
import conf.ConfigurationSettings;
import conf.DataRecorderSettings;
import conf.InvariantGeneratorSettings;
import conf.SettingsException;
import dfmaker.core.DaikonTraceProcessor;
import dfmaker.core.DaikonTraceProcessor.DTraceListener;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import flattener.core.Handler;
import flattener.handlers.DaikonHandler;
import flattener.handlers.RawHandler;

public class FileDataRecorderTest extends TestCase {
	
	
	private String methodSignature1 = "myPack.ClassA.method_one(Point,Point)";
	private String methodSignature2 = "myPack.ClassA.method_two(Point,Point)";
	private String methodSignature3 = "myPack.ClassA.method_three(Point,Point)";
	private String methodSignature4 = "myPack.ClassA.method_four(Point,Point)";
	private long threadId1 = 1;
	private long threadId2 = 2;
	private long threadId3 = 3;
	private String metaOne = "one";
	private String metaTwo = "two";
	
	private RawHandler[] parametersHandlers;
	private String tmpDir;
	private String dataRecordingDir;
	
	public class InteractionOracle {

		private HashMap<String,List<String>> expectedValues = new HashMap<String, List<String>>();
		
		public void addExpected(String sessionId,String threadId, List<String> expected) {
			expectedValues.put(sessionId+"|"+threadId, expected);
		}

		public void check(String sessionId, String threadId, List<String> recorded) {
			List<String> expected = expectedValues.get(sessionId+"|"+threadId);
			checkResult("Checking session"+sessionId+" thread "+threadId, expected, recorded);
		}
		
	}
	
	public FileDataRecorderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty("bct.home", TestArtifactsManager.getUnitTestFile("BCT_HOME").getAbsolutePath());
		File logDirFile = TestArtifactsManager.getNewUnitTestFile("recorders/fileDataRecorderTest/");
		
		File tmpFile = new File(logDirFile,"tmp");
		File dataRecording = new File(logDirFile,"DataRecording");
		
		
		tmpDir = tmpFile.getAbsolutePath();
		dataRecordingDir = dataRecording.getAbsolutePath();
		
		parametersHandlers = new RawHandler[2];
		
		
		parametersHandlers[0] = new RawHandler("parameter[0]");
		parametersHandlers[0].addNode(".x.intValue()", 1);
		parametersHandlers[0].addNode(".y.intValue()", 2);
		
		parametersHandlers[1] = new RawHandler("parameter[1]");
		parametersHandlers[1].addNode(".x.intValue()", 3);
		parametersHandlers[1].addNode(".y.intValue()", 4);
		
	}

	protected void tearDown() throws Exception {
		parametersHandlers = null;
		//File logDirFile = TestArtifactsManager.getNewUnitTestFile("recorders/fileDataRecorderTest/");
		
		super.tearDown();
	}
	
	protected FileDataRecorder getRecorder(){
		FileDataRecorder recorder = new FileDataRecorder();
		Properties p = new Properties();
		p.put(FileDataRecorder.Options.loggingDataDir, dataRecordingDir);
		ConfigurationSettings opts = new DataRecorderSettings(FileDataRecorder.class,p);
		recorder.init(opts);
		return recorder;
	}
	
	protected FileTracesReader getReader() throws SettingsException, BctSettingsException{
		FileTracesReader reader = new FileTracesReader();
		
		Properties pi = new Properties();
		
		
		pi.put( FileTracesReader.Options.tracesPath, dataRecordingDir );
		pi.put(FileTracesReader.Options.ioTracesDirName, "ioInvariantLogs" );
		pi.put(FileTracesReader.Options.interactionTracesDirName, "interactionInvariantLogs" );
		pi.put(InvariantGeneratorSettings.Options.traceReaderType, "traceReaders.raw.FileTracesReader" );
		pi.put("regressionTestManager.regressionTestManager.testCaseInfoHandler.type", "tools.invariantGenerator.TcInfoHandlerFile");
		pi.put(InvariantGeneratorSettings.Options.temporaryDir, tmpDir);
		pi.put(InvariantGeneratorSettings.Options.normalizedTraceHandlerType, "traceReaders.normalized.NormalizedTraceHandlerFile" );
		pi.put(InvariantGeneratorSettings.Options.fsaEngine, "KBehavior");
		pi.put(InvariantGeneratorSettings.Options.daikonConfig, "essentials");
		
		InvariantGeneratorSettings ivs = new InvariantGeneratorSettings(tools.InvariantGenerator.class,pi);
		reader.init(ivs);
		return reader;
	}

	public void testInit() {
		FileDataRecorder recorder = getRecorder();
		assertEquals( new File(dataRecordingDir+File.separator+"interactionInvariantLogs"), recorder.getInteractionLogDir() );
		assertEquals( new File(dataRecordingDir+File.separator+"ioInvariantLogs"), recorder.getIoLogDir() );
	}

	public void testSetLogDir() {
		String newDir = "tmp2";
		FileDataRecorder recorder = getRecorder();
		recorder.setLogDir(newDir);
		assertEquals( new File(newDir+File.separator+"interactionInvariantLogs"), recorder.getInteractionLogDir() );
		assertEquals( new File(newDir+File.separator+"ioInvariantLogs"), recorder.getIoLogDir() );
	}

	/**
	 * Test one method, two calls
	 * @throws RecorderException
	 * @throws TraceException
	 * @throws TraceReaderException
	 * @throws SettingsException
	 * @throws BctSettingsException
	 */
	public void testIoMeta_1m_2c() throws RecorderException, TraceException, TraceReaderException, SettingsException, BctSettingsException {
		FileDataRecorder recorder = getRecorder();
		recorder.recordIoEnterMeta(methodSignature1, parametersHandlers, metaOne);
		recorder.recordIoExitMeta(methodSignature1, parametersHandlers, metaOne);
		recorder.recordIoEnterMeta(methodSignature1, parametersHandlers, metaTwo);
		recorder.recordIoExitMeta(methodSignature1, parametersHandlers, metaTwo);
		
		FileTracesReader reader = getReader();
		IoTrace trace = reader.getIoTrace(methodSignature1);
		
		ArrayList<String> expectedValues = new ArrayList<String>();
		simulateioCallEnter(expectedValues,methodSignature1,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature1,parametersHandlers);
		simulateioCallEnter(expectedValues,methodSignature1,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature1,parametersHandlers);
		
		
		List<String> observedValues = getRecordedValues(trace);
		
		checkResult("Recorded data values not valid",expectedValues, observedValues);
		
		MetaDataIterator it = trace.getMetaDataIterator();
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertFalse( it.hasNext() );
		
	}
	
	private void simulateCallExit(List<String> sb, String methodSignature12, RawHandler[] parametersHandlers2) {
		simulateCall(sb, "EXIT1", methodSignature12, parametersHandlers);
	}

	/**
	 * Test 2methods two calls
	 * @throws RecorderException
	 * @throws TraceException
	 * @throws TraceReaderException
	 * @throws SettingsException
	 * @throws BctSettingsException
	 */
	public void testIoMeta_2m_2c() throws RecorderException, TraceException, TraceReaderException, SettingsException, BctSettingsException {
		FileDataRecorder recorder = getRecorder();
		recorder.recordIoEnterMeta(methodSignature1, parametersHandlers, metaOne);
		recorder.recordIoExitMeta(methodSignature1, parametersHandlers, metaOne);
		recorder.recordIoEnterMeta(methodSignature2, parametersHandlers, metaOne);
		recorder.recordIoExitMeta(methodSignature2, parametersHandlers, metaOne);
		recorder.recordIoEnterMeta(methodSignature1, parametersHandlers, metaTwo);
		recorder.recordIoExitMeta(methodSignature1, parametersHandlers, metaTwo);
		recorder.recordIoEnterMeta(methodSignature2, parametersHandlers, metaTwo);
		recorder.recordIoExitMeta(methodSignature2, parametersHandlers, metaTwo);
		
		FileTracesReader reader = getReader();
		IoTrace trace = reader.getIoTrace(methodSignature1);
		List<String> recordedValues = getRecordedValues(trace);
		
		ArrayList<String> expectedValues = new ArrayList<String>();
		simulateioCallEnter(expectedValues,methodSignature1,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature1,parametersHandlers);
		simulateioCallEnter(expectedValues,methodSignature1,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature1,parametersHandlers);

		
		checkResult("Recorded data values not valid",expectedValues, recordedValues);
		
		MetaDataIterator it = trace.getMetaDataIterator();
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertFalse( it.hasNext() );
		
		trace = reader.getIoTrace(methodSignature2);
		
		expectedValues = new ArrayList<String>();
		simulateioCallEnter(expectedValues,methodSignature2,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature2,parametersHandlers);
		simulateioCallEnter(expectedValues,methodSignature2,parametersHandlers);
		simulateCallExit(expectedValues,methodSignature2,parametersHandlers);

		recordedValues = getRecordedValues(trace);
		checkResult("Recorded data values not valid",expectedValues, recordedValues);
		
		it = trace.getMetaDataIterator();
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaOne, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertTrue ( it.hasNext() );
		assertEquals( metaTwo, it.next() );
		assertFalse( it.hasNext() );
	}

	
	private static void checkResult(String string, List<String> expectedValues,
			List<String> recordedValues) {
		for ( int i = 0 ; i < expectedValues.size() ; ++i ){
			assertEquals("Line "+i,expectedValues.get(i), recordedValues.get(i));
		}
		assertEquals("Size not match ",expectedValues.size(), recordedValues.size());
	}

	/**
	 * This method returns a string that represent how the flattener should have recorded the handlers on file
	 * @param methodSignature12 
	 * @param sb 
	 * 
	 * @param parametersHandlers
	 * @return
	 */
	private void simulateioCallEnter(List<String> sb, String methodSignature12, RawHandler[] parametersHandlers) {
		simulateCall(sb, "ENTER", methodSignature12, parametersHandlers);
	}
	
	private void simulateCall(List<String> resBuf, String type, String methodSignature12, RawHandler[] parametersHandlers) {
		resBuf.add("");
		resBuf.add(methodSignature12+":::"+type);
		for ( RawHandler handler : parametersHandlers ){
			for ( Entry<String,String> nodeEntry : handler.nodesEntrySet() ){
				String variableName = nodeEntry.getKey();
				String variableValue = nodeEntry.getValue();
				resBuf.add(variableName);
				resBuf.add(variableValue);
				resBuf.add("1");
			}
		}
		
		
	}

	private List<String> getRecordedValues(IoTrace trace) throws TraceException {
		LineIterator lineIt = trace.getLineIterator();
		ArrayList<String> resBuf = new ArrayList<String>();
		while ( lineIt.hasNext() ){
			resBuf.add ( lineIt.next() );
		}
		return resBuf;
	}

	public void testRecordInteractionMeta_1trace_1meth() throws RecorderException, SettingsException, BctSettingsException, TraceException {
		FileDataRecorder recorder = getRecorder();
		recorder.recordInteractionEnterMeta(methodSignature1, threadId1, metaOne );
		recorder.recordInteractionExitMeta(methodSignature1, threadId1, metaOne );
		
		List<String> expected;
		
		
		
		FileTracesReader reader = getReader();
		Iterator<InteractionTrace> tracesIt = reader.getInteractionTraces();
		
		assertTrue(tracesIt.hasNext());
		InteractionTrace trace = tracesIt.next();
		
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionExit(expected,methodSignature1);
		
		List<String> recorded; 
		recorded = getRecordedInteractionTrace(trace);
		
		checkResult("Interaction ", expected, recorded);
		
	}
	
	public void testRecordInteractionMeta_1trace_Nmeth() throws RecorderException, SettingsException, BctSettingsException, TraceException {
		FileDataRecorder recorder = getRecorder();
		recorder.recordInteractionEnterMeta(methodSignature1, threadId1, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId1, metaOne );
		
		List<String> expected;
		
		
		
		FileTracesReader reader = getReader();
		Iterator<InteractionTrace> tracesIt = reader.getInteractionTraces();
		
		assertTrue(tracesIt.hasNext());
		InteractionTrace trace = tracesIt.next();
		
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature1);
		
		List<String> recorded; 
		recorded = getRecordedInteractionTrace(trace);
		
		checkResult("Interaction ", expected, recorded);
		
	}
	
	public void testRecordInteractionMeta_2trace_Nmeth() throws RecorderException, SettingsException, BctSettingsException, TraceException {
		FileDataRecorder recorder = getRecorder();
		recorder.recordInteractionEnterMeta(methodSignature1, threadId1, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId1, metaOne );
		
		
		recorder.recordInteractionEnterMeta(methodSignature1, threadId2, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId2, metaOne );
		
		List<String> expected;
		
		//
		//Build oracle
		//
		InteractionOracle oracle = new InteractionOracle();
		
		//Thread 1
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "0",""+threadId1, expected );
		
		//Thread 2
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionEnter(expected,methodSignature3);
		simulateInteractionEnter(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "0",""+threadId2, expected );
		
		
		//Check
		
		FileTracesReader reader = getReader();
		Iterator<InteractionTrace> tracesIt = reader.getInteractionTraces();
		List<String> recorded;
		
		assertTrue(tracesIt.hasNext());
		InteractionTrace trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		 
		
		
	}
	
	
	public void testRecordInteractionMeta_2Executions_3traces_Nmeth() throws RecorderException, SettingsException, BctSettingsException, TraceException {
		RuntimeContextualDataUtil.setPid("0");
		FileDataRecorder recorder = getRecorder();
		recorder.recordInteractionEnterMeta(methodSignature1, threadId1, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId1, metaOne );
		
		recorder.recordInteractionEnterMeta(methodSignature1, threadId2, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId2, metaOne );
		
		
		//Simulate a new Exceution
		RuntimeContextualDataUtil.setPid("1");
		recorder = getRecorder();
		recorder.recordInteractionEnterMeta(methodSignature1, threadId1, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature3, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature3, threadId1, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId1, metaOne );
		
		
		recorder.recordInteractionEnterMeta(methodSignature1, threadId2, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature2, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature4, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature3, threadId2, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId2, metaOne );
		
		recorder.recordInteractionEnterMeta(methodSignature1, threadId3, metaOne );
		recorder.recordInteractionEnterMeta(methodSignature3, threadId3, metaTwo );
		recorder.recordInteractionEnterMeta(methodSignature4, threadId3, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature4, threadId3, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature3, threadId3, metaTwo );
		recorder.recordInteractionExitMeta(methodSignature1, threadId3, metaOne );
		
		
		List<String> expected;
		
		//
		//Build oracle
		//
		InteractionOracle oracle = new InteractionOracle();
		
		//First execution - Thread 1
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "0",""+threadId1, expected );
		
		//First execution - Thread 2
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionEnter(expected,methodSignature3);
		simulateInteractionEnter(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "0",""+threadId2, expected );
		
		//Second execution - Thread 1
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "1",""+threadId1, expected );
		
		//Second execution - Thread 2
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature2);
		simulateInteractionExit(expected,methodSignature2);
		simulateInteractionEnter(expected,methodSignature3);
		simulateInteractionEnter(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "1",""+threadId2, expected );
		
		//Second execution - Thread 3
		expected= new ArrayList<String>();
		simulateInteractionEnter(expected,methodSignature1);
		simulateInteractionEnter(expected,methodSignature3);
		simulateInteractionEnter(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature4);
		simulateInteractionExit(expected,methodSignature3);
		simulateInteractionExit(expected,methodSignature1);
		
		oracle.addExpected( "1",""+threadId3, expected );
		
		
		
		
		
		
		//Check
		
		FileTracesReader reader = getReader();
		Iterator<InteractionTrace> tracesIt = reader.getInteractionTraces();
		List<String> recorded;
		
		assertTrue(tracesIt.hasNext());
		InteractionTrace trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		 
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertFalse(tracesIt.hasNext());
		
		
		
		
		tracesIt = reader.getInteractionTracesForSession("0");
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		
		assertFalse(tracesIt.hasNext());
		
		
		
		
		tracesIt = reader.getInteractionTracesForSession("1");
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		
		assertTrue(tracesIt.hasNext());
		trace = tracesIt.next();
		recorded = getRecordedInteractionTrace(trace);
		oracle.check(trace.getSessionId(),trace.getThreadId(),recorded);
		
		assertFalse(tracesIt.hasNext());
		
		
	}

	private List<String> getRecordedInteractionTrace(InteractionTrace trace) throws TraceException {
		List<String> result = new ArrayList<String>();
		Token next;
		while ( ( next = trace.getNextToken() ) != null ){
			result.add(next.getTokenValue());
		}
		return result;
	}

	private void simulateInteractionExit(List<String> expected,
			String methodSignature12) {
		simulateInteraction(expected, "E", methodSignature12);
	}

	private void simulateInteractionEnter(List<String> expected,
			String methodSignature12) {
		simulateInteraction(expected, "B", methodSignature12);
	}

	private void simulateInteraction(List<String> expected, String type,
			String methodSignature12) {
		expected.add(methodSignature12+type);
	}


}

