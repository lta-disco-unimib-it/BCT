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

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import conf.EnvironmentalSetter;

import testSupport.check.ioInvariantParser.MyClass;

public class IoParserTest {

	@BeforeClass
	public static void setupStatic(){
		File bctHome = new File("test/unit/artifacts/BCT_HOME");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		IoInvariantParser.setOfflineAnalysis(false);
	}

	@Test
	public void testParameters() throws InvariantParseException{
		Object parameters[] = new Object[2];

		parameters[0] = new MyClass();

		parameters[1] = 3;

		checkParameters(parameters, null);

		checkParameters(parameters, new HashMap<String, Object>());
	}

	public void checkParameters(Object[] parameters, Map<String, Object> locals) throws InvariantParseException{

		assertTrue( IoInvariantParser.evaluateExpression("parameter[0] != null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[0] == null", parameters, null, locals ) );

		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].intValue == 4", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[0].intValue != 4", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[0].intValue == 5", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].intValue != 5", parameters, null, locals ) );


		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].stringValue == \"stringa\"", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[0].stringValue != \"stringa\"", parameters, null, locals ) );


		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].m2 != null", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[0].m2 == null", parameters, null, locals ) );

		assertTrue( IoInvariantParser.evaluateExpression("parameter[1] == 3", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[1].intValue() == 3", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[1] != 3", parameters, null, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("parameter[1].intValue() != 3", parameters, null, locals ) );

		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a != parameter[0].intValue", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a == parameter[0].intValue", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a > parameter[0].intValue", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a < parameter[0].intValue", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a >= parameter[0].intValue", parameters, null, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("parameter[0].a <= parameter[0].intValue", parameters, null, locals ) );
	}

}
