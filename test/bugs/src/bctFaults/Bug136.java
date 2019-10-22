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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import testSupport.TestArtifactsManager;
import traceReaders.normalized.VirtualNormalizedIoTrace;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import dfmaker.core.DaikonNormalizedTracesMaker;
import dfmaker.core.SuperstructuresMaker;

public class Bug136 extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public void testBug() throws TraceException, IOException{
		File traceFile = TestArtifactsManager.getBugFile("136/test.dtrace");
		File metaFile = null;//TestArtifactsManager.getBugFile("bugs/XXX/test.meta");
		
		IoTrace trace = new FileIoTrace("main.Persona.saluta((Lmain.Persona;)Ljava.lang.String;)",traceFile,metaFile);
		SuperstructuresMaker m = new SuperstructuresMaker(trace, true);
		
//		HashMap<String,Variable> programPointData = new HashMap<String, Variable>();
//		ProgramPointNormalizer ppn = new ProgramPointNormalizer(true, programPointData, m.getEntrySuperStructure());
//		Vector<Variable> x = ppn.getNormalizedPoint();
		
		DaikonNormalizedTracesMaker tm = new DaikonNormalizedTracesMaker(m, false);
		
		
		
		
		VirtualNormalizedIoTrace normalizedTrace = new VirtualNormalizedIoTrace();
		
		tm.normalizeTrace(trace, normalizedTrace, null, null);
		
		assertEquals( 1, normalizedTrace.getEntryPoints().size() );
		assertEquals( 1, normalizedTrace.getExitPoints().size() );
		assertEquals( 0, normalizedTrace.getObjPoints().size() );
		
		
		List<String> notNull = m.getEntrySuperStructure().getAlwaysNotNull();
		Collections.sort(notNull);
		
		
		ArrayList<String> expectedNotNull = new ArrayList<String>();
		expectedNotNull.add("parameter[0]");
		expectedNotNull.add("parameter[0].nome");
		expectedNotNull.add("parameter[1]");
		expectedNotNull.add("parameter[1].x");
		expectedNotNull.add("parameter[1].x.nome");
		expectedNotNull.add("parameter[2]");
		Collections.sort(expectedNotNull);
		
		assertEquals(expectedNotNull, notNull);
		notNull = m.getExitSuperStructure().getAlwaysNotNull();
		Collections.sort(notNull);
		
		expectedNotNull.add("returnValue");
		Collections.sort(expectedNotNull);
		
		assertEquals(expectedNotNull, notNull);
		
		}

}
