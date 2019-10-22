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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnTypesRegistry {

	private static final ReturnTypesRegistry INSTANCE = new ReturnTypesRegistry();
	private HashMap<String,FunctionDeclaration> returnTypes = new HashMap<String, FunctionDeclaration>();
	
	private CSourceAnalyzer csourceAnalyzer;
	
	private ReturnTypesRegistry(){
		csourceAnalyzer = CSourceAnalyzerRegistry.getCSourceAnalyzer();
	}
	
	public static int getParameters(FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findParameters( functionMonitoringData );
	}
	
	private int findParameters(FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			//FIXME: return -1, 0 kept just for the demo
//			return -1;
			return 0;
		}
		return function.getParametersNumber();
	}

	public static Set<String> getPointerArgs(FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findPointerArgs( functionMonitoringData );
	}
	
	public static Set<String> getScalarArgs(FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findScalarArgs( functionMonitoringData );
	}
	
	public static Set<String> getReferenceArgs(
			FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findReferenceArgs( functionMonitoringData );
	}
	
	private Set<String> findReferenceArgs(
			FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return new HashSet<String>();
		}
		return function.getReferenceArgs();
	}

	public static String getReturnType(FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findReturnType( functionMonitoringData );
	}
	
	public Set<String> findScalarArgs(FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return new HashSet<String>();
		}
		return function.getScalarArgs();
	}
	
	public Set<String> findPointerArgs(FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return new HashSet<String>();
		}
		return function.getPointerArgs();
	}
	
	public String findReturnType(FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return null;
		}
		return function.getReturnType();
	}
	
	public static List<LocalVariableDeclaration> getLocalVariableDeclarations(FunctionMonitoringData functionMonitoringData) {
		return INSTANCE.findLocalVariables( functionMonitoringData );
	}

	private List<LocalVariableDeclaration> findLocalVariables(
			FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return new ArrayList<LocalVariableDeclaration>();
		}
		return function.getLocalVariables();
	}

	public FunctionDeclaration getFunctionDeclaration(
			FunctionMonitoringData functionMonitoringData) {
		
		
		if ( csourceAnalyzer == null ){
			return null;
		}
		
		String functionName = functionMonitoringData.getDemangledName();
		
		functionName = DemangledNamesUtils.removeTemplatesFromSignature(functionName);
		
		if ( DemangledNamesUtils.isPlainCFunction(functionName) ){ //is a C function, remove parameters
			functionName = DemangledNamesUtils.getFunctionNameOnly(functionName);
		}
		
		FunctionDeclaration returnType = returnTypes.get(functionName);
		if ( returnType == null ) {
			
			if ( csourceAnalyzer != null ){
				if ( functionMonitoringData.isImplementedWithinProject() ){
					try {
						List<FunctionDeclaration> functions = csourceAnalyzer.retrieveFunctionsDeclaredInFile(functionMonitoringData.getAbsoluteFile());
						for ( FunctionDeclaration function :functions ){
							returnTypes.put(function.getName(), function );
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		
		return returnTypes.get(functionName);
	}


	public void setCsourceAnalyzer(CSourceAnalyzer csourceAnalyzer) {
		this.csourceAnalyzer = csourceAnalyzer;
	}

	public static void clear(){
		INSTANCE.returnTypes.clear();
	}
	
	
	public static List<Parameter> getAllArgs(
			FunctionMonitoringData functionMonitoringData) {
		FunctionDeclaration function = INSTANCE.getFunctionDeclaration(functionMonitoringData);
		if ( function == null ){
			return new ArrayList<Parameter>();
		}

		return function.getAllArgs();
	}
	

}
