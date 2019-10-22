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
package tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//import it.unimib.disco.lta.alfa.tools.FSAInspector;
///import it.unimib.disco.lta.alfa.tools.FSAInspector.TransitionPath;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import automata.State;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;
import tools.FSAInspector.TransitionPath;

@Ignore("TODO: no time to fix this test")
public class FSAInspectorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetTransitionPaths() {
		FiniteStateAutomaton fsa = getAutomatonA();
		FSAInspector fsaInspector = new FSAInspector();
		List<TransitionPath> res = fsaInspector.getTransitionPaths(fsa, "A-->.*-->.*", ".*", ".*");
		assertEquals(3, res.size());
		
		ArrayList<String> tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->C"));
		assertTrue(tss.contains("A-->B-->E"));
		assertTrue(tss.contains("A-->C-->F"));
		
		res = fsaInspector.getTransitionPaths(fsa, "A-->B-->.*",".*", ".*");
		assertEquals(2, res.size());
		
		tss = getStringTransitions(res);
		
		assertTrue(tss.contains("A-->B-->C"));
		assertTrue(tss.contains("A-->B-->E"));
		
		res = fsaInspector.getTransitionPaths(fsa, "A-->B-->C",".*", ".*");
		assertEquals(1, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->C"));
		
		res = fsaInspector.getTransitionPaths(fsa, "A-->C-->F",".*", ".*");
		assertEquals(1, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->C-->F"));
		
		
		//complex regex
		res = fsaInspector.getTransitionPaths(fsa, ".*-->B-->.*","q0", ".*");
		assertEquals(2, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->C"));
		assertTrue(tss.contains("A-->B-->E"));
		
		//complex regex
		res = fsaInspector.getTransitionPaths(fsa, ".*-->B-->.*","q0", "q.*");
		assertEquals(2, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->C"));
		assertTrue(tss.contains("A-->B-->E"));
		
		//complex regex
		res = fsaInspector.getTransitionPaths(fsa, ".*-->B-->.*","q1", ".*");
		assertEquals(0, res.size());
		
		//complex regex
		res = fsaInspector.getTransitionPaths(fsa, ".*-->B-->.*","q0", "q8");
		assertEquals(1, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->E"));
		
		
		//all
		res = fsaInspector.getTransitionPaths(fsa, ".*-->.*-->.*",".*", ".*");
		assertEquals(3, res.size());
		
		tss = getStringTransitions(res);
		assertTrue(tss.contains("A-->B-->C"));
		assertTrue(tss.contains("A-->B-->E"));
		assertTrue(tss.contains("A-->C-->F"));
		
		//outgoing
		res = fsaInspector.getTransitionPaths(fsa, ".*-->.*","q2", ".*");
		assertEquals(1, res.size());
		
		tss = getStringTransitions(res);
		
		assertTrue(tss.contains("B-->E"));
		
		//outgoing
		res = fsaInspector.getTransitionPaths(fsa, ".*-->.*-->.*",".*", "q9");
		assertEquals(1, res.size());
		
		tss = getStringTransitions(res);
		
		assertTrue(tss.contains("A-->C-->F"));
	}

	private ArrayList<String> getStringTransitions(List<TransitionPath> res) {
		
		ArrayList<String> tss = new ArrayList<String>();
		for ( int i = 0 ; i < res.size(); ++i ){
			tss.add(res.get(i).getTransitionsString());
		}
		
		return tss;
	}

	private FiniteStateAutomaton getAutomatonA() {
		FiniteStateAutomaton fsa = new FiniteStateAutomaton();
		State s0 = createState(0, fsa);
		
		State s1 = createState(1, fsa);
		State s2 = createState(2, fsa);
		State s3 = createState(3, fsa);
		connect(s0, s1, "A", fsa);
		connect(s0, s2, "A", fsa);
		connect(s0, s3, "A", fsa);
		
		State s4 = createState(4, fsa);
		connect(s1, s4, "B", fsa);
		
		State s5 = createState(5, fsa);
		connect(s2, s5, "B", fsa);
		
		State s6 = createState(6, fsa);
		connect(s3, s6, "C", fsa);
		
		State s7 = createState(7, fsa);
		connect(s4, s7, "C", fsa);
		
		State s8 = createState(8, fsa);
		connect(s5, s8, "E", fsa);
		
		State s9 = createState(9, fsa);
		connect(s6, s9, "F", fsa);
		
		
		fsa.setInitialState(s0);
		
		fsa.addFinalState(s7);
		fsa.addFinalState(s8);
		fsa.addFinalState(s9);
		
		FSAInspector.printFSA(fsa);
		
		return fsa;
	}

	private State createState(int i, FiniteStateAutomaton fsa) {
//		State s = new State(i,new Point(0,0),fsa);
//		s.setLabel(""+i);
//		s.setName(""+i);
		
		return fsa.createState(new Point(0,0));
		
	}

	private void connect(State s0, State s1, String string, FiniteStateAutomaton fsa) {
		FSATransition t = new FSATransition(s0,s1,string);
		fsa.addTransition(t);
	}

}
