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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import recorders.ExecutionsRepository;
import recorders.FileDataRecorder;
import recorders.FileExecutionsRepository;
import recorders.ZipFileExecutionsRepository;
import traceReaders.TraceReaderException;
import util.FileIndex.FileIndexException;
import conf.BctSettingsException;
import conf.InvariantGeneratorSettings;

/**
 * Trace reader that works on file
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileTracesReader implements TracesReader {
	private TraceRepository<IoTrace> ioRepository;
	private ExecutionsRepository executionsRepository;
	private TraceRepository<InteractionTrace> interactionRepository;
	private TraceRepository<InteractionTrace> ioInteractionMappingRepository;
	private Boolean readZipped;
	
	public Boolean getReadZipped() {
		return readZipped;
	}

	public interface Options {
		public final String tracesPath = "traceReader.tracesPath";
		public final String ioTracesDirName = "traceReader.ioTracesDirName";
		public final String interactionTracesDirName = "traceReader.interactionTracesDirName";
		public final String readZippedEntries = "traceReader.readZippedEntries";
	}
	
	/**
	 * Create an instance of a trace reader
	 * 
	 * @param ioTracesDir	directory containing IO traces and  file traces.idx
	 * @param interactionTracesDir directory containing Interaction traces and file traces.idx
	 * @throws BctSettingsException 
	 */
	public FileTracesReader( File tracesDir, String ioDirName, String interactionDirName, boolean readZipped ) throws BctSettingsException{
		init(tracesDir, ioDirName, interactionDirName, FileDataRecorder.DEFAULT_IO_INTERACTION_MAPPING_DIR, readZipped);
	}
	
	
	public FileTracesReader( File tracesDir, String ioDirName, String interactionDirName, String ioInteractionMappingsDirName, boolean readZipped ) throws BctSettingsException{
		init(tracesDir, ioDirName, interactionDirName, ioInteractionMappingsDirName, readZipped);
	}
	
//	public FileTracesReader( File ioTracesDir, File interactionTracesDir) throws BctSettingsException{
//		init(ioTracesDir,interactionTracesDir);
//	}
	
	public FileTracesReader() {
		// TODO Auto-generated constructor stub
	}

	private void initNotZipped(File ioTracesDir,File interactionTracesDir, File ioInteractionMappingsDir) throws BctSettingsException{
		if ( ! ioTracesDir.exists() || ! ioTracesDir.isDirectory() )
			throw new BctSettingsException("directory does not exists "+ioTracesDir);
	
		if ( ! interactionTracesDir.exists() || ! interactionTracesDir.isDirectory() )
			throw new BctSettingsException("directory does not exists "+interactionTracesDir);
		
		interactionRepository = new FileInteractionTraceRepository(interactionTracesDir);
		
		ioRepository = new FileIoTraceRepository(ioTracesDir);
		
		executionsRepository = new FileExecutionsRepository(interactionTracesDir);
		
		if ( interactionTracesDir.exists() ){
			ioInteractionMappingRepository  = new FileInteractionTraceRepository(ioInteractionMappingsDir);
		}
	}
	

	public Iterator<IoTrace> getIoTraces() throws FileReaderException {
//		File[] srcFiles = ioTracesDir.listFiles(dtraceFilter);
//		ArrayList<IoTrace> al = new ArrayList<IoTrace>(srcFiles.length);
//		
//		for (int i = 0; i < srcFiles.length; i++) {
//			al.add(getIoTrace( srcFiles[i] ));
//		}
		List<IoTrace> traces = ioRepository.getRawTraces();
//		List<IoTrace> al = new ArrayList<IoTrace>(traces.size());
//		al.addAll(traces);
		return traces.iterator();
	}

