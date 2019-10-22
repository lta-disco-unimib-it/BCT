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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import automata.State;
import conf.ConfigurationSettings;
import flattener.core.Flattener;
import flattener.core.StimuliRecorder;

/**
 * Records violations on file and add information on the currently running test case
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public abstract class FileTCViolationsRecorder implements ViolationsRecorder {
	private File violationsFile;
	
	public void init(ConfigurationSettings opts) {
		String dataDir = opts.getProperty("violationsFile");
		setViolationsFile(dataDir);
	}

	public void setViolationsFile( String file ) {
		violationsFile = new File(file);
	}
	

	/**
	 * Record an IO violation on file.
	 * 
	 * @param writer
	 * @param signature
	 * @param expression
	 * @param violationType
	 * @param result
	 * @throws RecorderException
	 */
	public synchronized void recordIoViolation( PrintWriter writer, String signature, String expression, IoViolationType violationType, boolean result, Map<String,Object> localVariables ) throws RecorderException {
		String state;
		if ( violationType.equals(IoViolationType.enter) )
			state = "enter";
		else
			state = "exit";
		
		writeTestInfo(writer);
		
		writer.println("Violation of IOinvariant for method " + signature + " during " + state );
		writer.println("\t" + expression + " = " + result);
		
		
	}

	private void writeTestInfo(PrintWriter writer) {
		try {
			writer.write("TEST_INFO\t"+getTestName()+"\n");
		} catch (ViolationsRecorderException e) {
			writer.write("TEST_INFO\tCANNOT_RETRIEVE_TEST_NAME\n");
		}
	}

	protected abstract String getTestName() throws ViolationsRecorderException;

	private void recordStack(PrintWriter bw, StackTraceElement[] stElements) {
		bw.println("Thread : "+Thread.currentThread().getId());
		bw.println("Stack trace : ");
		for ( StackTraceElement el : stElements ){
			bw.println(el.getClassName()+"."+el.getMethodName()+" : "+el.getLineNumber());
		}
		
	}

	/**
	 * Record an interaction violation on user defined file.
	 * This method is synchronized since there can be multiple accesses from different threads.
	 * 
	 */
	public synchronized void recordInteractionViolation(int callId, String invokingMethod, String invokedMethod, State[] state, InteractionViolationType type, StackTraceElement[] stElements) throws RecorderException {
		try {
			PrintWriter bw = new PrintWriter(new FileOutputStream(violationsFile,true));
			writeTestInfo(bw);
			bw.println("Interaction Invariant of the method " + invokingMethod + " has been violated.");
			bw.println("\t Current states are: ");
			
			for (int i = 0; i < state.length; ++i) {
				bw.println("\t" + state[i]);
			}
			
			if ( type.equals(InteractionViolationType.illegalTransition) )
				bw.println("\t Transition " + invokedMethod	+ " is not valid");
			else
				bw.println("\t Execution terminated in non-final states.");
			
			
			recordStack(bw, stElements);
			
			bw.close();
		} catch (FileNotFoundException e) {
			throw new RecorderException(e.getMessage());
		}
		
	}

	private static void recordParameters(PrintWriter pw, Object[] argumentValues ) {
		pw.println("Parameters : ");
		
		
							
    	//of.setDataHandler(new DOMHandler(rootName));
    	// Create the factory
		
		// Get the handler and set the root node name
    	FlattenerAssembler flattenerAssembler = FlattenerAssemblerFactory.INSTANCE; 
		for ( int i = 0; i < argumentValues.length; ++i ){
			
			Flattener of = flattenerAssembler.getFlattener( "parameter["+i+"]" );
			StimuliRecorder stimuliRecorder = flattenerAssembler.getStimuliRecorder();
			
			try {
				of.doSmash(argumentValues[i]);
			
				stimuliRecorder.setWriter(pw);
				
				stimuliRecorder.record(of.getDataHandler().getData());
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
		}
		
	}


	private static void recordReturnvalue(PrintWriter pw, Object returnValue ) {
		pw.println("ReturnValue : ");
		FlattenerAssembler flattenerAssembler = FlattenerAssemblerFactory.INSTANCE;
		
		Flattener of = flattenerAssembler.getFlattener( "returnValue" );
		StimuliRecorder stimuliRecorder = flattenerAssembler.getStimuliRecorder();

		try {
			of.doSmash(returnValue);

			stimuliRecorder.setWriter(pw);

			stimuliRecorder.record(of.getDataHandler().getData());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void recordIoViolationEnter(int callId, String signature, String expression, boolean result, Object[] argumentValues, Object returnValue, StackTraceElement[] stElements, Map<String,Object> localVariables) throws RecorderException {
		PrintWriter bw;
		try {
			bw = new PrintWriter(new FileOutputStream(violationsFile,true));
			recordIoViolation(bw, signature, expression, IoViolationType.enter, result, localVariables );

			//TODO: add paramenters trace
			
			recordParameters(bw, argumentValues);
			
			recordStack(bw,stElements);
			
			
			bw.close();

		} catch (FileNotFoundException e) {
			throw new RecorderException(e.getMessage());
		}
				
	}

	public void recordIoViolationExit(int callId, String signature, String expression, boolean result, Object[] argumentValues, Object returnValue, StackTraceElement[] stElements, Map<String,Object> localVariables, HashMap origValues) throws RecorderException {


		PrintWriter bw;
		try {
			bw = new PrintWriter(new FileOutputStream(violationsFile,true));
			recordIoViolation(bw, signature, expression, IoViolationType.exit, result, localVariables );

			recordParameters(bw, argumentValues);
			
			recordReturnvalue(bw, returnValue);
			
			recordOrigValues ( bw, origValues );
			
			
			recordStack(bw,stElements);
			
			
			bw.close();

		} catch (FileNotFoundException e) {
			throw new RecorderException(e.getMessage());
		}
		
		
	}

	private void recordOrigValues(PrintWriter bw, HashMap<String,Object> origValues) {
		bw.write("\nOrig values:\n");
		Iterator<String> it = origValues.keySet().iterator();
		while( it.hasNext() ){
			String key = it.next();
			bw.write("orig("+key+") = "+origValues.get(key)+"\n");
		}	
		bw.write("\n");
	}

	public void recordInteractionViolation(BctFSAModelViolation modelViolation)
			throws ViolationsRecorderException {
		throw new NotImplementedException();
	}

	public void recordIoViolation(BctIOModelViolation modelViolation)
			throws ViolationsRecorderException {
		throw new NotImplementedException();
	}

}
