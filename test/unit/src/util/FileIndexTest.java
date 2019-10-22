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
package util;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import util.FileIndex.FileIndexException;

public class FileIndexTest extends TestCase {
	File indexFile = new File ( "test/unit/artifacts/FileIndex/index.idx" );
	private FileIndex fileIndex;
	
	public FileIndexTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fileIndex = new FileIndex(indexFile);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		assertTrue( indexFile.delete() );
	}
	
	public void testAddVoidName() throws IOException, FileIndexException{
		String id = fileIndex.add("");
		fileIndex.save(indexFile);
		
		FileIndex newIndex = new FileIndex(indexFile);
		String name = newIndex.getNameFromId(id);
		
		assertEquals("", name);
		
		assertEquals(id,newIndex.getId(""));
		
	}

}
