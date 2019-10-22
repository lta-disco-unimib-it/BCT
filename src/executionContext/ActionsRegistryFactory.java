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

import conf.ActionsRegistrySettings;
import conf.EnvironmentalSetter;

/**
 * Factory that creates the action registry as configured by the user
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ActionsRegistryFactory {
	private static ActionsRegistry actionsRegistry;
	
	/**
	 * Returns the instance of the actions registry
	 * The registry is configured following user choices written i configuration files
	 * 
	 * 
	 * @return
	 */
	public synchronized static ActionsRegistry getExecutionContextRegistry(){
		if ( actionsRegistry != null ){
			return actionsRegistry;
		}
		
		ActionsRegistrySettings settings = EnvironmentalSetter.getActionsRegistrySettings();
		
		try {
			actionsRegistry =  (ActionsRegistry) settings.getType().newInstance() ;
			actionsRegistry.init(settings);
			
			return actionsRegistry;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		actionsRegistry = new ActionsRegistry( new ExecutionContextStateMaintainerMemory<ActionData>());
		return actionsRegistry;
	}
}
