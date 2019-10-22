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

import recorders.ThreadRecordedDataBuffer.RecordedData;
import conf.ConfigurationSettings;
import flattener.core.Handler;

public interface IThreadRecordedDataBuffer {

	public static enum DataTypes { 
		InteractionEnter,
		InteractionExit,
		IoEnter,
		IoExit,
		IoInteractionEnter,
		IoInteractionExit,
		InteractionEnterMeta,
		InteractionExitMeta,
		IoEnterMeta,
		IoExitMeta,
		IoInteractionEnterMeta,
		IoInteractionExitMeta, 
		IoExitRet, 
		IoExitRetMeta, 
		IoInteractionExitRet,
		IoInteractionExitRetMeta, 
		GenericProgramPoint,

		}
	
	
	public abstract long getThreadId();

	public abstract void init(ConfigurationSettings opts);

	public abstract Iterator<RecordedData> getBufferIterator();

	public abstract void recordInteractionEnter(String methodSignature,
			long threadId) throws RecorderException;

	public abstract void recordInteractionEnterMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException;

	public abstract void recordInteractionExit(String methodSignature,
			long threadId) throws RecorderException;

	public abstract void recordInteractionExitMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException;

	public abstract void recordIoEnter(String methodSignature,
			Handler[] parameters) throws RecorderException;

	public abstract void recordIoEnterMeta(String methodSignature,
			Handler[] parameters, String metaInfo) throws RecorderException;

	public abstract void recordIoExit(String methodSignature,
			Handler[] parameters) throws RecorderException;

	public abstract void recordIoExit(String methodSignature,
			Handler[] parameters, Handler returnValue) throws RecorderException;

	public abstract void recordIoExitMeta(String methodSignature,
			Handler[] parameters, String metaInfo) throws RecorderException;

	public abstract void recordIoExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, String metaInfo)
			throws RecorderException;

	public abstract void recordIoInteractionEnter(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException;

	public abstract void recordIoInteractionEnterMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException;

	public abstract void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException;

	public abstract void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId)
			throws RecorderException;

	public abstract void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException;

	public abstract void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException;

	public abstract void reset();

}