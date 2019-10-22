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

import java.util.logging.Level;
import java.util.logging.Logger;

import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import traceReaders.raw.IoTrace.LineIterator;

/**
 * This class is able to parse a daikon trace file and signalate to an external object events like the start of a program point 
 * and the values of the variables inside it  
 *
 * @author Fabrizio Pastore
 *
 */
public class DaikonTraceProcessor {
	private Logger LOGGER = Logger.getLogger(DaikonTraceProcessor.class.getCanonicalName());
	
	private DTraceListener commandExecuter;
	private int offsetCounter;

	/**
	 * This interface must be implemented by the components that want to use the processor class
	 * @author fabrizio
	 *
	 */
	public static interface DTraceListener {

		public void genericProgramPoint( long beginOffset, String line ) throws DTraceListenerException;

		public void entryPoint( long beginOffset, String line ) throws DTraceListenerException;

		public void exitPoint( long beginOffset, String line ) throws DTraceListenerException;

		public void newProgramVar( String varName, String varValue, String varModifier ) throws DTraceListenerException;

		public void traceEnd() throws DTraceListenerException;

	}

	public static class DTraceListenerException extends Exception {

		public DTraceListenerException(String msg) {
			super(msg);
		}

		public DTraceListenerException(String msg,TcInfoHandlerException e) {
			super(msg,e);
		}

	}


	public DaikonTraceProcessor( DTraceListener commandExecuter ){
		this.commandExecuter = commandExecuter;
	}


	public void process( LineIterator lineIterator ) throws DTraceListenerException{
		offsetCounter=-1;
		while ( lineIterator.hasNext() ) {
			extractProgramPoint(lineIterator);
		}
		commandExecuter.traceEnd();
	}




	protected void extractProgramPoint(LineIterator lineIterator) throws DTraceListenerException {
		String varName;
		String varValue;
		String varModifier;

		String line = lineIterator.next();
		

		offsetCounter++;



		//System.out.println("LINE "+line);
		//skip empty lines
		while ( line == null || line.length() == 0 ){
			if ( lineIterator.hasNext() ){
				line = lineIterator.next();
				offsetCounter++;
			} else {
				return;
			}
		}

		int beginOffset = offsetCounter;
		offsetCounter += line.length();
		try {
			if (DaikonReader.isEntryPoint(line)) {
				//System.out.println("ENTRY");
				//traceFile.add(new ProgramPoint(ProgramPoint.ENTRY_POINT, line));

				commandExecuter.entryPoint(beginOffset,line);

				while ((line = lineIterator.next() ) != null && ( !line.isEmpty() ) ) {
					// Variable name
					varName = line;
					// Variable value
					varValue = lineIterator.next();            
					// Variable modifier
					varModifier = lineIterator.next();

					offsetCounter+= varName.length()+varValue.length()+varModifier.length()+3;

					//System.out.println("N "+varName+"V "+varValue+"M "+varModifier);
					// Add variable name to the entry superstructure and let superstructure make type checks               	
					commandExecuter.newProgramVar(varName, varValue, varModifier);

				}

				offsetCounter++;
			} else if (DaikonReader.isExitPoint(line)) {
				//traceFile.add(new ProgramPoint(ProgramPoint.EXIT_POINT, line));
				commandExecuter.exitPoint(beginOffset,line);

				while ((line = lineIterator.next() ) != null && !line.equals(new String())) {
					// Variable name
					varName = line;
					// Variable value
					varValue = lineIterator.next();           
					// Variable modifier
					varModifier = lineIterator.next();
					
					offsetCounter+= varName.length()+varValue.length()+varModifier.length()+3;
					//System.out.println("N "+varName+"V "+varValue+"M "+varModifier);
					// Add variable name to the entry superstructure and let superstructure make type checks               	
					commandExecuter.newProgramVar(varName, varValue, varModifier);
				}

				offsetCounter++;
			}
			else if (DaikonReader.isObjectPoint(line)) {

				throw new DTraceListenerException( "ObjectPoint Not supported" );
			}else if (DaikonReader.isGenericProgramPoint(line)) {
				commandExecuter.genericProgramPoint(beginOffset,line);

				while ((line = lineIterator.next() ) != null && (!line.isEmpty()) ) {
					// Variable name
					varName = line;
					// Variable value
					varValue = lineIterator.next();           
					// Variable modifier
					varModifier = lineIterator.next();

					offsetCounter+= varName.length()+varValue.length()+varModifier.length()+3;
					//System.out.println("N "+varName+"V "+varValue+"M "+varModifier);
					// Add variable name to the entry superstructure and let superstructure make type checks               	
					commandExecuter.newProgramVar(varName, varValue, varModifier);
				}

				offsetCounter++;
			}    
		} catch ( RuntimeException t ) {
			LOGGER.log(Level.SEVERE, "Runtime exception processing line "+lineIterator.getCurrentLineNumber() , t);
			
			throw t;
		}
	}

}
