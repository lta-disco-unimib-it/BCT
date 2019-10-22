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
//import traceReaders.raw.FileIoTrace;
//import traceReaders.raw.FileIoTraceRepository;
import traceReaders.raw.FileReaderException;
import traceReaders.raw.InteractionTrace;
//import traceReaders.raw.IoTrace;
import util.FileIndex.FileIndexException;
import conf.ConfigurationSettings;
import conf.EnvironmentalSetter;
import flattener.core.Handler;
//import flattener.core.StimuliRecorder;

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
public class FileDataRecorder {
	static final String INTERACTION_DATA_FILE_EXTENSION = ".int";
	private static final String THREAD_EXECUTION_SEPARATOR = ".";
	
	public static final String DEFAULT_IO_DIR = "ioInvariantLogs";
	public static final String DEFAULT_INTERACTION_DIR = "interactionInvariantLogs";
	
	private File ioLogDir;
	protected File interactionLogDir;
	
	//private FileIoTraceRepository ioRepository;
	private FileInteractionTraceRepository interactionRepository;
	private FileExecutionsRepository executionRepository;
	
	private String executionId;
//	private static final boolean disableIoRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableIoRecording();
	
	private static interface State{
		public static final String ENTER = ":::ENTER";
		public static final String EXIT = ":::EXIT1";
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
	
	public void init ( File dataDir ){
		setLogDir(dataDir);
		
		//ioRepository = new FileIoTraceRepository(ioLogDir);
		interactionRepository = new FileInteractionTraceRepository(interactionLogDir);
		executionRepository = new FileExecutionsRepository( interactionLogDir );
		
		try {
			this.executionId = executionRepository.newExecution(""+System.currentTimeMillis());
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
//		//File file = new File ( ioLogDir , methodSignature+".meta" );
//		
//		if ( disableIoRecording ){
//			return;
//		}
//		
//		try {
//			FileIoTrace trace = getIoTraceForMethod(methodSignature);
//			Writer writer = new FileWriter( trace.getMetaTraceFile(), true );
//			recordMetaInfo( writer , metaInfo);
//			writer.close();
//		} catch (IOException e) {
//			
//		} catch (FileReaderException e) {
//			throw new RecorderException(e.getMessage());
//		}
	}
	
//	public FileIoTrace getIoTraceForMethod( String methodSignature ) throws FileReaderException{
//		return (FileIoTrace) ioRepository.getRawTrace(methodSignature);
//	}
	
	protected synchronized void recordInteractionMetaInfo( String methodSignature, String metaInfo, long threadId ) throws RecorderException {
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
		
		ioLogDir.mkdirs();
		interactionLogDir.mkdir();
		
	}
	
//	public void recordIoEnter( String methodSignature, Handler[] parametersHandlers) throws RecorderException {
//		recordIo( State.ENTER, methodSignature, parametersHandlers, null, false );
//	}
//
//
//	public void recordIoExit(String methodSignature, Handler[] parametersHandlers) throws RecorderException {
//		recordIo( State.EXIT, methodSignature, parametersHandlers, null, false );
//	}
//
//	public void recordIoExit(String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler) throws RecorderException {
//		recordIo( State.EXIT, methodSignature, parametersHandlers, returnValueHandler, true );
//	}
//
//	public void recordIoEnterMeta( String methodSignature, Handler[] parametersHandlers, String metaInfo ) throws RecorderException {
//		recordIo( State.ENTER, methodSignature, parametersHandlers, null, false );
//		recordIoMetaInfo( State.ENTER, methodSignature, metaInfo);
//	}
//
//
//	public void recordIoExitMeta(String methodSignature, Handler[] parametersHandlers, String metaInfo) throws RecorderException {
//		recordIo( State.EXIT, methodSignature, parametersHandlers, null, false );
//		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
//	}
//
//	public void recordIoExitMeta(String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler, String metaInfo ) throws RecorderException {
//		recordIo( State.EXIT, methodSignature, parametersHandlers, returnValueHandler, true );
//		recordIoMetaInfo( State.EXIT, methodSignature, metaInfo );
//	}
//	
//	protected synchronized void recordIo( String state, String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler, boolean writeReturn) throws RecorderException {
//		
//		if ( disableIoRecording  ){
//			return;
//		}
//		
//		try {
//			FileIoTrace trace = getIoTraceForMethod(methodSignature);
//
//			StimuliRecorder stimuliRecorder = FlattenerAssemblerFactory.INSTANCE.getStimuliRecorder();
//			Writer writer;
//
//
//			writer = new FileWriter( trace.getTraceFile(), true);
//
//			writer.write("\n"+methodSignature+state+"\n");
//
//
//			stimuliRecorder.setWriter(writer);
//			
//			for ( int i = 0; i < parametersHandlers.length; ++i ){
//				stimuliRecorder.record( parametersHandlers[i].getData() );
//			}
//			
//			if ( writeReturn )
//				stimuliRecorder.record( returnValueHandler.getData() );
//			
//			writer.close();
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage());
//			
//		} catch (Exception e) {
//			
//			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage());
//		}
//
//	
//		
//	}

	public void recordInteractionEnter(String methodSignature, long threadId ) throws RecorderException {
		recordInteraction( methodSignature+"B#", threadId);
	}

	public void recordInteractionExit(String methodSignature, long threadId ) throws RecorderException {
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
	
	public FileInteractionTrace getThreadTraceForCurrentSession( long threadId ) throws FileReaderException{
		return (FileInteractionTrace) interactionRepository.getRawTrace(getThreadExecutionId(threadId));
	}

	public void recordIoInteractionEnter(String methodSignature, Handler[] parameters, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"B#", threadId);
//		recordIo( State.ENTER, methodSignature, parameters, null, false ); 
	}

	public void recordIoInteractionExit(String methodSignature, Handler[] parameters, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"E#", threadId);
//		recordIo( State.EXIT, methodSignature, parameters, null, false );	
	}

	public void recordIoInteractionExit(String methodSignature, Handler[] parameters, Handler returnValue, long threadId) throws RecorderException {
		recordInteraction( methodSignature+"E#", threadId);
//		recordIo( State.EXIT, methodSignature, parameters, returnValue, true );
	}


	public File getInteractionLogDir() {
		return interactionLogDir;
	}


	public File getIoLogDir() {
		return ioLogDir;
	}


	public void recordIoInteractionEnterMeta(Object calledObject, String methodSignature, Handler[] parameters, long threadId, String metaInfo) throws RecorderException {
		recordInteractionEnterMeta( methodSignature, threadId, metaInfo);
//		recordIoEnterMeta( methodSignature, parameters,  metaInfo );
	}


	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature, Handler[] parameters, long threadId, String metaInfo) throws RecorderException {
		recordInteractionExitMeta( methodSignature, threadId, metaInfo);
//		recordIoExitMeta( methodSignature, parameters,  metaInfo );
	}


	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature, Handler[] parameters, Handler returnValue, long threadId, String metaInfo) throws RecorderException {
		recordInteractionExitMeta( methodSignature, threadId, metaInfo);
//		recordIoExitMeta( methodSignature, parameters,  returnValue, metaInfo );
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



	
	public void recordIoInteractionEnterMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionEnterMeta(null, methodSignature, parameters, threadId, metaInfo);
	}



	
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta(null, methodSignature, parameters, threadId, metaInfo);
	}



	
	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta(null, methodSignature, parameters, returnValue, threadId, metaInfo);
	}
}
