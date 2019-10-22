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

import probes.CheckerProbeStatic;
import probes.ComponentCallStack;

/**
 * BCT Checking Probe 
 */
class bctCPP4 {
	// Class for probe unnamed_probe
	public static class Probe_0 {
		// Fragment at class scope
		int id = 1;
		public static void _afterCall (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ calledObject,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				ComponentCallStack.getInstance().pop();
				CheckerProbeStatic.checkExit(className,methodName,methodSig,args,returnedObject,calledObject);
				
			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
		public static void _beforeCall (
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ calledObject,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				s.push( bctCP4.Probe_0.class );

				CheckerProbeStatic.checkEnter(className,methodName,methodSig,args,calledObject);

			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
	}
	// Class for probe unnamed_probe
	public static class Probe_1 {
		// Fragment at class scope
		int id = 0;
		public static void _exit (
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ calledObject,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.pop();
				if ( bctCP4.Probe_1.class == s.lastElement() ){
					return;
				}
				CheckerProbeStatic.checkExit(className,methodName,methodSig,args,returnedObject,calledObject);

			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
		public static void _entry (
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ calledObject,
				Object[] /*args*/ args      ) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				if ( bctCP4.Probe_1.class == s.lastElement() ){
					s.push( bctCP4.Probe_1.class );
					return;
				}
				s.push( bctCP4.Probe_1.class );
				CheckerProbeStatic.checkEnter(className,methodName,methodSig,args,calledObject);
				
			} catch ( Exception e ){
				System.out.println("BCT COMPONENT LOGGER EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
	}
}
