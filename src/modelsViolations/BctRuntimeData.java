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

import java.io.Serializable;

import util.ComparisonUtil;

public class BctRuntimeData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String id;
	protected long creationTime;
	protected String pid;
	protected String threadId;
	private String[] stackTrace;
	private String[] currentActions;
	private String[] currentTests;
	
	public BctRuntimeData(){
		
	}


	public BctRuntimeData(long creationTime, String id, String pid,String[] currentActions,
			String[] currentTests,
			String[] stackTrace, String threadId) {
		super();
		this.creationTime = creationTime;
		this.id = id;
		this.pid = pid;
		this.stackTrace = stackTrace;
		this.threadId = threadId;
		this.currentActions =
			 currentActions;
		this.currentTests = currentTests;
	}

	public String[] getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String[] stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public boolean equals( Object o ){

		if ( ! (  o instanceof BctRuntimeData ) ){
			return false;
		}
		
		BctRuntimeData rhs = (BctRuntimeData) o;
		
		if ( ! ComparisonUtil.equalsArray(stackTrace,rhs.stackTrace) ){
			return false;
		}
		
		if ( ! ComparisonUtil.equalsArrayElements(currentActions,rhs.currentActions) ){
			return false;
		}
		
		if ( ! ComparisonUtil.equalsArrayElements(currentTests,rhs.currentTests) ){
			return false;
		}

		return true;
	}
	
	public String[] getCurrentActions() {
		return currentActions;
	}

	public void setCurrentActions(String[] currentActions) {
		this.currentActions = currentActions;
	}

	public String[] getCurrentTests() {
		return currentTests;
	}

	public void setCurrentTests(String[] currentTests) {
		this.currentTests = currentTests;
	}
	
	/**
	 * Return a key that describe this data. It can be overridden to identify data that describe the same situation. 
	 * E.g. a same violation occurring multiple times. 
	 * @return
	 */
	public String getDescriptiveKey(){
		return id;
	}
	
}