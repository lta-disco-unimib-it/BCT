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



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testSupport.ViolationsAnalyzerSupport;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManager;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;

@Ignore("FIXME")
public class BctViolationsAnalyzerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSingleAnomalyGraph(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfiguration(vm, fm);
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		
		BctViolationsAnalyzer va = new BctViolationsAnalyzer(vm,fm,fsc,IdManagerAction.INSTANCE);
		
		AnomalyGraph ag = va.analyze("2").getBestAnomalyGraph();
		
		assertNotNull(ag);
		assertEquals(2,ag.getViolations().size());
	}
	
	@Test
	public void testSingleAnomalyGraph2(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfiguration2(vm, fm);
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		
		BctViolationsAnalyzer va = new BctViolationsAnalyzer(vm,fm,fsc,IdManagerAction.INSTANCE);
		AnomalyGraph ag = va.analyze("2").getBestAnomalyGraph();
		
		assertNotNull(ag);
		assertEquals(2,ag.getViolations().size());
	}
	
	@Ignore("FIXME")
	@Test
	public void testMediumAnomalyGraph(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);
		
		fm.addCorrectAction("1");
		fm.addFailingAction("2");
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		
		BctViolationsAnalyzer va = new BctViolationsAnalyzer(vm,fm,fsc,IdManagerAction.INSTANCE);
		
		List<AnomalyGraph> ags = va.analyze("2").getAnomalyGraphs();
		
		assertEquals(4, ags.size());
		
		AnomalyGraph ag0 = ags.get(0);
		assertEquals( 15, ag0.getEdgesNumber());
		assertEquals( 1, ag0.getConnectedComponents().size());
		assertEquals( 8.4, ag0.getGraphInverseCohesion());
		assertEquals( 16, ag0.getMaxWeigth());
		
		AnomalyGraph ag1 = ags.get(1);
		assertEquals( 4, ag1.getEdgesNumber());
		assertEquals( 3, ag1.getConnectedComponents().size());
		assertEquals( 2.22, ag1.getGraphInverseCohesion());
		assertEquals( 6, ag1.getMaxWeigth());
		
		AnomalyGraph ag2 = ags.get(2);
		assertEquals( 1, ag2.getEdgesNumber());
		assertEquals( 5, ag2.getConnectedComponents().size());
		assertEquals( 0.40, ag2.getGraphInverseCohesion());
		assertEquals( 2, ag2.getMaxWeigth());
		
		AnomalyGraph ag3 = ags.get(3);
		assertEquals( 0, ag3.getEdgesNumber());
		assertEquals( 6, ag3.getConnectedComponents().size());
		assertEquals( 0, ag3.getGraphInverseCohesion());
		assertEquals( 0, ag3.getMaxWeigth());
		
		AnomalyGraph ag = va.analyze("2").getBestAnomalyGraph();
		
		
		assertNotNull(ag);
		assertEquals(6,ag.getViolations().size());
		
		assertEquals( 4, ag.getEdgesNumber());
		assertEquals( 3, ag.getConnectedComponents().size());
		assertEquals( 2.22, ag.getGraphInverseCohesion());
		
		 Set<String> vids = ag.getRootViolationsIds();
		
		
		assertEquals(3, vids.size());
		
		assertTrue(vids.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
		
		assertTrue(vids.contains(ViolationsAnalyzerSupport.getIdPrefix()+"4"));
		
		assertTrue(vids.contains(ViolationsAnalyzerSupport.getIdPrefix()+"5"));
		
		
	}
	
	/**
	 * Best anomaly graph is the third
	 */
	@Ignore("FIXME")
	@Test
	public void testComplexAnomalyGraph(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeComplexConfiguration(vm);
		
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		fm.addFailingProcess(pid);
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		
		BctViolationsAnalyzer va = new BctViolationsAnalyzer(vm,fm,fsc,IdManagerProcess.INSTANCE);
		
		List<AnomalyGraph> ags = va.analyze(pid).getAnomalyGraphs();
		
		assertEquals(6, ags.size());
		
		AnomalyGraph ag0 = ags.get(0);
		assertEquals( 45, ag0.getEdgesNumber());
		assertEquals( 1, ag0.getConnectedComponents().size());
		assertEquals( 11.98, ag0.getGraphInverseCohesion());
		assertEquals( 24, ag0.getMaxWeigth());
		
		AnomalyGraph ag1 = ags.get(1);
		assertEquals( 28, ag1.getEdgesNumber());
		assertEquals( 2, ag1.getConnectedComponents().size());
		assertEquals( 10.98, ag1.getGraphInverseCohesion());
		assertEquals( 14, ag1.getMaxWeigth());
		
		AnomalyGraph ag2 = ags.get(2);
		assertEquals( 26, ag2.getEdgesNumber());
		assertEquals( 3, ag2.getConnectedComponents().size());
		assertEquals( 2.65, ag2.getGraphInverseCohesion());
		assertEquals( 13, ag2.getMaxWeigth());
		
		AnomalyGraph ag3 = ags.get(3);
		assertEquals( 9, ag3.getEdgesNumber());
		assertEquals( 5, ag3.getConnectedComponents().size());
		assertEquals( 1.47, ag3.getGraphInverseCohesion());
		assertEquals( 6, ag3.getMaxWeigth());
		
		AnomalyGraph ag4 = ags.get(4);
		assertEquals( 2, ag4.getEdgesNumber());
		assertEquals( 8, ag4.getConnectedComponents().size());
		assertEquals( 0.5, ag4.getGraphInverseCohesion());
		assertEquals( 2, ag4.getMaxWeigth());
		
		AnomalyGraph ag5 = ags.get(5);
		assertEquals( 0, ag5.getEdgesNumber());
		assertEquals( 10, ag5.getConnectedComponents().size());
		assertEquals( 0, ag5.getGraphInverseCohesion());
		assertEquals( 0, ag5.getMaxWeigth());
		
		AnomalyGraph ag = va.analyze(pid).getBestAnomalyGraph();
		
		
		assertNotNull(ag);
		assertEquals(10,ag.getViolations().size());
		
		assertEquals( 26, ag.getEdgesNumber());
		assertEquals( 3, ag.getConnectedComponents().size());
		assertEquals( 2.65, ag.getGraphInverseCohesion());
		
		 Set<String> vids = ag.getRootViolationsIds();
		
		
		assertEquals(3, vids.size());
		
		assertTrue(vids.contains("1"));
		
		assertTrue(vids.contains("9"));
		
		assertTrue(vids.contains("10"));
		
		
	}
	
	
}
