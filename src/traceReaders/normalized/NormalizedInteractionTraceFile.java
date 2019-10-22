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
package traceReaders.normalized;

import java.io.File;

public class NormalizedInteractionTraceFile implements NormalizedInteractionTrace {
	
	private String 	methodName;
	private File	traceFile;
	
	/**
	 * Constructor
	 * @param methodName	name of the method te trace refers to
	 * @param traceFile		file on wich the normalized trace is saved
	 */
	public NormalizedInteractionTraceFile ( String methodName, File traceFile ){
		this.methodName = methodName;
		this.traceFile = traceFile;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public File getTraceFile(){
		return traceFile;
	}

}