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

import util.MethodCallsRegistry;
import check.Checker;

/**
 * This class is used by real probes to check if an interaction is correct.
 * This implementation is optimized for static instrumentation since it does not check if the method being monitored is part of BCT or not.
 * Plus it cannot be used to monitor java classes (as they are called by BCT itself).
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class CheckerProbeStatic {	
	
	public static void checkEnter(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed
	){
		
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect(true);
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
//		String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );
		
		Checker.checkIoEnter(signature, argsPassed);
		Checker.checkInteractionEnter( signature, Thread.currentThread().getId() );
		
//		AspectFlowChecker.setInsideAnAspect(false);
		
	}

	public static void checkExit(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed,
			Object 	ret
	){
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect(true);

		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
		//String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );

		if ( methodS.endsWith("V") )
			Checker.checkIoExit(signature, argsPassed );
		else
			Checker.checkIoExit( signature, argsPassed, ret );
		
		Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
		
		
//		AspectFlowChecker.setInsideAnAspect(false);
	}
	
	
	public static void checkEnter(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed,
			Object calledObject
	){
		
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect(true);
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
//		String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );
		
		Checker.checkIoEnter(signature, argsPassed);
		Checker.checkInteractionEnter( signature, Thread.currentThread().getId() );
		MethodCallsRegistry.methodEntered(calledObject, signature);
//		AspectFlowChecker.setInsideAnAspect(false);
		
	}

	public static void checkExit(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed,
			Object 	ret,
			Object calledObject
	){
//		if ( AspectFlowChecker.isInsideAnAspect() )
//			return;
//		AspectFlowChecker.setInsideAnAspect(true);

		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
		//String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );

		if ( methodS.endsWith("V") )
			Checker.checkIoExit(signature, argsPassed );
		else
			Checker.checkIoExit( signature, argsPassed, ret );
		
		Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
		
		MethodCallsRegistry.methodExited(calledObject, signature);
//		AspectFlowChecker.setInsideAnAspect(false);
	}
}
