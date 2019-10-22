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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.FileUtil;

import cpp.gdb.GlobalVarsDetector.GlobalVariable;

public class GlobalVariablesCommandsCreator implements
		AdditionalGdbCommandsCreator {

	private GlobalVarsDetector varsDetector;
	private HashMap<File,String> additionalCOmmandsForFile = new HashMap<File,String>();
	private HashMap<File, Set<GlobalVariable>> globalVariableMap = new HashMap<File, Set<GlobalVariable> >();
	

	
	public Map<File, Set<GlobalVariable>> getGlobalVariableMap() {
		return Collections.unmodifiableMap(globalVariableMap);
	}


	public static void main ( String args[] ){
		
		 
		GlobalVariablesCommandsCreator c = new GlobalVariablesCommandsCreator(new File("/home/fabrizio/Programs/VTT_USI/a.out"));
		
		String srcFIle = "/home/fabrizio/Programs/VTT_USI/P2P_Joints_TG3_e.c";
		String commands = c.createAdditionalMonitoringCommands(new File(srcFIle), 210);
		System.out.println(commands);
		
		
		System.out.println(commands);
	}
	
	
	public GlobalVariablesCommandsCreator(File executable) {
		this(executable,null);
	}

	public GlobalVariablesCommandsCreator(File executable, List<String> variablesToExclude) {
		varsDetector = new GlobalVarsDetector(executable, variablesToExclude );
	}
	
	@Override
	public String createAdditionalMonitoringCommands(File file, int line) {
		//System.out.println("Creating aditional commands for "+file.getAbsolutePath()+" "+varsDetector.getExecutableToAnalyze().getAbsolutePath() );
		String commands = additionalCOmmandsForFile.get(file);
		if ( commands != null ){
			//System.out.println("Commands are: \n"+commands);
			return commands;
		}
		Set<GlobalVariable> vars = retrieveGlobalVariablesToMonitor(file);
		
		
		commands = createAdditionalCommands( vars );
		
		additionalCOmmandsForFile.put(file, commands);
		
		
		System.out.println("Commands are: \n"+commands);
		
		return commands;
	}

	
	public Set<GlobalVariable> retrieveGlobalVariablesToMonitor(File file) {
		
		file = file.getAbsoluteFile();
		Set<GlobalVariable> vars = globalVariableMap.get(file);

		if ( vars == null ){
			List<GlobalVariable> globals = varsDetector.getGlobalVariables();
			vars = VariablesDetector.identifyVariablesInFile( globals, file );
			
			if ( VariablesFilter.INSTANCE.varsToIgnoreInFile(file) ){
				HashSet<GlobalVariable> newVars = new HashSet<>();
				for ( GlobalVariable v : vars ){
					if ( ! VariablesFilter.INSTANCE.ignore(file, v.getName()) ){
						newVars.add(v);
					} else {
						System.out.println("IgnoringVariable: "+v.getName());
					}
				}
				vars = newVars;
			}
			
			System.out.println("Variables for file "+file.getName()+" "+vars);
			globalVariableMap.put(file, vars);
		}

		return vars;
	}

	private String createAdditionalCommands(Collection<GlobalVariable> vars) {
		StringBuffer sb = new StringBuffer();
		for ( GlobalVariable var : vars ){
			
			
			
			if ( var.isPointer() && ( ! printableWithoutDereference( var ) )){
				sb.append(GdbRegressionConfigCreator.bctPrintPointersFunctionName+" "+var.getName());
				sb.append("\n");
			} else {
				sb.append("echo !!!BCT-VARIABLE "+var.getName()+"\\n");
				sb.append("\n");
				sb.append("output "+var.getName());
				sb.append("\n");
				sb.append("echo \\n");
				sb.append("\n");
			}
			
			
		}
		
		return sb.toString();
	}

	private boolean printableWithoutDereference(GlobalVariable var) {
		return var.getType().equals("char");
	}



}
