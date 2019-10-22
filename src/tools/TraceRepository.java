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
/*
 * Created on 29-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import traceReaders.normalized.NormalizedInteractionTraceFile;
import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

/**
 * @author Leonardo Mariani
 *
 */
public class TraceRepository {
	
	private File outputFolder;

	private FileIndexAppend repository;

	private static boolean APPEND = true;
	private static boolean NOT_APPEND = false;
	private boolean alwaysNew = false;

	private int counter;
	
	/**
	 * @param string 
	 * @param b 
	 * 
	 */
	public TraceRepository(File outputFolder ) {
		this(outputFolder, false);
	}
	
	public TraceRepository(File outputFolder, boolean alwaysNew ) {
		this(outputFolder,alwaysNew,".txt");
	}
	
	public TraceRepository(File outputFolder, boolean alwaysNew, String traceSuffix ) {
		this.outputFolder = outputFolder;
		repository = new FileIndexAppend(new File(outputFolder,"interaction.idx"),traceSuffix);
		this.alwaysNew = alwaysNew;
	}

	public void addTrace(String methodName, String trace) throws FileIndexException {
		String methodId;
		// open outputFolder\methodName in append ed aggiungere trace
		//System.out.println("Method: " + methodName + " trace: " + trace + "\n");
		
		if ( alwaysNew ){
			methodId = repository.add(methodName+"-"+(counter++));
		} else {
			if ( repository.containsName(methodName) ) {
				methodId = repository.getId(methodName); 
			} else {
				methodId = repository.add(methodName);
			}
		}
		
		File file = new File( outputFolder + "/" + methodId );				
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file, APPEND);			
			fOut.write(trace.getBytes());			
			fOut.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}				
	}

	public List<NormalizedInteractionTraceFile> getTraces() throws FileIndexException {
		Set<String> ids = repository.getIds();
		ArrayList<NormalizedInteractionTraceFile> al = new ArrayList<NormalizedInteractionTraceFile>();
		for ( String id : ids ){
			al.add( new NormalizedInteractionTraceFile(repository.getNameFromId(id),new File ( outputFolder, id) ) );
		}
		return al;
	}


	
}