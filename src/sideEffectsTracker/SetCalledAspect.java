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
package sideEffectsTracker;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;



/**
 * This aspect is used to check if objectflattener change the status of the object is flattening.
 * Changes on status for exemple can occurr when a getter
 * method is not a pure getter but instantiates some variables (for example if lazy load is used ).
 * This aspects print a the name of the getter that is changing the status, 
 * the name of the setter and the stacktrace. 
 * To resolve the problem you have to tell ObjectFlattener to not flatten getters class 
 * (so you have to run another logging execution).
 * 
 * Checking for setters permits to check if status is changed via setters, it is not a complete
 * methodology to check for state changes but is less invasive than checking for assignments.
 * 
 * To use this aspect use code like this:
 * <code>
 * <aspect class="SetCalledAspect">
 *      <pointcut name="c0" expression="execution(* flattener.flatteners.ObjectFlattener.*(..))" />
 *      <pointcut name="c1" expression="execution(* flattener.handlers..*.*(..))" />
 *      <pointcut name="sideEffectsRemoval" expression="cflow( c0 ) AND ! cflow( c1 )" />
 *      <pointcut name="pc0" expression="sideEffectsRemoval  AND  mypointcutExpression" />
 *      <advice name="before" type="before" bind-to="pc0" />
 * </aspect>
 * </code>
 * 
 * @author Fabrizio Pastore [ fabrizio.pastore at gmail dot com ]
 *
 */
public class SetCalledAspect {
	
	public void before ( JoinPoint jp ){
		
		//if ( OFRunningRegistry.getInstance().isInFlattener() ){
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			//int fPos = -1;
			//if ( ( fPos = flattenerPos ( st ) ) >= 0 ){
			//System.out.println( jp.getSignature() );
			int pos = -1;
			int fPos = -1;
			
			//first is dumpThreads() second is getStackTrace() so start from 2
			for( int i = 2; fPos == -1 && i < st.length; ++i ){
				if ( st[i].getMethodName().startsWith("get") )
					pos = i;
				if ( st[i].getClassName().equals("flattener.flatteners.ObjectFlattener"))
					fPos = i;
					
				
			}
			if ( pos > -1 && fPos > -1 ){
				System.out.println ( "Called setter : "+jp.getSignature() );
				Thread.dumpStack();
				System.out.println(" Modified from : "+st[pos]+" "+pos);
				System.out.println(" "+st[0]+" ");
			}
			//System.out.println(  );
			
		//}
	}

	private int flattenerPos(StackTraceElement[] st) {
		int len = st.length;
		for( int i = 0; i < len; ++i ){
			if ( st[i].getClassName().startsWith("flattener."))
				return i;
		}
		
		return -1;
	}
}
