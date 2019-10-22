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
package probes;

import java.util.ArrayList;

import recorders.LoggingActionRecorder;
import traceReaders.metaData.ExecutionTokenMetaData;

public class LoggerProbe {
	
	/**
	 * Record the information about a method enter
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 */
	public static void enter(String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed ){

		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
		
		LoggingActionRecorder.logIoInteractionEnter(signature, argsPassed, Thread.currentThread().getId());
		
	}
	
	/**
	 * Record the information about a method exit
	 * 
	 * @param cName
	 * @param theMethodName
	 * @param methodS
	 * @param argsPassed
	 * @param ret
	 */
	public static void exit(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret){
//		You need the following lines only if you use Lorenzoli ObjectFlattener
//
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
//
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);

		//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
		
		if ( methodS.endsWith("V") ){						//void method
			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed,Thread.currentThread().getId());
		} else {											//non void method
			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed, ret,Thread.currentThread().getId());
		}

//      You need the following lines only if you use Lorenzoli ObjectFlattener
//		
//		//System.out.println("IOE "+signature);
//		AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );

	}

	public static void exitMeta(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret, String currentTestCase, Object calledObject) {
//		You need the following lines only if you use Lorenzoli ObjectFlattener
		//
//				if ( AspectFlowChecker.isInsideAnAspect() )
//					return;
//				AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
		//
				String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
				exitMeta(signature, argsPassed, ret, currentTestCase, calledObject);
	}
	
	public static void exitMeta(String signature, Object[] argsPassed, Object ret, String currentTestCase, Object calledObject) {
				
				
				//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
				
				ExecutionTokenMetaData md = new ExecutionTokenMetaData();
				if ( calledObject != null ){
					md.setCalledObjectId(""+System.identityHashCode(calledObject));
					md.setCalledObjectClass(calledObject.getClass().getCanonicalName());
				}
				
				md.setTimestamp(System.currentTimeMillis());
				
				StackTraceElement[] st = Thread.currentThread().getStackTrace();
				if ( st.length > 4 ){
					StackTraceElement caller;
					
//					if ( st[2].getMethodName().endsWith("Call") ){ 
//						caller = st[3];
//					} else {
//						caller = st[4];
//					}
					
					caller = st[3];
					
					String callerString = caller.getClassName()+":"+caller.getLineNumber();
					ArrayList<String> contextData = new ArrayList<String>();
					contextData.add(callerString);
					md.setContextData(contextData);
				}
				
				if ( currentTestCase != null ){
					ArrayList<String> tests = new ArrayList<String>(1);
					tests.add(currentTestCase);
					md.setCurrentTests(tests);
				}
				
				String metaData = md.storeToString();
				
				
				
				if ( signature.endsWith("V") ){						//void method
					LoggingActionRecorder.logIoInteractionExitMeta(calledObject, signature, argsPassed,Thread.currentThread().getId(),metaData);
				} else {											//non void method
					LoggingActionRecorder.logIoInteractionExitMeta(calledObject, signature, argsPassed, ret,Thread.currentThread().getId(),metaData);
				}

//		      You need the following lines only if you use Lorenzoli ObjectFlattener
//				
//				//System.out.println("IOE "+signature);
//				AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );
	}

	public static void enterMeta(String className, String methodName, String methodSig, Object[] args, String currentTestCase,  Object calledObject) {
		
		String signature = ClassFormatter.getSignature(className,methodName,methodSig);
		enterMeta(signature, args, currentTestCase, calledObject);
	}
	
	public static void enterMeta(String signature, Object[] args, String currentTestCase,  Object calledObject) {
		
		//System.out.println("ENTER "+signature); try { throw new Exception(); } catch ( Exception e ) { /*e.printStackTrace();*/ }; 
		ExecutionTokenMetaData md = new ExecutionTokenMetaData();
		if ( calledObject != null ){
			md.setCalledObjectId(""+System.identityHashCode(calledObject));
			md.setCalledObjectClass(calledObject.getClass().getCanonicalName());
		}
		
		md.setTimestamp(System.currentTimeMillis());
		
		
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		if ( st.length > 4 ){
			StackTraceElement caller;
			
//			if ( st[2].getMethodName().endsWith("Call") ){ 
//				caller = st[3];
//			} else {
//				caller = st[4];
//			}
			//Looks like previous instructions are not useful
			caller = st[3];
			
			String callerString = caller.getClassName()+":"+caller.getLineNumber();
			
			ArrayList<String> contextData = new ArrayList<String>();
			contextData.add(callerString);
			md.setContextData(contextData);
		}
		
		if ( currentTestCase != null ){
			ArrayList<String> tests = new ArrayList<String>(1);
			tests.add(currentTestCase);
			md.setCurrentTests(tests);
		}
		
		String metaData = md.storeToString();
		
		LoggingActionRecorder.logIoInteractionEnterMeta(calledObject,signature, args, Thread.currentThread().getId(),metaData);
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
	public static void enter(String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed,
			Object calledObject){
		
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
		
		LoggingActionRecorder.logIoInteractionEnter(signature, argsPassed, Thread.currentThread().getId(), calledObject);
		
	}
	
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
	public static void exit(String cName, String theMethodName, String methodS, Object[] argsPassed, Object ret, Object calledObject){
//		You need the following lines only if you use Lorenzoli ObjectFlattener
//
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect( Boolean.TRUE );
//
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);

		//System.out.println( Thread.currentThread().getId()+"#IO-EXIT : "+signature);
		
		if ( methodS.endsWith("V") ){						//void method
			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed,Thread.currentThread().getId(), calledObject);
		} else {											//non void method
			LoggingActionRecorder.logIoInteractionExit(signature, argsPassed, ret,Thread.currentThread().getId(), calledObject);
		}

//      You need the following lines only if you use Lorenzoli ObjectFlattener
//		
//		//System.out.println("IOE "+signature);
//		AspectFlowChecker.setInsideAnAspect( Boolean.FALSE );

	}
	
	public static void logExceptionOnExit(Object exception){

		LoggingActionRecorder.logExceptionOnExit(exception);
	}
}
