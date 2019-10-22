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

public class FieldModifiedAspect {
	
	public void before ( JoinPoint jp ){
		
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
				else if ( st[i].getMethodName().startsWith("is") )
					pos = i;
				if ( st[i].getClassName().equals("flattener.flatteners.ObjectFlattener"))
					fPos = i;
					
				
			}
//			System.out.println ( "Modified field : "+jp.getSignature() );
			if ( pos > -1 && fPos > -1 ){
				System.out.println ( "Modified field : "+jp.getSignature() );
				Thread.dumpStack();
				System.out.println(" Modified from : "+st[pos]+" "+pos);
				System.out.println(" "+st[0]+" ");
			}
	}
}
