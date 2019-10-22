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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dfmaker.core.Variable;

/**
 * Assumes that all enter are properly recorded, and just exits can be missing
 * 
 * @author fabrizio
 *
 */
public class FilterNotClosedFunctionCallsGdbThreadTraceListener implements
		GdbThreadTraceListener {

	private int counter=-1;
	private int traceCounter = 0;
	
	private LinkedList<Integer> positionsStack = new LinkedList<Integer>();
	private LinkedList<String> functionsStack = new LinkedList<String>();
	private List<Integer> callsToFilterOut = new ArrayList<Integer>();
	private HashMap<Integer,List<Integer>> functionsNotClosedInTrace = new HashMap<Integer,List<Integer>>();


	private void cleanSingleTraceData(){
		positionsStack = new LinkedList<Integer>();
		functionsStack = new LinkedList<String>();
		callsToFilterOut = new ArrayList<Integer>();
		counter=-1;
	}
	
	public HashMap<Integer, List<Integer>> getFunctionsNotClosedInTrace() {
		return functionsNotClosedInTrace;
	}

	@Override
	public void functionEnter(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			long threadId, String fileName, int lineNo,
			List<StackTraceElement> stack, List<String> globalVariableNames) {
		counter++;
		
		positionsStack.push( counter );
		functionsStack.push(functionName);
	}

	@Override
	public void functionExit(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues, long threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames) {
		// TODO Auto-generated method stub
		counter++;
		
		String lastOnStack;

		boolean wasEntered = checkIfFunctionWasEntered( functionName );
		
		if ( ! wasEntered ){
			//only current exit should be discarded
			callsToFilterOut.add(counter);
			return;
		}
		
		
		lastOnStack = functionsStack.pop();
		
		while ( ! lastOnStack.equals(functionName)  ){
			Integer callToFilterOut = positionsStack.pop();
			callsToFilterOut.add( callToFilterOut );
			
			if ( functionsStack.size() == 0 ){ 
				//everything popped out, which means that the ENTRY POINT WAS MISSING
				System.err.println("Missing ENTRY-POINT for "+functionName);
				return;
			}
			
			lastOnStack = functionsStack.pop();
		}
		
		positionsStack.pop();
	}

	private boolean checkIfFunctionWasEntered(String functionName) {
		Iterator<String> reverseIterator = functionsStack.descendingIterator();
		
		while ( reverseIterator.hasNext() ){
			String openedFunction = reverseIterator.next();
			if ( openedFunction.equals(functionName) ){
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void traceEnd(String name) {
		//Check if trace ended before all function calls were closed
		//if so add to functions to filter out the ones not closed
		while ( ! functionsStack.isEmpty() ){
			functionsStack.pop();
			Integer callToFilterOut = positionsStack.pop();
			callsToFilterOut.add( callToFilterOut );
		}
		
		if ( callsToFilterOut.size() > 0 ){
			functionsNotClosedInTrace.put(traceCounter, callsToFilterOut);
		}
		
		traceCounter++;
		
		cleanSingleTraceData();
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
		return new UsefulTraceSections[]{ GdbThreadTraceListener.UsefulTraceSections.FUNCTIONS } ;
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

}
