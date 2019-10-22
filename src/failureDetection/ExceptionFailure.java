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

import util.ComparisonUtil;

public class ExceptionFailure extends Failure {
	private String exceptionClass;
	private String exceptionMSg;
	private String stackTrace[];
	private String catchingMethod;
	
	
	public ExceptionFailure(
			String id,
			long detectionTime,
			String failingComponent,
			long failingTheadId,
			String exceptionClass, 
			String exceptionMSg,
			String[] stackTrace, 
			String catchingMethod) {
		super(id,detectionTime,failingComponent,failingTheadId);
		this.exceptionClass = exceptionClass;
		this.exceptionMSg = exceptionMSg;
		this.stackTrace = stackTrace;
		this.catchingMethod = catchingMethod;
	}
	
	public String getExceptionClass() {
		return exceptionClass;
	}
	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}
	public String getExceptionMsg() {
		return exceptionMSg;
	}
	public void setExceptionMsg(String exceptionMSg) {
		this.exceptionMSg = exceptionMSg;
	}
	public String[] getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String[] stackTrace) {
		this.stackTrace = stackTrace;
	}
	public String getCatchingMethod() {
		return catchingMethod;
	}
	public void setCatchingMethod(String catchingMethod) {
		this.catchingMethod = catchingMethod;
	}
	
	public boolean equals (Object o ){
		if ( ! super.equals(o) ){
			return false;
		}
		
		if ( ! ( o instanceof ExceptionFailure ) ){
			return false;
		}
		
		ExceptionFailure rhs = (ExceptionFailure) o;
		
		if ( ! exceptionClass.equals(rhs.exceptionClass) ){
			return false;
		}
		
		if ( ! exceptionMSg.equals(rhs.exceptionMSg) ){
			return false;
		}
		
		if ( ! catchingMethod.equals(rhs.catchingMethod) ){
			return false;
		}
		
		return ComparisonUtil.equalsArray(stackTrace, rhs.stackTrace);
		
	}
}
