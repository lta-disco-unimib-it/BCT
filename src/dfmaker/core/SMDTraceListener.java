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
/**
 * 
 */
package dfmaker.core;

import dfmaker.core.DaikonTraceProcessor.DTraceListener;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;

public class SMDTraceListener implements DTraceListener {
	

	private Superstructure currentStructure;
	private Superstructure entrySuperStructure;
	private Superstructure exitSuperStructure;
	private Superstructure genericProgramPointStructure;


	public Superstructure getGenericProgramPointStructure() {
		return genericProgramPointStructure;
	}

	public void genericProgramPoint(long beginOffset, String line) throws DTraceListenerException {
//		throw new RuntimeException("Not Implemented");
		currentStructure = getGenericProgramPointStructure( line );
	}
	
	public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
		currentStructure = getEntryStructure( line );
	}

	public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
		currentStructure = getExitStructure( line );
	}

	public void newProgramVar(String varName, String varValue, String varModifier) {
		currentStructure.add(varName,varValue);
	}
	
    private Superstructure getEntryStructure(String line) throws DTraceListenerException {
    	//System.out.println("ENTRY "+line);
    	if ( entrySuperStructure == null ){
    		entrySuperStructure = new Superstructure( line );
		} else {
			if (  ! entrySuperStructure.getProgramPointName().equals(line) )
				throw new SMDtraceListenerException("Malformed trace, too much different program point elements in a dtrace file");
		}
		return entrySuperStructure;
		
	}

    private Superstructure getGenericProgramPointStructure(String line) throws DTraceListenerException {
		//System.out.println("EXIT "+line);
		if ( genericProgramPointStructure == null ){
			genericProgramPointStructure = new Superstructure( line );
		} else {
			if (  ! genericProgramPointStructure.getProgramPointName().equals(line) )
				throw new SMDtraceListenerException("Malformed trace, too much different program point elements in a dtrace file");
		}
		return genericProgramPointStructure;
	}

	private Superstructure getExitStructure(String line) throws DTraceListenerException {
		//System.out.println("EXIT "+line);
		if ( exitSuperStructure == null ){
			exitSuperStructure = new Superstructure( line );
		} else {
			if (  ! exitSuperStructure.getProgramPointName().equals(line) )
				throw new SMDtraceListenerException("Malformed trace, too much different program point elements in a dtrace file");
		}
		return exitSuperStructure;
	}

	public Superstructure getEntrySuperStructure() {
		return entrySuperStructure;
	}

	public Superstructure getExitSuperStructure() {
		return exitSuperStructure;
	}

	public void traceEnd() {
		// TODO Auto-generated method stub
		
	}
	
}