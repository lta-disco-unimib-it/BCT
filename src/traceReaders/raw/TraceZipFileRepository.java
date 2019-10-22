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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import util.FileIndexAppend;
import util.ZipFileIndexAppend;
import util.FileIndex.FileIndexException;

public abstract class TraceZipFileRepository<T extends Object> implements TraceRepository<T> {
	private ZipFileIndexAppend fileIndex;
	private ZipFileIndexAppend metaIndex;

	private String storageDirName;
	private File archiveFile;
	private ZipFile zipFile;
	
	private static final String traceIndexFileName = "traces.idx";
	private static final String metaIndexFileName = "meta.idx";
	
	public TraceZipFileRepository( File archive, String storageDirName ) throws FileReaderException{
		this.archiveFile = archive;
		this.storageDirName = storageDirName;
		try {
			zipFile = new ZipFile(archiveFile);
			String traceIndexPath = storageDirName+"/"+traceIndexFileName;
			String metaIndexPath = storageDirName+"/"+metaIndexFileName;
			
			ZipEntry traceIndexEntry = zipFile.getEntry(traceIndexPath);
			ZipEntry metaIndexEntry = zipFile.getEntry(metaIndexPath);
			
//			traceIndexFile = new File( storageDir, traceIndexFileName );
//			metaIndexFile = new File( storageDir, metaIndexFileName );	
//			Enumeration<? extends ZipEntry> en = zipFile.entries();
//			while ( en.hasMoreElements() ){
//				System.out.println(en.nextElement());
//			}
//			System.out.println(storageDirName+"/"+traceIndexFileName+"   "+traceIndexEntry);
			
			if ( traceIndexEntry == null ){
				throw new FileReaderException("Archive "+archive.getAbsolutePath()+" does not contain resource "+traceIndexPath+" (traces index).");
			}
			
			if ( metaIndexEntry == null ){
				throw new FileReaderException("Archive "+archive.getAbsolutePath()+" does not contain resource "+metaIndexPath+" (metadata traces index).");
			}
			
			fileIndex = new ZipFileIndexAppend( zipFile, traceIndexEntry, ".dtrace" );
			metaIndex = new ZipFileIndexAppend( zipFile, metaIndexEntry, ".meta" );
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see traceReaders.raw.TraceRepository#getRawTrace(java.lang.String)
	 */
	public T getRawTrace( String methodName ) throws FileReaderException{
		
		
		String file;
		String meta;
		
		if ( ! fileIndex.containsName(methodName) )
			file = fileIndex.add(methodName);
		
		if ( ! metaIndex.containsName(methodName) )
			meta = metaIndex.add(methodName);
		
		try {
			file = fileIndex.getId(methodName);
			meta = metaIndex.getId(methodName);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileReaderException("Cannot get file for "+methodName);
		}
		
		return getTraceFile( file, methodName,  zipFile, getZipEntry ( file ) , getZipEntry ( meta ));
	}
	

	/* (non-Javadoc)
	 * @see traceReaders.raw.TraceRepository#getRawTraceFromId(java.lang.String)
	 */
	public T getRawTraceFromId(String id) throws FileReaderException {
		String methodName;
		try {
			methodName = fileIndex.getNameFromId(id);
		} catch (FileIndexException e) {
			throw new FileReaderException("Unconsistent index file: "+e.getMessage());
		}
		
		if ( metaIndex.containsName(methodName)){
			try {
				return  getTraceFile(id,methodName, zipFile, getZipEntry ( id ) , getZipEntry ( metaIndex.getId(methodName)) );
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		} else {
			return getTraceFile( id, methodName, zipFile, getZipEntry ( id ) );
		}
	}
	
	
	/* (non-Javadoc)
	 * @see traceReaders.raw.TraceRepository#getRawTraces()
	 */
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
					traces.add(  getTraceFile(id,methodName, zipFile, getZipEntry ( id ) , getZipEntry ( metaIndex.getId(methodName)) ) );
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				traces.add( getTraceFile( id, methodName, zipFile, getZipEntry ( id ) ) );
			}
					
		}
		
		
		
		return traces;
	}

	private ZipEntry getZipEntry(String id) {
		return zipFile.getEntry(storageDirName+"/"+id);
		
	}
	


	protected abstract T getTraceFile(String id, String elementName, ZipFile zipFile, ZipEntry dataEntry, ZipEntry metaDataEntry);
	
	protected abstract T getTraceFile(String id, String elementName, ZipFile zipFile, ZipEntry dataEntry );
	
}
