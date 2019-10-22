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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import dfmaker.core.Variable;

public class FilterGdbThreadTraceListener implements GdbThreadTraceListener {

	private HashMap<String,LinkedList<Integer>> map = new HashMap<String,LinkedList<Integer>>();
	private HashSet<String> functionsNotOpened = new HashSet<String>();
	private boolean filterNonTerminatingFunctions = true;
	
	private int counter = 0;
	
	@Override
	public void functionEnter(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		if ( functionsNotClosed.contains(functionName) ){
			return;
		}
		
		counter++;
		LinkedList<Integer> counterL = map.get( functionName );
		
		
		if ( counterL == null ){
			counterL = new LinkedList<Integer>();
			map.put(functionName, counterL);
		} 
		
		counterL.add(counter);
		

	}

	@Override
	public void functionExit(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		if ( functionsNotClosed.contains(functionName) ){
			return;
		}
		
		LinkedList<Integer> counterL = map.get( functionName );
		
		if ( counterL == null ){
			functionsNotOpened.add(functionName);
			return;
		}
		
		Integer lastCounter = counterL.removeLast();
		
		for ( Entry<String,LinkedList<Integer>> e : map.entrySet() ){
			LinkedList<Integer> l = e.getValue();
			if ( l.size() > 0 ){
				if ( l.getLast() > lastCounter ){
					functionsNotClosed.add(functionName);
				}
			}
		}
		
		for(String f : functionsNotClosed ){
			map.remove(f);
		}
	}
	
	Set<String> functionsNotClosed = new HashSet<String>();
	
	public Set<String> getFunctionsNotClosedOrOpened(){
		Set<String> all = new HashSet<String>();
		
		all.addAll(functionsNotOpened);
		all.addAll(functionsNotClosed);
		
		return all;
	}

	public void setFilterNonTerminatingFunctions(boolean b) {
		filterNonTerminatingFunctions  = b;
		
	}

	@Override
	public void traceEnd(String traceName) {
		System.out.println("Functions not closed for trace: "+traceName);
		for( String function : getFunctionsNotClosedOrOpened() ){
			System.out.println("\t"+function);
		}
		
		
		
	}

	@Override
	public void newSession(String sessionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genericProgramPoint(String functionName,
			List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			int threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UsefulTraceSections[] getUsefulTraceSections() {
		return new UsefulTraceSections[]{ GdbThreadTraceListener.UsefulTraceSections.FUNCTIONS };
	}

	@Override
	public void traceStart(File traceFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCode(Integer exitCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testCase(String testName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testFail() {
		// TODO Auto-generated method stub
		
	}

	public Set<String> getFunctionsNotFiltered() {
		return map.keySet();
	}

}
