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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;

public class FunctionMonitoringDataSerializerTest {

	@Test
	public void testStoreHugeData() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		Collection<FunctionMonitoringData> data = new ArrayList<FunctionMonitoringData>();
		
		for ( int i = 0; i < 1000; i ++ ){
			FunctionMonitoringData funcData = new FunctionMonitoringData("function_"+i);
			funcData.addAddressExit("0x2");
			
			data.add(funcData);
		}
		
		for ( FunctionMonitoringData fLeft : data ){
			for ( FunctionMonitoringData fRigth : data ){
				if ( ! fRigth.getMangledName().endsWith("9" ) ){
					fLeft.addCallee(fRigth);
					fLeft.addCaller(fRigth, "0x3");
				}
			}	
		}
		
		
		
		FunctionMonitoringDataSerializer.store(data, new File("test/unit/artifacts/functionMonitoringDataBig.ser"));
		
		Map<String, FunctionMonitoringData> loadedData = FunctionMonitoringDataSerializer.load( new File("test/unit/artifacts/functionMonitoringDataBig.ser"));
		
		
		
		
		
		
		
		FunctionMonitoringData function_1 = loadedData.get("function_1");
		
		assertNotNull(function_1);
		assertTrue( function_1.getCallers().contains(function_1) );
		
		
		FunctionMonitoringData function_9 = loadedData.get("function_9");
		
		assertNotNull(function_9);
		assertFalse( function_9.getCallers().contains(function_9) );
		assertTrue( function_9.getCallers().contains(function_1) );
	}

}
