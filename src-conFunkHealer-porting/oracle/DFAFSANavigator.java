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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

public class DFAFSANavigator implements FSANavigator {

	private FiniteStateAutomaton fsa;
	private State currentState;
	private HashMap<State,HashMap<String,Transition>> transitionsMap = new HashMap<State, HashMap<String,Transition>>();
	private boolean validState = true;
	private String method;
	private HashSet<State> finalStates;
	
	public FiniteStateAutomaton getFsa() {
		return fsa;
	}


	public String getCorrespondingMethod() {
		return method;
	}


	public DFAFSANavigator(FiniteStateAutomaton fsa, String method){
		init(fsa,method);
	}
	

	public void init(FiniteStateAutomaton fsa, String method) {
		this.fsa = fsa;
		this.method = method;
		validState = true;
		currentState = fsa.getInitialState();
		for ( State state : fsa.getStates() ){
			Transition[] ts = fsa.getTransitionsFromState(state);
			
			
			HashMap<String,Transition> transitions = new HashMap<String, Transition>();
			
			
			for ( Transition t : ts ){
				transitions.put(t.getDescription(),t);
			}
			
			transitionsMap.put(state, transitions);
		}
	
		finalStates= new HashSet<State>();
		for ( State state : fsa.getFinalStates() ){
			finalStates.add(state);
		}
	}


	public boolean newEvent(String event) {
		
		
		Transition t = transitionsMap.get(currentState).get(event);
		if ( t == null ){
			validState = false;
			return false;
		}
		
		currentState = t.getToState();
			
		return true;
		
	}

	public void reset(State startingState) {
		currentState = startingState;
	}
	
	/* (non-Javadoc)
	 * @see it.unimib.disco.lta.conFunkHealer.oracle.FSANavigator#clone()
	 */
	public Object clone() {
		DFAFSANavigator myClone = new DFAFSANavigator(this.fsa,this.method);
		
		myClone.currentState = currentState;
		myClone.validState = true;
		
		return myClone;
	}


	public List<Transition> getOutgoingTransitions() {
		ArrayList<Transition> res = new ArrayList<Transition>();
		for ( Transition t : fsa.getTransitionsFromState(currentState) ){
			res.add(t);
		}
		return res;
	}


	public boolean isValidState() {
		return validState;
	}
	
	public Set<State> getCurrentStates(){
		Set<State> states = new HashSet<State>();
		states.add(currentState);
		return states;
	}


	public boolean finalState() {
		return ( validState = finalStates.contains(currentState) );
	}


	public FiniteStateAutomaton getFSA() {
		return fsa;
	}

}
