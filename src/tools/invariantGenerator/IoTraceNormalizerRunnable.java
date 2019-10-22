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
package tools.invariantGenerator;

import java.io.IOException;

import regressionTestManager.MetaDataHandler;
import tools.InvariantGenerator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;

public class IoTraceNormalizerRunnable implements Runnable {

	private IoTrace trace;
	private MetaDataHandler metaHandler;
	private boolean addAdditionalInvariants;
	private boolean expandReferences;

	public IoTraceNormalizerRunnable( IoTrace trace, MetaDataHandler metaHandler, boolean addAdditionalInvariants, boolean expandReferences ) {
		this.trace = trace;
		this.metaHandler = metaHandler;
		this.addAdditionalInvariants = addAdditionalInvariants;
		this.expandReferences = expandReferences;
	}
	
	public void run() {
		try {
			InvariantGenerator.normalizeIoTrace(trace, metaHandler, addAdditionalInvariants, expandReferences, null);
		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
