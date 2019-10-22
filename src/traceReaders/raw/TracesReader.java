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
package traceReaders.raw;

import java.util.Iterator;
import java.util.Set;

import traceReaders.TraceReaderException;
import conf.BctSettingsException;
import conf.InvariantGeneratorSettings;

public interface TracesReader {

	/**
	 * Used to initialize internal InteractionTraceReader parameters
	 * @param trs
	 * @throws BctSettingsException
	 */
	public void init( InvariantGeneratorSettings trs ) throws BctSettingsException;
	
	/**
	 * Returns an Iterator over all IoTraces
	 * @return
	 * @throws TraceException 
	 * @throws TraceException 
	 * @throws FileReaderException 
	 */
	public Iterator<IoTrace> getIoTraces() throws TraceException, TraceReaderException;

	/**
	 * Returns an Iterator over all Interaction Traces
	 * @return
	 */
	public Iterator<InteractionTrace> getInteractionTraces() throws TraceException;

	
	/**
	 * Returns the IO trace of the passed method.
	 * The passed name must be exactly equals to the name of the method recorded in the data recording phase.
	 * 
	 * @param methodName
	 * @return
	 * @throws TraceReaderException 
	 */
	public IoTrace getIoTrace(String methodName) throws TraceReaderException;

	/**
	 * Return all the Ids of the recording sessions available
	 * 
	 * @return
	 * @throws FileReaderException 
	 */
	public Set<String> getRecordingSessionsIds() throws FileReaderException;

	/**
	 * Returns the traces recorded during a certain session
	 * 
	 * @param sessionId
	 * @return
	 */
	public Iterator<InteractionTrace> getInteractionTracesForSession(String sessionId) throws TraceException;

	/**
	 * Returns an iterator over all the interaction traces that have the given thread id associated (we will have at most one trace per recorded session)
	 * 
	 * @param threadId
	 * @return
	 * @throws TraceException 
	 */
	public Iterator<InteractionTrace> getInteractionTracesForThreadId(String threadId) throws TraceException;

	/**
	 * Return the interaction trace of a thread for a given session
	 * 
	 * @param sessionId
	 * @param threadId
	 * @return
	 */
	public InteractionTrace getInteractionTrace(String sessionId, String threadId);

	/**
	 * Returns the interaction trace with the given id
	 * @param traceId
	 * @return
	 * @throws TraceReaderException 
	 */
	public InteractionTrace getInteractionTrace(String traceId) throws TraceReaderException;

	public String getSessionName( String sessionId );

	String getSessionIdFromName(String sessionName) throws FileReaderException;
}
