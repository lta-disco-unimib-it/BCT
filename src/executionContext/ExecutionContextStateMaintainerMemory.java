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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import conf.ExecutionContextRegistrySettings;

/**
 * This class manages the recording of state information for context registries in memory
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 * @param <T>
 */
public class ExecutionContextStateMaintainerMemory<T extends ExecutionContextData> implements ExecutionContextStateMaintainer<T>{
	private HashMap<Integer,T> actions = new HashMap<Integer, T>();
	private Set<Integer> currentActions = new HashSet<Integer>();
	

	
	public synchronized void actionEnd(Integer actionId) throws ActionsRegistryException {
		if ( ! currentActions.contains(actionId) )
			throw new ActionsRegistryException("Action not registered:"+actionId);
		
		currentActions.remove(actionId);
	}

	public synchronized Integer actionStart(T executionContextData) {
		Integer id = generateNewId();
		actions.put(id,executionContextData);
		currentActions.add(id);
		
		return id;
	}

	private Integer generateNewId() {
		return actions.size();
	}

	public synchronized Set<Integer> getCurrentActions() throws ActionsRegistryException {
		
		return currentActions;
	}

	public void init(ExecutionContextRegistrySettings settings) {
		//nothing to do
	}

	public boolean isActionRunning(Integer actionId)
			throws ActionsRegistryException {
		return currentActions.contains(actionId);
	}

	public T getExecutionContextData(Integer actionId)
			throws ActionsRegistryException {
		
		return actions.get(actionId);
	}

}
