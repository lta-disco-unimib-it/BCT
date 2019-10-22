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


public class IoInvariantCheckerAspect {

	

	public void before(JoinPoint joinPoint) throws FileNotFoundException {
		//EnvironmentalSetter.setStreams();

		if ( AspectFlowChecker.isInsideAnAspect() )
			return;
		AspectFlowChecker.setInsideAnAspect(true);
		
		
		Checker.checkIoEnter(getSignature(joinPoint), getParameters(joinPoint));
		
		AspectFlowChecker.setInsideAnAspect(false);
	}

	public void after(JoinPoint joinPoint) throws FileNotFoundException {
		//EnvironmentalSetter.setStreams();

		if ( AspectFlowChecker.isInsideAnAspect() )
			return;
		AspectFlowChecker.setInsideAnAspect(true);

		
		
		Object[] parameters;
		
		if (joinPoint.getRtti() instanceof MethodRtti) {
			MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
			parameters  = rtti.getParameterValues();
			if ( rtti.getReturnType() != void.class ){
				Checker.checkIoExit(getSignature(joinPoint), parameters, rtti.getReturnValue() );
			} else {
				Checker.checkIoExit(getSignature(joinPoint), parameters );
			}
		} else {
				//Constructor
				Checker.checkIoExit(getSignature(joinPoint), new Object[0] );
		}
		
		
		
		
		AspectFlowChecker.setInsideAnAspect(false);
	}

	private String getSignature(JoinPoint joinPoint){
		if (joinPoint.getRtti() instanceof MethodRtti) {
			MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
			return ClassNameAndDefinitionFormatter.estractMethodSignature(
					joinPoint.getTargetClass().getName(), rtti.getName(), rtti
							.getReturnType(), rtti.getParameterTypes());
		} else {
			ConstructorRtti rtti = (ConstructorRtti) joinPoint.getRtti();
			
			return ClassNameAndDefinitionFormatter
					.estractConstructorSignature(rtti.getName(), rtti
							.getParameterTypes());
		}
	}
	
	
	private Object[] getParameters(JoinPoint joinPoint){
		if (joinPoint.getRtti() instanceof MethodRtti) {
			MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
			return rtti.getParameterValues();
		} else {
			return new Object[0];
		}
	}
	

}