package probes;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import conf.EnvironmentalSetter;

import recorders.LoggingActionRecorder;
import traceReaders.metadata.ExecutionTokenMetaData;
import util.ClassFormatter;



public class LoggerProbe {
	
	/**
	 * Record the information about a method enter
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 */
//	public static void enter(String /*className*/ cName,
//			String /*methodName*/ theMethodName,
//			String /*methodSig*/ methodS,
//			Object[] /*args*/ argsPassed ){
//
//		
//	}
	
	private static final int CALLER_POS = 2;

	/**
	 * Record the information about a method exit
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 * @param ret
	 */
//	public static void exit(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret){
////		You need the following lines only if you use Lorenzoli ObjectFlattener
////
////		if ( AspectFlowChecker.isInsideAnAspect() )
////			return;
////		AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
////
//		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
//
//		//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
//		
//
//
////      You need the following lines only if you use Lorenzoli ObjectFlattener
////		
////		//System.out.println("IOE "+signature);
////		AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );
//
//	}

	public static void exitMeta(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret, String currentTestCase, Object calledObject) {
//		You need the following lines only if you use Lorenzoli ObjectFlattener
		//
//				if ( AspectFlowChecker.isInsideAnAspect() )
//					return;
//				AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
		//
				String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);

				
				
				//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
				
				ExecutionTokenMetaData md = new ExecutionTokenMetaData();
				if ( calledObject != null ){
					md.setCalledObjectId(""+System.identityHashCode(calledObject));
				}

				md.setTimestamp(System.currentTimeMillis());

				try {
					throw new Exception("ENTER "+signature);
				} catch ( Exception e ) {
					//e.printStackTrace();
					
					StackTraceElement[] st = e.getStackTrace();
					if ( st.length > CALLER_POS ){
						StackTraceElement caller;

						if ( st[CALLER_POS-1].getMethodName().endsWith("Call") ){ 
							caller = st[CALLER_POS];
						} else {
							caller = st[CALLER_POS+1];
						}


						String callerString = caller.getClassName()+":"+caller.getLineNumber();

						//					contextData.add(callerString);
						//					md.setContextData(contextData);


						String callerString0="";
						String callerString2="";
						String callerString3="";

						if ( st.length > 2 ){
							caller = st[2];
							callerString0 = caller.getClassName()+":"+caller.getLineNumber();
						}

						if ( st.length > 3 ){
							caller = st[3];
							callerString2 = caller.getClassName()+":"+caller.getLineNumber();
						}

						if ( st.length > 4 ){
							caller = st[4];
							callerString3 = caller.getClassName()+":"+caller.getLineNumber();
						}

						ArrayList contextData = new ArrayList();
						//					contextData.add(callerString);
						contextData.add(callerString+", "+callerString0+",,,"+callerString2+",,,"+callerString3);
						md.setContextData(contextData);

						System.out.println("recording EXIT "+signature+" "+callerString);
					}

					if ( currentTestCase != null ){
						ArrayList tests = new ArrayList(1);
						tests.add(currentTestCase);
						md.setCurrentTests(tests);
					}

					String metaData = md.storeToString();

					if ( methodS.endsWith("V") ){						//void method
						LoggingActionRecorder.logIoInteractionExitMeta(calledObject, signature, argsPassed,System.identityHashCode(Thread.currentThread()),metaData);
					} else {											//non void method
						LoggingActionRecorder.logIoInteractionExitMeta(calledObject, signature, argsPassed, ret,System.identityHashCode(Thread.currentThread()),metaData);
					}
				}
				//		      You need the following lines only if you use Lorenzoli ObjectFlattener
				//				
				//				//System.out.println("IOE "+signature);
				//				AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );
	}

	public static void enterMeta(String className, String methodName, String methodSig, Object[] args, String currentTestCase,  Object calledObject) {
		
//		throw new RuntimeException(EnvironmentalSetter.getBctHome());
		
//		try {
//			BufferedWriter w = new BufferedWriter(new FileWriter(new File("/tmp/bct.log"),true));
//			w.write(className+"."+methodName+"\n");
//			w.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String signature = ClassFormatter.getSignature(className,methodName,methodSig);
		
		ExecutionTokenMetaData md = new ExecutionTokenMetaData();
		if ( calledObject != null ){
			md.setCalledObjectId(""+System.identityHashCode(calledObject));
		}
		
		md.setTimestamp(System.currentTimeMillis());
		
		try {
			throw new Exception();
			} catch ( Exception e ) {
				//e.printStackTrace();
		StackTraceElement[] st = e.getStackTrace();
		if ( st.length > CALLER_POS ){
			StackTraceElement caller;
			
			if ( st[CALLER_POS-1].getMethodName().endsWith("Call") ){ 
				caller = st[CALLER_POS];
			} else {
				caller = st[CALLER_POS+1];
			}
			
			String callerString = caller.getClassName()+":"+caller.getLineNumber();
			
			String callerString0="";
			String callerString2="";
			String callerString3="";
			
			if ( st.length > 2 ){
				caller = st[2];
				callerString0 = caller.getClassName()+":"+caller.getLineNumber();
			}

			if ( st.length > 3 ){
				caller = st[3];
				callerString2 = caller.getClassName()+":"+caller.getLineNumber();
			}

			if ( st.length > 4 ){
				caller = st[4];
				callerString3 = caller.getClassName()+":"+caller.getLineNumber();
			}
			
			ArrayList contextData = new ArrayList();
//			contextData.add(callerString);
			contextData.add(callerString+", "+callerString0+",,,"+callerString2+",,,"+callerString3);
			md.setContextData(contextData);
			
			
			System.out.println("recording ENTER "+signature+" "+callerString);
		}
			}
		if ( currentTestCase != null ){
			ArrayList tests = new ArrayList(1);
			tests.add(currentTestCase);
			md.setCurrentTests(tests);
		}
			
		String metaData = md.storeToString();
		
		LoggingActionRecorder.logIoInteractionEnterMeta(calledObject,signature, args, System.identityHashCode(Thread.currentThread()),metaData);
	}

	/**
	 * Record the information about a method enter, and records also information about the called object
	 * 
	 * For the called object it records just the hashcode on a file
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 */
//	public static void enter(String /*className*/ cName,
//			String /*methodName*/ theMethodName,
//			String /*methodSig*/ methodS,
//			Object[] /*args*/ argsPassed,
//			Object calledObject){
//		
//		
//	}
	
	/**
	 * Record the information about a method exit, and records also information about the called object
	 * 
	 * For the called object it records just the hashcode on a file
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 * @param ret
	 */
//	public static void exit(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret, Object calledObject){
////		You need the following lines only if you use Lorenzoli ObjectFlattener
////
////		if ( AspectFlowChecker.isInsideAnAspect() )
////			return;
////		AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
////
//		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
//
//		//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
//		
//		if ( methodS.endsWith("V") ){						//void method
//			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed,Thread.currentThread().getId(), calledObject);
//		} else {											//non void method
//			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed, ret,Thread.currentThread().getId(), calledObject);
//		}
//
////      You need the following lines only if you use Lorenzoli ObjectFlattener
////		
////		//System.out.println("IOE "+signature);
////		AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );
//
//	}
}
