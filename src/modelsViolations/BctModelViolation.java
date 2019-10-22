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



/**
 * @model
 */
public abstract class BctModelViolation extends BctRuntimeData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public static interface ViolationType {
		public static final String NOT_VALID = "NotValid"; //IO model not holds
		public static final String UNEXPECTED_EVENT = "UnexpectedEvent"; //unexpected method called
		public static final String UNEXPECTED_INVOCATION_SEQUENCE = "UnexpectedInternalSequence"; //unexpected sequence of methods
		public static final String UNEXPECTED_TERMINATION_SEQUENCE = "UnexpectedTerminationSequence"; //unexpected sequence of methods called beforeending
		public static final String UNEXPECTED_TERMINATION = "UnexpectedTermination"; //FSA ended in non final state
	};
	
	private String violatedModel;
	private String violation;
	
	protected String violationType; 
	public enum ViolatedModelsTypes{FSA,IO}
	
	public String getViolationType() {
		return violationType;
	}

	public void setViolationType(String violationType) {
		this.violationType = violationType;
	}

	public BctModelViolation(){
		
	}
	
	public BctModelViolation(String id, String violatedModel, String violation, String violationType,
			long creationTime, String[] currentActions,
			String[] currentTests, String[] stackTrace, String pid, String threadId) {
		super(creationTime, id, pid, currentActions, currentTests, stackTrace, threadId);
		this.id = id;
		this.violatedModel = violatedModel;
		this.violation = violation;
		this.creationTime = creationTime;
		
		this.pid = pid;
		this.threadId = threadId;
		this.violationType = violationType;
	}
	
	
	public int getCallId(){
		int pos = id.lastIndexOf('@');
		String lastPart = id.substring(pos+1);
		pos = lastPart.indexOf(':');

		if ( pos <= 0 ){
			return -1;
		}

		try {
			return Integer.valueOf(lastPart.substring(0, pos) );
		} catch ( RuntimeException e ){
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see modelsViolations.IBctModelViolation#getViolatedModel()
	 */
	public String getViolatedModel() {
		return violatedModel;
	}

	public void setViolatedModel(String violatedModel) {
		this.violatedModel = violatedModel;
	}

	/* (non-Javadoc)
	 * @see modelsViolations.IBctModelViolation#getViolation()
	 */
	public String getViolation() {
		return violation;
	}
	
	public void setViolation(String violation) {
		this.violation = violation;
	}

	


	public abstract ViolatedModelsTypes getViolatedModelType();
	
	public boolean equals(Object o){
		
		if ( ! super.equals(o) ){
			return false;
		}
		
		if ( ! (  o instanceof BctModelViolation ) ){
			return false;
		}
		
		BctModelViolation rhs = (BctModelViolation) o;
		
		if ( ! id.equals( rhs.id ) ){
			return false;
		}
		
		if ( ! violation.equals(rhs.violation) ){
			return false;
		}
		
		if ( ! violatedModel.equals(rhs.violatedModel) ){
			return false;
		}
		
		if ( creationTime != rhs.creationTime ){
			return false;
		}
		
		if ( ! pid.equals( rhs.pid ) ){
			return false;
		}
		
		if ( ! threadId.equals( rhs.threadId ) ){
			return false;
		}
		
		if ( ! violationType.equals( rhs.violationType) ){
			return false;
		}

		
		

		
		
		return true;
	}

	

	public boolean isFineGrain() {
		if ( violationType.equals(ViolationType.NOT_VALID ) || violationType.equals(ViolationType.UNEXPECTED_EVENT ) ){
			return false;
		}
		return true;
	}

}
