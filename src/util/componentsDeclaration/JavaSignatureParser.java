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
 * This class provides functionalities to parse different java signatures elements:
 * 
 * MethodSignature: method name + method parameters 
 * 						(we manageboth the java syntax and the bytecode syntax)
 * 						myMeth(I;Ljava.lang.Object)
 * 						myMeth(int , java.lang.Object)
 * CanonicalClassName: package name + class name
 * 					pack.MyClass
 * 					pack.MyClass$InnerClass
 * CompleteMethodSiganture: canonical class name + method signature
 * 						pack.MyClass.myMeth(I;Ljava.lang.Object)
 * 						pack.MyClass.myMeth(int ,java.lang.Object)
 * BytecodeMethodSignature: method name + method parameters+ return value
 * 				pack.MyClass.myMeth((I;Ljava.lang.Object)V)
 * 
 *   
 * @author Fabrizio Pastore
 *
 */
public class JavaSignatureParser implements SignatureParser {

	public static String getCanonicalClassNameFromCompleteMethodSignature(String completeMethodSignature) {
		String methodCompleteName = getCompleteMethodNameFromCompleteMethodSignature(completeMethodSignature);
		
		int methodSeparator = methodCompleteName.lastIndexOf('.');
		
		return methodCompleteName.substring(0, methodSeparator);
		
		
	}
	
	/**
	 * Return the complete name of a method, it accepts also method signatures not correct
	 * 
	 * Examples
	 * 
	 * pack.MyClass.method(int) => pack.MyClass.method
	 * 
	 * pack.MyClass.method() => pack.MyClass.method
	 * 
	 * pack.MyClass.method => pack.MyClass.method
	 * 
	 * MyClass.method(int) => MyClass.method
	 * 
	 * @param completeMethodSignature
	 * @return
	 */
	public static String getCompleteMethodNameFromCompleteMethodSignature(String completeMethodSignature) {
		int parenthesis = completeMethodSignature.indexOf('(');
		
		if ( parenthesis < 0 ){
			parenthesis = completeMethodSignature.length();
		}
		
		return completeMethodSignature.substring(0, parenthesis);
	}
	
	/**
	 * Returns the class name given the complete method signature.
	 * 
	 * If the signature does not include parenthesis it just go on with the matching on the method name.
	 * 
	 * 
	 * @param completeMethodSignature
	 * @return
	 */
	@Override
	public String getClassNameFromCompleteMethodSignature(String completeMethodSignature) {
		String methodCompleteName = getCompleteMethodNameFromCompleteMethodSignature(completeMethodSignature);
		int methodSeparator = methodCompleteName.lastIndexOf('.');
		int classSeparator = methodCompleteName.substring(0, methodSeparator).lastIndexOf('.');
		
		String className = methodCompleteName.substring(classSeparator+1, methodSeparator);
		return className;
		
	}
	
	/**
	 * Returns a complete method signature given a bytecode method signature
	 * 
	 * @param bytecodeMethodSignature
	 * @return
	 */
	@Override
	public String getCompleteMethodSignatureFromBytecodeMethodSignature(String bytecodeMethodSignature) {
		int openParenthesis = bytecodeMethodSignature.indexOf('(');
		int closedParenthesis = bytecodeMethodSignature.indexOf(')');
		
		if ( openParenthesis == -1 ){
			return bytecodeMethodSignature;//FIXME: I added this to manage C++ mangled names wthout parenthesis, probably would be better to extend the component types
		}
		
		String parameters = bytecodeMethodSignature.substring(openParenthesis+1, closedParenthesis+1);
		
		String completeMethodName = bytecodeMethodSignature.substring(0, openParenthesis);
		
		if ( ! parameters.startsWith("(") ){ //FIXME: I added this to manage C++ mangled names, probably would be better to extend the component types
			return bytecodeMethodSignature;
		}
		
		return completeMethodName+parameters;
		
		
	}
	
	@Override
	public String getMethodSignatureFromCompleteMethodSignature(String completeMethodSignature) {
		String methodCompleteName = getCompleteMethodNameFromCompleteMethodSignature(completeMethodSignature);
		
		int methodSeparator = methodCompleteName.lastIndexOf('.');
		
		return completeMethodSignature.substring(methodSeparator+1);
	}

	
	public static String getPackageNameFromCanonicalClassName(String canonicalClassName) {
		String packageName;
		
		int classSeparator = canonicalClassName.lastIndexOf('.');
		if ( classSeparator >= 0 ){
			packageName = canonicalClassName.substring(0,classSeparator);
		} else {
			packageName = "";
		}
		
		return packageName;
	}
	
	@Override
	public String getPackageNameFromCompleteMethodSignature( String completeMethodSignature){
		String canonicalClassName = getCanonicalClassNameFromCompleteMethodSignature(completeMethodSignature);
		return getPackageNameFromCanonicalClassName(canonicalClassName);
	}

	public static String getMethodNameFromCompleteMethodSignature(String completeMethodSignature) {
		int parenthesis = completeMethodSignature.indexOf('(');
		String methodCompleteName = completeMethodSignature.substring(0, parenthesis);
		int methodSeparator = methodCompleteName.lastIndexOf('.');
		
		return completeMethodSignature.substring(methodSeparator+1,parenthesis);
	}
	
	
}
