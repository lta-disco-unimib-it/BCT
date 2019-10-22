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
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipException;

import junit.framework.TestCase;
import testSupport.UnZipFolderUtil;
import testSupport.UnZipFolderUtil.ZipUtilException;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.IoTrace;
import traceReaders.raw.TraceException;
import traceReaders.raw.IoTrace.LineIterator;
import dfmaker.core.OptimizedDaikonTraceProcessor;
import dfmaker.core.OptimizedSMDTraceListener;
import dfmaker.core.ProgramPointHash;
import dfmaker.core.DaikonTraceProcessor.DTraceListenerException;
import dfmaker.utilities.ProgramPointHashExtractor;

public class Bug84Test extends TestCase {
	private File artifactsFolder = new File ( "test/bugs/artifacts/84/");
	public void testRegression() throws ZipUtilException, ZipException, IOException, TraceException, DTraceListenerException{
		UnZipFolderUtil uz = new UnZipFolderUtil( artifactsFolder );
		
		uz.unzip(new File( artifactsFolder, "84.zip" ));
		
		OptimizedSMDTraceListener listener = new OptimizedSMDTraceListener();
		File traceFile = new File( artifactsFolder, "84.dtrace" );
		
		IoTrace iotrace = new FileIoTrace("org.exolab.castor.mapping.xml.MapToDescriptor$2.setValue((Ljava.lang.Object;Ljava.lang.Object;)V)",traceFile);
		
		Collection<ProgramPointHash> ppHashes = ProgramPointHashExtractor.getProgramPointsHashes(iotrace);
		
		OptimizedDaikonTraceProcessor processor = new OptimizedDaikonTraceProcessor(listener,ppHashes);
		LineIterator lineIterator = iotrace.getLineIterator();
		
		processor.process(lineIterator);
	
		//if we are here the test finished succesfully
		assertTrue(true);
		
		uz.deleteCreatedFiles();
	}
}
