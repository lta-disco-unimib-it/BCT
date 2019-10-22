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
package regressionTestManager.tcData.handlers;

import java.util.ArrayList;
import java.util.Set;

import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.VariableInfo;
import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TcInfoEntity;
import regressionTestManager.tcData.TestCaseInfo;

public class TcInfoHandlerStub implements TcInfoHandler {

	public ProgramPointInfo createProgramPointInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public MethodInfo getMethodInfo(String methodName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMethodName(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getMethodsIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getProgramPointIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public ProgramPointInfo getProgramPointInfo(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public TestCaseInfo getTestCaseInfo(String testCaseName) {
		// TODO Auto-generated method stub
		return null;
	}

	public TestCaseInfo getTestCaseInfoFromId(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getTestCasesIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableInfo getVariableFromId(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getVariableIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableInfo getVariableInfo(String programPointName, String name,
			String value) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVariableName(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVariableProgramPoint(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVariableValue(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(RegressionTestManagerHandlerSettings settings) {
		// TODO Auto-generated method stub
		
	}

	public void save() throws TcInfoHandlerException {
		// TODO Auto-generated method stub
		
	}

	public void saveTestCaseInfo(TestCaseInfo info)
			throws TcInfoHandlerException {
		// TODO Auto-generated method stub
		
	}

	public void setModified(TcInfoEntity tcInfoElement) {
		// TODO Auto-generated method stub
		
	}


}
