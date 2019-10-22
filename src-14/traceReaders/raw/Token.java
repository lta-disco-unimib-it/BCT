package traceReaders.raw;

import java.io.Serializable;


/**
 * class used to normalize interaction traces
 * 
 * Encapsulate a string with the information about a method call
 * 
 * e.g.
 * 
 * pack.AClass.method()B
 * 
 * 
 *
 */
public class Token implements Serializable {
	
	private String methodExecutionSignature = "";
	private int id;
	private TokenMetaData tokenMetaData;
	
	
	public TokenMetaData getTokenMetaData() {
		return tokenMetaData;
	}

	public void setTokenMetaData(TokenMetaData tokenMetaData) {
		this.tokenMetaData = tokenMetaData;
	}

	/**
	 * This constructor is used for tokens extracted from DB traces.
	 * The id indicates the id of the method call (idBeginEndExecMethod in the BCT DB).
	 * 
	 * @param id
	 * @param methodSignature
	 */
	public Token (int id, String methodSignature) {
		this.id = id;
		this.methodExecutionSignature = methodSignature;
	}
	
	/**
	 * Creates a token for the following method invocation point. 
	 * The method invocation point is a string which corresponds to a method signature. 
	 * The methodInvocationPoint ends with B or E indicating whether it indicates the begin of a method call or the end of 
	 * a method call. 
	 * 
	 * This constructor is used for tokens extracted from trace files.
	 * 
	 * @param methodInvocationPoint
	 */
	public Token (String methodInvocationPoint) {
		this.methodExecutionSignature = methodInvocationPoint;
	}
	
	public String getTokenValue() {
		return methodExecutionSignature;
	}
	
	public int getId() {
		return id;
	}
	
	public String getMethodSignature(){
		return methodExecutionSignature.substring(0, methodExecutionSignature.length()-1);
	}

	public String toString(){
		return getTokenValue();
	}
	

}
