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
package cpp.gdb.coverage;

import java.io.Serializable;


public class BranchId implements Serializable, Comparable<BranchId> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int lineNo;
	private int branchNo;
	
	public BranchId(int lineNo, int branchNo) {
		super();
		this.lineNo = lineNo;
		this.branchNo = branchNo;
	}

	
	
	@Override
	public boolean equals(Object obj) {
		if ( ! ( obj instanceof BranchId) ) {
			return false;
		}
		BranchId rhs = (BranchId) obj;
		
		return ( rhs.lineNo == lineNo ) && ( rhs.branchNo == branchNo );
	}



	@Override
	public int hashCode() {
		return (""+lineNo + branchNo).hashCode();
	}



	@Override
	public int compareTo(BranchId o) {
		int lineComp = lineNo - o.lineNo;
		
		if ( lineComp != 0 ){
			return lineComp;
		}
		
		return branchNo-o.branchNo;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getBranchNo() {
		return branchNo;
	}
	
	public String toString(){
		return "branch-"+lineNo+"-"+branchNo;
	}
}