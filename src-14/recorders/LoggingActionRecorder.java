package recorders;

import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import conf.EnvironmentalSetter;

import flattener.core.Handler;

/**
 * This class is a Facade for the logging functionalities (io,Interactions, mixed).
 * 
 * Retrieve the correct Flattener to flatten parameters and call the user-specified DataRecorder to record data (io, interaction or both).
 * 
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class LoggingActionRecorder {


//	private static final boolean disableIoRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableIoRecording();

//	public static void logIoEnter(  String methodSignature, Object[] parameters ) {        
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordIoEnter(methodSignature,parametersHandlers);
//		} catch ( RecorderException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void logIoExit(  String methodSignature, Object[] parameters ) {        
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoExit(methodSignature,parametersHandlers);
//		} catch ( RecorderException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void logIoExit(  String methodSignature, Object[] parameters, Object returnValue ) {        
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		//flatten return value
//		Handler	  returnValueHandler = flattenParameter("returnValue", returnValue);
//
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//
//		try {
//			recorder.recordIoExit(methodSignature,parametersHandlers, returnValueHandler);
//		} catch ( RecorderException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private	static Handler[]	flattenParameters( Object[] parameters ){
//		
//		if ( disableIoRecording ){
//			return null;
//		}
//		Handler[] parametersHandlers = new Handler[parameters.length];
//
//		for (int i=0; i<parameters.length; i++) {
//			parametersHandlers[i] = flattenParameter("parameter["+i+"]", parameters[i]);
//		}
//
//		return parametersHandlers;
//	}
//
//	private static Handler flattenParameter( String rootName, Object parameter ){
//		//ObjectFlattener of = new ObjectFlattener();					
//
//		// Create the factory
//				
//		Flattener of = FlattenerAssemblerFactory.INSTANCE.getFlattener(rootName);
//
//		//smash the first parameter
//
//		try {
//			of.doSmash(parameter);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return of.getDataHandler();
//
//	}
//
//	public static void logInteractionEnter( String methodSignature, long threadId ){
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordInteractionEnter( methodSignature, threadId );
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public static void logInteractionExit( String methodSignature, long threadId ){
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordInteractionExit( methodSignature, threadId );
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public static void logIoInteractionEnter(String methodSignature, Object[] parameters, long threadId) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		//System.out.println("ENTER "+methodSignature);
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoInteractionEnter(methodSignature, parametersHandlers, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//	}
//
//	public static void logIoInteractionExit(String methodSignature, Object[] parameters, long threadId) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoInteractionExit(methodSignature,parametersHandlers, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//	}
//
//	public static void logIoInteractionExit(String methodSignature, Object[] parameters, Object returnValue, long threadId) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		//flatten return value
//		Handler	  returnValueHandler = flattenParameter("returnValue", returnValue);
//
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//
//		try {
//			recorder.recordIoInteractionExit(methodSignature,parametersHandlers, returnValueHandler, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static void logIoInteractionEnterMeta(Object calledObject, String methodSignature, Object[] parameters, long threadId,String metaInfo) {
		Handler[] parametersHandlers = null;
		
		FileDataRecorder recorder = RecorderFactory.loggingRecorder;
		try {
			recorder.recordIoInteractionEnterMeta(calledObject,methodSignature, parametersHandlers, threadId, metaInfo);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static void logIoInteractionExitMeta(Object calledObject, String methodSignature, Object[] parameters, long threadId, String metaInfo) {
		Handler[] parametersHandlers = null;
		
		FileDataRecorder recorder = RecorderFactory.loggingRecorder;
		try {
			recorder.recordIoInteractionExitMeta(calledObject,methodSignature,parametersHandlers, threadId, metaInfo);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public static void logIoInteractionExitMeta(Object calledObject, String methodSignature, Object[] parameters, Object returnValue, long threadId, String metaInfo) {
		Handler[] parametersHandlers = null;
		
		//flatten return value
		Handler	  returnValueHandler = null;

		FileDataRecorder recorder = RecorderFactory.loggingRecorder;

		try {
			recorder.recordIoInteractionExitMeta(calledObject,methodSignature,parametersHandlers, returnValueHandler, threadId, metaInfo);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public static void logIoEnterMeta(Object calledObject, String methodSignature, Object[] parameters, String metaInfo) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordIoEnterMeta(methodSignature,parametersHandlers,metaInfo);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void logIoExitMeta(Object calledObject, String methodSignature, Object[] parameters, String metaInfo ) {        
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoExitMeta(methodSignature,parametersHandlers,metaInfo);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void logIoExitMeta(Object calledObject, String methodSignature, Object[] parameters, Object returnValue, String metaInfo ) {        
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		//flatten return value
//		Handler	  returnValueHandler = flattenParameter("returnValue", returnValue);
//
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//
//		try {
//			recorder.recordIoExitMeta(methodSignature,parametersHandlers, returnValueHandler, metaInfo);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	
//	public static void logInteractionEnterMeta( String methodSignature, long threadId, String metaInfo ){
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordInteractionEnterMeta( methodSignature, threadId, metaInfo );
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public static void logInteractionExitMeta( String methodSignature, long threadId, String metaInfo ){
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		
//		try {
//			recorder.recordInteractionExitMeta( methodSignature, threadId, metaInfo );
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void logIoInteractionEnter(String methodSignature, Object[] parameters, long threadId, Object calledObject) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		//System.out.println("ENTER "+methodSignature);
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoInteractionEnter(methodSignature, parametersHandlers, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		recordCallInfo(methodSignature,calledObject,"ENTER");
//	}
//
//	public static void logIoInteractionExit(String methodSignature, Object[] parameters, long threadId, Object calledObject) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//		try {
//			recorder.recordIoInteractionExit(methodSignature,parametersHandlers, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		recordCallInfo(methodSignature,calledObject,"EXIT");
//	}
//
//	public static void logIoInteractionExit(String methodSignature, Object[] parameters, Object returnValue, long threadId, Object calledObject) {
//		Handler[] parametersHandlers = flattenParameters(parameters);
//		
//		//flatten return value
//		Handler	  returnValueHandler = flattenParameter("returnValue", returnValue);
//
//		DataRecorder recorder = RecorderFactory.getLoggingRecorder();
//
//		try {
//			recorder.recordIoInteractionExit(methodSignature,parametersHandlers, returnValueHandler, threadId);
//		} catch (RecorderException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		recordCallInfo(methodSignature,calledObject,"EXIT");
//	}

	private static void recordCallInfo(String methodSignature, Object calledObject, String additional) {
		
		File f = new File("/tmp/calledObjects");
		try {
			FileWriter fw = new FileWriter(f,true);
			fw.write(System.identityHashCode(calledObject)+" "+methodSignature+" "+additional+"\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
