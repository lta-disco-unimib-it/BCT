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
package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import oracle.automata.ClosureCalculator;

import tools.fsa2xml.LazyFSALoader;
import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;

import automata.Automaton;
import automata.LambdaCheckerFactory;
import automata.LambdaTransitionChecker;
import automata.State;
import automata.Transition;
import automata.fsa.FiniteStateAutomaton;

public class FSAInspector {
	
	/**
	 * Filter for searching for paths
	 * 
	 * @author Fabrizio Pastore
	 *
	 */
	public static interface PathFilter{
		
		public boolean matchPosition ( Transition t, int position );
	}
	
	public static class FSAStats {

		public int states;
		public int transitions;
		public int nonDeterministicTransitions;
		public int maxTransitionsPerState;
		private List<Double> allTransitionsPerState;
		public int nonDeterministicEvents;
		public int nonDeterministicStates;
		public int distinctEvents;
		public int distinctEventsPerState;
		
		public int getDistinctEvents() {
			return distinctEvents;
		}
		public void setDistinctEvents(int distinctEvents) {
			this.distinctEvents = distinctEvents;
		}
		public int getDistinctEventsPerState() {
			return distinctEventsPerState;
		}
		public void setDistinctEventsPerState(int distinctEventsPerState) {
			this.distinctEventsPerState = distinctEventsPerState;
		}
		public int getNonDeterministicEvents() {
			return nonDeterministicEvents;
		}
		public void setNonDeterministicEvents(int nonDeterministicEvents) {
			this.nonDeterministicEvents = nonDeterministicEvents;
		}
		public int getNonDeterministicStates() {
			return nonDeterministicStates;
		}
		public void setNonDeterministicStates(int nonDeterministicStates) {
			this.nonDeterministicStates = nonDeterministicStates;
		}
		public int getMaxTransitionsPerState() {
			return maxTransitionsPerState;
		}
		public void setMaxTransitionsPerState(int maxTransitionsPerState) {
			this.maxTransitionsPerState = maxTransitionsPerState;
		}
		public int getStates() {
			return states;
		}
		public void setStates(int states) {
			this.states = states;
		}
		public int getTransitions() {
			return transitions;
		}
		public void setTransitions(int transitions) {
			this.transitions = transitions;
		}
		public int getNonDeterministicTransitions() {
			return nonDeterministicTransitions;
		}
		public void setNonDeterministicTransitions(int nonDeterministicTransitions) {
			this.nonDeterministicTransitions = nonDeterministicTransitions;
		}
		
		public double getNonDeterministicTransitionsPerState(){
			return (double)nonDeterministicTransitions/(double)states;
		}
	
		public double getTransitionsPerState(){
			return (double)transitions/(double)states;
		}
		public void setAllTransitionsPerState(
				List<Double> transitionsPerStateMean) {
			this.allTransitionsPerState = transitionsPerStateMean;
			Collections.sort(allTransitionsPerState);
		}
		
		public double getTransitionsPerStateMedian(){
			if ( allTransitionsPerState == null || allTransitionsPerState.size() <= 0 ){
				return -1;
			}
			return allTransitionsPerState.get(allTransitionsPerState.size()/2);
		}
	}
	
	public static class PathFilterRegexp implements PathFilter {
		
		private String[] regexps;
		
		/**
		 * Costruct a new PathFilter, the passed strings are regular expressions that indicate the name of the element that can be matched
		 * 
		 * @param regexps
		 */
		public PathFilterRegexp ( List<String> regexps ){
			String exps[] = new String[regexps.size()];
			this.regexps = regexps.toArray(exps);
		}
		
		public PathFilterRegexp(String[] transitionsExpr) {
			regexps = new String[transitionsExpr.length];
			System.arraycopy(transitionsExpr, 0, regexps, 0, transitionsExpr.length);
		}

		public boolean matchPosition ( Transition t, int position ){
			if ( position >= regexps.length )
				return true;
			//System.out.println("match "+t.getFromState()+"-"+t.getDescription()+t.getToState()+" "+position);
			return t.getDescription().matches(regexps[position]);
		}
	}	

	public static class PathFilterExact implements PathFilter {
		
		private String[] regexps;

		/**
		 * Costruct a new PathFilter, the passed strings are regular expressions that indicate the name of the element that can be matched
		 * 
		 * @param regexps
		 */
		public PathFilterExact ( List<String> regexps ){
			String exps[] = new String[regexps.size()];
			this.regexps = regexps.toArray(exps);
		}
		
		

