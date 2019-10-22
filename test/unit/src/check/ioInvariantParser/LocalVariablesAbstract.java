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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.primitives.UnsignedLong;

import conf.EnvironmentalSetter;

import testSupport.check.ioInvariantParser.MyClass;

public abstract class LocalVariablesAbstract {
	
	
	private boolean offline;

	public LocalVariablesAbstract(boolean offline){
		File bctHome = new File("test/unit/artifacts/BCT_HOME");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		this.offline = offline;
		IoInvariantParser.setOfflineAnalysis(offline);
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


	}
	
	
	@Test
	public void testReturn() throws InvariantParseException{
		Object parameters[] = new Object[2];

		parameters[0] = new MyClass();

		parameters[1] = 3;

		Object returnValue = new MyClass();
		
		checkReturnValue(parameters, returnValue, null);

		checkReturnValue(parameters, returnValue, new HashMap<String, Object>());
	}

	private void checkReturnValue(Object[] parameters, Object returnValue,
			Map<String, Object> locals) throws InvariantParseException {
		assertTrue( IoInvariantParser.evaluateExpression("returnValue != null", parameters, returnValue, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("returnValue == null", parameters, returnValue, locals ) );

		assertTrue( IoInvariantParser.evaluateExpression("returnValue.intValue == 4", parameters, returnValue, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("returnValue.intValue != 4", parameters, returnValue, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("returnValue.intValue == 5", parameters, returnValue, locals ) );
		assertTrue( IoInvariantParser.evaluateExpression("returnValue.intValue != 5", parameters, returnValue, locals ) );


		assertTrue( IoInvariantParser.evaluateExpression("returnValue.stringValue == \"stringa\"", parameters, returnValue, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("returnValue.stringValue != \"stringa\"", parameters, returnValue, locals ) );


		assertTrue( IoInvariantParser.evaluateExpression("returnValue.m2 != null", parameters, returnValue, locals ) );
		assertFalse( IoInvariantParser.evaluateExpression("returnValue.m2 == null", parameters, returnValue, locals  ) );
	}



	
//	(*this != null)  ==>  (*this.val one of { 0, 5 })
	
}
