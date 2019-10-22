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
package bctFaults;

import static org.junit.Assert.*;

import grammarInference.Record.Trace;
import grammarInference.Record.kbhParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import testSupport.TestArtifactsManager;
import tools.Cleaner;
import tools.InvariantGenerator;
import traceReaders.normalized.NormalizedInteractionTrace;
import traceReaders.normalized.NormalizedInteractionTraceIterator;
import traceReaders.normalized.NormalizedTraceHandlerException;
import traceReaders.normalized.TraceHandlerFactory;
import util.FileUtil;

import conf.EnvironmentalSetter;

@Ignore("FIXME")
public class Bug165Test {

	@Test
	public void testBug() throws NormalizedTraceHandlerException, FileNotFoundException{
		File bctHome = TestArtifactsManager.getBugFile("165/BCT_DATA");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		Cleaner.deletePreprocessingData();
		
		InvariantGenerator.main(new String[]{"-preprocessOnly","-default"});
		
		NormalizedInteractionTraceIterator tracesIt = TraceHandlerFactory.getNormalizedInteractionTraceHandler().getInteractionTracesIterator();
		

		assertTrue( "Expected one method trace", tracesIt.hasNext() );

		NormalizedInteractionTrace ntrace = tracesIt.next();
		assertEquals( "Unexpected trace name", "program.Counter", ntrace.getMethodName() );

		kbhParser parser = new kbhParser(ntrace.getTraceFile().getAbsolutePath());

		Iterator<Trace> tit = parser.getTraceIterator();

		//we expect two traces 
		ArrayList<String> expected = new ArrayList<String>();
		expected.add("program.Counter.<init>(()V)");
		expected.add("program.Counter.increment(()V)");

		Trace trace;
		
		//This is the trace built from session 1
		trace = tit.next();
		List<String> observed = getTrace(trace);

		assertEquals(expected, observed);

		//This is the trace built form session 2
		trace = tit.next();
		observed = getTrace(trace);

		assertEquals(expected, observed);
		
		Cleaner.deletePreprocessingData();
		
		FileUtil.deleteRecursively(new File(bctHome,"PreprocessingCSVInteraction"));
		
		FileUtil.deleteRecursively(new File(bctHome,"Preprocessing"));
	}

	private List<String> getTrace(Trace first) {
		ArrayList res = new ArrayList();
		for ( int i = 0; i < first.getLength(); i ++ ){
			res.add(first.getSymbol(i));
		}
		return res;
	}
}
