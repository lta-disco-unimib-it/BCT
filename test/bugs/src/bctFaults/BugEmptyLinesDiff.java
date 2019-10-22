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
package bctFaults;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import cpp.gdb.FileChangeInfo;
import cpp.gdb.ModifiedFunctionsDetector;
import cpp.gdb.FileChangeInfo.Delta;

public class BugEmptyLinesDiff {

	@Test
	public void testBug(){
		
		List<File> originalSoftwareFolders = new ArrayList<File>();
		originalSoftwareFolders.add(new File("test/bugs/artifacts/emptyLinesBug/V1/"));
		
		List<File> modifiedSoftwareFolders= new ArrayList<File>();
		modifiedSoftwareFolders.add(new File("test/bugs/artifacts/emptyLinesBug/V2/"));
		
		Set<String> commonFiles = new HashSet<String>();
		commonFiles.add("/find.c");
		List<FileChangeInfo> diffs_ = ModifiedFunctionsDetector.extractDiffs( commonFiles, originalSoftwareFolders.get(0), modifiedSoftwareFolders.get(0) );
		
		boolean found = containsDiff(diffs_, "find.c", 1148);
		assertTrue("An expected difference was not found in line 1148", found);
		
	}
	
	
	private boolean containsDiff(List<FileChangeInfo> diffs_, String fileName, int line) {
		boolean found = false;
		for ( FileChangeInfo diff : diffs_ ){
			String name = diff.getFile().getName();
			System.out.println(name);
			if ( name.equals(fileName) ) {
				for ( Delta d : diff.getDeltas() ){
					if ( d.getStart() == line ){
						found = true;
					}
				}
			}
		}
		return found;
	}
}
