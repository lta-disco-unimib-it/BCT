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
package cpp.gdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import tools.gdbTraceParser.GdbInfoExtractor;
import util.ProcessRunner;
import cpp.gdb.GdbRegressionConfigCreator;
import cpp.gdb.GlobalVarsDetector.GlobalVariable;

public class GlobalVarsDetector {
	
	public static class GlobalVariable implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String name;
		private boolean pointer;
		private String type;
		public String getType() {
			return type;
		}
		public String getName() {
			return name;
		}
		public boolean isPointer() {
			return pointer;
		}
		public GlobalVariable(String name, String type, boolean isStatic) {
			super();
			this.name = name;
			this.type = type;
			this.pointer = isStatic;
		}
	}

	private GdbInfoExtractor data;
	private HashMap<String,String> constantValues = new HashMap<String, String>();
	private List<GlobalVariable> variables;
	private Set<String> variablesToExclude;
	
	{
		File gdb = new File( EnvUtil.getGdbExecutablePath() );
		
		 data = new GdbInfoExtractor( gdb );
	}
	
	public GlobalVarsDetector(File executable) {
		this(executable,null);
	}
	
	public GlobalVarsDetector(File executable, List<String> variablesToExclude) {
		data.setExecutableToAnalyze(executable);
		if( variablesToExclude != null ){
			this.variablesToExclude = new HashSet<String>();
			this.variablesToExclude.addAll( variablesToExclude );
		}
	}

	public File getExecutableToAnalyze() {
		return data.getExecutableToAnalyze();
	}

	public static void main(String args[]){
		File executable = new File(args[0]);
		
		GlobalVarsDetector valuesDetector = new GlobalVarsDetector(executable);
		
		for ( GlobalVariable v : valuesDetector.getGlobalVariables() ){
			System.out.println(v.getName());
		}
		
	}


	public List<GlobalVariable> getGlobalVariables() {

		if ( variables == null ){

			variables = runGdbAndGetGlobalVariables();
			
		}

		return variables;
	}

	private List<GlobalVariable> runGdbAndGetGlobalVariables() {
		
		String output = data.runGdbAndGetOutput("info variables");
		return extractGlobalVarsFromGdbOutput( output );
	}
	
	

	private List<GlobalVariable> extractGlobalVarsFromGdbOutput(String outputBuffer) {
		BufferedReader r = new BufferedReader(new StringReader( outputBuffer ));
		String line;

		List<GlobalVariable> variables = new ArrayList<GlobalVarsDetector.GlobalVariable>();
		
		try {
			while ( ( line = r.readLine() ) != null ){
				
				if ( line.isEmpty() ){
					continue;
				}
				
				if ( line.startsWith("File") ){
					continue;
				}
				
				if ( line.startsWith("<") ){
					continue;
				}
				
				if ( line.equals("Non-debugging symbols:") ){
					break;
				}
				
				line = line.trim();
				
//				int membersStart = line.indexOf(' ');
//				if ( membersStart <= 0 ){
//					throw new IllegalArgumentException("Unexpected string: "+line);
//				}
				
				String varLine[] = line.split(" ");

				//probably its enough to write the following,
				//but I suggest not to do it to not introduce regressions
//				String var = varLine[varLine.length-1];
//				String type = varLine[varLine.length-2];
				
				String type;
				String var;
				if ( varLine.length == 2 ){
					type = varLine[0];
					var = varLine[1];
				} else {
					//FIXME: this will prevent the monitoring of something like: const char *name;
					if (  varLine.length == 3 && "static".equals(varLine[0]) ){
						type = varLine[1];
						var = varLine[2];
					} else {
						//which is safe for now, but not good
						System.out.println("SKIPPING MONITORING OF "+line);
						continue;
					}
				}
				//TODO: handle static vars declaration, and remove line above
				

				
				
				if ( var.charAt(var.length()-1) == ';' ){
					var = var.substring(0,var.length()-1);
				}
				
				if ( var.contains("[") ){
					int pos = var.indexOf("[");
					var = var.substring(0, pos);
				}
				
				if ( type.equals("void") ){
					continue; //monitoring of void leads to dereference errors 
				}
				
				GlobalVariable variable;
				if ( var.charAt(0) == '*' ){
					variable = new GlobalVariable(var.substring(1), type, true);
				} else {
					variable = new GlobalVariable(var, type, false);
				}
				
				if ( variablesToExclude == null || ! variablesToExclude.contains( variable.getName() ) ){
					variables.add(variable);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return variables;
	}



	
}
