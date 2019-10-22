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
package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MethodCallsRegistry {
	
	/**
	 * Tell the registry that a specific method of an object was entered
	 *
	 */
	public static void methodEntered ( Object calledObject, String methodSignature ){
		recordCallInfo(methodSignature, calledObject, "ENTER");
	}
	
	/**
	 * Tell the registry that a specific method of an object was entered
	 *
	 */
	public static void methodExited ( Object calledObject, String methodSignature ){
		recordCallInfo(methodSignature, calledObject, "EXIT");
	}
	
	private static void recordCallInfo(String methodSignature, Object calledObject, String additional) {
		
		File f = new File("/tmp/calledObjects");
		try {
			FileWriter fw = new FileWriter(f,true);
			fw.write(System.identityHashCode(calledObject)+" "+methodSignature+" "+additional+"\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
