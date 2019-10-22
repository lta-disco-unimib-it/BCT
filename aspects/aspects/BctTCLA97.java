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
import probes.ComponentCallStack;
import java.util.ArrayList;
import recorders.LoggingActionRecorder;


import util.ClassNameAndDefinitionFormatter;
import util.TcMetaInfoHandler;

public class BctTCLA97 {

  private final static int ENTER = 0;

  private final static int EXIT = 1;
  
  public static class Exec {

	  public void logEntry(final JoinPoint joinPoint) {
		  //System.out.println("ENTER "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {	
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( BctTCLA97.Exec.class == s.lastElement() ){
				  s.push( BctTCLA97.Exec.class );
				  //System.out.println("ENTER#"+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
				  return;
			  }
			  s.push( BctTCLA97.Exec.class );
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
			  logIO(joinPoint, ENTER, metaInfo);
			  LoggingActionRecorder.logInteractionEnterMeta(getSignature(joinPoint), Thread.currentThread().getId(), metaInfo );
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

	  

	public void logExit(final JoinPoint joinPoint) {
		  //System.out.println("EXIT "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( s.lastElement() != BctTCLA97.Exec.class )
				  System.out.println("POPERROR "+BctTCLA97.Exec.class.getName()+" "+getSignature(joinPoint));
			  
			  s.pop();
			  if ( BctTCLA97.Exec.class == s.lastElement() ){
				  //System.out.println("EXIT#"+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
				  return;
			  }
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
			  logIO(joinPoint, EXIT, metaInfo);
			  LoggingActionRecorder.logInteractionExitMeta(getSignature(joinPoint), Thread.currentThread().getId(), metaInfo );
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
			  s.push( BctTCLA97.Call.class );
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
			  logIO(joinPoint, ENTER, metaInfo);
			  LoggingActionRecorder.logInteractionEnterMeta(getSignature(joinPoint), Thread.currentThread().getId(), metaInfo );
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

	  public void logExit(final JoinPoint joinPoint) {
		  //System.out.println("EXIT "+BctLA0.Call.class.getName()+" "+getSignature(joinPoint));
		  try {
			  ComponentCallStack.getInstance().pop();
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
			  logIO(joinPoint, EXIT, metaInfo);
			  LoggingActionRecorder.logInteractionExitMeta(getSignature(joinPoint), Thread.currentThread().getId(), metaInfo );
			  
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
  
  private static void logIO(JoinPoint joinPoint, int callType, String metaInfo) {
	  
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
	  
	  
	  if ( callType == ENTER   ) {
		  LoggingActionRecorder.logIoEnterMeta(joinPoint.getCallee(),signature, argumentValues, metaInfo);
	  } else {

		  if ( returnType == void.class )
			  LoggingActionRecorder.logIoExitMeta(joinPoint.getCallee(),signature, argumentValues, metaInfo);
		  else
			  LoggingActionRecorder.logIoExitMeta(joinPoint.getCallee(),signature, argumentValues, returnValue, metaInfo);

	  }
  }
}
