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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import traceReaders.raw.IoTrace.LineIterator;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;

/**
 * @author Fabrizio Pastore [ fabrizio dot pastore at gmail dot com 
 */
public class SuperstructuresMaker {
    private Superstructure entrySuperStructure;
    private Superstructure exitSuperStructure;
    private boolean made = false;
	private IoTrace trace;
	private boolean expandReferences;
	private Collection<ProgramPointHash> ppHashes;
	private List<String> exclusionRegexs;
    
	
	public SuperstructuresMaker ( IoTrace trace, Collection<ProgramPointHash> ppHashes, boolean expandReferences ){
		this.trace = trace;
		this.expandReferences = expandReferences;
		this.ppHashes  = ppHashes;
	}

	public SuperstructuresMaker ( IoTrace trace, boolean expandReferences ){
		this.trace = trace;
		this.expandReferences = expandReferences;	
	}
	
    
    /**
     * Init the super structures containers. It fills the super structures with
     * the values read from the given file.
     * @param inFile
     * @return
     * @throws IOException
     * @throws TraceException 
     */
    private void initSuperStructures() throws TraceException {                        
                       
        LineIterator lineIterator  = trace.getLineIterator();
    
        SMDTraceListener traceListener;
        DaikonTraceProcessor processor;
        
        if ( ppHashes != null ){ //check if it is possible to do optimization
        	OptimizedSMDTraceListener optimizedTraceListener = new OptimizedSMDTraceListener();
        	traceListener = optimizedTraceListener;
        	processor = new OptimizedDaikonTraceProcessor(optimizedTraceListener,ppHashes);
        } else {
        	traceListener = new SMDTraceListener();
        	processor = new DaikonTraceProcessor( traceListener );
        }

        
        try {
			processor.process(lineIterator);
		} catch (DTraceListenerException e) {		
			throw new TraceException( e.getMessage() );
		}
        
		// Close stream
        trace.release();
        
        
        entrySuperStructure = traceListener.getEntrySuperStructure();
        exitSuperStructure = traceListener.getExitSuperStructure();
        
        
        if ( entrySuperStructure == null ||
        		exitSuperStructure == null ){
        	
        	Superstructure genericStructure = traceListener.getGenericProgramPointStructure();
        	if ( genericStructure != null ){
        		entrySuperStructure = genericStructure;
            	exitSuperStructure = genericStructure;
        	} else {
        		if ( entrySuperStructure == null ) {
        			throw new TraceException("Malformed trace while processing trace "+trace.getMethodName());
        		} //EXIT could be equal to entry...
        		
        		String ppName = entrySuperStructure.getProgramPointName();
        		int pos = ppName.lastIndexOf(':');
        		
        		exitSuperStructure = new Superstructure( ppName.substring(0, pos+1)+"EXIT1" );
        	}
        	
        	
        }
        
        if ( expandReferences ){
        	//REFERENCE HANDLING
        	entrySuperStructure.expandReferences();


        	Set<Entry<String, String>> constantReferences = entrySuperStructure.getConstantReferences();


        	exitSuperStructure.setNotExpandableReferences(constantReferences);


        	System.out.println("EXPAND REF");
        	exitSuperStructure.expandReferences();
        }
        
        
        //Since the ExitSuperstructure MUST contains the EnterSuperstructure we
        // need normalizing the ExitSuperstructure (if necessary)        
        // This can be the case when an object is modified during method executons and some paths are no longer availavble
        
        if ( entrySuperStructure != null ){
        	Iterator entryIt = entrySuperStructure.varFields().iterator();
        	while ( entryIt.hasNext() ){
        		SuperstructureField entryField = (SuperstructureField) entryIt.next();
        		if ( ! exitSuperStructure.contains(entryField) ){
        			exitSuperStructure.add(entryField);
        		}	
        	}
        }
        
        if ( exclusionRegexs != null ){
        	filterStructure(entrySuperStructure);
        	filterStructure(exitSuperStructure);
        }

        made = true;
        
    }
    
    private void filterStructure(Superstructure superStructure) {
    	List<SuperstructureField> toRemove = new ArrayList<SuperstructureField>();
    	for ( SuperstructureField field : superStructure.varFields() ){
    		for ( String regex : exclusionRegexs ){
    			if ( field.getVarName().matches(regex) ){
    				toRemove.add(field);
    			}
    		}
    	}
    	
    	for ( SuperstructureField field : toRemove ){
    		superStructure.remove(field);
    	}
	}

	public void setExclusionRegexs( List<String> regex ){
    	exclusionRegexs = regex;
    }


	private Superstructure getStructure(SuperstructureCollection superStructuresCollection, String line) throws TraceException {
    	Superstructure res = superStructuresCollection.get(line);
    	if ( res == null ){
    		if ( superStructuresCollection.values().size() > 1 )
    			throw new TraceException("Malformed trace, too much elements in a dtrace file");
    		res = new Superstructure( line );
    		superStructuresCollection.put(res);
    	}
		return res;
	}

	public Superstructure getEntrySuperStructure() throws TraceException {
		if ( ! made )
			initSuperStructures();
		return entrySuperStructure;
	}


	public Superstructure getExitSuperStructure() throws TraceException {
		if ( ! made )
			initSuperStructures();
		return exitSuperStructure;
	}


}