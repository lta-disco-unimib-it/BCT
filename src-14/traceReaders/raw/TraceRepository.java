package traceReaders.raw;

import java.util.List;

public interface TraceRepository {

	public abstract Object getRawTrace(String methodName) throws FileReaderException;

	public abstract Object getRawTraceFromId(String id) throws FileReaderException;

	public abstract List getRawTraces() throws FileReaderException;

}