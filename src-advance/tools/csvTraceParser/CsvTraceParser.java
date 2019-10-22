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
package tools.csvTraceParser;


//import it.unimib.disco.lta.alfa.csv.CsvFileParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dfmaker.core.Variable;

import tools.gdbTraceParser.GdbThreadTraceListener;
import util.FileUtil;

public class CsvTraceParser {

	private List<GdbThreadTraceListener> listeners = new ArrayList<GdbThreadTraceListener>();
	private CsvFileParser csvParser = new CsvFileParser();
	private LinkedList<StackTraceElement> stack = new LinkedList<StackTraceElement>();
	private long threadId = 0;
	private boolean traceHasInternalSeparator;
	private String internalTraceSeparator = "|";
	
	public CsvTraceParser() {
	}

	/**
	 * This method is invoked on a folder that contains multiple folders 
	 * each one with a trace
	 * 
	 * TreeFolder/
	 * TreeFolder/MethodA/
	 * TreeFolder/MethodA/1.csv
	 * TreeFolder/MethodA/2.csv
	 * TreeFolder/MethodB/
	 * TreeFolder/MethodB/1.csv
	 * TreeFolder/MethodB/2.csv
	 * 
	 * @param trace
	 * @throws IOException
	 */
	public void parseTracesTreeFolder(File trace) throws IOException {
	
		if ( ! trace.isDirectory() ){
			throw new IllegalArgumentException(trace.getAbsolutePath()+" is not a directory!");
		}
		
		for ( File tracesFolder : FileUtil.getDirContents(trace) ){
			if ( ! tracesFolder.isDirectory() ){
				continue;
			}
			
			parseTracesInFolder( tracesFolder );
			
		}
	}
	
	/**
	 * This method is invoked on a folder that contains traces for a same method
	 * 
	 * MethodA/
	 * MethodA/1.csv
	 * MethodA/2.csv
	 * 
	 * 
	 * @param tracesFolder
	 * @throws IOException
	 */
	public void parseTracesInFolder(File tracesFolder) throws IOException {
		String modelName = tracesFolder.getName();
		
		for ( File trace : FileUtil.getDirContents(tracesFolder) ){
			if ( trace.isDirectory() ){
				continue;
			}
			parseTrace( modelName, trace );
		}
	}

	/**
	 * Parses a trace as if it is sequence o methods invoked by modelName
	 *  
	 * @param modelName
	 * @param trace
	 * @throws IOException
	 */
	public void parseTrace(String modelName, File trace) throws IOException {
		
		
		
		BufferedReader reader = new BufferedReader(new FileReader(trace));
		
		String line;
		
		recordBeginOfFile( modelName );
		
		try {
			int lineNo = 0;
			while ( ( line = reader.readLine() ) != null ){
				lineNo++;
				
				if ( traceHasInternalSeparator ){
					if ( line.trim().equals(internalTraceSeparator)){
						
						recordEndOfFile( modelName );
						
						recordBeginOfFile( modelName );
						
						continue;
					}
				}
				
				List<String> columns = csvParser.getColumns(line);
				
				String functionName = columns.get(0);
				
				ArrayList<Variable> parameters = new ArrayList<Variable>();
				for ( int i = 1; i < columns.size(); i++ ){
					String name = "parameter["+i+"]";
					String value = columns.get(i);
					Variable v = new Variable(name, value, 1);
					parameters.add(v);
				}
				
				recordNewLine( functionName, parameters,
						new ArrayList<Variable>(),new ArrayList<Variable>(),new ArrayList<Variable>(),
						getThreadId(), trace.getName(), lineNo );
			}
			
			recordEndOfFile( modelName );
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void recordBeginOfFile( String traceName) {
		
		pushOntoStack( traceName, 0 );
		
		for ( GdbThreadTraceListener listener : listeners ){
			listener.functionEnter(traceName, new ArrayList<Variable>(), 
					new ArrayList<Variable>(),new ArrayList<Variable>(),new ArrayList<Variable>(),
					getThreadId(), traceName, 0, getStack(), null);
		}
	}
	
	private void recordEndOfFile( String traceName) {
		
		pushOntoStack( traceName, 0 );
		
		for ( GdbThreadTraceListener listener : listeners ){
			listener.functionExit(traceName, new ArrayList<Variable>(), 
					new ArrayList<Variable>(),new ArrayList<Variable>(),new ArrayList<Variable>(),
					new ArrayList<Variable>(), getThreadId(), traceName, 0, getStack(), null);
		}
	}

	private long getThreadId() {
		return threadId;
	}

	public void incrementThreadId() {
		threadId++;
	}

	private void recordNewLine(String functionName,
			ArrayList<Variable> parameters, ArrayList<Variable> a,ArrayList<Variable> b,ArrayList<Variable> c, long threadId, String fileName, int lineNo) {
		
		pushOntoStack( fileName, lineNo );
		
		for ( GdbThreadTraceListener listener : listeners ){
			listener.functionEnter(functionName, parameters, a, b, c, threadId, fileName, lineNo, getStack(), null);
		}
		
		for ( GdbThreadTraceListener listener : listeners ){
			listener.functionExit(functionName, new ArrayList<Variable>(), a, b, c, new ArrayList<Variable>(), threadId, fileName, lineNo, getStack(), null);
		}
		
	}

	private List<StackTraceElement> getStack() {
		ArrayList<StackTraceElement> list = new ArrayList<StackTraceElement>();
		
		list.addAll(stack);
		
		return list;
	}

	private void pushOntoStack(String fileName, int lineNo) {
		
	}

	public void addGdbThreadTraceListener(GdbThreadTraceListener listener) {
		listeners.add( listener );
	}

	public void setTraceHasInternalSeparator(boolean b) {
		traceHasInternalSeparator = b;
	}

}
