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

public class ExecutionContextStateMaintainerFixed<T extends ExecutionContextData>  extends ExecutionContextStateMaintainerMemory<ExecutionContextData> {

	public static class Options {
		public static final String testCaseName = "testCaseName";
	}

	private String testName;
	private Integer currentActionId;
	
	public void init(ExecutionContextRegistrySettings settings) {
		super.init(settings);
		
		String tmpString = settings.getProperty(Options.testCaseName);
		
		if ( tmpString == null ){
			testName = "DefaultTest";
		}
		
		TestCaseData executionContextData = new TestCaseDataImpl(testName);
		currentActionId = super.actionStart(executionContextData);
		
	}

	@Override
	public synchronized Set<Integer> getCurrentActions() throws ActionsRegistryException {
		return super.getCurrentActions();
	}

	
	

	@Override
	public synchronized void actionEnd(Integer actionId)
			throws ActionsRegistryException {
		
	}

	@Override
	public synchronized Integer actionStart( ExecutionContextData executionContextData) {
		return currentActionId;
	}
	
}
