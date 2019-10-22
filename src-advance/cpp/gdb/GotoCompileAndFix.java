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
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import util.FileUtil;
import util.ProcessRunner;

public class GotoCompileAndFix {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NotFixable 
	 */
	public static void main(String[] args) throws IOException, NotFixable {


		List<String> command = Arrays.asList(args);

		while ( true ){
			boolean canRepair = false;
			
			Appendable outputBuffer = new StringBuffer();

			int exitCode = ProcessRunner.run(command, outputBuffer, outputBuffer, 0);

			BufferedReader r = new BufferedReader(new StringReader(outputBuffer.toString()));

			System.out.println(outputBuffer);

			if ( exitCode != 0 ){
				String line;
				while ( ( line = r.readLine() ) != null ){
					if ( line.contains("file") && line.contains("line") ){
						int fileNameStart = line.indexOf("file") + 5;
						int fileNameEnd = line.indexOf(" line");
						int lineStart = fileNameEnd+6;
						int lineEnd = line.indexOf("function");

						String fileString = line.substring(fileNameStart, fileNameEnd);
						String lineString = line.substring(lineStart, lineEnd).trim();

						int lineNo = Integer.valueOf(lineString);

						canRepair = true;
						repair( fileString,lineNo, line );
					}
				}
			} else {
				break;
			}
			
			if ( ! canRepair ){
				break;
			}
		}


	}

	private static void repair(String fileString, int lineNo, String error) throws NotFixable, IOException {
		try {
			File file = new File( fileString );
			System.out.println(file.getAbsolutePath() + " " + file.exists());
			List<String> lines = FileUtil.getLines( file );
			int pos = lineNo-1;

			String line = lines.get(pos);

			if ( line.trim().startsWith("assert( ") ){
				line = "assert( 0 ); //BCT-FIXED, WAS : "+line;
				lines.set(pos, line);
			} else {
				//file src/store_test.c line 234 function main: wrong number of function arguments: expected 2, but got 0
				if ( error.contains("wrong number of function arguments: expected") ){
					int expectedStart = error.indexOf("expected")+9;
					String remain = error.substring(expectedStart);
					int end = remain.indexOf(',');
					try {
						Integer argsN = Integer.valueOf(remain.substring(0,end));
						int argsStart = line.indexOf('(');
						String newLine = line.substring(0,argsStart);
						newLine += "(";
						for( int i = 0; i < argsN; i++ ){
							if ( i > 0 ){
								newLine += ", ";
							}
							newLine += "cbmc_rand_int()";
						}
						newLine += "); //BCT-FIXED AA was : "+line;
						lines.set(pos, newLine);
						
						
					} catch (NumberFormatException e) {
						System.err.println(line);
						throw new NotFixable(fileString,lineNo);
					}
				} else {
					System.err.println(line);
					throw new NotFixable(fileString,lineNo);
				}
			}

			FileUtil.writeToTextFile(lines, file);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
