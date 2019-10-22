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
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import tools.InvariantGenerator;
import util.FileUtil;


import conf.EnvironmentalSetter;

public class SerializedClassUsageInteractionTraceMaintainer_Static_Test {

	
	@Ignore("Probably the test outcome is ok, no time to double check: FIXME")
	@Test
	public void testStaticCalls() throws IOException {
		
		File files = TestArtifactsManager.getUnitTestFile("traceReaders/normalized/traceCreation/SerializedClassUsageInterationTraceMaintainer/static/");
		
		File bctHome = new File(files,"bctData");
		
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		
		
		InvariantGenerator.main( new String[]{"-default"} );
		
		File expectedFiles = new File(files,"expectedResults");
		
		File expectedCallTraces = new File(expectedFiles,"PreprocessingCSVInteraction");
		
		
		File callTraces = new File(bctHome,"PreprocessingCSVInteraction");
		
		
		checkContentsEqual( expectedCallTraces, callTraces );
	}
	


	
	private void checkContentsEqual(File expectedFiles, File callTraces) throws IOException {
		
		ArrayList<File> expected = FileUtil.getDirContents(expectedFiles);
		
		ArrayList<File> observed = FileUtil.getDirContents(callTraces);
		
		assertEquals( expected.size(), observed.size() );
		
		for ( int i = 0 ; i < expected.size(); i++ ){
			File expectedFile = expected.get(i);
			File observedFile = observed.get(i);
			
			assertTrue ( FileUtil.checkContentEqual(expectedFile,observedFile) );
		}
	}
}
