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
package tools.dataExporting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import traceReaders.raw.DBIoTrace;
import traceReaders.raw.DBIoTrace.DBLineIterator;
import dfmaker.core.DaikonTraceProcessor;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;

public class CsvDataWriter {

	private HashMap<String,HashMap<String,Integer>> positionsMaps = new HashMap<String,HashMap<String,Integer>>();
	private String csvSeparator = ",";
	
	public HashMap<String, HashMap<String, Integer>> getPositionsMaps() {
		return positionsMaps;
	}
	
	void writeTraceSeparator(BufferedWriter writer) throws IOException {
		writer.append("|\n");
	}

	private void writeDataLine(BufferedWriter writer, String method, String methodBE,
			Map<Integer, String> positionsData) throws IOException {
		writer.append(method);
		writer.append(csvSeparator );
		writer.append(methodBE);
		writer.append(csvSeparator );
		for ( int i = 0; i < positionsData.size(); i++ ){
			String valueForPosition = positionsData.get(Integer.valueOf(i));
			if ( valueForPosition != null ){
				writer.append(valueForPosition);
			}
			writer.append(csvSeparator );	
		}
		writer.append("\n" );
	}

	
	private Map<Integer, String> mapDataToPosition(String method, Map<String, String> data) {
		Map<Integer,String> result = new HashMap<Integer, String>();
		
		for ( Entry<String,String> entry : data.entrySet() ){
			String parameterName = entry.getKey();
			String parameterValue = entry.getValue();
			
			Integer position = getPositionForParameter( method, parameterName );
			
			result.put(position, parameterValue);
		}
		
		return result;
	}

	private int getPositionForParameter(String method, String parameterName) {
		HashMap<String, Integer> positions = getPositionsMap(method);
		Integer position = positions.get(parameterName);
		
		if ( position == null ){
			position = Integer.valueOf( positions.size() );
			positions.put(parameterName, position);
		}
		
		return position;
	}

	private HashMap<String, Integer> getPositionsMap(String method) {
		HashMap<String, Integer> map = positionsMaps.get(method);
		if ( map == null ){
			map = new HashMap<String, Integer>();
			positionsMaps.put(method, map);
		}
		return map;
	}

	private Map<String, String> getData(String dataDefinition) throws DTraceListenerException, IOException {
		//System.out.println("GETTING DATA :"+dataDefinition+"\n|||");
		if ( dataDefinition.length() == 0 ){
			return new HashMap<String, String>();
		}
		
		BufferedReader reader = new BufferedReader(new StringReader(dataDefinition));
		DBLineIterator it = new DBIoTrace.DBLineIterator(reader);
		//while ( it.hasNext() ){
//			System.out.println("IT "+it.next());
//			System.out.println("IT "+it.next());
//			System.out.println("IT "+it.next());
//			System.out.println("IT "+it.next());
//		//}
		
		
		reader = new BufferedReader(new StringReader(dataDefinition));
		DumperDaikonTraceListener listener = new DumperDaikonTraceListener();
		DaikonTraceProcessor processor = new DaikonTraceProcessor(listener); 
		processor.process(it);
		
		return listener.getVarValues();
	}

	public void writeDataLine(BufferedWriter writer, String method,
			String methodBE, String dataDefinition) throws IOException {
		
		try {
			Map<String,String> data = getData(dataDefinition);
			Map<Integer,String> positionsData = mapDataToPosition(method+"."+methodBE,data);
			
			writeDataLine(writer,method,methodBE,positionsData);
		} catch (DTraceListenerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	public void writeColumnsDefinitions(BufferedWriter defWriter) throws IOException {
		defWriter.write("\"InvokingMethod.InvokedMethod\",\"Parameter\",\"Column\"\n");
		for ( Entry<String,HashMap<String,Integer>> callingMethodEntry : positionsMaps.entrySet() ){
			String callingMethod = callingMethodEntry.getKey();
			for ( Entry<String,Integer> columnEntry : callingMethodEntry.getValue().entrySet() ){
				defWriter.write("\"");
				defWriter.write(callingMethod);
				defWriter.write("\",\"");
				defWriter.write(columnEntry.getKey());
				defWriter.write("\",\"");
				defWriter.write(columnEntry.getValue().toString());
				defWriter.write("\"\n");
				
			}
		}
	}
	
}
