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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import conf.EnvironmentalSetter;

import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.FileReaderException;
import flattener.core.Handler;
import flattener.core.StimuliRecorder;

/**
 * This recorded save information on files. It is optimized:
 * writers are kept open during the execution
 * 
 * There should be only one data recorded per execution
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FileOptimizedDataRecorder extends FileDataRecorder implements OptimizedDataRecorder {
	private static final long SLEEP_WHEN_CLOSING = 1500;
	private static final boolean disableIoRecording = EnvironmentalSetter.getDataRecorderSettings().getDisableIoRecording();
	private HashMap<String,Writer> ioWriters=new HashMap<String, Writer>();
	private HashMap<String,Long> ioWritersSizes=new HashMap<String, Long>();
	private HashMap<String,Writer> ioMetaWriters=new HashMap<String, Writer>();
	private HashMap<Long,Writer> interactionWriters=new HashMap<Long, Writer>();
	private HashMap<Long,Writer> interactionMetaWriters=new HashMap<Long, Writer>();
	
	private AtomicBoolean isClosing = new AtomicBoolean(false);


	public FileOptimizedDataRecorder(){
		
	}
	
	public FileOptimizedDataRecorder( File dataDirFile ){
		super(dataDirFile);
	}
	
	protected synchronized void recordIoMetaInfo( String state, String methodSignature, String metaInfo ) throws RecorderException{
		//File file = new File ( ioLogDir , methodSignature+".meta" );
		if ( disableIoRecording ){
			return;
		}
		
		try {
			
			
			Writer writer = getIoMetaTraceWriter(methodSignature);
			
			recordMetaInfo( writer , metaInfo);	
			
			
		} catch (IOException e) {
			
		} catch (FileReaderException e) {
			throw new RecorderException(e.getMessage());
		}
	}
	
	private Writer getIoTraceWriter(String methodSignature) throws FileReaderException, IOException, RecorderException {
		
		synchronized (ioWriters) {


			Writer writer = ioWriters.get(methodSignature);
			if ( writer == null ){
				FileIoTrace trace = getIoTraceForMethod(methodSignature);
				File traceFile = trace.getTraceFile();
				writer = createWriter(traceFile);
				
				ioWriters.put(methodSignature, writer);
				ioWritersSizes.put(methodSignature, traceFile.length());
			}

			return writer;
		}

		
	}
	
	
	private long getWriterOffset(String methodSignature) {
		return ioWritersSizes.get(methodSignature);
	}
	
	private long setWriterOffset(String methodSignature, long offset) {
		synchronized (ioWriters) {
			return ioWritersSizes.put(methodSignature, offset);
		}
	}
	
	public FileIoTrace getIoTraceForMethod( String methodSignature ) throws FileReaderException{
		//if too many files are open lets close some of them
		while ( true ) {
			try {
				return super.getIoTraceForMethod(methodSignature);
			} catch (Throwable e) {
				try {
					closeWriters();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
	private Writer getIoMetaTraceWriter(String methodSignature) throws FileReaderException, IOException, RecorderException {
		synchronized (ioMetaWriters) {
			Writer writer = ioMetaWriters.get(methodSignature);
			if ( writer == null ){
				
				FileIoTrace trace = getIoTraceForMethod(methodSignature);
				
				writer = createWriter(trace.getMetaTraceFile());
			
				ioMetaWriters.put(methodSignature, writer);
			}
			return writer;
		}
	}
	
	private Writer getInteractionTraceWriter(long threadId) throws RecorderException {
		//System.out.println("BCT -- getInteractionTraceWriter");
		synchronized (interactionWriters) {
			
			Writer writer = interactionWriters.get(threadId);
			
			if ( writer == null ){
				
				//System.out.println("BCT -- need to create writer");
				FileInteractionTrace trace;
				try {
					trace = getThreadTraceForCurrentSession(threadId);
				} catch (FileReaderException e1) {
					throw new RecorderException(e1);
				}

				writer = createWriter(trace.getFile());
				
				interactionWriters.put(threadId, writer);
			}
			return writer;
		}
	}

	private Writer getInteractionMetaTraceWriter(long threadId) throws RecorderException {
		synchronized (interactionMetaWriters) {
			Writer writer = interactionMetaWriters.get(threadId);
			if ( writer == null ){

				FileInteractionTrace trace;
				try {
					trace = (FileInteractionTrace) getThreadTraceForCurrentSession(threadId);
				} catch (FileReaderException e1) {
					throw new RecorderException(e1);
				}

				writer = createWriter(trace.getMetaTraceFile());
				
				interactionMetaWriters.put(threadId, writer);
			}
			return writer;
		}
	}

	private Writer createWriter( File traceFile ) throws RecorderException{
		//System.out.println("BCT -- creating writer");
		while ( true ) {
			try {
				return new FileWriter( traceFile, true );

			} catch ( IOException e ){
				e.printStackTrace();
				try {
					closeWriters();
				} catch (IOException e1) {
					throw new RecorderException("Problem when closing writers.",e1);
				}
			} 
		}
	}
	
	/**
	 * Does not require to be synchronized, each thread accesses its writer only
	 */
	protected void recordInteractionMetaInfo( String methodSignature, String metaInfo, long threadId ) throws RecorderException {
		

		try {
			Writer writer = getInteractionMetaTraceWriter(threadId);

			recordMetaInfo(writer, metaInfo);	

			
		} catch (IOException e) {
			throw new RecorderException(e.getMessage());
		}

	}
	
	
		
	protected  long recordIo( String state, String methodSignature, Handler[] parametersHandlers, Handler returnValueHandler, boolean writeReturn) throws RecorderException {
		
		if ( disableIoRecording ){
			return -1;
		}
		
		try {
			
			StimuliRecorder stimuliRecorder = FlattenerAssemblerFactory.INSTANCE.getStimuliRecorder();
			Writer writer;


			writer = getIoTraceWriter(methodSignature);
			long offset = getWriterOffset(methodSignature);
			long writtenData = 0;
			synchronized ( this ){
				String head = "\n"+methodSignature+state+"\n";
				writer.write(head);

				writtenData += head.length();

				stimuliRecorder.setWriter(writer);

				for ( int i = 0; i < parametersHandlers.length; ++i ){
					writtenData += stimuliRecorder.record( parametersHandlers[i].getData() );
				}

				if ( writeReturn ){
					writtenData += stimuliRecorder.record( returnValueHandler.getData() );
				}
				
			}
			
			setWriterOffset(methodSignature, offset+writtenData);
			
			return offset+1; //we return a pointer to the beginning of the data
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage());
			
		} catch (Exception e) {
			
			throw new RecorderException("Error writing trace for "+methodSignature+ ": \n"+e.getMessage());
		}

	
		
	}
	
	

	protected void recordInteraction(String methodSignatureState, long threadId ) throws RecorderException {
		
		try {
			Writer writer = getInteractionTraceWriter(threadId);

			writer.write(methodSignatureState);			

		} catch (FileNotFoundException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		} catch (IOException e) {
			throw new RecorderException("Error writing "+e.getMessage());
		}
		
	}

	public void recordingEnd() {
		try {
			closeWriters();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeWriters() throws IOException {
		//System.err.println("BCT -- Closing writers");
		//check if some other thread is closing writers
		if ( ! isClosing.compareAndSet(false, true) ){
			//if another thread is closing writers we just need that it finishes
			while ( isClosing.get() ){
				try {
					//System.err.println("BCT -- Waiting for another thread to close writers");
					Thread.sleep(SLEEP_WHEN_CLOSING);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		try {
			closeWriters(ioWriters);
			closeWriters(ioMetaWriters);
			closeWriters(interactionWriters);
			closeWriters(interactionMetaWriters);
		} finally {
			//we are no longer closing writers
			isClosing.compareAndSet(true, false);
			//System.err.println("BCT -- Writers closed");
		}
		
	}

	private void closeWriters(HashMap<? extends Object, Writer> writers) throws IOException {
		
//		System.out.println("Closing writers");
		
		synchronized (writers) {


			for ( Writer writer : writers.values() ){
				
					writer.flush();
					writer.close();
				
			}
			writers.clear();

		}
	}

	public void recordingStart() {
		
	}

}
