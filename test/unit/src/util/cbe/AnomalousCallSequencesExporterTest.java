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
package util.cbe;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import modelsViolations.BctAnomalousCallSequence;

import org.eclipse.tptp.logging.events.cbe.FormattingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recorders.FileCBEViolationsRecorder;
import recorders.ViolationsRecorderException;
import testSupport.TestArtifactsManager;

public class AnomalousCallSequencesExporterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadAnomalousCallSequence() {
		File file = TestArtifactsManager.getUnitTestFile("/util/cbe/AnomalousCallSequenceExporter/1sequence.cbe");
	}
	
	@Test
	public void testStoreAndLoadAnomalousCallSequence() throws ViolationsRecorderException, FormattingException {
		File file = TestArtifactsManager.getNewUnitTestFile("/util/cbe/AnomalousCallSequencesExporter/storeLoadSequence1.cbe");
		
		System.out.println(file.getAbsolutePath());
		
		List<String> stack = new ArrayList<String>();
		for ( StackTraceElement ste : Thread.currentThread().getStackTrace() ){
			stack.add(ste.getClassName()+"."+ste.getMethodName());
		}
		
		
		
		BctAnomalousCallSequence cs = new BctAnomalousCallSequence(
				System.currentTimeMillis(), 
				"CS1",
				"1@local",
				stack.toArray(new String[stack.size()]), 
				new String[]{"A1","A2"},
				new String[]{"T1","T2"},
				"T1", 
				new String[]{"A.b()","B.c()","pack.A.b()"}, 
				"Main.main()");
		
		FileCBEViolationsRecorder recorder = new FileCBEViolationsRecorder(file);
		
		recorder.recordAnomalousCallSequence(cs);
		
		
		
		CBELogLoader loader = new CBELogLoader();
		Object[] entities = loader.loadEntitiesFromCBEFile(file);
		
		assertEquals(1,entities.length);
		
		BctAnomalousCallSequence loadedCS = (BctAnomalousCallSequence) entities[0];
		
		assertEquals(cs, loadedCS);

	}

}