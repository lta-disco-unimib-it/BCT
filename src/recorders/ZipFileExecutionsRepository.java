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
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import traceReaders.raw.TraceFileRepository;
import util.FileIndex.FileIndexException;
import util.FileIndexAppend;
import util.ZipFileIndexAppend;

public class ZipFileExecutionsRepository implements ExecutionsRepository {
	private ZipFileIndexAppend fileIndex;
	
	private ZipFile zipFile;
	
	public ZipFileExecutionsRepository (File archive, String storageDirName) throws ZipException, IOException{
		this.zipFile = new ZipFile(archive);
		ZipEntry repositoryEntry = zipFile.getEntry(storageDirName+"/executionTraces.idx");
		
		this.fileIndex = new ZipFileIndexAppend(zipFile,repositoryEntry, "");
	}
	
	/* (non-Javadoc)
	 * @see recorders.ExecutionsRepository#newExecution(java.lang.String)
	 */
	public String newExecution(String string){
		return fileIndex.add(string);
	}
	
	/* (non-Javadoc)
	 * @see recorders.ExecutionsRepository#getExecutionsIds()
	 */
	public Set<String> getExecutionsIds(){
		return fileIndex.getIds();
	}

	@Override
	public String getExecutionInfo(String sessionId) throws FileIndexException {
		// TODO Auto-generated method stub
		return fileIndex.getNameFromId(sessionId);
	}

	@Override
	public String getExecutionId(String sessionName) throws FileIndexException {
		// TODO Auto-generated method stub
		return fileIndex.getId(sessionName);
	}

}
