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

import java.util.Set;

import conf.ExecutionContextRegistrySettings;

/**
 * This interface is extended by classes that maintain the stateful information of execution cntext registry
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 * @param <T>
 */
public interface ExecutionContextStateMaintainer <T extends ExecutionContextData>{

	/**
	 * This method register that a new action has started
	 * 
	 * @param executionContextData data that describe the action
	 * @return action ID unique ID for the action
	 */
	public Integer actionStart( T executionContextData ) throws ActionsRegistryException;
	
	/**
	 * This method register that the given action has ended
	 * @param actionData
	 * @throws ActionsRegistryException 
	 */
	public void actionEnd( Integer actionId ) throws ActionsRegistryException;
	
	/**
	 * This method return the user actions that have been started and not stopped yet.
	 * More than one action can be returned because of the multi-threading nature of many java systems.
	 * @return
	 * @throws ActionsRegistryException
	 */
	public Set<Integer> getCurrentActions() throws ActionsRegistryException;

	/**
	 * Returns true if the action is not ended yet
	 * 
	 * @return
	 * @throws ActionsRegistryException
	 */
	public boolean isActionRunning(Integer actionId) throws ActionsRegistryException;
	
	/**
	 * Return the ExecutionContextData associated with a given action
	 * 
	 * @param actionId
	 * @return
	 * @throws ActionsRegistryException
	 */
	public T getExecutionContextData(Integer actionId) throws ActionsRegistryException;
	
	/**
	 * Initialize the instance
	 * 
	 * @param settings
	 */
	public void init(ExecutionContextRegistrySettings settings);
	
}
