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
package tools.fsa2xml.codec.impl;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tools.fsa2xml.FSANamesDecoder;
import tools.fsa2xml.codec.api.FSACodec;
import automata.State;
import automata.Transition;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;

public class FSAdot implements FSACodec{
	//Gli atrtributi rappresentano lo stato di un oggetto.
	//Il codec non ha stato, se è invocato due volte di fila non deve ricordarsi cosa ha fatto la volta prima quindi non ha senso che abbia var di stato.
	
	
	
	
	
	public final static FSAdot INSTANCE = new FSAdot();
	

	public static class AutomataLoader{
		private LabelFactory labelFactory = new LabelFactory();
		private HashMap<Integer,State> keyStatesMapLoad = new HashMap<Integer,State>();
		private String filename;
		private FiniteStateAutomaton fsa;
		private Integer initialStatePos;
		//This list contains arrays of three elements that represent the transition:
		//1st from, 2nd to, 3 description
		private ArrayList transitionsToSave = new ArrayList();
		
		private Map<String,String> namesMapping;
		
		AutomataLoader( String filename ){
			this.filename = filename;
		}
		
		AutomataLoader( String filename, Map<String,String> namesMapping ){
			this.filename = filename;
			this.namesMapping = namesMapping;
		}
		
		/**
		 * Load the fsa from the file this loader is associated to
		 * 
		 * @return
		 * @throws IOException 
		 */
		public void loadFSA() throws IOException{
			BufferedReader r = null;
			try{
			fsa  = new FiniteStateAutomaton();
			r = new BufferedReader( new FileReader(filename) );
			
			
			String line;
			while ( ( line = r.readLine() ) != null ){
				line = line.trim();

				if ( isTransition( line ) ) {
					loadTransition( line );
				}
			}
			
			setTransitions();
			
			setInitialState();
			
			} finally {
				if ( r != null )
					r.close();
			}
		}
		
		/**
		 * Set the initial state in the automaton
		 * 
		 */
		private void setInitialState() {
			fsa.setInitialState(keyStatesMapLoad.get(initialStatePos));
		}

		/**
		 * Add the transitions to the automaton
		 * 
		 */
		private void setTransitions() {
			for ( Object o : transitionsToSave ){
				Object transitionElements[] = (Object[]) o;
				
				String desc;
				if ( isLambda((String) transitionElements[2] ) ){
					desc = "";
				} else {
					desc = (String) transitionElements[2];
				}
				
				Transition t = new FSATransition((State)keyStatesMapLoad.get(transitionElements[0]),(State)keyStatesMapLoad.get(transitionElements[1]),desc);
				
				fsa.addTransition(t);
			}
		}
		
		/**
		 * This method replace get a description formatted for xml saving and return it in original format
		 *  
		 *  Replaces &lt; with <
		 *  
		 *  
		 * @param description
		 * @param labelFactory 
		 * @return
		 */
		private String getUnFormattedDescription(String description, LabelFactory labelFactory) {
			
			description = description.replace("&lt;","<").replace("&quot;", "\"").replace("&amp;", "&").replace("&gt;", ">").replace("&#64;", "@");
			
			description = FSANamesDecoder.getReplacedLabel( description, namesMapping ); 
			
			return labelFactory.getLabel(description);
			
		}

		private boolean isLambda(String object) {
			return object.equals("\u03BB");
		}

		/**
		 * Load an element of type FSA
		 * 
		 * @param line
		 */
		private void loadFSAElement(String line) {
			String[] elements = line.split("\"");
			for ( int i = 0; i < elements.length; ++i ){
				if ( elements[i].endsWith("initialState=") ){
					initialStatePos = Integer.valueOf(elements[i+1].substring(10));
					break;
				}
			}
				
		}

		private boolean isFSA(String line) {
			return line.startsWith("<fsa:FSA");
		}

		/**
		 * parse the given line and load the transition it represents
		 * 
		 * @param line
		 */
		private void loadTransition(String line) {
			String[] s2 = line.split("\"");		
			String from = null;
			String to = null;
			String descr = null;
			
			for ( int i = 0; i < s2.length; ++i ){
				
				if ( s2[i].endsWith("to=") ){
					to = s2[++i];
				} else if ( s2[i].endsWith("from=") ){
					from = s2[++i];
				} else if ( s2[i].endsWith("description=") ){
					descr = getUnFormattedDescription(s2[++i], labelFactory);
				}
			}
			
			int idFrom = Integer.parseInt(from.substring(10, from.length()));	
			int idTo = Integer.parseInt(to.substring(10, to.length()));
			
			transitionsToSave.add(new Object[]{idFrom,idTo,descr} );
			
			
		}
		


		/**
		 * Read the given line and create in the automaton th estate it represent
		 * 
		 * @param line
		 */
		private void loadAndSetState(String line) {
			line=getUnFormattedDescription(line, labelFactory);
			String[] s = line.split("\"");
			State state = fsa.createState(new Point(0,0));
			state.setName(s[1]);
			
			if ( s[2].trim().equals("final=") ){
				if ( s[3].equals("true") ){
					fsa.addFinalState(state);
				}
			}
			
			keyStatesMapLoad.put(keyStatesMapLoad.size(),state);
		}
		
