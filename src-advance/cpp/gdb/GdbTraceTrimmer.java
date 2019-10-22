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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GdbTraceTrimmer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File traceToCorrect = new File( args[0] );
		File correctTrace = new File( args[0]+".rewritten" );
		
		
		BufferedReader reader = new BufferedReader( new FileReader ( traceToCorrect ) );
		BufferedWriter writer = new BufferedWriter( new FileWriter( correctTrace ) );
		
		String line;
		
		while( ( line = reader.readLine() ) != null ){
			if ( line.startsWith("!!!BCT") ){
				line = line.trim();
			}
			writer.write(line);
			writer.newLine();
		}
		
		writer.close();
		reader.close();
	}

}
