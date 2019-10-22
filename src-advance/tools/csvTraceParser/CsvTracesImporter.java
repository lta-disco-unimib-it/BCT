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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tools.gdbTraceParser.BctGdbCheckingThreadTraceListener;
import tools.gdbTraceParser.BctGdbThreadTraceListener;
import tools.gdbTraceParser.GdbThreadTraceListener;
import util.FileUtil;

import conf.EnvironmentalSetter;
import conf.management.ConfigurationFilesManager;
import conf.management.ConfigurationFilesManagerException;

public class CsvTracesImporter {
	
	private boolean checking = false;
	private boolean skipCleaning = false;
	private boolean skipConfigCreation= false;
	private File resultDir = new File("CsvTracesAnalysis");
	
	public File getResultDir() {
		return resultDir;
	}

	public void setResultDir(File resultDir) {
		this.resultDir = resultDir;
	}

	public boolean isSkipCleaning() {
		return skipCleaning;
	}

	public void setSkipCleaning(boolean skipCleaning) {
		this.skipCleaning = skipCleaning;
	}

	public boolean isSkipConfigCreation() {
		return skipConfigCreation;
	}

	public void setSkipConfigCreation(boolean skipConfigCreation) {
		this.skipConfigCreation = skipConfigCreation;
	}

	public CsvTracesImporter(){
		
	}

	/**
	 * @param args
	 * @throws ConfigurationFilesManagerException 
	 */
	public static void main(String[] args) throws ConfigurationFilesManagerException {
		// TODO Auto-generated method stub

		
		CsvTracesImporter importer = new CsvTracesImporter();
		
		
		
		
		List<File> traces = new ArrayList<File>();
		
		if ( args.length == 0 ){
			System.exit(1);
		}
		
//		boolean enableAva = false;
//		boolean filterNonTerminatingFunctions = false;
		
		File tracesTree = null;
		
		for ( int i = 0; i < args.length; i++ ){
			
			String arg = args[i];
			
			if ( "-check".equals(arg) ){
				importer.setChecking( true );
			} else if ( "-skipCleaning".equals(arg) ){
				importer.setSkipCleaning(true);
			} else if ( "-skipConfigCreation".equals(arg) ){
				importer.setSkipConfigCreation(true);	
			} else if ( "-resultsDir".equals(arg) ){
				importer.setResultDir( new File(args[++i]) );
			} else if ("-tracesTreeDir".equals(arg) ){
				if ( traces.size() > 0 ){
					System.err.println("You cannnot mix -tracesTree with traces");
					System.err.println("Traces are: "+traces);
					System.exit(-1);
				}
				tracesTree = new File( args[++i] );
//			} else if ( "-enableAva".equals(arg) ){
//				enableAva = true;
//			} else if ( "-filterNonTerminatingFunctions".equals(arg) ){
//				filterNonTerminatingFunctions = true;
			} else {
				if ( tracesTree != null ){
					System.err.println("You cannnot mix -tracesTree with traces");
					System.err.println("Traces are: "+traces);
					System.err.println("TracesTreeDir is : "+tracesTree);
					System.exit(-1);
				}
				traces.add(new File(arg));
			}
			
			
		}
		
		EnvironmentalSetter.setFlattenerType(flattener.flatteners.BreadthObjectFlattener.class);
		
		
		try {
			if ( tracesTree != null ){
				importer.importTracesTree(tracesTree);	
			} else {
				importer.importTraces( traces );
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvTracesImporterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void importTracesTree(File tracesTreeFolder) throws ConfigurationFilesManagerException, IOException, CsvTracesImporterException{
		
		GdbThreadTraceListener listener = setupListenerAndEnvironment();
		
		CsvTraceParser parser = new CsvTraceParser();
		parser.addGdbThreadTraceListener( listener );
		
		parser.setTraceHasInternalSeparator( true );
		for ( File traceFolder : FileUtil.getDirContents(tracesTreeFolder) ){
			
			parser.parseTracesTreeFolder(traceFolder);
			
		}
	}
	
	public void importTraces(List<File> traces) throws ConfigurationFilesManagerException, IOException, CsvTracesImporterException{
		
		GdbThreadTraceListener listener = setupListenerAndEnvironment();
		
		CsvTraceParser parser = new CsvTraceParser();
		parser.addGdbThreadTraceListener( listener );
		
		parser.setTraceHasInternalSeparator( true );
		for ( File trace : traces ){
			String modelName = trace.getName();
			
			parser.parseTrace( modelName, trace );
		}
	}

	
	private GdbThreadTraceListener setupListenerAndEnvironment() throws ConfigurationFilesManagerException, CsvTracesImporterException {
		if ( (!skipCleaning) && ( ! checking ) && resultDir.exists() ){
			throw new CsvTracesImporterException( "ResultDir exists " + resultDir.getAbsolutePath() );
		}
		
		resultDir.mkdir();
		
		EnvironmentalSetter.setBctHome(resultDir.getAbsolutePath());
	
		GdbThreadTraceListener listener;
		
		if ( checking ){
			listener = new BctGdbCheckingThreadTraceListener();
		} else {
			if ( (!skipConfigCreation) ){
				createConfig();	
			}

			listener = new BctGdbThreadTraceListener(checking);
		}

		return listener;
	}

	public boolean isChecking() {
		return checking;
	}

	public void setChecking(boolean checking) {
		this.checking = checking;
	}

	private static void createConfig() throws ConfigurationFilesManagerException {
		ConfigurationFilesManager.updateConfigurationFiles();
	}
}
