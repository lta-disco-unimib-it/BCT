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

import java.util.Iterator;
import java.util.NoSuchElementException;

import conf.ConfigurationSettings;
import flattener.core.Handler;

public class ThreadRecordedDataBuffer implements DataRecorder, IThreadRecordedDataBuffer {
	private int size;
	
	
	
	private long threadId;
	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#getThreadId()
	 */
	public long getThreadId() {
		return threadId;
	}

	private RecordedData[] buffer;
	private int position = 0;
	
	public int getElementsNumber(){
		return position;
	}
	
	public class RecordedData {
		DataTypes type;
		String methodSignature;
		String metaInfo;
		Handler[] parameters;
		Handler returnValue;
		public long threadId;
	}

	public static class RecordedDataIterator implements Iterator<RecordedData> {

		private RecordedData[] buffer;
		private int elements;
		private int position;
		
		public RecordedDataIterator ( RecordedData[] buffer, int elements ){
			this.buffer = buffer;
			this.elements = elements;
		}
		
		public boolean hasNext() {
			return position < elements;
		}

		public RecordedData next() {
			if ( position >= elements ){
				throw new NoSuchElementException();
			}
			return buffer[position++]; 
		}

		public void remove() {
			
		}
		
	}
	
	public ThreadRecordedDataBuffer ( long threadId, int size ){
		this.threadId = threadId;
		this.size = size;
		this.position = 0;
		buffer = new RecordedData[size];
		for ( int i = 0; i < size; ++i ){
			buffer[i]=new RecordedData();
		}
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#init(conf.ConfigurationSettings)
	 */
	public void init(ConfigurationSettings opts) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#getBufferIterator()
	 */
	public Iterator<RecordedData> getBufferIterator(){
		return new RecordedDataIterator(buffer, position);	
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordInteractionEnter(java.lang.String, long)
	 */
	public void recordInteractionEnter(String methodSignature, long threadId)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.type = DataTypes.InteractionEnter;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordInteractionEnterMeta(java.lang.String, long, java.lang.String)
	 */
	public void recordInteractionEnterMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.type = DataTypes.InteractionEnter;
		data.metaInfo = metaInfo;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordInteractionExit(java.lang.String, long)
	 */
	public void recordInteractionExit(String methodSignature, long threadId)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.type = DataTypes.InteractionExit;
		
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordInteractionExitMeta(java.lang.String, long, java.lang.String)
	 */
	public void recordInteractionExitMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.metaInfo = metaInfo;
		data.type = DataTypes.InteractionExitMeta;
		
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoEnter(java.lang.String, flattener.core.Handler[])
	 */
	public void recordIoEnter(String methodSignature, Handler[] parameters)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.type = DataTypes.IoEnter;
		
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoEnterMeta(java.lang.String, flattener.core.Handler[], java.lang.String)
	 */
	public void recordIoEnterMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.metaInfo = metaInfo;
		data.type = DataTypes.IoEnterMeta;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoExit(java.lang.String, flattener.core.Handler[])
	 */
	public void recordIoExit(String methodSignature, Handler[] parameters)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.type = DataTypes.IoExit;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoExit(java.lang.String, flattener.core.Handler[], flattener.core.Handler)
	 */
	public void recordIoExit(String methodSignature, Handler[] parameters,
			Handler returnValue) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.returnValue = returnValue;
		data.type = DataTypes.IoExitRet;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoExitMeta(java.lang.String, flattener.core.Handler[], java.lang.String)
	 */
	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.type = DataTypes.IoExitMeta;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoExitMeta(java.lang.String, flattener.core.Handler[], flattener.core.Handler, java.lang.String)
	 */
	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			Handler returnValue, String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.returnValue = returnValue;
		data.type = DataTypes.IoExitRetMeta;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionEnter(java.lang.String, flattener.core.Handler[], long)
	 */
	public void recordIoInteractionEnter(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.type = DataTypes.IoInteractionEnter;
	}
	
	public void recordGenericProgramPoint(String programPointName,
			Handler[] variables, long threadId) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=programPointName;
		data.parameters=variables;
		data.type = DataTypes.GenericProgramPoint;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionEnterMeta(java.lang.String, flattener.core.Handler[], long, java.lang.String)
	 */
	public void recordIoInteractionEnterMeta(Object calledObject, String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.metaInfo = metaInfo;
		data.type = DataTypes.IoInteractionEnterMeta;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionExit(java.lang.String, flattener.core.Handler[], long)
	 */
	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.type = DataTypes.IoInteractionExit;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionExit(java.lang.String, flattener.core.Handler[], flattener.core.Handler, long)
	 */
	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.returnValue = returnValue;
		data.type = DataTypes.IoInteractionExitRet;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionExitMeta(java.lang.String, flattener.core.Handler[], long, java.lang.String)
	 */
	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.metaInfo = metaInfo;
		data.type = DataTypes.IoInteractionExitMeta;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#recordIoInteractionExitMeta(java.lang.String, flattener.core.Handler[], flattener.core.Handler, long, java.lang.String)
	 */
	public void recordIoInteractionExitMeta(Object calledObject, String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		RecordedData data = buffer[position++];
		data.methodSignature=methodSignature;
		data.parameters=parameters;
		data.metaInfo = metaInfo;
		data.returnValue = returnValue;
		data.type = DataTypes.IoInteractionExitRetMeta;
	}
	
	public boolean isFull(){
		return position == size;
	}

	/* (non-Javadoc)
	 * @see recorders.IThreadRecordedDataBuffer#reset()
	 */
	public void reset() {
		position = 0;
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
		recordIoInteractionExitMeta(null, methodSignature, parameters, returnValue, threadId, metaInfo);
		
	}

	@Override
	public void newExecution(String execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordAdditionalInfoToLast(Handler additionalData) {
		RecordedData data = buffer[position-1];
		Handler[] newParameters = new Handler[data.parameters.length+1];
		System.arraycopy(data.parameters, 0, newParameters, 0, data.parameters.length);
		newParameters[data.parameters.length] = additionalData;
		data.parameters=newParameters;
	}
}
