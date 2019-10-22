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
package cpp.gdb.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GCovExecutor {

	private static final ArrayList<String> sourcesExtensions = new ArrayList<String>();

	static {
		sourcesExtensions.add("c");
		sourcesExtensions.add("cpp");
		sourcesExtensions.add("h");
	}
	
	
	
	public static enum CoverageType { LINE, BRANCH };

	private CoverageType coverageType;
	
	public GCovExecutor() {
		this(CoverageType.BRANCH);
	}
	
	public GCovExecutor(CoverageType coverageType) {
		this.coverageType = coverageType;
	}

	public void runGCov(String sourcesFolderPath) {
		List<File> sources = new ArrayList<File>();

		searchSourcesFile(new File(sourcesFolderPath), sources);

		for (File source : sources) {
			try {
				runGCovOnFile(source.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void searchSourcesFile(File folder, List<File> sources) {
		File[] children = folder.listFiles();
		for (int i = 0; i < children.length; i++) {
			File file = children[i];

			if (!file.isDirectory()) {
				String extension = FileUtil.getExtension(file);
				if (extension != null && sourcesExtensions.contains(extension)) {
					sources.add(file);
				}
			} else {
				searchSourcesFile(file, sources);
			}
		}
	}

	private void runGCovOnFile(String filePath) {
		

		ArrayList<String> command = new ArrayList<String>();
		command.add("gcov");
		switch ( coverageType ) {
			case BRANCH:
				command.add("-b");
				break;
		}
			
		command.add(filePath);
		
		System.err.println("Running "+command);
		
		ProcessBuilder pb = new ProcessBuilder(command);
		
		String directoryPath = FileUtil.getDirectory(new File(filePath));
		File workingDir = new File(directoryPath);
		pb.directory(workingDir);

		Process p;
		try {
			p = pb.start();
			
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = "";

			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
