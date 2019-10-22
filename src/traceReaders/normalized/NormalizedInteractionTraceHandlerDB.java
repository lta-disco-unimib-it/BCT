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

import java.io.File;
import java.util.List;
import java.util.Vector;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tools.TraceRepository;
import traceReaders.raw.Token;
import util.FileIndex.FileIndexException;
import database.DataLayerException;
import database.InteractionTrace;

public class NormalizedInteractionTraceHandlerDB implements NormalizedInteractionTraceHandler {
	
	
	private File outputfolder;
	private TraceRepository repository;
	
	public NormalizedInteractionTraceHandlerDB(File outputFolder ) {
		this.outputfolder = outputFolder;
		repository = new TraceRepository( this.outputfolder );
	}
	
	/**
	 * Add a method call sequence for the passed method
	 * 
	 * @param methodName	name of the method
	 * @param trace			sequence of methods called
	 * @throws NormalizedTraceHandlerException 
	 */
	public void addInteractionTrace(String methodName, List<Token> trace, String threadId) throws NormalizedTraceHandlerException {
		try{
		if(trace == null) {
			try {
				InteractionTrace.insert(methodName , trace, threadId);
			} catch (DataLayerException e) {
				e.printStackTrace();
			}
			//questo metodo deve essere chiamato nel metodo sotto
			repository.addTrace(methodName, "|");
		} else {
			try {
				InteractionTrace.insert(methodName, trace, threadId);
			} catch (DataLayerException e) {
				e.printStackTrace();
			}
			
			String methodCalls = "";
			for (int currentPosition = 0; currentPosition < trace.size(); currentPosition++) {
				methodCalls = methodCalls.concat(((Token)trace.get(currentPosition)).getTokenValue().substring(0, ((Token)trace.get(currentPosition)).getTokenValue().length()-1) + "#");
			}
			methodCalls = methodCalls.substring(0, methodCalls.length()-1) + "|";
		
			//questo metodo deve essere chiamato nel metodo sotto
			repository.addTrace(methodName, methodCalls);
		}
		} catch ( FileIndexException e ){
			throw new NormalizedTraceHandlerException(e.getMessage());
		}
	}

	/**
	 * Returns an iterator over the normalized traces.
	 * 
	 * @return an iterator over the normalized interaction traces 
	 */
	public NormalizedInteractionTraceIterator getInteractionTracesIterator() {
		//FIXME: why this works on file???
		return new FileInteractionTracesIterator ( repository );
	}

	public void addUsageTrace(String methodName, List<Token> trace, String threadId)
			throws NormalizedTraceHandlerException {
		throw new NotImplementedException();
	}

	public NormalizedInteractionTraceIterator getUsageTracesIterator() {
		throw new NotImplementedException();
	}

	@Override
	public void cleanup() {
		//FIXME: implement
	}

}
