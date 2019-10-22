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
package check;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import junit.framework.TestFailure;

import modelsFetchers.ModelsFetcherException;
import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctModelViolation.ViolationType;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import recorders.RecorderFactory;
import recorders.TestViolationsRecorderStub;
import recorders.ViolationsRecorder;

import conf.EnvironmentalSetter;
import conf.ExecutionContextRegistrySettings;
import conf.FineInteractionCheckerSettings;
import conf.ModelsFetcherSettings;
import executionContext.ActionsRegistry;
import executionContext.TestCasesRegistry;

public class FineInteractionChecker_DFA_Test {

	
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		
		Properties p = new Properties();
		p.put("type", TestModelsFetcherStub.class.getCanonicalName() );
		EnvironmentalSetter.setModelsFetcherSettings(p);
	
		EnvironmentalSetter.setTestCasesSettings(getTestCasesRegistrySettings());
		
		EnvironmentalSetter.setActionsRegistrySettings(getActionsRegistrySettings());
		
		EnvironmentalSetter.setViolationsRecorderSettings(getViolationsRecorderSettings());
	}

	private long startTime;

	@Before
	public void setUp() throws Exception {
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		recorder.clear();
		
		startTime = System.currentTimeMillis();
	}
	
	private static Properties getActionsRegistrySettings() {
		Properties p = new Properties();
		p.put("type", ActionsRegistry.class.getCanonicalName());
		p.put(ExecutionContextRegistrySettings.Options.stateRecorderType,"executionContext.ExecutionContextStateMaintainerMemory");
		return p;
	}

	private static Properties getTestCasesRegistrySettings() {
		Properties p = new Properties();
		p.put("type", TestCasesRegistry.class.getCanonicalName());
		p.put(ExecutionContextRegistrySettings.Options.stateRecorderType,"executionContext.ExecutionContextStateMaintainerMemory");
		return p;
	}

	private static Properties getViolationsRecorderSettings() {
		Properties p = new Properties();
		p.put("type", TestViolationsRecorderStub.class.getCanonicalName() );
		
		return p;
	}

	private FineInteractionCheckerSettings getInteractionCheckerSettings_recordSequence_recordAllAnomalies(){
		Properties p = new Properties();
		p.put("type", FineInteractionChecker.class.getCanonicalName());
		p.put(FineInteractionCheckerSettings.Options.anomalousSequencesRecordingEnabled, "true");
		p.put(FineInteractionCheckerSettings.Options.avaPathLen, "2");
		p.put(FineInteractionCheckerSettings.Options.dfa, "true");
		p.put(FineInteractionCheckerSettings.Options.fineAnalysisEnabled, "true");
		
		return new FineInteractionCheckerSettings(FineInteractionChecker.class,p);
	}
	
	private FineInteractionCheckerSettings getInteractionCheckerSettings_recordSequence_dontRecordAllAnomalies(){
		Properties p = new Properties();
		p.put("type", FineInteractionChecker.class.getCanonicalName());
		p.put(FineInteractionCheckerSettings.Options.anomalousSequencesRecordingEnabled, "true");
		p.put(FineInteractionCheckerSettings.Options.avaPathLen, "2");
		p.put(FineInteractionCheckerSettings.Options.dfa, "true");
		p.put(FineInteractionCheckerSettings.Options.fineAnalysisEnabled, "false");
		
		return new FineInteractionCheckerSettings(FineInteractionChecker.class,p);
	}
	
	private static FineInteractionCheckerSettings getInteractionCheckerSettings_dontRecordSequence_dontRecordAllAnomalies(){
		Properties p = new Properties();
		p.put("type", FineInteractionChecker.class.getCanonicalName());
		p.put(FineInteractionCheckerSettings.Options.anomalousSequencesRecordingEnabled, "false");
		p.put(FineInteractionCheckerSettings.Options.avaPathLen, "2");
		p.put(FineInteractionCheckerSettings.Options.dfa, "true");
		p.put(FineInteractionCheckerSettings.Options.fineAnalysisEnabled, "false");
		
		return new FineInteractionCheckerSettings(FineInteractionChecker.class,p);
	}
	
	private static FineInteractionCheckerSettings getInteractionCheckerSettings_dontRecordSequence_recordAllAnomalies(){
		Properties p = new Properties();
		p.put("type", FineInteractionChecker.class.getCanonicalName());
		p.put(FineInteractionCheckerSettings.Options.anomalousSequencesRecordingEnabled, "false");
		p.put(FineInteractionCheckerSettings.Options.avaPathLen, "2");
		p.put(FineInteractionCheckerSettings.Options.dfa, "true");
		p.put(FineInteractionCheckerSettings.Options.fineAnalysisEnabled, "true");
		
		return new FineInteractionCheckerSettings(FineInteractionChecker.class,p);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSimpleAutomaton_recordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(0, fsaViolations.size() );
		
		 
	
	}

	
	@Test
	public void testSimpleAutomaton_newFinal_recordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newFinal(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newFinal();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newFinal();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newBranch_recordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newBranch();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		System.out.println(anomalousCallSequence.getAnomalousCallSequenceList());
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_INVOCATION_SEQUENCE,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newTail_recordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newTail();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		System.out.println(anomalousCallSequence.getAnomalousCallSequenceList());
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION_SEQUENCE,violation.getViolationType());
		
		
	 
	}
	
	@Test
	public void testSimpleAutomaton_newBranch_newTail_recordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newBranch_newTail();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(2, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_INVOCATION_SEQUENCE,violation.getViolationType());
		
		
		
		
		
		violation = fsaViolations.get(1);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(1),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION_SEQUENCE,violation.getViolationType());
		
		
	 
	}
	
	
	
	//
	//Record only anomalies
	//
	
	@Test
	public void testSimpleAutomaton_dontRecordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(0, fsaViolations.size() );
		
	 
	}

	
	@Test
	public void testSimpleAutomaton_newFinal_dontRecordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newFinal(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newFinal();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newBranch_dontRecordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_INVOCATION_SEQUENCE,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newTail_dontRecordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION_SEQUENCE,violation.getViolationType());
		
		
	 
	}
	
	@Test
	public void testSimpleAutomaton_newBranch_newTail_dontRecordSequence_recordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_recordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(2, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_INVOCATION_SEQUENCE,violation.getViolationType());
		
		
		
		
		
		violation = fsaViolations.get(1);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(1),violation.getUnexpectedSequenceList());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION_SEQUENCE,violation.getViolationType());
		
		
	 
	}
	
	
	
	//
	//Record only anomalous sequence
	//

	
	@Test
	public void testSimpleAutomaton_recordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(0, fsaViolations.size() );
		
	 
	}

	
	@Test
	public void testSimpleAutomaton_newFinal_recordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newFinal(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newFinal();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newFinal();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(0,violation.getUnexpectedSequenceList().size());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newBranch_recordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newBranch();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(0,violation.getUnexpectedSequenceList().size());
		
		assertEquals(unexpectedSequences.get(0).get(0), violation.getViolation() );
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newTail_recordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newTail();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0).get(0),violation.getViolation());
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
		
		
	 
	}
	
	@Test
	public void testSimpleAutomaton_newBranch_newTail_recordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(1,anomalousCallSequences.size());
		
		BctAnomalousCallSequence anomalousCallSequence = anomalousCallSequences.get(0);
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newBranch_newTail();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(sequence, anomalousCallSequence.getAnomalousCallSequenceList() );
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		List<String> unexpectedSequence = unexpectedSequences.get(0);
		
		assertEquals(new ArrayList<String>(0),violation.getUnexpectedSequenceList());
		
		assertEquals(unexpectedSequence.get(0),violation.getViolation());
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
		
			
	 
	}
	
	
	//
	//Record only simple violations
	//

	
	@Test
	public void testSimpleAutomaton_dontRecordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(0, fsaViolations.size() );
		
	 
	}

	
	@Test
	public void testSimpleAutomaton_newFinal_dontRecordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newFinal(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		

		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newFinal();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newFinal();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(0,violation.getUnexpectedSequenceList().size());
		
		assertEquals(ViolationType.UNEXPECTED_TERMINATION,violation.getViolationType());
		 
	}
	
	
	@Test
	public void testSimpleAutomaton_newBranch_dontRecordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		
		
		HashMap<String, List<String>> expectedAnomalousCallSequences = InteractionCheckTestRunner.getAnomalousSequence_SimpleCheck_newBranch();  
		List<String> sequence = expectedAnomalousCallSequences.get("Simple.main()");
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(0,violation.getUnexpectedSequenceList().size());
		
		assertEquals(unexpectedSequences.get(0).get(0), violation.getViolation() );
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
	 
	}
	
	
	@Test
	public void testSimpleAutomaton_newTail_dontRecordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		assertEquals(1, fsaViolations.size() );
		
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		
		
		assertEquals(unexpectedSequences.get(0).get(0),violation.getViolation());
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
		
		
	InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
	}
	
	@Test
	public void testSimpleAutomaton_newBranch_newTail_dontRecordSequence_dontRecordAllAnomalies() throws ModelsFetcherException{
		FineInteractionChecker checker = getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies();	
		InteractionCheckTestRunner.runSimpleCheck_newBranch_newTail(checker);
		InteractionCheckerPerformanceRegistry.INSTANCE.addExecutionTime(this.getClass().getCanonicalName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName(), System.currentTimeMillis()-startTime);
		
		TestViolationsRecorderStub recorder = (TestViolationsRecorderStub) RecorderFactory.getViolationsRecorder();
		
		List<BctAnomalousCallSequence> anomalousCallSequences = recorder.getAnomalousCallSequences();
		
		assertEquals(0,anomalousCallSequences.size());
		
		
		
		List<BctFSAModelViolation> fsaViolations = recorder.getFsaViolations();
		
		
		
		assertEquals(1, fsaViolations.size() );
		
		
		List<List<String>> unexpectedSequences = InteractionCheckTestRunner.getUnexpectedSequence_SimpleCheck_newBranch_newTail();
		
		BctFSAModelViolation violation = fsaViolations.get(0);
		assertNotNull( violation.getStackTrace() );
		
		List<String> unexpectedSequence = unexpectedSequences.get(0);
		
		assertEquals(new ArrayList<String>(0),violation.getUnexpectedSequenceList());
		
		assertEquals(unexpectedSequence.get(0),violation.getViolation());
		
		assertEquals(ViolationType.UNEXPECTED_EVENT,violation.getViolationType());
		
			
	
	}
	
	
	
	
	
	
	
	
	
	private FineInteractionChecker getAnomaliesRecordingChecker_recordSequence_recordAllAnomalies() {
		FineInteractionChecker checker = new FineInteractionChecker();
		checker.init(getInteractionCheckerSettings_recordSequence_recordAllAnomalies());
		return checker;
	}
	
	private FineInteractionChecker getAnomaliesRecordingChecker_dontRecordSequence_recordAllAnomalies() {
		FineInteractionChecker checker = new FineInteractionChecker();
		checker.init(getInteractionCheckerSettings_dontRecordSequence_recordAllAnomalies());
		return checker;
	}
	
	private FineInteractionChecker getAnomaliesRecordingChecker_recordSequence_dontRecordAllAnomalies() {
		FineInteractionChecker checker = new FineInteractionChecker();
		checker.init(getInteractionCheckerSettings_recordSequence_dontRecordAllAnomalies());
		return checker;
	}

	private FineInteractionChecker getAnomaliesRecordingChecker_dontRecordSequence_dontRecordAllAnomalies() {
		FineInteractionChecker checker = new FineInteractionChecker();
		checker.init(getInteractionCheckerSettings_dontRecordSequence_dontRecordAllAnomalies());
		return checker;
	}
	
}
