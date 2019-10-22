/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
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

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;

import util.cbe.FailuresExporter;
import failureDetection.Failure;

public abstract class CBEFailureRecorder implements FailureRecorder {
	FailuresExporter exporter = new FailuresExporter();
	
	public void recordFailure(Failure failure) throws RecorderException {
		CommonBaseEvent cbe = exporter.createFailureEvent(failure);
		
		recordEvent(cbe);
	}

	protected abstract void recordEvent(CommonBaseEvent cbe) throws RecorderException;

}
