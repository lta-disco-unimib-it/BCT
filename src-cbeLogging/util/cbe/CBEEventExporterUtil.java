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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctModelViolation;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.CompletionException;
import org.eclipse.tptp.logging.events.cbe.ComponentIdentification;
import org.eclipse.tptp.logging.events.cbe.EventFactory;
import org.eclipse.tptp.logging.events.cbe.EventFactoryHome;
import org.eclipse.tptp.logging.events.cbe.ReportSituation;
import org.eclipse.tptp.logging.events.cbe.Situation;
import org.eclipse.tptp.logging.events.cbe.impl.EventXMLFileEventFactoryHomeImpl;

public class CBEEventExporterUtil {

	public static CommonBaseEvent createGenericEvent( BctRuntimeData modelViolation )  {
		
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
		
		commonBaseEvent.setCreationTimeAsLong(modelViolation.getCreationTime());
		
		
		String[] stackTraceElements = modelViolation.getStackTrace();
		
		if ( stackTraceElements == null ){
			stackTraceElements = new String[]{"null:-1"};
		}
		
		String applicationName = stackTraceElements[stackTraceElements.length-1].split(":")[0]; //use the main method class (hoping we are in main)
		
		int stackStart;
		if ( modelViolation instanceof BctFSAModelViolation ){
			stackStart = 1;
		} else {
			stackStart = 0;
		}
		
		if ( stackStart >= stackTraceElements.length ){ //TODO: remove this, is an hack for debugging
			stackStart = stackTraceElements.length -1 ;
		}
		//TODO: use component definition to save the component name instead of the method called
		String subcomponent = stackTraceElements[stackStart];
		
		String environment = System.getProperty("os.name")+"_"+System.getProperty("os.arch")+"#"+System.getProperty("os.version");
		
		
		
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
        cid.setProcessId(modelViolation.getPid());
        cid.setThreadId(String.valueOf(modelViolation.getThreadId()));
		
		commonBaseEvent.setSourceComponentId(cid);

		cid = EventFactory.eINSTANCE.createComponentIdentification();
		
		cid.setApplication("BCT");
        cid.setComponent("BCT");
        cid.setSubComponent(CBEEventExporterUtil.class.getCanonicalName());
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
        cid.setProcessId(ManagementFactory.getRuntimeMXBean().getName());
        cid.setThreadId(String.valueOf(Thread.currentThread().getId()));
		
		commonBaseEvent.setReporterComponentId(cid);
		
		
		//Complete the event:
		if (!eventFactory.getCompleteEvent()) {

			try {
				commonBaseEvent.complete();
			}
			catch (CompletionException c) {
				c.printStackTrace();
			}
		}


		//Save stackTrace info
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.STACK_TRACE, stackTraceElements);
		
		//Save current actions
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.CURRENT_ACTIONS, modelViolation.getCurrentActions());
		
		//Save current test
		commonBaseEvent.addExtendedDataElement(CBEExtendedDataElements.CURRENT_TESTS, modelViolation.getCurrentTests());
		
		commonBaseEvent.setGlobalInstanceId(modelViolation.getId());
		
		return commonBaseEvent; 
		

	}
	
}
