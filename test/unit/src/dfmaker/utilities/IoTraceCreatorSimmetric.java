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
package dfmaker.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class IoTraceCreatorSimmetric {
	private String methodName;
	private int simpleTraceItearations = 100000;
	private int hashEnter;
	private int hashExit;
	private LinkedList<File> createdTraces=new LinkedList<File>();
	private boolean newLineAtEOF = false;
	public IoTraceCreatorSimmetric(String methodName,int iterations) {
		this.methodName = methodName;
		this.simpleTraceItearations = iterations;
	}
	
	public void setNewLineAtEOF( boolean value ){
		this.newLineAtEOF = value;
	}
	

	public void createSimpleTrace(File trace) throws IOException {
		System.out.println(trace.getAbsolutePath());
		BufferedWriter w = new BufferedWriter(new FileWriter(trace));
		
		
		StringBuffer bEnter = new StringBuffer();
		bEnter.append(methodName);
		bEnter.append(":::ENTER\n");
		bEnter.append("parameter[0].intValue()\n");
		bEnter.append("6\n");
		bEnter.append("1\n");
		
		StringBuffer bExit = new StringBuffer();
		bExit.append(methodName);
		bExit.append(":::EXIT1\n");
		bExit.append("parameter[0].intValue()\n");
		bExit.append("6\n");
		bExit.append("1\n");
		
		
		
		for ( int i = 0; i < simpleTraceItearations; ++i ){
			w.write("\n");
			w.write(bEnter.toString());
			w.write("\n");
			w.write(bExit.toString());
			
		}
		
		if ( newLineAtEOF )
			w.write("\n");
		
		w.close();
		
		
		hashEnter = bEnter.toString().hashCode();
		hashExit = bExit.toString().hashCode();
	}

	public int getSimpleTraceItearations() {
		return simpleTraceItearations;
	}

	public int getHashEnter() {
		return hashEnter;
	}

	public int getHashExit() {
		return hashExit;
	}

	public String getMethodName() {
		return methodName;
	}


}
