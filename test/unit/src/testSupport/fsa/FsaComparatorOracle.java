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
package testSupport.fsa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

/**
 * This oracle provides methods to compare different fsa
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FsaComparatorOracle {
	/**
	 * This method checks that two FSA are equals.
	 * This happens when all the transition, the states, the initial state and the final states are equals. 
	 * 
	 * Two states are equals when they have the same name.
	 * 
	 * Two transitions are equals when they have the same description andtheir from and to states are equals.
	 * 
	 * @param fsaExpected
	 * @param fsaActual
	 */
	public static void assertFsaEquals(FiniteStateAutomaton fsaExpected,
		FiniteStateAutomaton fsaActual) {
		
		if ( fsaExpected.getInitialState() != null || fsaActual.getInitialState() != null ){
		
			if ( fsaExpected.getInitialState() == null ){
				fail("Initial states do not match expected null found "+fsaActual.getInitialState().getName());
			} else if ( fsaActual.getInitialState() == null ){
				fail("Initial states do not match expected "+fsaExpected.getInitialState().getName()+" found null");
			}
			
			assertEquals( "Initial states do not match" ,fsaExpected.getInitialState().getName(), fsaActual.getInitialState().getName() );
			
		}
		
		State statesFsa[] = fsaExpected.getStates();
        State statesFsaToCompare[] = fsaActual.getStates();
        
        assertStatesEquals(statesFsa,statesFsaToCompare);
        
        State[] expectedFinal = fsaExpected.getFinalStates();
        State[] actualFInal = fsaActual.getFinalStates();
        
        assertStatesEquals(expectedFinal, actualFInal);
        
		Transition transitionFsa[] = fsaExpected.getTransitions();
		Transition transitionFsaToCompare[] = fsaActual.getTransitions();
		
		assertTransitionsEquals(transitionFsa,transitionFsaToCompare);
		
		
	}

	/**
	 * This method checks that two arrays of states are equals. 
	 * They are equals when their elements are equals without considering the ordering.
	 * 
	 * Two states are equals when they have the same name.
	 * 
	 * @param statesFsaExpected
	 * @param statesFsaActual
	 */
	private static void assertStatesEquals(State[] statesFsaExpected, State[] statesFsaActual) {
		
		
		ArrayList<String> statesNamesExpected = new ArrayList<String>();
		ArrayList<String> statesNamesActual = new ArrayList<String>();
		
		
		//populate
		for ( State s : statesFsaExpected ){
			statesNamesExpected.add(s.getName());
		}
		
		for ( State s : statesFsaActual ) {
			statesNamesActual.add(s.getName());
		}
		
		
		//check
		for ( String expected : statesNamesExpected ){
			if ( ! statesNamesActual.contains(expected) ){
				fail("State "+expected+" not found");
			}
		}
		
		for ( String actual : statesNamesActual ){
			if ( ! statesNamesExpected.contains(actual) ){
				fail("State "+actual+" not expected");
			}
		}
		
		
	}

	/**
	 * This method checks that two arrays of transitions are equals.
	 * This happens when they contain elements which are equals to each other without considering the ordering.
	 * 
	 *  
	 * @param expectedTransitions
	 * @param actualTransitions
	 */
	public static void assertTransitionsEquals(Transition expectedTransitions[],Transition actualTransitions[]){
		
		ArrayList<String> expectedToString = new ArrayList<String>();
		ArrayList<String> actualToString = new ArrayList<String>();
	
		
		//populate
		for ( Transition expected : expectedTransitions ){
			expectedToString.add(getTransitionString(expected));
		}

		for ( Transition actual : actualTransitions ){
			actualToString.add(getTransitionString(actual));
		}
		
		
		//double check
		for ( String expected : expectedToString ){
			if ( ! actualToString.contains(expected) ){
				fail("Transition "+expected+" not found");
			}
		}
		
		for ( String actual : actualToString ){
			if ( ! actualToString.contains(actual) ){
				fail("Transition "+actual+" not expected");
			}
		}
		
		
	}

	/**
	 * Given a transition return its string representation in the form
	 * 
	 * <StateNameFrom>--><transitionDescription>--><StateNameTo>
	 * @param expected
	 * @return
	 */
	private static String getTransitionString(Transition expected) {
		return expected.getFromState().getName()+"-->"+expected.getDescription()+"-->"+expected.getToState().getName();
	}
}
