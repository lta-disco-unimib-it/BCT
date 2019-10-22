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
package traceReaders.normalized.traceCreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import tools.TokenMetaData;
import traceReaders.normalized.NormalizedInteractionTraceHandler;
import traceReaders.normalized.NormalizedInteractionTraceHandlerFile;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import util.componentsDeclaration.ComponentsDefinitionException;
import conf.InvariantGeneratorSettings;

public class TestedMethodsDector implements TraceMaintainer  {

	private BufferedWriter writer;
	private String SEPARATOR = "\t";

	public TestedMethodsDector() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void programPointBegin(InteractionTrace rawTrace,
			Token programPointToken) {
		
		String method = programPointToken.getMethodSignature();
		TokenMetaData meta = programPointToken.getTokenMetaData();
		
		int argsStart = method.indexOf('(');
		String classAndmethodName = method.substring(0,argsStart);
		
		int classEnd = classAndmethodName.lastIndexOf('.');
		String className = classAndmethodName.substring(0, classEnd);
		String methodName = classAndmethodName.substring(classEnd+1);
		String signature = method.substring(argsStart);
		
		String calledClass = meta.getCalledObjectClass();
		if ( calledClass != null ){
			className = calledClass;
		}
		
		String testLocation = meta.getContextData().get(0);
		int colonsPos = testLocation.indexOf(':');
		
		String testName = testLocation.substring(0, colonsPos);
		String testLine = testLocation.substring( colonsPos + 1);
		
		
		
		traceTestedMethod( className, methodName+signature, testName, Integer.valueOf(testLine) );
	}

	private void traceTestedMethod(String className, String methodName,
			String testName, Integer line) {
		try {
			writer.write(className+"."+methodName+SEPARATOR+testName+SEPARATOR+line);
			writer.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void programPointEnd(InteractionTrace rawTrace,
			Token programPointToken) {

		
	}

	@Override
	public void init(InvariantGeneratorSettings invariantGeneratorSettings,
			NormalizedInteractionTraceHandler handler)
			throws ComponentsDefinitionException {

		File dest = new File ( ((NormalizedInteractionTraceHandlerFile)handler).getInteractionOutputFolder().getParent(), "TestedMethods.txt" );
		
		
		try {
			writer = new BufferedWriter( new FileWriter(dest) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void analysisEnd() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void analysisBegin() {
		
	}

	@Override
	public void sessionBegin(String sessionId) {

		
	}

	@Override
	public void sessionEnd() {

		
	}

	@Override
	public void traceBegin(InteractionTrace trace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void traceEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void programPointGeneric(InteractionTrace trace, Token token) {
		// TODO Auto-generated method stub
		
	}

}
