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

import java.lang.reflect.Array;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

/**
 * Implements non equals check.
 * Rember that a NonExistentVariable is always considered different from any other variable so
 * a != b returns true if at least one of the two is a NonExistentVariable.
 * 
 * @author Fabrizio Pastore [ fabrizio.pastore at gmail dot com ]
 *
 */
public class NonEqualsAssembler extends ComparisonOperatorAssembler {

	protected NonEqualsAssembler() {
		super("!=");
		// TODO Auto-generated constructor stub
	}
	
}