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

public class GccCompileAndFix {

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
					if ( line.contains("error:") ){
						
						int fileNameStart = 0;
						int fileNameEnd = line.indexOf(":");

						if (fileNameEnd < 0 ){
							System.out.println("No filename: "+line);
							continue;
						}

						String fileString = line.substring(fileNameStart, fileNameEnd);

						line = line.substring(fileNameEnd+1);

						int lineEnd = line.indexOf(":");

						if ( lineEnd < 0 ){
							System.out.println("No line: "+line);
							continue;
						}

						String lineString = line.substring(0, lineEnd).trim();

						try {
							int lineNo = Integer.valueOf(lineString);
							canRepair = true;
							repair( fileString,lineNo );
						} catch ( NumberFormatException e ){
							continue;
						}

					}
				}
				
				if ( ! canRepair ){
					break;
				}
			} else {
				break;
			}
		}


	}

	private static void repair(String fileString, int lineNo) throws NotFixable, IOException {
		try {
			File file = new File( fileString );
			System.out.println("Fixing "+file.getAbsolutePath() +" : "+ lineNo+ " " + file.exists());
			List<String> lines = FileUtil.getLines( file );
			int pos = lineNo-1;

			String line = lines.get(pos);

			
			if ( line.trim().startsWith("assert( ") ){
				line = "assert( 0 ); //BCT-FIXED, WAS : "+line;
				lines.set(pos, line);
				FileUtil.writeToTextFile(lines, file);
			} else {
				System.err.println("Cannot repair : "+line);
				throw new NotFixable(fileString,lineNo);
			}
			

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
