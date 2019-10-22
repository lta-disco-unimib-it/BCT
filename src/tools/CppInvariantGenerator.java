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

import java.io.File;
import java.util.ArrayList;

import tools.CppTracePreprocessor.CppTracePreprocessorException;

public class CppInvariantGenerator extends InvariantGenerator {
	
	public static void main( String[] args ){
		ArrayList<String> invgenArgs = new ArrayList<String>();
		ArrayList<File> files = new ArrayList<File>();
		
		for ( int i = 0; i < args.length; i++ ){
			if ( args[i].equals("-invariantGeneratorConfFile") ){
				invgenArgs.add("-invariantGeneratorConfFile");
				invgenArgs.add(args[++i]);
			}
			else if ( args[i].equals("-modelsFetcherConfFile") ){
				invgenArgs.add("-modelsFetcherConfFile");
				invgenArgs.add(args[++i]);
			} else if ( args[i].equals("-inferenceEngineConfFile") ){
				invgenArgs.add("-inferenceEngineConfFile");
				invgenArgs.add(args[++i]);
			} else if ( args[i].equals("-default") ){
				invgenArgs.add("-default");
			} else {
				files.add(new File(args[i]));
			}
		}
		
		CppTracePreprocessor preprocessor = new CppTracePreprocessor();
		for ( File file : files ){
			try {
				preprocessor.process(file);
			} catch (CppTracePreprocessorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String[] argsNew = new String[invgenArgs.size()];
		int i = 0;
		for ( String arg : invgenArgs ){
			argsNew[i++] = arg;
		}
		
		InvariantGenerator.main(argsNew);
		
	}
}
