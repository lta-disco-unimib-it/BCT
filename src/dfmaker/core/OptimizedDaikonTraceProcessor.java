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
package dfmaker.core;

import java.util.Collection;
import java.util.Iterator;

import traceReaders.raw.IoTrace.LineIterator;
import dfmaker.core.ProgramPointHash.Type;

public class OptimizedDaikonTraceProcessor extends DaikonTraceProcessor {

	/**
	 * This interface must be implemented by the components that want to use the processor class
	 * @author fabrizio
	 *
	 */
	public static interface OptimizedDTraceListener extends DTraceListener {
		
		/**
		 * The user communicate that now we are working on an entry point with the given hash, this method return true if it already contains an equal program point and so is required a process of the entry point
		 * (if it is not cached) otherwise returns false
		 *  
		 * @param hashcode
		 * @return
		 * @throws DTraceListenerException
		 */
		public boolean entryPoint( int hashcode ) throws DTraceListenerException;
		
		
		public boolean exitPoint( int hashcode ) throws DTraceListenerException;



		
	}


	private Collection<ProgramPointHash> pphashes;
	private OptimizedDTraceListener optimizedCommandExecuter;
	
	
	public OptimizedDaikonTraceProcessor(OptimizedDTraceListener commandExecuter,Collection<ProgramPointHash> pphashes) {
		super(commandExecuter);
		this.pphashes = pphashes;
		this.optimizedCommandExecuter = commandExecuter;
	}

	/**
	 * Process the trace file using the ProgramPointhash to optmize the process
	 */
	public void process( LineIterator lineIterator ) throws DTraceListenerException{
		Iterator<ProgramPointHash> hashIt = pphashes.iterator();
		//int c=0;
		while ( hashIt.hasNext() ){
			ProgramPointHash ppHash = hashIt.next();
//			System.out.println(c);
//			++c;
			int hash = ppHash.getHash();

			boolean skipProcessing;
			Type type = ppHash.getType();
			
			if ( type == ProgramPointHash.Type.ENTRY ){
				skipProcessing = optimizedCommandExecuter.entryPoint(hash);
			} else {
				skipProcessing = optimizedCommandExecuter.exitPoint(hash);
			}
			
			if ( !skipProcessing ){
				extractProgramPoint(lineIterator);
			} else {
				//skip to the starting line of the program point
				long skipToStart = ppHash.getStartline()-lineIterator.getCurrentLineNumber()-1;
				skip(lineIterator,skipToStart);
				
				//skip the program point
				long skip = ppHash.getLength();
				skip(lineIterator,skip);
			}
			//System.out.println("LINE "+lineIterator.getCurrentLineNumber());
		}
		optimizedCommandExecuter.traceEnd();
	}

	private void skip(LineIterator lineIterator, long skip) {
		for ( int i = 0; i < skip; ++i ){
			lineIterator.next();
		}
	}
}
