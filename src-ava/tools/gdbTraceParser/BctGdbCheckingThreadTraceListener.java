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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import cpp.gdb.TraceUtils;

import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import tools.violationsAnalyzer.ViolationsUtil;
import util.RuntimeContextualDataUtil;
import util.TcMetaInfoHandler;

import check.Checker;
import check.IoChecker;
import check.ViolationsFilter;
import check.ioInvariantParser.IoInvariantParser;

import dfmaker.core.Variable;
import executionContext.ActionsRegistryException;
import executionContext.TestCaseData;
import executionContext.TestCaseDataImpl;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;
import failureDetection.Failure;
import failureDetection.FailureDetectorFactory;
import flattener.flatteners.BreadthObjectFlattener;

public class BctGdbCheckingThreadTraceListener implements
		GdbThreadTraceListener {
	
	
	public static class ReturnObject {
		public Object eax;
	}
	
	{
	IoInvariantParser.setOfflineAnalysis(true);
	BreadthObjectFlattener.skipFakeJavaInspectors = true;//HACK C/C++
	
	}
	
	private HashMap<Long,LinkedList<StackTraceElement>> stacks = new HashMap<Long,LinkedList<StackTraceElement>>();
	private List<ProgramPointsCluster> programPointsClusters;
	private long threads;
	private int callIds = -1;
	private LinkedList<Integer> callIdsStack = new LinkedList<Integer>();
	private boolean excludeGenericProgramPointsFromFSA;
	private ViolationsFilter violationsFilter;
	private FunctionNamesMapper functionNamesMapper = new FunctionNamesMapper();
	private boolean renameFunctions = true; //true by default to handle Template classes
	private Integer currentTestId;

	@Override
	public void functionEnter(String functionName, List<Variable> parameters,
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		
		int callId = incrementCallId();
		
//		if ( localVariables.size() > 0 || parentLocalVariables.size() > 0 || parentArguments.size() > 0  ){
//			//FIXME
//			throw new RuntimeException("checking for local variables not implemented yet");
//		}
		
		Map<String, Object> localVariablesMap = createLocalVariablesMap(
				parameters, localVariables, parentLocalVariables, parentArguments);

		System.out.println("Checking ENTER "+functionName+" "+parameters);
		
		functionName = BctCNamesUtil.getCleanFunctionName( functionName );
		
		functionName = getCorrespondingMangledName( functionName );
		
		Object[] pars = new Object[0]; //getParameters( parameters );
		
		
		
		LinkedList<StackTraceElement> stack = getStack( threadId );
		StackTraceElement calledMethod = new StackTraceElement("", functionName, fileName, lineNo);
		stack.push(calledMethod);
		if ( traceStack == null ){
			traceStack = stack;
		}



		setSimulatedStack( traceStack );
		
		
		Checker.checkInteractionEnter(callId, functionName, threadId);
		Checker.checkIoEnter(callId, functionName, pars, localVariablesMap );
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private String getCorrespondingMangledName(String functionName) {
		if ( ! renameFunctions ){
			return functionName;
		}
		
		if ( TraceUtils.isLineProgramPoint(functionName) ){
			functionName=functionName.trim();
			String functionOnly = TraceUtils.getFunctionName(functionName)+" ";
			functionOnly = functionNamesMapper.getCorrespondingFunctionName( functionOnly ).trim();
			functionName = functionOnly + ":" + TraceUtils.getLine(functionName) + " ";
			return functionName;
		}
		
		return functionNamesMapper.getCorrespondingFunctionName(functionName);
	}



	public boolean isRenameFunctions() {
		return renameFunctions;
	}



	public void setRenameFunctions(boolean renameFunctions) {
		this.renameFunctions = renameFunctions;
	}



	private int incrementCallId() {
		++callIds ;
		callIdsStack.push(callIds);
		return callIds;
	}
	
	private int popCallId() {
		return callIdsStack.pop();
	}



	public static Map<String, Object> createLocalVariablesMap(
			List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments ) {
		Map<String,Object> localVariablesMap = new HashMap<String,Object>();
		addVariablesToMap( localVariablesMap, parameters );
		addVariablesToMap( localVariablesMap, localVariables );
		addVariablesToMap( localVariablesMap, parentArguments );
		addVariablesToMap( localVariablesMap, parentLocalVariables );
		
		return localVariablesMap;
	}
	
	

	private static void addVariablesToMap(Map<String, Object> localVariablesMap,
			List<Variable> localVariables) {
		if ( localVariables == null ){
			return;
		}
		
		for ( Variable localVariable : localVariables){
			localVariablesMap.put( localVariable.getName(), ViolationsUtil.parseValue ( localVariable.getValue() ) );
			
			if ( localVariable.hasPointerAddress() ){
				localVariablesMap.put( "&"+localVariable.getName() , ViolationsUtil.parseValue ( localVariable.getPointerAddress() ) );
			}
		}
	}



	private void setSimulatedStack(List<StackTraceElement> stack) {
		// TODO Auto-generated method stub
		StackTraceElement[] stackArray = stack.toArray(new StackTraceElement[stack.size()]);
		Checker.setSimulatedStackTrace( stackArray );
			
	}



	/**
	 * @deprecated Use {@link ViolationsUtil#parseValue(String)} instead
	 */
	private static Object parseValue(String value) {
		return ViolationsUtil.parseValue(value);
	}

	/**
	 * @deprecated Use {@link ViolationsUtil#unescape(String)} instead
	 */
	public static String unescape( String escapedString ){
		return ViolationsUtil.unescape(escapedString);
	}

	@Override
	public void functionExit(String functionName, List<Variable> parameters, 
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames ) {
		
		int callId = popCallId();
		
		Map<String, Object> localVariablesMap = createLocalVariablesMap(
				parameters, localVariables, parentLocalVariables, parentArguments);
		
		System.out.println("Checking EXIT "+functionName+" "+parameters);
		functionName = BctCNamesUtil.getCleanFunctionName( functionName );
		functionName = getCorrespondingMangledName( functionName );
		
		System.out.println(localVariablesMap);
		
		Object[] pars = new Object[0]; //getParameters( parameters );
		
		LinkedList<StackTraceElement> simulatedStack = getStack( threadId );
		
		if ( traceStack == null ){
			traceStack = simulatedStack;
		}
		
		setSimulatedStack( traceStack );
		
		
		
		
		if ( returnValues == null || returnValues.size() == 0 ){
			Checker.checkIoExit(callId, functionName, pars, localVariablesMap );
		} else {
			Object returnValue = getReturnObject( returnValues );
			Checker.checkIoExit(callId, functionName, pars, returnValue, localVariablesMap );
		}
		
		Checker.checkInteractionExit( callId, functionName, threadId );
		
		simulatedStack.pop();
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}



	private Object getReturnObject(List<Variable> returnValues) {
		
		ReturnObject result = new ReturnObject();
		for ( Variable returnValue : returnValues ){
			if ( "eax".equals( returnValue.getName() ) ){
				result.eax = ViolationsUtil.parseValue(returnValue.getValue());
			}
		}
		
		return result;
	}



	private LinkedList<StackTraceElement> getStack(long threadId) {
		LinkedList<StackTraceElement> stack = stacks.get(threadId);
		if ( stack == null ){
			stack = new LinkedList<StackTraceElement>();
			stacks.put(threadId, stack);
		}
		
		return stack;
	}



	private Object[] getParameters(List<Variable> parameters) {
		Object[] pars = new Object[parameters.size()];
		
		for ( int i = 0; i < parameters.size(); i++ ){
			Variable var = parameters.get(i);
			pars[i]= ViolationsUtil.parseValue ( var.getValue() );
		}
		
		return pars;
	}



	@Override
	public void traceEnd(String name) {
		resetCallIds();
	}



	private void resetCallIds() {
		callIds = 0;
		callIdsStack.clear();
	}



	@Override
	public void newSession(String sessionId) {
		RuntimeContextualDataUtil.setPid(sessionId);
	}



	@Override
	public void genericProgramPoint(String functionName,
			List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			int threadId, String fileName,
			int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		
		if ( programPointsClusters == null ){
			return;
		}
		
		int callId = incrementCallId();
		popCallId();
		
		
		Map<String, Object> localVariablesMap = createLocalVariablesMap(
				parameters, localVariables, parentLocalVariables, parentArguments);

		System.out.println("Checking POINT "+functionName+" "+parameters);
		
		functionName = BctCNamesUtil.getCleanFunctionName( functionName );
		
		functionName = getCorrespondingMangledName(functionName);
		
		
		Object[] pars = new Object[0]; //getParameters( parameters );
		
		LinkedList<StackTraceElement> stack = getStack( threadId );
		if ( traceStack == null ){
			traceStack = stack;
		}
		setSimulatedStack( traceStack );
		
		if ( programPointsClusters.size() == 0 ){
			if ( ! excludeGenericProgramPointsFromFSA ){
				Checker.checkProgramPoint(callId, functionName, threadId);
			}

			Checker.checkIoProgramPoint(callId, functionName, pars, localVariablesMap );
		}
		
		for ( ProgramPointsCluster ppc : programPointsClusters  ){
			if ( ppc.accept(functionName) ){
				String programPointId =  ppc.getProgramPointId(functionName);
				if ( ! excludeGenericProgramPointsFromFSA ){
					Checker.checkProgramPoint(callId, functionName, threadId);
				}
				
				Checker.checkIoProgramPoint(callId, programPointId, pars, localVariablesMap );
			}
		}
	}

	
	public void setProgramPointsClusters(
			List<ProgramPointsCluster> programPointsClusters) {
		this.programPointsClusters = programPointsClusters;
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



	public void setExcludeGenericProgramPointsFromFSAChecking(
			boolean excludeGenericProgramPointsFromFSA) {
		this.excludeGenericProgramPointsFromFSA = excludeGenericProgramPointsFromFSA;
	}



	public void setCheckingFilter(ViolationsFilter bctCheckingFilter) {
		this.violationsFilter = bctCheckingFilter;
		Checker.getIoChecker().setViolationsFilter(bctCheckingFilter);
	}



	@Override
	public void exitCode(Integer exitCode) {
		if ( exitCode == 0 ){
			return;
		}
		Throwable exception = new RuntimeException();
		FailureDetectorFactory.INSTANCE.throwableCaughtInTest(exception , "FakeFailure","");
	}



	@Override
	public void testCase(String testName) {
		TestCaseData executionContextData = new TestCaseDataImpl(testName);
		try {
			
			if ( currentTestId != null ){
				TestCasesRegistryFactory.getExecutionContextRegistry().actionEnd(currentTestId);
			}
			
			currentTestId = TestCasesRegistryFactory.getExecutionContextRegistry().actionStart(executionContextData );
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Override
	public void testFail() {
		FailureDetectorFactory.INSTANCE.throwableCaughtInTest(new RuntimeException("TEST"), "testMethod", ""+currentTestId);	
	}

}
