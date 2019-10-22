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
/* probekit /BCT/probes/bctCheckingProbeOutgoing.probe
*/
// "imports" specifications for probes (if any):
import probes.IoCheckerProbe; // from unnamed_probe
import probes.InteractionCheckerProbe; // from unnamed_probe
class bctCheckingProbeOutgoing_probe {
  // Class for probe unnamed_probe
  public static class Probe_0 {
    public static void _beforeCall (
      String /*className*/ cName,
      String /*methodName*/ theMethodName,
      String /*methodSig*/ methodS,
      Object[] /*args*/ argsPassed      ) {
      // Internal signature for this method: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//------------------ begin user-written fragment code ----------------
try{
	IoCheckerProbe.checkEnter(cName,theMethodName,methodS,argsPassed);
	InteractionCheckerProbe.checkEnter(cName,theMethodName,methodS,argsPassed);
	
} catch ( Exception e ){
 	System.out.println("EXC ENTER "+e.getMessage());
 	e.printStackTrace();
}
//------------------- end user-written fragment code -----------------
    }
    public static void _afterCall (
      Object /*returnedObject*/ ret,
      String /*className*/ cName,
      String /*methodName*/ theMethodName,
      String /*methodSig*/ methodS,
      Object[] /*args*/ argsPassed      ) {
      // Internal signature for this method: (Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V
//------------------ begin user-written fragment code ----------------
try{
	IoCheckerProbe.checkExit(cName,theMethodName,methodS,argsPassed,ret);
	InteractionCheckerProbe.checkExit(cName,theMethodName,methodS,argsPassed,ret);
} catch ( Exception e ){
 	System.out.println("EXC EXIT"+e.getMessage());
}
//------------------- end user-written fragment code -----------------
    }
  }
}
