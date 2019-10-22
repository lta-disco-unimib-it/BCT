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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import util.FileUtil;
import util.ProcessRunner;

/**
 * This class generates a dump of an ELF executable with info useful for debugging with bct
 * 
 * @author fabrizio
 *
 */
public class ObjDumper {

	private String objDumpCommand = "objdump";
	
	public String getObjDumpCommand() {
		return objDumpCommand;
	}

	public void setObjDumpCommand(String objDumpCommand) {
		this.objDumpCommand = objDumpCommand;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjDumper dumper = new ObjDumper();
		
		File executable = new File( args[0] ); 
		File dumpOutput = new File(args[1]);
		
		try {
			dumper.dump( executable, dumpOutput );
			
			System.out.println("Objdump succesfully generated: "+dumpOutput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void dumpSymbols(File executable, File dumpOutput) throws IOException {
		
		ArrayList<String> command = new ArrayList<String>();
		command.add(objDumpCommand);
		command.add("-t");
		command.add(executable.getAbsolutePath());
		
		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer errorBuffer = new StringBuffer();
		
		ProcessRunner.run(command, outputBuffer, errorBuffer, 0);
		
		FileUtil.writeToTextFile( outputBuffer.toString(), dumpOutput );
		
		
		
	}
	
	public void dump(File executable, File dumpOutput) throws IOException {
		
		if ( dumpOutput.lastModified() > executable.lastModified() ){
			System.out.println("Objdump already up to date. SKIPPING objdump creation.");
			return;
		}
		
		ArrayList<String> command = new ArrayList<String>();
		command.add(objDumpCommand);
		command.add("-l");
		command.add("-r");
		command.add("-g");
		command.add("-d");
		command.add(executable.getAbsolutePath());
		
//		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer errorBuffer = new StringBuffer();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(dumpOutput));
		ProcessRunner.run(command, bw, errorBuffer, 0);
		bw.close();
		
//		FileUtil.writeToTextFile( outputBuffer.toString(), dumpOutput );
		
		
		
	}

}
