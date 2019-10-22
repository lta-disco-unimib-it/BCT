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
// generated source from Probekit compiler
/* probekit /BCT/probes/bctLoggingProbeOutgoing.probe
*/
// "imports" specifications for probes (if any):
import probes.IoLoggerProbe; // from unnamed_probe
import probes.InteractionLoggerProbe; // from unnamed_probe
class bctLoggingProbeOutgoing_probe {
  // Class for probe unnamed_probe
  public static class Probe_0 {
    public static void _afterCall (
      Object /*returnedObject*/ returnedObject,
      String /*className*/ className,
      String /*methodName*/ methodName,
      String /*methodSig*/ methodSig,
      Object[] /*args*/ args      ) {
      // Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//------------------ begin user-written fragment code ----------------
try{
   IoLoggerProbe.ioLogExit(className,methodName,methodSig,args,returnedObject);
   InteractionLoggerProbe.intLogExit(className,methodName,methodSig,args);
} catch ( Exception e ){
 	System.out.println("EXC "+e.getMessage());
}
//------------------- end user-written fragment code -----------------
    }
    public static void _beforeCall (
      String /*className*/ className,
      String /*methodName*/ methodName,
      String /*methodSig*/ methodSig,
      Object[] /*args*/ args      ) {
      // Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//------------------ begin user-written fragment code ----------------
try{
	IoLoggerProbe.ioLogEnter(className,methodName,methodSig,args);
   InteractionLoggerProbe.intLogEnter(className,methodName,methodSig,args);
} catch ( Exception e ){
 	System.out.println("EXC "+e.getMessage());
}
//------------------- end user-written fragment code -----------------
    }
  }
}
