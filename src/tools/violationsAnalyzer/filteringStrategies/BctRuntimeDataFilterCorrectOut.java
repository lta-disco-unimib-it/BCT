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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;
import tools.violationsAnalyzer.BctRuntimeDataManager;
import tools.violationsAnalyzer.BctViolationsManager;
import tools.violationsAnalyzer.FailuresManager;

public class BctRuntimeDataFilterCorrectOut implements BctRuntimeDataFilter {

	
	
	public BctRuntimeDataFilterCorrectOut(){
	}
	
	public <T extends BctRuntimeData> List<T> getFilteredData(
			BctRuntimeDataManager<T> violationsManager, FailuresManager failuresManager, IdManager idManager, String failureId) {
		
		
		Set<String> correctNames = new HashSet<String>();
		
		//add the correct violations to the list
		Set<String> correctIds = idManager.getCorrectIds(failuresManager);
		for ( String correctId : correctIds ){
			List<T> violations = idManager.getData(violationsManager,correctId);
			for ( T violation : violations ){
				String name = violation.getDescriptiveKey();
				correctNames.add(name);
			}
		}
		
		List<T> viols = idManager.getData(violationsManager, failureId);
		List<T> violsToCOnsider = new ArrayList<T>();
		Set<T> violsFiltered = new HashSet<T>();
		for ( T viol : viols ){
			if ( ! correctNames.contains(viol.getDescriptiveKey()) ){
				violsToCOnsider.add(viol);
			} else {
				violsFiltered.add(viol);
			}
		}
		
		for ( T viol : violsFiltered ){
			String descriptiveFiltered = viol.getDescriptiveKey();
			System.out.println("Filtered anomaly: "+viol.getDescriptiveKey());
			System.out.println("Correct ids for this anomaly:");
			for ( String correctId : correctIds ){
				List<T> violations = idManager.getData(violationsManager,correctId);
				for ( T violation : violations ){
//					String name = violation.getDescriptiveKey();
					if ( descriptiveFiltered.equals(violation.getDescriptiveKey()) ){
						System.out.println(correctId);
						break;
					}
				}
			}
		}
		
		
		
		return violsToCOnsider;
	}

	
	
	
}