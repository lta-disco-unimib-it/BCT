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
package executionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import conf.ExecutionContextRegistrySettings;

public class ExecutionContextStateMaintainerTextFile<T extends ExecutionContextData> extends ExecutionContextStateMaintainerFile<ExecutionContextData> {

	public static class Options {
		public static final String testNameFile = "testNameFile";
	}

	private File testNameFile;
	
	public void init(ExecutionContextRegistrySettings settings) {
		super.init(settings);
		
		String tmpFileString = settings.getProperty(Options.testNameFile);
		testNameFile = new File ( tmpFileString );
	}

	@Override
	public synchronized Set<Integer> getCurrentActions() throws ActionsRegistryException {
		updateCurrentActions();
		
		return super.getCurrentActions();
	}

	public int updateCurrentActions() throws ActionsRegistryException {
		String currentTestId = retrieveCurrentTestFromFile();
		
		int active = isTestAlreadyActive( currentTestId );
		if ( active > -1 ){
			return active;
		}
		
		TestCaseData executionContextData = new TestCaseDataImpl(currentTestId);
		return super.actionStart(executionContextData);
	}

	
	
	private int isTestAlreadyActive(String currentTestId) throws ActionsRegistryException {
		Set<Integer> acs = super.getCurrentActions();
		for( Integer action : acs ){
			TestCaseData executionContextData =  (TestCaseData) super.getExecutionContextData(action);
			if ( currentTestId.equals( executionContextData.getTestCaseName() ) ){
				return action;
			}
		}
		return -1;
	}

	@Override
	public synchronized void actionEnd(Integer actionId)
			throws ActionsRegistryException {
		
	}

	@Override
	public synchronized Integer actionStart(
			ExecutionContextData executionContextData)
			throws ActionsRegistryException {
		return updateCurrentActions();
	}

	private String retrieveCurrentTestFromFile() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(testNameFile));
			try {
				String test = br.readLine();
				return test;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	
}
