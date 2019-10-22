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
package log;

import org.codehaus.aspectwerkz.joinpoint.ConstructorRtti;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;

import recorders.LoggingActionRecorder;
import util.ClassNameAndDefinitionFormatter;
import check.AspectFlowChecker;

public class IOInteractionLoggingAspect {

	  private final static int ENTER = 0;

	  private final static int EXIT = 1;

	  public IOInteractionLoggingAspect() {
		  
	  }
	  
	  public void ioInteractionLogEntry(final JoinPoint joinPoint) { 
		  if ( AspectFlowChecker.isInsideAnAspect() )
			  return;
		  AspectFlowChecker.setInsideAnAspect( true );

		  try {
			  logInteractions(joinPoint, ENTER);
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }
		  AspectFlowChecker.setInsideAnAspect( false );
	  }
	  
	  public void ioInteractionLogExit(final JoinPoint joinPoint) {
		  if ( AspectFlowChecker.isInsideAnAspect() )
			  return;
		  AspectFlowChecker.setInsideAnAspect( true );

		  try {
			  logInteractions(joinPoint, EXIT);
		  } catch ( Exception e ){
			  e.printStackTrace();
		  }
		  
		  AspectFlowChecker.setInsideAnAspect( false );
	  }
	  
	  private void logInteractions(JoinPoint joinPoint, int callType) {
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
			  LoggingActionRecorder.logIoInteractionEnter(signature, argumentValues, Thread.currentThread().getId());
		  } else {

			  if ( returnType == void.class )
				  LoggingActionRecorder.logIoInteractionExit(signature, argumentValues, Thread.currentThread().getId());
			  else
				  LoggingActionRecorder.logIoInteractionExit(signature, argumentValues, returnValue, Thread.currentThread().getId());

		  }
	  }
}
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  