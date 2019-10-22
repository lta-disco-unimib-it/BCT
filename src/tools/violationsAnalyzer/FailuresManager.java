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

import java.util.HashSet;
import java.util.Set;

import failureDetection.Failure;

public class FailuresManager {
	public Set<String> correctActions = new HashSet<String>();
	public Set<String> failingActions = new HashSet<String>();
	
	public Set<String> correctTests = new HashSet<String>();
	public Set<String> failingTests = new HashSet<String>();
	
	public Set<String> correctProcesses = new HashSet<String>();
	public Set<String> failingProcesses = new HashSet<String>();
	
	public void addFailingAction( String id ){
		failingActions.add(id);
	}
	
	public void addCorrectAction( String id ){
		correctActions.add(id);
	}
	
	public void addFailingTest( String id ){
		failingTests.add(id);
	}
	
	public void addCorrectTest( String id ){
		correctTests.add(id);
	}
	
	public void addFailingProcess( String id ){
		failingProcesses.add(id);
	}
	
	public void addCorrectProcess( String id ){
		correctProcesses.add(id);
	}

	public Set<String> getCorrectActions() {
		return new HashSet<String>(correctActions);
	}

	public Set<String> getFailingActions() {
		return  new HashSet<String>(failingActions);
	}

	public Set<String> getCorrectTests() {
		return  new HashSet<String>(correctTests);
	}

	public Set<String> getFailingTests() {
		return  new HashSet<String>(failingTests);
	}

	public Set<String> getCorrectProcesses() {
		return new HashSet<String>(correctProcesses);
	}

	public Set<String> getFailingProcesses() {
		return  new HashSet<String>(failingProcesses);
	}
	
	public void addFailure ( Failure failure ){
		String pid = failure.getFailingPID();
		addFailingProcess(pid);
		
		String action = failure.getFailingActionId();
		String test = failure.getFailingTestId();
		
		if ( action != null || test != null ){
			if ( action != null ){
				addFailingAction(action);
			}


			if ( test != null ){
				addFailingTest(test);
			}
		} else {
			for ( String id : failure.getActiveActionsIds() ){
				addFailingAction(id);
			}
			for ( String id : failure.getActiveTestsIds() ){
				addFailingTest(id);
			}
		}
		
		
	}

	public void addFailingProcesses(Set<String> failingProcesses) {
		this.failingProcesses.addAll(failingProcesses);
	}
	
	public void addFailingActions(Set<String> failingProcesses) {
		this.failingActions.addAll(failingProcesses);
	}
	
	public void addFailingTests(Set<String> failingProcesses) {
		this.failingTests.addAll(failingProcesses);
	}
	
	public void addCorrectProcesses(Set<String> correctProcesses) {
		this.correctProcesses.addAll(correctProcesses);
	}
	
	public void addCorrectActions(Set<String> correctActions) {
		this.correctActions.addAll(correctActions);
	}
	
	public void addCorrectTests(Set<String> correctTests) {
		this.correctTests.addAll(correctTests);
	}
}
