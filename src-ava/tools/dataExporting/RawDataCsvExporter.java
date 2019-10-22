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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import database.ConnectionDispenser;
import database.DBException;
import dfmaker.core.DaikonTraceProcessor;
import dfmaker.core.OptimizedDaikonTraceProcessor;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import traceReaders.raw.DBIoTrace;
import traceReaders.raw.TraceReaderFactory;
import traceReaders.raw.DBIoTrace.DBLineIterator;

public class RawDataCsvExporter {

	CsvDataWriter csvWriter = new CsvDataWriter();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File dest = new File(args[0]);
		
		RawDataCsvExporter dumper = new RawDataCsvExporter();
		try {
			dumper.dumpData(dest);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
				
//		
		
	}

	private void dumpData(File dest) throws SQLException, DBException, IOException {
		List<Integer> threads;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
		
		try {
			threads = getThreads();
			
			for ( Integer thread : threads ){
				dumpThreadData(thread,writer);
				csvWriter.writeTraceSeparator(writer);
			}
			
		
			
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		for ( Entry<String, HashMap<String, Integer>> entry : positionsMaps.entrySet() ){
//			System.out.print(entry.getKey());  //methodName
//			HashMap<String, Integer> positions = entry.getValue();
//			System.out.print(csvSeparator);
//			
//			for ( int i = 0; i < positions.size(); ++i ){
//				Integer columnsName = positions.get(Integer.valueOf(i));
//				System.out.print(columnsName);
//				System.out.print(csvSeparator);
//			}
//			
//			System.out.print("\n");
//		}
	}

	private void dumpThreadData(Integer thread, BufferedWriter writer) throws SQLException, DBException, IOException {
		
		PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement(
				"SELECT methodDeclaration, beginEnd, idBeginEndExecMethod, datadefinition FROM method as m, beginendexecmethod as b, thread as t, datum as d" +
				" WHERE m.idMethod = b.method_idMethod AND t.idThread = b.thread_idThread AND t.idThread = ? AND d.beginendexecmethod_idBeginEndExecMethod = b.idBeginEndExecMethod" +
				" ORDER BY occurrence ");
		
		
//				" SELECT methodDeclaration, idBeginEndExecMethod, beginEnd FROM method, beginendexecmethod WHERE idMethod = method_idMethod " +
//				" AND thread_idThread = ? " +
//				" ORDER BY occurrence ");	
			stmt.setInt(1, thread);
			
			System.out.println(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			rs.beforeFirst();
			
			while ( rs.next() ){
				String method = rs.getString(1);
				String methodBE = method+rs.getString(2);
				String dataDefinition = rs.getString(4);
				
				csvWriter.writeDataLine(writer, method, methodBE, dataDefinition);
				//System.out.println(dataDefinition);
				
				
				
			}
			
		
	}

	

	private static List<Integer> getThreads() throws SQLException, DBException {
		//TraceReaderFactory.getReader().getIoInteractionTraces();
		PreparedStatement stmt = ConnectionDispenser.getConnection().prepareStatement( "SELECT idThread FROM thread");
				//"SELECT methodDeclaration, beginEnd, idBeginEndExecMethod FROM method, beginendexecmethod, thread " +
				//"WHERE idMethod = method_idMethod AND idThread = thread_idThread AND idThread = ? " +
				//"ORDER BY occurrence ");
				
		ResultSet rs = stmt.executeQuery();
		
		rs.beforeFirst();
		
		List<Integer> res = new ArrayList<Integer>();
		while ( rs.next() ){
			int id = rs.getInt(1);
			res.add(id);
			
		}
		return res;
		
	}

}
