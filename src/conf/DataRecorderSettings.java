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
package conf;

import java.util.Properties;

public class DataRecorderSettings extends ConfigurationSettings {
	
	///PROPERTIES-DESCRIPTION: Options that control how data is recorded
	
	///diasables recording of IO data
	private static final String BCT_DATA_RECORDING_IO_DISABLE = "bct.dataRecording.io.disable";
	
	private boolean disableIoRecording = false;
	private boolean flattenCalledObject = false;

	private boolean eableIoInteractionMapping = true;
	
	private boolean disableInteractionRecording = false;
	private boolean disableMetaRecording = false;

	private boolean traceExceptions = false;
	private boolean traceExceptionsClassNameOnly = false;
	private boolean traceExceptionsAbsence = false;
	
	
	public boolean getDisableInteractionRecording() {
		return disableInteractionRecording;
	}

	public void setDisableInteractionRecording(boolean disableInteractionRecording) {
		this.disableInteractionRecording = disableInteractionRecording;
	}

	public static class Options {
		
		/**
		 * Indicates whether to flatten the object invoked.
		 * 
		 * It is set to true when recording all metho invocations.
		 * 
		 */
		public static final String FLATTEN_CALLED_OBJECT = "flattenCalledObject";
		public static final String TRACE_EXCEPTIONS = "traceExceptions";
		public static final String TRACE_EXCEPTIONS_ABSENCE = "traceExceptionsAbsence";
		public static final String TRACE_EXCEPTIONS_CLASS_NAME_ONLY = "traceExceptionsClassNameOnly";
		public static final String ENABLE_IO_INTERACTION_MAPPING = "enableIoInteractionMapping";
		public static final String DISABLE_INTERACTION_RERCORDING = "disableInteractionRecording";
		public static final String DISABLE_META_RERCORDING = "disableMetaRecording";
		
	}

	public DataRecorderSettings(Class type, Properties p) {
		super(type, p);
		
		String disableIoString = System.getProperty(BCT_DATA_RECORDING_IO_DISABLE);
//		System.out.println("DISABLE? "+disableIoString);
		if ( disableIoString != null ){
			disableIoRecording = Boolean.parseBoolean(disableIoString);
		}
		
		
		String flattenCalledObjectString = p.getProperty(Options.FLATTEN_CALLED_OBJECT);
//		System.out.println("DISABLE? "+disableIoString);
		if ( flattenCalledObjectString != null ){
			flattenCalledObject = Boolean.parseBoolean(flattenCalledObjectString);
		}
		
		
		String traceExceptionsString = p.getProperty(Options.TRACE_EXCEPTIONS);
//		System.out.println("DISABLE? "+disableIoString);
		if ( traceExceptionsString != null ){
			traceExceptions = Boolean.parseBoolean(traceExceptionsString);
		}
		
		String traceExceptionsAbsenceString = p.getProperty(Options.TRACE_EXCEPTIONS_ABSENCE);
//		System.out.println("DISABLE? "+disableIoString);
		if ( traceExceptionsAbsenceString != null ){
			traceExceptionsAbsence = Boolean.parseBoolean(traceExceptionsAbsenceString);
		}
		
		String traceExceptionsClassNameOnlyString = p.getProperty(Options.TRACE_EXCEPTIONS_CLASS_NAME_ONLY);
//		System.out.println("DISABLE? "+disableIoString);
		if ( traceExceptionsClassNameOnlyString != null ){
			traceExceptionsClassNameOnly = Boolean.parseBoolean(traceExceptionsClassNameOnlyString);
		}
		
		String enableMappingString = p.getProperty(Options.ENABLE_IO_INTERACTION_MAPPING);
		if ( enableMappingString != null ){
			eableIoInteractionMapping = Boolean.parseBoolean(enableMappingString);
		}
		
		String disableInteractionRecordingString = p.getProperty(Options.DISABLE_INTERACTION_RERCORDING);
		if ( disableInteractionRecordingString != null ){
			disableInteractionRecording = Boolean.parseBoolean(disableInteractionRecordingString);
		}
		
		String disableMetaRecordingString = p.getProperty(Options.DISABLE_META_RERCORDING);
		if ( disableMetaRecordingString != null ){
			disableMetaRecording = Boolean.parseBoolean(disableMetaRecordingString);
		}
		
	}

	public boolean getDisableIoRecording() {
		return disableIoRecording;
	}

	public boolean getFlattenCalledObject() {
		return flattenCalledObject;
	}

	public boolean getEnableIoInteractionMapping() {
		return eableIoInteractionMapping;
	}

	public boolean getDisableMetaRecording() {
		return disableMetaRecording;
	}

	public boolean getTraceExceptions() {
		return traceExceptions ;
	}

	public boolean getTraceExceptionsClassNameOnly() {
		return traceExceptionsClassNameOnly;
	}

	public boolean getTraceExceptionsAbsence() {
		return traceExceptionsAbsence;
	}

}
