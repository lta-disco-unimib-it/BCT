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
package testSupport.regressionTestManager;

import java.util.ArrayList;

import regressionTestManager.VariableInfo;

public class ProgramPointInfoStub extends regressionTestManager.tcData.ProgramPointInfo {
	private static String myId = "1";
	private VariableInfo variable1 = new VariableInfo(null, "1");

	public ProgramPointInfoStub() {
		// TODO Auto-generated constructor stub
		super(null, myId, new ArrayList() );
	}

	public VariableInfo getVariable1() {
		return variable1;
	}

}
