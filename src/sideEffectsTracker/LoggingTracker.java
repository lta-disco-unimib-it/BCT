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
import org.codehaus.aspectwerkz.joinpoint.MethodRtti;

/**
 * This aspect is used to keep trace of the execution sequence of IOLogginAspect and InteractionLoggingAspect.
 * It is used to locate the objects that (after beeing inspected by the object flattener) cause changes
 * in the method call sequence. 
 * 
 * It traces:
 * Class Types of the Objects ObjectFlattener is working on 
 * and   
 * Methods the InteractionLogginAspect is loggin.
 * 
 * 
 * It is useful because we can see what objects are ispected near (after or before ) the call of a method
 * and so deduce what are the objects that cause side effects on invariants violation  
 * @author Fabrizio Pastore [ fabrizio.pastore at gmail dot com ]
 *
 */
public class LoggingTracker {
	
	public void ioLogger ( JoinPoint joinPoint ){
		MethodRtti logMeth = (MethodRtti) joinPoint.getRtti();
		
		Object[] pars = logMeth.getParameterValues();
		
		Object[] values = (Object[])pars[3];
		
		for ( int i = 0; i < values.length; i++ ){
			if ( values[i] != null )
				System.out.println("IOLogging working on "+values[i].getClass());
		}
	}
}
