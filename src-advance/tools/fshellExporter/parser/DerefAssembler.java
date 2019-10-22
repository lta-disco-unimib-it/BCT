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

public class DerefAssembler extends Assembler {

	private DerefOperatorAssembler derefOperatorAssembler;

	public DerefAssembler(DerefOperatorAssembler derefOperatorAssembler) {
		this.derefOperatorAssembler = derefOperatorAssembler;
	}

	@Override
	public void workOn(Assembly a) {
		Target target = (Target) a.getTarget();
//		System.out.println("DEREF TARGET"+target.toString());
		
		Object popped = a.pop();
		
		String varName = popped.toString();
		
		int dereferences = pendingDereferences(target);
		
//		System.out.println("VARNAME "+varName+" pending "+dereferences);
		for ( int i = 0; i < dereferences; i++ ){
				varName = "(*"+varName+")";
		}
		
		
		target.push(varName);
	}

	private int pendingDereferences(Target target) {
		int count = 0;
		while (  ( ! target.isEmpty() ) && ( target.top() instanceof DerefOperatorAssembler.Dereference ) ) {
			count++;
			target.pop();
		}
		return count;
	}

}
