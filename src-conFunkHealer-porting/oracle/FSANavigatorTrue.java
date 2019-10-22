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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

public class FSANavigatorTrue implements FSANavigator {
	
	private ArrayList<Transition> transitions = new ArrayList<Transition>(0);
	
	public List<Transition> getOutgoingTransitions() {
		return transitions;
	}

	public boolean newEvent(String event) {
		return true;
	}

	public void reset(State startingState) {
		
	}

	public FSANavigator clone(){
		return new FSANavigatorTrue();
	}

	public boolean isValidState() {
		return true;
	}

	public Set<State> getCurrentStates() {
		return new HashSet<State>();
	}

	public void init(FiniteStateAutomaton fsa, String method) {
		
	}
	
	public String getCorrespondingMethod(){
		return null;
	}

	public boolean finalState() {
		return true;
	}
	
	public FiniteStateAutomaton getFSA() {
		return null;
	}
}
