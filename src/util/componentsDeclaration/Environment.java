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
package util.componentsDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent the system environment, the collection of all the objects that are not part of the monitiored components.
 * 
 * FIXME: create a common interface for Component
 * @author Fabrizio Pastore
 *
 */
public class Environment implements SystemElement {

	private List<Component> components = new ArrayList<Component>();
	private String name;


	public Environment(String name, List<Component> components ) {
		this.name = name;
		this.components.addAll(components);
	}

	public String getName() {
		return name;
	}

	public boolean acceptBytecodeMethodSignature(String bytecodeMethodSignature) {
		for ( Component component : components ){
			if ( component.acceptBytecodeMethodSignature(bytecodeMethodSignature) ){
				return false;
			}
		}
		return true;
	}


	public boolean acceptClass(String packageName, String className) {
		for ( Component component : components ){
			if ( component.acceptClass(packageName,className) ){
				return false;
			}
		}
		return true;
	}


	public boolean acceptFunction(String packageName, String functionName) {
		for ( Component component : components ){
			if ( component.acceptFunction(packageName,functionName) ){
				return false;
			}
		}
		return true;
	}


	public boolean acceptMethod(String packageName, String className,
			String methodSignature) {
		for ( Component component : components ){
			if ( component.acceptMethod(packageName,className,methodSignature) ){
				return false;
			}
		}
		return true;
	}


	public boolean acceptMethodSignature(String completeMethodSignature) {
		for ( Component component : components ){
			if ( component.acceptMethodSignature(completeMethodSignature) ){
				return false;
			}
		}
		return true;
	}

	public boolean acceptPackage(String packageName) {
		for ( Component component : components ){
			if ( component.acceptPackage(packageName) ){
				return false;
			}
		}
		return true;
	}


	//We do not want to serialize the environment component
//	public Environment() {
//		// TODO Auto-generated constructor stub
//	}

}
