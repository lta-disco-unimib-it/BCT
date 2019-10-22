/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.FormattingException;

import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilter;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManager;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;
import tools.violationsAnalyzer.filteringStrategies.IdManagerTest;
import util.cbe.CBELogLoader;
import failureDetection.Failure;

public class CBEViolationsAnalyzer {
	private Set<String> failingProcesses = new HashSet<String>();
	private Set<String> failingActions = new HashSet<String>();
	private Set<String> failingTests = new HashSet<String>();
	private List<File> cbeLogs = new ArrayList<File>();

	private BctViolationsManager violationsManager = new BctViolationsManager();
	private BctViolationsLogData logData;
	
	public CBEViolationsAnalyzer(List<File> cbeLogs){
		this.cbeLogs.addAll(cbeLogs);
		loadFaultsInformation();
	}

	private void loadFaultsInformation() {

		
		CBEBctViolationsLogLoader loader = new CBEBctViolationsLogLoader();
		
		try {
			logData = loader.load(cbeLogs);


			violationsManager.addData( logData.getViolations() );

			for ( Failure f : logData.getFailures() ){
				addFailingAction(f.getFailingActionId());
				addFailingProcess(f.getFailingPID());
				addFailingTest(f.getFailingTestId());
			}


		} catch (CBEBctViolationsLogLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<String> getFailingProcesses() {
		return failingProcesses;
	}

	public Set<String> getFailingActions() {
		return failingActions;
	}

	public Set<String> getFailingTests() {
		return failingTests;
	}

	public void addFailingProcess(String process) {
		failingProcesses.add(process);
	}

	public void addFailingTest(String test) {
		failingTests.add(test);
	}

	public void addFailingAction(String action) {
		failingActions.add(action);
	}

	public void addFailingProcesses(Collection<String> process) {
		failingProcesses.addAll(process);
	}

	public void addFailingTests(Collection<String> test) {
		failingTests.addAll(test);
	}
	
	public BctViolationsAnalysisResult analyzeProcessId( String pid ){
		return analyzeId(pid,IdManagerProcess.INSTANCE);
	}
	
	public BctViolationsAnalysisResult analyzeTestId( String pid ){
		return analyzeId(pid,IdManagerTest.INSTANCE);
	}

	public BctViolationsAnalysisResult analyzeActionId( String pid ){
		return analyzeId(pid,IdManagerAction.INSTANCE);
	}

	private BctViolationsAnalysisResult analyzeId( String pid, IdManager idManager ){
		FailuresManager fm = new FailuresManager();

		
		fm.addFailingProcesses(failingProcesses);
		fm.addFailingActions(failingActions);
		fm.addFailingTests(failingTests);

		Set<String> correctActions = new HashSet<String>();
		correctActions.addAll(logData.getActionsIds());
		correctActions.removeAll(failingActions);
		fm.addCorrectActions(correctActions);

		Set<String> correctProcesses = new HashSet<String>();
		correctProcesses.addAll(logData.getProcessesIds());
		correctProcesses.removeAll(failingProcesses);
		fm.addCorrectProcesses(correctProcesses);

		Set<String> correctTests = new HashSet<String>();
		correctTests.addAll(logData.getTestsIds());
		correctTests.removeAll(failingTests);
		fm.addCorrectTests(correctTests);


		BctRuntimeDataFilter filter = new BctRuntimeDataFilterCorrectOut();

		BctViolationsAnalyzer analyzer = new BctViolationsAnalyzer(violationsManager,fm, filter, idManager);
		
		return analyzer.analyze(pid);
		
	}




	public List<BctModelViolation> getViolationsForAction(String id) {
		return violationsManager.getDataForAction(id);
	}




	public List<BctModelViolation> getViolationsForProcess(String id) {
		return violationsManager.getDataForProcess(id);
	}




	public List<BctModelViolation> getViolationsForTest(String id) {
		return violationsManager.getDataForTest(id);
	}




	public Set<String> getProcesses() {
		return logData.getProcessesIds();
	}




	public Set<String> getActions() {
		return logData.getActionsIds();
	}




	public Set<String> getTests() {
		return logData.getTestsIds();
	}
}
