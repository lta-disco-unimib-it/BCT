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
package dfmaker.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import regressionTestManager.MetaDataHandler;
import traceReaders.normalized.NormalizedIoTrace;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.raw.IoTrace;
import traceReaders.raw.MetaDataIterator;
import traceReaders.raw.TraceException;
import traceReaders.raw.IoTrace.LineIterator;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;

/**
 * 
 * @author Davide Lorenzoli
 * @author Fabrizio Pastore [ fabrizio.pastore at gmail dot com ]
 *
 */
public class DaikonNormalizedTracesMaker {
	private SuperstructuresMaker superstructuresMaker;
	
	private boolean expandReferences;

	private Collection<ProgramPointHash> ppHashes;
	
	/**
	 * Construct a DaikonNormalizedTracesMaker taht uses the given array of ProgramPointHashes to optimize the normalization process.
	 * 
	 * @param superstructuresMaker
	 * @param ppHashes
	 * @param expandReferences
	 */
	public DaikonNormalizedTracesMaker( SuperstructuresMaker superstructuresMaker, Collection<ProgramPointHash> ppHashes, boolean expandReferences ){
		this.superstructuresMaker = superstructuresMaker;
		this.expandReferences = expandReferences;
		this.ppHashes = ppHashes;
	}
	
	public DaikonNormalizedTracesMaker( SuperstructuresMaker superstructuresMaker, boolean expandReferences ){
		this.superstructuresMaker = superstructuresMaker;
		this.expandReferences = expandReferences;
	}
	
	/**
     * Normalizes a given Daikon trace file respecting to the super structure values. 
     * If ProgramPointHashes have been passed to the constructor the process is optimized by using an OptimizedDNTMTraceProcessor instead of a DNTMTraceProcessor. 
	 * @param entryPointsToSkip 
     * @param inFile
     * @throws TraceException 
     * @throws IOException  
     */
    public void normalizeTrace(IoTrace trace, NormalizedIoTrace normalizeTrace, MetaDataHandler metaHandler, Set<Long> entryPointsToSkip ) throws TraceException, IOException {    	
        // Set input stream    	
    	Superstructure entrySuperStructure = superstructuresMaker.getEntrySuperStructure();
    	Superstructure exitSuperStructure = superstructuresMaker.getExitSuperStructure();
    	System.out.println("TRACE "+trace.getMethodName());
    	//System.out.println(entrySuperStructure.varNamesSet());
    	normalizeTrace.setEntrySuperStructure(entrySuperStructure);
    	normalizeTrace.setExitSuperStructure(exitSuperStructure);
                               
        LineIterator lineIterator = trace.getLineIterator();
        
        //FIXME: this is not very good
        MetaDataIterator metaIterator = null;
        if ( metaHandler != null )
        	metaIterator = trace.getMetaDataIterator();
        
        
        DaikonTraceProcessor processor;
        
        if ( ppHashes != null ){//optimize
        	if ( entryPointsToSkip != null ){
        		throw new IOException("Cannot filter entry points with optimization active");
        	}
        	OptimizedDNTMTraceListener optimizedlistener = new OptimizedDNTMTraceListener(normalizeTrace,entrySuperStructure,exitSuperStructure,metaHandler,metaIterator,expandReferences);
        	processor = new OptimizedDaikonTraceProcessor(optimizedlistener,ppHashes);
        	
        } else {
        	DNTMTraceListener listener = new DNTMTraceListener(normalizeTrace,entrySuperStructure,exitSuperStructure,metaHandler,metaIterator,expandReferences);
        	processor = new DaikonTraceProcessor(listener);
        	if ( entryPointsToSkip != null ){
        		listener.setEntryPointsToSkip( entryPointsToSkip );
        	}
        }
        
        try {
			processor.process(lineIterator);
		} catch (DTraceListenerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }            
    
    
 

	
}
