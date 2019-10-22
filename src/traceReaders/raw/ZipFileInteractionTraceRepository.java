/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package traceReaders.raw;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import recorders.FileDataRecorder;

public class ZipFileInteractionTraceRepository extends TraceZipFileRepository<InteractionTrace> {
	
	public ZipFileInteractionTraceRepository(File archive, String storageDirName) throws FileReaderException {
		super(archive,storageDirName);
	}

	protected InteractionTrace getTraceFile( String traceId, String methodName, ZipFile zipFile, ZipEntry dataEntry ) {
		String sessionId = FileDataRecorder.getExecutionId(methodName);
		String threadId = FileDataRecorder.getThreadId(methodName);
		return new ZipFileInteractionTrace( traceId, sessionId, threadId, zipFile, dataEntry, null );
	}



	protected InteractionTrace getTraceFile( String traceId, String methodName, ZipFile zipFile, ZipEntry dataEntry, ZipEntry metaDataEntry ){
		String sessionId = FileDataRecorder.getExecutionId(methodName);
		String threadId = FileDataRecorder.getThreadId(methodName);
		return new ZipFileInteractionTrace( traceId, sessionId, threadId, zipFile, dataEntry, metaDataEntry );
	}


	
	
}
