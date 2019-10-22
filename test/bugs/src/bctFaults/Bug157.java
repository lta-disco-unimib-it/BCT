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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

//import it.unimib.disco.lta.alfa.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import tools.violationsAnalyzer.BctViolationsAnalysisResult;
import tools.violationsAnalyzer.BctViolationsAnalyzer;
import tools.violationsAnalyzer.BctViolationsManager;
import tools.violationsAnalyzer.CBEViolationsAnalyzer;
import tools.violationsAnalyzer.FailuresManager;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilter;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;
import util.cbe.CBELogLoader;

public class Bug157 {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAnalysis() throws ZipException, IOException {
		
		File cbeLogFile = TestArtifactsManager.getBugFile("157/bctCBELog");
		
		
		
		
		BctViolationsManager violationsManager = new BctViolationsManager();
		FailuresManager failuresManager = new FailuresManager();
		BctRuntimeDataFilter strategy = new BctRuntimeDataFilterCorrectOut();
		IdManagerProcess idManager = IdManagerProcess.INSTANCE;
		
		List<File> cbeLogs = new ArrayList<File>();
		cbeLogs.add(cbeLogFile);
		CBEViolationsAnalyzer analyzer = new CBEViolationsAnalyzer(cbeLogs);
		String pid = "3782@amon.disco.unimib.it";
		analyzer.addFailingProcess(pid);
		
		Set<String> falsePositives = new HashSet<String>();
		Set<String> falsePositivesKeys = new HashSet<String>();
		for ( String process : analyzer.getProcesses() ){
			if ( process.equals(pid) ){
				continue;
			}
			
			for ( BctModelViolation viol : analyzer.getViolationsForProcess(process) ){
				String model = viol.getViolatedModel();
				String mv = viol.getViolation();
				String key = model + "----" + mv;
				falsePositives.add(key);
				falsePositivesKeys.add(viol.getDescriptiveKey());
			}
		}
		
		
		assertEquals(1,analyzer.getViolationsForProcess(pid).size());
		
		ArrayList<BctModelViolation> expectedTPViolations = new ArrayList<BctModelViolation>();
		
		for ( BctModelViolation viol : analyzer.getViolationsForProcess(pid) ){
			String model = viol.getViolatedModel();
			String mv = viol.getViolation();
			String key = model + "----" + mv;
			if ( ! falsePositives.contains(key) ){
				
				assertFalse(viol.getDescriptiveKey(),falsePositivesKeys.contains(viol.getDescriptiveKey()));
				expectedTPViolations.add(viol);
			}
		}
		
		
		BctViolationsAnalysisResult result = analyzer.analyzeProcessId(pid);
		
		assertEquals(2,analyzer.getProcesses().size());
		
		AnomalyGraph ag = result.getBestAnomalyGraph();
		Set<BctModelViolation> violations = ag.getViolations();
		
		assertEquals(expectedTPViolations.size(),violations.size());
		assertEquals(1,violations.size());
		
//		BctViolationsAnalyzer violationsAnalyzer = new BctViolationsAnalyzer(violationsManager, failuresManager, strategy, idManager);
//		violationsAnalyzer.analyze("3782");
	}
}
