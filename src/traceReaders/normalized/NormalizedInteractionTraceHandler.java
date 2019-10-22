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
package traceReaders.normalized;

import java.util.List;

import traceReaders.raw.Token;

/**
 * 
 * Interface for all NormalizedInteractionTrace finders.
 * The aim of this interface is to define  methods useful to retrieve and store ioTraces
 *  
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public interface NormalizedInteractionTraceHandler {
		
	/**
	 * Add a call sequence for the passed monitored element (method, component, programPoint) that indicates how the element interact with other methods
	 * 
	 * @param methodName	name of the method
	 * @param trace			sequence of methods called
	 */
	public abstract void addInteractionTrace(String methodName, List<Token> trace, String threadId) throws NormalizedTraceHandlerException;
	
//	/**
//	 * Add a call sequence for the passed monitored element (method, component, programPoint) that indicates how the element is used
//	 * 
//	 * @param methodName
//	 * @param trace
//	 * @throws NormalizedTraceHandlerException
//	 */
//	public abstract void addUsageTrace(String methodName, List<Token> trace) throws NormalizedTraceHandlerException;
	
//	/**
//	 * Add a call sequence for the passed monitored element (method, component, programPoint) that indicates how the element and how it interacts with other elements
//	 * 
//	 * @param methodName
//	 * @param trace
//	 * @throws NormalizedTraceHandlerException
//	 */
//	public abstract void addUsageInteractionTrace(String methodName, List<Token> trace) throws NormalizedTraceHandlerException;

	/**
	 * Returns an iterator over the normalized traces.
	 * 
	 * @return an iterator over the normalized interaction traces 
	 */
	public abstract NormalizedInteractionTraceIterator getInteractionTracesIterator();

	/**
	 * Delete all the data generated by preprocessing
	 */
	public abstract void cleanup();
	
//	/**
//	 * Returns an iterator over the normalized traces.
//	 * 
//	 * @return
//	 */
//	public abstract NormalizedInteractionTraceIterator getUsageTracesIterator();
	
}
