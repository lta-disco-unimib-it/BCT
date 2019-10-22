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
package tools.fshellExporter.parser;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

public abstract class ComparisonOperatorAssembler extends Assembler {

	private String operator;


	protected ComparisonOperatorAssembler(String operator){
		this.operator=operator;
	}


	public void workOn(Assembly a) {
		Target target = null;
		Object parameter2 = null;

		try{
			target = (Target)a.getTarget();
			parameter2 = (Object)target.pop();  
		}catch(ArrayIndexOutOfBoundsException e) {
			EvaluationRuntimeErrors.emptyStack();
			return;
		}

		//Object parameter1 = (Object)target.pop();
		Object parameter1 = null;
		try{
			parameter1 = (Object)target.pop(); 
		}catch(ArrayIndexOutOfBoundsException e) {
			EvaluationRuntimeErrors.emptyStack();
			return;
		}

		target.push(parameter1+" " +operator+" "+parameter2);
	}
}
