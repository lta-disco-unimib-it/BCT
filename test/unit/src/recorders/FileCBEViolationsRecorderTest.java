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
package recorders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctIOModelViolation;

import org.eclipse.tptp.logging.events.cbe.FormattingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import testSupport.ViolationsAnalyzerSupport;
import tools.violationsAnalyzer.BctViolationsManager;
import util.RuntimeContextualDataUtil;
import util.cbe.CBELogLoader;
import conf.ConfigurationSettings;
import conf.ViolationsRecorderSettings;
import failureDetection.ExceptionFailure;
import failureDetection.Failure;

public class FileCBEViolationsRecorderTest {
	private FileCBEViolationsRecorder  r;
	File cbeFile;
	
	@Before
	public void setUp() throws Exception {
		r = new FileCBEViolationsRecorder();
		Properties p = new Properties();
		cbeFile = TestArtifactsManager.getNewUnitTestFile("recorders/viol.xml");
		p.setProperty("violationsFile", cbeFile.getAbsolutePath());
		ConfigurationSettings s = new ViolationsRecorderSettings(FileCBEViolationsRecorder.class,p);
		r.init(s);
	}

	@After
	public void tearDown() throws Exception {
		r = null;
	}

	@Test
	public void testRecordIoViolationSingle() throws FormattingException, ViolationsRecorderException {
		
		BctViolationsManager vm = new BctViolationsManager();
		ViolationsAnalyzerSupport.makeComplexConfiguration(vm);
		
		BctRuntimeData v1 = vm.getDatum("12345628@112@localhost@"+"1");
		
		r.recordIoViolation((BctIOModelViolation)v1);
		
		CBELogLoader loader = new CBELogLoader();
		Object[] entities = loader.loadEntitiesFromCBEFile(cbeFile);
		
		assertEquals(1, entities.length);

		assertTrue(v1.equals(entities[0]));
	}

	@Test
	public void testRecordIoViolationMultiple() throws FormattingException, ViolationsRecorderException {
		
		BctViolationsManager vm = new BctViolationsManager();
		ViolationsAnalyzerSupport.makeComplexConfiguration(vm);
		
		BctRuntimeData v1 = vm.getDatum("12345628@112@localhost@"+"1");
		BctRuntimeData v2 = vm.getDatum("12345628@112@localhost@"+"2");
		BctRuntimeData v3 = vm.getDatum("12345628@112@localhost@"+"3");
		
		r.recordIoViolation((BctIOModelViolation)v1);
		r.recordIoViolation((BctIOModelViolation)v2);
		r.recordIoViolation((BctIOModelViolation)v3);
		
		CBELogLoader loader = new CBELogLoader();
		Object[] entities = loader.loadEntitiesFromCBEFile(cbeFile);
		
		assertEquals(3, entities.length);

		assertTrue(v1.equals(entities[0]));
		
		HashMap<String, String> paramatersV1 = ((BctIOModelViolation)entities[0]).getParametersMap();
		assertEquals(2, paramatersV1.size() );
		assertEquals("9",paramatersV1.get("parameter[0]"));
		assertEquals("\"A string\"",paramatersV1.get("parameter[1].toString()"));
		
		
		assertTrue(v2.equals(entities[1]));
		assertTrue(v3.equals(entities[2]));
	}
	
	@Test
	public void testRecordFailure() throws RecorderException, FormattingException{
		String failureId = "12345628@112@localhost@"+"1";
		long time = System.currentTimeMillis();
		String component = "ATest.testX()";
		int thread = 1;
		Failure f = new Failure(failureId,time,component,thread);
		
		r.recordFailure(f);
		
		CBELogLoader loader = new CBELogLoader();
		Object[] entities = loader.loadEntitiesFromCBEFile(cbeFile);
		assertEquals(1, entities.length);
		
		assertTrue(f.equals(entities[0]));
		
	}
	
	@Test
	public void testRecordExceptionFailure() throws RecorderException, FormattingException{
		String failureId = "12345628@112@localhost@"+"1";
		long time = System.currentTimeMillis();
		String component = "ATest.testX()";
		int thread = 1;
		Failure f = new ExceptionFailure(failureId,time,component,thread,NullPointerException.class.getCanonicalName(),"NPE",
				RuntimeContextualDataUtil.retrieveStringStackTrace(Thread.currentThread().getStackTrace()),
				"myMethod"
				);
		
		r.recordFailure(f);
		
		CBELogLoader loader = new CBELogLoader();
		Object[] entities = loader.loadEntitiesFromCBEFile(cbeFile);
		assertEquals(1, entities.length);
		
		assertTrue(f.equals(entities[0]));
		
	}
	

}
