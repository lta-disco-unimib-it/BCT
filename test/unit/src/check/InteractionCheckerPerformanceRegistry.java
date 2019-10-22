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
package check;

import java.util.HashMap;

public class InteractionCheckerPerformanceRegistry {

	public static final InteractionCheckerPerformanceRegistry INSTANCE = new InteractionCheckerPerformanceRegistry();
	
	private HashMap<String,Long> executionTime = new HashMap<String, Long>();
	
	public void addExecutionTime( String testId, Long time ){
		System.out.println("ADD TEST ID : "+testId+" "+time);
		executionTime.put(testId,time);
	}
	
	public long getExcutionTime ( String testId ){
		System.out.println("GET TEST ID : "+testId);
		return executionTime.get(testId);
	}
	
}
