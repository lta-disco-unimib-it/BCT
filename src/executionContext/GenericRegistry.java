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
 * This class keeps track of information associated to current execution
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 * @param <T>
 */
public class GenericRegistry<T extends ExecutionContextData> implements ExecutionContextRegistry<T> {

	private ExecutionContextStateMaintainer<T> maintainerRecorder;

	public GenericRegistry(){
		
	}
	
	public GenericRegistry(ExecutionContextStateMaintainer<T> stateRecorder ){
		this.maintainerRecorder = stateRecorder;
	}
	
	public void actionEnd(Integer actionId) throws ActionsRegistryException {
		maintainerRecorder.actionEnd(actionId);
	}

	public Integer actionStart(T executionContextData)
			throws ActionsRegistryException {
		return maintainerRecorder.actionStart(executionContextData);
	}

	public Set<Integer> getCurrentActions() throws ActionsRegistryException {
		return maintainerRecorder.getCurrentActions();
	}

	public T getExecutionContextData(Integer actionId)
			throws ActionsRegistryException {
		return maintainerRecorder.getExecutionContextData(actionId);
	}

	public void init(ExecutionContextRegistrySettings settings) {
		maintainerRecorder = settings.newMaintainerRecorderInstance();
	}

	public boolean isActionRunning(Integer actionId)
			throws ActionsRegistryException {
		return maintainerRecorder.isActionRunning(actionId);
	}

}
