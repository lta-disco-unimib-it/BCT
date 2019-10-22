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
package flattener.utilities;

import java.lang.reflect.Field;

public abstract class FieldCondition {

	private boolean accept;
	
	public FieldCondition (){
		
	}
	
	public FieldCondition( boolean accept){
		this.accept = accept;
	}
	
	/**
	 * Return if a field is acceptable:
	 * 
	 * @param field
	 * @param object
	 * @return
	 */
	public boolean applies(Field field) {
		return ( ( match(field) ) );
	}

	protected abstract boolean match(Field field);

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}
	
	
}
