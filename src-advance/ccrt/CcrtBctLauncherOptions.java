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

public class CcrtBctLauncherOptions {

	/**
	 * This class contains the File objects for the outputs created by the tools
	 * 
	 * @author Fabrizio Pastore
	 *
	 */
	public static class OutputFiles {



		
		
		public File outputFolder;

		public OutputFiles( File parentFolder ){
			outputFolder = parentFolder;
		}
		
	}
	
	
	
	//
	//	Inputs
	//
	


	///Regexp that matches the beginning of a message
	public String validTraces;
	public String faultyTraces;
	

	///Path to the folder containing the KLFA distribution
//	public File installDir;
	
	
	
	public boolean enableAva;

	
	
	///Dir that will contain all the results
	private String projectDirPath;
	public final File projectDir;

	
	
	//Patterns use to identify the actions
	public String[] actionPatterns;

	//
	//	Outputs
	//
	
	public final OutputFiles outputFiles;

	
	public boolean clean;
	public boolean cleanOnly;
	public boolean enableInference = true;
	public boolean enableChecking = true;
//	public boolean filterNonTerminatingFunctions;
	
	


	
	


	///Folder that will contain ccrt outputs
	//public File ccrtOutputsDir;

	

	/**
	 * 
	 * @param projectDirPath path to the project dir
	 * @param ccrtOutputsDir 
	 */
	public CcrtBctLauncherOptions( String projectDirPath ){
		this.projectDirPath = projectDirPath;
		this.projectDir = new File ( projectDirPath );
		this.outputFiles = new OutputFiles(projectDir);
	}
	
}
