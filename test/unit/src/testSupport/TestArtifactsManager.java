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
package testSupport;

import java.io.File;

import util.FileSystemUtils;

public class TestArtifactsManager {
	private static final String testArtifactsFolder ="test/unit/artifacts/"; 
	private static final String bugsArtifactsFolder ="test/bugs/artifacts/";	
	/**
	 * Return the given test file
	 * testFIleRelativePath is the  path of the file (with the file name) relative to the test folder. 
	 * @param testFileRelativePath
	 * @return
	 */
	public static File getUnitTestFile(String testFileRelativePath){
		File file = new File(testArtifactsFolder+testFileRelativePath);
		file.getParentFile().mkdirs();
			
		return file;
	}
	
	
		
	
	public static File getBugFile(String testFileRelativePath){
		File file = new File(bugsArtifactsFolder+testFileRelativePath);
		file.getParentFile().mkdirs();
			
		return file;
	}
	
	/**
	 * Return a file which does not exists on the disk.
	 * Be careful if it exists deletes it.
	 * @param testFileRelativePath
	 * @return
	 */
	public static File getNewUnitTestFile(String testFileRelativePath){
		File file = getUnitTestFile(testFileRelativePath);
		FileSystemUtils.deleteRecursively(file);
		
		return file;
	}
	
	/**
	 * Return a bug file which does not exists on disk
	 * 
	 * @param testFileRelativePath
	 * @return
	 */
	public static File getNewBugFile(String testFileRelativePath){
		File file = getBugFile(testFileRelativePath);
		FileSystemUtils.deleteRecursively(file);
		return file;
	}
	
}
