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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import util.FileUtil;
import util.ProcessRunner;
import util.QuotedStringTokenizer;

public class ObjDumpSymbolsLoader {
	
	

	public List<String> getGlobalVariables(File executable) throws IOException{
		
		ObjDumper dumper = new ObjDumper();
		ArrayList<String> command = new ArrayList<String>();
		command.add(dumper.getObjDumpCommand());
		command.add("--syms");
		command.add(executable.getAbsolutePath());
		
		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer errorBuffer = new StringBuffer();
		
		ProcessRunner.run(command, outputBuffer, errorBuffer, 0);
		
		
		System.out.println(outputBuffer);
		BufferedReader r = new BufferedReader(new StringReader( outputBuffer.toString() ));
		String line;
		List<String> vars = new ArrayList<String>();
		
		try {
			while ( ( line = r.readLine() ) != null ){
				String varName = getGlobalVariable( line );
				if (varName != null ){
					vars.add(varName);
				}
			}
		} finally {
			r.close();
		}
		return vars;
	}

	private String getGlobalVariable(String line) {
		
		String[] columns = line.split("\\s+");
		
		if ( columns.length < 6 ){
			return null;
		}
		
		if ( ".text".equals(columns[3]) ){
//			if ( columns[5].contains("[") ){
//				return null; //for arrays
//			}
			return columns[5];
		}
		return null;
	}
	
	public static void main(String args[]) throws IOException{
		File file = new File(args[0]);
		ObjDumpSymbolsLoader p = new ObjDumpSymbolsLoader();
		
		List<String> vars = p.getGlobalVariables(file);
		
		for ( String var : vars ){
			System.out.println(var);
		}
	}
}
