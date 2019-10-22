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

import modelsViolations.BctRuntimeData;
import modelsViolations.BctModelViolation.ViolatedModelsTypes;

/**
 * This class encapsulate the information about an anomalous sequence of method calls from a component
 * 
 * @author Fabrizio Pastore
 *
 */
public class BctAnomalousCallSequence extends BctRuntimeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String violatedModel;
	private String[] anomalousCallSequence;

	public BctAnomalousCallSequence(long creationTime, String id, String pid,
			String[] stackTrace, String[] currentActions,
			String[] currentTests,String threadId, String anomalousCallSequence[], String violatedModel ) {
		super(creationTime, id, pid, currentActions, currentTests, stackTrace, threadId);
		this.anomalousCallSequence = anomalousCallSequence;
		this.violatedModel = violatedModel;
	}
	
	public String getViolatedModel() {
		return violatedModel;
	}

	public String[] getAnomalousCallSequence() {
		return anomalousCallSequence;
	}
	
	public List<String> getAnomalousCallSequenceList() {
		ArrayList<String> res = new ArrayList<String>();
		for ( String s : anomalousCallSequence ){
			res.add(s);
		}
		return res;
	}	
	
	public boolean equals(Object o ){
		if ( ! ( o instanceof BctAnomalousCallSequence ) ){
			return false;
		}
		
		if ( ! super.equals( o ) ){
			return false;
		}
		
		BctAnomalousCallSequence rhs = (BctAnomalousCallSequence) o;
		
		
		
		if ( violatedModel == null && rhs.violatedModel != null ){
			return false;
		}
		
		if ( ! violatedModel.equals(rhs.violatedModel) ){
			return false;
		}
		
		if ( ( anomalousCallSequence == null ) && ( rhs.anomalousCallSequence != null ) ){
			return false;
		}
		
		if ( ! ComparisonUtil.equalsArray( anomalousCallSequence,rhs.anomalousCallSequence ) ){
			return false;
		}
		
		return true;
	}

	@Override
	public String getDescriptiveKey() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getViolatedModel());
		
		sb.append("_");
		
		sb.append(getAnomalousCallSequenceList().hashCode());
		
		sb.append(anomalousCallSequence.length);
		
		return sb.toString();
	}

	public ViolatedModelsTypes getViolatedModelType() {
		return ViolatedModelsTypes.FSA;
	}

}
