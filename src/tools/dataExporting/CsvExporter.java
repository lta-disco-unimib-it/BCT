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
package tools.dataExporting;

import traceReaders.normalized.NormalizedIoTrace;
import traceReaders.normalized.NormalizedIoTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;

public class CsvExporter {

	/**
	 * @param args
	 * @throws NormalizedTraceHandlerException 
	 */
	public static void main(String[] args) throws NormalizedTraceHandlerException {
		// TODO Auto-generated method stub
		NormalizedIoTraceIterator tit = TraceHandlerFactory.getNormalizedIoTraceHandler().getIoTracesIterator();
		while ( tit.hasNext() ){
			NormalizedIoTrace trace = tit.next();
			
			
		}
	}

}
