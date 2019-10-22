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

import java.util.HashSet;

import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import dfmaker.core.OptimizedDaikonTraceProcessor.OptimizedDTraceListener;

public class OptimizedSMDTraceListener extends SMDTraceListener implements OptimizedDTraceListener {

	private HashSet<Integer> processedEntryProgramPoints = new HashSet<Integer>();
	private HashSet<Integer> processedExitProgramPoints = new HashSet<Integer>();
	
	private int currentProgramPointHash;
	private boolean pendingPP;
	
	public boolean entryPoint(int hashcode) throws DTraceListenerException {
		checkStateInitial();
		if ( processedEntryProgramPoints.contains(hashcode) ){
			return true;
		}
		pendingPP = true;
		currentProgramPointHash = hashcode;
		return false;
	}

	/**
	 * This method checks that no program point is pending
	 * 
	 * @throws DTraceListenerException
	 */
	private void checkStateInitial() throws DTraceListenerException {
		if ( pendingPP ){
			throw new DTraceListenerException("A program point is pending, expecting a call to entryPoint(String) or exitPoint(String)");
		}
			
	}
	
	private void setInitialState(){
		pendingPP = false;
	}

	public boolean exitPoint(int hashcode) throws DTraceListenerException {
		checkStateInitial();
		if ( processedExitProgramPoints.contains(hashcode) ){
			return true;
		}
		pendingPP = true;
		currentProgramPointHash = hashcode;
		return false;
	}

	@Override
	public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
		super.entryPoint(beginOffset,line);
		processedEntryProgramPoints.add(currentProgramPointHash);
		setInitialState();
	}

	@Override
	public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
		super.exitPoint(beginOffset,line);
		processedExitProgramPoints.add(currentProgramPointHash);
		setInitialState();
	}

}
