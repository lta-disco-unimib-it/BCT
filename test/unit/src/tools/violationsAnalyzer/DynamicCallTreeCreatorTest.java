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

import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.ViolationsAnalyzerSupport;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTree;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTreeCreator;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;

public class DynamicCallTreeCreatorTest {

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
		
		BctModelViolation v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctModelViolation v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");

		assertEquals(6, dc.getUndirectedViolationDistance(v2, v3));
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
		
		assertEquals(6, viols.size());
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);
		assertEquals(6,dc.getModelViolations().size());
		
		BctModelViolation v1 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"1");
		BctModelViolation v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctModelViolation v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");
		BctModelViolation v4 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"4");
		BctModelViolation v5 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"5");
		BctModelViolation v6 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"6");
		BctModelViolation v7 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"7");
		BctModelViolation v8 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"8");

		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v2));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v3));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v4));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v5));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v6));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v7));
		assertEquals(-1, dc.getUndirectedViolationDistance(v1, v8));
		
		
		assertEquals(6, dc.getUndirectedViolationDistance(v2, v3));
		assertEquals(11, dc.getUndirectedViolationDistance(v2, v4));
		assertEquals(11, dc.getUndirectedViolationDistance(v2, v5));
		assertEquals(-1, dc.getUndirectedViolationDistance(v2, v6));
		assertEquals(15, dc.getUndirectedViolationDistance(v2, v7));
		assertEquals(13, dc.getUndirectedViolationDistance(v2, v8));
		
		assertEquals(11, dc.getUndirectedViolationDistance(v3, v4));
		assertEquals(11, dc.getUndirectedViolationDistance(v3, v5));
		assertEquals(-1, dc.getUndirectedViolationDistance(v3, v6));
		assertEquals(15, dc.getUndirectedViolationDistance(v3, v7));
		assertEquals(13, dc.getUndirectedViolationDistance(v3, v8));
		
		assertEquals(16, dc.getUndirectedViolationDistance(v4, v5));
		assertEquals(-1, dc.getUndirectedViolationDistance(v4, v6));
		assertEquals(20, dc.getUndirectedViolationDistance(v4, v7));
		assertEquals(18, dc.getUndirectedViolationDistance(v4, v8));
		
		assertEquals(-1, dc.getUndirectedViolationDistance(v5, v6));
		assertEquals(20, dc.getUndirectedViolationDistance(v5, v7));
		assertEquals(18, dc.getUndirectedViolationDistance(v5, v8));
		
		assertEquals(-1, dc.getUndirectedViolationDistance(v6, v7));
		assertEquals(-1, dc.getUndirectedViolationDistance(v6, v8));
		
		assertEquals(22, dc.getUndirectedViolationDistance(v7, v8));
	}
	
	@Test
	public void test3(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeComplexConfiguration(vm);
		
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		fm.addFailingProcess(pid);
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerProcess.INSTANCE, pid);
		
		assertEquals(10, viols.size());
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);
		assertEquals(10,dc.getModelViolations().size());
		
		BctModelViolation v1 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"1");
		BctModelViolation v2 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"2");
		BctModelViolation v3 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"3");
		BctModelViolation v4 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"4");
		BctModelViolation v5 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"5");
		BctModelViolation v6 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"6");
		BctModelViolation v7 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"7");
		BctModelViolation v8 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"8");
		BctModelViolation v9 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"9");
		BctModelViolation v10 = vm.getDatum(ViolationsAnalyzerSupport.getIdPrefix()+"10");

		assertEquals(6, dc.getUndirectedViolationDistance(v1, v2));
		assertEquals(6, dc.getUndirectedViolationDistance(v1, v3));
		assertEquals(11, dc.getUndirectedViolationDistance(v1, v4));
		assertEquals(11, dc.getUndirectedViolationDistance(v1, v5));
		assertEquals(9, dc.getUndirectedViolationDistance(v1, v6));
		assertEquals(15, dc.getUndirectedViolationDistance(v1, v7));
		assertEquals(13, dc.getUndirectedViolationDistance(v1, v8));
		assertEquals(17, dc.getUndirectedViolationDistance(v1, v9));
		assertEquals(17, dc.getUndirectedViolationDistance(v1, v10));
		
		assertEquals(17, dc.getUndirectedViolationDistance(v9, v2));
		assertEquals(17, dc.getUndirectedViolationDistance(v9, v3));
		assertEquals(22, dc.getUndirectedViolationDistance(v9, v4));
		assertEquals(22, dc.getUndirectedViolationDistance(v9, v5));
		assertEquals(22, dc.getUndirectedViolationDistance(v9, v6));
		assertEquals(26, dc.getUndirectedViolationDistance(v9, v7));
		assertEquals(24, dc.getUndirectedViolationDistance(v9, v8));
		assertEquals(28, dc.getUndirectedViolationDistance(v9, v10));
		
		assertEquals(17, dc.getUndirectedViolationDistance(v10, v2));
		assertEquals(17, dc.getUndirectedViolationDistance(v10, v3));
		assertEquals(22, dc.getUndirectedViolationDistance(v10, v4));
		assertEquals(22, dc.getUndirectedViolationDistance(v10, v5));
		assertEquals(22, dc.getUndirectedViolationDistance(v10, v6));
		assertEquals(26, dc.getUndirectedViolationDistance(v10, v7));
		assertEquals(24, dc.getUndirectedViolationDistance(v10, v8));
		
		assertEquals(6, dc.getUndirectedViolationDistance(v2, v3));
		assertEquals(11, dc.getUndirectedViolationDistance(v2, v4));
		assertEquals(11, dc.getUndirectedViolationDistance(v2, v5));
		assertEquals(11, dc.getUndirectedViolationDistance(v2, v6));
		assertEquals(15, dc.getUndirectedViolationDistance(v2, v7));
		assertEquals(13, dc.getUndirectedViolationDistance(v2, v8));
		
		assertEquals(11, dc.getUndirectedViolationDistance(v3, v4));
		assertEquals(11, dc.getUndirectedViolationDistance(v3, v5));
		assertEquals(11, dc.getUndirectedViolationDistance(v3, v6));
		assertEquals(15, dc.getUndirectedViolationDistance(v3, v7));
		assertEquals(13, dc.getUndirectedViolationDistance(v3, v8));
		
		assertEquals(16, dc.getUndirectedViolationDistance(v4, v5));
		assertEquals(16, dc.getUndirectedViolationDistance(v4, v6));
		assertEquals(20, dc.getUndirectedViolationDistance(v4, v7));
		assertEquals(18, dc.getUndirectedViolationDistance(v4, v8));
		
		assertEquals(16, dc.getUndirectedViolationDistance(v5, v6));
		assertEquals(20, dc.getUndirectedViolationDistance(v5, v7));
		assertEquals(18, dc.getUndirectedViolationDistance(v5, v8));
		
		assertEquals(20, dc.getUndirectedViolationDistance(v6, v7));
		assertEquals(18, dc.getUndirectedViolationDistance(v6, v8));
		
		assertEquals(22, dc.getUndirectedViolationDistance(v7, v8));
	}
}
