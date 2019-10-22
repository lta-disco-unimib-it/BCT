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

import static org.junit.Assert.assertEquals;

import it.unimib.disco.lta.ava.SingleAutomataViolationsAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import testSupport.TestArtifactsManager;
import tools.fsa2xml.LazyFSALoader;
import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;


import automata.Automaton;
import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

/**
 * FiniteStateAutomaton method clone() does not work as expected
 * 
 * @author fabrizio
 *
 */
public class Bug160 {

	@Test
	public void testBug() throws FileNotFoundException, LazyFSALoaderException{
		
		FiniteStateAutomaton fsa = LazyFSALoader.loadFSA(TestArtifactsManager.getBugFile("160/27.fsa").getAbsolutePath());
		
		FiniteStateAutomaton clone = (FiniteStateAutomaton) fsa.clone();
		
		assertEquals( fsa.getInitialState().getName(), clone.getInitialState().getName() ) ;
		
		ArrayList<String> names = new ArrayList<String>();
		Map<String,List<Transition>> originalStatesOutgoingTransitions = new HashMap<String,List<Transition>>();
		for ( State s : fsa.getStates() ){
			names.add(s.getName());
			
			List<Transition> fsaTrans = new ArrayList<Transition>() ;
			
			for ( Transition cloneT : fsa.getTransitionsFromState(s) ){
				fsaTrans.add(cloneT);
			}
			originalStatesOutgoingTransitions.put(s.getName(), fsaTrans );
		}
		Collections.sort(names);
		
		
		ArrayList<String> cloneNames = new ArrayList<String>();
		for ( State s : clone.getStates() ){
			cloneNames.add(s.getName());
		}
		Collections.sort(cloneNames);
		
		assertEquals( names, cloneNames );
		
		for ( State s : clone.getStates() ){
			List<String> cloneTrans = new ArrayList<String>() ;
			
			for ( Transition cloneT : clone.getTransitionsFromState(s) ){
				cloneTrans.add(cloneT.getFromState()+"-"+cloneT.getDescription()+"-"+cloneT.getToState());
			}
			
			List<String> fsaSTrans = new ArrayList<String>() ;
			List<Transition> fsaTrans = originalStatesOutgoingTransitions.get(s.getName());
			for ( Transition cloneT : fsaTrans ){
				fsaSTrans.add(cloneT.getFromState()+"-"+cloneT.getDescription()+"-"+cloneT.getToState());
			}
			
			Collections.sort(fsaSTrans);
			
			Collections.sort(cloneTrans);
			
			assertEquals(fsaSTrans, cloneTrans);
		}
	}
}
