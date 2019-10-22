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

import java.util.HashMap;
import java.util.Vector;

import regressionTestManager.MetaDataHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import traceReaders.normalized.NormalizedIoTrace;
import traceReaders.raw.MetaDataIterator;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import dfmaker.core.OptimizedDaikonTraceProcessor.OptimizedDTraceListener;

public class OptimizedDNTMTraceListener extends DNTMTraceListener implements OptimizedDTraceListener {

	@Override
	public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
		super.entryPoint(beginOffset, line);
		setInitialState();
	}

	private void setInitialState() {
		pendingPP = false;
	}

	@Override
	public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
		super.exitPoint(beginOffset,line);
		setInitialState();
	}

	private HashMap<Integer, String> processedEntryProgramPointsNames = new HashMap<Integer,String>();
	private HashMap<Integer, String> processedExitProgramPointsNames = new HashMap<Integer,String>();
	
	private HashMap<Integer, Vector<Variable>> processedEntryProgramPoints = new HashMap<Integer,Vector<Variable>>();
	private HashMap<Integer, Vector<Variable>> processedExitProgramPoints = new HashMap<Integer,Vector<Variable>>();
	
	private int currentProgramPointHash;
	private boolean pendingPP;
	private boolean shouldSave = false;
	private String ppNameEnter=null;
	private String ppNameExit = null;
	
	public OptimizedDNTMTraceListener(NormalizedIoTrace normalizedTrace, Superstructure entrySuperStructure, Superstructure exitSuperStructure, MetaDataHandler metaHandler, MetaDataIterator metaIterator, boolean expandReferences) {
		super(normalizedTrace, entrySuperStructure, exitSuperStructure, metaHandler, metaIterator, expandReferences);
		
	}

	public boolean entryPoint(int hashcode) throws DTraceListenerException {
		//System.out.println("entryPoint "+hashcode);
		if ( shouldSave ){
			saveCurrentPoint();
		}
		checkStateInitial();
		if ( processedEntryProgramPoints.containsKey(hashcode) ){
			Vector<Variable> normalizedPoint = processedEntryProgramPoints.get(hashcode);
			handleMetaData(PType.ENTRY,normalizedPoint,hashcode,ppNameEnter);
			normalizedTrace.addEntryPoint(processedEntryProgramPointsNames.get(hashcode), normalizedPoint);
			
			return true;
		}
		shouldSave = true;
		pendingPP = true;
		currentProgramPointHash = hashcode;
		
		return false;
	}

	private void checkStateInitial() throws DTraceListenerException {
		if ( pendingPP ){
			throw new DTraceListenerException("A program point is pending, expecting a call to entryPoint(String) or exitPoint(String)");
		}
	}

	public boolean exitPoint(int hashcode) throws DTraceListenerException {
		//System.out.println("exitPoint "+hashcode);
		if ( shouldSave ){
			saveCurrentPoint();
		}
		checkStateInitial();
		if ( processedExitProgramPoints.containsKey(hashcode) ){
			Vector<Variable> normalizedPoint = processedExitProgramPoints.get(hashcode);
			handleMetaData(PType.EXIT,normalizedPoint,hashcode,ppNameExit);
			normalizedTrace.addExitPoint(processedExitProgramPointsNames.get(hashcode), normalizedPoint);
			
			return true;
		}
		pendingPP = true;
		currentProgramPointHash = hashcode;
		shouldSave = true;
		return false;
	}

	
	
	public void traceEnd() throws DTraceListenerException {
		if  ( shouldSave  ){ 
			saveCurrentPoint();
		}
		try {
			if ( metaHandler != null ){
				metaHandler.save();
			}
		} catch (TcInfoHandlerException e) {
			throw new DTraceListenerException("Problem saving meta info",e);
		}
	}
	
	@Override
	protected void normalizeAndSaveCurrentPoint() {
	}
	
	protected void saveCurrentPoint() {
		
		if ( currentPoint == null ){
			return;
		}
		if ( ppNameEnter == null ){
			String completeName = getCurrentPointName();
			String ppNamePure = completeName.substring(0, completeName.indexOf(":::"));
			ppNameEnter = ppNamePure+":::ENTER";
			ppNameExit  = ppNamePure+":::EXIT1";
		}
		//System.out.println("SAVE "+getCurrentType()+" "+currentProgramPointHash);
		shouldSave = false;
		
		Vector<Variable> normalizedPoint;
		ProgramPointNormalizer ppn = new ProgramPointNormalizer(expandReferences, currentPoint, getCurrentSuperstructure() );
		
		normalizedPoint = ppn.getNormalizedPoint();
		handleMetaData(getCurrentType(),normalizedPoint,currentProgramPointHash,getCurrentPointName());
		addToTrace(normalizedPoint);
		
		
		if ( getCurrentType() == PType.ENTRY ){
			processedEntryProgramPoints.put(currentProgramPointHash, normalizedPoint);
			processedEntryProgramPointsNames.put(currentProgramPointHash, getCurrentPointName());
		} else {
			processedExitProgramPoints.put(currentProgramPointHash, normalizedPoint);
			processedExitProgramPointsNames.put(currentProgramPointHash, getCurrentPointName());
		}
		//System.out.println("SAVED "+getCurrentType()+" "+currentProgramPointHash);
	}


	
}
