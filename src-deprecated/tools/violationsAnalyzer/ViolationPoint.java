package tools.violationsAnalyzer;

public class ViolationPoint {

	private Violation violation;
	private int lineNumber;
	private CallTrace callTrace;

	public ViolationPoint(Violation violation, int lineNumber, CallTrace callTrace){
		this.violation = violation;
		this.lineNumber = lineNumber;
		this.callTrace = callTrace;
	}

	public CallTrace getCallTrace() {
		return callTrace;
	}

	public int getTimeStamp() {
		return lineNumber;
	}

	public Violation getViolation() {
		return violation;
	}
	
	public boolean equals ( Object o ){
		if ( o == this )
			return true;
		if ( ! ( o instanceof ViolationPoint ) )
			return false;
		ViolationPoint rhs = (ViolationPoint)o;
		return ( rhs.lineNumber == this.lineNumber &&
				this.violation.equals(rhs.violation) && 
				this.callTrace.equals(rhs.callTrace) );
				
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
