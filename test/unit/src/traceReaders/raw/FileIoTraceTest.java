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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import traceReaders.raw.FileIoTrace.FileLineIterator;

public class FileIoTraceTest extends TestCase {

	private int testFileLines=300000;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void testLineIteratorPerformace() throws IOException{
		File file = createTestFile();
		BufferedReader r = new BufferedReader( new FileReader(file) );
		long startTime = System.currentTimeMillis();
		FileLineIterator it = new FileLineIterator(r);
		int c=0;
		while ( it.hasNext() ){
			String line = it.next();
			++c;
		}
		long finishTime = System.currentTimeMillis();
		System.out.println(finishTime-startTime);
		assertEquals(testFileLines, c);
		
		assertTrue( deleteTestFile() );
	}

	private boolean deleteTestFile() {
		File file = new File("test/unit/artifacts/traceReaders/FileLineIteratorTest.tmp.txt");
		return file.delete();
	}

	private File createTestFile() throws IOException {
		File file = new File("test/unit/artifacts/traceReaders/FileLineIteratorTest.tmp.txt");
		FileWriter w = new FileWriter(file);
		
		int c = 0;
		while ( c < testFileLines ){
			w.write("testLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLinetestLine\n");
			++c;
		}
		
		w.close();
		
		return file;
	}
}
