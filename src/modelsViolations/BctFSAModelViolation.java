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
package modelsViolations;

import java.util.ArrayList;
import java.util.List;

import util.ComparisonUtil;
import util.cbe.AnomalousCallSequencesExporter;

/**
 * This class represent the violation of an Interaction Model represented with a Finite State Automaton
 *  
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BctFSAModelViolation extends BctModelViolation {

	public void setDestinationStateName(String destinationStateName) {
		this.destinationStateName = destinationStateName;
	}

	public void setUnexpectedSequence(String[] unexpectedSequence) {
		this.unexpectedSequence = unexpectedSequence;
	}

	public void setAnomalousEventPosition(int anomalousEventPosition) {
		this.anomalousEventPosition = anomalousEventPosition;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] violationStatesNames;
	private String destinationStateName;
	private BctAnomalousCallSequence anomalousCallSequence = null;
	
	public BctAnomalousCallSequence getAnomalousCallSequence() {
		return anomalousCallSequence;
	}

	public void setAnomalousCallSequence(
			BctAnomalousCallSequence anomalousCallSequence) {
		this.anomalousCallSequence = anomalousCallSequence;
	}

	public String getDestinationStateName() {
		return destinationStateName;
	}

	public String[] getUnexpectedSequence() {
		return unexpectedSequence;
	}

	private String[] unexpectedSequence;
	private int anomalousEventPosition;

	public BctFSAModelViolation(){
		
	}
	
	public BctFSAModelViolation(String id, String violatedModel,
			String violation, String violationType, long creationTime,
			String[] currentActions, 
			String[] currentTests,
			String[] stackTrace, 
			String pid, 
			String threadId, 
			String[] violationStatesNames ) {
		
		this(id, violatedModel, violation, violationType, creationTime, currentActions, currentTests, stackTrace, pid, threadId, violationStatesNames, null, null, -1);
	}
	
	public BctFSAModelViolation(
			String id, 
			String violatedModel,
			String violation, 
			String violationType, 
			long creationTime,
			String[] currentActions, 
			String[] currentTests,
			String[] stackTrace, 
			String pid, 
			String threadId, 
			String[] violationStatesNames,
			String[] unexpectedSequence,
			String destinationStateName,
			int anomalousEventPosition) {
		super(id, 
				violatedModel, 
				violation, 
				violationType, 
				creationTime,
				currentActions, 
				currentTests, 
				stackTrace, 
				pid, 
				threadId);
		this.violationStatesNames = violationStatesNames;
		this.unexpectedSequence = unexpectedSequence;
		this.destinationStateName = destinationStateName;
		this.anomalousEventPosition = anomalousEventPosition;
	}

	@Override
	public ViolatedModelsTypes getViolatedModelType() {
		return BctModelViolation.ViolatedModelsTypes.FSA;
	}


	/**
	 * Return an array with the names of the states in which anomalies occurred
	 * 
	 * @return
	 */
	public String[] getViolationStatesNames() {
		return violationStatesNames;
	}

	public void setViolationStatesNames(String[] violationStatesNames) {
		this.violationStatesNames = violationStatesNames;
	}
	
	public boolean equals(Object o){
		if ( ! ( o instanceof BctFSAModelViolation ) ){
			return false;
		}
		
		BctFSAModelViolation rhs = (BctFSAModelViolation) o; 
		if ( ! super.equals(o)){
			return false;
		}
	
		return ComparisonUtil.equalsArrayElements(violationStatesNames, rhs.violationStatesNames);
	}

	public List<String> getUnexpectedSequenceList() {
		List<String> res = new ArrayList<String>();
		if ( unexpectedSequence == null ){
			return res;
		}
		for ( String s : unexpectedSequence ){
			res.add(s);
		}
		return res;
	}

	@Override
	public String getDescriptiveKey() {
		StringBuffer keyB = new StringBuffer();
		

		keyB.append( "[" );
		for ( String state : getViolationStatesNames() ){
			keyB.append(state);
		}
		keyB.append( "]" );

		keyB.append ( getViolation() );
		
		return keyB.toString();
	}

	public int getAnomalousEventPosition() {
		return anomalousEventPosition;
	}

	
	
}
