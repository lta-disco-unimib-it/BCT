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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import conf.DataRecorderSettings;

import recorders.RecorderException;
import testSupport.TestArtifactsManager;
import traceReaders.raw.FileInteractionTrace;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.Token;
import traceReaders.raw.TraceException;
import util.JavaRunner;


public class Bug167 {

	public static final int cycles = 2;
	public static final int bufSize = 10;
	public static final int additional = 2; //if the bug is present these two are not written
	
	public static final File dir = TestArtifactsManager.getBugFile("167/DataRecording");
	
	@Test
	public void testBug() throws RecorderException, TraceException, IOException, InterruptedException{
		
		
		JavaRunner.runMainInClass(Bug167Runner.class,new ArrayList<String>(),-1);
		
		
		int counter = bufSize*cycles+additional;
		
		System.out.println("Performed "+counter);
		
		File iDir = new File(dir,"interactionInvariantLogs");
		File traceFile = new File(iDir,"0.dtrace");
		
		System.out.println(traceFile.getAbsolutePath()+".");
		
		Thread.sleep(2000);
		
		
		String threadId = "1";
		
		FileInteractionTrace trace = new FileInteractionTrace("0","0", threadId ,traceFile, null);
		Token token;
		
		assertTrue( trace.getFile().exists() );
		
		
		FileReader fr = new FileReader(trace.getFile());
		
		System.out.println(fr.read());
		
		
		
		assertEquals("Not all the expected tokens were written!!",counter, trace.getTokensCount() );
		
		
		int tokensRead = 0;
		while ( ( token = trace.getNextToken() ) != null ){
			tokensRead++;
		}
		
		assertEquals("Not all the expected tokens were written!!",counter,tokensRead);
		
		
	}
	

}
