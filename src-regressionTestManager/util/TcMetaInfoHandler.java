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
package util;

import java.util.Set;

import executionContext.ActionsRegistryException;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;

/**
 * This class retrieve from TCExecutionRegistry information about current test case execution.
 * It is a facade for TCExecutionRegistry to retrieve information about current tests running.
 * 
 * It is directly called by probes/aspects pay attention when refactoring.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class TcMetaInfoHandler {
	private static TestCasesRegistry registry = TestCasesRegistryFactory.getExecutionContextRegistry();
	
	public static String getCurrentTestCase() {
		Set<Integer> actions = null;
		
		try {
			actions = registry.getCurrentActions();
			
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( actions == null || actions.size() == 0 ){
			String currentTC;
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			if ( st.length > 3 ){
				StackTraceElement sel = st[3];
				currentTC = "*"+sel.getClassName()+"."+sel.getMethodName();
			} else {
				currentTC="";
			}
			return currentTC;
		} 
		
		
		
		boolean first = true;
		
		
		
		String currentTC="";
		for ( Integer actionId : actions ){
			try {
				if ( ! first ){
					currentTC+="|";
					currentTC+=registry.getExecutionContextData(actionId).getTestCaseName();
				} else {
					first = false;
					currentTC=registry.getExecutionContextData(actionId).getTestCaseName();
				}
			} catch (ActionsRegistryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return currentTC;
	}
	
//	/**
//	 * Return the name of the currently running test case, if a problem occurrs during the call to the TCExecutionRegistry it returns a blnk line.
//	 * 
//	 * @return the name of the current test case or a blank line
//	 * 
//	 */
//	public static String getCurrentTestCase() {
//		String currentTC;
//
//		try {
//			currentTC = TCExecutionRegistry.getInstance().getCurrentTest();
//		} catch (TCExecutionRegistryException e ) {
//			if ( e.getStackTrace().length > 3 ){
//				StackTraceElement sel = e.getStackTrace()[3];
//				currentTC = "*"+sel.getClassName()+"."+sel.getMethodName();
//			} else {
//				currentTC="";
//				e.printStackTrace();
//			}
//			
//		}
//		return currentTC;
//	}
}
