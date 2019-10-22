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

import util.ClassNameAndDefinitionFormatter;
import util.TCExecutionRegistry;
import util.TCExecutionRegistryException;

public class TCObserverAspect {
	
	public static void enter( JoinPoint joinPoint ){
		try {
			System.out.println("ENTER");
			TCExecutionRegistry.getInstance().tcEnter(getSignature(joinPoint));
		} catch (TCExecutionRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void exit( JoinPoint joinPoint ){
		try {
			System.out.println("EXIT");
			TCExecutionRegistry.getInstance().tcExit(getSignature(joinPoint));
		} catch (TCExecutionRegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
