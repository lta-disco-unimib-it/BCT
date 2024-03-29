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

import probes.ComponentCallStack;
import recorders.LoggingActionRecorder;
import util.ClassNameAndDefinitionFormatter;

public class BctIntegratedLA13 {

  private final static int ENTER = 0;

  private final static int EXIT = 1;

  public static class Exec {

	  public void logEntry(final JoinPoint joinPoint) {
		  //System.out.println("ENTER "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {	
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( BctIntegratedLA13.Exec.class == s.lastElement() ){
				  s.push( BctIntegratedLA13.Exec.class );
				  //System.out.println("ENTER#"+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
				  return;
			  }
			  s.push( BctIntegratedLA13.Exec.class );
			  logIOInteraction(joinPoint, ENTER);
			  
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }

	  }

	  public void logExit(final JoinPoint joinPoint) {
		  //System.out.println("EXIT "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
		  try {
			  ComponentCallStack s = ComponentCallStack.getInstance();
			  if ( s.lastElement() != BctIntegratedLA13.Exec.class )
				  System.out.println("POPERROR "+BctIntegratedLA13.Exec.class.getName()+" "+getSignature(joinPoint));
			  
			  s.pop();
			  if ( BctIntegratedLA13.Exec.class == s.lastElement() ){
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
			  s.push( BctIntegratedLA13.Call.class );
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
	  
	  
	  if ( callType == ENTER   ) {
		  LoggingActionRecorder.logIoInteractionEnter(signature, argumentValues, Thread.currentThread().getId() );
	  } else {

		  if ( returnType == void.class )
			  LoggingActionRecorder.logIoInteractionExit(signature, argumentValues, Thread.currentThread().getId());
		  else
			  LoggingActionRecorder.logIoInteractionExit(signature, argumentValues, returnValue, Thread.currentThread().getId());

	  }
  }
}
