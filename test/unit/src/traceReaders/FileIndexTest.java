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
package traceReaders;

import java.io.File;
import java.io.IOException;

import util.FileIndex;
import util.FileIndex.FileIndexException;

import junit.framework.TestCase;

public class FileIndexTest extends TestCase {

	public FileIndexTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFileIndexOC() throws FileIndexException{
		File f = new File("testIndex.idx");
		FileIndex fi = new FileIndex(f);
		String one = fi.add("one");
		String two = fi.add("two");
		assertFalse( one.equals(two));
		assertEquals( one, fi.getId("one") );
		assertEquals( two, fi.add("two") );
		assertEquals( "two", fi.getNameFromId(two) );
		try {
			fi.save(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fi = new FileIndex(f);
		assertEquals( one, fi.getId("one") );
		assertEquals( two, fi.getId("two") );
		
		assertTrue( f.delete() );
	}

	public void testGetId() throws FileIndexException {
		File f = new File("testIndex.idx");
		
		FileIndex fi = new FileIndex(f);
		String one = fi.add("one");
		assertEquals( one, fi.getId("one") );
		
		f.delete();
		
	}



}
