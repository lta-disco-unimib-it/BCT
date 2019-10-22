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
package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class TestStackAnalyzer {

	private String separator;
	
	private CountingSet<String> elements = new CountingSet<String>();

	private int threshold;
	
	public TestStackAnalyzer(String separator, int threshold ) {
		this.separator = separator;
		this.threshold = threshold;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		TestStackAnalyzer tsa = new TestStackAnalyzer("-",11);
		
		File file = new File(args[0]); 
		BufferedReader br = new BufferedReader( new FileReader(file) );
		String line;
		int c=0;
		while ( ( line = br.readLine() ) != null ){
			tsa.addTestStack(line.substring(1));
			//System.out.println(++c);
		}
		br.close();
		
		HashMap<String, Integer> map = tsa.getTestOccurrenciesMap();
		for ( Entry<String, Integer> el : map.entrySet() ){
			System.out.println(el.getKey()+","+el.getValue());
		}
	}

	private HashMap<String, Integer> getTestOccurrenciesMap() {
		return elements.getOccurrenciesMap();
	}

	private void addTestStack(String testStack) {
		String[] stackElements = testStack.split(separator);
		String curElement = "";
		int c = 0;
		for( String stackElement : stackElements ){
			if ( c < threshold && stackElement != null ){
				if ( c > 0 ){
					curElement += separator;
				}
				curElement += stackElement;
				elements.add(curElement);
				c++;
			}
		}
		
		
	}

}
