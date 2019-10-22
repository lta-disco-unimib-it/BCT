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
package recorders;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.HashMap;

import org.junit.BeforeClass;

import recorders.TestConcurrentRecording.RecordingType;
import testSupport.TestArtifactsManager;
import util.FileUtil;

/**
 * 
 * @author usiusi
 *
 */
public abstract class TestConcurrentRecordingFile extends TestConcurrentRecording {
	

	protected static HashMap<RecordingType,File> tracesDirs = new HashMap<RecordingType, File>();
	protected static File bctHome;
	
	
	public static void setupDirs(String path) throws InterruptedException {
		bctHome = TestArtifactsManager.getUnitTestFile(path);
		for ( RecordingType recordingType : RecordingType.values() ){
		File tracesDir = TestArtifactsManager.getUnitTestFile(path+"/data-"+recordingType.name());
		
		if ( tracesDir.exists() ){
			FileUtil.deleteRecursively(tracesDir);
		}
		
		assertFalse( tracesDir.exists() );
		
		tracesDir.mkdirs();
		
		tracesDirs.put(recordingType, tracesDir);
		}
		
		
	}
	
	@Override
	public File getBctHome(RecordingType recordingType) {
		return bctHome;
	}
	
	
	protected static File getTracesDir(RecordingType recordingType) {
		return tracesDirs.get(recordingType);
	}
}
