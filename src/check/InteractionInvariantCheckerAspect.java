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
package check;

import java.io.FileNotFoundException;

import org.codehaus.aspectwerkz.joinpoint.ConstructorRtti;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;

import util.ClassNameAndDefinitionFormatter;

public class InteractionInvariantCheckerAspect {

  public void before(JoinPoint joinPoint) throws FileNotFoundException {
      
	  if ( AspectFlowChecker.isInsideAnAspect() )
		  return;
	  AspectFlowChecker.setInsideAnAspect(true);
	  

      
	  try{
	  processEvent(joinPoint, InteractionInvariantHandler.ENTER);
  	}catch(Exception e){
		e.printStackTrace();
	}
  	
  		
  		AspectFlowChecker.setInsideAnAspect(false);
  }

  public void after(JoinPoint joinPoint) throws FileNotFoundException {

	  if ( AspectFlowChecker.isInsideAnAspect() )
		  return;
	  AspectFlowChecker.setInsideAnAspect(true);
	  
	  
	 try{  
		processEvent(joinPoint, InteractionInvariantHandler.EXIT);
	}catch(Exception e){
		e.printStackTrace();
	}	
		
		AspectFlowChecker.setInsideAnAspect(false);
  }



  private void processEvent(JoinPoint joinPoint, InteractionInvariantHandler.MethodCallType eventType) {
    String signature = null;
    if ( joinPoint.getRtti() instanceof MethodRtti ) {
      MethodRtti rtti = (MethodRtti)joinPoint.getRtti();
      signature = ClassNameAndDefinitionFormatter.estractMethodSignature(joinPoint.getTargetClass().getName(),rtti.getName(),rtti.getReturnType(),rtti.getParameterTypes());
    }
    else {
      ConstructorRtti rtti = (ConstructorRtti)joinPoint.getRtti();
      signature = ClassNameAndDefinitionFormatter.estractConstructorSignature(rtti.getName(),rtti.getParameterTypes());
    }
    long threadID = Thread.currentThread().getId();


    if ( eventType.equals(InteractionInvariantHandler.ENTER))
    	Checker.checkInteractionEnter(signature, threadID );
    else
    	Checker.checkInteractionExit(signature, threadID );
  }
}
