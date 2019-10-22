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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import recorders.ThreadRecordedDataBuffer.RecordedData;
import util.RuntimeContextualDataUtil;

import conf.ConfigurationSettings;
import flattener.core.Handler;

public class BufferedRecorder implements DataRecorder {
	private int bufferSize = 1000;
	
	public int getBufferSize() {
		return bufferSize;
	}


	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}


	private RecorderPool recorderPool = new RecorderPool();
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private OptimizedDataRecorder dataRecorder;
	private Hashtable<Long,ThreadRecordedDataBuffer> lastDataBuffers = new Hashtable<Long,ThreadRecordedDataBuffer>();
	
	private ThreadLocal<ThreadRecordedDataBuffer> recorderBuffer = new ThreadLocal<ThreadRecordedDataBuffer>(){

		@Override
		protected ThreadRecordedDataBuffer initialValue() {
			ThreadRecordedDataBuffer dbf = new ThreadRecordedDataBuffer(Thread.currentThread().getId(),bufferSize);
			lastDataBuffers.put(Thread.currentThread().getId(),dbf);
			return dbf;
		}
		
	};
	private BufferedRecorderShutdownHook shutdownHook;
	private ConfigurationSettings lastConfiguration;
	
	public class BufferedRecorderShutdownHook extends Thread {
		
		public void run(){
			
//			System.err.println("BufferedRecorderShutdownHook");
			
			for ( ThreadRecordedDataBuffer dbfSet : lastDataBuffers.values() ){
				schedule(dbfSet);
			}
			
//			System.err.println("BufferedRecorderShutdownHook waiting for recording termination");
			try {
				executor.shutdown();
				executor.awaitTermination(1000000,TimeUnit.SECONDS);
				//Thread.sleep(7000);
//				System.err.println("Recording Terminated");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.err.println("BufferedRecorderShutdownHook flushing data");
			dataRecorder.recordingEnd();
			
//			System.err.println("BufferedRecorderShutdownHook termination");
			//System.err.flush();
		}
		
	}
	
	{
		initShutdownHook();
	}
	
	private void initShutdownHook(){
		shutdownHook = new BufferedRecorderShutdownHook();
		//Add shutdownhook to flush
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}
	
	/**
	 * Flush the recorder by invoking the shutdownhook
	 * This should bot be invoked.
	 * 
	 */
	public void shutDownRecorder(){
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
		shutdownHook.run();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public BufferedRecorder ( OptimizedDataRecorder optimizedDataRecorder ){
		this.dataRecorder = optimizedDataRecorder;
		
	}
	
	
	public BufferedRecorder (  ){
		
		
	}
	
	private class RecorderPool {
		private LinkedList<ThreadRecordedDataBuffer> elements = new LinkedList<ThreadRecordedDataBuffer>();
		
		synchronized ThreadRecordedDataBuffer getCleanRecordedDataBuffer(){
			if ( elements.size() == 0 ){
				return new ThreadRecordedDataBuffer(Thread.currentThread().getId(),bufferSize);
			}
			return elements.removeLast();
		}
		
		void addCleanRecordedDataBuffer( ThreadRecordedDataBuffer recorder ){
			elements.add(recorder);
		}
	}
	
	public void restartRecorder(){
		executor = Executors.newSingleThreadExecutor();
		initShutdownHook();
		init(lastConfiguration);
	}
	
	public void init(ConfigurationSettings opts) {
		this.lastConfiguration = opts;
		String recorderClass = opts.getProperty("optimizedDataRecorder");
		try {
			this.dataRecorder = (OptimizedDataRecorder) Class.forName(recorderClass).newInstance();
			dataRecorder.init(opts);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recordInteractionEnter(String methodSignature, long threadId)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordInteractionEnter(methodSignature, threadId);
		scheduleIfNeeded(recorder);
		
	}

	private void scheduleIfNeeded(final ThreadRecordedDataBuffer recordedDataBuffer) {
		if ( recordedDataBuffer.isFull() ){
			
			ThreadRecordedDataBuffer currentDataBuffer = recorderPool.getCleanRecordedDataBuffer();
			recorderBuffer.set(currentDataBuffer);
			long threadId = recordedDataBuffer.getThreadId();
			lastDataBuffers.remove(threadId);
			lastDataBuffers.put(threadId, currentDataBuffer);
			
			schedule(recordedDataBuffer);
		}
	}
	

	private void schedule(final ThreadRecordedDataBuffer recordedDataBuffer) {
		executor.execute(new Runnable(){

			public void run() {
				try {
					
					//System.out.println("Scheduling flush "+recordedDataBuffer.getElementsNumber());
					flushBuffer( recordedDataBuffer);
					//System.out.println("Flushed");
					
				} catch (RecorderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recordedDataBuffer.reset();
			}

			
		});
		
		
	}
	
	private void flushBuffer(
			IThreadRecordedDataBuffer recordedDataBuffer) throws RecorderException {
		//System.err.println("Flushing "+recordedDataBuffer.getThreadId()+ " "+recordedDataBuffer.toString());
		Iterator<RecordedData> iterator = recordedDataBuffer.getBufferIterator();
		while ( iterator.hasNext() ){
			RecordedData data = iterator.next();
//			System.out.println("###DEBUG#### _ "+data.methodSignature+" "+data.type+" "+dataRecorder.getClass().getCanonicalName());
			switch( data.type ){
				case IoInteractionEnter:
					dataRecorder.recordIoInteractionEnter(data.methodSignature, data.parameters, recordedDataBuffer.getThreadId() );
					break;
				case IoInteractionExit:
					dataRecorder.recordIoInteractionExit(data.methodSignature, data.parameters, recordedDataBuffer.getThreadId() );
					break;
				case IoInteractionEnterMeta:
					dataRecorder.recordIoInteractionEnterMeta(data.methodSignature, data.parameters, recordedDataBuffer.getThreadId(), data.metaInfo );
					break;
				case IoInteractionExitMeta:
					dataRecorder.recordIoInteractionExitMeta(data.methodSignature, data.parameters, recordedDataBuffer.getThreadId(), data.metaInfo );
					break;
				case IoInteractionExitRet:
					dataRecorder.recordIoInteractionExit(data.methodSignature, data.parameters, data.returnValue, recordedDataBuffer.getThreadId() );
					break;
				case IoInteractionExitRetMeta:
					dataRecorder.recordIoInteractionExitMeta(data.methodSignature, data.parameters, data.returnValue, recordedDataBuffer.getThreadId(), data.metaInfo );
					break;
				case IoEnter:
					dataRecorder.recordIoEnter(data.methodSignature, data.parameters );
					break;
				case IoExit:
					dataRecorder.recordIoExit(data.methodSignature, data.parameters );
					break;
				case IoEnterMeta:
					dataRecorder.recordIoEnterMeta(data.methodSignature, data.parameters, data.metaInfo );
					break;
				case IoExitMeta:
					dataRecorder.recordIoExitMeta(data.methodSignature, data.parameters, data.metaInfo );
					break;
				case IoExitRet:
					dataRecorder.recordIoExit(data.methodSignature, data.parameters, data.returnValue );
					break;
				case IoExitRetMeta:
					dataRecorder.recordIoExitMeta(data.methodSignature, data.parameters, data.returnValue, data.metaInfo );
					break;	
				case InteractionEnter:
					dataRecorder.recordInteractionEnter(data.methodSignature, recordedDataBuffer.getThreadId() );
					break;
				case InteractionExit:
					dataRecorder.recordInteractionExit(data.methodSignature, recordedDataBuffer.getThreadId() );
					break;
				case InteractionEnterMeta:
					dataRecorder.recordInteractionEnterMeta(data.methodSignature, recordedDataBuffer.getThreadId(), data.metaInfo );
					break;
				case InteractionExitMeta:
					dataRecorder.recordInteractionExitMeta(data.methodSignature, recordedDataBuffer.getThreadId(), data.metaInfo );
					break;
				case GenericProgramPoint:
					dataRecorder.recordGenericProgramPoint(data.methodSignature, data.parameters, recordedDataBuffer.getThreadId() );
					break;
				
			}
		}
	}

	public void recordInteractionEnterMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordInteractionEnterMeta(methodSignature, threadId, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordInteractionExit(String methodSignature, long threadId)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordInteractionExit(methodSignature, threadId);
		scheduleIfNeeded(recorder);
	}

	public void recordInteractionExitMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordInteractionExitMeta(methodSignature, threadId, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoEnter(String methodSignature, Handler[] parameters)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoEnter(methodSignature, parameters);
		scheduleIfNeeded(recorder);
	}

	public void recordIoEnterMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoEnterMeta(methodSignature, parameters, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoExit(String methodSignature, Handler[] parameters,
			Handler returnValue) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoExit(methodSignature, parameters, returnValue);
		scheduleIfNeeded(recorder);
	}

	public void recordIoExit(String methodSignature, Handler[] parameters)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoExit(methodSignature, parameters);
		scheduleIfNeeded(recorder);
	}

	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			Handler returnValue, String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoExitMeta(methodSignature, parameters, returnValue,
				metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoExitMeta(methodSignature, parameters, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionEnter(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionEnter(methodSignature, parameters, threadId);
		scheduleIfNeeded(recorder);
	}
	
	public void recordGenericProgramPoint(String programPointName,
			Handler[] variables, long threadId) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordGenericProgramPoint(programPointName, variables, threadId);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionEnterMeta(Object calledObject, String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionEnterMeta(methodSignature, parameters,
				threadId, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionExit(methodSignature, parameters,
				returnValue, threadId);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionExit( String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionExit(methodSignature, parameters, threadId);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionExitMeta(methodSignature, parameters,
				returnValue, threadId, metaInfo);
		scheduleIfNeeded(recorder);
	}

	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordIoInteractionExitMeta(methodSignature, parameters,
				threadId, metaInfo);
		scheduleIfNeeded(recorder);
	}


	@Override
	public void recordIoInteractionEnterMeta(
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionEnterMeta( null, methodSignature, parameters, threadId, metaInfo);
	}


	@Override
	public void recordIoInteractionExitMeta(
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta( null, methodSignature, parameters, threadId, metaInfo);
	}


	@Override
	public void recordIoInteractionExitMeta(
			String methodSignature, Handler[] parameters, Handler returnValue,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		recordIoInteractionExitMeta( null, methodSignature, parameters, returnValue, threadId, metaInfo);
	}


	@Override
	public void newExecution(String sessionId) {
		//System.out.println("NOTIFIED FOR NEW EXECUTION: "+sessionId);
		//System.out.println("CURRENT EXCUTION "+RuntimeContextualDataUtil.retrievePID());
		shutDownRecorder();
		//Fix for bug 202
		RuntimeContextualDataUtil.setPid(sessionId);
		//System.out.println("NEW EXCUTION "+RuntimeContextualDataUtil.retrievePID());
		restartRecorder();
	}


	@Override
	public void recordAdditionalInfoToLast(Handler additionalData) {
		ThreadRecordedDataBuffer recorder = recorderBuffer.get();
		recorder.recordAdditionalInfoToLast(additionalData);
		scheduleIfNeeded(recorder);
	}



}
