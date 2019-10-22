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

import check.AspectFlowChecker;
import check.Checker;

public class IoCheckerProbe {	
	
	public static void checkEnter(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed
	){
		
		if ( AspectFlowChecker.isInsideAnAspect() )
			return;
		AspectFlowChecker.setInsideAnAspect(true);
		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
//		String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );
		
		Checker.checkIoEnter(signature, argsPassed);
	
		AspectFlowChecker.setInsideAnAspect(false);
		
	}

	public static void checkExit(
			String /*className*/ cName,
			String /*methodName*/ theMethodName,
			String /*methodSig*/ methodS,
			Object[] /*args*/ argsPassed,
			Object 	ret
	){
		if ( AspectFlowChecker.isInsideAnAspect() )
			return;
		AspectFlowChecker.setInsideAnAspect(true);

		String signature = ClassFormatter.getSignature(cName,theMethodName,methodS);
		//String signature = ClassFormatter.getSignature(methodS, argsPassed, cName, theMethodName );

		if ( methodS.endsWith("V") )
			Checker.checkIoExit(signature, argsPassed );
		else
			Checker.checkIoExit( signature, argsPassed, ret );
		
		
		
		AspectFlowChecker.setInsideAnAspect(false);
	}
	
}
