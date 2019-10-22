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

import java.util.NoSuchElementException;

import check.ioInvariantParser.NonExistentVariable.Types;

import sjm.parse.Alternation;
import sjm.parse.Assembler;
import sjm.parse.Assembly;
import sjm.parse.Parser;
import sjm.parse.tokens.Token;

public class WordAssembler extends Assembler {

	
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
			String variableName = tokenIndex.sval();
//			System.out.println("V "+variableName);
			//HACK for *name
			if ( ! target.isEmpty() ){
				Object top = target.top();
				if ( top instanceof Star ){
					target.pop();
					variableName = ((Star) top).getValue()+variableName;
				} else if ( top instanceof Ref ){
					target.pop();
					variableName = ((Ref) top).getValue()+variableName;
				}
			}
			
			//HACK HACK HACK
			//this is an hack, following code should not be there,
			//in principle we could properlu handle null by changing term() in IoInvariantParser
			//but I am afraid of changig that code
//			  private static Parser term() {
//				    return new Alternation()
//				      .add(identifier())
//				      .add(literal())
//				      .add(arrayDeclaration());
//				  } 
			
			if ( variableName.equals("null") ){
				target.push(null);
				return;
			}
			
			//HACK END
			
			try {
				Object localVariable = target.getLocalVariable( variableName );
				
//				System.out.println("LOCAL VAR: "+variableName + " : " + localVariable);
				
				target.push(localVariable);
			} catch ( NoSuchElementException e ){
				target.block();
				target.push(new NonExistentVariable(Types.VARIABLE,variableName));
				EvaluationRuntimeErrors.noSuchVariable(variableName);

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
