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
package oracle;

import java.util.List;
import java.util.Set;

import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

public interface FSANavigator {

	/**
	 * Method that navigate through the FSA following transitions
	 * that equal the input event and lambda ones.
	 * @param event the name of the correct transition to follow
	 * @return true only if there is a transition that equals the event,
	 * 		   false otherwise
	 */
	public abstract boolean newEvent(String event);
	
	/**
	 * Indicate that the current state is final
	 * Returns true if it is final in the model, otherwise returns false and set 
	 * @return
	 */
	public abstract boolean finalState();
	

	/**
	 * Method used to clone this object.
	 * @return A copy of this FSA navigator object.
	 */
	public abstract Object clone();

	/**
	 * Reset the state of the navigator to the selected one
	 * @param startingState
	 */
	public abstract void reset(State startingState);

	/**
	 * Returns the transitions outgoing from te current state/states
	 * @return
	 */
	public abstract List<Transition> getOutgoingTransitions();
	
	
	/**
	 * Returns weather or not the current state is valid. A state is not valid if the previous event was not accepted by
	 *  the automaton and the request to navigate was done with updatePointers set to true.
	 *  
	 * @return
	 */
	public boolean isValidState();
	
	/**
	 * Returns the list of the current states the navigator points to
	 * @return
	 */
	public Set<State> getCurrentStates();
	
	/**
	 * Initialize the navigator
	 * 
	 * @param fsa
	 * @param method
	 */
	public void init(FiniteStateAutomaton fsa, String method);
	
	/**
	 * Returns the method to which the automata belongs to
	 * @return
	 */
	public String getCorrespondingMethod();

	/**
	 * Returns the finite state automaton this navigator is working on
	 * 
	 * @return
	 */
	public FiniteStateAutomaton getFSA();
}