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
package tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import regressionTestManager.MetaDataHandler;
import regressionTestManager.TestCasesMetaDataHandler;
import regressionTestManager.detectionMatrix.DetectionMatrixGenerator;
import regressionTestManager.detectionMatrix.InteractionTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.handlers.TcInfoHandler;
import regressionTestManager.tcData.handlers.TcInfoHandlerException;
import regressionTestManager.tcData.handlers.TcInfoHandlerFactory;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TraceReaderFactory;
import traceReaders.raw.TracesReader;
import conf.BctSettingsException;

/**
 * This class just builds an interaction matrix for regression test selection
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BuildInteractionMatrix {
	
	public static void main(String args[]){
		TestCasesMetaDataHandler metaDataHandler = new TestCasesMetaDataHandler();
		try {
			normalizeInteractionInvariantLog(metaDataHandler);
			
			
			TcInfoHandler infoHandler = TcInfoHandlerFactory.getTcInfoHandler();
			
			InteractionTcDetectionMatrix dmInteraction = DetectionMatrixGenerator.createInteractionMatrix(infoHandler);
			
			TcDetectionMatrix.printCSVFormat( dmInteraction, new FileOutputStream("interactionMatrix.csv") );
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BctSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void normalizeInteractionInvariantLog( MetaDataHandler metaHandler  ) throws IOException, BctSettingsException, TraceException {


		TracesReader tr = TraceReaderFactory.getReader();
		System.out.println("InteractionTraceReader "+tr.getClass().getCanonicalName());
		Iterator traces = tr.getInteractionTraces();
		int i = 0;

		while ( traces.hasNext() ){
			++i;
			InteractionTrace trace = (InteractionTrace) traces.next();
			System.out.println("processing interaction trace of thread \"" + trace.getThreadId() + "("+ i + ")");

			// full traces
			NormalizedInteractionTraceHandler analyzedTraces;
			try {
				analyzedTraces = TraceHandlerFactory.getNormalizedInteractionTraceHandler();

				//MethodOutgoingTraceMaintainer tmpTraces = new MethodOutgoingTraceMaintainer(analyzedTraces);

				Token token = trace.getNextToken();
				String method = token.getTokenValue();
				int open =0;

				while (method != null) {
					int ml = method.length();
					if (method.charAt(ml-1) == 'B' ) {
						++open;
						

						//if needed add new trace
						
							String methodName = method.substring(0, ml - 1);
		
							String metaInfo = trace.getNextMetaData();
							
							//if needed add meta info
							try {
								metaHandler.handleInteractionBegin(methodName, metaInfo);
							} catch (TcInfoHandlerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						

					} else {
						--open;
						
						
						trace.getNextMetaData();
						
						
						
						
						
					}

					//FIXME bad code below
					token = trace.getNextToken();
					if(token != null)
						method = token.getTokenValue();
					
					else method = null;


				} //end of WHILE != null

				if ( open != 0 ){
					System.err.println("Error open "+open +" traces");
				}

				metaHandler.save();
				
			} catch (NormalizedTraceHandlerException e) {
				e.printStackTrace();
			} catch (TraceException e) {
				e.printStackTrace();
			} catch (TcInfoHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			trace.close();		

		} //end of WHILE traces.hasNext()

	}
}
