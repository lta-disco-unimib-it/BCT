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

import testSupport.IoTraceCreator;

public abstract class IoTraceCreatorCyclic extends IoTraceCreator {


	protected boolean newLineAtEOF = false;
	private int hashEnter;
	private int hashExit;
	private int iterations;

	public IoTraceCreatorCyclic(String methodName, int iterations) {
		super(methodName);
		this.iterations = iterations;
	}

	public abstract String getPPEnter();
	
	public abstract String getPPExit();

	public int getHashEnter() {
		return hashEnter;
	}

	public int getHashExit() {
		return hashExit;
	}
	
	
	public void createTrace(File trace) throws IOException {
		//System.out.println(trace.getAbsolutePath());
		BufferedWriter w = new BufferedWriter(new FileWriter(trace));
		
		String ppEnter = getPPEnter();
		String ppExit = getPPExit();
		
		for ( int i = 0; i < iterations; ++i ){
			w.write("\n");
			w.write(ppEnter);
			w.write("\n");
			w.write(ppExit);
		}
		
		if ( newLineAtEOF ){
			w.write("\n");
		}
		
		w.close();
		
		hashEnter = ppEnter.hashCode();
		hashExit = ppExit.hashCode();
	}
	
}