		public boolean matchPosition ( Transition t, int position ){
			return t.getDescription().equals(regexps[position]);
		}
	}

	public static class TransitionPath{
	
		private List<Transition> transitions = new ArrayList<Transition>();
		private List<State> states = new ArrayList<State>();
		
		
		public TransitionPath( State s ){
			states.add(s);
		}
		
		public TransitionPath( TransitionPath rhs ){
			states.addAll(rhs.states);
			transitions.addAll(rhs.transitions);
		}
		
		public void addStep( Transition t, State s ){
			transitions.add(t);
			states.add(s);
		}

		public List<State> getStates() {
			return states;
		}

		public List<Transition> getTransitions() {
			return transitions;
		}

		public Transition getTransitionAtLevel( int level ) {
			return transitions.get(level);
		}
		
		public State getLastState() {
			return states.get(states.size()-1);
		}

		public int length() {
			return transitions.size();
		}
		
		public String getTransitionsString(){
			StringBuffer msg = new StringBuffer();
			
			for ( int i = 0; i < transitions.size(); ++i ){
				if ( i > 0 ){
					msg.append("-->");
				}
				
				msg.append(transitions.get(i).getDescription());
				
			}
			
			return msg.toString();
			
		}
		
		public String toString(){
			StringBuffer msg = new StringBuffer();
			msg.append("PATH: ");
			msg.append(" ("+states.get(0).getName()+") ");
			if ( transitions.size() > 0 ){
				msg.append("-->");
			}
			for ( int i = 0; i < transitions.size(); ++i ){
				msg.append(transitions.get(i).getDescription());
				msg.append("-->");
				msg.append(" ("+states.get(i+1).getName()+") ");
			}
			
			return msg.toString();
		}
		
		public boolean equals ( Object o ){
			if ( o == this ){
				return true;
			}
			if ( ! ( o instanceof TransitionPath ) ){
				return false;
			}
			
			TransitionPath rhs = (TransitionPath)o;
			 if ( ! this.states.equals(rhs.states))
				 return false;
			 return this.transitions.equals(rhs.transitions);
		}
		
		public int hashCode(){
			return toString().hashCode();
		}

		public boolean matchSequence(List<String> sequence) {
			if ( transitions.size() != sequence.size() )
				return false;
			int size = transitions.size();
			for ( int i = 0; i < size; ++i ){
				Transition t = transitions.get(i);
				if ( ! ( t.getDescription().equals(sequence.get(i)) ) ){
					return false;
				}
			}
			
			return true;
		}

		public State getFirstState() {
			return states.get(0);
		}

		public void removeLastStep() {
			states.remove(states.size()-1);
			transitions.remove(transitions.size()-1);
		}
	}
	
	
	public static class NoSuchStateException extends Exception {
		private String name;
		
		public NoSuchStateException(String stateName) {
			super("Cannot find "+stateName);
			name = stateName;
		}
		
		public String getStateName(){
			return name;
		}
	}

	private int level = 2;
	

	private String outGoing = null;
	private String transitionExpr = null;
	private String transitionPath = null;
	
	private String tgfFile = null;
	private boolean print = false;
	private boolean printFSA = false;
	private boolean incomingFinal = false;
	private boolean fineAnalysis = false;
	private List<FSAStats> stats = new ArrayList<FSAStats>();
	
	public List<FSAStats> getStats() {
		return stats;
	}

