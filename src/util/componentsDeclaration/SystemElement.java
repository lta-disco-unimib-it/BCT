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

/**
 * Interface for the elements of the system that can be monitored, for example components and environment
 * 
 * @author Fabrizio Pastore
 *
 */
public interface SystemElement {

	/**
	 * Returns the name of this element
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns true if the given signature is part of this element
	 * 
	 * @param bytecodeMethodSignature
	 * @return
	 */
	public abstract boolean acceptBytecodeMethodSignature(
			String bytecodeMethodSignature);

	/**
	 * Returns true if the given class is part of this element
	 * @param packageName
	 * @param className
	 * @return
	 */
	public abstract boolean acceptClass(String packageName, String className);

	/**
	 * Returns true if the given function is part of this element
	 * @param packageName
	 * @param functionName
	 * @return
	 */
	public abstract boolean acceptFunction(String packageName,
			String functionName);
	/**
	 * Returns true if the given method is part of this element
	 * @param packageName
	 * @param className
	 * @param methodSignature
	 * @return
	 */
	public abstract boolean acceptMethod(String packageName, String className,
			String methodSignature);

	/**
	 * Returns true if the given method is part of this element
	 * @param completeMethodSignature
	 * @return
	 */
	public abstract boolean acceptMethodSignature(String completeMethodSignature);

	/**
	 * Returns true if the given package is part of this element
	 * @param packageName
	 * @return
	 */
	public abstract boolean acceptPackage(String packageName);

}