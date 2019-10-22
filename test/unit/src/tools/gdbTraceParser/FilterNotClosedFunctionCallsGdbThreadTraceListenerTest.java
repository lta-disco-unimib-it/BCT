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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import recorders.DataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import traceReaders.raw.FileReaderException;
import util.FileUtil;
import flattener.core.Handler;

public class FilterNotClosedFunctionCallsGdbThreadTraceListenerTest {

	private File testDir;
	private File resultDir;
	private File resultParent;



	@Before
	public void setup(){
		testDir = new File("test/unit/artifacts/tools/gdbTraceParser/BctGdbThreadTraceListenerTest");
		resultParent = new File("test/unit/artifacts/tools/gdbTraceParser/FilterNotClosedFunctionCallsGdbThreadTraceListenerTest");
		resultDir = new File(resultParent,"results");

		resultDir.mkdirs();

		FileUtil.deleteDirectoryContents(resultDir);
	}

	public void tearDown(){
		FileUtil.deleteDirectoryContents(resultDir);
	}

	@Test
	public void testTwoFunctionsClosed_noFakeCallsParser_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(false);
	}

	@Test
	public void testTwoFunctionsClosed_fakeCallsParser_noFakeCallsListener() throws FileReaderException, IOException, RecorderException {
		testTwoFunctionsClosed(true);
	}



	private void testTwoFunctionsClosed(boolean parserFakeCallsAtProcessEnd ) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"twoFunctionsClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();

		FilterNotClosedFunctionCallsGdbThreadTraceListener listener = new FilterNotClosedFunctionCallsGdbThreadTraceListener();
		listeners.add(listener);



		parser.processTraces(listeners );

		HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
		assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());

	}





	@Test
	public void testIncompleteTrace_noFakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(false);
	}
	
	@Test
	public void testIncompleteTrace_fakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testIncompleteTrace(true);
	}

	
	@Test
	public void testAlmostIncompleteTrace_noFakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testAlmostIncompleteTrace(false);
	}
	
	@Test
	public void testAlmostIncompleteTrace_fakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testAlmostIncompleteTrace(true);
	}
	
	@Test
	public void testMultipleCompleteIncompleteTraces_noFakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testMultipleCompleteIncompleteTraces(false);
	}
	
	@Test
	public void testMultipleCompleteIncompleteTraces_fakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testMultipleCompleteIncompleteTraces(true);
	}

	
	public void testIncompleteTrace(boolean parserFakeCallsAtProcessEnd) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"allFunctionsNotClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


		FilterNotClosedFunctionCallsGdbThreadTraceListener listener = new FilterNotClosedFunctionCallsGdbThreadTraceListener();
		listeners.add(listener);




		parser.processTraces(listeners );

		if ( parserFakeCallsAtProcessEnd ){
			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		} else {

			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			List<Integer> notClosedFunctions = new ArrayList<Integer>();
			notClosedFunctions.add(5);
			notClosedFunctions.add(4);
			notClosedFunctions.add(3);
			notClosedFunctions.add(2);
			notClosedFunctions.add(1);
			notClosedFunctions.add(0);

			expectedFunctionsNotClosed.put(0, notClosedFunctions);

			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		}
	}
	
	
	public void testAlmostIncompleteTrace(boolean parserFakeCallsAtProcessEnd) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"recursiveFunctionsNotClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


		FilterNotClosedFunctionCallsGdbThreadTraceListener listener = new FilterNotClosedFunctionCallsGdbThreadTraceListener();
		listeners.add(listener);




		parser.processTraces(listeners );

		if ( parserFakeCallsAtProcessEnd ){
			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		} else {

			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			List<Integer> notClosedFunctions = new ArrayList<Integer>();
			notClosedFunctions.add(24);
			notClosedFunctions.add(21);
			notClosedFunctions.add(18);
			notClosedFunctions.add(15);
			notClosedFunctions.add(12);
			notClosedFunctions.add(9);
			notClosedFunctions.add(6);
			notClosedFunctions.add(3);
			notClosedFunctions.add(0);

			expectedFunctionsNotClosed.put(0, notClosedFunctions);

			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		}
	}

	
	
	
	public void testMultipleCompleteIncompleteTraces(boolean parserFakeCallsAtProcessEnd) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"allClosed_8recursiveNotClosed_3recursiveNotClosed.gdb.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


		FilterNotClosedFunctionCallsGdbThreadTraceListener listener = new FilterNotClosedFunctionCallsGdbThreadTraceListener();
		listeners.add(listener);




		parser.processTraces(listeners );

		if ( parserFakeCallsAtProcessEnd ){
			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		} else {

			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			List<Integer> notClosedFunctions = new ArrayList<Integer>();
			notClosedFunctions.add(24);
			notClosedFunctions.add(21);
			notClosedFunctions.add(18);
			notClosedFunctions.add(15);
			notClosedFunctions.add(12);
			notClosedFunctions.add(9);
			notClosedFunctions.add(6);
			notClosedFunctions.add(3);
			notClosedFunctions.add(0);

			expectedFunctionsNotClosed.put(1, notClosedFunctions); 

			notClosedFunctions = new ArrayList<Integer>();
			notClosedFunctions.add(9);
			notClosedFunctions.add(6);
			notClosedFunctions.add(3);
			notClosedFunctions.add(0);

			expectedFunctionsNotClosed.put(2, notClosedFunctions);
			
			
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		}
	}
	
	@Test
	public void testUnexpectedExitWithoutEnter_BUG196_noFakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testUnexpectedExitWithoutEnter_BUG196(false);
	}
	
	@Test
	public void testUnexpectedExitWithoutEnter_BUG196_fakeCallsParent() throws FileReaderException, IOException, RecorderException {
		testUnexpectedExitWithoutEnter_BUG196(true);
	}
	
	
	public void testUnexpectedExitWithoutEnter_BUG196(boolean parserFakeCallsAtProcessEnd) throws FileReaderException, IOException, RecorderException {
		File trace = new File(testDir,"unexpectedExit.txt");

		List<File> traces = new ArrayList<File>();
		traces.add(trace);

		GdbTraceParser parser = new GdbTraceParser(resultDir);
		parser.setAddFakeCallsAtProcessEnd(parserFakeCallsAtProcessEnd);
		parser.parseTraces(traces);


		List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();


		FilterNotClosedFunctionCallsGdbThreadTraceListener listener = new FilterNotClosedFunctionCallsGdbThreadTraceListener();
		listeners.add(listener);




		parser.processTraces(listeners );

		if ( parserFakeCallsAtProcessEnd ){
			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		} else {

			HashMap<Integer,List<Integer>> expectedFunctionsNotClosed = new HashMap<Integer, List<Integer>>();
			List<Integer> notClosedFunctions = new ArrayList<Integer>();
			notClosedFunctions.add(2);
			expectedFunctionsNotClosed.put(0, notClosedFunctions); 

			
			
			
			assertEquals(expectedFunctionsNotClosed, listener.getFunctionsNotClosedInTrace());
		}
	}
	
}
