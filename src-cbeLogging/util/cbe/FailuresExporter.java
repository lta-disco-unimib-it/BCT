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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.CompletionException;
import org.eclipse.tptp.logging.events.cbe.ComponentIdentification;
import org.eclipse.tptp.logging.events.cbe.EventFactory;
import org.eclipse.tptp.logging.events.cbe.EventFactoryHome;
import org.eclipse.tptp.logging.events.cbe.ReportSituation;
import org.eclipse.tptp.logging.events.cbe.Situation;
import org.eclipse.tptp.logging.events.cbe.impl.EventXMLFileEventFactoryHomeImpl;

import util.RuntimeContextualDataUtil;
import executionContext.ActionsRegistryException;
import executionContext.TestCaseData;
import executionContext.TestCasesRegistry;
import executionContext.TestCasesRegistryFactory;
import failureDetection.ExceptionFailure;
import failureDetection.Failure;

public class FailuresExporter {

	private static final String BCT_FAILURE_MSG = "Failure detected by BCT";

	public CommonBaseEvent createFailureEvent( Failure failure )  {
		CommonBaseEvent cbe = createGenericFailureEvent(failure);
		
		if ( failure instanceof ExceptionFailure ){
			addExceptionElements(cbe,(ExceptionFailure)failure);
		}
		
		return cbe;
	}
	
