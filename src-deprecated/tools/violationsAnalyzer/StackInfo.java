package tools.violationsAnalyzer;

public class StackInfo {
	private String methodName;
	private int line;

	public StackInfo( String methodName, int line ){
		this.methodName = methodName;
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public String getMethodName() {
		return methodName;
	}
}
