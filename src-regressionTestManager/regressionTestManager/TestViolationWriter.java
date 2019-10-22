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
package regressionTestManager;

import util.TCExecutionRegistry;
import util.TCExecutionRegistryException;

public class TestViolationWriter {

	private static String getSignature ( String className, String methodName, String methodParameters ){
		return className+"."+methodName+methodParameters;
	}
	
	public static void writeTestStart( String className, String methodName, String methodParameters ){
		try {
			TCExecutionRegistry.getInstance().tcEnter(getSignature(className, methodName, methodParameters));
		} catch (TCExecutionRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeTestEnd( String className, String methodName, String methodParameters ){
		try {
			TCExecutionRegistry.getInstance().tcExit(getSignature(className, methodName, methodParameters));
		} catch (TCExecutionRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
