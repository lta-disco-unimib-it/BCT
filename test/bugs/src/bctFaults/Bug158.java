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



import static org.junit.Assert.*;

import java.io.File;

import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.FormattingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.TestArtifactsManager;

import util.cbe.CBELogLoader;
import util.cbe.ModelViolationsExporter;

public class Bug158 {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBug() throws FormattingException{
		File logFile = TestArtifactsManager.getBugFile("180/ModelsViolations.cbe.xml");
		CBELogLoader loader = new CBELogLoader();
		CommonBaseEvent[] entities = loader.loadCBE(logFile);
		
		ModelViolationsExporter exporter = new ModelViolationsExporter();
		
		assertEquals(1,entities.length);
		
		
		
		BctModelViolation viol = exporter.loadViolation(entities[0]);
		
		assertEquals("27837@laptop",viol.getPid());
		
		BctIOModelViolation ioViol = (BctIOModelViolation) viol;
		assertEquals("", ioViol.getParameters());
	}
}
