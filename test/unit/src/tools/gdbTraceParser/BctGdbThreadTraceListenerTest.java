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
package tools.gdbTraceParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import recorders.DataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import traceReaders.raw.FileReaderException;
import util.FileUtil;
import flattener.core.Handler;

@Ignore("FIXME")
public class BctGdbThreadTraceListenerTest {
	DataRecorder recorder;
	private File testDir;
	private File resultDir;



	@Before
	public void setup(){
		recorder = EasyMock.createMock(DataRecorder.class);
		RecorderFactory.setLoggingRecorder(recorder);	

		testDir = new File("test/unit/artifacts/tools/gdbTraceParser/BctGdbThreadTraceListenerTest");
		resultDir = new File(testDir,"results");

		resultDir.mkdirs();

		FileUtil.deleteDirectoryContents(resultDir);
	}

	public void tearDown(){
		FileUtil.deleteDirectoryContents(resultDir);
	}

	@Test
	public void testTwoFunctionsClosed_noFakeCallsParser_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(false, false);
	}

	@Test
	public void testTwoFunctionsClosed_fakeCallsParser_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(true, false);
	}

	@Test
	public void testTwoFunctionsClosed_noFakeCallsParser_fakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(false, true);
	}

	@Test
	public void testTwoFunctionsClosed_fakeCallsParser_fakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(true, true);
	}

	private void testTwoFunctionsClosed( boolean parserFakeCallsAtProcessEnd, boolean listenerSimulateFunctionsExit) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"twoFunctionsClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();

		BctGdbThreadTraceListener listener = new BctGdbThreadTraceListener( listenerSimulateFunctionsExit  );
		listeners.add(listener);


		recorder.recordIoInteractionEnter( EasyMock.eq("main"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), (Handler) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionExit( EasyMock.eq("main"), (Handler[]) EasyMock.anyObject(), (Handler) EasyMock.anyObject(), EasyMock.anyLong() );

		EasyMock.replay(recorder);

		parser.processTraces(listeners );

		EasyMock.verify(recorder);
	}





	@Test
	public void testIncompleteTrace_noFakeCallsParent_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(false, false);
	}
	
	@Test
	public void testIncompleteTrace_fakeCallsParent_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(true, false);
	}

	@Test
	public void testIncompleteTrace_noFakeCallsParent_fakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(false, true);
	}
	
	@Test
	public void testIncompleteTrace_fakeCallsParent_fakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(true, true);
	}
	
	public void testIncompleteTrace(boolean parserFakeCallsAtProcessEnd, boolean listenerSimulateFunctionsExit) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"allFunctionsNotClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


		BctGdbThreadTraceListener listener = new BctGdbThreadTraceListener( listenerSimulateFunctionsExit  );
		listeners.add(listener);

		recorder.recordIoInteractionEnter( EasyMock.eq("main"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		recorder.recordIoInteractionEnter( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		
		if ( parserFakeCallsAtProcessEnd || listenerSimulateFunctionsExit ){
			recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
			recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
			recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
			recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
			recorder.recordIoInteractionExit( EasyMock.eq("_ZN8Recurser7recurseEi"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
			recorder.recordIoInteractionExit( EasyMock.eq("main"), (Handler[]) EasyMock.anyObject(), EasyMock.anyLong() );
		}
		
		EasyMock.replay(recorder);

		parser.processTraces(listeners );

		EasyMock.verify(recorder);

	}

}
