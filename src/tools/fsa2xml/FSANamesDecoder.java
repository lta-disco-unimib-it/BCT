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
package tools.fsa2xml;

import java.util.Map;

public class FSANamesDecoder {

	public static String getReplacedLabel(String description, Map<String,String> namesMapping) {
System.out.println(namesMapping);
		if ( namesMapping != null ){
			System.out.println(description);
			for ( String key : namesMapping.keySet() ){
				if ( description.contains(key) ){
					return description.replace(key,namesMapping.get(key));
					
				}
			}
		}
		return description;
	}
}
