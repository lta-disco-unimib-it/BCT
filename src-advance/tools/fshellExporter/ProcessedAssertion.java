/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
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
package tools.fshellExporter;

public class ProcessedAssertion {
	private String relativePath;
	private int lineNo;
	private String assertion;
	private String functionName;
	private int sourcelineNo;
	
	public ProcessedAssertion(String relativePath, int lineNo,
			String assertion) {
		this(relativePath, lineNo, assertion, null );
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public int getLineNo() {
		return lineNo;
	}

	public String getAssertion() {
		return assertion;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	public String toString(){
		return "[ProcessedAssertion"+
				" relativePath:"+relativePath+
				" lineNo:"+lineNo+
				" sourceLineNo:"+sourcelineNo+
				" functionName:"+functionName+
				" assertion:"+assertion+"]";
	}

	public ProcessedAssertion(String relativePath, int lineNo,
			String assertion, String functionName) {
		super();
		this.relativePath = relativePath;
		this.lineNo = lineNo;
		this.assertion = assertion;
		this.functionName = functionName;
	}
	
	public String toStringWithLine(){
		return relativePath+"\t"+sourcelineNo+"\t"+assertion;
	}
	
	public String toStringWithFunctionNameOrLine(){
		String func;
		if ( functionName == null ){
			func = String.valueOf(lineNo);
		} else {
			func = functionName;
		}
		return relativePath+"\t"+func+"\t"+assertion;
	}

	public void setSourceLineNo(int lineNo) {
		this.sourcelineNo = lineNo;
	}

	public int getSourcelineNo() {
		return sourcelineNo;
	}
	
}