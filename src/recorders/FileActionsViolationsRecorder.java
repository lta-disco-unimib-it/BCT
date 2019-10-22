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
package recorders;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import automata.State;
import executionContext.ActionsRegistry;
import executionContext.ActionsRegistryException;
import executionContext.ActionsRegistryFactory;

/**
 * This recorder keep track of violations in plain text files and for each violation records:
 * 
 * java process ID
 * time of violation
 * current user action (if one)
 * current test case (if one)
 * model violated
 * stack trace
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileActionsViolationsRecorder extends FileViolationsRecorder {

	@Override
	public synchronized void recordInteractionViolation(int callId, String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements) throws RecorderException {
		
		PrintWriter bw;
		try {
			bw = new PrintWriter(new FileOutputStream(getViolationsFile(),true));
			writeAdditionalInfo(bw);
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		super.recordInteractionViolation(callId, invokingMethod, invokedMethod, state, type,
				stElements);
	}

	protected void writeAdditionalInfo(PrintWriter bw) {
		
			
		
		writeProcessInfo(bw);
		writeActionsInfo(bw);
			
			
		
	}

	private void writeProcessInfo(PrintWriter bw) {
		
		bw.write("PROCESS:\t");
		bw.write(ManagementFactory.getRuntimeMXBean().getName());
		bw.write("\n");
		
		bw.write("TIME:\t");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date();
        String datetime = dateFormat.format(date);
		bw.write(datetime);
		bw.write("\n");
	}

	private void writeActionsInfo(PrintWriter bw) {
		ActionsRegistry registry = ActionsRegistryFactory.getExecutionContextRegistry();
		String message;
		try {
			 Set<Integer> actions = registry.getCurrentActions();
			
			StringBuffer sb = new StringBuffer();
			
			boolean first = true; 
			for ( Integer action : actions ){
				if ( ! first ){
					sb.append("|");
				} else {
					first = false;
				}
				sb.append(action);
			}
			message=sb.toString();
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message="null";
		}
		
		bw.write("ACTIONS:\t");
		bw.write(message);
		bw.write("\n");
	}



	@Override
	public void recordIoViolationEnter(int callId, String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables) throws RecorderException {
		
		PrintWriter bw;
		try {
			bw = new PrintWriter(new FileOutputStream(getViolationsFile(),true));
			writeAdditionalInfo(bw);
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		super.recordIoViolationEnter( callId, signature, expression, result, argumentValues,
				returnValue, stElements, localVariables);
	}

	@Override
	public void recordIoViolationExit(int callId, String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables, HashMap origValues)	throws RecorderException {
		
		PrintWriter bw;
		try {
			bw = new PrintWriter(new FileOutputStream(getViolationsFile(),true));
			writeAdditionalInfo(bw);
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		super.recordIoViolationExit(callId, signature, expression, result, argumentValues,
				returnValue, stElements, localVariables, origValues);
	}

}
