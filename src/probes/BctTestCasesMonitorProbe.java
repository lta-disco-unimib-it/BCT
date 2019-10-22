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
package probes;

import java.util.HashMap;

import executionContext.ActionsRegistryException;
import executionContext.TestCaseDataImpl;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;
import failureDetection.FailureDetectorFactory;




/**
 * This class is used by the BctActionsMonitoringProbe, it monitors the starting and ending of actions
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BctTestCasesMonitorProbe {
	private static HashMap<String,Integer> monitoredActions = new HashMap<String, Integer>();
	private static TestCasesRegistry registry = TestCasesRegistryFactory.getExecutionContextRegistry();
	
	public static void enter( String className, String  methodName, String methodSignature ){

		try {
		
			//we maintain an association between the actions started observed in a probe and the action id returned
			String startingLocation = getStartingLocation(className,methodName,methodSignature);
			//System.err.println("BCT -- Entering test "+startingLocation);
			Integer id = registry.actionStart(new TestCaseDataImpl(startingLocation));
			
			//System.out.println(startingLocation);
			monitoredActions.put(startingLocation, id);
			
		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

	}

	private static String getStartingLocation(String className,
			String methodName, String methodSignature) {

		return ClassFormatter.getSignature(className,methodName,methodSignature)+Thread.currentThread().getId();
	}

	public static void exit( String className, String  methodName, String methodSignature, Throwable exception ){

		try{		
			String startingLocation = getStartingLocation(className,methodName,methodSignature);
			//System.err.println("BCT -- Exiting test "+startingLocation);
			Integer id = monitoredActions.remove(startingLocation);
			
			
			if ( exception != null ){
				
				String stringId = null;
				if ( id == null ){
					stringId = null;
					//System.err.println("BCT -- Current test does not have an associated id: "+startingLocation);
				} else {
					stringId = id.toString();
				}
				
				FailureDetectorFactory.INSTANCE.throwableCaughtInTest(exception, startingLocation, stringId);
			}
			
			registry.actionEnd(id);

		} catch (ActionsRegistryException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

	}

}
