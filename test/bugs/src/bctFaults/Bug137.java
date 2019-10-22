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
package bctFaults;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;

import testSupport.TestArtifactsManager;
import tools.violationsAnalyzer.BctViolationsAnalysisResult;
import tools.violationsAnalyzer.BctViolationsManager;
import tools.violationsAnalyzer.CBEViolationsAnalyzer;
import tools.violationsAnalyzer.FailuresManager;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilter;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerTest;
import traceReaders.raw.TraceException;

public class Bug137 extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public void testBug() throws TraceException, IOException{
		File logFile = TestArtifactsManager.getBugFile("137/bctCBELog");
		ArrayList<File> list = new ArrayList<File>();
		list.add(logFile);
		CBEViolationsAnalyzer analyzer = new CBEViolationsAnalyzer(list);

		Set<String> expectedFailungTests = new HashSet<String>();
		expectedFailungTests.add("main.RiunioneTest.testInizia(()V)1");
		assertEquals(expectedFailungTests, analyzer.getFailingTests() );

		BctViolationsAnalysisResult result = analyzer.analyzeTestId("main.RiunioneTest.testInizia(()V)1");
		AnomalyGraph ag = result.getBestAnomalyGraph();
		Set<BctModelViolation> violations = ag.getViolations();

		assertEquals(6, violations.size());

	}
	
	public void testViolationsLoading() throws TraceException, IOException{
		File logFile = TestArtifactsManager.getBugFile("137/bctCBELog");
		ArrayList<File> list = new ArrayList<File>();
		list.add(logFile);
		CBEViolationsAnalyzer analyzer = new CBEViolationsAnalyzer(list);
		String id = "main.RiunioneTest.testInizia(()V)1";
		List<BctModelViolation> violations = analyzer.getViolationsForTest(id);

		assertEquals(6, violations.size());
		
		
		FailuresManager fm = new FailuresManager();

		
		fm.addFailingProcesses(analyzer.getFailingProcesses());
		fm.addFailingActions(analyzer.getFailingActions());
		fm.addFailingTests(analyzer.getFailingTests());

		Set<String> correctActions = new HashSet<String>();
		correctActions.addAll(analyzer.getActions());
		correctActions.removeAll(analyzer.getFailingActions());
		fm.addCorrectActions(correctActions);

		Set<String> correctProcesses = new HashSet<String>();
		correctProcesses.addAll(analyzer.getProcesses());
		correctProcesses.removeAll(analyzer.getFailingProcesses());
		fm.addCorrectProcesses(correctProcesses);

		Set<String> correctTests = new HashSet<String>();
		correctTests.addAll(analyzer.getTests());
		correctTests.removeAll(analyzer.getFailingTests());
		fm.addCorrectTests(correctTests);
		
		
		BctViolationsManager violationsManager = new BctViolationsManager();
		
		for ( BctModelViolation viol : violations ){
			violationsManager.addDatum(viol);
		}
		
		Set<String> observedCorrectIds = fm.getCorrectTests();
		
		Set<String> expectedCorrectIds = new HashSet<String>();
		expectedCorrectIds.add("main.PersonaTest.testSalutaNomeNull(()V)1");
		assertEquals(expectedCorrectIds, observedCorrectIds);
		
		Set<String> expectedFailingIds = new HashSet<String>();
		Set<String> observedFailingIds = fm.getFailingTests();
		expectedFailingIds.add(id);
		assertEquals(expectedFailingIds, observedFailingIds);
		
		BctRuntimeDataFilter filter = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> filteredViolations = filter.getFilteredData(violationsManager, fm, IdManagerTest.INSTANCE, id);
		
		assertEquals(6, filteredViolations.size());
		
		

	}

}
