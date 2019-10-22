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

import java.io.File;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import conf.BctSettingsException;

import recorders.TestConcurrentRecording.RecordingType;
import testSupport.TestArtifactsManager;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.TracesReader;
import util.FileUtil;

import static org.junit.Assert.*;


public class TestConcurrentRecordingFileDataRecorder extends
		TestConcurrentRecordingFile {
	

	@BeforeClass
	public static void classSetup() throws InterruptedException {
		setupDirs("recorders/concurrentRecordingFileDataRecorder");
		
		
		
	}

	private FileDataRecorder recorder;
	
	@Override
	public File getBctHome(RecordingType recordingType) {
		return bctHome;
	}

	@Override
	public TracesReader getReader(RecordingType recordingType) throws BctSettingsException {
		return new FileTracesReader(  getTracesDir(recordingType), FileDataRecorder.DEFAULT_IO_DIR, FileDataRecorder.DEFAULT_INTERACTION_DIR, false);
	}

	@Override
	public DataRecorder getRecorder(RecordingType recordingType) {
		recorder = new FileDataRecorder(getTracesDir(recordingType));
		
		return recorder;
	}


	@Override
	public void recordingStart() throws Exception {
		
	}

	@Override
	public void recordingEnd() throws Exception {
		
	}

	@Override
	public void testEnd() throws Exception {
		// TODO Auto-generated method stub
		
	}



}
