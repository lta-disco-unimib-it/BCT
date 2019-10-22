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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;




@Ignore("FIXME")
public class InteractionCheckerPerformanceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleAutomaton_dontRecordSequence_dontRecordAllAnomalies(){
	
		String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		long fineTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_DFA_Test.class.getCanonicalName()+"."+testName);
		
		long fineNFATime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_NFA_Test.class.getCanonicalName()+"."+testName);
		
		long defaultTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(InteractionInvariantHandlerTest.class.getCanonicalName()+"."+testName);
		
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineNFATime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < fineNFATime );
	}
	
	@Test
	public void testSimpleAutomaton_newFinal_dontRecordSequence_dontRecordAllAnomalies(){
		String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		long fineTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_DFA_Test.class.getCanonicalName()+"."+testName);
		
		long fineNFATime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_NFA_Test.class.getCanonicalName()+"."+testName);
		
		long defaultTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(InteractionInvariantHandlerTest.class.getCanonicalName()+"."+testName);
		
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineNFATime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < fineNFATime );
	}
	

	
	
	@Test
	public void testSimpleAutomaton_newBranch_dontRecordSequence_dontRecordAllAnomalies(){
		String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		long fineTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_DFA_Test.class.getCanonicalName()+"."+testName);
		
		long fineNFATime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_NFA_Test.class.getCanonicalName()+"."+testName);
		
		long defaultTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(InteractionInvariantHandlerTest.class.getCanonicalName()+"."+testName);
		
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineNFATime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < fineNFATime );
	}
	
	
	@Test
	public void testSimpleAutomaton_newTail_dontRecordSequence_dontRecordAllAnomalies(){
		String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		long fineTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_DFA_Test.class.getCanonicalName()+"."+testName);
		
		long fineNFATime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_NFA_Test.class.getCanonicalName()+"."+testName);
		
		long defaultTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(InteractionInvariantHandlerTest.class.getCanonicalName()+"."+testName);
		
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineNFATime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < fineNFATime );
	}
	
	@Test
	public void testSimpleAutomaton_newBranch_newTail_dontRecordSequence_dontRecordAllAnomalies(){
		String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
		
		long fineTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_DFA_Test.class.getCanonicalName()+"."+testName);
		
		long fineNFATime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(FineInteractionChecker_NFA_Test.class.getCanonicalName()+"."+testName);
		
		long defaultTime = InteractionCheckerPerformanceRegistry.INSTANCE.getExcutionTime(InteractionInvariantHandlerTest.class.getCanonicalName()+"."+testName);
		
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineNFATime < defaultTime );
		
		assertTrue( "fineTime: "+fineTime+" defaultTime: "+defaultTime, fineTime < fineNFATime );	
	}
	
	
	
}
