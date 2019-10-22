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
package tools.gdbTraceParser;

import java.io.File;

public class PinTraceUtil {

	public static String geThreadId(File trace) {
		String traceName = trace.getName();
		traceName = traceName.substring(0, traceName.length()-4);
		
		int lastDot = traceName.lastIndexOf('.');
		String threadId = traceName.substring(lastDot+1);
		
		return threadId;
	}
	
	public static boolean isPinTrace(File trace) {
		return trace.getName().endsWith(".pin") || trace.getName().endsWith(".pin.gz");
	}

	public static String getPID(File trace) {
		
		String traceName = trace.getName();
		//bdciTrace.1455541322.28732.0.pin
		
		traceName = traceName.substring(0, traceName.length()-4);
		//bdciTrace.1455541322.28732.0
		
		int lastDot = traceName.lastIndexOf('.');
		traceName = traceName.substring(0,lastDot);
		//bdciTrace.1455541322.28732

		lastDot = traceName.lastIndexOf('.');
		String pid = traceName.substring(lastDot+1);
		traceName = traceName.substring(0,lastDot);
		//bdciTrace.1455541322
		
		lastDot = traceName.lastIndexOf('.');
		String time = traceName.substring(lastDot+1);
		//1455541322
		
		return time+pid;
	}

}
