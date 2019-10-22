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
package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class StringUtils {

	public static int occurrenciesOf(String line, char c) {
		int len = line.length();
		int count = 0;
		for ( int i = 0; i < len ; i++ ){
			if ( line.charAt(i) == c ){
				count++;
			}
		}
		
		return count;
	}

	public static String lastNonEmptyLine( String buffer ){
		BufferedReader br = new BufferedReader(new StringReader(buffer));
		
		String line;
		
		String lastNonEmpty = null;
		
		try {
			while ( ( line = br.readLine() ) != null ){
				if ( ! line.isEmpty() ){
					lastNonEmpty = line;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return lastNonEmpty;
	}
	
	public static int countOccurrences(String haystack, char needle){
		int count = 0;
		boolean escaped=false;
		for (int i=0,l=haystack.length(); i<l; i++)
		{
			if (haystack.charAt(i) == '\\'){
				if ( ! escaped )
					escaped=true;
				else
					escaped=false;
			} else {
				escaped=false;
			}

			if (haystack.charAt(i) == needle)
			{
				if( ! escaped ){
					count++;
				}
			}
		}
		return count;
	}
	
	public static boolean isNumeric(String str)  {  
		try  {  
			double d = Double.parseDouble(str);  
		}  catch(NumberFormatException nfe)  {  
			return false;  
		}  
		return true;  
	}
}
