/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import theoremProver.Simplify;
import theoremProver.SimplifyResult;

public class SimplifyIoChecker extends IoChecker {

	@Override
	public boolean evaluateExpression(String expression,
			Object[] argumentValues, Object returnValue,
			Map<String, Object> localVariables) {
		
		
		ArrayList<String> preconditions = new ArrayList<String>();
		
		//We should use the flattener in principle
		
		for ( Entry<String,Object> localVar : localVariables.entrySet() ){
			String precondition = createPrecondition ( localVar.getKey(), localVar.getValue() );
			if ( precondition != null ){
				preconditions.add( precondition );
			}
		}
		
		Simplify simplify = new Simplify();
		
		try {
			ArrayList<SimplifyResult> result = simplify.isValid(preconditions, expression);
			
			if ( result.size() != 1 ){
				throw new RuntimeException("Unexpected number of results");
			}
			
			SimplifyResult res = result.get(0);
			
			if ( ! ( res.getResult() == SimplifyResult.VALID ) ){
				System.out.println(res.getProgramOutput());
			}
			
			if ( res.getResult() == SimplifyResult.INVALID ){
				return false;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	private String createPrecondition(String key, Object value) {
		
		int pos = key.indexOf('.');
		if ( pos > 0 ){
			key = "(select |"+key.substring(0,pos)+"| |"+key.substring(pos+1)+"|)";
		}
		
		if ( value == null ){
			return "(EQ "+key+" null)";
		}
		
		if ( value instanceof Number ){
			return "(EQ "+key+" "+value+")";
		}
		
		if ( value.equals("!NULL") ){
				return "(NEQ "+key+" null)";
			
		}
		
		if ( value instanceof String ){
			String sval = (String) value;
			if ( sval.startsWith("0x") ){
				return "(NEQ "+key+" null)";
			}
		}
		
		return null;
	}
		
		
	

}
