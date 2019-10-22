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
package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

public class InvariantsFilter {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File  dir = new File(args[0]);
		File  dirDest = new File(args[1]);
		
		String[] files = dir.list(new FilenameFilter(){

			public boolean accept(File dir, String name) {
				return ( name.endsWith(".enter")
						|| name.endsWith(".exit") );
			}
			
		});
		
		for ( String name : files ){
			File fileOrig = new File(dir,name);
			File fileDest = new File(dirDest,name);
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(fileOrig));
				String line = null;
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileDest));
				
				while ( ( line = reader.readLine() ) != null ){
					if ( 	line.length() == 0 || 
							count(line,'.') > 4 ||
							line.contains("one of") ||
							line.contains(".tk_")
							){
						continue;
					}
					
					bw.write(line);
					bw.write("\n");
				}
				bw.close();
				reader.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Returns the number of char in a line
	 * 
	 * @param line
	 * @return
	 */
	private static int count(String line, char element ) {
		char[] chars = line.toCharArray();
		int count = 0;
		for ( char aChar : chars ){
			if ( aChar == element ){
				++count;
			}
		}
		return count;
	}

}
