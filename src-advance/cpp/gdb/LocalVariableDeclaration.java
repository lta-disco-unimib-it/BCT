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
package cpp.gdb;


public class LocalVariableDeclaration {

	private int lineNo;
	private int scopeEndLine = -1;
	
	public int getScopeEndLine() {
		return scopeEndLine;
	}

	private String name;

	public int getLineNo() {
		return lineNo;
	}

	public String getName() {
		return name;
	}

	public LocalVariableDeclaration(String name, int lineNo) {
		this.name = name;
		this.lineNo = lineNo;
	}
	
	public LocalVariableDeclaration(String name, int lineNo, int scopeEndLine) {
		this(name,lineNo);
		this.scopeEndLine = scopeEndLine;
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! ( obj instanceof LocalVariableDeclaration ) ){
			return false;
		}
		LocalVariableDeclaration rhs = (LocalVariableDeclaration) obj;
		if ( name == null && rhs.name != null ){
			return false;
		}
		
		if ( ! name.equals(rhs.name) ){
			return false;
		}
		
		if( ! ( lineNo == rhs.lineNo ) ){
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "[DECLARATION "+name+":"+lineNo+"]";
	}

	public boolean inScopeForMonitoring(int lineNo) {
		if ( scopeEndLine > 0 ){
			if ( lineNo > scopeEndLine ){
				return false;
			}
		}
		
		return lineNo > this.lineNo;
	}

	
}
