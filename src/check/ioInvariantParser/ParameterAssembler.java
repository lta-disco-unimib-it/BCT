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

import java.security.Policy.Parameters;

import check.ioInvariantParser.NonExistentVariable.Types;

import sjm.parse.Assembler;
import sjm.parse.Assembly;
import sjm.parse.tokens.Token;

public class ParameterAssembler extends Assembler {

  public void workOn(Assembly a) {   
    Target target = (Target)a.getTarget();
    //Token tokenIndex = (Token)a.pop();
    Token tokenIndex = null;
    try{
    	tokenIndex = (Token)a.pop();
    }catch(ArrayIndexOutOfBoundsException e) {
      	EvaluationRuntimeErrors.emptyStack();
      	return;
    }
    try{
    	int index = (int)tokenIndex.nval();
    	
    	Object[] parameters = target.getParameters();
    	
    	if ( index >= parameters.length ){
    		target.push(new NonExistentVariable(Types.VARIABLE, "parameter["+index+"]"));
    		target.block();
    		EvaluationRuntimeErrors.noSuchField("parameter["+index+"]");
    	} else {
    	
    		Object value = parameters[index];
    	
    	/*
    	 Boolean cannot be chaged
    	 
    	if(value instanceof Boolean)
    		if(((Boolean)value).booleanValue())
    			value = new Double(1);
    		else
    			value = new Double(0);
    	*/
    	target.push(value);
    	}
    }
    catch (Exception e) {
    	e.printStackTrace();
		EvaluationRuntimeErrors.evaluationError();
		//target.push(Boolean.FALSE);
		return;
	}
  }
}