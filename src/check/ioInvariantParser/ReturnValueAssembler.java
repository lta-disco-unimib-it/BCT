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
package check.ioInvariantParser;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

public class ReturnValueAssembler extends Assembler {

  public void workOn(Assembly a) {
    Target target = null; 
    Object returnValue = null;
    try{
    	target = (Target)a.getTarget();
    	returnValue = target.getReturnValue();
    	/*
    	if(returnValue instanceof Boolean)
    		if(((Boolean)returnValue).booleanValue())
    			returnValue = new Double(1);
    		else
    			returnValue = new Double(0); 
    			*/
    	target.push(returnValue);
    }
    catch (Exception e) {
		EvaluationRuntimeErrors.evaluationError();
		//target.push(null);
		return;
	}
  }
}
