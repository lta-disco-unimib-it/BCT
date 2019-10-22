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

import java.lang.management.ManagementFactory;
import java.util.List;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.ViolationsAnalyzerSupport;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraphCreator;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTree;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTreeCreator;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;
import tools.violationsAnalyzer.filteringStrategies.IdManagerTest;

public class AnomalyGraphCreatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfiguration(vm, fm);
		
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);

		AnomalyGraph ag = AnomalyGraphCreator.createAnomalyGraph(dc);
		
		BctRuntimeData v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctRuntimeData v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");
		
		assertEquals( 6, ag.getEdgeWeigth(v2,v3),0.001);
		assertEquals(-1, ag.getEdgeWeigth(v3,v2),0.001);
	}

	@Test
	public void test2(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);
		
		fm.addCorrectAction("1");
		fm.addFailingAction("2");
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);

		AnomalyGraph ag = AnomalyGraphCreator.createAnomalyGraph(dc);
		
		BctRuntimeData v1 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"1");
		BctRuntimeData v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctRuntimeData v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");
		BctRuntimeData v4 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"4");
		BctRuntimeData v5 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"5");
		BctRuntimeData v6 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"6");
		BctRuntimeData v7 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"7");
		BctRuntimeData v8 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"8");
		
		assertEquals( -1, ag.getEdgeWeigth(v1,v2), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v3), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v4), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v5), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v6), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v7), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v1,v8), 0.001 );
		
		assertEquals( 6, ag.getEdgeWeigth(v2,v3), 0.001 );
		assertEquals( 11, ag.getEdgeWeigth(v2,v4), 0.001 );
		assertEquals( 11, ag.getEdgeWeigth(v2,v5), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v2,v6), 0.001 );
		assertEquals( 15, ag.getEdgeWeigth(v2,v7), 0.001 );
		assertEquals( 13, ag.getEdgeWeigth(v2,v8), 0.001 );
		
		assertEquals( 11, ag.getEdgeWeigth(v3,v4), 0.001 );
		assertEquals( 11, ag.getEdgeWeigth(v3,v5), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v3,v6), 0.001 );
		assertEquals( 15, ag.getEdgeWeigth(v3,v7), 0.001 );
		assertEquals( 13, ag.getEdgeWeigth(v3,v8), 0.001 );
		
		assertEquals( 16, ag.getEdgeWeigth(v4,v5), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v4,v6), 0.001 );
		assertEquals( 20, ag.getEdgeWeigth(v4,v7), 0.001 );
		assertEquals( 18, ag.getEdgeWeigth(v4,v8), 0.001 );
		
		assertEquals( -1, ag.getEdgeWeigth(v5,v6), 0.001 );
		assertEquals( 20, ag.getEdgeWeigth(v5,v7), 0.001 );
		assertEquals( 18, ag.getEdgeWeigth(v5,v8), 0.001 );
		
		assertEquals( -1, ag.getEdgeWeigth(v6,v7), 0.001 );
		assertEquals( -1, ag.getEdgeWeigth(v6,v8), 0.001 );
		
		assertEquals( 22, ag.getEdgeWeigth(v7,v8), 0.001 );
		
		
	}
	

	@Test
	public void test3(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		fm.addFailingProcess(pid);
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerProcess.INSTANCE, pid);
		
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);

		AnomalyGraph ag = AnomalyGraphCreator.createAnomalyGraph(dc);
		
		BctRuntimeData v1 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"1");
		BctRuntimeData v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctRuntimeData v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");
		BctRuntimeData v4 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"4");
		BctRuntimeData v5 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"5");
		BctRuntimeData v6 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"6");
		BctRuntimeData v7 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"7");
		BctRuntimeData v8 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"8");
		
		assertEquals( 6, ag.getEdgeWeigth(v1,v2),0.001);
		assertEquals( 6, ag.getEdgeWeigth(v1,v3),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v1,v4),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v1,v5),0.001);
		assertEquals( 9, ag.getEdgeWeigth(v1,v6),0.001);
		assertEquals( 15, ag.getEdgeWeigth(v1,v7),0.001);
		assertEquals( 13, ag.getEdgeWeigth(v1,v8),0.001);
		
		assertEquals( 6, ag.getEdgeWeigth(v2,v3),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v2,v4),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v2,v5),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v2,v6),0.001);
		assertEquals( 15, ag.getEdgeWeigth(v2,v7),0.001);
		assertEquals( 13, ag.getEdgeWeigth(v2,v8),0.001);
		
		assertEquals( 11, ag.getEdgeWeigth(v3,v4),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v3,v5),0.001);
		assertEquals( 11, ag.getEdgeWeigth(v3,v6),0.001);
		assertEquals( 15, ag.getEdgeWeigth(v3,v7),0.001);
		assertEquals( 13, ag.getEdgeWeigth(v3,v8),0.001);
		
		assertEquals( 16, ag.getEdgeWeigth(v4,v5),0.001);
		assertEquals( 16, ag.getEdgeWeigth(v4,v6),0.001);
		assertEquals( 20, ag.getEdgeWeigth(v4,v7),0.001);
		assertEquals( 18, ag.getEdgeWeigth(v4,v8),0.001);
		
		assertEquals( 16, ag.getEdgeWeigth(v5,v6),0.001);
		assertEquals( 20, ag.getEdgeWeigth(v5,v7),0.001);
		assertEquals( 18, ag.getEdgeWeigth(v5,v8),0.001);
		
		assertEquals( 20, ag.getEdgeWeigth(v6,v7),0.001);
		assertEquals( 18, ag.getEdgeWeigth(v6,v8),0.001);
		
		assertEquals( 22, ag.getEdgeWeigth(v7,v8),0.001);
		
		
	}
	
	
	
}
