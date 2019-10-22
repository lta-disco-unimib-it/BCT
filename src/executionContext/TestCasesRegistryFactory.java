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
package executionContext;

import conf.EnvironmentalSetter;
import conf.ExecutionContextRegistrySettings;
import conf.SettingsException;

/**
 * This class return the test cases registry as configured in configuration files
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class TestCasesRegistryFactory {
	private static TestCasesRegistry executionContextRegistry;
	
	public synchronized static TestCasesRegistry getExecutionContextRegistry(){
		if ( executionContextRegistry != null ){
			return executionContextRegistry;
		}
		
		try {
			
			ExecutionContextRegistrySettings settings = EnvironmentalSetter.getTestCasesRegistrySettings();
		
			executionContextRegistry = (TestCasesRegistry) settings.getType().newInstance();
			executionContextRegistry.init(settings);
			
			return executionContextRegistry;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		executionContextRegistry = new TestCasesRegistry(new ExecutionContextStateMaintainerMemory<TestCaseData>());
		
		return executionContextRegistry;
	}
}
