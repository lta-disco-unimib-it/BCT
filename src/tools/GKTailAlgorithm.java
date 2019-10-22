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
package tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import modelsFetchers.IoModel;
import modelsFetchers.ModelsFetcher;
import modelsFetchers.ModelsFetcherFactoy;
import traceReaders.normalized.GKTailNormalizedIoTraceDB;
import traceReaders.normalized.NormalizedIoTraceHandler;
import traceReaders.normalized.NormalizedIoTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import util.FileIndexAppend;
import util.FileIndex.FileIndexException;
import conf.ClassPath;
import conf.ControlFileConfiguration;
import conf.EnvironmentalSetter;
import conf.InvariantGeneratorSettings;
import dfmaker.core.DaikonModelsParserInterface;
import dfmaker.core.DaikonSimplifyModelsParser;

public class GKTailAlgorithm {
	
	private static ArrayList methodList = new ArrayList();
	private static Set memory = new HashSet();
	
	private static void runDaikon( File outputPath, String configFile, String daikonAdditionalOptions) throws NormalizedTraceHandlerException {

		NormalizedIoTraceHandler nth;

		nth = TraceHandlerFactory.getNormalizedIoTraceHandler();

		NormalizedIoTraceIterator traceIterator = nth.getIoTracesIterator();
		
		File disteilledIdxFile = new File(outputPath,"iotraces.idx");
		FileIndexAppend distilledIndex = new FileIndexAppend( disteilledIdxFile, ".io.txt");
		
		System.out.println("HAS NEXT:" + traceIterator.hasNext());
		
		while ( traceIterator.hasNext() ) {
			try {
				//FIXME non uso l'interfaccia perche' mi serve il metodo getIdMethodCall() che altrimenti avrei dovuto aggiungere a tutte le classi che implementano l'interfaccia
			
				//System.out.println(" Trace class "+theTrace.getClass().getCanonicalName());
				GKTailNormalizedIoTraceDB trace = (GKTailNormalizedIoTraceDB) traceIterator.next();
				//CanonicalPath is used because under linux daikon cannot work passing absolutePath
				//Under linux parameters cannot be passed between double quiotes
				String cmd;
				
					cmd = "java -Xmx512m -classpath " + ClassPath.getDaikonPath() + " daikon.Daikon "+daikonAdditionalOptions+" --config "+ EnvironmentalSetter.getBctHomeNoJAR()  + File.separator + "conf" + File.separator + "files" + File.separator + GKTailAlgorithm.setFileConfig(configFile) 
					+ " -o "
					//String cmd = "java -classpath \"" + ClassPath.getDaikonPath() + "\" daikon.Daikon -o "
	
					//+ "\""
					+ new File( outputPath, "tmp" ).getCanonicalPath()
					+ " "
					//+ "\""
					+ trace.getDaikonDeclFile()
					+ " "
					//+ "\""
					+ trace.getDaikonTraceFile()
					//+ "\""
					;
				System.out.println(cmd);
	
				Process p = Runtime.getRuntime().exec(cmd);
				
				final BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				final BufferedInputStream err = new BufferedInputStream(p.getErrorStream());
				
				String id = null;
				try {
					id = distilledIndex.getId(trace.getMethodName());
					
				} catch (FileIndexException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if ( id == null ){
					try {
						id = distilledIndex.add( trace.getMethodName() );
					} catch (FileIndexException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				// creating output file
				final File distilledModel = new File(outputPath,id);
				final BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( distilledModel ) );
				
				// Daikon output stream thread
				Thread daikonOuputStreamThread = new Thread() {
	
					public void run() {
						try {
							while (true) {
								int c = in.read();
								if (c < 0)
									break;
								else
									out.write(c);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				
				// Daikon error stream thread
				Thread daikonErrorStreamThread = new Thread() {
	
					public void run() {
						try {
							while (true) {
								int c = err.read();
								if (c < 0)
									break;
								else
									System.err.write(c);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				
				// Start thread
				daikonOuputStreamThread.start();			
				daikonErrorStreamThread.start();
				
				// Closing streams
				try {
					p.waitFor();
					in.close();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				processImplicationsFile(distilledModel, trace.getIdMethodCall());
	
				// Removing daikon output file
				/*
				System.out.println("Removing: " + distilledModel.getAbsolutePath());
				if (!distilledModel.delete()) {
					System.out.println("Unable to remove temporary file " + distilledModel.getAbsolutePath() + " remove it manually");
				}
				*/
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		System.out.println("Daikon execution done");
	}
	
	private static String setFileConfig(String configFile ) {
		
		if (configFile.equalsIgnoreCase(ControlFileConfiguration.INTERMEDIATE))
		{	
			String result = "intermediates.txt";
			return result;
		}
		else if(configFile.equalsIgnoreCase(ControlFileConfiguration.ESSENTIALS))
		{	
			String result = "essentials.txt";
			return result;
		}
		else 
		{	
			String result = "default.txt";
			return result;
		}
		
	}
	
	private static void processImplicationsFile( File f, int idMethodCall) throws IOException {
		//FIXME: this can be done better
		String methodName = processMethodName(f);
		try {
			if (methodName == null) {
				System.out.println("File " + f + " skipped");
			}
			else {															
				/*
				 * Old code
				 * 								
				IoModel ioModel = new IoModel();
				setPreconditions(f, methodName, ioModel );
				setPostconditions(f, methodName, ioModel );
				*/
				
				/*
				 * PRINT THE TRACE FILE
				 * 
				FileReader fileReader = new FileReader(f);
				int i;
				System.out.println("******************************************************");
				while ((i = fileReader.read()) != -1) {
					System.out.print((char) i);
				}
				System.out.println("******************************************************");
				*/

				/*
				 * New Code
				 */
				DaikonModelsParserInterface daikonModelsParser = new DaikonSimplifyModelsParser();				
				
				ArrayList<String> preconditions = daikonModelsParser.getPreconditions(new FileReader(f));				
				ArrayList<String > postconditions = daikonModelsParser.getPostconditions(new FileReader(f));
				
				IoModel ioModel = new IoModel();
				
				ioModel.addPreconditions(preconditions);
				ioModel.addPostconditions(postconditions);
				
				methodList.add(methodName);
				
				ModelsFetcher mr = ModelsFetcherFactoy.modelsFetcherInstance;
				
				/*
				 * SOME PRINT FOR DEBUGGING
				 * 
				System.out.println("Printing file " + f.getCanonicalPath() + ":");
				FileReader fileReader = new  FileReader(f);
				int i;
				while ((i = fileReader.read()) != -1) {
					System.out.print((char) i);
				}				
				System.out.println("Preconditions:");
				for (Iterator iter = ioModel.preconditionsIt(); iter.hasNext();) {
					String element = (String) iter.next();
					System.out.println(element);
					
				}
				System.out.println("Postconditions:");
				for (Iterator iter = ioModel.postconditionsIt(); iter.hasNext();) {
					String element = (String) iter.next();
					System.out.println(element);					
				}
				*/
				
				mr.addIoModel( ""+idMethodCall, ioModel);
				
				System.out.println("File " + f + " processed");
			}
		} catch ( Exception e ) {
			System.err.println("There was an error processing "+f);
			e.printStackTrace();
		}
	}
	
	private static String processMethodName(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		while (true) {
			String line = in.readLine();
			if (line == null) {
				in.close();
				return null;
			} else if (line.contains("====")) {
				int idx = -1;
				//we can have white lines between ====== and method name
				do{
					line = in.readLine();
					idx = line.indexOf(":");
				} while ( line != null && idx < 0 );
				
				in.close();
				
				return line.substring(0, idx );
			}
		}
	}

	private static void processLogs() {
		methodList.clear();
		memory.clear();
		
		InvariantGeneratorSettings iGS = EnvironmentalSetter.getInvariantGeneratorSettings();
		try {
			
			System.out.println("Inferring IO invariants");
			runDaikon(  iGS.getDistilledDir(), iGS.getDaikonConfig(), iGS.getDaikonAdditionalOptions() );
		
		} catch (NormalizedTraceHandlerException e) {			
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		processLogs();
	}

}
