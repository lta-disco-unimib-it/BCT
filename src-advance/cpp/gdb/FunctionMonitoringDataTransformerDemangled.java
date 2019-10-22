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

//import it.unimib.disco.lta.alfa.logging.Logger;

import java.io.File;

public class FunctionMonitoringDataTransformerDemangled implements
		FunctionMonitoringDataTransformer {


	
	@Override
	public String getClassName(FunctionMonitoringData func) {
		return getClassName(func.getDemangledName(), func.getSourceFileLocation());
	}
	
	
	public String getClassName(String demangledName, String location) {	
		return parseSignature(demangledName, location, SignatureParserOutput.className);
	}
	
	public String getClassNameNoTemplates(FunctionMonitoringData func) {
		return getClassNameNoTemplates(func.getDemangledName(), func.getSourceFileLocation());
	}
	
	
	public String getClassNameNoTemplates(String demangledName, String location) {	
		return parseSignature(demangledName, location, SignatureParserOutput.classNameNoTemplates);
	}

	private String getSourceLocationAsClass(String srcLocation) {

		
		if ( srcLocation != null ){
			File file = new File ( srcLocation );
			return file.getName();
		}
		return "";
	}
	
	private String getSourceLocationAsPackage(String srcLocation) {

		if ( srcLocation != null ){
			File file = new File ( srcLocation );
			String parent = file.getParent();
			if ( parent != null ){
				return parent;
			}
		}
		return "";
	}

	@Override
	public String getMethodSignature(FunctionMonitoringData func) {
		return getMethodSignature(func.getDemangledName(), func.getSourceFileLocation());
	}
	
	public String getMethodSignature(String functionDemangledName, String sourceFileLocation) {
		return parseSignature(functionDemangledName, sourceFileLocation, SignatureParserOutput.methodName);
	}

	public String getMethodName(FunctionMonitoringData func) {
		return getMethodName(func.getDemangledName(), func.getSourceFileLocation());
	}
	
	public String getMethodName(String functionDemangledName, String sourceFileLocation) {
		String methodSignature = parseSignature(functionDemangledName, sourceFileLocation, SignatureParserOutput.methodName);
		int parStart = methodSignature.indexOf('(');
		
		if ( parStart < 0 ){
			return methodSignature;
		}
		
		return methodSignature.substring(0,parStart);
	}
	
	private String getFunctionName(FunctionMonitoringData func) {
	return getFunctionName(func.getDemangledName(), func.getSourceFileLocation());	
	}
	
	private String getFunctionName(String functionDemangledName, String sourceFileLocation) {
		return parseSignature(functionDemangledName, sourceFileLocation, SignatureParserOutput.methodName);
	}

	@Override
	public String getPackageName(FunctionMonitoringData func) {
		return getPackageName(func.getDemangledName(), func.getSourceFileLocation());
	}
	
	public String getPackageName(String functionDemangledName, String sourceFileLocation) {
		return parseSignature(functionDemangledName, sourceFileLocation, SignatureParserOutput.packageName);
	}
	
	private enum SignatureParserOutput { methodName, className, packageName, classNameNoTemplates };
	
	public String parseSignature(String functionName, String functionLocation, SignatureParserOutput requestedOutput) {
//		String functionName = func.getDemangledName();
		
		int parenthesisStart = functionName.indexOf('(');
		
		String functionNameNoParenthesis;
		if ( parenthesisStart < 0 ){
			//			example of a function name without parenthesis: __libc_start_main@plt
			//			throw new IllegalArgumentException("Functon name unexpectedly has no parenthesis: "+functionName);
			functionNameNoParenthesis = functionName;	
		} else {
			functionNameNoParenthesis = functionName.substring(0, parenthesisStart);
		}
		
		int methodStart = functionNameNoParenthesis.lastIndexOf("::");
		
		if ( methodStart < 0 ){
			switch ( requestedOutput ){
			case methodName:
				return functionName;
			case className:
			case classNameNoTemplates:
				return getSourceLocationAsClass(functionLocation);
			case packageName:
			return getSourceLocationAsPackage( functionLocation );
			default:
				throw new IllegalStateException("New signature chunk type not handled!!!");
			}
		}
		
		if ( requestedOutput == SignatureParserOutput.methodName ){
			return functionName.substring(methodStart+2);
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
			className = functionName.substring(classStart+2);	
			packageName = functionName.substring(0,classStart);
		} else {
			className = functionName;
			packageName = "";
		}

		if ( requestedOutput == SignatureParserOutput.className  ){
			return className;
		} else if ( requestedOutput == SignatureParserOutput.classNameNoTemplates ){
			int pos = className.indexOf('<');
			if ( pos < 0 ){
				return className;
			}
			return className.substring(0, pos);
		}

		return packageName;
	}

	@Override
	public String getMethodFullSignature(FunctionMonitoringData func) {
//		String sig =  getPackageName(func)+"."+getClassName(func)+"."+getMethodSignature(func);
		return func.getDemangledName();
	}

}
