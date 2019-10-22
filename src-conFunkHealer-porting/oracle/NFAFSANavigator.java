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

import oracle.automata.ClosureCalculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

/**
 * Class used to navigate the automaton to following only transitions
 * that equal the input event inserted.
 * Implements also NFA and Lambda transitions.
 * 
 */
public class NFAFSANavigator implements FSANavigator {
	
	private FiniteStateAutomaton fsa;
	
	private Set<State> pointers;
	private Set<State> closureStates;
	HashSet<State> finalStates;

	private boolean validState = true;

	private String method;
	
	/**
	 * Default constructor.
	 * @param fsa the finite state automaton.
	 */
	public NFAFSANavigator(FiniteStateAutomaton fsa, String method) {
		
		
		init(fsa,method);
	}
	
	/**
	 * Method used to instantiate and initialize variables.
	 * @param method 
	 * @param fsa2 
	 */
	public void init(FiniteStateAutomaton fsa, String method) {
		this.fsa = fsa;
		this.method = method;
		this.pointers = new HashSet<State>();
		pointers.add(fsa.getInitialState());
		
		validState=true;
		
		finalStates = new HashSet<State>();
		for ( State state : fsa.getStates() ){
			finalStates.add(state);
		}
		
		
	}
	
	/**
	 * Return the state pointers.
	 * @return the state pointers.
	 */
	public Set<State> getCurrentStates() {
		return pointers;
	}
	
	public List<Transition> getOutgoingTransitions(){
		
		updateClosure();
		
		List<Transition> result = new ArrayList<Transition>();
		
		
		//for each pointer in the list
		for(State state : closureStates) {
			//get transitions emanating from the state
			Transition transitions[] = fsa.getTransitionsFromState(state);
			
			//for each transition
			for(Transition transition : transitions) {
				//if it equals with the event
				result.add(transition);
			}
		}
		
		
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see it.unimib.disco.lta.conFunkHealer.oracle.FSANavigator#navigate(java.lang.String, boolean)
	 */
	public boolean newEvent(String event ) {
		
		updateClosure();
		
		Set<State> newPointers = new HashSet<State>();
		
		
		//for each pointer in the list
		for(State state : closureStates) {
			//get transitions emanating from the state
			Transition transitions[] = fsa.getTransitionsFromState(state);
			
			//for each transition
			for(Transition transition : transitions) {
				//if it equals with the event
				if(transition.getDescription().equals(event)) {
					//add transition's toState in the newPointers' list
					newPointers.add(transition.getToState());
				}
			}
		}
		
		if(newPointers.size() > 0) {
			//if it is true update pointers else continue without update
			//closure is not recalculated

			pointers = newPointers;
			
			return true;
		} else {
			
			validState=false;
			
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see it.unimib.disco.lta.conFunkHealer.oracle.FSANavigator#clone()
	 */
	public Object clone() {
		NFAFSANavigator myClone = new NFAFSANavigator(this.fsa,this.method);
		
		myClone.pointers = new HashSet<State>(this.pointers);
		myClone.validState = validState;
		return myClone;
	}

	/* (non-Javadoc)
	 * @see it.unimib.disco.lta.conFunkHealer.oracle.FSANavigator#reset(automata.State)
	 */
	public void reset(State startingState) {
		this.pointers = new HashSet<State>();
		pointers.add(startingState);
		validState = true;
	}

	public boolean isValidState() {
		return validState;
	}
	
	public String getCorrespondingMethod(){
		return method;
	}
	
	public void updateClosure(){
		closureStates = ClosureCalculator.calculateClosure(pointers, fsa);
	}

	/**
	 * Returns true if one of the current states is final
	 * 
	 */
	public boolean finalState() {
		updateClosure();
		
		for ( State state : fsa.getFinalStates() ){
			if ( closureStates.contains(state) ){
				return true;
			}
		}
		validState = false;
		return false;
	}
	
	
	public FiniteStateAutomaton getFSA() {
		return fsa;
	}
}
