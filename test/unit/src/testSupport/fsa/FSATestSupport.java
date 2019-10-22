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

import java.awt.Point;

import automata.State;
import automata.Transition;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;

public class FSATestSupport {
	
	/**
	 * This method create an FSA with a single state and no transitions
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createSingleStateFSA(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		fsa.setInitialState(state);
		fsa.addFinalState(state);
		return fsa;
	}
	public static FiniteStateAutomaton createAutomaton1S1T(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state,"A");
		fsa.setInitialState(state);
		fsa.addFinalState(state);
		fsa.addTransition(t);
		return fsa;
	}
	
	public static FiniteStateAutomaton createAutomaton1S2T(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state,"A");
		Transition t2 = new FSATransition(state,state,"B");
		fsa.setInitialState(state);
		fsa.addFinalState(state);
		fsa.addTransition(t);
		fsa.addTransition(t2);
		return fsa;
	}
	
	
	/**
	 * This method creates a FSA with two states connected by a transition named A
	 * 
	 * q0--A-->*q1*
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createSimpleAutomaton(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state2,"A");
		fsa.setInitialState(state);
		fsa.addFinalState(state2);
		fsa.addTransition(t);
		
		return fsa;
	}
	
	public static FiniteStateAutomaton createLambdaAutomaton(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state2,"");
		fsa.setInitialState(state);
		fsa.addFinalState(state2);
		fsa.addTransition(t);
		
		return fsa;
	}
	
	/**
	 * This method creates a FSA with 3 states connected by transitions
	 * 
	 * q0--A-->q1--B-->*q3*
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createAutomaton3S2T(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		State state3 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state2,"A");
		Transition t2 = new FSATransition(state2,state3,"B");
		fsa.setInitialState(state);
		fsa.addFinalState(state3);
		fsa.addTransition(t);
		fsa.addTransition(t2);
		return fsa;
	}
	/**
	 * This method creates a FSA with 4 states connected by  transitions 
	 * 
	 * q0--A-->q1--B-->q3--C-->*q4*
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createAutomaton4S3T(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		State state3 = fsa.createState(new Point(0,0));
		State state4 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state2,"A");
		Transition t2 = new FSATransition(state2,state3,"B");
		Transition t3 = new FSATransition(state3,state4,"C");
		fsa.setInitialState(state);
		fsa.addFinalState(state4);
		fsa.addTransition(t);
		fsa.addTransition(t2);
		fsa.addTransition(t3);
		return fsa;
	}
	/**
	 * This method creates a FSA with 5 states connected by  transitions 
	 * 
	 * 
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createAutomaton5S4T(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		State state3 = fsa.createState(new Point(0,0));
		State state4 = fsa.createState(new Point(0,0));
		State state5 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state2,"A");
		Transition t2 = new FSATransition(state2,state3,"B");
		Transition t3 = new FSATransition(state3,state4,"C");
		Transition t4 = new FSATransition(state2,state5,"D");
		fsa.setInitialState(state);
		fsa.addFinalState(state4);
		fsa.addFinalState(state5);
		fsa.addTransition(t);
		fsa.addTransition(t2);
		fsa.addTransition(t3);
		fsa.addTransition(t4);
		return fsa;
	}
	
	/**
	 * This method creates a FSA with 5 states connected by  transitions 
	 * 
	 * 
	 * 
	 * @return
	 */
	public static FiniteStateAutomaton createAutomatonComplete(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		State state3 = fsa.createState(new Point(0,0));
		State state4 = fsa.createState(new Point(0,0));
		State state5 = fsa.createState(new Point(0,0));
		State state6 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state3,"A");
		Transition t2 = new FSATransition(state,state2,"B");
		Transition t3 = new FSATransition(state2,state4,"C");
		Transition t4 = new FSATransition(state4,state6,"D");
		Transition t5 = new FSATransition(state4,state5,"E");
		Transition t6 = new FSATransition(state5,state,"F");
		Transition t7 = new FSATransition(state6,state6,"G");
		fsa.setInitialState(state);
		fsa.addFinalState(state);
		fsa.addFinalState(state3);
		fsa.addFinalState(state6);
		fsa.addTransition(t);
		fsa.addTransition(t2);
		fsa.addTransition(t3);
		fsa.addTransition(t4);
		fsa.addTransition(t5);
		fsa.addTransition(t6);
		fsa.addTransition(t7);
		return fsa;
	}
	public static FiniteStateAutomaton createBigAutomatonComplete(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		State state = fsa.createState(new Point(0,0));
		State state2 = fsa.createState(new Point(0,0));
		State state3 = fsa.createState(new Point(0,0));
		State state4 = fsa.createState(new Point(0,0));
		State state5 = fsa.createState(new Point(0,0));
		State state6 = fsa.createState(new Point(0,0));
		State state7 = fsa.createState(new Point(0,0));
		State state8 = fsa.createState(new Point(0,0));
		State state9 = fsa.createState(new Point(0,0));
		State state10 = fsa.createState(new Point(0,0));
		State state11 = fsa.createState(new Point(0,0));
		State state12 = fsa.createState(new Point(0,0));
		State state13 = fsa.createState(new Point(0,0));
		Transition t = new FSATransition(state,state3,"A");
		Transition t2 = new FSATransition(state,state2,"B");
		Transition t3 = new FSATransition(state2,state4,"C");
		Transition t4 = new FSATransition(state4,state6,"D");
		Transition t5 = new FSATransition(state4,state5,"E");
		Transition t6 = new FSATransition(state5,state,"F");
		Transition t7 = new FSATransition(state6,state6,"G");
		Transition t8 = new FSATransition(state6,state7,"H");
		Transition t9 = new FSATransition(state7,state8,"I");
		Transition t10 = new FSATransition(state8,state8,"L");
		Transition t11 = new FSATransition(state3,state9,"M");
		Transition t12 = new FSATransition(state9,state10,"N");
		Transition t13 = new FSATransition(state11,state12,"O");
		Transition t14 = new FSATransition(state12,state13,"P");
		Transition t15 = new FSATransition(state13,state13,"Q");
		Transition t16 = new FSATransition(state11,state10,"R");
		Transition t17= new FSATransition(state10,state2,"S");
		Transition t18 = new FSATransition(state12,state12,"T");
		Transition t19 = new FSATransition(state11,state13,"U");
		Transition t20 = new FSATransition(state3,state12,"V1");
		Transition t21 = new FSATransition(state3,state12,"V2");
		
		
		fsa.setInitialState(state);
		fsa.addFinalState(state);
		fsa.addFinalState(state8);
		fsa.addFinalState(state13);	
		fsa.addTransition(t);
		fsa.addTransition(t2);
		fsa.addTransition(t3);
		fsa.addTransition(t4);
		fsa.addTransition(t5);
		fsa.addTransition(t6);
		fsa.addTransition(t7);
		fsa.addTransition(t8);
		fsa.addTransition(t9);
		fsa.addTransition(t10);
		fsa.addTransition(t11);
		fsa.addTransition(t12);
		fsa.addTransition(t13);
		fsa.addTransition(t14);
		fsa.addTransition(t15);
		fsa.addTransition(t16);
		fsa.addTransition(t17);
		fsa.addTransition(t18);
		fsa.addTransition(t19);
		fsa.addTransition(t20);
		fsa.addTransition(t21);
		return fsa;
	}
	public static FiniteStateAutomaton createStressAutomata(){
		FiniteStateAutomaton fsa =new FiniteStateAutomaton();
		
		
		return fsa;
		
	}
	
}
