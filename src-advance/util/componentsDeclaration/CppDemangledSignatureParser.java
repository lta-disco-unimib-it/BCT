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
package util.componentsDeclaration;

import tools.gdbTraceParser.BctCNamesUtil;
import cpp.gdb.FunctionMonitoringDataTransformerDemangled;


public class CppDemangledSignatureParser implements SignatureParser {
	FunctionMonitoringDataTransformerDemangled transf = new FunctionMonitoringDataTransformerDemangled();
	@Override
	public String getPackageNameFromCompleteMethodSignature(
			String completeMethodSignature) {
		return transf.getPackageName(completeMethodSignature, null);
	}

	@Override
	public String getMethodSignatureFromCompleteMethodSignature(
			String completeMethodSignature) {
		return transf.getMethodSignature(completeMethodSignature, null);
	}

	@Override
	public String getCompleteMethodSignatureFromBytecodeMethodSignature(
			String bytecodeMethodSignature) {
		
		if ( bytecodeMethodSignature.charAt(bytecodeMethodSignature.length()-1) == BctCNamesUtil.ADDITIONAL_CHAR_FOR_NONDETERMINISTIC_NAMES) {
			bytecodeMethodSignature = bytecodeMethodSignature.substring(0, bytecodeMethodSignature.length()-1);
		}
		
		String method = getMethodSignatureFromJavaBytecodeSignature(bytecodeMethodSignature);
		String className = getClassNameFromJavaBytecodeSignature(bytecodeMethodSignature);
		String packageName = getPackageFromJavaBytecodeSignature(bytecodeMethodSignature);
		
		StringBuffer completeSignatureBuffer = new StringBuffer();
		if (packageName.length()>0){
			completeSignatureBuffer.append(packageName);
			completeSignatureBuffer.append("::");
		}
		if ( className.length()>0){
			completeSignatureBuffer.append(className);
			completeSignatureBuffer.append("::");
		}
		completeSignatureBuffer.append(method);
		
		return completeSignatureBuffer.toString();
	}
	
	
	private enum SignatureParserOutput { methodName, className, packageName };
	
	public String getMethodSignatureFromJavaBytecodeSignature(
			String completeMethodSignature) {
		return parseJavaBytecodeMethodSiganture(completeMethodSignature, SignatureParserOutput.methodName);
	}
	
	public String getClassNameFromJavaBytecodeSignature(
			String completeMethodSignature) {
		return parseJavaBytecodeMethodSiganture(completeMethodSignature, SignatureParserOutput.className);
	}
	
	public String getPackageFromJavaBytecodeSignature(
			String completeMethodSignature) {
		return parseJavaBytecodeMethodSiganture(completeMethodSignature, SignatureParserOutput.packageName);
	}
	
	
	public String parseJavaBytecodeMethodSiganture(String functionName, SignatureParserOutput requestedOutput) {
//		String functionName = func.getDemangledName();
		
		int parenthesisStart = functionName.indexOf('(');
		
		String functionNameNoParenthesis;
		if ( parenthesisStart < 0 ){
			functionNameNoParenthesis = functionName;	
		} else {
			functionNameNoParenthesis = functionName.substring(0, parenthesisStart);
		}
		
		int methodStart = functionNameNoParenthesis.lastIndexOf('.');
		
		if ( methodStart < 0 ){
			switch ( requestedOutput ){
			case methodName:
				return functionName;
			case className:
				return "";
			case packageName:
			return "";
			default:
				throw new IllegalStateException("New signature chunk type not handled!!!");
			}
		}
		
		if ( requestedOutput == SignatureParserOutput.methodName ){
			return functionName.substring(methodStart+1);
		}
		
		functionName = functionName.substring(0, methodStart );
		
		int templateStart = functionName.indexOf('<');
		String classNameNoTemplate;
		if ( templateStart > 0 ){
			classNameNoTemplate = functionName.substring(0, templateStart );	
		} else {
			classNameNoTemplate = functionName;
		}
		
		int classStart = classNameNoTemplate.lastIndexOf("::");
		
		String packageName;
		String className;
		if ( classStart > 0 ){
			className = functionName.substring(classStart+1);	
			packageName = functionName.substring(0,classStart);
		} else {
			className = functionName;
			packageName = "";
		}

		if ( requestedOutput == SignatureParserOutput.className ){
			return className;
		}

		return packageName;
	}
	
	

	@Override
	public String getClassNameFromCompleteMethodSignature(
			String completeMethodSignature) {
		// TODO Auto-generated method stub
		return null;
	}

}
