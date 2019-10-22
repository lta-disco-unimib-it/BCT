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
package traceReaders.normalized.traceCreation;

import static org.junit.Assert.*;



import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import testSupport.TestArtifactsManager;
import tools.InvariantGenerator;
import util.FileUtil;


import conf.EnvironmentalSetter;

public class SerializedClassUsageInteractionTraceMaintainer_NoStatic_Test {

	

	
	@Test
	public void testNoStaticCalls() throws IOException {
		
		File files = TestArtifactsManager.getUnitTestFile("traceReaders/normalized/traceCreation/SerializedClassUsageInterationTraceMaintainer/noStatic/");
		
		File bctHome = new File(files,"bctData");
		File callTraces = new File(bctHome,"PreprocessingCSVInteraction");
		
		FileUtil.deleteDirectoryContents(callTraces);
		
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		
		
		InvariantGenerator.main( new String[]{"-default"} );
		
		File expectedFiles = new File(files,"expectedResults");
		
		File expectedCallTraces = new File(expectedFiles,"PreprocessingCSVInteraction");
		
		
		
		
		
		checkContentsEqual( expectedCallTraces, callTraces );
	}

	
	private void checkContentsEqual(File expectedFiles, File callTraces) throws IOException {
		
		List<File> expected = Arrays.asList( expectedFiles.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if ( pathname.getName().endsWith(".txt") ){
					return true;
				}
				if ( pathname.getName().endsWith(".idx") ){
					return true;
				}
				return false;
			}
		}) );
		
		ArrayList<File> observed = FileUtil.getDirContents(callTraces);
		
		assertEquals( expected.size(), observed.size() );
		

		
		for ( int i = 0 ; i < expected.size(); i++ ){
			File expectedFile = expected.get(i);
			File observedFile = observed.get(i);
			
			assertTrue ( observed.get(i)+" vs "+ expected.get(i) , FileUtil.checkContentEqual(expectedFile,observedFile) );
		}
	}
}
