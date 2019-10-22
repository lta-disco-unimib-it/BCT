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

import recorders.BufferedRecorder;
import recorders.DataRecorder;
import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import util.RuntimeContextualDataUtil;
import dfmaker.core.Variable;
import failureDetection.Failure;
import flattener.handlers.RawHandler;

public class BctGdbThreadTraceListener implements GdbThreadTraceListener {
	



	private DataRecorder recorder;
	private List<StackTraceElement> lastStack;
	private LinkedList<String> functionsInStack = new LinkedList<String>();
	private boolean simulateFunctionExit;
	private long lastThreadId;
	private List<ProgramPointsCluster> programPointsClusters;
	private boolean traceSessions = true;
	private boolean traceParents;
	

	public void setProgramPointsClusters(
			List<ProgramPointsCluster> programPointsClusters) {
		this.programPointsClusters = programPointsClusters;
	}



	public BctGdbThreadTraceListener(boolean simulateFunctionExit) {
		
		this.simulateFunctionExit = simulateFunctionExit;
		//		recorder = new FileDataRecorder(dataRecordingDir);
	}



	@Override
	public void functionEnter(String functionName, List<Variable> parameters, 
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		
		if ( simulateFunctionExit ) {
			if ( isFuctionReturnMissingBeforeEnter( traceStack ) ){
				List<StackTraceElement> missingExits = StackComparisonUtil.identifyMissingExitsDuringEnter( lastStack, traceStack );
				simulateExits( missingExits, threadId );
			}

			functionsInStack.push(functionName);

		}
		
		functionName = BctCNamesUtil.getCleanFunctionName( functionName );
		
		
		RawHandler hs[] = createParametersHandler( parameters, 
				 localVariables, parentLocalVariables, parentArguments );
		
		try {
			
			lastStack = traceStack;
			
			recorder.recordIoInteractionEnter(functionName, hs, threadId);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void checkInit() {
		if ( recorder != null ){
			return;
		}
		recorder = RecorderFactory.getLoggingRecorder();
	}



	private RawHandler[] createParametersHandler_old(List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments) {
		
		int size = parameters.size() +  localVariables.size() ;
		if ( traceParents ){
			size += parentLocalVariables.size() + parentArguments.size();
		}

		RawHandler hs[] = new RawHandler[ size ];
		
		int i = 0;
		for ( Variable par : parameters ){
//			RawHandler h = new RawHandler("parameter["+i+"]");
			RawHandler h = new RawHandler(par.getName());
			h.addNodeValue("", par.getValue() );
			hs[i]=h;
			i++;
		}
		
		for ( Variable par : localVariables ){
			RawHandler h = new RawHandler(par.getName());
			h.addNodeValue("", par.getValue() );
			hs[i]=h;
			i++;
		}
		
		if ( traceParents ){

			for ( Variable par : parentLocalVariables ){
				RawHandler h = new RawHandler(".."+par.getName());
				h.addNodeValue("", par.getValue() );
				hs[i]=h;
				i++;
			}

			for ( Variable par : parentArguments ){
				RawHandler h = new RawHandler("."+par.getName());
				h.addNodeValue("", par.getValue() );
				hs[i]=h;
				i++;
			}
		}
		
		return hs;
	}
	
	
	/**
	 * Create parameters handlers. This new implementation handle duplicates (happen for example when monitoring "this", which is seen also as a parameter.
	 * 
	 * @param parameters
	 * @param localVariables
	 * @param parentLocalVariables
	 * @param parentArguments
	 * @return
	 */
	private RawHandler[] createParametersHandler(List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables,
			List<Variable> parentArguments) {
		
		HashMap<String,RawHandler> handlersMap = new HashMap<String, RawHandler>();
		
		
		addToParametersHandlers(parameters, handlersMap, "");
		addToParametersHandlers(localVariables, handlersMap, "");
		
		if ( traceParents ){
			addToParametersHandlers(parentArguments, handlersMap, "..");
			addToParametersHandlers(parentLocalVariables, handlersMap, "..");
		}

		RawHandler hs[] = new RawHandler[handlersMap.size()];
		
		int count = 0;
		for ( RawHandler h : handlersMap.values() ){
			hs[count++] = h;
		}
		
		return hs;
	}



	private void addToParametersHandlers(List<Variable> parameters,
			HashMap<String, RawHandler> handlersMap, String namePrefix) {
		if ( parameters == null ) {
			return;
		}
		for ( Variable par : parameters ){
			RawHandler h = new RawHandler(par.getName());
			
			String value = par.getValue();
			if ( value.startsWith("\"") ){
				value = value.replace("\\f", "\\\\f");
			}
			
			h.addNodeValue("", value );
			handlersMap.put(namePrefix+par.getName(), h);
			
			if ( par.hasPointerAddress() ){
				h = new RawHandler("&"+par.getName());
				h.addNodeValue("", par.getPointerAddress() );
				handlersMap.put(namePrefix+"&"+par.getName(), h);
			}
		}
	}



	public boolean isTraceParents() {
		return traceParents;
	}



	public void setTraceParents(boolean traceParents) {
		this.traceParents = traceParents;
	}



	@Override
	public void genericProgramPoint(String functionName,
			List<Variable> parameters, 
			List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			int threadId, String fileName,
			int lineNo, List<StackTraceElement> stack, List<String> globalVariableNames) {

		
		
		if ( programPointsClusters == null ){
			return; 
		}
		
		functionName = BctCNamesUtil.getCleanFunctionName(functionName);
		
		RawHandler hs[] = createParametersHandler(parameters, localVariables, parentLocalVariables, parentArguments);
		
		try {
			
			
			if( programPointsClusters.size() == 0 ){
				recorder.recordGenericProgramPoint(functionName, hs, threadId);
			}
			
			
			for ( ProgramPointsCluster ppc : programPointsClusters  ){
				if ( ppc.accept(functionName) ){
					String programPointId =  ppc.getProgramPointId(functionName);
					recorder.recordGenericProgramPoint(programPointId, hs, threadId);
				}
			}
			
			
			
			
			
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	





	private void simulateExits(List<StackTraceElement> missingExits, long threadId)  {
		LinkedList<StackTraceElement> stackToPop = new LinkedList<StackTraceElement>();

		while ( stackToPop.size() > 0 ){
			ArrayList<StackTraceElement> simulatedStack = new ArrayList<StackTraceElement>();
			simulatedStack.addAll(lastStack);
			simulatedStack.addAll(stackToPop);



			String function = functionsInStack.pop();


			RawHandler hs[] = new RawHandler[ 0 ];
			//TODO: add a fake parameter to indicate that the exit was simulated

			try {	
				System.out.println("Simulating exit "+function);
				recorder.recordIoInteractionExit(function, hs, threadId);
			} catch (RecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stackToPop.pop();
		}

	}








	/**
	 * This function checks if some function call return was not properly recorded
	 * 
	 * @param traceStack
	 * @return 
	 */
	private boolean isFuctionReturnMissingBeforeEnter(List<StackTraceElement> traceStack) {
		if ( lastStack == null ){
			return false;
		}

		//remove the new functin called
		List<StackTraceElement> traceStackCallers = traceStack.subList(0, lastStack.size() - 1 );

		return lastStack.equals( traceStackCallers );
	}


	private boolean isFuctionReturnMissingBeforeExit(List<StackTraceElement> traceStack) {
		return lastStack.equals( traceStack );
	}


	@Override
	public void functionExit(String functionName, List<Variable> parameters, 
			 List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues, long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack, List<String> globalVariableNames) {
		
		
		simulateFunctionExitDuringExit( traceStack, threadId, functionName );
		
		//System.out.println("EXIT "+returnValues.size());

		lastStack = traceStack;
		lastThreadId = threadId;
		
		functionExitNoCheck(functionName, parameters, 
				localVariables, parentLocalVariables, parentArguments,
				returnValues, threadId, fileName, lineNo, traceStack);
	}

	private void simulateFunctionExitDuringExit(
			List<StackTraceElement> traceStack, long threadId, String functionName) {
		// TODO Auto-generated method stub
		
		if ( simulateFunctionExit) {
			if ( isFuctionReturnMissingBeforeExit( traceStack ) ){
				List<StackTraceElement> missingExits = StackComparisonUtil.identifyMissingExitsDuringExit( lastStack, traceStack );
				simulateExits( missingExits, threadId );
			}


			String poppedFunction = functionsInStack.pop();
			if ( ! poppedFunction.equals(functionName) ){
				throw new IllegalStateException("Unexpected closed function: "+functionName+", expecting "+poppedFunction);
			}

		}
		
	}



	private void functionExitNoCheck(String functionName, List<Variable> parameters,
			List<Variable> localVariables, List<Variable> parentLocalVariables, List<Variable> parentArguments,
			List<Variable> returnValues, long threadId, String fileName, int lineNo, List<StackTraceElement> traceStack) {
		
		functionName = BctCNamesUtil.getCleanFunctionName( functionName );


//		RawHandler hs[] = new RawHandler[ parameters.size()];
//		for ( int i = 0; i < hs.length; i++ ){
//			Variable par = parameters.get(i);
//			RawHandler h = new RawHandler("parameter["+i+"]");
//			h.addNodeValue("", par.getValue());
//			hs[i]=h;
//		}
		
		
		
		RawHandler hs[] = createParametersHandler( parameters, 
				 localVariables, parentLocalVariables, parentArguments );
		
		try {

			if (returnValues == null || returnValues.size() == 0 ) {
				recorder.recordIoInteractionExit(functionName, hs, threadId);

			} else {
				//System.out.println("RET "+returnValues);
				RawHandler returnHandler = new RawHandler("returnValue");

				for ( Variable par : returnValues){
					returnHandler.addNodeValue("."+par.getName(), par.getValue());
				}

				recorder.recordIoInteractionExit(functionName, hs, returnHandler, threadId);
			}
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop(){
		
		ViolationsRecorder violationRecorder = RecorderFactory.getViolationsRecorder();
		
		
		
		
		try {
			RuntimeContextualDataUtil.setThreadId(1L);
			Failure failure = new Failure("", System.currentTimeMillis(), null, 1 );
			failure.setPid(RuntimeContextualDataUtil.retrievePID());
			violationRecorder.recordFailure( failure );
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( recorder instanceof BufferedRecorder ){
			((BufferedRecorder)recorder).shutDownRecorder();
		}
	}



	@Override
	public void traceEnd(String name) {
		
		if ( functionsInStack.size() == 0 ){
			return;
		}
		
		simulateFunctionExitDuringExit( new ArrayList<StackTraceElement>(), lastThreadId, functionsInStack.peek() );
	}



	@Override
	public void newSession(String sessionId) {
		checkInit();
		if ( traceSessions ){
			
			recorder.newExecution(sessionId);
		}
	}



	public void setTraceSessions(boolean b) {
		traceSessions = b;
	}



	@Override
	public UsefulTraceSections[] getUsefulTraceSections() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void traceStart(File traceFile) {
		// TODO Auto-generated method stub
		checkInit();
	}



	@Override
	public void exitCode(Integer exitCode) {
		
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