//	public FileIoTrace getIoTrace(String methodName, String fileName) {
//		File traceFile = new File(ioTracesDir,fileName+".dtrace");
//		return getIoTrace(methodName, traceFile);
//	}
//	/**
//	 * Get the IoTrace object from the passed trace 
//	 * @param file
//	 * @return
//	 */
//	public FileIoTrace getIoTrace(String methodName, File file) {
//		String fileName = file.getName();
//		
//		String methName = fileName.substring(0,fileName.length()-7);
//		
//		File metaFile = new File( ioTracesDir, methName+".meta");
//		if ( ! metaFile.exists() )
//			metaFile = null;
//		return new FileIoTrace(methodName,file,metaFile);
//	
//	}

	public void init(InvariantGeneratorSettings trs) throws BctSettingsException {
		String dir = trs.getProperty(Options.tracesPath);
		if ( dir == null ){
			throw new BctSettingsException("parameter "+Options.tracesPath+" not properly set");
		}
		
		String ioDir = trs.getProperty(Options.ioTracesDirName);
		if ( dir == null ){
			throw new BctSettingsException("parameter "+Options.ioTracesDirName+" not properly set");
		}
		
		String intDir = trs.getProperty(Options.interactionTracesDirName);
		if ( dir == null ){
			throw new BctSettingsException("parameter "+Options.interactionTracesDirName+" not properly set");
		}
		
		String readZippedString = trs.getProperty(Options.readZippedEntries);
		boolean readZipped;
		if ( readZippedString != null ){
			readZipped = Boolean.valueOf(readZippedString);
		} else {
			readZipped = true;
		}
		File tracesDir = new File ( dir );
		
		
		init(tracesDir,ioDir,intDir,FileDataRecorder.DEFAULT_IO_INTERACTION_MAPPING_DIR, readZipped);
	}
		
	private void init( File tracesDir, String ioDir, String intDir, String ioIntMappingsDir, boolean readZipped ) throws BctSettingsException{
		
		File tracesZipped = new File ( tracesDir.getParentFile(), tracesDir.getName()+".zip" );
		
		if ( ( ! tracesDir.exists() ) && readZipped && tracesZipped.exists()){
			initZipped(tracesZipped,tracesDir.getName()+"/"+ioDir,tracesDir.getName()+"/"+intDir,tracesDir.getName()+"/"+ioIntMappingsDir);
		} else {
			initNotZipped(new File(tracesDir,ioDir),new File(tracesDir,intDir),new File(tracesDir,ioIntMappingsDir));
		}
		
	}

	
	private void initZipped(File tracesZipped, String ioDir, String intDir, String ioInteractionMappingDir) throws BctSettingsException {
		
		try {
			interactionRepository = new ZipFileInteractionTraceRepository(tracesZipped,intDir);
		
			ioRepository = new ZipFileIoTraceRepository(tracesZipped,ioDir);
		
			executionsRepository = new ZipFileExecutionsRepository(tracesZipped,intDir);
			
			
		} catch (ZipException e) {
			throw new BctSettingsException(e);
		} catch (IOException e) {
			throw new BctSettingsException(e);
		} catch (FileReaderException e) {
			throw new BctSettingsException(e);
		}
		
		
		
		try {
			ioInteractionMappingRepository = new ZipFileInteractionTraceRepository(tracesZipped,ioInteractionMappingDir);
		} catch (FileReaderException e) {
			e.printStackTrace();
		}
		
	}



	/**
	 * Return all the interaction traces recorded
	 * 
	 */
	public Iterator<InteractionTrace> getInteractionTraces() {
		List<InteractionTrace> traces;
		try {
			traces = interactionRepository.getRawTraces();
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
//		List<InteractionTrace> result = new ArrayList<InteractionTrace>(traces.size());
//		result.addAll(traces);
		return traces.iterator();
	}
	
	
	public IoTrace getIoTrace(String methodName) throws TraceReaderException {
		return ioRepository.getRawTrace(methodName);		
	}

	public Iterator<InteractionTrace> getInteractionTracesForSession(
			String sessionId) {
		List<InteractionTrace> result = new ArrayList<InteractionTrace>();
		try {
			
			List<InteractionTrace> traces = interactionRepository.getRawTraces();
			for ( InteractionTrace trace : traces ){
				if ( trace.getSessionId().equals(sessionId) ){
					result.add(trace);
				}
			}
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.iterator();
	}

	public Set<String> getRecordingSessionsIds() throws FileReaderException {
		try {
			return executionsRepository.getExecutionsIds();
		} catch (FileIndexException e) {
			throw new FileReaderException(e);
		}
	}
	
	@Override
	public String getSessionIdFromName( String sessionName ) throws FileReaderException{
		try {
			return executionsRepository.getExecutionId(sessionName);
		} catch (FileIndexException e) {
			throw new FileReaderException(e);
		}
	}
	
	public String getSessionName( String sessionId ){
		try {
			return executionsRepository.getExecutionInfo(sessionId);
		} catch (FileIndexException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getMethodCallId( String sessionId, String threadId, long callNumber ){
		return sessionId+"."+threadId+"."+callNumber;
	}
	
	public static String getSessionId( String methodCallId ){
		int pos = methodCallId.indexOf('.');
		return methodCallId.substring(0,pos);
	}
	
	public static String getThreadId( String methodCallId ){
		int last = methodCallId.lastIndexOf('.');
		int first = methodCallId.indexOf('.');
		return methodCallId.substring(first+1,last);
		
	}
	
	
	public static long getMethodCallNumber( String methodCallId ){
		int pos = methodCallId.lastIndexOf('.');
		String numString = methodCallId.substring(pos+1);
		return Long.valueOf(numString);
	}
	
	public IoInteractionMappingTrace getIoMappingsForInteractionTrace(String sessionId, String threadId) {
		try {
			
			List<InteractionTrace> traces = ioInteractionMappingRepository.getRawTraces();
			for ( InteractionTrace trace : traces ){
				if ( trace.getThreadId().equals(threadId) && trace.getSessionId().equals(sessionId) ){
					
					return loadIoInteractionMappings(sessionId, threadId, trace);
					
					
				}
			}
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private IoInteractionMappingTrace loadIoInteractionMappings(String sessionId, String threadId, InteractionTrace trace) {
		Token token;
		List<Long> result = new ArrayList<Long>();
		
		try {
			while ( ( token = trace.getNextToken() ) != null ){
				Long offset = Long.valueOf(token.getTokenValue());
				result.add(offset);
			}
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new IoInteractionMappingTrace(sessionId,threadId,result);
	}


	public InteractionTrace getInteractionTrace(String sessionId, String threadId) {
		try {
			
			List<InteractionTrace> traces = interactionRepository.getRawTraces();
			for ( InteractionTrace trace : traces ){
				
				if ( trace.getThreadId().equals(threadId) && trace.getSessionId().equals(sessionId) ){
					return trace;
				}
			}
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Iterator<InteractionTrace> getInteractionTracesForThreadId(
			String threadId) {
		List<InteractionTrace> result = new ArrayList<InteractionTrace>();
		try {
			
			List<InteractionTrace> traces = interactionRepository.getRawTraces();
			for ( InteractionTrace trace : traces ){
				if ( trace.getThreadId().equals(threadId) ){
					result.add(trace);
				}
			}
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.iterator();
	}

	public InteractionTrace getInteractionTrace(String traceId) throws TraceReaderException {
		try {
			return interactionRepository.getRawTraceFromId(traceId);
		} catch (FileReaderException e) {
			throw new TraceReaderException(e);
		}
	}


}
