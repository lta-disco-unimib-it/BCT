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
package testSupport.check.ioInvariantParser;

public class MyClass {
	private		int intValue = 4;
	private		double doubleValue = 4.0;
	private		String stringValue = "stringa";
	private		MyClas2 m2 = new MyClas2();
	
	public int getIntValue() {
		return intValue;
	}
	
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	
	public String getStringValue() {
		return stringValue;
	}
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
}
