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

import tools.fshellExporter.CBMCExecutor.ValidationResult;

public class CBMCClaim {

	private String fileName;
	private String claimId;
	private int line;
	private ValidationResult result;
	private boolean bctAssertion;
	

	public boolean isBctAssertion() {
		return bctAssertion;
	}

	public ValidationResult getResult() {
		return result;
	}

	public CBMCClaim(int linePos, String claimId, String fileName) {
		this.line = linePos;
		this.claimId = claimId;
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getClaimId() {
		return claimId;
	}

	public int getLine() {
		return line;
	}

	public void setResult(ValidationResult result) {
		this.result = result;
	}
	
	public String toString(){
		return "[Claim: "+claimId+" file:"+fileName+" line:"+line+" result:"+result+"]";
	}

	public void setBctAssertion(boolean bctAssertion) {
		this.bctAssertion = bctAssertion;
	}

}
