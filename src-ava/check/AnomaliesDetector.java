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
package check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import automata.fsa.FiniteStateAutomaton;
import grammarInference.Record.Trace;
import grammarInference.Record.VectorTrace;
import it.unimib.disco.lta.ava.AvaReportGenerator;
import it.unimib.disco.lta.ava.anomaliesInterpretation.AnomalyInterpretation;
import it.unimib.disco.lta.ava.anomaliesInterpretation.AnomalyInterpretationUtil;
import it.unimib.disco.lta.ava.anomaliesInterpretation.BasicAnomalyInterpretation;
import it.unimib.disco.lta.ava.anomaliesInterpretation.CompositeAnomalyInterpretation;
import it.unimib.disco.lta.ava.anomaliesInterpretation.InterComponentAnomalyInterpretation;
import it.unimib.disco.lta.ava.automataExtension.AutomataExtension;
import it.unimib.disco.lta.ava.automataExtension.KBehaviorFSAExtender;
import it.unimib.disco.lta.ava.engine.AVAResult;
import it.unimib.disco.lta.ava.engine.AutomataViolationsAnalyzer;
import it.unimib.disco.lta.ava.engine.ComponentBehavioralData;
import it.unimib.disco.lta.ava.engine.ComponentBehavioralDataMemory;
import it.unimib.disco.lta.ava.engine.ViolationsAnalyzerException;
import it.unimib.disco.lta.ava.engine.configuration.AvaConfiguration;
import it.unimib.disco.lta.ava.engine.configuration.AvaConfigurationFactory;
import it.unimib.disco.lta.ava.utils.FileSerializer;
import modelsFetchers.ModelsFetcherException;
import modelsFetchers.ModelsFetcherFactoy;
import tools.InvariantGenerator;
import tools.fsa2xml.LazyFSALoader;
import tools.fsa2xml.LazyFSALoader.LazyFSALoaderException;
import traceReaders.normalized.NormalizedInteractionTrace;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedInteractionTraceHandlerCsv;
import traceReaders.normalized.NormalizedInteractionTraceHandlerFile;
import traceReaders.normalized.NormalizedInteractionTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import util.FileUtil;

public class AnomaliesDetector {

	
	public void detectAllAnomalies() throws NormalizedTraceHandlerException{
		
		
		NormalizedInteractionTraceHandler handler = TraceHandlerFactory.getNormalizedInteractionTraceHandler();
		
		File reportsFolder = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParentFile().getParentFile(),"UsageAnomalies" );
		reportsFolder.mkdir();
		
		File dest = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteraction" );
		
