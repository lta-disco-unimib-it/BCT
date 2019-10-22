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
import util.ClassNameAndDefinitionFormatter;
import check.Checker;
import check.InteractionInvariantHandler;

public class BctCA55 {

	public static class Exec {

		public void checkEntry(final JoinPoint joinPoint) {
			
			//System.out.println("ENTER "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
			try {	
				ComponentCallStack s = ComponentCallStack.getInstance();
				if ( BctCA55.Exec.class == s.lastElement() ){
					s.push( BctCA55.Exec.class );
					return;
				}
				s.push( BctCA55.Exec.class );

				Checker.checkIoEnter(getSignature(joinPoint), getParameters(joinPoint));
				processEvent(joinPoint, InteractionInvariantHandler.ENTER);


			} catch ( Exception e ){
				e.printStackTrace();
			}

		}

		public void checkExit(final JoinPoint joinPoint) {
			
			
			
			
			//System.out.println("EXIT "+BctLA0.Exec.class.getName()+" "+getSignature(joinPoint));
			try {
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.pop();
				if ( BctCA55.Exec.class == s.lastElement() ){
					return;
				}


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

				processEvent(joinPoint, InteractionInvariantHandler.EXIT);

			} catch ( Exception e ){
				e.printStackTrace();
			}

		}

	}

	public static class Call {

		public void checkEntry(final JoinPoint joinPoint) {
			//System.out.println("ENTER "+BctLA0.Call.class.getName()+" "+getSignature(joinPoint));
			try {	
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.push( BctCA55.Call.class );

				Checker.checkIoEnter(getSignature(joinPoint), getParameters(joinPoint));
				processEvent(joinPoint, InteractionInvariantHandler.ENTER);

			} catch ( Exception e ){
				e.printStackTrace();
			}

		}

		public void checkExit(final JoinPoint joinPoint) {
			//System.out.println("EXIT "+BctLA0.Call.class.getName()+" "+getSignature(joinPoint));
			try {
				ComponentCallStack.getInstance().pop();

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
				processEvent(joinPoint, InteractionInvariantHandler.EXIT);


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


	private static Object[] getParameters(JoinPoint joinPoint){
		if (joinPoint.getRtti() instanceof MethodRtti) {
			MethodRtti rtti = (MethodRtti) joinPoint.getRtti();
			return rtti.getParameterValues();
		} else {
			return new Object[0];
		}
	}

	private static void processEvent(JoinPoint joinPoint, InteractionInvariantHandler.MethodCallType eventType) {
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
