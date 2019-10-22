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

import conf.ConfigurationSettings;
import flattener.core.Handler;

public class NullDataRecorder implements DataRecorder {

	public void init(ConfigurationSettings opts) {
		// TODO Auto-generated method stub

	}

	public void recordInteractionEnter(String methodSignature, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordInteractionEnterMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordInteractionExit(String methodSignature, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordInteractionExitMeta(String methodSignature,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoEnter(String methodSignature, Handler[] parameters)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoEnterMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoExit(String methodSignature, Handler[] parameters)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoExit(String methodSignature, Handler[] parameters,
			Handler returnValue) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoExitMeta(String methodSignature, Handler[] parameters,
			Handler returnValue, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionEnter(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionEnterMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, long threadId) throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionExit(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, long threadId, String metaInfo)
			throws RecorderException {
		// TODO Auto-generated method stub

	}

	public void recordIoInteractionExitMeta(String methodSignature,
			Handler[] parameters, Handler returnValue, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub

	}

	@Override
	public void recordIoInteractionEnterMeta(Object calledObject,
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordIoInteractionExitMeta(Object calledObject,
			String methodSignature, Handler[] parameters, long threadId,
			String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordIoInteractionExitMeta(Object calledObject,
			String methodSignature, Handler[] parameters, Handler returnValue,
			long threadId, String metaInfo) throws RecorderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newExecution(String execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordGenericProgramPoint(String programPointName,
			Handler[] variables, long threadId) throws RecorderException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordAdditionalInfoToLast(Handler additionalData)
			throws RecorderException {
		// TODO Auto-generated method stub
		
	}

}