		if ( dest.exists() ){
			FileUtil.deleteRecursively(dest);
		}
		dest.mkdirs();
		
		
		File destCtx = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent()+"CSVInteractionContext" );
		if ( destCtx.exists() ){
			FileUtil.deleteRecursively(destCtx);
		}
		
		//first run preprocesing
		InvariantGenerator.main(new String[]{"-skipInference"});
		
		NormalizedInteractionTraceHandlerCsv csvHandler = new NormalizedInteractionTraceHandlerCsv(dest);
		NormalizedInteractionTraceIterator it = csvHandler.getInteractionTracesIterator();
		
		
		NormalizedInteractionTraceHandlerCsv csvHandlerCtx = new NormalizedInteractionTraceHandlerCsv(destCtx);
		NormalizedInteractionTraceIterator itCtx = csvHandlerCtx.getInteractionTracesIterator();
		
		while ( it.hasNext() ){
			NormalizedInteractionTrace t = it.next();
			String name = t.getMethodName();
			System.out.println("Name: "+name);
			
			int pos = name.lastIndexOf("-");
			
			String className = name.substring(0, pos);
			String objNumber = name.substring(pos+1);
			
			System.out.println("ClassName: "+className);
			System.out.println("N: "+objNumber);
		
			
			NormalizedInteractionTrace contextTrace = itCtx.next();
			try {
				detectAnomalies(reportsFolder,className, objNumber, t.getTraceFile(), contextTrace.getTraceFile() );
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModelsFetcherException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void detectAnomalies(File reportsFolder, String className, String objNumber, File traceFile, File contextTraceFile) throws FileNotFoundException, ModelsFetcherException {
		// TODO Auto-generated method stub
		FiniteStateAutomaton fsa = ModelsFetcherFactoy.modelsFetcherInstance.getInteractionModel(className);
	
		
		AvaConfiguration conf = AvaConfigurationFactory.createDefaultAvaConfiguration(2);
		
		
		AutomataViolationsAnalyzer analyzer = new AutomataViolationsAnalyzer( conf );
		
		
		FiniteStateAutomaton original = (FiniteStateAutomaton) fsa.clone();
		
		KBehaviorFSAExtender extender = new KBehaviorFSAExtender(2);
		
		Trace trace = loadAnomalousTrace(traceFile);
		
		
		List<AutomataExtension> extensions = extender.extendFSA(fsa, trace);
		
		for ( AutomataExtension extension: extensions ){
			System.out.println(extension);
		}
		
		if ( extensions.size() == 0 ){
			return;
		}
		
		List<String> els = new ArrayList<String>();
		Iterator<String> sit = trace.getSymbolIterator();
		while ( sit.hasNext() ){
			els.add(sit.next());
		}
		
		
		ComponentBehavioralDataMemory cdata = new ComponentBehavioralDataMemory(className, els, extensions, original );
		
		List<ComponentBehavioralData> data = new ArrayList<ComponentBehavioralData>();
		data.add(cdata);
		
		
		
		try {
			
			
			
			AVAResult result = analyzer.processViolations(data);
			
				
			saveContextInfo( result, contextTraceFile );
			
			AvaReportGenerator.createCsvHumanReports( reportsFolder, className+"-"+objNumber, result,conf);
			
			File avaResultSer = new File ( reportsFolder, "diagnosis-"+className+"-"+objNumber+".ava" );
			
			FileSerializer.serialize( avaResultSer, result  );
			
		} catch (ViolationsAnalyzerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}

	

	private void saveContextInfo(AVAResult result, File contextTraceFile) throws FileNotFoundException {
		Trace contextTrace = loadAnomalousTrace(contextTraceFile);
		System.out.println(contextTraceFile.getAbsolutePath());
		for ( AnomalyInterpretation interpr : result.getAllAnomaliesInterpretationsAccepted() ){
			addContextInfo(interpr, contextTrace);
			
		}
	}

	private void addContextInfo(AnomalyInterpretation interpr,
			Trace contextTrace) {
		if( interpr instanceof BasicAnomalyInterpretation ){
			BasicAnomalyInterpretation basic = (BasicAnomalyInterpretation) interpr;
			List<Integer> lines = AnomalyInterpretationUtil.getViolationLines(basic);
			
			List<String> contextData = new ArrayList<String>();
			for( int line : lines ){
				try {
					if ( line > contextTrace.getLength() ){
						line--; //workaround for case of final state
					}
					contextData.add(contextTrace.getSymbol(line-1));
				} catch (Throwable t ){
					System.err.println("Exception caught");
					System.err.println(" "+basic.getAnomalyType()+" "+basic.getViolation().getStartViolationLine()+" "+basic.getViolation().getEndViolationLine());
					t.printStackTrace();
					contextData.add("notFound");
				}
			}
			
			
			basic.getViolation().setContextData( contextData );
		} else if( interpr instanceof CompositeAnomalyInterpretation ){
			CompositeAnomalyInterpretation comp = (CompositeAnomalyInterpretation) interpr;
			for ( AnomalyInterpretation child : comp.getInterpretations() ){
				addContextInfo(child, contextTrace);
			}
		} else if( interpr instanceof InterComponentAnomalyInterpretation ){
			InterComponentAnomalyInterpretation comp = (InterComponentAnomalyInterpretation) interpr;
			for ( AnomalyInterpretation child : comp.getInterpretations() ){
				addContextInfo(child, contextTrace);
			}
		}
	}

	public static Trace loadAnomalousTrace(File file) throws FileNotFoundException {
		
		BufferedReader r = new BufferedReader(new FileReader(file));
		
		
		Trace trace = new VectorTrace();
		
		String line = null;
		try {
			while ( ( line = r.readLine() ) != null ) {
				if ( ! line.equals("|") ){
					trace.addSymbol(line);
				}
			}
		} catch (IOException e) {
			
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return trace;
	}

	public static FiniteStateAutomaton loadAutomaton(File file) throws FileNotFoundException, LazyFSALoaderException {
		return LazyFSALoader.loadFSA(file.getAbsolutePath());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		AnomaliesDetector ad = new AnomaliesDetector();
		try {
			ad.detectAllAnomalies();
		} catch (NormalizedTraceHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
