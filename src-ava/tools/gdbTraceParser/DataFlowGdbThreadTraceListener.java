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
package tools.gdbTraceParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import tools.gdbTraceParser.DataFlowGdbThreadTraceListener.Var;
import tools.gdbTraceParser.DataFlowGdbThreadTraceListener.VarLinesCluster;


import dfmaker.core.Variable;

public class DataFlowGdbThreadTraceListener implements GdbThreadTraceListener {

	private Stack<Map<Var, Object>> previousVarValuesStack;
	private Stack<Map<Var, VarLinesCluster>> previousVarLineClustersStack;

	@Override
	public void newSession(String sessionId) {
		// TODO Auto-generated method stub
		
		
		previousVarLineClustersStack = new Stack<Map<Var, VarLinesCluster>>();
		 previousVarValuesStack = new Stack<Map<Var,Object>>();
	}

	@Override
	public void functionEnter(String functionName, List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments, long threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames) {
		previousVarLineClusters = new HashMap<DataFlowGdbThreadTraceListener.Var, DataFlowGdbThreadTraceListener.VarLinesCluster>();
		previousVarValues = new HashMap<Var, Object>();
		
		if ( previousVarValuesStack.size() > 0 ){
			Map<Var, Object> _previousVarValues = previousVarValuesStack.peek();

			if ( _previousVarValues != null ){
				for ( Var var : _previousVarValues.keySet() ){
					if( ! var.startsWith("<") ){
						previousVarValues.put(var, _previousVarValues.get(var));
					}
				}
			}
		}
		
		previousVarLineClustersStack.push(previousVarLineClusters);
		previousVarValuesStack.push(previousVarValues);
		
		processExecutionPoint(functionName, parameters, localVariables,
				parentLocalVariables, parentArguments, lineNo,
				globalVariableNames, "ENTER");
	}

	@Override
	public void functionExit(String functionName, List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments, List<Variable> returnValues,
			long threadId, String fileName, int lineNo,
			List<StackTraceElement> stack, List<String> globalVariableNames) {
		// TODO Auto-generated method stub
		processExecutionPoint(functionName, parameters, localVariables,
				parentLocalVariables, parentArguments, lineNo,
				globalVariableNames, "EXIT");
		
		removePreviousVariables( functionName );
	}

	private void removePreviousVariables(String functionName) {
		Map<Var,Object> toKeep = new HashMap<DataFlowGdbThreadTraceListener.Var, Object>();
		
		for ( Var var : previousVarValues.keySet() ){
			if( ! var.startsWith("<"+functionName+">") ){
				toKeep.put(var, previousVarValues.get(var));
			}
		}
		
		previousVarValuesStack.pop();
		if ( previousVarValuesStack.size() > 0 ){
		previousVarValues = previousVarValuesStack.peek();
		} else {
			previousVarValues = new HashMap<DataFlowGdbThreadTraceListener.Var, Object>();
		}
		
		previousVarLineClustersStack.pop();
		if ( previousVarLineClustersStack.size() > 0 ){
			previousVarLineClusters = previousVarLineClustersStack.peek();		
		} else {
			previousVarLineClusters = new HashMap<DataFlowGdbThreadTraceListener.Var, DataFlowGdbThreadTraceListener.VarLinesCluster>();
		}
	
		previousVarValues.putAll(toKeep);
	}

