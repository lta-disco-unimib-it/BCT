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
package tools.gdbTraceParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import util.ProcessRunner;

import cpp.gdb.EnvUtil;
import cpp.gdb.GdbRegressionConfigCreator;

public class GdbInfoExtractor {
	private Logger LOGGER = Logger.getLogger(GdbInfoExtractor.class.getCanonicalName());
	private File gdbExecutable;
	private File executableToAnalyze;
	private static int increment = 0;

	public GdbInfoExtractor(File gdbExecutable) {
		this.setGdbExecutable(gdbExecutable);
	}

	public File getExecutableToAnalyze() {
		return executableToAnalyze;
	}

	public void setExecutableToAnalyze(File executableToAnalyze) {
		this.executableToAnalyze = executableToAnalyze;
	}

	public File getGdbExecutable() {
		return gdbExecutable;
	}

	public void setGdbExecutable(File gdbExecutable) {
		this.gdbExecutable = gdbExecutable;
	}
	
	private StringBuffer executeGdbAndRetrieveOutput(File scriptFile) {

		ArrayList<String> command = new ArrayList<String>();
		command.add(gdbExecutable.getAbsolutePath());
		command.add("-x");
		command.add( scriptFile.getAbsolutePath() );
		
		command.add(executableToAnalyze.getAbsolutePath());
		
		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer errorBuffer = new StringBuffer();
		
		try {
			ProcessRunner.run(command, outputBuffer, errorBuffer, 5);
			
			//System.out.println(outputBuffer);
			
			return outputBuffer;

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


	
	public String runGdbAndGetOutput(String commandToExecute) {
		LOGGER.info("Executing GDB with "+commandToExecute);
		File scriptFile;
		try {
			scriptFile = generateGdbScript(commandToExecute);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		
		StringBuffer output = executeGdbAndRetrieveOutput(scriptFile);
		
		return output.toString();
		
		
		
	}
	
	private File generateGdbScript( String commandToExecute ) throws IOException {
		File tmpFolder = new File ( EnvUtil.getTmpFolderPath() );
		if ( ! tmpFolder.exists() ){
			LOGGER.warning("tmpFolder does not exist, trying to create...");
			boolean created = tmpFolder.mkdirs();
			LOGGER.info("Creating tmpFolder, success? "+created);
		}
		File commandFile = new File(tmpFolder, "bct.gdb.conf.tmp."+EnvUtil.getPID()+"."+increment() );
		BufferedWriter w = new BufferedWriter(new FileWriter(commandFile) );
		try {
			
//
//			File file = new File("/tmp/bct.gdb.out.tmp");
//			file.delete();
			GdbRegressionConfigCreator.writeGdbScriptHeader(w, null );
			w.write(commandToExecute);
			w.newLine();
			w.write("quit");
			w.newLine();
			
			return commandFile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
				w.close();
			
		}
		return null;
		
	}

	private int increment() {
		return increment++;
	}
}