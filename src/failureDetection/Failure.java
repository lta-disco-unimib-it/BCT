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
package failureDetection;

import modelsViolations.BctRuntimeData;

/**
 * Class containing failure information
 * 
 * FIXME: this class need to be refactore
 * @author Fabrizio Pastore
 *
 */
public class Failure extends BctRuntimeData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String failingActionId;
	private String failingTestId;
	private String failingPID;
	private boolean critical;
	
	private String[] activeActionsIds;
	private String[] activeTestsIds;
	private long detectionTime;
	private String id;
	private String failingComponent;
	private long failingThreadId;
	
	
	public Failure( String id, long detectionTime, String failingComponent, long failingThreadId ){
		super(detectionTime, id, "", null, null, null, String.valueOf(failingThreadId));
		this.id = id;
		this.detectionTime = detectionTime;
		this.failingComponent = failingComponent;
		this.failingThreadId = failingThreadId;
	}

	public String getFailingActionId() {
		return failingActionId;
	}

	public void setFailingActionId(String failingActionId) {
		this.failingActionId = failingActionId;
	}

	public String getFailingTestId() {
		return failingTestId;
	}

	public void setFailingTestId(String failingTestId) {
		this.failingTestId = failingTestId;
	}

	public String getFailingPID() {
		return failingPID;
	}

	public void setFailingPID(String failingPID) {
		this.failingPID = failingPID;
	}

	public String[] getActiveActionsIds() {
		return activeActionsIds;
	}

	public void setActiveActionsIds(String[] activeActionsIds) {
		this.activeActionsIds = activeActionsIds;
	}

	public String[] getActiveTestsIds() {
		return activeTestsIds;
	}

	public void setActiveTestsIds(String[] activeTestsIds) {
		this.activeTestsIds = activeTestsIds;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	public long getDetectionTime() {
		return detectionTime;
	}

	public void setDetectionTime(long detectionTime) {
		this.detectionTime = detectionTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFailingComponent() {
		return failingComponent;
	}

	public void setFailingComponent(String failingComponent) {
		this.failingComponent = failingComponent;
	}

	public long getFailingThreadId() {
		return failingThreadId;
	}

	public void setFailingThreadId(long failingThreadId) {
		this.failingThreadId = failingThreadId;
	}
	
	public boolean equals(Object o){
		if ( o == null ){
			return false;
		}
		
		if ( !(o instanceof Failure) ){
			return false;
		}
		
		Failure rhs = (Failure) o;
		
		if ( !id.equals(rhs.id))
			return false;
		
		if ( failingActionId != null && 
				! failingActionId.equals(rhs.failingActionId )){
			return false;
		}
		
		if ( failingTestId != null && 
				! failingTestId.equals(rhs.failingTestId )){
			return false;
		}
		
		if ( failingPID != null && 
				! failingPID.equals(rhs.failingPID )){
			return false;
		}
		
		
		
		return true;
	}
}
