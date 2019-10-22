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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import modelsViolations.BctAnomalousCallSequence;
import modelsViolations.BctRuntimeData;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.CompletionException;
import org.eclipse.tptp.logging.events.cbe.ComponentIdentification;
import org.eclipse.tptp.logging.events.cbe.EventFactory;
import org.eclipse.tptp.logging.events.cbe.EventFactoryHome;
import org.eclipse.tptp.logging.events.cbe.ExtendedDataElement;
import org.eclipse.tptp.logging.events.cbe.FormattingException;
import org.eclipse.tptp.logging.events.cbe.ReportSituation;
import org.eclipse.tptp.logging.events.cbe.Situation;
import org.eclipse.tptp.logging.events.cbe.impl.EventXMLFileEventFactoryHomeImpl;


public class ModelViolationsExporter {
	
	private static final String BCT_MODEL_VIOLATION_KEY_ID = "BCTMODELVIOLATION-";

	
	private static final String BCT_MODEL_VIOLATION_MSG = "BCT Behavioral Model Violation";

	
	
	private CommonBaseEvent createGenericViolationEvent( BctRuntimeData modelViolation )  {
	
		
		//CommonBaseEvent commonBaseEvent = org.eclipse.tptp.logging.events.cbe.EventFactory.eINSTANCE.createCommonBaseEvent();
		
		CommonBaseEvent commonBaseEvent = CBEEventExporterUtil.createGenericEvent(modelViolation);
		
		addModelViolationInfo( (BctModelViolation) modelViolation, commonBaseEvent );
		
		
		return commonBaseEvent; 
		

	}



	private void addModelViolationInfo(BctModelViolation modelViolation,
			CommonBaseEvent commonBaseEvent) {
		
		commonBaseEvent.setSeverity(((short) (30))); //violation is a warning
		
		commonBaseEvent.setMsg(BCT_MODEL_VIOLATION_MSG);
		
		commonBaseEvent.setLocalInstanceId(BCT_MODEL_VIOLATION_KEY_ID+modelViolation.getId());
		

		
		//violated model
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATED_MODEL, modelViolation.getViolatedModel());
		
