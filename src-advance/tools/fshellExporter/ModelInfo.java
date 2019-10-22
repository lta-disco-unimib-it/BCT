/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
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
package tools.fshellExporter;

import java.util.Map;

import cpp.gdb.FunctionMonitoringData;

public class ModelInfo {
	protected final String functionName;
	protected final int lineOriginal;
	private final int lineModified;
	
	private boolean entryExitPoint;
	private boolean isEnter;
	private String bctModelName;
	
	private FunctionMonitoringData functionOriginal;
	private FunctionMonitoringData functionModified;



	public FunctionMonitoringData getFunctionOriginal() {
		return functionOriginal;
	}

	public FunctionMonitoringData getFunctionModified() {
		return functionModified;
	}

	public String getFunctionName() {
		return functionName;
	}

	public int getLineOriginal() {
		return lineOriginal;
	}
	
	public int getLineModified() {
		return lineModified;
	}

	public String getFileOriginal() {
		return getFile(functionOriginal);
	}

	public String getFileModified() {
		return getFile(functionModified);
	}

	public String getFile(FunctionMonitoringData data) {
		if ( data != null ){
			return data.getSourceFileLocationClean();
		} 
		return null;

	}

	public boolean isEntryExitPoint() {
		return entryExitPoint;
	}

	public ModelInfo(String bctModelName, String functionName, int lineOriginal, int lineModified, boolean entryExitPoint, boolean isEntry) {
		this.functionName = functionName;
		this.entryExitPoint = entryExitPoint;
		this.isEnter = isEntry;
		this.bctModelName = bctModelName;
		this.lineOriginal = lineOriginal;
		this.lineModified = lineModified;
	}

	public ModelInfo(String bctModelName,  FunctionMonitoringData functionV0, FunctionMonitoringData functionV1, String functionName, int lineOriginal, int lineModified, boolean entryExitPoint, boolean isEntry) {
		this.functionName = functionName;
		this.lineOriginal = lineOriginal;
		this.lineModified = lineModified;
		this.entryExitPoint = entryExitPoint;
		this.isEnter = isEntry;
		this.bctModelName = bctModelName;
		this.functionOriginal = functionV0;
		this.functionModified = functionV1;
	}

	public String getBctModelName() {
		return bctModelName;
	}

	public boolean isLineModel(){
		return ! isEntryExitPoint();
	}

	/**
	 * @deprecated Use {@link #isEnter()} instead
	 */
	public boolean isEntry() {
		return isEnter();
	}

	public boolean isEnter() {
		return isEnter;
	}

	


}