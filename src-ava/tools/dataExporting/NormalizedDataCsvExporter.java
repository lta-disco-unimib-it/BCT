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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import database.ConnectionDispenser;
import database.DBException;

public class NormalizedDataCsvExporter {

	private CsvDataWriter csvWriter = new CsvDataWriter();
	private boolean addCallContextData = true;
	private boolean recordExitData = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if ( args.length != 2 ){
			printHelp();
			System.exit(-1);
		}
		
		
		if ( "--help".equals(args[0]) ){
			printHelp();
			System.exit(-1);
		}
		
		File dest = new File(args[0]);
		File definition = new File(args[1]);
		
		NormalizedDataCsvExporter dumper = new NormalizedDataCsvExporter();
		try {
			dumper.dumpData(dest,definition);
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

	private static void printHelp() {
		System.out.println("Usage:\n" +
				NormalizedDataCsvExporter.class.getCanonicalName()+" definitionFile.csv dataFile.csv \n" +
						"\tdefinitionFile.csv is the name of the file that will" +
						"contain for each parameter of the exported methods " +
						"the corresponding column in of the final csv.\n" +
						"\tdataFile.csv is the name of the csv file that will contain for each exported method the " +
						"values of its parameters.\n" +
						"\n");
	}

	/**
	 * This method exports the values associated to each method to dataFile and writes in definitionFile the 
	 * correspondences between columns and parameters.
	 *   
	 * @param dataFile
	 * @param definitionFile
	 * @throws SQLException
	 * @throws DBException
	 * @throws IOException
	 */
	private void dumpData(File dataFile, File definitionFile) throws SQLException, DBException, IOException {
		List<Integer> threads;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
		
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

		BufferedWriter defWriter = new BufferedWriter(new FileWriter(definitionFile));
		try{	
			csvWriter.writeColumnsDefinitions(defWriter);
		} finally {
			try {
				defWriter.close();
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
			
			LinkedList<List<String[]>> stack = new LinkedList<List<String[]>>();
			LinkedList<String> callers = new LinkedList<String>();
			
			while ( rs.next() ){
				String method = rs.getString(1);
				String methodBE = method+rs.getString(2);
				String dataDefinition = rs.getString(4);
				
				
				//System.out.println("SEQUENCE : "+method+" "+methodBE+" "+rs.getInt(3));
				
				
				if ( isEnter(methodBE) ){
					
					List<String[]> top = stack.peek();
					String caller = callers.peek();
					
					if ( top != null ){
						top.add(new String[]{caller,method+"B",dataDefinition});
					}
					
					callers.push(method);
					
					LinkedList<String[]> calls = new LinkedList<String[]>();
					if ( addCallContextData  ){
						calls.add(new String[]{method,"()B",dataDefinition});
					}
					stack.push(calls);
					
//					top = stack.peek();
//					String caller = callers.peek();
//					if ( top != null ){
//						top.add(new String[]{caller,method,dataDefinition});
//					}	
				} else {
					List<String[]> top = stack.pop();
					String invokedMethod = callers.pop();
					
					if ( top != null ){
						
						//if we record exit information and keep trace of the calling context (
						if ( recordExitData && addCallContextData  ){
							top.add(new String[]{invokedMethod,"()E",dataDefinition});
						}
						
						writeData(writer,top);
						
						if ( recordExitData ){
							String invokingMethod = callers.peek();
							//retrieve the list of method invoked by the invoking method
							List<String[]> invokingCallList = stack.peek();
							if ( invokingCallList != null){
								invokingCallList.add(new String[]{invokingMethod,invokedMethod+"E",dataDefinition});
							}
							
							
						}
					}
					
					
				}
				
				
				
				//System.out.println(dataDefinition);
				
				
				
			}

	}

	

	private void writeData(BufferedWriter writer, List<String[]> top) throws IOException {
		System.out.println("Writing ");
		for ( String[] invocation : top ){
			//System.out.println(invocation[1]);
			csvWriter.writeDataLine(writer, invocation[0], invocation[1], invocation[2]);
		}
		csvWriter.writeTraceSeparator(writer);
	}

	private boolean isEnter(String methodBE) {
		return methodBE.endsWith("B#");
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
