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

public interface TcInfoHandler {
	
	
	void init(RegressionTestManagerHandlerSettings settings);
	
	TestCaseInfo getTestCaseInfo(String testCaseName);

	VariableInfo getVariableInfo(String programPointName, String name, String value);

	void saveTestCaseInfo(TestCaseInfo info) throws TcInfoHandlerException;

	MethodInfo getMethodInfo(String methodName);

	public void setModified( TcInfoEntity tcInfoElement );
	
	public void save() throws TcInfoHandlerException;

	String getMethodName(String id);
	
	public Set<String> getTestCasesIds();
	
	public Set<String> getMethodsIds();
	
	public TestCaseInfo getTestCaseInfoFromId( String id );

	public Set<String> getProgramPointIds();
	
	public Set<String> getVariableIds();

	public VariableInfo getVariableFromId(String string);

	public String getVariableValue(String id);
	
	public ProgramPointInfo createProgramPointInfo();

	public ProgramPointInfo getProgramPointInfo(String string);

	public String getVariableProgramPoint(String id);

	public String getVariableName(String id);
	
}
