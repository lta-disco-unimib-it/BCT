package tools.violationsAnalyzer;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;


public class ProgramGraph {
	public HashMap<String,ThreadElement> threads = new HashMap<String, ThreadElement>();
	NumberFormat nf = NumberFormat.getInstance();
	
	public ProgramGraph(){
		nf.setMinimumIntegerDigits(10);
	}
	
	public void addTraceExecution(ViolationPoint vp, CallTrace trace) {
		ThreadElement threadElement;
		if ( threads.containsKey(trace.getThreadID()) ){
			threadElement = threads.get(trace.getThreadID());
		} else {
			threadElement = new ThreadElement( trace.getThreadID() );
			threads.put(trace.getThreadID(), threadElement);
		}
		
		CallElement curEl = null;
		LinkedList<StackInfo> calls = trace.getCalls();
		//for ( Iterator<StackInfo> it = trace.getCallsIterator(); it.hasNext(); ){
		for( int i = 0; i < calls.size(); ++i ){
			//StackInfo call = it.next();
			StackInfo call = calls.get(i);
			
			String id = call.getMethodName();
			String line = ""+call.getLine();
			
			//add method
			if ( curEl == null ){
				if ( threadElement.hasChild( id ) ){
					curEl = threadElement.getChild(id);
				} else {
					curEl = new CallElement( id );
					threadElement.addChild( id, curEl);
				}
			} else {
				if ( curEl.hasChild(id) ){
					curEl = curEl.getChild(id);
				} else {
					CallElement newEl = new CallElement( id );
					curEl.addChild(id, newEl);
					curEl = newEl;
				}
			}
			int remainingSteps = 2 * ( calls.size()-i ) -1;
			curEl.addReachableViolation( vp, remainingSteps );
			
			
			//add line
			if ( curEl.hasChild(line) ){
				curEl = curEl.getChild(line);
			} else {
				CallElement newEl = new CallElement( line );
				curEl.addChild(line, newEl);
				curEl = newEl;
			}
			
			curEl.addReachableViolation( vp, remainingSteps -1 );
		}
		
		curEl.addViolationId( vp );
		
	}
	
	private String getCallId(String string, int i ){
		
		String res = nf.format(i);
		return string+":"+res;
	}

	public Set<Entry<String, ThreadElement>> getThreads() {
		return threads.entrySet();
	}
	
	
}
