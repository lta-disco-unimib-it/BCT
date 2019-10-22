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
package check.ioInvariantParser;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import conf.EnvironmentalSetter;

import dfmaker.core.Variable;

import tools.gdbTraceParser.BctGdbCheckingThreadTraceListener;

public class LocalVariablesWithWrappingTest {

	
	@BeforeClass
	public static void setupStatic(){
		File bctHome = new File("test/unit/artifacts/BCT_HOME");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		IoInvariantParser.setOfflineAnalysis(true);
	}
	
	
	@Test
	public void testBug() throws InvariantParseException {
		
		
		List<Variable> localVars = new ArrayList<Variable>();
		localVars.add( new Variable("a.field", "5", 1) );
		localVars.add( new Variable("b.var", "7", 1) );
		
		Map<String, Object> locals = BctGdbCheckingThreadTraceListener.createLocalVariablesMap(new ArrayList<Variable>(), localVars, new ArrayList<Variable>(), new ArrayList<Variable>());
		
		
		
		boolean result = IoInvariantParser.evaluateExpression("a != b", null, null, locals );
		assertTrue(result);
	}
}
