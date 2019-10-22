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
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import regressionTestManager.MetaDataHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import traceReaders.normalized.NormalizedIoTrace;
import traceReaders.raw.MetaDataIterator;
import dfmaker.core.DaikonTraceProcessor.DTraceListener;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;

public class DNTMTraceListener implements DTraceListener {
	
	private static Logger LOGGER = Logger.getLogger(DNTMTraceListener.class.getCanonicalName()); 
	
	protected static enum PType {ENTRY,EXIT, POINT};
	protected HashMap<String, Variable> currentPoint;
	private PType currentType;
	private String currentPointName;
	private MetaDataIterator metaIterator;
	protected MetaDataHandler metaHandler;
	protected boolean expandReferences;
	protected NormalizedIoTrace normalizedTrace;
	protected Superstructure entrySuperstructure;
	protected Superstructure exitSuperstructure;
	private Set<Long> entryPointsToSkip;
	private boolean skip;
	

	
	
	public DNTMTraceListener( NormalizedIoTrace normalizedTrace, Superstructure entrySuperStructure, Superstructure exitSuperStructure, MetaDataHandler metaHandler, MetaDataIterator metaIterator, boolean expandReferences){
		this.metaIterator = metaIterator;
		this.metaHandler = metaHandler;
		this.expandReferences = expandReferences;
		this.normalizedTrace = normalizedTrace;
		this.entrySuperstructure = entrySuperStructure;
		this.exitSuperstructure = exitSuperStructure;
	}
	
	public void entryPoint(long beginOffset, String line) throws DTraceListenerException {
		newProgramPoint(PType.ENTRY, line);
		checkSkipping(beginOffset);
	}

	public void checkSkipping(long beginOffset) {
		if ( entryPointsToSkip != null ){
			if( entryPointsToSkip.contains(beginOffset) ){
				skip = true;
			}
		}
	}


	public void exitPoint(long beginOffset, String line) throws DTraceListenerException {
		newProgramPoint(PType.EXIT, line);
		checkSkipping(beginOffset);
	}
	
	/**
	 * This method is called when a new program point is encountered, it calls the normalizeAndSaveCurrentPoint method and then reinitialize the fields.
	 * @param type
	 * @param pointName
	 */
	protected void newProgramPoint(PType type,String pointName){
		if ( ! skip ){
			normalizeAndSaveCurrentPoint();
		}
		skip = false;
		currentPointName = pointName;
		currentType = type;
		currentPoint = new HashMap<String,Variable>();
		
	}
	
	/**
	 * Save the current program point in the normalized trace before normalizing it. This method is called before a new program point is encountered or before the end of the trace
	 *
	 */
	protected void normalizeAndSaveCurrentPoint() {

		if ( currentPoint != null ){
			Vector<Variable> normalizedPoint;
			ProgramPointNormalizer ppn = new ProgramPointNormalizer(expandReferences, currentPoint, getCurrentSuperstructure());
			normalizedPoint = ppn.getNormalizedPoint();
			handleMetaData(currentType,normalizedPoint,currentPointName);
			addToTrace(normalizedPoint);
		}
			
	}

	protected void addToTrace(Vector<Variable> normalizedPoint) {
		if ( currentType == PType.ENTRY ){
			normalizedTrace.addEntryPoint( currentPointName, normalizedPoint );
		} else {
			normalizedTrace.addExitPoint( currentPointName, normalizedPoint );
		}
	}

	protected void handleMetaData(PType type, Vector<Variable> normalizedPoint, String pointName) {
		if ( metaHandler != null ){
			String metaData = (String) metaIterator.next();
		
			try {
				if ( type == PType.ENTRY ){
					metaHandler.handleIOEnter( pointName, normalizedPoint, metaData );
				} else {
					metaHandler.handleIOExit( pointName, normalizedPoint, metaData );
				}
			} catch (TcInfoHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	protected void handleMetaData(PType type, Vector<Variable> normalizedPoint, Integer ppHash, String pointName) {
		if ( metaHandler != null ){
			String metaData = (String) metaIterator.next();
			try {
				if ( type == PType.ENTRY ){
					metaHandler.handleIOEnter( pointName, normalizedPoint, metaData, ppHash );
				} else {
					metaHandler.handleIOExit( pointName, normalizedPoint, metaData, ppHash );
				}
			} catch (TcInfoHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void newProgramVar(String varName, String varValue,String varModifier) throws DTraceListenerException {
		try {
		currentPoint.put(varName,new Variable(varName, varValue, Integer.parseInt(varModifier)));
		} catch ( NumberFormatException e ){
			LOGGER.log(Level.SEVERE, "NumberFormatException", e );
			LOGGER.severe("ERROR procesing trace: varName:"+varName+" varValue:"+varValue+" modifier:"+varModifier);
			throw e;
		}
	}

	public void traceEnd() throws DTraceListenerException{
		if ( ! skip ){
			normalizeAndSaveCurrentPoint();
			skip=false;
		}
		try {
			if ( metaHandler != null ){
				metaHandler.save();
			}
		} catch (TcInfoHandlerException e) {
			throw new DTraceListenerException("Problem saving meta info",e);
		}
	}

	public PType getCurrentType() {
		return currentType;
	}

	public String getCurrentPointName() {
		return currentPointName;
	}
	

    protected Superstructure getCurrentSuperstructure() {
    	if ( getCurrentType() == PType.ENTRY ){
    		return entrySuperstructure;
    	} else {
    		return exitSuperstructure;
    	}
	}

	@Override
	public void genericProgramPoint(long beginOffset, String line)
			throws DTraceListenerException {
		newProgramPoint(PType.POINT, line);
	}

	public void setEntryPointsToSkip(Set<Long> entryPointsToSkip) {
		this.entryPointsToSkip = entryPointsToSkip;
	}

	
}
