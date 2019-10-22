package tools.violationsAnalyzer;
import java.util.Iterator;
import java.util.LinkedList;


public class CallTrace {
	
	private String threadID;
	private LinkedList<StackInfo> calls = new LinkedList<StackInfo>();
	
	public CallTrace( String threadName ){
		threadID = threadName;
	}
	
	public void addBottom( String methodName, int line ){
		calls.addFirst(new StackInfo(methodName, line));
	}
	
	public void addTop( String methodName, int line ){
		calls.addLast( new StackInfo(methodName, line) );
	}

	public String getThreadID() {
		return threadID;
	}

	public LinkedList<StackInfo> getCalls() {
		return new LinkedList<StackInfo>( calls );
	}
	
	public Iterator<StackInfo> getCallsIterator() {
		return calls.iterator();
	}

	public void removeLast(int number) {
		for ( int i=0; i<number;++i)
			calls.removeLast();
	}
}
