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

public class Parameter {
	private String name;
	private String type;
	private boolean pointer;
	private boolean reference;
	private int pointerOperatorsNum;
	
	public Parameter(String type, String name, boolean pointer, boolean reference, int pointerOperatorsNum ) {
		super();
		this.name = name;
		this.type = type;
		this.pointer = pointer;
		this.reference = reference;
		this.pointerOperatorsNum = pointerOperatorsNum;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isPointer() {
		return pointer;
	}

	public boolean isReference() {
		return reference;
	}

	public int getPointerOperatorsNum() {
		return pointerOperatorsNum;
	}

	boolean isUnsignedInt() {
		if ( "unsigned int".equals(type) ){
			return true;
		}
		if ( "uint".equals(type) ){
			return true;
		}
		return false;
	}
	
	boolean isInt() {
		return "int".equals(type);
	}
	
	boolean  isFloat() {
		return "float".equals(type);
	}
	
	boolean  isDouble() {
		return "double".equals(type);
	}
	
	boolean  isLong() {
		return "long".equals(type);
	}
	
	
	public boolean equals( Object rhs ){
		if ( ! ( rhs instanceof Parameter ) ){
			return false;
		}
		
		Parameter p = (Parameter) rhs;
		
		if ( ! p.type.equals(type) ){
			return false;
		}
		
		if ( ! p.name.equals(name) ){
			return false;
		}
		
		if ( pointer != p.pointer ){
			return false;
		}
		
		if ( reference != p.reference ){
			return false;
		}
		
		if ( p.pointerOperatorsNum != pointerOperatorsNum ){
			return false;
		}
		
		return true;
	}

	public boolean isBuiltinType() {
		return isBuiltinType(type);
	}
	
	public String getMainType() {
		return getMainType(type);
	}

	public static boolean isBuiltinType(String type) {
		

		String endType = getMainType(type);

		switch ( endType ) {
		case "int":
		case "double":
		case "bool":
		case "long":
		case "float":
		case "byte":
		case "size_t":
		case "char":
		case "uint32_t":
			return true;
		}

		return false;
	}

	public static String getMainType(String type) {
		int pos = type.lastIndexOf(' ');
		String endType = type;
		if ( pos > 0 ){
			endType = type.substring( pos+1 );
		}
		return endType.trim();
	}

	public void setName(String name) {
		this.name = name;
	}
	
}