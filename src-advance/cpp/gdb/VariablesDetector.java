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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import tools.violationsAnalyzer.ViolationsUtil;

import cpp.gdb.GlobalVarsDetector.GlobalVariable;
import cpp.gdb.VariablesDetector.VarNamesMatcher;

public class VariablesDetector {


	
	public static interface VarNamesMatcher<T> {
		
		public String getName( T variable );
		
	}
	
	public static Set<GlobalVariable> identifyVariablesInFile(
			Collection<GlobalVariable> varsToIdentify, File file) {
		return identifyVariablesInFile(varsToIdentify, file, 0, -1 );
	}

	protected static final VarNamesMatcher<GlobalVariable> globalVariableMatcher = new VarNamesMatcher<GlobalVariable>() {

		@Override
		public String getName(GlobalVariable variable) {
			return variable.getName();
		}
	};
	
	public static Set<GlobalVariable> identifyVariablesInFile(
			Collection<GlobalVariable> varsToIdentify, File file,int begin, int end) {
		return identifyGenericVariablesInFile(varsToIdentify, file, globalVariableMatcher, begin, end );

	}

	protected static final VarNamesMatcher<String> stringMatcher = new VarNamesMatcher<String>() {

		@Override
		public String getName(String variable) {
			return variable;
		}
	};
	
	public static Set<String> identifyVariableNamesInFile(
			Collection<String> varsToIdentify, File file, int begin, int end) {
		return identifyGenericVariablesInFile(varsToIdentify, file, stringMatcher, begin, end );
		
	}
	
	
	public static <T> Set<T> identifyGenericVariablesInFile( Collection<T> varsToIdentify, File file, VarNamesMatcher<T> matcher, int begin, int end ) {
		Set<T> globalVarsInFile = new HashSet<T>();
		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(file));


			List<T> vars = new ArrayList<T>();
			vars.addAll(varsToIdentify);

			String line;

			int lineCounter = 0;
			try {
				while( ( line = r.readLine() ) != null ){
					lineCounter++;

					if ( lineCounter < begin ){
						continue;
					}

					if ( end != -1 && lineCounter >= end ){
						break;
					}

					globalVarsInFile.addAll( findNewGlobalVariablesInLine( line, vars, matcher ) );
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

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();

		}
		
		return globalVarsInFile;
	}

	/**
	 * This method cannot be used to look for variable names like: 
	 * 		myStruct.field
	 * 		*this
	 * 
	 * @param line
	 * @param vars
	 * @param matcher
	 * @return
	 */
	protected static <T> List<T> findNewGlobalVariablesInLine(String line,
			List<T> vars, VarNamesMatcher<T> matcher) {
		
		
		List<T> globalVarsInLine = new ArrayList<T>();
		for ( int i = vars.size()-1; i >= 0; i-- ){
			T var = vars.get(i);
			String varName = matcher.getName( var );
			
			//if it is an array we consider only the array, not the single position
			int arrayStart = varName.indexOf('[');
			if ( arrayStart > 0 ){
				varName = varName.substring(0,arrayStart);
			}
			
			StringTokenizer tkn = new StringTokenizer(line, ViolationsUtil.DELIMITERS); 
			String lineNoArrows = line.replace("->", ".");
			while ( tkn.hasMoreTokens() ){
				String token = tkn.nextToken();

				if ( token.equals(varName) ){
					if ( ! dotBefore(lineNoArrows, varName) ){
						globalVarsInLine.add(var);	
					}
					vars.remove(i);
					break;
				}
			}
		}
		
		return globalVarsInLine;
	}
	
	protected static boolean dotBefore( String line, String var ){
		
		boolean withDotBefore = false;
		boolean withoutDotBefore = false;
		while ( ! line.isEmpty() ){
			int idx = line.indexOf(var);

			if ( idx == -1 ){
				break;
			}
			
			if ( idx > 0 ){
				if ( line.charAt(idx-1)=='.' ){
					withDotBefore = true;
				} else {
					withoutDotBefore = true;
				}
			}
			
			line=line.substring(idx+var.length());
		}
		
		if ( withoutDotBefore ){
			return false; //at least once without a dot before
		}
		
		if ( withDotBefore ){
			return true; //always with dot before
		}
		
		//the variable has not been found
		throw new IllegalStateException("The variable "+var+" has not been found in line "+line);
	}
}
