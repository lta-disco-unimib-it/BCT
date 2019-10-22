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

import probes.LoggerProbe; 
import probes.ComponentCallStack;

import util.TcMetaInfoHandler;

class bctTCLP15 {
	// Class for probe unnamed_probe
	public static class Probe_0 {
		// Fragment at class scope
		int id = 1;
		public static void _afterCall (
				
				Object /*returnedObject*/ returnedObject,
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ thisObject,
				Object[] /*args*/ args) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//			------------------ begin user-written fragment code ----------------
			try{
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"CALLEXIT"+bctLP15.class);
				ComponentCallStack.getInstance().pop();
				LoggerProbe.exitMeta(className,methodName,methodSig,args,returnedObject,TcMetaInfoHandler.getCurrentTestCase(),thisObject);
				
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}
//			------------------- end user-written fragment code -----------------
		}
		public static void _beforeCall (
				
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ thisObject,
				Object[] /*args*/ args) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"CALLENTER"+bctLP15.class);
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.push( bctLP15.Probe_0.class );

				LoggerProbe.enterMeta(className,methodName,methodSig,args,TcMetaInfoHandler.getCurrentTestCase(),thisObject);
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

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
				Object /*thisObject*/ thisObject,
				Object[] /*args*/ args) {
			// Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{
				ComponentCallStack s = ComponentCallStack.getInstance();
				s.pop();
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"EXIT "+className+"."+methodName+" "+methodSig);
				if ( bctLP15.Probe_1.class == s.lastElement() ){
					//System.out.println("#"+Thread.currentThread().getId()+"#"+"DISCARD");
					return;
				}
				LoggerProbe.exitMeta(className,methodName,methodSig,args,returnedObject,TcMetaInfoHandler.getCurrentTestCase(),thisObject);

			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

		}
		
		
		public static void _entry (
				
				String /*className*/ className,
				String /*methodName*/ methodName,
				String /*methodSig*/ methodSig,
				Object /*thisObject*/ thisObject,
				Object[] /*args*/ args) {
			// Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V

			try{

				ComponentCallStack s = ComponentCallStack.getInstance();
				//System.out.println("#"+Thread.currentThread().getId()+"#"+"ENTER "+className+"."+methodName+" "+methodSig);
				if ( bctLP15.Probe_1.class == s.lastElement() ){
					s.push( bctLP15.Probe_1.class );
					//System.out.println("#"+Thread.currentThread().getId()+"#"+"DISCARD");
					return;
				}
				s.push( bctLP15.Probe_1.class );
				LoggerProbe.enterMeta(className,methodName,methodSig,args,TcMetaInfoHandler.getCurrentTestCase(),thisObject);
			} catch ( Throwable e ){
				System.err.println("BCT COMPONENT LOGGER ERROR/EXCEPTION: "+e.getMessage());
				e.printStackTrace();
			}

		}
	}
}
