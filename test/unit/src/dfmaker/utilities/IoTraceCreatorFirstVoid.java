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


public class IoTraceCreatorFirstVoid extends IoTraceCreatorCyclic {

	private String ppEnter;
	private String ppExit;

	public IoTraceCreatorFirstVoid(String methodName, int iterations) {
		super(methodName,iterations);
		StringBuffer bEnter = new StringBuffer();
		bEnter.append(methodName);
		bEnter.append(":::ENTER\n");
		ppEnter = bEnter.toString();
		
		StringBuffer bExit = new StringBuffer();
		bExit.append(methodName);
		bExit.append(":::EXIT1\n");
		bExit.append("returnValue.booleanValue()\n");
		bExit.append("0\n");
		bExit.append("1\n");
		ppExit = bExit.toString();
	}

	@Override
	public String getPPEnter() {
		return ppEnter;
	}

	@Override
	public String getPPExit() {
		return ppExit;
	}

}
