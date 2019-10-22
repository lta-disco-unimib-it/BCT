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
package util;

import java.lang.management.ManagementFactory;
import java.util.Set;

import executionContext.ActionsRegistry;
import executionContext.ActionsRegistryException;
import executionContext.ActionsRegistryFactory;
import executionContext.TestCaseData;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;

public class RuntimeContextualDataUtil {

	private static int idsCounter;
	private static Long threadId; //use only when processing recorded traces
	private static String pid;
	
	public static void setPid(String pid) {
		RuntimeContextualDataUtil.pid = pid;
	}

	public static String retrievePID(){
		if ( pid != null ){
			//System.out.println("RuntimeContextualDataUtil PID "+pid);
			return pid;
		}
		//System.out.println("RuntimeContextualDataUtil (new) PID "+pid);
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	public static String retrieveThreadId(){
		if ( threadId != null ){
			return threadId.toString();
		}
		
		return String.valueOf(Thread.currentThread().getId());
	}
	
	public static void setThreadId(Long threadId) {
		RuntimeContextualDataUtil.threadId = threadId;
	}

	public static String[] retrieveStringStackTrace( StackTraceElement[] stElements ){
		

		int startPosition = getStartPosition ( stElements );
		
		String stackTraceElements[] = new String[stElements.length-startPosition];
		
		for ( int i = startPosition; i < stElements.length; ++i ){
			StackTraceElement stElement = stElements[i];
			stackTraceElements[i-startPosition] = retrieveMethod(stElement);
		}
		
		return stackTraceElements;
	}
	
	private static int getStartPosition(StackTraceElement[] stElements) {
		
		for ( int i = 0; i< stElements.length; ++i ){
			String className = stElements[i].getClassName();
			if ( className.startsWith("probes.") || className.startsWith("aspects.")){
				return i+2;
			}
		}
		
		return 0;
	}

	

	public static String retrieveMethod( StackTraceElement stElement ){
		return stElement.getClassName()+"."+stElement.getMethodName()+":"+stElement.getLineNumber();
	}
	
	public static String[] retrieveCurrentActions(){
		ActionsRegistry registry = ActionsRegistryFactory.getExecutionContextRegistry();
		Set<Integer> actions;
		try {
			actions = registry.getCurrentActions();
			String res [] = new String[actions.size()];
			 int i = 0;
			for ( Integer action : actions ){
			res[i++] = action.toString();

			}

			return res;
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static String[] retrieveCurrentTestCases(){
		TestCasesRegistry registry = TestCasesRegistryFactory.getExecutionContextRegistry();
		
		try {
			Set<Integer> actions = registry.getCurrentActions();
			String res[] = new String[actions.size()];
			
			int i = 0;
			for ( Integer action : actions ){
				TestCaseData data = registry.getExecutionContextData(action);
				res[i] =  data.getTestCaseName();
				i++;
			}
			
			return res;
			
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public static synchronized String getNewUniqueId() {
		return String.valueOf(idsCounter++);
	}

	public static String retrieveApplicationName() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return retrieveMethod(st[st.length-1]);
	}

	public static String getEnvironmentalInfoString() {
		return System.getProperty("os.name")+"_"+System.getProperty("os.arch")+"#"+System.getProperty("os.version");
	}
	
}
