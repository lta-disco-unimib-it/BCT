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
package traceReaders.normalized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.sun.corba.se.impl.orbutil.ObjectWriter;

import dfmaker.core.DaikonDeclarationMaker.DaikonComparisonCriterion;
import dfmaker.core.ProgramPointDataStructures;

import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import util.FileIndex;
import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

public class NormalizedIoTraceHandlerFile implements NormalizedIoTraceHandler {

	/**
	 * Inner class to implement the NormalizedIoTraceIterator.
	 * It is declared private because we want that this is used only locally.
	 * Classes that uses an instance of this class must refer to its interface and cannot instantiate it 
	 * 
	 * 
	 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
	 *
	 */
	private class FileIoTracesIterator implements NormalizedIoTraceIterator {
		private Iterator<String> declsIterator;
		//private Iterator<String> dtraceIterator;

		public FileIoTracesIterator(FileIndex decls, FileIndex dtraces) {
			declsIterator = decls.getIds().iterator();


		}

		public boolean hasNext() {
			return ( declsIterator.hasNext() );
		}

		public NormalizedIoTrace next() {

			String id = declsIterator.next();
			String name = null;
			try {
				name = decls.getNameFromId(id);
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			File declsFile = getDeclFile(name);
			File tracesFile = getTraceFile(name);

			return new NormalizedIoTraceFile (name,declsFile,tracesFile);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private FileIndexAppend decls;
	private FileIndexAppend dtraces;
	private File declsIndex;
	private File traceIndex;
	private File declsDir;
	private File traceDir;
	private File superStructuresFile;
	private HashMap<String,ProgramPointDataStructures> superStructures;

	public NormalizedIoTraceHandlerFile(File decls, File dtrace) {
		this.declsDir = decls;
		this.traceDir = dtrace;
		this.declsIndex = new File ( decls, "declarations.idx" );
		this.traceIndex = new File ( dtrace, "traces.idx" );
		this.decls = new FileIndexAppend(declsIndex,".decls");
		this.dtraces = new FileIndexAppend(traceIndex,".dtrace");

		this.superStructuresFile = new File ( decls, "superStructures.ser" );
		superStructures = loadSuperstructures();
	}



	private HashMap<String, ProgramPointDataStructures> loadSuperstructures() {
		if ( superStructuresFile.exists() ){

			try {


				ObjectInputStream ois = new ObjectInputStream( new FileInputStream(superStructuresFile) );
				try {
					return (HashMap<String,ProgramPointDataStructures>) ois.readObject();
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} finally {
					ois.close();
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new HashMap<String,ProgramPointDataStructures>();

	}



	public File getTraceFile( String methodName ){

		String file = null;
		if ( ! dtraces.containsName(methodName) ){
			try {
				file = dtraces.add(methodName);
			} catch (FileIndexException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				dtraces.save(traceIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				file = dtraces.getId(methodName);
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new File ( traceDir, file );
	}

	public File getDeclFile( String methodName ){
		String file = null;
		if ( ! decls.containsName(methodName) ){
			try {
				file = decls.add(methodName);
			} catch (FileIndexException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				decls.save(declsIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				file = decls.getId(methodName);
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new File ( declsDir, file );
	}

	public NormalizedIoTrace newIoTrace(IoTrace trace) {
		String methodName = trace.getMethodName();
		return new NormalizedIoTraceFile ( methodName, getTraceFile(methodName), getDeclFile(methodName)) ;
	}

	public NormalizedIoTraceIterator getIoTracesIterator() {	
		return new FileIoTracesIterator(decls,dtraces);
	}

	public void saveTrace(NormalizedIoTrace normalizedTrace, DaikonComparisonCriterion comparisonCriterion) throws TraceException {
		normalizedTrace.commit(comparisonCriterion);
	}


	public ProgramPointDataStructures getProgramPointData( String programPointName ){
		return superStructures.get(programPointName);
	}

	@Override
	public void addProgramPointData(ProgramPointDataStructures programPointData) {


		superStructures.put(programPointData.getProgramPointName(), programPointData);

		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(superStructuresFile));
			try {
				out.writeObject(superStructures);
			} finally {
				out.close(); 	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
