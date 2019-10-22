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
package util.cbe;

import java.io.File;
import java.util.ArrayList;

import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctRuntimeData;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.FormattingException;

import failureDetection.Failure;

import recorders.FileCsvViolationsRecorder;
import recorders.RecorderException;
import recorders.ViolationsRecorderException;
import tools.violationsAnalyzer.BctViolationsLogData;
import tools.violationsAnalyzer.CBEBctViolationsLogLoader;
import tools.violationsAnalyzer.CBEBctViolationsLogLoaderException;

public class CBEToCsvConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CBEToCsvConverter converter = new CBEToCsvConverter();
		
		File input = new File(args[0]);
		File output = new File(args[1]);
		
		if ( output.exists() ){
			System.err.println("File "+output+" exists, delete it.");
			System.exit(-1);
		}
		
		try {
			converter.convert(input,output);
		} catch (FormattingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CBEBctViolationsLogLoaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void convert(File input, File output) throws FormattingException, CBEBctViolationsLogLoaderException {
		
		
		CBEBctViolationsLogLoader lloader = new CBEBctViolationsLogLoader();
		ArrayList<File> inputs = new ArrayList<File>();
		inputs.add(input);
		BctViolationsLogData data = lloader.load(inputs);
		
		FileCsvViolationsRecorder recorder = new FileCsvViolationsRecorder(output);
		
		for ( BctRuntimeData datum : data.getAllRuntimeData() ){
			try {
				record(recorder,datum);
			} catch (ViolationsRecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RecorderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	private void record(FileCsvViolationsRecorder recorder, BctRuntimeData datum) throws RecorderException {
		if ( datum instanceof Failure ){
			recorder.recordFailure((Failure)datum);
		} else if ( datum instanceof BctIOModelViolation ){
			recorder.recordIoViolation((BctIOModelViolation)datum);
		} else if ( datum instanceof BctFSAModelViolation ){
			recorder.recordInteractionViolation((BctFSAModelViolation)datum);
		}
	}

}
