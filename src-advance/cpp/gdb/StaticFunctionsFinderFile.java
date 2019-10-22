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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class StaticFunctionsFinderFile extends SignatureStaticFunctionsFinder {

	private Properties staticFunctions;

	public StaticFunctionsFinderFile( File propertiesFileWithStaticFunctions) throws FileNotFoundException, IOException{
		staticFunctions = new Properties();
		staticFunctions.load(new FileReader(propertiesFileWithStaticFunctions));
	}
	
	@Override
	public boolean isStatic(FunctionMonitoringData functionMonitoringData) {
		String mangled = functionMonitoringData.getMangledName();
		
		if( ! staticFunctions.containsKey(mangled) ){
			return super.isStatic(functionMonitoringData);
		}
		
		return Boolean.parseBoolean(staticFunctions.getProperty(mangled));
	}

}
