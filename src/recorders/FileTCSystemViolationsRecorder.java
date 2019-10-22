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
package recorders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import automata.State;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import conf.EnvironmentalSetter;
import conf.ViolationsRecorderSettings;
import failureDetection.Failure;

/**
 * Record violations adding the name of the current system test case.
 * The name of the currenly running test case must be written in the first line 
 * of the file specified in the FileTCSystemViolationsRecorder config file.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileTCSystemViolationsRecorder extends FileTCViolationsRecorder {

	@Override
	protected String getTestName() throws ViolationsRecorderException {
		ViolationsRecorderSettings settings = EnvironmentalSetter.getViolationsRecorderSettings();
		String file = settings.getProperty("currentTestCaseFile");
		
		try {
			BufferedReader br = new BufferedReader( new FileReader(file) );
			String testName = br.readLine();
			br.close();
			return testName;
		} catch (IOException e) {
			throw new ViolationsRecorderException(e.getMessage());
		}
	}

	public void recordInteractionViolation(BctFSAModelViolation modelViolation)
			throws ViolationsRecorderException {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public void recordIoViolation(BctIOModelViolation modelViolation)
			throws ViolationsRecorderException {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public void recordFailure(Failure failure) throws RecorderException {
		// TODO Auto-generated method stub
		
	}


	public void recordAnomalousCallSequence(String correspondingMethod,
			List<String> invocations, StackTraceElement[] stElements, Map<String,Object> localVariables) {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordAnomalousCallSequence(
			BctAnomalousCallSequence anomalousCallSequence)
			throws ViolationsRecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordInteractionViolation(int callId, String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements, String[] anomalousSequence,
			String toState, int anomalousEventPosition) throws RecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordAnomalousCallSequence(String correspondingMethod,
			List<String> invocations, String[] currentActions,
			String[] currentTests, StackTraceElement[] stElements)
			throws ViolationsRecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

}

