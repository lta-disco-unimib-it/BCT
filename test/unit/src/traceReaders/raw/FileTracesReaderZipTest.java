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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import traceReaders.raw.IoTrace.LineIterator;

/**
 * Test file traces reader when data is stored in a zip archive
 * 
 * @author Fabrizio Pastore
 * 
 */
public class FileTracesReaderZipTest {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMultipleZippedTraces() throws Exception {
		FileTracesReader reader = new FileTracesReader(
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", "interactionInvariantLogs", true);

		Iterator<IoTrace> traces = reader.getIoTraces();

		assertTrue(traces.hasNext());

		IoTrace trace;

		List<String> methods = new ArrayList<String>();
		List<IoTrace> tracesList = new ArrayList<IoTrace>();

		List<String> expectedMethods = new ArrayList<String>();
		expectedMethods
				.add("java.io.FileInputStream.<init>((Ljava.lang.String;)V)");
		expectedMethods
				.add("net.n3.nanoxml.StdXMLReader.fileReader((Ljava.lang.String;)Lnet.n3.nanoxml.IXMLReader;)");
		expectedMethods
				.add("net.n3.nanoxml.XMLParserFactory.createDefaultXMLParser(()Lnet.n3.nanoxml.IXMLParser;)");

		trace = traces.next();
		tracesList.add(trace);
		assertNotNull(trace);
		methods.add(trace.getMethodName());

		trace = traces.next();
		tracesList.add(trace);
		assertNotNull(trace);
		methods.add(trace.getMethodName());

		trace = traces.next();
		tracesList.add(trace);
		assertNotNull(trace);
		methods.add(trace.getMethodName());

		Collections.sort(methods);
		Collections.sort(tracesList, new Comparator<IoTrace>() {

			public int compare(IoTrace o1, IoTrace o2) {
				return o1.getMethodName().compareTo(o2.getMethodName());
			}

		});

		assertEquals(expectedMethods, methods);

		checkIoTrace(
				tracesList.get(2),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 0);
		checkIoTrace(
				tracesList.get(1),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 1);
		checkIoTrace(
				tracesList.get(0),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 2);

		// Check if works on multiple calls for same trace

		checkIoTrace(
				tracesList.get(2),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 0);
		checkIoTrace(
				tracesList.get(1),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 1);
		checkIoTrace(
				tracesList.get(0),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 2);

		trace = reader
				.getIoTrace("java.io.FileInputStream.<init>((Ljava.lang.String;)V)");
		checkIoTrace(
				trace,
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 2);

		trace = reader
				.getIoTrace("net.n3.nanoxml.XMLParserFactory.createDefaultXMLParser(()Lnet.n3.nanoxml.IXMLParser;)");
		checkIoTrace(
				tracesList.get(2),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 0);

		trace = reader
				.getIoTrace("net.n3.nanoxml.StdXMLReader.fileReader((Ljava.lang.String;)Lnet.n3.nanoxml.IXMLReader;)");
		checkIoTrace(
				tracesList.get(1),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"ioInvariantLogs", 1);

		Iterator<InteractionTrace> intTracesIterator = reader
				.getInteractionTraces();
		List<InteractionTrace> intTraces = new ArrayList<InteractionTrace>();

		assertTrue(intTracesIterator.hasNext());
		InteractionTrace intTrace0 = intTracesIterator.next();
		assertNotNull(intTrace0);
		intTraces.add(intTrace0);

		assertTrue(intTracesIterator.hasNext());
		InteractionTrace intTrace1 = intTracesIterator.next();
		assertNotNull(intTrace1);
		intTraces.add(intTrace1);

		assertTrue(intTracesIterator.hasNext());
		InteractionTrace intTrace2 = intTracesIterator.next();
		assertNotNull(intTrace2);
		intTraces.add(intTrace2);

		assertTrue(intTracesIterator.hasNext());
		InteractionTrace intTrace3 = intTracesIterator.next();
		assertNotNull(intTrace3);
		intTraces.add(intTrace3);

		Collections.sort(intTraces, new Comparator<InteractionTrace>() {

			public int compare(InteractionTrace o1, InteractionTrace o2) {
				return (o1.getSessionId() + "." + o1.getThreadId())
						.compareTo((o2.getSessionId() + "." + o2.getThreadId()));
			}
		});

		checkInteractionTrace(
				intTraces.get(0),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"interactionInvariantLogs", 0);
		checkInteractionTrace(
				intTraces.get(1),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"interactionInvariantLogs", 1);
		checkInteractionTrace(
				intTraces.get(2),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"interactionInvariantLogs", 2);
		checkInteractionTrace(
				intTraces.get(3),
				TestArtifactsManager
						.getUnitTestFile("traceReaders/raw/FileTracesReaderZip/DataRecordingMany"),
				"interactionInvariantLogs", 3);
	}

	private void checkInteractionTrace(InteractionTrace interactionTrace,
			File zippedDataFolder, String interactionFolderName, int i)
			throws Exception {
		File archive = new File(zippedDataFolder.getParent(), zippedDataFolder
				.getName()
				+ ".zip");
		ZipFile f = new ZipFile(archive);

		ZipEntry expectedEntry = f.getEntry(zippedDataFolder.getName() + "/"
				+ interactionFolderName + "/" + i + ".dtrace");
		assertNotNull(expectedEntry);

		InputStream expectedIs = f.getInputStream(expectedEntry);
		InputStreamReader expectedIR = new InputStreamReader(expectedIs);

		InteractionTraceReader r = new InteractionTraceReader(expectedIR);

		int count = 0;
		Token observedToken;
		String expectedToken;
		while ((expectedToken = r.getNextToken()) != null) {
			observedToken = interactionTrace.getNextToken();
			assertNotNull("Trace " + i + " terminated before expected.",
					observedToken);
			assertEquals("Error in line " + count, expectedToken, observedToken
					.getTokenValue());
		}
		expectedIs.close();

		f.close();

		assertNull(
				"Error in line " + count + ". Trace is longer than expected",
				interactionTrace.getNextToken());
	}

	private void checkIoTrace(IoTrace ioTrace, File zippedDataFolder,
			String ioFolderName, int i) throws Exception {
		File archive = new File(zippedDataFolder.getParent(), zippedDataFolder
				.getName()
				+ ".zip");
		ZipFile f = new ZipFile(archive);

		ZipEntry expectedEntry = f.getEntry(zippedDataFolder.getName() + "/"
				+ ioFolderName + "/" + i + ".dtrace");
		assertNotNull(expectedEntry);

		InputStream expectedIs = f.getInputStream(expectedEntry);
		BufferedReader expectedBR = new BufferedReader(new InputStreamReader(
				expectedIs));

		LineIterator lit = ioTrace.getLineIterator();

		String expectedLine;
		int count = 0;
		while ((expectedLine = expectedBR.readLine()) != null) {
			assertTrue("Error in line " + count, lit.hasNext());
			String observedLine = lit.next();
			assertEquals("Error in line " + count, expectedLine, observedLine);
		}
		expectedIs.close();

		f.close();
		assertFalse("Error in line " + count
				+ ". Trace is longer than expected", lit.hasNext());
	}

}
