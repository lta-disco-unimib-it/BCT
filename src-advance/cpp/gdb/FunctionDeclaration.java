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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class FunctionDeclaration {
	
	public String getName() {
		return name;
	}

	public String getReturnType() {
		return returnType;
	}

	private String name;
	private String returnType;
	private int parametersNumber = 0;
	private Set<String> pointerArgs = new HashSet<String>();
	private Set<String>  scalarArgs = new HashSet<String>();
	private Set<String>  referenceArgs = new HashSet<String>();
	private int startLine;
	private int endLine;
	
	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public Set<String> getReferenceArgs() {
		return referenceArgs;
	}

	public Set<String> getScalarArgs() {
		return scalarArgs;
	}

	public int getParametersNumber() {
		return parametersNumber;
	}

	public FunctionDeclaration(String name, String returnType) {
		this(name, returnType, 0, null);
	}
	
	public FunctionDeclaration(String name, String returnType, int parametersNumber, List<Parameter> parameters) {
		super();
		this.name = name;
		this.returnType = returnType;
		if ( returnType.startsWith("*") ){
			returnType = "void"+returnType;
		}
		this.parametersNumber = parametersNumber;
		this.allArgs = parameters;
	}

	@Override
	public boolean equals(Object obj) {
		if ( !(  obj instanceof FunctionDeclaration ) ){
			return false;
		}
		FunctionDeclaration rhs = (FunctionDeclaration) obj;
		
		if ( name == null && rhs.name != null ){
			return false;
		}
		
		if ( name != null && ( ! name.equals(rhs.name) ) ){
			return false;
		}
		
		if ( returnType == null && rhs.returnType != null ){
			return false;
		}
		
		if ( returnType != null && ( ! returnType.equals(rhs.returnType) ) ){
			return false;
		}
		
		if ( parametersNumber != rhs.parametersNumber ){
			return false;
		}
		
		if ( ! pointerArgs.equals(rhs.pointerArgs) ){
			return false;
		}
		
		if ( this.allArgs != null && rhs.allArgs != null ){
			if ( ! allArgs.equals(rhs.allArgs) ){
				return false;
			}
		} else {
			if ( allArgs != rhs.allArgs ){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return (returnType+";"+name+";"+parametersNumber+";"+String.valueOf(allArgs)).hashCode();
	}

	@Override
	public String toString() {
		return "["+returnType+";"+name+"]";
	}

	public void setPointerArgs(Set<String> pointerArgs) {
		this.pointerArgs.clear();
		this.pointerArgs.addAll ( pointerArgs );
	}

	public Set<String> getPointerArgs() {
		return pointerArgs;
	}

	public void setScalarArgs(Set<String> scalarArgs) {
		this.scalarArgs.clear();
		this.scalarArgs.addAll ( scalarArgs );
	}

	public void setReferenceArgs(Set<String> referenceArgs) {
		this.referenceArgs.clear();
		this.referenceArgs.addAll ( referenceArgs );
	}

	public void setStartLine(int line) {
		startLine = line;
	}

	public void setEndLine(int line) {
		endLine = line;
	}

	public void addLocalVariable(LocalVariableDeclaration varDecl) {
		localVariables.add(varDecl);
	}

	public List<LocalVariableDeclaration> getLocalVariables() {
		return localVariables;
	}

	private List<LocalVariableDeclaration> localVariables = new ArrayList<LocalVariableDeclaration>();
	private boolean takesVarArgs;
	private List<Parameter> allArgs;

	public boolean takesVarArgs() {
		return takesVarArgs;
	}

	public void setTakesVarArgs(boolean takesVarArgs) {
		this.takesVarArgs = takesVarArgs;
	}

	public List<Parameter> getAllArgs() {
		if ( allArgs == null ){
			return Collections.EMPTY_LIST;
		}
		return allArgs;
	}
	
	
	

}
