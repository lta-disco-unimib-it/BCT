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

import org.junit.BeforeClass;

import traceReaders.raw.FileTracesReader;
import traceReaders.raw.TracesReader;
import conf.BctSettingsException;

public class TestConcurrentRecordingOptimizedFileDataRecorder extends
		TestConcurrentRecordingFile {

	@BeforeClass
	public static void classSetup() throws InterruptedException {
		setupDirs("recorders/concurrentRecordingOptimizedFileDataRecorder/");
	}

	private FileOptimizedDataRecorder recorder;
	private FileTracesReader reader;

	@Override
	public TracesReader getReader(RecordingType recordingType)
			throws BctSettingsException {
		reader = new FileTracesReader(getTracesDir(recordingType),
				FileDataRecorder.DEFAULT_IO_DIR,
				FileDataRecorder.DEFAULT_INTERACTION_DIR, false);
		return reader;
	}

	@Override
	public DataRecorder getRecorder(RecordingType recordingType) {
		recorder = new FileOptimizedDataRecorder(getTracesDir(recordingType));

		return recorder;
	}

	@Override
	public void recordingStart() throws Exception {
		recorder.recordingStart();
	}

	@Override
	public void recordingEnd() throws Exception {
		recorder.recordingEnd();
	}

	@Override
	public void testEnd() throws Exception {

	}

}
