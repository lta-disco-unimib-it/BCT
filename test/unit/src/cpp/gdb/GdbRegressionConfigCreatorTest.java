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
package cpp.gdb;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.MatchingRuleInclude;
import cpp.gdb.GdbRegressionConfigCreator.Configuration;

public class GdbRegressionConfigCreatorTest {

	String folderMangled = "test/unit/artifacts/cpp/gdbRegressionConfigCreator/mangledNames/";
	
	@Ignore("FIXME: Demangled names are not properly implemented, and there no interst at the moment")
	@Test
	public void testWithDemangledNames() throws IOException {
		File componentsConfigFile = new File( folderMangled+"components.txt");
		File dump = new File( folderMangled+ "originalSoftware.objdump");
		
		File result = new File( folderMangled+ "result.gdb.monitoring.conf");

		
		GdbRegressionConfigCreator.main(new String[]{
				dump.getAbsolutePath(),
				componentsConfigFile.getAbsolutePath(),
				
				result.getAbsolutePath()
		});
		
		assertTrue( result.exists() );
		
		System.out.println(result.length());
		
		
		assertTrue( result.length() > 90000 );
		
		
		assertTrue( result.delete() );
		
	}
	
	
	
	@Test
	public void test() throws IOException {
		
		Configuration config = new Configuration();
		config.setMonitorIntraComponentCalls(true);
		config.setMonitorCallersOfModifiedFunctions(false);
		config.setMonitorFunctionsDefinedOutsideProject(false);
		config.setUseDemangledNames(false);
		
		GdbRegressionConfigCreator creator = new GdbRegressionConfigCreator(config);
		
		File objdumpFile = new File("test/unit/artifacts/cpp/gdbRegressionConfigCreator/simpleProject/originalSoftware.objdump");
		
		Collection<Component> components = new ArrayList<Component>();
		Component c = new Component();
		c.addRule(new MatchingRuleInclude(".*", ".*", ".*") );
		components.add(c);
		
		Collection<String> locationPrefixesToExclude = new ArrayList<String>();
		locationPrefixesToExclude.add("/home/fabrizio/Workspaces/workspaceTruncatedCallsTest/RecursiveNew/src");
		File resultFile = new File("test/unit/artifacts/cpp/gdbRegressionConfigCreator/simpleProject/createdConfig.txt"); 
		
		Set<FunctionMonitoringData> monitoredFunctions = creator.createConfig(objdumpFile, components , resultFile, locationPrefixesToExclude, null );
		
		assertTrue( monitoredFunctions.size() > 0 );
		
	}
	
	
	
	@Test
	public void testMonoitoringInternal() throws IOException {
		
		Configuration config = new Configuration();
		config.setMonitorIntraComponentCalls(true);
		config.setMonitorCallersOfModifiedFunctions(false);
		config.setMonitorFunctionsDefinedOutsideProject(false);
		config.setUseDemangledNames(false);
		config.setMonitorInternalLines(true);
		
		GdbRegressionConfigCreator creator = new GdbRegressionConfigCreator(config);
		
		File objdumpFile = new File("test/unit/artifacts/cpp/gdbRegressionConfigCreator/simpleProject/originalSoftware.objdump");
		
		Collection<Component> components = new ArrayList<Component>();
		Component c = new Component();
		c.addRule(new MatchingRuleInclude(".*", ".*", ".*") );
		components.add(c);
		
		Collection<String> locationPrefixesToExclude = new ArrayList<String>();
		locationPrefixesToExclude.add("/home/fabrizio/Workspaces/workspaceTruncatedCallsTest/RecursiveNew/src");
		File resultFile = new File("test/unit/artifacts/cpp/gdbRegressionConfigCreator/simpleProject/createdConfig.txt"); 
		
		Set<FunctionMonitoringData> monitoredFunctions = creator.createConfig(objdumpFile, components , resultFile, locationPrefixesToExclude, null );
		
		assertTrue( monitoredFunctions.size() > 0 );
		
	}
	

}
