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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void testCleanPath(){
		
		assertEquals( "file",  FileUtil.getCleanPath("Debug/../file") );
		assertEquals( "file/",  FileUtil.getCleanPath("Debug/../file/") );
		assertEquals( "Debug/file",  FileUtil.getCleanPath("Debug/./file") );
		assertEquals( "Debug/a/file",  FileUtil.getCleanPath("Debug/a/file") );
		assertEquals( "",  FileUtil.getCleanPath("") );
		assertEquals( "Debug/",  FileUtil.getCleanPath("Debug/") );
		assertEquals( "file",  FileUtil.getCleanPath("file") );
	}
	
	@Test
	public void testRealativePath() throws IOException{
		assertEquals("../t.txt", FileUtil.getRelativePath( new File ("/a/b/c"), new File("/a/b/t.txt" ) ) );
		assertEquals("t.txt", FileUtil.getRelativePath( new File ("/a/b"), new File("/a/b/t.txt" ) ) );
		assertEquals("../../../t.txt", FileUtil.getRelativePath( new File ("/a/b/c"), new File("/t.txt" ) ) );
		
		
		assertEquals("../../../x/t.txt", FileUtil.getRelativePath( new File ("/a/b/c/d/e"), new File("/a/b/x/t.txt" ) ) );
	}
}
