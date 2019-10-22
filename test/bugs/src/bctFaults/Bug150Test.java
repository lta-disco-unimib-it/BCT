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
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Ignore;

import conf.InteractionInferenceEngineSettings;

import junit.framework.TestCase;

import automata.Automaton;
import automata.State;
import automata.fsa.FiniteStateAutomaton;

import testSupport.TestArtifactsManager;

import tools.ShowFSA;
import grammarInference.Engine.KBehaviorEngine;
import grammarInference.Engine.kTailEngine;
import grammarInference.Log.ConsoleLogger;
import grammarInference.Log.Logger;
//import it.unimib.disco.lta.alfa.inferenceEngines.Test;


public class Bug150Test {

	@Before
	public void setUp() throws Exception {
		InteractionInferenceEngineSettings.logger = new ConsoleLogger(5);
	}
	
	/**
	 * Empty trace at the begin of the file without minimization
	 */
	@org.junit.Test
	public void testEmptyBegin() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/1.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"none",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
		
		checkFinal( fsa, fsa.getInitialState() );
		//ShowFSA.showFSA(fsa);
		
	}
	
	private static void checkFinal(FiniteStateAutomaton fsa, State initialState) {
		initialState.getAutomaton();
		
		for ( State finalState : fsa.getFinalStates() ){
			if ( finalState == initialState ){
				//OK
				assertTrue("State is final",true);
				return;
			}
		}
		
		assertTrue("State is not final",false);
	}

	/**
	 * Empty trace at the begin of the file with step minimization 
	 */
	@org.junit.Test
	public void testEmptyBegin_STEP() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/1.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"step",
				new ConsoleLogger(5)); 
		
		final FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		
		//ShowFSA.showFSA(fsa);
		
		assertEquals( 1, fsa.getFinalStates().length );
		
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		
			
		checkFinal( fsa, fsa.getInitialState() );
		
		
		
		
	}
	
	/**
	 * Empty trace at the begin of the file with end minimization
	 */
	@org.junit.Test
	public void testEmptyBegin_END() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/1.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"end",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		
		checkFinal( fsa, fsa.getInitialState() );
		//ShowFSA.showFSA(fsa);
		
	}
	

	
	/**
	 * Empty trace in the middle of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyMiddle() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/2.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"none",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		
		//ShowFSA.showFSA(fsa);
		
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
		checkFinal( fsa, fsa.getInitialState() );
	}
	
	/**
	 * Empty trace at the end of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyEnd3Traces() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/3.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"none",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * Empty trace at the end of a file with 2 traces
	 */
	@org.junit.Test
	public void testEmptyEnd2Traces() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/4.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"none",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
		checkFinal( fsa, fsa.getInitialState() );
	}
	
	/**
	 * 2 Empty traces in the middle of the file
	 */
	@org.junit.Test
	public void testTwoEmptyMiddle() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/5.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"none",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	
	/**
	 * Empty trace in the middle of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyMiddle_END() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/2.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"end",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		
		assertEquals( 1, fsa.getFinalStates().length );
		
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * Empty trace at the end of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyEnd3Traces_END() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/3.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"end",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());

		assertEquals( 1, fsa.getFinalStates().length );
		
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * Empty trace at the end of a file with 2 traces
	 */
	@org.junit.Test
	public void testEmptyEnd2Traces_END() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/4.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"end",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());

		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * 2 Empty traces in the middle of the file
	 */
	@org.junit.Test
	public void testTwoEmptyMiddle_END() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/5.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"end",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	
	/**
	 * Empty trace in the middle of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyMiddle_STEP() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/2.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"step",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * Empty trace at the end of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyEnd3Traces_STEP() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/3.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"step",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * Empty trace at the end of a file with 2 traces
	 */
	@org.junit.Test
	public void testEmptyEnd2Traces_STEP() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/4.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"step",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	/**
	 * 2 Empty traces in the middle of the file
	 */
	@org.junit.Test
	public void testTwoEmptyMiddle_STEP() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/5.txt");
		
		KBehaviorEngine engine = new KBehaviorEngine(
				2,
				2,
				true,
				"step",
				new ConsoleLogger(5)); 
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 1, fsa.getFinalStates().length );
		assertEquals( fsa.getInitialState(), fsa.getFinalStates()[0] );
		assertEquals( 2, fsa.getStates().length );
		assertEquals( 2, fsa.getTransitions().length );
		checkFinal( fsa, fsa.getInitialState() );
		
	}
	
	
	/**
	 * Empty trace at the begin of the file without minimization
	 */
	@org.junit.Test
	public void testKTailEmptyBegin() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/1.txt");
		
		kTailEngine engine = new kTailEngine(2);
		engine.setEnableMinimization(false);
		
		
		FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
		
		checkFinal( fsa, fsa.getInitialState() );
		//ShowFSA.showFSA(fsa);
		
	}
	
	/**
	 * Empty trace at the begin of the file with step minimization 
	 */
	@Ignore("Double check the expected behavior")
	@org.junit.Test
	public void testKTailEmptyBegin_MINIMIZE() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("150/1.txt");
		
		kTailEngine engine = new kTailEngine(2); 
		engine.setEnableMinimization(true);
		
		final FiniteStateAutomaton fsa = engine.inferFSAfromFile(file.getAbsolutePath());
		
		//ShowFSA.showFSA(fsa);
		
		assertEquals( 3, fsa.getStates().length );
		assertEquals( 3, fsa.getTransitions().length );
		
			
		checkFinal( fsa, fsa.getInitialState() );
		
		
		
		
	}
	

	
}
