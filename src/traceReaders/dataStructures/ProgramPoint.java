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
package traceReaders.dataStructures;

import java.util.ArrayList;
import java.util.List;

import dfmaker.core.Variable;

/**
 * This class represent a Daikon program point
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public abstract class ProgramPoint {

	private String name;
	private List<Variable> variables = new ArrayList<Variable>();

	public ProgramPoint ( String name ){
		this.name = name;
	}

	public boolean addVariable(Variable e) {
		return variables.add(e);
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public String getName() {
		return name;
	}
	
	
}