	@Override
	public void traceEnd(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UsefulTraceSections[] getUsefulTraceSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void genericProgramPoint(String functionName,
			List<Variable> parameters, List<Variable> localVariables,
			List<Variable> parentLocalVariables,
			List<Variable> parentArguments, int threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames) {
		
		processExecutionPoint(functionName, parameters, localVariables,
				parentLocalVariables, parentArguments, lineNo,
				globalVariableNames, null);
	}

	public void processExecutionPoint(String functionName,
			List<Variable> parameters, List<Variable> localVariables,
			List<Variable> parentLocalVariables,
			List<Variable> parentArguments, int lineNo,
			List<String> globalVariableNames, String type) {
		Map<String, Object> variablesMap = BctGdbCheckingThreadTraceListener.createLocalVariablesMap(parameters, localVariables, parentLocalVariables, parentArguments);
		
		variablesMap = renameVariables( variablesMap, functionName, globalVariableNames );
		
		for ( Entry<String, Object> entry : variablesMap.entrySet() ){
			String line = getLine( functionName, lineNo, type );
			Var varName = new Var( entry.getKey() );
			
			VarLinesCluster varLinesCluster = getVarLinesCluster(varName, line);
			System.out.println("CURRENT_CLUSTER "+varLinesCluster.toString());
			Object value = entry.getValue();
			if ( previousVarValues.containsKey(varName) ){
				Object previousValue = previousVarValues.get(varName);
				if ( isEqual( previousValue , value ) ){
					
						varLinesCluster.addLine( line );
					
				} else {
					varLinesCluster = removeFromCluster( line, varName, varLinesCluster );
				}
			} else {
				
					varLinesCluster.addLine( line );
				
			}
			previousVarValues.put(varName, value);
		}
	}
	
	private VarLinesCluster removeFromCluster(String line, Var varName, VarLinesCluster varLinesCluster) {
		System.out.println("REMOVE "+line+" "+varName);
		
		List<String> removedLines = varLinesCluster.remove( line );
		System.out.println("REMOVED_LINES "+removedLines);
		
		if ( removedLines == null ){
			return varLinesCluster;
		}

		Map<String, VarLinesCluster> linesCluster = varLineClustersMap.get(varName);
		
		for ( String removedLine : removedLines ){
			linesCluster.remove(removedLine);
		}
		
		VarLinesCluster newCluster = new VarLinesCluster(varName);
			newCluster.addLine(line);

		linesCluster.put(line, newCluster);
		
		System.out.println("OLD CLUSTER "+varLinesCluster);
		System.out.println("NEW CLUSTER "+newCluster);
		previousVarLineClusters.put(varName, newCluster);
		
		
		System.out.println("LINES_CLUSTERS " + varLineClustersMap);
		return newCluster;
	}

	private boolean isEqual(Object previousValue, Object value) {
		if ( previousValue == null ){
			if ( value == null ){
				return true;
			}
			return false;
		}
		return previousValue.equals(value);
	}

	private String getLine(String functionName, int lineNo, String type) {
		if ( type == null ){
			return ""+lineNo;
		}
		return functionName+":"+type;
	}

	public VarLinesCluster getVarLinesCluster( Var varName, String line){
		System.out.println("VAR_LINES_CLUSTER "+varName+" "+line);
		Map<String, VarLinesCluster> cluster = varLineClustersMap.get(varName);
		
		if ( cluster == null ){
			cluster = new HashMap<String, DataFlowGdbThreadTraceListener.VarLinesCluster>();
			varLineClustersMap.put(varName, cluster);
		}
		
		VarLinesCluster varLineCluster = cluster.get(line);
		if ( varLineCluster == null ){
			varLineCluster = previousVarLineClusters.get(varName);
			if ( varLineCluster == null ){
				varLineCluster = new VarLinesCluster(varName);
			}
			cluster.put(line, varLineCluster);
		}
		
		
		previousVarLineClusters.put(varName,varLineCluster);
		
		System.out.println(varLineClustersMap);
		
		return varLineCluster;
	}
	
	public static class Var {
		String varName;

		public Var(String varName) {
			super();
			this.varName = varName;
		}

		public boolean startsWith(String string) {
			return varName.startsWith(string);
		}

		public boolean equals(Object arg0) {
			if ( ! ( arg0 instanceof Var ) ){
				return false;
			}
			return varName.equals(((Var)arg0).varName);
		}

		public int hashCode() {
			return varName.hashCode();
		}
		
		public String toString(){
			return varName.toString();
		}
		
		
	}
	
	Map<Var, Object> previousVarValues = new HashMap<Var,Object>();
	Map<Var, VarLinesCluster> previousVarLineClusters = new HashMap<Var,VarLinesCluster>();
	HashMap<Var,Map<String,VarLinesCluster>> varLineClustersMap = new HashMap<Var,Map<String,VarLinesCluster>>();
	
	private static class DifferentPathException extends Exception{
		
	}
	
	public static class VarLinesCluster {
		private LinkedList<String> lines = new LinkedList<String>();
		private Var var;
		private int pos = -1;
		
		public VarLinesCluster( Var var ){
			this.var = var;
		}
		
		public void addLine(String line) {
			pos++;
			if ( pos < lines.size() ){
				if ( ! lines.get(pos).equals(line) ){
					
				}
			}
			lines.add(line);
		}

		public void addLines(List<String> removedLines) {
			lines.addAll(removedLines);
		}

		public List<String> remove(String line) {
			int index = lines.indexOf(line);
			
			
			
			List<String> removed = new ArrayList<String>();
			if ( index == -1 || index == lines.size() - 1 ){
				return  removed;
			}
			
			while ( lines.size() < index ){
				removed.add( lines.removeLast() );
			}
			return removed;
		}
		
		public String toString(){
			return var.toString()+":"+lines.toString();
		}

		@Override
		public boolean equals(Object arg0) {
			if ( ! ( arg0 instanceof VarLinesCluster )){
				return false;
			}
			VarLinesCluster r = ( VarLinesCluster )arg0;
			if (! var.equals(r.var) ){
				return false;
			}
			return lines.equals(r.lines);
		}

		@Override
		public int hashCode() {
			return lines.hashCode();
		}
		
		
	}

	private Map<String, Object> renameVariables(
			Map<String, Object> variablesMap, String functionName,
			List<String> globalVariableNames) {
		
		
		HashMap<String, Object> newVariables = new HashMap<String, Object>();
		for( Entry<String, Object> entry : variablesMap.entrySet() ){
			String varName = entry.getKey();
			if ( ! globalVariableNames.contains(varName) ){
				varName = "<"+functionName+">"+varName;
			}
			newVariables.put(varName, entry.getValue());
		}
		
		return newVariables;
	}

	@Override
	public void traceStart(File traceFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCode(Integer exitCode) {
		// TODO Auto-generated method stub
		
	}

	public static class VarValueClustersCollection {

		private HashMap<Var, Map<String, VarLinesCluster>> map;

		public VarValueClustersCollection(
				HashMap<Var, Map<String, VarLinesCluster>> varLineClustersMap) {
			map = varLineClustersMap;
			normalize();
		}
		
		public Map<String, Set<VarLinesCluster>> getClusters( ){
			Map<String,Set<VarLinesCluster>> newmap = new HashMap<String, Set<VarLinesCluster>>();
			
			for ( Entry<Var, Map<String, VarLinesCluster>> e : map.entrySet() ){
				Var var = e.getKey();
				System.out.println(var);
				Set<VarLinesCluster> set = newmap.get(var.varName);
				if ( set == null ){
					set = new HashSet<DataFlowGdbThreadTraceListener.VarLinesCluster>();
					newmap.put(var.varName, set);
				}
				for ( VarLinesCluster cluster : e.getValue().values() ){
					System.out.println(cluster);
					set.add(cluster);
				}
			}
			
			return newmap;
		}
		
		private void normalize() {
			Set<Var> vars = map.keySet();
			for ( Var var : vars ){
				Map<String, VarLinesCluster> linesMap = map.get(var);
				
				List<VarLinesCluster> clusters = new ArrayList<DataFlowGdbThreadTraceListener.VarLinesCluster>();
				clusters.addAll( linesMap.values() );
				for ( VarLinesCluster cluster : clusters ){
					for ( String line : cluster.lines ){
						linesMap.put(line, cluster);
					}
				}
			}
		}

		public boolean sameValues( String varName, int line1, int line2 ){
			Map<String, VarLinesCluster> cluster = map.get(new Var(varName) );
			VarLinesCluster c1 = cluster.get(""+line1);
			VarLinesCluster c2 = cluster.get(""+line2);
			
			if ( c1 == null || c2 == null ){
				return false;
			}
			
			return c1 == c2;
		}
		
		public boolean sameValues( String varName, int line1, String functionExited ){
			Map<String, VarLinesCluster> cluster = map.get(new Var(varName) );
			VarLinesCluster c1 = cluster.get(""+line1);
			VarLinesCluster c2 = cluster.get(""+functionExited+":EXIT");
			
			if ( c1 == null || c2 == null ){
				return false;
			}
			
			return c1 == c2;
		}
		
		public boolean sameValues( String varName, String functionEntered, int line2 ){
			Map<String, VarLinesCluster> cluster = map.get(new Var(varName) );
			VarLinesCluster c1 = cluster.get(functionEntered+":ENTER");
			VarLinesCluster c2 = cluster.get(""+line2);
			
			if ( c1 == null || c2 == null ){
				return false;
			}
			
			return c1 == c2;
		}
	}
	
	public VarValueClustersCollection getValueClusters() {
		return new VarValueClustersCollection(varLineClustersMap);
	}

	@Override
	public void testCase(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testFail() {
		// TODO Auto-generated method stub
		
	}

}
