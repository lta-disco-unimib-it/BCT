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
package bctFaults;


import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import modelsViolations.BctRuntimeData;
import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tools.violationsAnalyzer.BctViolationsManager;
import tools.violationsAnalyzer.FailuresManager;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraph;
import tools.violationsAnalyzer.anomalyGraph.AnomalyGraphCreator;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTree;
import tools.violationsAnalyzer.dynamicCallTree.DynamicCallTreeCreator;
import tools.violationsAnalyzer.filteringStrategies.BctRuntimeDataFilterCorrectOut;
import tools.violationsAnalyzer.filteringStrategies.IdManagerAction;
import tools.violationsAnalyzer.filteringStrategies.IdManagerProcess;

/**
 * This test do not identify the problem in BUG 127.
 * The problem is related to the logging, the log contains the complete stack trace.
 * It just detects that the problem is not in the AnomalyGraph generator
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class Bug127Test extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	
	
	@Test
	public void testBug(){
		BctViolationsManager vm = new BctViolationsManager();
		FailuresManager fm = new FailuresManager();
		
		String[] st1 = new String[]{
				"javax.servlet.jsp.JspFactory.getDefaultFactory:75",
				"eltest.ChipsListener.contextInitialized:18",
				"org.apache.catalina.core.StandardContext.listenerStart:3827",
				"org.apache.catalina.core.StandardContext.start:4336",
				"org.apache.catalina.core.ContainerBase.addChildInternal:760",
				"org.apache.catalina.core.ContainerBase.addChild:740",
				"org.apache.catalina.core.StandardHost.addChild:525",
				"org.apache.catalina.startup.HostConfig.deployWAR:825",
				"org.apache.catalina.startup.HostConfig.deployWARs:714",
				"org.apache.catalina.startup.HostConfig.deployApps:490",
				"org.apache.catalina.startup.HostConfig.start:1138",
				"org.apache.catalina.startup.HostConfig.lifecycleEvent:311",
				"org.apache.catalina.util.LifecycleSupport.fireLifecycleEvent:120",
				"org.apache.catalina.core.ContainerBase.start:1022",
				"org.apache.catalina.core.StandardHost.start:719",
				"org.apache.catalina.core.ContainerBase.start:1014",
				"org.apache.catalina.core.StandardEngine.start:443",
				"org.apache.catalina.core.StandardService.start:451",
				"org.apache.catalina.core.StandardServer.start:710",
				"org.apache.catalina.startup.Catalina.start:552",
				"sun.reflect.NativeMethodAccessorImpl.invoke0:-2",
				"sun.reflect.NativeMethodAccessorImpl.invoke:39",
				"sun.reflect.DelegatingMethodAccessorImpl.invoke:25",
				"java.lang.reflect.Method.invoke:585",
				"org.apache.catalina.startup.Bootstrap.start:288",
				"org.apache.catalina.startup.Bootstrap.main:413",
		};
		
		BctIOModelViolation viol1 = new BctIOModelViolation(
				"1",
				"javax.servlet.jsp.JspFactory.getDefaultFactory(()Ljavax.servlet.jsp.JspFactory;):::EXIT",
				"returnValue != null",
				BctModelViolation.ViolationType.NOT_VALID,
				System.currentTimeMillis(),
				new String[0],
				new String[0],
				st1,
				"1",
				"1",
				"");
		
		String[] st2 = new String[]{
				"javax.servlet.jsp.JspFactory.&lt;init&gt;:-1",
				"org.apache.jasper.runtime.JspFactoryImpl.&lt;init&gt;:40",
				"org.apache.jasper.compiler.JspRuntimeContext.&lt;clinit&gt;:73",
				"org.apache.jasper.servlet.JspServlet.init:101",
				"org.apache.catalina.core.StandardWrapper.loadServlet:1161",
				"org.apache.catalina.core.StandardWrapper.load:981",
				"org.apache.catalina.core.StandardContext.loadOnStartup:4044",
				"org.apache.catalina.core.StandardContext.start:4350",
				"org.apache.catalina.core.ContainerBase.addChildInternal:760",
				"org.apache.catalina.core.ContainerBase.addChild:740",
				"org.apache.catalina.core.StandardHost.addChild:525",
				"org.apache.catalina.startup.HostConfig.deployDirectory:920",
				"org.apache.catalina.startup.HostConfig.deployDirectories:883",
				"org.apache.catalina.startup.HostConfig.deployApps:492",
				"org.apache.catalina.startup.HostConfig.start:1138",
				"org.apache.catalina.startup.HostConfig.lifecycleEvent:311",
				"org.apache.catalina.util.LifecycleSupport.fireLifecycleEvent:120",
				"org.apache.catalina.core.ContainerBase.start:1022",
				"org.apache.catalina.core.StandardHost.start:719",
				"org.apache.catalina.core.ContainerBase.start:1014",
				"org.apache.catalina.core.StandardEngine.start:443",
				"org.apache.catalina.core.StandardService.start:451",
				"org.apache.catalina.core.StandardServer.start:710",
				"org.apache.catalina.startup.Catalina.start:552",
				"sun.reflect.NativeMethodAccessorImpl.invoke0:-2",
				"sun.reflect.NativeMethodAccessorImpl.invoke:39",
				"sun.reflect.DelegatingMethodAccessorImpl.invoke:25",
				"java.lang.reflect.Method.invoke:585",
				"org.apache.catalina.startup.Bootstrap.start:288",
				"org.apache.catalina.startup.Bootstrap.main:413",

				
		};
		BctFSAModelViolation viol2 = new BctFSAModelViolation(
				"2",
				"org.apache.jasper.servlet.JspServlet.init((Ljavax.servlet.ServletConfig;)V)",
				"javax.servlet.jsp.JspFactory.<init>(()V)",
				BctModelViolation.ViolationType.UNEXPECTED_EVENT,
				System.currentTimeMillis(),
				new String[0],
				new String[0],
				st2,
				"1",
				"1",
				new String[]{"q10"});
		
		vm.addDatum(viol1);
		vm.addDatum(viol2);
		
		fm.addFailingProcess("1");
		
		BctRuntimeDataFilterCorrectOut fsc = new BctRuntimeDataFilterCorrectOut();
		List<BctModelViolation> viols = fsc.getFilteredData(vm, fm, IdManagerProcess.INSTANCE, "1");
		assertEquals(2, viols.size());
		
		DynamicCallTree dc = DynamicCallTreeCreator.createDynamicCallTree(viols);
		
		Set<BctModelViolation> mvs = dc.getModelViolations();
		
		assertEquals(2, mvs.size());
		
		assertEquals( 22, dc.getUndirectedViolationDistance(viol1, viol2));
		
		AnomalyGraph ag = AnomalyGraphCreator.createAnomalyGraph(dc);
		
		mvs = ag.getViolations();
		
		assertEquals(2, mvs.size());
		
		assertEquals( 22.0, ag.getEdgeWeigth(viol1, viol2), 0.001 );
	}
}
