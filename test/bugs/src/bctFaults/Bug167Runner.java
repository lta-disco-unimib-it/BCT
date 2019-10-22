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
package bctFaults;

import recorders.BufferedRecorder;
import recorders.FileOptimizedDataRecorder;
import recorders.OptimizedDataRecorder;
import recorders.RecorderException;
import util.FileUtil;
import conf.EnvironmentalSetter;

public class Bug167Runner {
	
	

	public static void main(String args[]) throws RecorderException{
		
		
		Bug167.dir.mkdirs();
		
		FileUtil.deleteRecursively(Bug167.dir);
		
		EnvironmentalSetter.setBctHome(Bug167.dir.getParentFile().getAbsolutePath());
		
		OptimizedDataRecorder or = new FileOptimizedDataRecorder(Bug167.dir);
		BufferedRecorder r = new BufferedRecorder(or);
		
		
		r.setBufferSize(Bug167.bufSize);
		
		System.out.println("HERE");
		
		int counter = 0;
		
		long threadId = Thread.currentThread().getId();
		
		for( int j = 0; j < Bug167.cycles; j++){
			for( int i = 0; i < Bug167.bufSize; i++){
				counter++;
				System.out.println("Calling recorder");
				r.recordInteractionEnter(""+counter, threadId);
			}
		}
		
		
		for( int j = 0; j < Bug167.additional; j++){
			System.out.println("Caling recorder");
			r.recordInteractionEnter(""+counter, threadId);
		}
		
		
		//Addded because the executor in the bufferedRecorder keeps the VM running
		//we should add a monitoring thread there
		
		System.exit(0);
	}
}