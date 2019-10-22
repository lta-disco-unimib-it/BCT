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
package traceReaders.raw;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

public abstract class TraceFileRepository<T extends Object> implements TraceRepository<T>{
	private FileIndexAppend fileIndex;
	private FileIndexAppend metaIndex;
	private File traceIndexFile; 
	private File metaIndexFile;
	private File storageDir;
	
	private static final String traceIndexFileName = "traces.idx";
	private static final String metaIndexFileName = "meta.idx";
	
	public TraceFileRepository( File storageDir ){
		this.storageDir = storageDir;
		traceIndexFile = new File( storageDir, traceIndexFileName );
		metaIndexFile = new File( storageDir, metaIndexFileName );	
		fileIndex = new FileIndexAppend( traceIndexFile, ".dtrace" );
		metaIndex = new FileIndexAppend( metaIndexFile, ".meta" );
	}
	
	public boolean containsTrace( String methodName ) throws FileReaderException{
		return fileIndex.containsName(methodName);
	}
	
	public T getRawTrace( String methodName ) throws FileReaderException{
		
		String file;
		String meta;
		
		if ( ! fileIndex.containsName(methodName) ){
			try {
				file = fileIndex.add(methodName);
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		}
		
		if ( ! metaIndex.containsName(methodName) ){
			try {
				meta = metaIndex.add(methodName);
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		}
		
		try {
			file = fileIndex.getId(methodName);
			meta = metaIndex.getId(methodName);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileReaderException("Cannot get file for "+methodName);
		}
		
		return getTraceFile( file, methodName,  getFile ( file ) , getFile ( meta ) );
	}
	

	public T getRawTraceFromId(String id) throws FileReaderException {
		String methodName;
		try {
			methodName = fileIndex.getNameFromId(id);
		} catch (FileIndexException e) {
			throw new FileReaderException("Unconsistent index file: "+e.getMessage());
		}
		
		if ( metaIndex.containsName(methodName)){
			try {
				return  getTraceFile(id,methodName, getFile ( id ) , getFile ( metaIndex.getId(methodName)) );
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		} else {
			return getTraceFile( id, methodName, getFile ( id ) );
		}
	}
	
	
	public List<T> getRawTraces() throws FileReaderException {
		Set<String> ids = fileIndex.getIds();
		ArrayList<T> traces = new ArrayList<T>();

		
		for ( String id :  ids ){
			
			String methodName;
			try {
				methodName = fileIndex.getNameFromId(id);
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				throw new FileReaderException("Unconsistent index file");
			}
			if ( metaIndex.containsName(methodName)){
				try {
					traces.add(  getTraceFile(id,methodName, getFile ( id ) , getFile ( metaIndex.getId(methodName)) ) );
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				traces.add( getTraceFile( id, methodName, getFile ( id ) ) );
			}
					
		}
		
		
		
		return traces;
	}

	private File getFile(String id) {
		return new File( storageDir, id );
	}
	
	public void setGZipped() {
		fileIndex.setGZipped();
	}

	protected abstract T getTraceFile(String id, String elementName, File dataId, File metaId);
	
	protected abstract T getTraceFile(String id, String elementName, File dataId );

	
	
}
