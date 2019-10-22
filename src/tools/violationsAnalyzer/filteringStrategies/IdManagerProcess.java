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
/**
 * 
 */
package tools.violationsAnalyzer.filteringStrategies;

import java.util.List;
import java.util.Set;

import modelsViolations.BctModelViolation;
import tools.violationsAnalyzer.BctRuntimeDataManager;
import tools.violationsAnalyzer.FailuresManager;

public class IdManagerProcess implements IdManager{
	
	public static final IdManagerProcess INSTANCE = new IdManagerProcess();
	
	private IdManagerProcess(){
		
	}

	public List<BctModelViolation> getData(
			BctRuntimeDataManager violationsManager, String id) {
		return violationsManager.getDataForProcess(id); 
	}

	public Set<String> getCorrectIds(FailuresManager failureManager) {
		return failureManager.getCorrectProcesses();
	}

	public Set<String> getFailingIds(FailuresManager failureManager) {
		return failureManager.getFailingProcesses();
	}
	
}