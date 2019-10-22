package traceReaders.raw;

import java.io.IOException;

public class TraceException extends Exception {

	public TraceException(String string) {
		super (string);
	}

	public TraceException(String msg, IOException e) {
		super(msg, e);
	}

	public TraceException(Exception e) {
		super(e);
	}

}
