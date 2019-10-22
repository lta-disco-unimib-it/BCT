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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import automata.State;

import conf.ConfigurationSettings;
import failureDetection.Failure;

public class FileCsvViolationsRecorder implements ViolationsRecorder {
	private File violationsFile;
	private char separator = ',';
	private String listSeparator = ";";
	
	public FileCsvViolationsRecorder(){};
	
	public FileCsvViolationsRecorder(File file){
		setViolationsFile(file);
	};
	
	public void init(ConfigurationSettings opts) {
		String dataDir = opts.getProperty("violationsFile");
		setViolationsFile(new File(dataDir));
	}

	public void setViolationsFile( File file ) {
		violationsFile = file;
	}

	public void recordAnomalousCallSequence(
			BctAnomalousCallSequence anomalousCallSequence)
			throws ViolationsRecorderException {
		
	}

	public void recordAnomalousCallSequence(String correspondingMethod,
			List<String> invocations, String[] currentActions,
			String[] currentTests, StackTraceElement[] stElements)
			throws ViolationsRecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}
	
	private void record(String... data) throws RecorderException {
		PrintWriter bw = null;
		try {
			 bw = new PrintWriter(new FileOutputStream(violationsFile,true));
			for ( String datum : data ){
				bw.print(datum);
				bw.print(separator );
			}
			bw.print('\n');
			
		} catch (FileNotFoundException e) {
			throw new RecorderException(e);
		} finally {
			if ( bw != null ){
				bw.close();
			}
		}
	}

	public void recordFailure(Failure failure) throws RecorderException {
		record ( String.valueOf(failure.getCreationTime()), 
				
				"\""+failure.getId()+"\"",
				"Failure",
				"\""+failure.getPid()+"\"",
				convertToString ( failure.getCurrentActions() ),
				convertToString(failure.getCurrentTests())
				
				);
	}

	private String convertToString(String[] list) {
		if ( list == null ){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("\"");
		for ( String element : list ){
			sb.append(element);
			sb.append(listSeparator );
		}
		sb.append("\"");
		return sb.toString();
	}

	public void recordInteractionViolation(int callId,String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements) throws RecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordInteractionViolation(int callId,String invokingMethod,
			String invokedMethod, State[] state, InteractionViolationType type,
			StackTraceElement[] stElements, String[] anomalousSequence,
			String toState, int anomalousEventPosition)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordInteractionViolation(BctFSAModelViolation modelViolation)
			throws ViolationsRecorderException {
		try {
			record ( String.valueOf(modelViolation.getCreationTime()), 
					"\""+modelViolation.getId()+"\"",
					modelViolation.getViolatedModelType().toString(),
					"\""+modelViolation.getPid()+"\"",
					convertToString ( modelViolation.getCurrentActions() ),
					convertToString(modelViolation.getCurrentTests()),
					
					modelViolation.getViolatedModel(),
					modelViolation.getViolation()
					);
		} catch (RecorderException e) {
			throw new ViolationsRecorderException(e);
		}
	}

	public void recordIoViolation(BctIOModelViolation modelViolation)
			throws ViolationsRecorderException {
		try {
			record ( String.valueOf(modelViolation.getCreationTime()), 
					"\""+modelViolation.getId()+"\"",
					modelViolation.getViolatedModelType().toString(),
					"\""+modelViolation.getPid()+"\"",
					convertToString ( modelViolation.getCurrentActions() ),
					convertToString(modelViolation.getCurrentTests()),
					
					modelViolation.getViolatedModel(),
					modelViolation.getViolation()
					);
		} catch (RecorderException e) {
			throw new ViolationsRecorderException(e);
		}
	}

	public void recordIoViolationEnter(int callId,String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> origValues) throws RecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}

	public void recordIoViolationExit(int callId,String signature, String expression,
			boolean result, Object[] argumentValues, Object returnValue,
			StackTraceElement[] stElements, Map<String,Object> localVariables, HashMap origValues)
			throws RecorderException {
		// TODO Auto-generated method stub
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
	}
}
