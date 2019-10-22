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
package tools.gdbTraceParser;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import tools.violationsAnalyzer.ViolationsUtil;

import cpp.gdb.FunctionMonitoringData;

public class BctCheckingFilterTest {

	@Test
	public void testFilterSameViolationRepeated(){
		
		String function = "_ZN10WorkersMap9getSalaryESs";
		
		Map<String, FunctionMonitoringData> map = new HashMap<String, FunctionMonitoringData>();
		
		BctCheckingFilter filter = new BctCheckingFilter(map);
		
		filter.enterFunction(function);
		
		String expression1 = "x > y";
		String expression2 = "z > y";
		
		{
			

			Map<String, Object> localVariables = new HashMap<String, Object>();
			localVariables.put("x", 5);
			localVariables.put("y", 3);
			localVariables.put("z", 2);

			filter.newProgramPointIoData(localVariables);
			List<String> anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression1, localVariables );

			assertFalse( filter.isViolationPresentInPreviousLine(function+":12", expression1, localVariables, anomalousVariables ) );
			
			anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression2, localVariables );
			assertFalse( filter.isViolationPresentInPreviousLine(function+":12", expression2, localVariables, anomalousVariables ) );
		}
		

		
		{
			

			Map<String, Object> localVariables = new HashMap<String, Object>();
			localVariables.put("x", 5);
			localVariables.put("y", 3);
			localVariables.put("z", 3);

			filter.newProgramPointIoData(localVariables);
			List<String> anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression1, localVariables );

			assertTrue( filter.isViolationPresentInPreviousLine(function+":13", expression1, localVariables, anomalousVariables ) );
			
			anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression2, localVariables );
			assertFalse( filter.isViolationPresentInPreviousLine(function+":13", expression2, localVariables, anomalousVariables ) );
		}
		
		
		{
			Map<String, Object> localVariables = new HashMap<String, Object>();
			localVariables.put("x", 5);
			localVariables.put("y", 2);
			localVariables.put("z", 3);
			
			filter.newProgramPointIoData(localVariables);
			List<String> anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression1, localVariables );

			assertFalse( filter.isViolationPresentInPreviousLine(function+":14", expression1, localVariables, anomalousVariables ) );
			
			anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression2, localVariables );
			assertFalse( filter.isViolationPresentInPreviousLine(function+":14", expression2, localVariables, anomalousVariables ) );
		}
		
		{

			Map<String, Object> localVariables = new HashMap<String, Object>();
			localVariables.put("x", 5);
			localVariables.put("y", 2);
			localVariables.put("z", 3);
			
			filter.newProgramPointIoData(localVariables);
			List<String> anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression1, localVariables );

			assertTrue( filter.isViolationPresentInPreviousLine(function+":15", expression1, localVariables, anomalousVariables ) );
			
			anomalousVariables = ViolationsUtil.extractAnomalousVariables( expression2, localVariables );
			assertTrue( filter.isViolationPresentInPreviousLine(function+":15", expression2, localVariables, anomalousVariables ) );
		}
		
	}
}
