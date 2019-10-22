package traceReaders.raw;

import java.io.File;

import recorders.FileDataRecorder;

public class FileInteractionTraceRepository extends TraceFileRepository {
	
	public FileInteractionTraceRepository(File storageDir) {
		super(storageDir);
	}

	protected Object getTraceFile( String traceId, String methodName, File dataFile ) {
		String sessionId = FileDataRecorder.getExecutionId(methodName);
		String threadId = FileDataRecorder.getThreadId(methodName);
		return new FileInteractionTrace( traceId, sessionId, threadId, dataFile, null );
	}



	protected Object getTraceFile( String traceId, String methodName, File dataFile, File metaFile ){
		String sessionId = FileDataRecorder.getExecutionId(methodName);
		String threadId = FileDataRecorder.getThreadId(methodName);
		return new FileInteractionTrace( traceId, sessionId, threadId, dataFile, metaFile );
	}
	
}
