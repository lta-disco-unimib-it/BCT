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
package tools.gdbTraceParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import cpp.gdb.Demangler;
import cpp.gdb.TraceUtils;

import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;

public class FunctionNamesMapper {
	// <DemangledNameV1, MangledNameV1> mangled name has ending space
	private HashMap<String,String> functionNamesV1 = new HashMap<String,String>();
	//speeds up loading
	private HashSet<String> functionsV1 = new HashSet<String>();
	// <MangledNameV2, MangledNameV2> mangled names have ending space 
	private HashMap<String,String> functionNamesV2 = new HashMap<String,String>();
	private boolean loaded;
	private Demangler demangler = new Demangler();
	
	public String getCorrespondingFunctionName(String _functionName) {
		populate();
		
		
		
		String newName = functionNamesV2.get(_functionName);
		if ( newName != null ){
			System.out.println("Corresponding for: "+_functionName+" "+newName);
			return newName;
		}
		
		String functionName = _functionName.trim();
		
		String demangled = demangler.demangleNoTemplate(functionName);
		String nameV1 = functionNamesV1.get(demangled);
		
		if (nameV1 == null ){
			nameV1 = functionName;
		}
		
		functionNamesV2.put(_functionName, nameV1);
		System.out.println("Corresponding for: "+_functionName+" "+nameV1);
		return nameV1;
	}

	private void populate() {
		if ( loaded ){
			return;
		}
		
		try {
			for ( String _functionName : ModelsFetcherFactoy.modelsFetcherInstance.getIoModelsNames() ){

				String functionName = TraceUtils.extractFunctionSignatureFromGenericProgramPointName(_functionName.trim());
				if ( ! functionsV1.contains(functionName) ){
					functionsV1.add(functionName);
					functionNamesV1.put(demangler.demangleNoTemplate(functionName), functionName+" " );
				}
			}
		} catch (ModelsFetcherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Function names V1" );
		for( Entry<String,String> e : functionNamesV1.entrySet() ){
			System.out.println(e.getKey()+" "+e.getValue());
		}
		
		loaded = true;
	}
	
	
	
}
