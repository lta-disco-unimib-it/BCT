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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

import dfmaker.core.Variable;

public class KLFAGdbThreadTraceListener implements GdbThreadTraceListener {

	private FileIndexAppend fileIndex;
	private File recordingDir;
	private HashMap<Long,LinkedList<String>> threadsStacks = new HashMap<Long,LinkedList<String>>();
	
	public KLFAGdbThreadTraceListener(File dataRecordingDir) {
		recordingDir = dataRecordingDir;
		fileIndex = new FileIndexAppend( new File( dataRecordingDir, "traces.idx"), ".klfa.csv" );
	}

	@Override
	public void functionEnter(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		functionName = KlfaCNamesUtil.getCleanFunctionName(functionName);
		function("ENTER", functionName, parameters,
				localVariables,parentLocalVariables, parentArguments,
				threadId);
		getThreadStack(threadId).push(functionName);
	}
	
	
	private LinkedList<String> getThreadStack(long threadId) {
		LinkedList<String> stack = threadsStacks.get(threadId);
		if ( stack == null ){
			stack = new LinkedList<String>();
			stack.add("_");
			threadsStacks.put(threadId, stack);
		}
		return stack;
	}

	public void function(String state, String functionName, List<Variable> parameters, 
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
				long threadId) {	
		try {
			
			String caller = getThreadStack(threadId).peek();
			
			
			
			BufferedWriter w = getWriter( threadId );
			
			w.write(caller);
			
			w.write(',');
			
			w.write(state);
			
			w.write('-');
			
			w.write(functionName);
			
			
			
			for ( Variable par : parameters ){
				w.write(',');
				w.write(par.getValue());
				
			}
			w.newLine();
			w.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private BufferedWriter getWriter(long threadId) throws IOException, FileIndexException {
		String thread = ""+threadId;
		String fileId = fileIndex.add(thread);
		
		File file = new File ( recordingDir, fileId );
		
		BufferedWriter w = new BufferedWriter(new FileWriter(file, true) );
		
		return w;
	}

	@Override
	public void functionExit(String functionName, List<Variable> parameters, 
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		functionName = KlfaCNamesUtil.getCleanFunctionName(functionName);
		getThreadStack(threadId).pop();
		function("EXIT", functionName, parameters, 
				localVariables,parentLocalVariables, parentArguments,
				threadId);
	}

	@Override
	public void traceEnd(String name) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
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
