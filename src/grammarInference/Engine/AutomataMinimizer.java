/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package grammarInference.Engine;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.tree.DefaultTreeModel;

import tools.fsa2xml.LazyFSALoader;
import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;

import automata.Automaton;
import automata.fsa.FiniteStateAutomaton;
import automata.fsa.Minimizer;
import automata.fsa.NFAToDFA;

public class AutomataMinimizer {

	
	public static FiniteStateAutomaton minimizeFSA(FiniteStateAutomaton currentFSA) {
		/*
		 * The minimizer is able to minimize only DFA, therefore we need to transform the
		 * currentFSA into a DFA. 
		 */
		
		 NFAToDFA converter = new NFAToDFA();
		 currentFSA=converter.convertToDFA(currentFSA);
		 
		 Minimizer minimizer = new Minimizer();
		 minimizer.initializeMinimizer();
		 Automaton tmpAutomaton = minimizer.getMinimizeableAutomaton(currentFSA);
	
		 DefaultTreeModel dtm = minimizer.getDistinguishableGroupsTree(tmpAutomaton);
		
/*		EngineConfig.logger.logInfo("Tree Model Built: " + dtm);
		 while (dtm != null) {
			EngineConfig.logger.logInfo("Begin Splitting States");
			ArrayList groups = minimizer.split(tmpAutomaton.getStates(),tmpAutomaton, dtm);
			EngineConfig.logger.logInfo("States Splitted");
			
			dtm = minimizer.getDistinguishableGroupsTree(tmpAutomaton);
			EngineConfig.logger.logInfo("Tree Model Built: " + dtm);
		 }*/
		 
		 
		 currentFSA= (FiniteStateAutomaton) minimizer.getMinimumDfa(tmpAutomaton,dtm);
		 
		 return currentFSA;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for ( String file : args ){
			File fsa = new File( file );
			File orig = new File( fsa.getAbsolutePath()+".bak" );
			fsa.renameTo(orig);
			
			try {
				FiniteStateAutomaton origFSA = LazyFSALoader.loadFSA(orig.getAbsolutePath());
				FiniteStateAutomaton newFSA = minimizeFSA(origFSA);
				
				
				
				fsa = new File( file );
				LazyFSALoader.storeFSA(newFSA, fsa);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LazyFSALoaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
