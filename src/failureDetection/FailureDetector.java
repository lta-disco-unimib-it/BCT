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
package failureDetection;

import recorders.RecorderException;
import recorders.RecorderFactory;
import recorders.ViolationsRecorder;
import util.RuntimeContextualDataUtil;

public class FailureDetector {
	
	
	public Failure throwableCaught( Throwable e, String catchingMethod ){
		if ( e instanceof Error ||
				e instanceof RuntimeException ){
			ExceptionFailure f = new ExceptionFailure(
					RuntimeContextualDataUtil.getNewUniqueId(),
					System.currentTimeMillis(),
					catchingMethod,
					Thread.currentThread().getId(),
					e.getClass().getCanonicalName(), 
					e.getMessage(), 
					RuntimeContextualDataUtil.retrieveStringStackTrace(e.getStackTrace()),
					catchingMethod
			);
			f.setCritical(true);
			fillContextData( f );
			
			return f;
		}
		
		return null;
	}
	
	public Failure throwableCaughtInTest( Throwable e, String catchingMethod, String testId ){
		if ( e instanceof Error ||
				e instanceof RuntimeException ){
			ExceptionFailure f = new ExceptionFailure(
					RuntimeContextualDataUtil.getNewUniqueId(),
					System.currentTimeMillis(),
					catchingMethod,
					Thread.currentThread().getId(),
					e.getClass().getCanonicalName(), 
					e.getMessage(), 
					RuntimeContextualDataUtil.retrieveStringStackTrace(e.getStackTrace()),
					catchingMethod
			);
			
			if ( ! ( e instanceof AssertionError ) ){
				f.setCritical(true);
			}
			
			f.setFailingTestId(testId);
			
			fillContextData( f );
			
			traceFailure(f);
			return f;
		}
		
		return null;
	}
	
	private void traceFailure(ExceptionFailure failure) {
		ViolationsRecorder violationRecorder = RecorderFactory.getViolationsRecorder();
		try {
			violationRecorder.recordFailure(failure);
		} catch (RecorderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//TODO: update failure manager state, necessary for automatic healing
	}

	public Failure throwableCaughtInAction( Throwable e, String catchingMethod, String actionId ){
		if ( e instanceof Error ||
				e instanceof RuntimeException ){
			ExceptionFailure f = new ExceptionFailure(
					RuntimeContextualDataUtil.getNewUniqueId(),
					System.currentTimeMillis(),
					catchingMethod,
					Thread.currentThread().getId(),
					e.getClass().getCanonicalName(), 
					e.getMessage(), 
					RuntimeContextualDataUtil.retrieveStringStackTrace(e.getStackTrace()),
					catchingMethod
			);
			f.setCritical(true);
			f.setFailingActionId(actionId);
			
			fillContextData( f );
			
			return f;
		}
		
		return null;
	}
	
	

	private void fillContextData(Failure f) {
		f.setFailingPID(RuntimeContextualDataUtil.retrievePID());
		
		f.setActiveActionsIds(RuntimeContextualDataUtil.retrieveCurrentActions());
		
		f.setActiveTestsIds(RuntimeContextualDataUtil.retrieveCurrentTestCases());
	}
}
