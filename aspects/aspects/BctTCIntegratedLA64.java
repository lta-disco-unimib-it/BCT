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
package aspects;

import org.codehaus.aspectwerkz.joinpoint.ConstructorRtti;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;

import traceReaders.metaData.ExecutionTokenMetaData;

import java.util.ArrayList;
import probes.ComponentCallStack;
import util.TcMetaInfoHandler;

import recorders.LoggingActionRecorder;


import util.ClassNameAndDefinitionFormatter;

public class BctTCIntegratedLA64 {

  private final static int ENTER = 0;

  private final static int EXIT = 1;

  public static class Exec {

	  public void logEntry(final JoinPoint joinPoint) {
		  //System.out.println("ENTER "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {	
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( BctTCIntegratedLA64.Exec.class == s.lastElement() ){
				  s.push( BctTCIntegratedLA64.Exec.class );
				  //System.out.println("ENTER#"+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
				  return;
			  }
			  
			  s.push( BctTCIntegratedLA64.Exec.class );
			  logIOInteraction(joinPoint, ENTER);
			  
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

	  public void logExit(final JoinPoint joinPoint) {
		  //System.out.println("EXIT "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( s.lastElement() != BctTCIntegratedLA64.Exec.class )
				  System.out.println("POPERROR "+BctTCIntegratedLA64.Exec.class.getName()+" "+getSignature(joinPoint));
			  
			  s.pop();
			  if ( BctTCIntegratedLA64.Exec.class == s.lastElement() ){
				  //System.out.println("EXIT#"+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
				  return;
			  }
			  logIOInteraction(joinPoint, EXIT);
			  
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

  }

  public static class Call {

	  public void logEntry(final JoinPoint joinPoint) {
		  //System.out.println("ENTER "+BctLA0.Call.class.getName()+" "+getSignature(joinPoint));
		  try {	
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  s.push( BctTCIntegratedLA64.Call.class );
			  logIOInteraction(joinPoint, ENTER);
			  
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

	  public void logExit(final JoinPoint joinPoint) {
		  //System.out.println("EXIT "+BctLA0.Call.class.getName()+" "+getSignature(joinPoint));
		  try {
			  ComponentCallStack.getInstance().pop();
			  logIOInteraction(joinPoint, EXIT);
			  
			  
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }
	  
	  
  }
  
  
  private static String getSignature(JoinPoint joinPoint){
	  String signature = null;
	    if ( joinPoint.getRtti() instanceof MethodRtti ) {
	      MethodRtti rtti = (MethodRtti)joinPoint.getRtti();
	      signature = ClassNameAndDefinitionFormatter.estractMethodSignature(joinPoint.getTargetClass().getName(),rtti.getName(), rtti.getReturnType(), rtti.getParameterTypes());
	    }
	    else {
	      ConstructorRtti rtti = (ConstructorRtti)joinPoint.getRtti();
	      signature = ClassNameAndDefinitionFormatter.estractConstructorSignature(rtti.getName(), rtti.getParameterTypes());
	    }
	    return signature;
  }
  
  private static void logIOInteraction(JoinPoint joinPoint, int callType) {
	  
	  Object returnValue = null;
	  Class returnType = null;
	  Class[] argumentTypes = null;
	  Object[] argumentValues = null;
	  String signature = null;
	  
	  
	  //Get method signature
	  
	  if ( joinPoint.getRtti() instanceof MethodRtti ) {
		  MethodRtti rtti = (MethodRtti)joinPoint.getRtti();
		  returnValue = rtti.getReturnValue();
		  returnType = rtti.getReturnType();
		  
		  argumentValues = rtti.getParameterValues();
		  argumentTypes = rtti.getParameterTypes();
		  
		  signature = ClassNameAndDefinitionFormatter.estractMethodSignature(joinPoint.getTargetClass().getName(), rtti.getName(), returnType, argumentTypes);
	  }
	  else {
		  ConstructorRtti rtti = (ConstructorRtti)joinPoint.getRtti();
		  
		  argumentValues = rtti.getParameterValues();
		  argumentTypes = rtti.getParameterTypes();
		  signature = ClassNameAndDefinitionFormatter.estractConstructorSignature(rtti.getName(), argumentTypes);
	  }
	  
	  //do logging actions
	  
	  
	  ExecutionTokenMetaData md = new ExecutionTokenMetaData();
			  Object calledObject = joinPoint.getCallee();
			  if ( calledObject != null ){
				  md.setCalledObjectId(""+System.identityHashCode(calledObject));
			  }

			  md.setTimestamp(System.currentTimeMillis());

			  String currentTestCase = TcMetaInfoHandler.getCurrentTestCase();
			  if ( currentTestCase != null ){
				  ArrayList<String> tests = new ArrayList<String>(1);
				  tests.add(currentTestCase);
				  md.setCurrentTests(tests);
			  }

			  String metaInfo = md.storeToString();
	  
	  if ( callType == ENTER   ) {
		  LoggingActionRecorder.logIoInteractionEnterMeta(joinPoint.getCallee(),signature, argumentValues, Thread.currentThread().getId(), metaInfo );
	  } else {

		  if ( returnType == void.class )
			  LoggingActionRecorder.logIoInteractionExitMeta(joinPoint.getCallee(),signature, argumentValues, Thread.currentThread().getId(), metaInfo);
		  else
			  LoggingActionRecorder.logIoInteractionExitMeta(joinPoint.getCallee(),signature, argumentValues, returnValue, Thread.currentThread().getId(), metaInfo);

	  }
  }
}
