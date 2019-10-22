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
package cpp.gdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import util.StringUtils;

public class SourceFileUtil {
	
	public static final String VARIABLES_DELIMITERS = "\t ,;+-=/!~|{}():<>?\"[]&"; //no .

	public static final String[] reservedCKeywords = new String[]{ "auto","else","long","switch", "break","enum","register","typedef", 
			"case","extern","return","union", "char","float","short","unsigned", "const","for","signed","void", "continue",
			"goto","sizeof","volatile", "default","if","static","while", "do","int","struct","_Packed", "double" };
	
	public static final Set<String> reservedCKeywordsSet;
	
	static {
		HashSet<String> _reservedCKeywordsSet = new HashSet<String>();
		_reservedCKeywordsSet.addAll(Arrays.asList(reservedCKeywords));
		reservedCKeywordsSet = Collections.unmodifiableSet(_reservedCKeywordsSet);
	}
	
	public static Set<String> extractVariablesFromLine( String line ){

		Set<String> result = new HashSet<String>();
		
		String lineNoArrows = line.replace("->", ".");
		
		
		StringTokenizer tkn = new StringTokenizer(lineNoArrows, VARIABLES_DELIMITERS); 
		while ( tkn.hasMoreTokens() ){
			String token = tkn.nextToken();

			if ( StringUtils.isNumeric(token) ){
				continue;
			}
			
			result.add(token);
		}

		result.removeAll(reservedCKeywordsSet);
		
		return result;
		
	}
	
	
	
	
	public static Set<String> extractVariablesDefinedInLine( String line ){

		
		
		int equalsPos = line.indexOf('=');
		if ( equalsPos == -1 ){
			return Collections.EMPTY_SET;
		}
		
		String before = line.substring(0,equalsPos);
		String after = line.substring(equalsPos+1);
		
		if ( after.startsWith("=") ){
			return Collections.EMPTY_SET;
		}
		
		String[] definedElements = before.split("\\s");
		
		Set<String> result = new HashSet<String>();
		
		String definedVar = definedElements[definedElements.length-1].replace("*", "");
		
		result.add(definedVar);
		

		return result;
		
	}
	
	
}
