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
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import testSupport.ViolationsAnalyzerSupport;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;

public class FilteringStrategyCorrectOutTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetViolations1() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfiguration(vm, fm);
		
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		ArrayList<String> violIds = new ArrayList<String>();
		for ( BctRuntimeData bctMV : viols ){
			violIds.add(bctMV.getId());
		}
		
		assertEquals(2, violIds.size());
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
	}
	
	@Test
	public void testGetViolations2() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfigurationAllFaulty(vm, fm);
		
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		ArrayList<String> violIds = new ArrayList<String>();
		for ( BctRuntimeData bctMV : viols ){
			violIds.add(bctMV.getId());
		}
		
		assertEquals(2, violIds.size());
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
	}
	
	@Test
	public void testGetViolations3() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfigurationAllCorrect(vm, fm);
		
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		ArrayList<String> violIds = new ArrayList<String>();
		for ( BctRuntimeData bctMV : viols ){
			violIds.add(bctMV.getId());
		}
		
		assertEquals(0, violIds.size());
		
		
	}
	
	@Test
	public void testGetViolations4() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		ViolationsAnalyzerSupport.makeSimpleConfiguration2(vm, fm);
		
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
		
		ArrayList<String> violIds = new ArrayList<String>();
		for ( BctRuntimeData bctMV : viols ){
			violIds.add(bctMV.getId());
		}
		
		assertEquals(2, violIds.size());
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
		
		assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
		
	}
	
	@Test
	public void testGetViolations5() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeSimpleConfiguration2AllFaulty(vm, fm);


		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");
			

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(3, violIds.size());

			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"6"));

			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));

			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
		}

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "1");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(1, violIds.size());

			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"1"));

		}
	}

	@Test
	public void testGetViolations6() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeSimpleConfiguration2AllCorrect(vm, fm);


		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(0, violIds.size());

			
		}

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "1");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(0, violIds.size());


		}
	}
	
	@Test
	public void testGetViolations7() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeSimpleConfiguration2AllCorrect2(vm, fm);


		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(0, violIds.size());

			
		}

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "1");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(0, violIds.size());


		}
	}
	
	@Test
	public void testGetViolations8() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);

		fm.addCorrectAction("1");
		fm.addFailingAction("2");

		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(6, violIds.size());
			
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"4"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"5"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"7"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"8"));
		}

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "1");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(0, violIds.size());
		}
	}

	@Test
	public void testGetViolations9() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);

		fm.addFailingAction("1");
		fm.addFailingAction("2");

		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "2");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(7, violIds.size());
			
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"4"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"5"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"6"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"7"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"8"));
		}

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerAction.INSTANCE, "1");

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(1, violIds.size());
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"1"));
		}
	}
	
	@Test
	public void testGetViolations10() {
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();

		ViolationsAnalyzerSupport.makeMediumConfiguration(vm);
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		fm.addFailingProcess(pid);
		

		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();

		{
			List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerProcess.INSTANCE, pid);

			ArrayList<String> violIds = new ArrayList<String>();
			for ( BctRuntimeData bctMV : viols ){
				violIds.add(bctMV.getId());
			}

			assertEquals(8, violIds.size());
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"1"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"2"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"3"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"4"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"5"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"6"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"7"));
			assertTrue(violIds.contains(ViolationsAnalyzerSupport.getIdPrefix()+"8"));
		}

	}
	
}
