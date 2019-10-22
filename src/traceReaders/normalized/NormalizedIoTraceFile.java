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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import traceReaders.raw.TraceException;
import dfmaker.core.DaikonDeclarationMaker;
import dfmaker.core.Superstructure;
import dfmaker.core.Variable;
import dfmaker.core.DaikonDeclarationMaker.DaikonComparisonCriterion;

public class NormalizedIoTraceFile implements NormalizedIoTrace {
	private PrintWriter trace;
	private PrintWriter decl;
	private File fileTrace;
	private File fileDecl;
	private Superstructure entrySuperstructure;
	private Superstructure exitSuperstructure;
	private String methodName;
	
	
	/**
	 * Constructor 
	 * @param method	the name of the method the trace we handles refers to
	 * @param fileTrace	the daikon dtrace file associated with this trace
	 * @param fileDecl	the daikon declarations file associated with this trace
	 */
	public NormalizedIoTraceFile( String method, File fileTrace, File fileDecl ) {
		this.fileDecl = fileDecl;
		this.fileTrace = fileTrace;
		this.methodName = method;
	}
	
	public void addGenericPoint(String genericProgramPoint, Vector<Variable> normalizedPoint) {
		addPoint(genericProgramPoint,normalizedPoint);
	}
	
	public void addEntryPoint(String entryPoint, Vector<Variable> normalizedPoint) {
		addPoint(entryPoint,normalizedPoint);
	}


	public void addExitPoint(String exitPoint, Vector<Variable> normalizedPoint) {
		addPoint(exitPoint,normalizedPoint);
	}
	
	private void addPoint( String point, Vector<Variable> normalizedPoint ){
		  if ( trace == null ){
			  try {
				trace = new PrintWriter ( new FileWriter( fileTrace ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		  }
		
		  trace.println(point);
		  Enumeration enumeration = normalizedPoint.elements();
          while (enumeration.hasMoreElements()) {
          	Variable variable = (Variable) enumeration.nextElement();
          	trace.println(variable.getName());
          	trace.println(variable.getValue());
          	trace.println(variable.getModified());
          }
          trace.println();
          
	}


	public void addObjectPoint(String objectPoint, Vector<Variable> normalizedPoint) {
		throw new UnsupportedOperationException("Object Point Not Supported ");
	}

	

	public void commit(DaikonComparisonCriterion comparisonCriterion) throws TraceException {
		writeDeclarations(comparisonCriterion);
		if ( decl != null ){
			decl.close();
			decl = null;
		}
		
		if ( trace != null ){
			trace.close();
			trace = null;
		}
	}
	
	private void writeDeclarations(DaikonComparisonCriterion comparisonCriterion) throws TraceException {
		if ( decl == null ){
			try {
				decl = new PrintWriter ( new FileWriter( fileDecl ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			DaikonDeclarationMaker.write(decl, entrySuperstructure, exitSuperstructure, comparisonCriterion );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new TraceException("Problem saving "+entrySuperstructure.getJavaName());
		}
	}
	
	public void setEntrySuperStructure( Superstructure entrySuperStructure) {
		this.entrySuperstructure = entrySuperStructure;
	}
	
	public void setExitSuperStructure( Superstructure exitSuperStructure) {
		this.exitSuperstructure = exitSuperStructure;
	}

	public String getMethodName() {
		return methodName;
	}

	
	public File getDaikonDeclFile() {
		return fileDecl;
	}

	
	public File getDaikonTraceFile() {
		return fileTrace;
	}
}