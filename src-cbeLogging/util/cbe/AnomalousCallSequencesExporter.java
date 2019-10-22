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
package util.cbe;

import java.util.Map;

import modelsViolations.BctAnomalousCallSequence;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.ComponentIdentification;


public class AnomalousCallSequencesExporter {

	private static final String BCT_ANOMALOUS_SEQUENCE_KEY_ID = "BCTANOMALOUSSEQUENCE-";
	
	private static final String BCT_ANOMALOUS_CALL_SEQUENCE_MSG = "BCT Anomalous Call Sequence";
	
	public void addAnomalousCallSequenceInfo(
			BctAnomalousCallSequence modelViolation,
			CommonBaseEvent commonBaseEvent) {
		
		commonBaseEvent.setSeverity(((short) (10))); //just a report
		
		commonBaseEvent.setLocalInstanceId(BCT_ANOMALOUS_SEQUENCE_KEY_ID+modelViolation.getId());
		
		commonBaseEvent.setMsg(BCT_ANOMALOUS_CALL_SEQUENCE_MSG);
		
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATED_MODEL, modelViolation.getViolatedModel() );
		
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATED_MODEL_TYPE, modelViolation.getViolatedModelType().toString() );
		
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.ANOMALOUS_CALL_SEQUENCE, modelViolation.getAnomalousCallSequence());
	}


	public CommonBaseEvent createCBEAnomalousCallSequence(
			BctAnomalousCallSequence anomalousCallSequence) {
	
		
		CommonBaseEvent commonBaseEvent = CBEEventExporterUtil.createGenericEvent(anomalousCallSequence);
		addAnomalousCallSequenceInfo( anomalousCallSequence, commonBaseEvent );
		
		
		return commonBaseEvent; 
		

	}
	
	public BctAnomalousCallSequence loadAnomalousCallSequence ( CommonBaseEvent commonBaseEvent ){

		
		long creationTime = commonBaseEvent.getCreationTimeAsLong();
			
		
		ComponentIdentification cid = commonBaseEvent.getSourceComponentId();
		
		String threadId = cid.getThreadId();
		
		String pid = cid.getProcessId();
		
		String id = commonBaseEvent.getGlobalInstanceId();
		if ( id == null || ! id.contains("@")){
			id = pid+"@"+commonBaseEvent.getLocalInstanceId().substring(BCT_ANOMALOUS_SEQUENCE_KEY_ID.length());
		}
		
		
		
		Map<String,Object> extendedElementsMap = CBEUtil.getExtendedMap(commonBaseEvent);
		
		
		
		String[] stackTrace = (String[]) extendedElementsMap.get(CBEExtendedDataElements.STACK_TRACE);

		String[] currentTests = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_TESTS);
		
		String[] currentActions = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_ACTIONS);
	
		String[] anomalousCallSequence = (String[]) extendedElementsMap.get(CBEExtendedDataElements.ANOMALOUS_CALL_SEQUENCE);
		
		String violatedModel = (String) extendedElementsMap.get(CBEExtendedDataElements.VIOLATED_MODEL);
		
		return new BctAnomalousCallSequence(creationTime, id, pid,
				stackTrace, currentActions, currentTests, threadId, anomalousCallSequence, violatedModel );
	}


	public boolean isAnomalousCallSequence(CommonBaseEvent commonBaseEvent) {
		return BCT_ANOMALOUS_CALL_SEQUENCE_MSG.equals(commonBaseEvent.getMsg());
	}
}
