package traceReaders.raw;

import traceReaders.TraceReaderException;

public class FileReaderException extends TraceReaderException {

	public FileReaderException(String string) {
		super(string);
	}

	public FileReaderException(Exception e) {
		super(e);
	}

}
