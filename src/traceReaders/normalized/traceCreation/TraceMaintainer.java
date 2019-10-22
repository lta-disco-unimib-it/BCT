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
package traceReaders.normalized.traceCreation;

import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import util.componentsDeclaration.ComponentsDefinitionException;
import conf.InvariantGeneratorSettings;

/**
 * A trace maintainer has the role of receiving information about method (or more generally interesting program point) begin and end, and create the corresponding normalized traces.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public interface TraceMaintainer {

	/**
	 * Indicates that The passed token represent the begin of a method
	 * @param rawTrace 
	 * @param methodStart
	 */
	public abstract void programPointBegin( InteractionTrace rawTrace, Token programPointToken );
	
	/**
	 * Indicates that The passed token represent the end of a method
	 * @param methodStart
	 */
	public abstract void programPointEnd(InteractionTrace rawTrace, Token programPointToken );
	
	/**
	 * Initialize the trace maintainer with configuration information
	 */
	public abstract void init(
			InvariantGeneratorSettings invariantGeneratorSettings, NormalizedInteractionTraceHandler traceHandler) throws ComponentsDefinitionException;

	/**
	 * Communicate to the trace maintainer that the analysis has finished and can close streams
	 * 
	 */
	public abstract void analysisEnd();
	
	/**
	 * This method is called to indicate the start of the analysis of the traces
	 */
	public abstract void analysisBegin();

	/**
	 * This method is called to indicate that the analysis of the traces recorded in a certain session is going to start
	 */
	public abstract void sessionBegin(String sessionId);
	
	/**
	 * This method is called to indicate that the analysis of the traces recorded in a certain session is finished
	 */
	public abstract void sessionEnd();
	
	/**
	 * This method is called to indicate that the analysis of a certain trace is going to start
	 * @param trace The trace that is going to be processed
	 */
	public abstract void traceBegin(InteractionTrace trace);
	
	/**
	 * This method is called to indicate that the analysis of a certain trace is finished
	 */
	public abstract void traceEnd();

	void programPointGeneric(InteractionTrace trace, Token token);

}