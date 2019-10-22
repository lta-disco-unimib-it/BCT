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
package ccrt;

import java.io.File;

public class CcrtBctLauncherValidator {
	public static void main(String[] args)   {
		String ccrtOptions;
		String ccrtLogs;
	
		String ccrtResultsFolderName = null;
		
		ccrtOptions = args[0];
		ccrtLogs = args[1];
		ccrtResultsFolderName  = args[2];
		

		System.out.println("Options loaded from file "+ccrtOptions);

		File optionsFile = new File ( ccrtOptions ); 
		File logsFile = new File ( ccrtLogs );
		
		File ccrtResultsFolder = new File( ccrtResultsFolderName );
		ccrtResultsFolder.mkdirs();
		
		File resultsFile = new File ( ccrtResultsFolder, "results.xml" );
		
		//Temporary for eventual errors during setup
		CcrtOutputFiles ccrtOutputs = new CcrtOutputFiles(logsFile, resultsFile );
		
		CcrtBctLauncherOptions options = null;
		try {
			CcrtBctLauncher.cleanWorkingDir(options, ccrtOutputs);
			options = CcrtBctLauncher.extractCcrtBctLauncherOptions(optionsFile);
			CcrtBctLauncher.writeLog(ccrtOutputs, "success", "Validation of 'options.xml'", "Validation process is finished with success");
		} catch ( Exception e ) {
			//TODO: put the exception message in log.xml
			String value = "";
			e.printStackTrace();
			StackTraceElement[] g = e.getStackTrace();
			for(int i = 0; i<g.length; i++){
				value = value + g[i].toString() + " ";
			}
			CcrtBctLauncher.writeLog(ccrtOutputs, "error", e.getMessage(), value);
		}
	   
		
	}

}
