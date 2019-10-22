package traceReaders;


public class TraceReaderException extends Exception {

	public TraceReaderException(String string) {
		super ( string );
	}

	public TraceReaderException(Exception e) {
		super(e);
	}

	

}
