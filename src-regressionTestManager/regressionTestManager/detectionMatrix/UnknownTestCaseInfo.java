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
package regressionTestManager.detectionMatrix;

import regressionTestManager.tcData.TestCaseInfo;

public class UnknownTestCaseInfo extends TestCaseInfo {

	public UnknownTestCaseInfo( String name) {
		super(name, name, null);
	}

	public boolean equals(Object o){
		if ( o == this )
			return true;
		if ( ! ( o instanceof UnknownTestCaseInfo ) ){
			return false;
		}
		UnknownTestCaseInfo rhs = (UnknownTestCaseInfo) o;
		if ( name == null ){
			if ( rhs.name == null )
				return true;
			return false;
		}
		return name.equals(rhs.name);
	}
	
	public int hashCode(){
		return name.hashCode();
	}
}
