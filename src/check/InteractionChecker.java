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

import conf.InteractionCheckerSettings;

/**
 * Interface that describe a class that checks if method invocations can be accepted or not
 * @author Fabrizio Pastore
 *
 */
public interface InteractionChecker {

	/**
	 * Checks if the invocation of a certain method is accepted by the model
	 * 
	 * @param threadId
	 * @param signature
	 */
	public void checkEnter( int callId, long threadId, String signature );
	
	/**
	 * Communicate the exit from a method 
	 * 
	 * @param threadId
	 * @param signature
	 */
	public void checkExit( int callId, long threadId, String signature );

	/**
	 * Initialize the checker 
	 * 
	 * @param settings
	 */
	public void init(InteractionCheckerSettings settings);

	public void checkProgramPoint(int callId, int threadId, String programPointId);
	
}