		//violated model type
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATED_MODEL_TYPE, modelViolation.getViolatedModelType().toString() );
		
		//violation
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATION, modelViolation.getViolation() );
		
		//violationType
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.VIOLATION_TYPE, modelViolation.getViolationType());
	}

	/**
	 * Creates a CBE with the info of the given IO model violation
	 * 
	 * @param modelViolation
	 * @return
	 */
	public CommonBaseEvent createCBEIoViolation(BctIOModelViolation modelViolation) {
		
		CommonBaseEvent event = createGenericViolationEvent(modelViolation);
		
		event.addExtendedDataElement(CBEExtendedDataElements.PARAMETERS_STATES,modelViolation.getParameters());
		
		return event;
	}

	/**
	 * Creates a CBE with the info of the given FSA violation
	 * 
	 * @param modelViolation
	 * @return
	 */
	public CommonBaseEvent createCBEFSAModelViolation(BctFSAModelViolation modelViolation) {
		CommonBaseEvent event = createGenericViolationEvent(modelViolation);
		event.addExtendedDataElement(CBEExtendedDataElements.CURRENT_STATES, modelViolation.getViolationStatesNames());
		
		if ( modelViolation.getDestinationStateName() != null ){
			event.addExtendedDataElement(CBEExtendedDataElements.DESTINATION_STATE, modelViolation.getDestinationStateName());
		}
		
		if ( modelViolation.getUnexpectedSequence() != null ) {
			event.addExtendedDataElement(CBEExtendedDataElements.UNEXPECTED_SEQUENCE, modelViolation.getUnexpectedSequence() );
		}
		
		return event;
	}

	/**
	 * This method returns true if the given object is a common base event
	 * 
	 * @param cbe
	 * @return
	 */
	public boolean isModelViolation( CommonBaseEvent cbe ){
		return BCT_MODEL_VIOLATION_MSG.equals(cbe.getMsg());
	}
	
	public BctModelViolation loadViolation( CommonBaseEvent commonBaseEvent ){

		if (! isModelViolation(commonBaseEvent) )
			return null;
		
		
		long creationTime = commonBaseEvent.getCreationTimeAsLong();
		
		
		ComponentIdentification cid = commonBaseEvent.getSourceComponentId();
		
		String threadId = cid.getThreadId();
		
		String pid = cid.getProcessId();
		
		String id = commonBaseEvent.getGlobalInstanceId();
		if ( id == null || ! id.contains("@")){ //Backward Compatibility
			id = pid+"@"+commonBaseEvent.getLocalInstanceId().substring(BCT_MODEL_VIOLATION_KEY_ID.length());
		}
		
		Map<String,Object> extendedElementsMap = CBEUtil.getExtendedMap(commonBaseEvent);
		
		
		
		String[] stackTraceElements = (String[]) extendedElementsMap.get(CBEExtendedDataElements.STACK_TRACE);
		
		String actions[] = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_ACTIONS);
		
		String tests[] = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_TESTS );
		
		String model = (String) extendedElementsMap.get( CBEExtendedDataElements.VIOLATED_MODEL) ;
		
		String modelType = (String) extendedElementsMap.get( CBEExtendedDataElements.VIOLATED_MODEL_TYPE) ;
		
		String violation = (String) extendedElementsMap.get( CBEExtendedDataElements.VIOLATION) ;
		
		String violationType = (String) extendedElementsMap.get( CBEExtendedDataElements.VIOLATION_TYPE );
		
		String destinationState =  (String) extendedElementsMap.get( CBEExtendedDataElements.DESTINATION_STATE );
		
		String[] unexpectedSequence =  (String[]) extendedElementsMap.get( CBEExtendedDataElements.UNEXPECTED_SEQUENCE);
		
		String anomalousEventPositionString = (String) extendedElementsMap.get( CBEExtendedDataElements.ANOMALOUS_EVENT_POSITION);
		int anomalousEventPosition;
		if ( anomalousEventPositionString != null ){
			anomalousEventPosition = Integer.valueOf(anomalousEventPositionString);
		} else {
			anomalousEventPosition = -1;
		}
		
		if ( modelType.equals(BctModelViolation.ViolatedModelsTypes.FSA.toString() ) ){
			return createFSAViolation(commonBaseEvent,extendedElementsMap,id, creationTime, pid, threadId, stackTraceElements,actions,tests,model,modelType,violation,violationType,unexpectedSequence,destinationState,anomalousEventPosition);
		} else if ( modelType.equals(BctModelViolation.ViolatedModelsTypes.IO.toString() ) ){
			return createIOViolation(commonBaseEvent,extendedElementsMap,id,creationTime, pid, threadId, stackTraceElements,actions,tests,model,modelType,violation,violationType);
		}
		
		return null; 
	}

	

	private BctIOModelViolation createIOViolation(
			CommonBaseEvent commonBaseEvent, Map<String, Object> extendedElementsMap, String id, long time, String pid, String threadId, String[] stackTraceElements,
			String[] actions, String[] tests, String model, String modelType,
			String violation, String violationType) {
		
		String parametersStates = (String) extendedElementsMap.get(CBEExtendedDataElements.PARAMETERS_STATES);
		
		return new BctIOModelViolation(
				id,
				model,
				violation,
				violationType,
				time,
				actions,
				tests,
				stackTraceElements,
				pid,
				threadId,
				parametersStates
		);
	}

	private BctModelViolation createFSAViolation(
			CommonBaseEvent commonBaseEvent, Map<String, Object> extendedElementsMap, String id, long creationTime, String pid, String threadId, String[] stackTraceElements,
			String[] actions, String[] tests, String model, String modelType,
			String violation, String violationType, String[] unexpectedSequence, String destinationState, int anomalousEventPosition) {
		
		String states[] = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_STATES);
		
		return new BctFSAModelViolation(
				id,
				model,
				violation,
				violationType,
				creationTime,
				actions,
				tests,
				stackTraceElements,
				pid,
				threadId,
				states,
				unexpectedSequence,
				destinationState, 
				anomalousEventPosition
		);
	}

	private String[] getStringArray(List<ExtendedDataElement> list) {
		
		String[] elements = new String[list.size()];
		for ( int i = 0; i < list.size(); ++i ){
			elements[i] = list.get(i).getValuesAsString();
		}
		
		return elements;
	}

	public static void main( String args[] ){
		File file = new File( args[0]);
		
		CBELogLoader loader = new CBELogLoader();
		try {
			CommonBaseEvent[] entities = loader.loadCBE(file);
			ModelViolationsExporter exporter = new ModelViolationsExporter();
			for ( CommonBaseEvent commonBaseEvent : entities ){
				exporter.loadViolation(commonBaseEvent);
			}
		} catch (FormattingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
