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
package util.cbe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.ExtendedDataElement;

public class CBEUtil {
	public static Map<String, Object> getExtendedMap(CommonBaseEvent commonBaseEvent) {
		List<ExtendedDataElement> elements = commonBaseEvent.getExtendedDataElements();
		Map<String,Object> map = new HashMap<String, Object>();
		
		for ( ExtendedDataElement element : elements ){
			Object value = null;
			try {
				if(element.getValues().size() == 0 ){
					if (element.getType().equals("stringArray") ){
						value = new String[0];
					} else if ( element.getType().equals("string") ) {
						value = "";
					}	
				} else {

					if (element.getType().equals("stringArray") ){
						value = element.getValuesAsStringArray();
					} else if ( element.getType().equals("string") ) {
						value = element.getValuesAsString();
					}
				}
			} catch ( IllegalStateException e ){
				throw new IllegalStateException("Problem reading value for element "+element.getName(), e);
			}
			
			map.put(element.getName(), value );
		}
		
		return map;
	}
}
