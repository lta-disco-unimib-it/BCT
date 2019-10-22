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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Implementation of InteractionTrace interface to work on files
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class ZipFileInteractionTrace implements InteractionTrace {

	private String threadId;
	private InteractionTraceReader reader = null;
	private MetaDataIterator metaIterator;
	private InputStreamReader fr;
	private String sessionId;
	private String traceId;
	private Integer tokensCount;
	private ZipEntry dataEntry;
	private ZipEntry metaDataEntry;
	private ZipFile zipFile;
	
	public String getTraceId() {
		return traceId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public ZipFileInteractionTrace( String traceId, String sessionId, String threadId, ZipFile zipFile, ZipEntry dataEntry, ZipEntry metaDataEntry ){
		
		this.traceId = traceId;
		this.sessionId = sessionId;
		this.zipFile = zipFile;
		this.metaDataEntry = metaDataEntry;
		this.dataEntry = dataEntry;
		this.threadId = threadId;
	}
	
	public Token/*String*/ getNextToken() throws TraceException {
	
		if ( reader == null ){
			
			try {
				fr = new InputStreamReader(zipFile.getInputStream(dataEntry));
				reader = new InteractionTraceReader(fr);
			} catch (FileNotFoundException e) {
				throw new TraceException("Interaction traces file " + dataEntry + " does not exists",e);
			} catch (IOException e) {
				throw new TraceException(e);
			}
			
		}
		try {
			String value = reader.getNextToken();
			if ( value == null ){
				return null;
			}
			return new Token(value);
		} catch (IOException e) {
			throw new TraceException("problem occurred while reading file. "+e.getMessage());
		}
	}

	public String getThreadId() {
		return threadId;
	}

	public String getNextMetaData() throws TraceException {
		if ( metaDataEntry == null )
			throw new TraceException("No meta information available");
		
		if ( metaIterator == null ){
			try {
				metaIterator = new MetaDataIterator ( new BufferedReader( new InputStreamReader( zipFile.getInputStream(metaDataEntry) ) ) );
			} catch (FileNotFoundException e) {
				throw new TraceException("No meta information available");
			} catch (IOException e) {
				throw new TraceException(e);
			}
		}
		
		return (String) metaIterator.next();
	}
	
	/**
	 * Close thetrace, this is done for optimization purposes
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException{
		fr.close();
		reader = null;
	}


	
	/**
	 * Return the total number of tokens in the trace
	 * @return
	 * @throws TraceException 
	 */
	public int getTokensCount() throws TraceException {
		if ( tokensCount != null ){
			return tokensCount;
		}
		int count = 0;
		
		InputStreamReader fr = null;
		try {
			fr = new InputStreamReader(zipFile.getInputStream(dataEntry));

			char[] buffer = new char[500];
			int read;


			do {
				read = fr.read(buffer);

				for ( int i = 0 ; i < read; ++i ){
					if ( buffer[i] == '#' ) {
						count++;
					}
				}
			} while  ( read > 0 );
		} catch (IOException e) {
			throw new TraceException("Problem reading "+dataEntry,e);
		} finally {
			if ( fr != null ){
				try {
					fr.close();
				} catch (IOException e) {
				}
			}
		}
		tokensCount = count;
		return count;
	}

}
