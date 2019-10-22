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
package framac.pdg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class FramaCExecutor {

	private String framaCCommand;
	private List<String> options;

	public FramaCExecutor(String framaCCommand, List<String> options) {
		this.framaCCommand = framaCCommand;
		this.options = options;
	}
	
	public void runFramaC(String sourceFolderPath, String outputFileName, String sources) {		
		try {
			ProcessBuilder pb = new ProcessBuilder(framaCCommand);
			for (String option : options) {
				pb.command().add(option);
			}

			String directoryPath = new File(sourceFolderPath).getCanonicalPath();
			File workingDir = new File(directoryPath);
			pb.directory(workingDir);

			System.out.println("Running frama-c");
			Process p;
			p = pb.start();
			
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
