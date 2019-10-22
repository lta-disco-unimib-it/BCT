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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import failureDetection.Failure;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctModelViolation;
import modelsViolations.BctRuntimeData;

/**
 * This class groups the information contained in a BCT log
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BctViolationsLogData {
	private List<BctModelViolation> violations = new ArrayList<BctModelViolation>();
	private Set<String> processesIds = new HashSet<String>();
	private Set<String> actionsIds = new HashSet<String>();
	private Set<String> testsIds = new HashSet<String>();
	private List<Failure> failures = new ArrayList<Failure>();
	private List<BctAnomalousCallSequence> anomalousCallSequences = new ArrayList<BctAnomalousCallSequence>();
	
	public List<Failure> getFailures() {
		return failures;
	}

	public List<BctModelViolation> getViolations() {
		return violations;
	}
	
	public Set<String> getProcessesIds() {
		return processesIds;
	}
	
	public Set<String> getActionsIds() {
		return actionsIds;
	}
	
	public Set<String> getTestsIds() {
		return testsIds;
	}
	
	public List<BctAnomalousCallSequence> getAnomalousCallSequences(){
		return anomalousCallSequences;
	}
	
	public void addViolation( BctModelViolation violation ){
		violations.add(violation);
	}
	
	public void setViolations( Collection<BctModelViolation> violations ){
		this.violations = new ArrayList<BctModelViolation>();
		this.violations.addAll(violations);
	}
	
	public void addProcessId( String id ){
		processesIds.add(id);
	}
	
	public void addActionId( String id ){
		actionsIds.add(id);
	}
	
	public void addTestId( String id ){
		testsIds.add(id);
	}
	
	public void addFailure( Failure failure ){
		failures.add(failure);
	}
	
	public void addAnomalousCallSequence( BctAnomalousCallSequence anomalousCallSequence ){
		anomalousCallSequences.add(anomalousCallSequence);
	}

	public List<BctRuntimeData> getAllRuntimeData(){
		List<BctRuntimeData> data = new ArrayList<BctRuntimeData>();
		data.addAll(violations);
		data.addAll(failures);
		data.addAll(anomalousCallSequences);
		return data;
	}

}
