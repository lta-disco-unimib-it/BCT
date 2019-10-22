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
package grammarInference.Log;

public class NullLogger implements Logger {

	public void close() {
		
	}

	public int getVerboseLevel() {
		return 0;
	}

	public void logCriticalEvent(String event) {
		
	}

	public void logDebugInfo(String event) {
		
	}

	public void logEvent(String event) {
		
	}

	public void logInfo(String info) {
		
	}

	public void logUnexpectedEvent(String event) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void printDoubleStringArray(String[][] ar) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void setVerboseLevel(int verboseLevel) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

}
