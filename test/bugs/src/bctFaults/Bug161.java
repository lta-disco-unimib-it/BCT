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

import it.unimib.disco.lta.alfa.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;
import recorders.FileDataRecorder;
import recorders.FileOptimizedDataRecorder;
import recorders.OptimizedDataRecorder;
import recorders.RecorderException;
import testSupport.TestArtifactsManager;
import traceReaders.raw.FileTracesReader;
import traceReaders.raw.InteractionTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import traceReaders.raw.TracesReader;
import util.FileUtil;
import conf.BctSettingsException;
import conf.EnvironmentalSetter;

public class Bug161 {
	
	private static File comparisonTracesDir = TestArtifactsManager.getBugFile("161/comparison/");
	private static File standardTracesDir = TestArtifactsManager.getBugFile("161/standardRecorder/");
	private static File optimizedTracesDir = TestArtifactsManager.getBugFile("161/optimizedRecorder/");
	
	private static long expectedExecutionTime;

	private static final int iterationLenght = 50000;
	@BeforeClass
	public  static void setUpClass() throws IOException{
		
		if ( comparisonTracesDir.exists() )
			assertTrue( FileUtil.deleteRecursively(comparisonTracesDir) );
		if ( standardTracesDir.exists() )
			assertTrue( FileUtil.deleteRecursively(standardTracesDir) );
		if ( optimizedTracesDir.exists() )
			assertTrue( FileUtil.deleteRecursively(optimizedTracesDir) );
		
		generateComparisonTrace();
		generateWrongComparisonTrace();
	}
	
	
	public static void generateComparisonTrace() throws IOException{
		System.out.println("COmparison");
		
		comparisonTracesDir.mkdirs();
		long startTime = System.currentTimeMillis();
		
		File testFile = new File( comparisonTracesDir, "comparisonTrace.txt");
		FileWriter w = new FileWriter(testFile,true);
		
		for( int i = 0; i < iterationLenght; i++ ){
			w.write("myMeth()B#");
			w.write("myMeth()E#");
		}
		
		w.close();
		
		expectedExecutionTime = (long) Math.rint(1.30 * ( System.currentTimeMillis() - startTime) );
		
		System.out.println("Execution  time with calls to Writer only"+expectedExecutionTime);
		
	}
	
	public static void generateWrongComparisonTrace() throws IOException{
		
		
		comparisonTracesDir.mkdirs();
		long startTime = System.currentTimeMillis();
		
		File testFile = new File( comparisonTracesDir, "wrongComparisonTrace.txt");
		
		
		for( int i = 0; i < iterationLenght; i++ ){
			FileWriter w = new FileWriter(testFile,true);
			w.write("myMeth()B#");
			w.write("myMeth()E#");
			w.close();
		}
		
		
		
		long executionTime = (long) Math.rint(1.30 * ( System.currentTimeMillis() - startTime) );
		
		System.out.println("Execution time with calls to Writer(file,true), Writer.write() Writer.close() "+executionTime);
		
	}
	
	
	@Test
	public void testStandardRecorder() throws RecorderException, BctSettingsException, TraceException, IOException{
		
		
		standardTracesDir.mkdirs();
		
		EnvironmentalSetter.setBctHome(standardTracesDir.getAbsolutePath());
		
		long startTime = System.currentTimeMillis();
		
		FileDataRecorder recorder = new FileDataRecorder(standardTracesDir.getAbsoluteFile());
		
		
		long time = System.currentTimeMillis();
		
		for( int i = 0; i < iterationLenght; i++ ){
			recorder.recordInteractionEnter("myMeth()", 1);
			recorder.recordInteractionExit("myMeth()", 1);
		}
		
		
		System.out.println("Execution time with FileDataRecorder "+(System.currentTimeMillis()-time));
		
		long executionTime = System.currentTimeMillis() - startTime;
		
		checkTrace( standardTracesDir );
		
		assertTrue("Execution took too much (> "+expectedExecutionTime+")",executionTime<(expectedExecutionTime));
		
		
	}
	
	@Test
	public void testOptimizedRecorder() throws RecorderException, BctSettingsException, TraceException, IOException{
		
		
		
		optimizedTracesDir.mkdirs();
		
		EnvironmentalSetter.setBctHome(optimizedTracesDir.getAbsolutePath());
		
		long startTime = System.currentTimeMillis();
		
		OptimizedDataRecorder recorder = new FileOptimizedDataRecorder(optimizedTracesDir);
		
		recorder.recordingStart();
		
		
		long time = System.currentTimeMillis();
		
		for( int i = 0; i < iterationLenght; i++ ){
			recorder.recordInteractionEnter("myMeth()", 1);
			recorder.recordInteractionExit("myMeth()", 1);
		}
		
		recorder.recordingEnd();
		
		System.out.println("Execution time with FileOptimizedDataRecorder "+(System.currentTimeMillis()-time));
		
		long executionTime = System.currentTimeMillis() - startTime;
		
		checkTrace( optimizedTracesDir );
		
		assertTrue("Execution took too much (> "+expectedExecutionTime+")",executionTime<(expectedExecutionTime));
		
		
	}


	private void checkTrace(File tracesDir) throws BctSettingsException, TraceException, IOException {
		FileTracesReader reader = new FileTracesReader(  tracesDir, FileDataRecorder.DEFAULT_IO_DIR, FileDataRecorder.DEFAULT_INTERACTION_DIR, false);
		Iterator<InteractionTrace> traces = reader.getInteractionTraces();
		assertTrue ( traces.hasNext() );
		InteractionTrace trace = traces.next();
		for( int i = 0; i < iterationLenght; i++ ){
			Token token = trace.getNextToken();
			token.getTokenValue().equals("myMeth()B");
			token.getMethodSignature().equals("myMeth()");
			
			token = trace.getNextToken();
			token.getTokenValue().equals("myMeth()E");
			token.getMethodSignature().equals("myMeth()");
		}
		
		assertNull( "Trace should be ended at this point", trace.getNextToken() );
		trace.close();
		
	}

}
