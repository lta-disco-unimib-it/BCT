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
package grammarInference.Record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;

/**
 * Test for the parser. This test class automatically generates traces to test
 * the parser.
 * 
 * Trace file format is the following: # is the char separator | is the symbol
 * that indicates the end of a trace symbols have size > 0
 * 
 * 
 * @author Fabrizio Pastore
 * 
 */
public class kbhParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTwoCharsSymbols() throws IOException {

		symbolTest("AA");
	}

	@Test
	public void testSymbolWithBufferSizeLen() throws IOException {
		symbolTest(buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE));
	}

	@Test
	public void testSymbolWithBufferSizeLenMinusOne() throws IOException {
		symbolTest(buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE - 1));
	}

	@Test
	public void testSymbolWithBufferSizeLenPlusOne() throws IOException {
		symbolTest(buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE + 1));
	}

	@Test
	public void testSymbolSpanningOverManyBuffers() throws IOException {
		int traces = 2;
		int traceLen = kbhParser.DEFAULT_BUFFER_SIZE * 10;
		File traceFile = TestArtifactsManager
				.getNewUnitTestFile("grammarInference.Record.kbhParserTest/trace.txt");
		String symbol = buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE * 3);
		createTraceFile(traceFile, traceLen, traces, symbol);
		kbhParser parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		symbol = buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE * 3 + 1);
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		symbol = buildSymbol(kbhParser.DEFAULT_BUFFER_SIZE * 3 - 1);
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traceFile.delete();
	}

	public String buildSymbol(int chars) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars; ++i) {
			sb.append("A");
		}
		return sb.toString();
	}

	@Test
	public void testThreeCharsSymbols() throws IOException {

		symbolTest("AAA");
	}

	public void symbolTest(String symbol) throws IOException {
		File traceFile = TestArtifactsManager
				.getNewUnitTestFile("grammarInference.Record.kbhParserTest/trace.txt");
		kbhParser parser;
		int traceLen;

		int traces;

		traces = 1;

		traceLen = 0;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 1;

		traceLen = 2;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 1;

		traceLen = kbhParser.DEFAULT_BUFFER_SIZE;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 1;
		traceLen = kbhParser.DEFAULT_BUFFER_SIZE - 1;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 1;
		traceLen = kbhParser.DEFAULT_BUFFER_SIZE + 1;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 1;
		traceLen = kbhParser.DEFAULT_BUFFER_SIZE * 2;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traces = 2;
		traceLen = kbhParser.DEFAULT_BUFFER_SIZE;
		createTraceFile(traceFile, traceLen, traces, symbol);
		parser = new kbhParser(traceFile.getAbsolutePath());
		validateTraceFile(parser, traceFile, traceLen, traces, symbol);

		traceFile.delete();
	}

	@Test
	public void testSingletonSymbol() throws IOException {
		symbolTest("A");
	}

	@Test
	public void testEmptyFile() throws IOException {
		File traceFile = TestArtifactsManager
				.getNewUnitTestFile("grammarInference.Record.kbhParserTest/trace.txt");
		kbhParser parser;
		int traceLen;

		int traces;

		traces = 0;

		traceLen = 0;
		createTraceFile(traceFile, traceLen, traces, "");
		parser = new kbhParser(traceFile.getAbsolutePath());
		Iterator<Trace> tit = parser.getTraceIterator();
		assertFalse(tit.hasNext());
		traceFile.delete();
	}

	private void validateTraceFile(kbhParser parser, File traceFile,
			int traceLen, int traces, String symbol) {

		Iterator<String> sit;
		Iterator<Trace> traceIterator;

		int slen = symbol.length();
		traceIterator = parser.getTraceIterator();
		int expectedTraceLen = traceLen / (slen + 1);
		if (traceLen % (slen + 1) > 1) {
			expectedTraceLen++;
		}
		for (int i = 0; i < traces; i++) {
			assertTrue(traceIterator.hasNext());
			Trace trace = (Trace) traceIterator.next();
			assertEquals(expectedTraceLen, trace.getLength());
			sit = trace.getSymbolIterator();
			int size = 0;
			while (size < traceLen) {

				if ((traceLen - size) == 1) {
					assertFalse(sit.hasNext());
					break;
				}

				size += slen + 1;

				if (size > traceLen) {
					int diff = size - traceLen;
					assertEquals(symbol.substring(0, slen - diff), sit.next());
				} else {
					assertEquals(symbol, sit.next());
				}
			}
			assertFalse(sit.hasNext());
		}
		assertFalse(traceIterator.hasNext());

	}

	public void createTraceFile(File traceFile, int traceLen, int traces,
			String symbol) throws IOException {
		BufferedWriter wr = new BufferedWriter(new FileWriter(traceFile));

		for (int i = 0; i < traces; i++) {

			fillTrace(wr, symbol, traceLen);

		}

		wr.close();
	}

	private void fillTrace(BufferedWriter wr, String symbol, int traceLen)
			throws IOException {
		int len = 0;
		int slen = symbol.length();

		while (len < traceLen) {

			if (len > 0) {
				wr.write('#');
			}
			len += slen + 1;

			if (len > traceLen) {
				int diff = len - traceLen;

				wr.write(symbol.substring(0, slen - diff));
			} else {
				wr.write(symbol);
			}
		}

		wr.write('|');

	}

}
