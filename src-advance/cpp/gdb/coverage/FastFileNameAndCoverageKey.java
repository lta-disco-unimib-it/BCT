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
package cpp.gdb.coverage;

public class FastFileNameAndCoverageKey<T extends Comparable<T>> extends FileNameAndCoverageKey<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FastFileNameAndCoverageKey(String fileName, T lineNumber) {
		super(fileName, lineNumber);
//		System.err.println("FAST: "+fileName+"-"+lineNumber);
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof FastFileNameAndCoverageKey ){ 
			return this == obj;
		}
		return super.equals(obj);
	}

}
