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
import java.io.FileNotFoundException;

import automata.State;
import automata.fsa.FiniteStateAutomaton;
import grammarInference.Engine.KBehaviorEngine;
import grammarInference.Log.ConsoleLogger;
import junit.framework.TestCase;
import testSupport.TestArtifactsManager;


public class Bug149Test extends TestCase {

	
	/**
	 * Empty trace at the begin of the file
	 */
	@org.junit.Test
	public void testEmptyBegin() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("149/1.txt");
		
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
	

	
	/**
	 * Empty trace in the middle of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyMiddle() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("149/2.txt");
		
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
	 * Empty trace at the end of a file with 3 traces
	 */
	@org.junit.Test
	public void testEmptyEnd3Traces() throws FileNotFoundException{
		File file = TestArtifactsManager.getBugFile("149/3.txt");
		
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
		File file = TestArtifactsManager.getBugFile("149/4.txt");
		
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
		File file = TestArtifactsManager.getBugFile("149/5.txt");
		
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
}
