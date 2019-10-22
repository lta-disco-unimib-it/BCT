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
package util;

import java.util.HashSet;
import java.util.Set;

public class ComparisonUtil {
	/**
	 * Returns true if two arrays have the same elements in the same order
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static boolean equalsArray(String[] a1, String[] a2) {
		if ( a1.length != a2.length ){
			return false;
		}
		
		for ( int i = 0; i < a1.length; ++i ){
			if ( ! a1[i].equals(a2[i]) ){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Returns true if two arrays contain the same elements in any order
	 * 
	 * @param actions1
	 * @param actions2
	 * @return
	 */
	public static boolean equalsArrayElements(String[] actions1, String[] actions2) {
		if ( actions1.length != actions2.length ){
			return false;
		}
		
		Set<String> actions1Set = new HashSet<String>();
		Set<String> actions2Set = new HashSet<String>();
		
		for ( String action : actions1 ){
			actions1Set.add(action);
		}
		
		for ( String action : actions2 ){
			actions2Set.add(action);
		}
		
		return actions1Set.equals(actions2Set);
	}
}
