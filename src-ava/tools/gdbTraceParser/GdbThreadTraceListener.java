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
import java.util.List;

import dfmaker.core.Variable;

public interface GdbThreadTraceListener {
	
	public enum UsefulTraceSections { ALL, FUNCTIONS }
	
	void newSession(String sessionId);

	void functionEnter(String functionName, List<Variable> parameters, List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments, long threadId, String fileName, int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames );
	
	
	void functionExit(String functionName, List<Variable> parameters, List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments, List<Variable> returnValues, long threadId, String fileName, int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames );

	void traceEnd(String name);

	UsefulTraceSections[] getUsefulTraceSections();

	void genericProgramPoint(String functionName, List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments, int threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames);

	void traceStart(File traceFile);

	void exitCode(Integer exitCode);

	void testCase(String testName);

	void testFail();
}