		/**
		 * Return the automaton that corresponds to the file
		 * 
		 * @return
		 * @throws IOException
		 */
		public FiniteStateAutomaton getFSA() throws IOException{
			if ( fsa == null ){
				loadFSA();
			}
			//System.out.println(fsa.toString());
			//System.out.println("Initial "+fsa.getInitialState());
			return fsa;
		}
	}
	
	/**
	 * Load the automaton given its path
	 * 
	 */
	public FiniteStateAutomaton loadFSA(String filename) throws IOException,
	ClassNotFoundException {
		return loadFSA(filename, null);

	}
	
	public FiniteStateAutomaton loadFSA(String filename, Map<String,String> namesMapping) throws IOException,
	ClassNotFoundException {
		AutomataLoader loader = new AutomataLoader(filename, namesMapping);
		return loader.getFSA();

	}

	

	/**
	 * This method return whether or not a line represent a transition
	 * @param line
	 * @return
	 */
	private static boolean isTransition(String line) {
		return line.contains("->");
	}





	public FiniteStateAutomaton loadFSA(File file) throws IOException,
	ClassNotFoundException {

		return loadFSA(file, null);
	}

	public FiniteStateAutomaton loadFSA(File file, Map<String,String> namesMapping) throws IOException,
	ClassNotFoundException {

		return loadFSA(file.getAbsolutePath(),namesMapping);
	}
	
	
	
	/**
	 * Save the passed FSA to the given filename
	 * 
	 */
	public void saveFSA(FiniteStateAutomaton fsa, String filename)
	throws FileNotFoundException, IOException {

		File file = new File(filename);

		//Build  a map in which for every state we associate a position number
		
		HashMap<State, Integer> statesMap = new HashMap<State,Integer>();
		State states[] = fsa.getStates();
		for ( int i = 0; i< states.length; ++i ){
			statesMap.put(states[i],i);
		}

		//Write down states and transitions
		
		BufferedWriter w = null;
		try{
			w = new BufferedWriter( new FileWriter(file) );

			writeHeader(fsa,w,statesMap);

			writeStates(fsa,w,statesMap);

			writeTransition(fsa,w,statesMap);

			writeFooter(fsa,w,statesMap);

			w.close();
		} finally { 
			if ( w != null ){
				w.close();
			}	
		}
	}



	private void writeFooter(FiniteStateAutomaton fsa, BufferedWriter w, HashMap<State, Integer> statesMap) throws IOException {
		w.write("}\n");
		
	}



	private void writeTransition(FiniteStateAutomaton fsa, BufferedWriter w, HashMap<State, Integer> statesMap) throws IOException {

		if(fsa.getTransitions()!=null){
			for ( Transition t : fsa.getTransitions() ){
				w.write(statesMap.get(t.getFromState())+" -> "+statesMap.get(t.getToState()));
				if(t.getDescription()!=null)
					w.write("[label=\""+getFormattedDescription(t.getDescription())+"\"]");
				
				
				w.write(";\n");

			}
		}
		
		
		

	}




	/**
	 * This method replace special characters in description fields in order to be able to save them in xml
	 *
	 *  
	 *  Replaces < with &lt;
	 *  
	 * @param description
	 * @return
	 */
	private String getFormattedDescription(String description) {
		return description.replace("<", "&lt;").replace("\"", "&quot;").replace( "&", "&amp;").replace(">","&gt;").replace( "@", "&#64;");
	}








	/**
	 * Write the states in the order defined by the map
	 * 
	 * @param fsa
	 * @param w
	 * @param statesMap
	 * @throws IOException
	 */
	private void writeStates(FiniteStateAutomaton fsa, BufferedWriter w, HashMap<State, Integer> statesMap) throws IOException {
		
//		State states[] = new State[statesMap.size()];
//		for ( State state : statesMap.keySet() ){
//			Integer pos = statesMap.get(state);
//			states[pos] = state;
//		}
//
//		for ( State state : states ){
//			
//			w.write("<states name=\""+getFormattedDescription(state.getName())+"\" ");
//			if ( fsa.isFinalState(state) ){
//				w.write("final=\"true\" "); 
//			}
//			w.write("fsa=\"/\"");
//			w.write("/>");
//			w.write("\n");
//		}
	}







	/**
	 * Write the header
	 * 
	 * @param fsa
	 * @param w
	 * @param statesMap
	 * @throws IOException
	 */
	private void writeHeader(FiniteStateAutomaton fsa, BufferedWriter w, HashMap<State, Integer> statesMap) throws IOException {

		w.write("digraph g {");
		w.write("\n");

	}





	public void saveFSA(FiniteStateAutomaton o, File file)
	throws FileNotFoundException, IOException {

		saveFSA(o, file.getAbsolutePath());
	}


}
