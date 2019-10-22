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
package asp;

import org.aspectj.lang.JoinPoint;

import check.Checker;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;

import probes.BctTestCasesMonitorAspect;
import probes.LoggerProbe;
import recorders.LoggingActionRecorder;
import util.TcMetaInfoHandler;

public class BctAspectUtil {
	
	private static final boolean checking = Boolean.getBoolean("bct.run.checking");
	
	public void enter(JoinPoint jp){
		String signature = extractSignature(jp);
		
		if ( checking ){
			Checker.checkIoEnter( signature, jp.getArgs());
			Checker.checkInteractionEnter( signature, Thread.currentThread().getId() );
		} else {
			LoggerProbe.enterMeta(signature,jp.getArgs(),TcMetaInfoHandler.getCurrentTestCase(),jp.getThis());
		}
	}

	public void exit(JoinPoint jp,Object res, Throwable t){
		String signature = extractSignature(jp);
		if ( checking ){
			Checker.checkIoExit( signature, jp.getArgs(), res);
			Checker.checkInteractionExit( signature, Thread.currentThread().getId() );
		} else {
			LoggerProbe.exitMeta(signature, jp.getArgs(), res, TcMetaInfoHandler.getCurrentTestCase(),jp.getThis());
			if ( t != null ){
				LoggingActionRecorder.logExceptionOnExit(t);
			}
		}
	}
	
	public void exitException(JoinPoint jp,Throwable t){
		LoggingActionRecorder.logExceptionOnExit(t);
	}
	
	public void enterTest(JoinPoint jp){
		String signature = extractSignature(jp);
		BctTestCasesMonitorAspect.enter(signature);
	}

	public void exitTest(JoinPoint jp,Throwable t){
		String signature = extractSignature(jp);
		BctTestCasesMonitorAspect.exit(signature,t);
	}
	
	public void exitTestFailure(JoinPoint jp,Throwable t){
		String signature = extractSignature(jp);
		//System.out.println("TEST FAILURE");
		BctTestCasesMonitorAspect.traceTestFailure( signature, t );
	}
	
	private String extractSignature(JoinPoint joinPoint) {
		String signature = joinPoint.getSignature().toLongString();
		
		
		int indexOfPar = signature.indexOf('(');
		
		int indexOfSpace = signature.substring(0, indexOfPar).lastIndexOf(' ');
		

		signature=signature.substring(indexOfSpace+1);
		signature=signature.replace(' ', '_');
		
		return signature;
	}
	
	

}
