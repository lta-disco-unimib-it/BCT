/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package recorders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileExecutionsMapper {

	private File interactionLogDir;
	private final static String executionTracesRegistryFileName = "executionsTracesRegistry.etr";
	private final static String executionIdsRegistryFileName = "executionsTracesRegistry.eid";
	private File executionTracesRegistryFile;
	private File executionIdsRegistryFile;
	private List<List<File>> executionsFiles;
	
	public FileExecutionsMapper(File interactionLogDir) {
		this.interactionLogDir = interactionLogDir;
		executionTracesRegistryFile = new File ( interactionLogDir, executionTracesRegistryFileName );
		executionIdsRegistryFile = new File ( interactionLogDir, executionIdsRegistryFileName );
	}
	
	public void newExecution(String id){
		
		
		File[] files = interactionLogDir.listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name) {
				return name.endsWith(FileDataRecorder.INTERACTION_DATA_FILE_EXTENSION);
			}
			
		});
		
		
		StringBuffer openedFiles = new StringBuffer();
		
		
		for ( int i = 0; i < files.length; ++i ){
			File file = files[i];
			try {
				FileWriter w = new FileWriter(file,true);
				w.append('|');
				w.close();
				
				if ( i > 0 ){
					openedFiles.append(",");
				}
				openedFiles.append(file.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		FileWriter w;
		try {
			w = new FileWriter(executionTracesRegistryFile,true);
			w.append(openedFiles.toString());
			w.append("\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			w = new FileWriter(executionIdsRegistryFile,true);
			w.append(id);
			w.append("\n");
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns the number of recorded executions
	 * 
	 * @return
	 */
	public int getExecutionsNumber(){
		populate();
		return executionsFiles.size()+1;
	}
	
	/**
	 * Returns the files that were filled up during execution
	 * @param executionNumber
	 * @return
	 */
	public List<File> getFilesForExecution(int executionNumber){
		populate();
		return executionsFiles.get(executionNumber);
	}

	private void populate() {
		if ( executionsFiles != null ){
			return;
		}
		executionsFiles = new ArrayList<List<File>>();
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(executionTracesRegistryFile));

			String line;

			ArrayList<File> executionFiles = new ArrayList<File>();
			while ( ( line = r.readLine() ) != null ){
				for ( String fileName : line.split(",") ){
					File file = new File(interactionLogDir,fileName);
					executionFiles.add(file);
				}
			}
			executionsFiles.add(executionFiles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
