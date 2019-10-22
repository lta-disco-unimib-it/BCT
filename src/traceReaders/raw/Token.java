/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package traceReaders.raw;

import java.io.Serializable;

import tools.TokenMetaData;

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
