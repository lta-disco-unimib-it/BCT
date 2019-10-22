package traceReaders.raw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementation of InteractionTrace interface to work on files
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileInteractionTrace implements InteractionTrace {
	private File file;
	private File metaTraceFile;
	private String threadId;
//	private InteractionTraceReader reader = null;
//	private MetaDataIterator metaIterator;
	private FileReader fr;
	private String sessionId;
	private String traceId;
	private Integer tokensCount;
	
	public String getTraceId() {
		return traceId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public FileInteractionTrace( String traceId, String sessionId, String threadId, File file, File metaFile ){
		this.file = file;
		this.traceId = traceId;
		this.sessionId = sessionId;
		this.metaTraceFile = metaFile;
//		if ( ! file.exists() )
//			throw new IllegalArgumentException("Interaction traces file "+file+" does not exists");
		this.threadId = threadId;
	}
	
	public Token/*String*/ getNextToken() throws TraceException {
		throw new RuntimeException("NOt implemented");
//		if ( reader == null ){
//			
//			try {
//				fr = new FileReader(file);
//				reader = new InteractionTraceReader(fr);
//			} catch (FileNotFoundException e) {
//				throw new TraceException("Interaction traces file " + file + " does not exists");
//			}
//			
//		}
//		try {
//			String value = reader.getNextToken();
//			if ( value == null ){
//				return null;
//			}
//			return new Token(value);
//		} catch (IOException e) {
//			throw new TraceException("problem occurred while reading file. "+e.getMessage());
//		}
	}

	public String getThreadId() {
		return threadId;
	}

	public String getNextMetaData() throws TraceException {
		throw new RuntimeException("NOt implemented");
//		if ( metaTraceFile == null )
//			throw new TraceException("No meta information available");
//		
//		if ( metaIterator == null ){
//			try {
//				metaIterator = new MetaDataIterator ( new BufferedReader( new InputStreamReader( new FileInputStream (metaTraceFile) ) ) );
//			} catch (FileNotFoundException e) {
//				throw new TraceException("No meta information available");
//			}
//		}
//		
//		if ( metaIterator.hasNext() ){
//			return (String) metaIterator.next();
//		} else {
//			return null;
//		}
	}
	
	/**
	 * Close thetrace, this is done for optimization purposes
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException{
		throw new RuntimeException("NOt implemented");
//		fr.close();
//		reader = null;
	}

	public File getFile() {
		return this.file;
	}

	public File getMetaTraceFile() {
		return metaTraceFile;
	}
	
	/**
	 * Return the total number of tokens in the trace
	 * @return
	 * @throws TraceException 
	 */
	public int getTokensCount() throws TraceException {
		throw new RuntimeException("NOt implemented");
//		if ( tokensCount != null ){
//			return tokensCount;
//		}
//		int count = 0;
//		File traceFile = file;
//		FileReader fr = null;
//		try {
//			fr = new FileReader(traceFile);
//
//			char[] buffer = new char[500];
//			int read;
//
//
//			do {
//				read = fr.read(buffer);
//
//				for ( int i = 0 ; i < read; ++i ){
//					if ( buffer[i] == '#' ) {
//						count++;
//					}
//				}
//			} while  ( read > 0 );
//		} catch (IOException e) {
//			throw new TraceException("Problem reading "+file.getAbsolutePath(),e);
//		} finally {
//			if ( fr != null ){
//				try {
//					fr.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//		tokensCount = count;
//		return count;
	}
}
