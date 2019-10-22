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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Wrapper for io trace files
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ZipFileIoTrace implements IoTrace {
	private String methodName;
	private BufferedReader reader = null;
	private BufferedReader metaReader = null;
	private ZipEntry traceFile;
	
	private ZipEntry metaTraceFile;
	private ZipFile zipFile;
	
	
	public static class FileLineIterator implements LineIterator {
		
		private BufferedReader reader;
		private long curLine = 0;
		
		public FileLineIterator(BufferedReader reader) {
			this.reader = reader; 
		}

		public boolean hasNext() {
			try {
				return reader.ready();
			} catch (IOException e) {
				return false;
			}
		}

		public String next() {
			++curLine;
			try {
				String line = reader.readLine();
				return line;
			} catch (IOException e) {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public long getCurrentLineNumber() {
			return curLine;
		}
		
	}
	
	public ZipFileIoTrace( String methodName, ZipFile zipFile, ZipEntry trace, ZipEntry  metaTrace ){
		this.methodName = methodName;
		this.zipFile = zipFile;
		traceFile = trace;
		metaTraceFile = metaTrace;
	}
	
	public ZipFileIoTrace(String methodName, ZipFile zipFile, ZipEntry trace) {
		this(methodName,zipFile, trace,null);
	}

	public String getMethodName() {
		return methodName;
	}

	public String nextLine()  throws TraceException {

		try {
			String line = reader.readLine();
			System.out.println(line);
			return line;
		} catch (IOException e) {
			e.printStackTrace();
			throw new TraceException("Problem reading file "+traceFile+" ");
		}
	}


	public void release() throws TraceException {
		try {
			if ( reader != null )
				reader.close();
			
			if ( metaReader != null )
				metaReader.close();
			
		} catch (IOException e) {
			throw new TraceException("Cannot release "+e.getMessage());
		} finally {
			reader = null;
			metaReader = null;
		}
	}

	public LineIterator getLineIterator() throws TraceException {
		try {
			return new FileLineIterator( new BufferedReader( new InputStreamReader( zipFile.getInputStream(traceFile) ) ) );
		
		} catch (IOException e) {
			throw new TraceException(e);
		}
	}

	public MetaDataIterator getMetaDataIterator() throws TraceException {
		
		if ( metaTraceFile == null)
			throw new TraceException("Metadata file is set to null, cannot read");
		try {
			if ( metaReader == null ){
				metaReader = new BufferedReader( new InputStreamReader( zipFile.getInputStream(metaTraceFile) ) );
			}else{
				metaReader.close();
				metaReader = new BufferedReader( new InputStreamReader( zipFile.getInputStream(metaTraceFile) ) );
			}
		} catch (FileNotFoundException e) {
			throw new TraceException("Cannot read metadata file");
		} catch (IOException e) {
			throw new TraceException("Cannot read metadata file");
		}
	
		return new MetaDataIterator( metaReader );
	}

	public ZipEntry getMetaTraceEntry() {
		return metaTraceFile;
	}

	public ZipEntry getTraceEntry() {
		return traceFile;
	}
}
