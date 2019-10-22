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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.FileUtil;

public class SignatureStaticFunctionsFinder implements StaticFunctionsFinder {
	
	protected Map<String,Boolean> staticFunctions = new HashMap<String,Boolean>();

	@Override
	public boolean isStatic(FunctionMonitoringData functionMonitoringData) {
		
		
		String mangledName = functionMonitoringData.getMangledName();
		
		if ( staticFunctions.containsKey(mangledName) ){
			return staticFunctions.get(mangledName);
		}
		
		return _isStatic(functionMonitoringData);
	}
	
	public boolean _isStatic(FunctionMonitoringData functionMonitoringData) {
		String mangledName = functionMonitoringData.getMangledName();
		
		if ( mangledName.startsWith("_GLOBAL_") ){
			return true;
		}
		
		if ( mangledName.startsWith("_ZN") ){
			
			if ( functionMonitoringData.isConsructor() || functionMonitoringData.isDestructor() ){
				return true;
			}
			
			return false;
		} else if ( mangledName.startsWith("_Z") ){
			return true;
		}
		
		return true;
	}

	@Override
	public void cacheAll(Collection<FunctionMonitoringData> functionMonitoringData) {
		Map<File,List<FunctionMonitoringData>> files = new HashMap<File,List<FunctionMonitoringData>>();
		
		
		for ( FunctionMonitoringData func : functionMonitoringData ){
			if ( ! func.isImplementedWithinProject() ){
				continue;
			}
			
			
			List<FunctionMonitoringData> list = files.get(func.getAbsoluteFile());
			if ( list == null ){
				list = new ArrayList<FunctionMonitoringData>();
				files.put(func.getAbsoluteFile(), list);
			}
			list.add(func);
		}
		
		for ( Entry<File,List<FunctionMonitoringData>> e : files.entrySet() ){
			File file = e.getKey();
			List<FunctionMonitoringData> funcs = e.getValue();
			try {
				List<String> lines = FileUtil.getLines(file);
				
				for ( FunctionMonitoringData func : funcs ){
					int first = func.getFirstSourceLine();
					if ( first >= lines.size() ){
						continue;
					}
					String line = lines.get(first-1);
					Boolean isStatic = _isStatic(func);
					if ( ! isStatic ){
						if ( line.contains("static") ){
							isStatic = Boolean.TRUE;
						}
					}
					staticFunctions.put(func.getMangledName(), isStatic);
				}
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
