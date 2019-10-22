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
package tools.violationsAnalyzer.convert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import failureDetection.Failure;

import recorders.FileCBEViolationsRecorder;

public class CBEFailureLogMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<String> failingTests = new ArrayList<String>();
		for ( int i = 0; i < args.length-1; i++ ){
			failingTests.add(args[i]);
		}
		
		File CBEfile = new File(args[args.length-1]);
		if ( CBEfile.exists() ){
			System.err.println("File "+CBEfile.getAbsolutePath()+" exists");
			System.exit(-1);
		}
		
		FileCBEViolationsRecorder recorder = new FileCBEViolationsRecorder();
		for ( String test : failingTests ){
			
		}
	}

}