	private void addExceptionElements(CommonBaseEvent commonBaseEvent,
			ExceptionFailure failure) {
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.STACK_TRACE, failure.getStackTrace());
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.CATCHING_METHOD, failure.getCatchingMethod());
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.EXCEPTION_CLASS, failure.getExceptionClass());
		if ( failure.getExceptionMsg() != null ){
			commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.EXCEPTION_MSG, failure.getExceptionMsg());
		}
	}

	private CommonBaseEvent createGenericFailureEvent( Failure failure )  {
		
		EventFactoryHome eventFactoryHome = new EventXMLFileEventFactoryHomeImpl();
		
		EventFactory eventFactory = eventFactoryHome.getAnonymousEventFactory();
		ReportSituation reportSituation = eventFactory.createReportSituation();
		reportSituation.setReasoningScope("INTERNAL");
		reportSituation.setReportCategory("LOG");
		
		
		//Create a new instance of a situation:
		Situation situation = eventFactory.createSituation();
		situation.setCategoryName("ReportSituation");
		situation.setSituationType(reportSituation);

		//Create a new instance of a Common Base Event:
		CommonBaseEvent commonBaseEvent = eventFactory.createCommonBaseEvent();
		commonBaseEvent.setSituation(situation);
		short severity;
		if ( failure.isCritical() ){
			severity = 60;
		} else {
			severity = 50;
		}
		commonBaseEvent.setSeverity(severity);
		
		commonBaseEvent.setCreationTimeAsLong(failure.getDetectionTime());
		
		commonBaseEvent.setMsg(BCT_FAILURE_MSG);
		
		commonBaseEvent.setGlobalInstanceId(failure.getId());
		
		String applicationName = RuntimeContextualDataUtil.retrieveApplicationName();
		
		
		String subcomponent = failure.getFailingComponent();
		
		String environment = RuntimeContextualDataUtil.getEnvironmentalInfoString();
		
		commonBaseEvent.setLocalInstanceId(failure.getId());
	
		{
		ComponentIdentification cid = EventFactory.eINSTANCE.createComponentIdentification();
		
		cid.setApplication(applicationName);
        cid.setComponent(applicationName);
        cid.setSubComponent(subcomponent);
        cid.setComponentType("Application");
        cid.setComponentIdType("Application");
        cid.setExecutionEnvironment(environment);
        cid.setInstanceId(null);
        
        try {
			cid.setLocation(InetAddress.getLocalHost().toString());
		} catch (UnknownHostException e) {
			cid.setLocation("0.0.0.0");
		}
        cid.setLocationType("IPV4");
        cid.setProcessId(failure.getFailingPID());
        cid.setThreadId(String.valueOf(failure.getFailingThreadId()));
		
		commonBaseEvent.setSourceComponentId(cid);
	}

		{
		ComponentIdentification cid = EventFactory.eINSTANCE.createComponentIdentification();
		
		cid.setApplication("BCT");
        cid.setComponent("BCT");
        cid.setSubComponent(getClass().getCanonicalName());
        cid.setComponentType("Application");
        cid.setComponentIdType("Application");
        cid.setExecutionEnvironment(environment);
        cid.setInstanceId(null);
        try {
			cid.setLocation(InetAddress.getLocalHost().toString());
		} catch (UnknownHostException e) {
			cid.setLocation("0.0.0.0");
		}
        cid.setLocationType("IPV4");
        cid.setProcessId(RuntimeContextualDataUtil.retrievePID());
        cid.setThreadId(RuntimeContextualDataUtil.retrieveThreadId());
		
		commonBaseEvent.setReporterComponentId(cid);
		
		
	}

		//Save current actions
		String[] activeActions = failure.getActiveActionsIds();
		if (activeActions != null ){
			commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.CURRENT_ACTIONS, activeActions);
		}
		
		//Save current test
		String[] activeTests = failure.getActiveTestsIds();
		if ( activeTests != null ){
			commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.CURRENT_TESTS, activeTests );
		}
		
		String testId = failure.getFailingTestId();
		if ( testId != null && ! testId.isEmpty()){
			
			TestCasesRegistry tcr = TestCasesRegistryFactory.getExecutionContextRegistry();
			
				TestCaseData data;
				try {
					data = tcr.getExecutionContextData(Integer.valueOf(testId));
					//Save current test
					commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.FAILING_TEST_ID, data.getTestCaseName() );
					commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.FAILING_TEST_NAME, data.getTestCaseName()  );
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ActionsRegistryException e) {
					e.printStackTrace();
				}
			
		}
		
		String actionId = failure.getFailingActionId();
		if ( actionId != null ){
			commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.FAILING_ACTION_ID, actionId );
		}
		
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.FAILURE_CLASS, failure.getClass().getCanonicalName() );
		
		//Complete the event:
		if (!eventFactory.getCompleteEvent()) {

			try {
				commonBaseEvent.complete();
			}
			catch (CompletionException c) {
				c.printStackTrace();
			}
		}
		
		return commonBaseEvent; 
		

	}
	
	
	public Failure loadFailureEvent( CommonBaseEvent commonBaseEvent )  {
		
		if ( ! isFailure(commonBaseEvent) ){
			return null;
		}
		
		
		
		Map<String,Object> extendedElementsMap = CBEUtil.getExtendedMap(commonBaseEvent);
		
		String failureCLass = (String) extendedElementsMap.get(CBEExtendedDataElements.FAILURE_CLASS);
		
		ComponentIdentification cid = commonBaseEvent.getSourceComponentId();
		String failingComponent = cid.getSubComponent();
		String pid = cid.getProcessId();
		Integer threadId = Integer.valueOf(cid.getThreadId());
		
		String id = commonBaseEvent.getGlobalInstanceId();
		if ( id == null || ! id.contains("@")){
			id = pid+"@"+commonBaseEvent.getLocalInstanceId();
		}
		
		long time = commonBaseEvent.getCreationTimeAsLong();
		
		boolean fatal;
		if ( commonBaseEvent.getSeverity() == 60 ){
			fatal = true;
		}
		
		if ( failureCLass.equals(ExceptionFailure.class.getCanonicalName() ) ){
			return loadExceptionFailure(commonBaseEvent,extendedElementsMap,id,time,failingComponent,threadId);
		} else {
			return loadGenericFailure(commonBaseEvent,extendedElementsMap,id,time,failingComponent,threadId);
		}
		
		
				

	}

	public Failure loadExceptionFailure(CommonBaseEvent commonBaseEvent,
			Map<String, Object> extendedElementsMap, String id, long time,
			String failingComponent, Integer threadId) {
		String[] stackTraceElements = (String[]) extendedElementsMap.get(CBEExtendedDataElements.STACK_TRACE);
		
		String exceptionClass = (String) extendedElementsMap.get(CBEExtendedDataElements.EXCEPTION_CLASS);
		String exceptionMsg = (String) extendedElementsMap.get(CBEExtendedDataElements.EXCEPTION_MSG);
		String catchingMethod = (String) extendedElementsMap.get(CBEExtendedDataElements.CATCHING_METHOD);
		
		
		ExceptionFailure f = new ExceptionFailure(
				id,
				time,
				catchingMethod,
				threadId,
				exceptionClass, 
				exceptionMsg, 
				stackTraceElements,
				catchingMethod
		);
		
		setGenericFailureElements(commonBaseEvent, extendedElementsMap, f);
		
		return f;
		
	}

	private Failure loadGenericFailure(CommonBaseEvent cbe, Map<String, Object> extendedElementsMap, String id, long detectionTime, String failingComponent, long failingThreadId ) {
		Failure f = new Failure(id, detectionTime, failingComponent, failingThreadId );
		
		setGenericFailureElements( cbe, extendedElementsMap, f );
		
		return f;
		
	}

	private void setGenericFailureElements(
			CommonBaseEvent cbe, Map<String, Object> extendedElementsMap, Failure f) {
		
		String actions[] = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_ACTIONS);
		
		String tests[] = (String[]) extendedElementsMap.get(CBEExtendedDataElements.CURRENT_TESTS );
		
		String testId = (String) extendedElementsMap.get(CBEExtendedDataElements.FAILING_TEST_ID );
		
		String actionId = (String) extendedElementsMap.get(CBEExtendedDataElements.FAILING_ACTION_ID );
		
		f.setActiveActionsIds(actions);
		
		f.setActiveTestsIds(tests);
		
		f.setFailingTestId(testId);
		
		f.setFailingActionId(actionId);
		
		f.setFailingPID(cbe.getSourceComponentId().getProcessId());
		
	}

	public boolean isFailure(CommonBaseEvent commonBaseEvent) {
		return BCT_FAILURE_MSG.equals(commonBaseEvent.getMsg());
	}
	
}
