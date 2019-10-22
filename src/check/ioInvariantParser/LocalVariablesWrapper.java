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
package check.ioInvariantParser;

import java.util.List;
import java.util.NoSuchElementException;

public class LocalVariablesWrapper {
	private String variableName; 
	private List<String> fields;
	private Target target;
	
	public String getVariableName(){
		return variableName;
	}
	
	public LocalVariablesWrapper(String variableName, List<String> fields,
			Target target) {
		this.variableName = variableName;
		this.fields = fields;
		this.target = target;
	}

	public Object getValueOfChild(String memberName) throws NoSuchElementException{
		String completeChildName = variableName+"."+memberName;
		

			Object value = target.getLocalVariable(completeChildName);
			if ( value instanceof LocalVariablesWrapper ){
				return new LocalVariablesWrapper(completeChildName, fields, target);
			} else {
				return value;
			}
		
	}
	
	
	@Override
	public boolean equals(Object arg0) {
		if ( ! ( arg0 instanceof LocalVariablesWrapper ) ){
			return false;
		}
		
		LocalVariablesWrapper arg = (LocalVariablesWrapper) arg0;
		if ( variableName == null ){
			if ( arg.variableName == null ){
				return true;
			} else {
				return false;
			}
		}
		return ( variableName.equals( arg.variableName ) );
	}

	public String toString(){
		return "!NULL";
	}

}
