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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import testSupport.TestArtifactsManager;
import traceReaders.normalized.VirtualNormalizedIoTrace;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.InteractionTraceReader;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import dfmaker.core.DaikonNormalizedTracesMaker;
import dfmaker.core.SuperstructuresMaker;

public class Bug152Test extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public void testBug() throws TraceException, IOException{
		File traceFile = TestArtifactsManager.getBugFile("152/0.dtrace");


		InputStreamReader isr = new InputStreamReader( new FileInputStream(traceFile) );
		InteractionTraceReader reader = new InteractionTraceReader(isr);


		assertEquals( "testPackage.Test0.m(int)B", reader.getNextToken()) ;
		assertEquals( "testPackage.Test0.m(int)E", reader.getNextToken()) ;
		assertEquals( null, reader.getNextToken()) ;

		isr.close();
	}

}
