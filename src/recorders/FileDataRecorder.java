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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.FileInteractionTraceRepository;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.FileIoTraceRepository;
import traceReaders.raw.FileReaderException;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.IoTrace;
import util.FileIndex.FileIndexException;
import conf.ConfigurationSettings;
import conf.EnvironmentalSetter;
import flattener.core.Handler;
import flattener.core.StimuliRecorder;

/**
 * Recorder that saves all informations on file.
 * These informations are saved in the directory indicated by the RecorderSettings passed to the init method.
 * Models are then saved under 01-logs: 01-logs/ioInvariantLogs 01-log/interactionInvariantLogs.
 * InteractionsLog are savend in a file named "Logs For Thread #TID.txt" where #TID is the thread id.
 * IoLog are saved with the name of the method that belong to. 
 * 
 * TODO: change the naming of IoLog file name, we need a shorter name.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileDataRecorder implements DataRecorder {
	public static final String INTERACTION_DATA_FILE_EXTENSION = ".int";
	public static final String THREAD_EXECUTION_SEPARATOR = ".";
	
	public static final String DEFAULT_IO_DIR = "ioInvariantLogs";
	public static final String DEFAULT_INTERACTION_DIR = "interactionInvariantLogs";
	public static final String DEFAULT_IO_INTERACTION_MAPPING_DIR = "ioInteractionMappings";
	
	
	private File ioLogDir;
	protected File interactionLogDir;
	
	private FileIoTraceRepository ioRepository;
	private FileInteractionTraceRepository ioInteractionMappingRepository;
	private FileInteractionTraceRepository interactionRepository;
	private FileExecutionsRepository executionRepository;
	
	private String executionId;
	private File ioInteractionMappingLogDir;
	private int stackCounter;
	
	private static final boolean disableMetaRecording;
	private static final boolean disableIoRecording;
	private static final boolean enableIoInteractionMapping;
	private static final boolean disableInteractionRecording;
	
	private static final byte[] NEW_LINE_BYTES = "#".getBytes();
	
	
	static {
		if ( EnvironmentalSetter.getUseBctHome() ){
			disableIoRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableIoRecording();
			disableMetaRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableMetaRecording();
			enableIoInteractionMapping = EnvironmentalSetter.getDataRecorderSettings().getEnableIoInteractionMapping();
			disableInteractionRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableInteractionRecording();
		} else {
			disableMetaRecording = false;
			disableIoRecording = false;
			enableIoInteractionMapping = true;
			disableInteractionRecording = false;
		}
	}
	
	private static interface State{
		public static final String ENTER = ":::ENTER";
		public static final String EXIT = ":::EXIT1";
		public static final String POINT = ":::POINT";
	}
	

	public interface Options {
		final static String loggingDataDir = "loggingDataDir";
		final static String traceExecutionStatus = "traceExecutionStatus";
	}
	
	
	public FileDataRecorder ( ){
	}
	
	

	/**
	 * Create a data recorder that records data in the provided data dir
	 * 
	 * @param dataDirFile
	 */
	public FileDataRecorder(File dataDirFile) {
		init(dataDirFile);
	}




	/*
	 * Initialization method, the passed recorderSettings must contain a "loggingDataDir" field.
	 * 
	 * @see recorders.DataRecorder#init(conf.RecorderSettings)
	 */
	public void init( ConfigurationSettings opts ){
		String dataDir = opts.getProperty(Options.loggingDataDir);
		
		init(new File(dataDir));
	}
	
	public void newExecution(String sessionId){
		
		if ( stackCounter != 0 ){
			System.err.println("Asymmetric trace "+sessionId);
		}
		
		try {
			this.executionId = executionRepository.newExecution(sessionId);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void init ( File dataDir ){
		setLogDir(dataDir);
		
		ioRepository = new FileIoTraceRepository(ioLogDir);
		interactionRepository = new FileInteractionTraceRepository(interactionLogDir);
		ioInteractionMappingRepository = new FileInteractionTraceRepository(ioInteractionMappingLogDir);
		executionRepository = new FileExecutionsRepository( interactionLogDir );
		
		try {
			this.executionId = executionRepository.newExecution(SessionsRegistry.getCurrentSessionId());
			//System.out.println("FileDataRecorder EXECUTION ID "+executionId);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	protected void recordMetaInfo( Writer writer, String metaInfo ) throws IOException{
		
		String message =  metaInfo+"\n#\n";
					
		writer.write(message);
		
	}
	
	protected synchronized void recordIoMetaInfo( String state, String methodSignature, String metaInfo ) throws RecorderException{
		//File file = new File ( ioLogDir , methodSignature+".meta" );
		
		if ( disableIoRecording || disableMetaRecording ){
			return;
		}
		
		try {
			FileIoTrace trace = getIoTraceForMethod(methodSignature);
			Writer writer = new FileWriter( trace.getMetaTraceFile(), true );
			recordMetaInfo( writer , metaInfo);
			writer.close();
		} catch (IOException e) {
			
		} catch (FileReaderException e) {
			throw new RecorderException(e.getMessage());
		}
	}
	
	public FileIoTrace getIoTraceForMethod( String methodSignature ) throws FileReaderException{
		return (FileIoTrace) ioRepository.getRawTrace(methodSignature);
	}
	
	protected synchronized void recordInteractionMetaInfo( String methodSignature, String metaInfo, long threadId ) throws RecorderException {
		if ( disableInteractionRecording || disableMetaRecording ){
			return;
		}
		
		FileInteractionTrace trace;
		try {
			trace = getThreadTraceForCurrentSession(threadId);
		} catch (FileReaderException e1) {
			throw new RecorderException(e1);
		}

		try {
			Writer writer = new FileWriter( trace.getMetaTraceFile(), true );
			recordMetaInfo(writer, metaInfo);
			writer.close();
		} catch (IOException e) {
			throw new RecorderException(e.getMessage());
		}

	}
	
	private String getThreadExecutionId(long threadId) {
		return this.executionId+THREAD_EXECUTION_SEPARATOR+threadId;
	}

	public static String getExecutionId(String threadExecutionId) {
		int pos = threadExecutionId.indexOf(THREAD_EXECUTION_SEPARATOR);
		if ( pos < 0 ){
			return "";
		}
		return threadExecutionId.substring(0,pos);
	}
	
	public static String getThreadId(String threadExecutionId) {
		int pos = threadExecutionId.indexOf(THREAD_EXECUTION_SEPARATOR);
		if ( pos == 0 ){
			return threadExecutionId;
		}
		return threadExecutionId.substring(pos+1);
	}

	public void setLogDir( String dataDir ){
		setLogDir(new File(dataDir));
	}
	
	public void setLogDir( File dataDir ){
		ioLogDir = new File(dataDir,DEFAULT_IO_DIR);
		interactionLogDir = new File(dataDir,DEFAULT_INTERACTION_DIR);
		ioInteractionMappingLogDir = new File(dataDir,DEFAULT_IO_INTERACTION_MAPPING_DIR);
		ioLogDir.mkdirs();
		interactionLogDir.mkdir();
		ioInteractionMappingLogDir.mkdir();
		
	}
	
	public void recordIoEnter( String methodSignature, Handler[] parametersHandlers) throws RecorderException {
		recordIo( State.ENTER, methodSignature, parametersHandlers, null, false );
	}


	public void recordIoExit(String methodSignature, Handler[] parametersHandlers) throws RecorderException {
		recordIo( State.EXIT, methodSignature, parametersHandlers, null, false );
	}

	public void recordIoExit(String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler) throws RecorderException {
		recordIo( State.EXIT, methodSignature, parametersHandlers, returnValueHandler, true );
	}

	public void recordIoEnterMeta( String methodSignature, Handler[] parametersHandlers, String metaInfo ) throws RecorderException {
		recordIo( State.ENTER, methodSignature, parametersHandlers, null, false );
		recordIoMetaInfo( State.ENTER, methodSignature, metaInfo);
	}


	public void recordIoExitMeta(String methodSignature, Handler[] parametersHandlers, String metaInfo) throws RecorderException {
		recordIo( State.EXIT, methodSignature, parametersHandlers, null, false );
		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
	}

	public void recordIoExitMeta(String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler, String metaInfo ) throws RecorderException {
		recordIo( State.EXIT, methodSignature, parametersHandlers, returnValueHandler, true );
		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
	}
	
	protected synchronized long recordIo( String state, String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler, boolean writeReturn) throws RecorderException {
		
		if ( disableIoRecording  ){
			return -1;
		}
		
		try {
			FileIoTrace trace = getIoTraceForMethod(methodSignature);

			StimuliRecorder stimuliRecorder = FlattenerAssemblerFactory.INSTANCE.getStimuliRecorder();
			Writer writer;

			File traceFile = trace.getTraceFile();
			
			long begin = traceFile.length();
			writer = new FileWriter( trace.getTraceFile(), true);

			writer.write("\n"+methodSignature+state+"\n");


			stimuliRecorder.setWriter(writer);
			
			for ( int i = 0; i < parametersHandlers.length; ++i ){
				stimuliRecorder.record( parametersHandlers[i].getData() );
			}
			
			if ( writeReturn )
				stimuliRecorder.record( returnValueHandler.getData() );
			
			writer.close();
			
			return begin+1;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage(),e);
			
		} catch (Exception e) {
			
			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage(),e);
		}

	
		
	}

	public void recordInteractionEnter(String methodSignature, long threadId ) throws RecorderException {
		stackCounter++;
		recordInteraction( methodSignature+"B#", threadId);
	}

	public void recordInteractionExit(String methodSignature, long threadId ) throws RecorderException {
		stackCounter--;
		recordInteraction( methodSignature+"E#", threadId);
	}

	public void recordInteractionEnterMeta(String methodSignature, long threadId, String metaInfo ) throws RecorderException {
		recordInteraction( methodSignature+"B#", threadId);
		recordInteractionMetaInfo( methodSignature, metaInfo, threadId);
	}

	public void recordInteractionExitMeta(String methodSignature, long threadId, String metaInfo ) throws RecorderException {
		recordInteraction( methodSignature+"E#", threadId);
		recordInteractionMetaInfo( methodSignature, metaInfo, threadId);
	}
	
	protected synchronized void recordInteraction(String methodSignatureState, long threadId ) throws RecorderException {
		if ( disableInteractionRecording ){
			return;
		}
		
		FileInteractionTrace trace;
		try {
			trace = getThreadTraceForCurrentSession(threadId);
			
			//open an output stream in append mode
			FileOutputStream fos = new FileOutputStream( trace.getFile(), true );
			fos.write(methodSignatureState.getBytes());			
	        fos.close();
		} catch (FileNotFoundException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		} catch (IOException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected synchronized void recordIoInteractionMapping(long offset, long threadId ) throws RecorderException {
		if ( ! enableIoInteractionMapping ){
			return;
		}
		
		FileInteractionTrace trace;
		try {
			trace = getIoInteractionMappingTraceForCurrentSession(threadId);
			
			//open an output stream in append mode
			FileOutputStream fos = new FileOutputStream( trace.getFile(), true );
			fos.write(String.valueOf(offset).getBytes());
			fos.write(NEW_LINE_BYTES);
	        fos.close();
		} catch (FileNotFoundException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		} catch (IOException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		} catch (FileReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public FileInteractionTrace getIoInteractionMappingTraceForCurrentSession( long threadId ) throws FileReaderException{
		return (FileInteractionTrace) ioInteractionMappingRepository.getRawTrace(getThreadExecutionId(threadId));
	}
	
	public FileInteractionTrace getThreadTraceForCurrentSession( long threadId ) throws FileReaderException{
		return (FileInteractionTrace) interactionRepository.getRawTrace(getThreadExecutionId(threadId));
	}

	public void recordIoInteractionEnter(String methodSignature, Handler[] parameters, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"B#", threadId);
		long offset = recordIo( State.ENTER, methodSignature, parameters, null, false );
		recordIoInteractionMapping(offset, threadId);
	}
	

	
	public void recordGenericProgramPoint(String programPointName, Handler[] variables, long threadId) throws RecorderException {
		//TODO: we could add interaction traces also for program points, they could trace internal behaviors
		recordInteraction( programPointName+"P#", threadId);
		long offset = recordIo( State.POINT, programPointName, variables, null, false );
		recordIoInteractionMapping(offset, threadId);
	}

	public void recordIoInteractionExit(String methodSignature, Handler[] parameters, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"E#", threadId);
		long offset = recordIo( State.EXIT, methodSignature, parameters, null, false );
		recordIoInteractionMapping(offset, threadId);
	}

	public void recordIoInteractionExit(String methodSignature, Handler[] parameters, Handler returnValue, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"E#", threadId);
		long offset = recordIo( State.EXIT, methodSignature, parameters, returnValue, true );
		recordIoInteractionMapping(offset, threadId);
	}


	public File getInteractionLogDir() {
		return interactionLogDir;
	}


	public File getIoLogDir() {
		return ioLogDir;
	}


	public void recordIoInteractionEnterMeta(Object calledObject, String methodSignature, Handler[] parameters, long threadId, String metaInfo) throws RecorderException {
		recordInteractionEnterMeta( methodSignature, threadId, metaInfo);
		long offset = recordIo( State.ENTER, methodSignature, parameters, null, false );
		recordIoMetaInfo( State.ENTER, methodSignature, metaInfo);
		recordIoInteractionMapping(offset, threadId);
	}


	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature, Handler[] parameters, long threadId, String metaInfo) throws RecorderException {
		recordInteractionExitMeta( methodSignature, threadId, metaInfo);
		long offset = recordIo( State.EXIT, methodSignature, parameters, null, false );
		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
		recordIoInteractionMapping(offset, threadId);
	}


	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature, Handler[] parameters, Handler returnValue, long threadId, String metaInfo) throws RecorderException {
		recordInteractionExitMeta( methodSignature, threadId, metaInfo);
		long offset = recordIo( State.EXIT, methodSignature, parameters, returnValue, true );
		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
		recordIoInteractionMapping(offset, threadId);
	}

	public void deleteInteractionTraceForThread(long threadId) throws RecorderException {
		FileInteractionTrace trace;
		try {
			trace = (FileInteractionTrace) interactionRepository.getRawTrace(getThreadExecutionId(threadId));

			boolean result = trace.getFile().delete();
			if ( ! result ){
				throw new RecorderException("Cannot delete trace "+trace.getFile());
			}
		} catch (FileReaderException e) {
			throw new RecorderException(e);
		}
	}



	@Override
	public void recordIoInteractionEnterMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionEnterMeta(null, methodSignature, parameters, threadId, metaInfo);
	}



	@Override
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta(null, methodSignature, parameters, threadId, metaInfo);
	}



	@Override
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta(null, methodSignature, parameters, returnValue, threadId, metaInfo);
	}



	@Override
	public void recordAdditionalInfoToLast(Handler additionalData)
			throws RecorderException {
		//TODO: could be enough to write to the current file, but need to deal with the offset
		throw new RecorderException("Not implemented: recordAdditionalInfoToLast");
	}
}