	public void setStats(List<FSAStats> stats) {
		this.stats = stats;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isFineAnalysis() {
		return fineAnalysis;
	}

	public void setFineAnalysis(boolean ndfaAnalysis) {
		this.fineAnalysis = ndfaAnalysis;
	}

	public String getIncoming() {
		return incoming;
	}

	public void setIncoming(String incoming) {
		this.incoming = incoming;
	}

	private String incoming = null;


	
	
	public String getOutGoing() {
		return outGoing;
	}

	public void setOutGoing(String outGoing) {
		this.outGoing = outGoing;
	}

	public String getTransitionExpr() {
		return transitionExpr;
	}

	public void setTransitionExpr(String transitionExpr) {
		this.transitionExpr = transitionExpr;
	}

	public String getTransitionPath() {
		return transitionPath;
	}

	public void setTransitionPath(String transitionPath) {
		this.transitionPath = transitionPath;
	}

	public String getTgfFile() {
		return tgfFile;
	}

	public void setTgfFile(String tgfFile) {
		this.tgfFile = tgfFile;
	}

	public boolean isPrint() {
		return print;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}
	
	public boolean isPrintFSA() {
		return printFSA;
	}

	public void setPrintFSA(boolean print) {
		this.printFSA = print;
	}

	public boolean isIncomingFinal() {
		return incomingFinal;
	}

	public void setIncomingFinal(boolean incomingFinal) {
		this.incomingFinal = incomingFinal;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		if ( args.length < 2 ){
			printHelp();
			System.exit(-1);
		}
		int i;
		
		List<String> files = new ArrayList<String>();
		FSAInspector inspector = new FSAInspector();
		
		for( i = 0; i < args.length; ++i ){
			if ( ! args[i].startsWith("-") ){
				files.add(args[i]);
			} else if ( args[i].equals("-print") ) {
				inspector.setPrint( true );
			} else if ( args[i].equals("-printFSA") ) {
					inspector.setPrintFSA( true );
			} else if ( args[i].equals("-incomingFinal") ){
				inspector.setIncomingFinal( true );
			} else if ( args[i].equals("-fineAnalysis") ){
				inspector.setFineAnalysis ( true );
			} else {
				if ( i >= args.length -2 ){
					System.err.println("Not enough parameters or Unrecognized option : "+args[i]);
					printHelp();
					System.exit(-1);
				}
				if ( args[i].equals("-outgoing") ){
					inspector.setOutGoing( args[++i].replace("_", "") );
				} else if ( args[i].equals("-incoming") ){
					inspector.setIncoming  ( args[++i] );
				
				} else if ( args[i].equals("-transition") ){
					inspector.setTransitionExpr ( args[++i] );
				}
				else if ( args[i].equals("-exportTGF") ){
					inspector.setTgfFile ( args[++i] );
				}
				else if ( args[i].equals("-transitionsPath") ){
					inspector.setTransitionPath ( args[++i] );
				} else if ( args[i].equals("-level") ){
					inspector.setLevel ( Integer.valueOf(args[++i]) );
				} else {
					System.err.println("Unrecognized option : "+args[i]);
					printHelp();
					System.exit(-1);
				}

			}
		}
		
		for ( String file : files ){
			inspector.process(file);
		}
		
		inspector.processSummary(0);
		
		inspector.processSummary(1);
	}
	
	private void processSummary(int minTransNumber) {
		FSAStats sstats = new FSAStats();
		List<Double> transitionsPerStateMean = new ArrayList<Double>();
		int count = 0;
		for ( FSAStats stat : stats ){
			if ( stat.transitions < minTransNumber ){
				continue;
			}
			count++;
			sstats.states += stat.states;
			sstats.transitions += stat.transitions;
			sstats.nonDeterministicTransitions += stat.nonDeterministicTransitions;
			transitionsPerStateMean.add(stat.getTransitionsPerState());
			if ( stat.maxTransitionsPerState > sstats.maxTransitionsPerState ){
				sstats.maxTransitionsPerState = stat.maxTransitionsPerState;
			}
			sstats.distinctEvents += stat.distinctEvents;
			sstats.distinctEventsPerState += stat.distinctEventsPerState;
			sstats.nonDeterministicEvents += stat.nonDeterministicEvents;
			sstats.nonDeterministicStates += stat.nonDeterministicStates;
		}
		
		
		sstats.setAllTransitionsPerState( transitionsPerStateMean );
		printFSAStats(count+" FSA with at least "+minTransNumber+" transitions",sstats);
	}

	public void process( String file ){
		try {
			
			FiniteStateAutomaton fsa = LazyFSALoader.loadFSA(file);
			if ( print ){
				System.out.println("File: "+file);
				int states = fsa.getStates().length;
				FSAStats fsaStats = new FSAStats();
				fsaStats.states = states;
				fsaStats.transitions = fsa.getTransitions().length;
				
				addStats(fsaStats);
				
				
				
				if ( fineAnalysis ){
					calculateFineAnalysis(fsaStats, fsa);
				}
				
				printFSAStats ( file , fsaStats );
				
				if ( printFSA ){
					printFSA ( fsa );
				}
			}
			if ( incomingFinal ){
				State[] finalStates = fsa.getFinalStates();
				for ( State state : finalStates ){
					Transition[] transitions = fsa.getTransitionsToState(state);
					for ( Transition transition : transitions ){
						System.out.println( transition.getDescription() + " --> " + transition.getToState().getName() );
					}	
				}
			}
			if ( incoming != null || outGoing != null || transitionPath != null ){
//				State state = getState(fsa,outGoing);
//				List<TransitionPath> paths = getPathsFrom(state, level);
//				
//				System.out.println("Paths from "+state.getName());
//				for ( TransitionPath p : paths ){
//					System.out.println(p.toString());
//				}
				
				String stateExpr;
				if ( outGoing == null ){
					stateExpr =".*";
				} else {
					stateExpr = outGoing;
				}
				
				String incomingExpr;
				if ( incoming == null ){
					incomingExpr = ".*";
				} else {
					incomingExpr = incoming;
				}
				
				System.out.println("Searching for path : "+transitionPath + "from state "+ outGoing + " to state "+incomingExpr);
				
				String transitionRegExp;
				if ( transitionPath == null ){
					transitionRegExp = ".*";
				} else {
					transitionRegExp = transitionPath.replace(")", "\\)");
				}
				
				List<TransitionPath> transitionPaths = getTransitionPaths(fsa, transitionRegExp,stateExpr,incomingExpr);
				
				for ( TransitionPath path : transitionPaths ){
					System.out.println(path.toString());
				}
				
//				Transition[] transitions = fsa.getTransitionsFromState(state);
//				for ( Transition transition : transitions ){
//					System.out.println( transition.getDescription() + " --> " + transition.getToState().getName() );
//					
//				}
			}
			if ( transitionExpr != null ) {
				Collection<Transition> transitions = getTransitions(fsa,transitionExpr.replace(")","\\)"),".*");
				for ( Transition transition : transitions ){
					printPathTo( transition.getFromState(), 3 );
					
					System.out.println( " :::"+transition.getFromState().getName() + "-->" + transition.getDescription() + " --> " + transition.getToState().getName() );
					
					printPathFrom( transition.getToState(), level );
					
				}
			}
			
			
			
			if ( tgfFile != null ) {
				exportToTGF( fsa, tgfFile );
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LazyFSALoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

	}



	private void addStats(FSAStats fsaStats) {
		stats.add(fsaStats);
	}

	private void printFSAStats(String string, FSAStats fsaStats) {
		System.out.println ( "Statistics for : "+string);
		System.out.println ( "Number of States : "+fsaStats.states);
		System.out.println ( "Number of Transitions : "+ fsaStats.transitions);
		System.out.println ( "Number of Transitions per state: "+ fsaStats.getTransitionsPerState());
		System.out.println ( "Number of Non Deterministic Transitions : "+fsaStats.nonDeterministicTransitions );
		System.out.println ( "Number of Events generating Non Determinism : "+fsaStats.nonDeterministicEvents );
		System.out.println ( "Number of States with non determinism : "+fsaStats.nonDeterministicStates );
		System.out.println ( "Number of Distinct Events : "+fsaStats.distinctEvents );
		System.out.println ( "Number of Distinct Events per State: "+fsaStats.distinctEventsPerState );
		System.out.println ( "Max Number Transitions in a State : "+fsaStats.maxTransitionsPerState );
		System.out.println ( "Average Non Deterministic Transitions per State : "+fsaStats.getNonDeterministicTransitionsPerState() );
		System.out.println ( "Median of Transitions per state: "+ fsaStats.getTransitionsPerStateMedian());
	}

	private void calculateFineAnalysis(FSAStats fsaStat, FiniteStateAutomaton fsa) {
		int counter = 0;
		int nonDeterministicEvents = 0;
		int nonDeterministicStates = 0;
		
		LambdaTransitionChecker lambdaChecker = LambdaCheckerFactory.getLambdaChecker(fsa);
		
		int transitionsCount = 0;
		HashSet<String> totalEvents = new HashSet<String>();
		
		int allStateEvents = 0;
		
		for ( State s : fsa.getStates() ){
			HashSet<String> transitionsSet = new HashSet<String>();
			HashSet<String> nonDeterministicTransitions = new HashSet<String>();
			
			List<State> states = new ArrayList<State>();
			states.add(s);
			HashSet<String> events = new HashSet<String>();
			List<Transition> transitions = new ArrayList<Transition>();
			
			for ( State cs : ClosureCalculator.calculateClosure(states, fsa) ){
				for ( Transition trans : fsa.getTransitionsFromState(cs) ){
					transitions.add(trans);
				}
			}
			
			transitionsCount+=transitions.size();
			if ( transitions.size() > fsaStat.maxTransitionsPerState ){
				fsaStat.maxTransitionsPerState = transitions.size(); 
			}
			for ( Transition t : transitions ){
				String tDesc = t.getDescription();
				
				events.add(tDesc);
				totalEvents.add(tDesc);
				
				if ( transitionsSet.contains( tDesc ) 
						|| lambdaChecker.isLambdaTransition(t) 
						) {
					
					if ( nonDeterministicTransitions.size() == 0 ){ //the state has non determinism
						nonDeterministicStates++;
					}
					
					//check if LAMBDA
					if ( lambdaChecker.isLambdaTransition(t) 
							){
						tDesc="";
					}
					
					//check if this event in this state was already counted as nondeterministic
					if ( nonDeterministicTransitions.add(tDesc) ){
						nonDeterministicEvents++;
						
						if ( tDesc.length() > 0 ){
							//this is necessary because the first time we find a nondeterministic element we need to add 2
							counter++;  
						}
					}
					
					counter++;
				} else {
					transitionsSet.add( tDesc );
				}
			}
			allStateEvents  += events.size();
			
			
			
		}
		
		fsaStat.distinctEvents = totalEvents.size();
		fsaStat.distinctEventsPerState = allStateEvents;
		fsaStat.transitions = transitionsCount;
		fsaStat.nonDeterministicTransitions = counter;
		fsaStat.nonDeterministicEvents = nonDeterministicEvents;
		fsaStat.nonDeterministicStates = nonDeterministicStates;
	}

	/**
	 * Return a list of paths that match the given regular expression
	 * 
	 * 
	 * 
	 * @param fsa
	 * @param transitionPathExpr
	 * @param incomingExpr 
	 * @return
	 */
	public List<TransitionPath> getTransitionPaths(FiniteStateAutomaton fsa, String transitionPathExpr, String initialStateExpr, String incomingExpr) {
		String[] transitionsExpr = transitionPathExpr.split("-->");
		
		
		String firstTransition = transitionsExpr[0];
		
		Collection<Transition> transitions = getTransitions(fsa, firstTransition, initialStateExpr);
		
		PathFilter filter = new PathFilterRegexp(transitionsExpr);
		List<TransitionPath> paths = new ArrayList<TransitionPath>();
		for ( Transition transition : transitions ){
			System.out.println("Transition ");
			for ( TransitionPath path : getPathsFrom(transition, Math.max(transitionsExpr.length-1,level), filter ) ){
				if ( path.getLastState().getName().matches(incomingExpr) ){
					paths.add(path);
				}
			}
			
			
		}
		
		
		return paths;
		
	}


	private void exportToTGF(FiniteStateAutomaton fsa, String tgfFile) throws IOException {
		File dest = new File( tgfFile );
		
		BufferedWriter bw = new BufferedWriter( new FileWriter( dest ));
		
		HashSet<State> finals = new HashSet<State>();
		for ( State state : fsa.getFinalStates() )
			finals.add(state);
		
		
		
		
		for ( State state : fsa.getStates() ){
			int stateId = state.getID();
			String stateName = state.getName();
			if ( state.equals(fsa.getInitialState()) ){
				stateName=">"+stateName+"<";
			}
			if ( finals.contains(state) )
				stateName="["+stateName+"]";
			bw.write(stateId+" "+stateName+"\n");
		}
		bw.write("#\n");
		for ( Transition transition : fsa.getTransitions() ){
			bw.write(transition.getFromState().getID()+" "+transition.getToState().getID()+" "+transition.getDescription()+"\n");
		}
		
		bw.close();
	}

	private static void printPathTo( State state, int level) {
		if ( level == 0 )
			return;
		
		Automaton fsa = state.getAutomaton();
		Transition[] transitionsTo = fsa.getTransitionsToState(state);
		for ( Transition t : transitionsTo ){
			printPathTo( t.getFromState(), level -1 );
			String tabs="";
			for ( int i = level; i > 0; --i )
				tabs+="\t";
			System.out.println(tabs+t.getDescription()+"-->"+state.getName());
		}
	}
	
	private static void printPathFrom( State state, int level) {
		
		if ( level == 0 )
			return;
		Automaton fsa = state.getAutomaton();
		Transition[] transitionsTo = fsa.getTransitionsFromState(state);
		for ( Transition t : transitionsTo ){
			String tabs="";
			for ( int i = level; i > 0; --i )
				tabs+="\t";
			System.out.println(tabs+state.getName()+"-->"+t.getDescription());
			printPathFrom( t.getToState(), level -1 );
		}
	}
	
	public static List<TransitionPath> getPathsFrom( State state, int level) {
		TransitionPath path = new TransitionPath(state);
		return getPathsFrom( path, level, null );
	}
	
	public static List<TransitionPath> getPathsFrom( Transition t, int level) {
		return getPathsFrom(t, level, null);
	}
	
	public static List<TransitionPath> getPathsFrom( Transition t, int level, PathFilter filter ) {
		TransitionPath path = new TransitionPath(t.getFromState());
		path.addStep(t, t.getToState());
		return getPathsFrom( path, level, filter );
	}
	
	public static List<TransitionPath> getPathsFrom( TransitionPath path, int level, PathFilter filter) {
		ArrayList<TransitionPath> paths = new ArrayList<TransitionPath>();
		if ( level <= 0 ){
			paths.add(path);
			return paths;
		}
		State state = path.getLastState();
		Automaton fsa = state.getAutomaton();
		Transition[] transitionsTo = fsa.getTransitionsFromState(state);
		for ( Transition t : transitionsTo ){
			if ( filter != null && ! filter.matchPosition(t, path.length() ) ){
				continue;
			}
			TransitionPath tpath = new TransitionPath(path);
			tpath.addStep(t, t.getToState());
			paths.addAll(getPathsFrom( tpath, level -1, filter ));
		}
		
		return paths;
	}
	

	static void printFSA(FiniteStateAutomaton fsa) {
		System.out.println ( "FSA : " );
		System.out.println ( "Initial state : "+fsa.getInitialState().getName() );
		System.out.println ( fsa.toString() );
		
	}

	public static Collection<Transition> getTransitions(FiniteStateAutomaton fsa, String transitionExpr, String stateExpr) {
		ArrayList<Transition> ts = new ArrayList<Transition>();
		Transition[] transitions = fsa.getTransitions();
		for ( Transition transition : transitions ){
			if ( transition.getFromState().getName().matches(stateExpr) && transition.getDescription().matches(transitionExpr)){
				ts.add(transition);
			}
		}
		return ts;
	}

	public static State getState(FiniteStateAutomaton fsa, String stateName) throws NoSuchStateException {
		State[] states = fsa.getStates();
		for ( State state : states ){
			
			if ( state.getName().equals(stateName) )
				return state;
		}
		throw new NoSuchStateException( stateName );
	}

	private static void printHelp() {
		System.err.println("This program permits to analyze FSA files.");
		String usage ="\nUsage:\n\t tools.FSAInspector [options] [<serializedFsaFile>]+\n";
		String options="Options are:" +
				"\n-outgoing <state> : returns the names of the transitions that go out from a state\n" +
				"-transition <regexp> : returns transitions whose description match the given java regular expression. For each transition the program prints also the three transitions that preceeds and follow it.\n" +
				"-transitionsPath <pathexp> : returns all the paths that match the given expression,\n" +
				"\t\t<pathexp> should be in the form <regexp>--><regexp>"+
				"-print : print FSA to stdout\n" +
				"-exportTGF <outputFileName> : export fsa into tgf graph format ";
		String example="Examples:" +
				"\nTo print the content of an FSA: java -cp path/to/jar -print myFSA.fsa" +
				"\nTo show the archs going out from state q_6: java -cp path/to/jar -outgoing q_6 myFSA.fsa" +
				"\nTo show the 4 length paths from transitions that starts with \"componenet_startup\":" +
				"\n\tjava -cp path/to/jar -transitionsPath component_startup.*-->.*-->.*-->.*";
		System.err.println(usage+"\n\n"+options+"\n\n"+example);
		
	}

	public static List<TransitionPath> getPathsFrom(State state, int k, PathFilter f) {
		TransitionPath path = new TransitionPath(state);
		return getPathsFrom(path, k, f);
	}

	
}
