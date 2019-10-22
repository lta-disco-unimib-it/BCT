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
import java.util.ListIterator;
import java.util.Vector;

import tools.TraceRepository;
import traceReaders.raw.Token;
import util.FileUtil;
import util.FileIndex.FileIndexException;

public class NormalizedInteractionTraceHandlerFile implements
NormalizedInteractionTraceHandler {

	private File interactionOutputFolder;
	public File getInteractionOutputFolder() {
		return interactionOutputFolder;
	}



	private TraceRepository interactionRepository;

//	private File usageOutputFolder;
//	private TraceRepository usageRepository;
//	
//	private File usageInteractionOutputFolder;
//	private TraceRepository usageInteractionRepository;
	/**
	 * Constructor 
	 * 
	 * @param outputFolder	directory that contain (or will contain) all normalized traces
	 */
	public NormalizedInteractionTraceHandlerFile( File interactionOutputFolder ){
		this.interactionOutputFolder = interactionOutputFolder;
		interactionRepository = new TraceRepository( this.interactionOutputFolder );
		

		
	}


	public void addInteractionTrace(String methodName, List<Token> trace, String threadId) throws NormalizedTraceHandlerException {
		addTrace( methodName, trace, interactionRepository);
	}
	
	/**
	 * Generic method to add a trace into a traceRepository
	 * 
	 * @param methodName
	 * @param trace
	 * @param traceRepository
	 * @throws NormalizedTraceHandlerException
	 */
	protected void addTrace(String methodName, List<Token> trace, TraceRepository traceRepository ) throws NormalizedTraceHandlerException{
		try {
			if(trace == null) {
				traceRepository.addTrace(methodName, "|");
			} else {
				StringBuffer methodCallsBuf = new StringBuffer();
				
				ListIterator<Token> it = trace.listIterator();
				
				int currentPosition = -1;
				while ( it.hasNext() ){
					currentPosition++;
					//for (int currentPosition = 0; currentPosition < ; currentPosition++) {
					//System.out.println("TK"+((Token)trace.get(currentPosition)).getTokenValue());
					
					if ( currentPosition > 0 ){
						methodCallsBuf.append("#");
					}
					
					Token token = it.next();
					
					String methodCalled = token.getTokenValue();
					methodCallsBuf.append(methodCalled.substring(0, methodCalled.length()-1));
					
				}
				methodCallsBuf.append("|");
				traceRepository.addTrace(methodName, methodCallsBuf.toString());
			}
		} catch (FileIndexException e) {
			throw new NormalizedTraceHandlerException(e.getMessage());
		}
	}



	public NormalizedInteractionTraceIterator getInteractionTracesIterator() {
		return new FileInteractionTracesIterator(interactionRepository);
	}


	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		FileUtil.deleteDirectoryContents(interactionOutputFolder);
		interactionRepository =  new TraceRepository( this.interactionOutputFolder );
	}





//	public void addUsageTrace(String methodName, List<Token> trace)
//			throws NormalizedTraceHandlerException {
//		addTrace(methodName, trace, usageRepository);
//	}
//	
//	public void addUsageInteractionTrace(String methodName, List<Token> trace)
//	throws NormalizedTraceHandlerException {
//		addTrace(methodName, trace, usageInteractionRepository);
//	}
//	
//
//
//	public NormalizedInteractionTraceIterator getUsageTracesIterator() {
//		return new FileInteractionTracesIterator(usageRepository);
//	}



}
