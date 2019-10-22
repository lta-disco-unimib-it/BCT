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
package check;

import java.util.LinkedList;
import java.util.Map;

import conf.EnvironmentalSetter;
import conf.InteractionCheckerSettings;

public class Checker {

	public static InteractionChecker getInteractionchecker() {
		return interactionChecker;
	}

	public static IoChecker getIoChecker() {
		return IoChecker;
	}

	private static final InteractionChecker interactionChecker = createInteractionChecker();
	private static IoChecker IoChecker = new IoChecker();
	public static void setIoChecker(IoChecker ioChecker) {
		IoChecker = ioChecker;
	}

	static StackTraceElement[] simulatedStackTrace;
	
	static {
		EnvironmentalSetter.getBctHome();

	}

	public static synchronized void checkInteractionEnter(String signature, long threadId) {
		interactionChecker.checkEnter(-1, threadId, signature);
	}

	private static InteractionChecker createInteractionChecker() {
		InteractionCheckerSettings settings = EnvironmentalSetter.getInteractionCheckerSettings();
		Class type = settings.getType();
		InteractionChecker _checker = null;
		
		try {
			_checker = (InteractionChecker) type.newInstance();
			_checker.init(settings);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return _checker;
	}

	public static synchronized void checkInteractionExit(String signature, long threadId) {
		interactionChecker.checkExit(-1, threadId, signature);
	}

	
	public static synchronized void checkIoEnter(String methodSignature, Object[] parameters, Map<String,Object> localVariables) {
		IoChecker.checkEnter(-1, methodSignature, parameters, localVariables);
	}

	public static synchronized void checkIoExit(String methodSignature, Object[] parameters, Map<String,Object> localVariables) {
		IoChecker.checkExit(-1, methodSignature, parameters, localVariables);
	}

	public static synchronized void checkIoExit(String methodSignature, Object[] parameters, Object returnValue, Map<String,Object> localVariables) {
		IoChecker.checkExit(-1, methodSignature, parameters, returnValue, localVariables);
	}
	
	
	public static synchronized void checkIoEnter(String methodSignature, Object[] parameters) {
		IoChecker.checkEnter(-1, methodSignature, parameters, null);
	}

	public static synchronized void checkIoExit(String methodSignature, Object[] parameters) {
		IoChecker.checkExit(-1, methodSignature, parameters, null);
	}

	public static synchronized void checkIoExit(String methodSignature, Object[] parameters, Object returnValue) {
		IoChecker.checkExit(-1, methodSignature, parameters, returnValue, null);
	}

	public static void setSimulatedStackTrace(
			StackTraceElement[] simulatedStack) {
		simulatedStackTrace = simulatedStack;
	}

	public static void checkProgramPoint(String programPointId, int threadId) {
		interactionChecker.checkProgramPoint(-1, threadId, programPointId);
	}

	public static void checkIoProgramPoint(String programPointId,
			Object[] pars, Map<String, Object> localVariablesMap) {
		IoChecker.checkProgramPoint(-1, programPointId, pars, localVariablesMap);
		
	}

	public static void checkInteractionEnter(int callId, String signature,
			long threadId) {
		
		interactionChecker.checkEnter(callId, threadId, signature);
	}

	public static void checkIoEnter(int callId, String functionName,
			Object[] pars, Map<String, Object> localVariablesMap) {
		IoChecker.checkEnter(callId, functionName, pars, localVariablesMap);
		
	}

	public static void checkInteractionExit(int callId, String functionName,
			long threadId) {
		interactionChecker.checkExit(callId, threadId, functionName);
		
	}

	public static void checkIoExit(int callId, String functionName,
			Object[] parameters, Map<String, Object> localVariablesMap) {
		IoChecker.checkExit(callId, functionName, parameters, localVariablesMap);
	}

	public static void checkIoExit(int callId, String functionName,
			Object[] pars, Object returnValue,
			Map<String, Object> localVariablesMap) {
		IoChecker.checkExit(callId, functionName, pars, returnValue, localVariablesMap);
	}

	public static void checkIoProgramPoint(int callId, String programPointId,
			Object[] pars, Map<String, Object> localVariablesMap) {
		IoChecker.checkProgramPoint(callId, programPointId, pars, localVariablesMap);
	}
	
	public static void checkProgramPoint(int callId, String programPointId, int threadId) {
		interactionChecker.checkProgramPoint(callId, threadId, programPointId);
	}
	

}
