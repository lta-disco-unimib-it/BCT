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
package tools.violationsAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctModelViolation;
import modelsViolations.BctRuntimeData;

public class BctRuntimeDataManager <T extends BctRuntimeData > {

	public BctRuntimeDataManager() {
		super();
	}
	
	private HashMap<String,T> elements = new HashMap<String, T>();
	private MultiHashMap<String,T> testsElements = new MultiHashMap<String, T>();
	private MultiHashMap<String,T> actionsElements = new MultiHashMap<String, T>();
	private MultiHashMap<String,T> processesElements = new MultiHashMap<String, T>();
	
	public void addDatum( T modelViolation ){
		elements.put(modelViolation.getId(), modelViolation);
		
		String[] tests = modelViolation.getCurrentTests();
		for ( String test : tests ){
			testsElements.put(test, modelViolation);
		}
		
		String[] actions = modelViolation.getCurrentActions();
		for ( String action : actions ){
			actionsElements.put(action, modelViolation);
		}
		
		processesElements.put(modelViolation.getPid(), modelViolation);
	}
	
	public List<T> getDataForAction( String id ){
		List<T> res = actionsElements.get(id);
		if ( res == null ){
			return new ArrayList<T>(0);
		}
		return res;
	}
	
	public List<T> getDataForTest( String id ){
		List<T> res = testsElements.get(id);
		if ( res == null ){
			return new ArrayList<T>(0);
		}
		return res;
	}
	
	public List<T> getDataForProcess( String id ){
		List<T> res = processesElements.get(id);
		if ( res == null ){
			return new ArrayList<T>(0);
		}
		return res;
	}
	
	public Collection<T> getData() {
		return elements.values();
	}

	public T getDatum(String violationId) {
		return elements.get(violationId);
	}

	public void addData(List<T> violationsToAdd) {
		for ( T v : violationsToAdd ){
			addDatum(v);
		}
	}
	
	public void removeData(List<T> modelViolations) {
		for ( T modelViolation : modelViolations ){
			elements.remove(modelViolation.getId());
		}
		reset();
	}

	public void removeDatum(T modelViolation) {
		elements.remove(modelViolation.getId());
		reset();
	}
	
	private void reset(){
		testsElements.clear();
		actionsElements.clear();
		processesElements.clear();
		
		for ( T modelViolation : elements.values() ){
			String[] tests = modelViolation.getCurrentTests();
			for ( String test : tests ){
				testsElements.put(test, modelViolation);
			}

			String[] actions = modelViolation.getCurrentActions();
			for ( String action : actions ){
				actionsElements.put(action, modelViolation);
			}

			processesElements.put(modelViolation.getPid(), modelViolation);
		}
	}


}