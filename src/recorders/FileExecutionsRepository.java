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

import java.io.File;
import java.util.Set;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

public class FileExecutionsRepository implements ExecutionsRepository {
	private FileIndexAppend fileIndex;
	
	
	public FileExecutionsRepository (File storageDir){
		File indexFile = new File( storageDir, "executionTraces.idx" );
		this.fileIndex = new FileIndexAppend(indexFile, "");
	}
	
	public String newExecution(String string) throws FileIndexException {
		return fileIndex.add(string);
	}
	
	public Set<String> getExecutionsIds() throws FileIndexException {
		return fileIndex.getIds();
	}

	@Override
	public String getExecutionInfo(String sessionId) throws FileIndexException {
		return fileIndex.getNameFromId(sessionId);
	}

	@Override
	public String getExecutionId(String sessionName) throws FileIndexException {
		return fileIndex.getId(sessionName);
	}

}
