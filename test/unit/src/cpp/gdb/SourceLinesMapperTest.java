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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import cpp.gdb.FileChangeInfo.Delta;

public class SourceLinesMapperTest {
	
	File V1 = new File("test/unit/artifacts/cpp/traceUtils/oneFile/V1");
	File V2 = new File("test/unit/artifacts/cpp/traceUtils/oneFile/V2");
	
	File V1_addition = new File("test/unit/artifacts/cpp/traceUtils/oneFile-Addition/V1");
	File V2_addition = new File("test/unit/artifacts/cpp/traceUtils/oneFile-Addition/V2");
	
	File V1_multi = new File("test/unit/artifacts/cpp/traceUtils/multipleFiles/V1");
	File V2_multi = new File("test/unit/artifacts/cpp/traceUtils/multipleFiles/V2");
	
	
	File V1_deletion = new File("test/unit/artifacts/cpp/traceUtils/oneFile-Deletion/V1");
	File V2_deletion = new File("test/unit/artifacts/cpp/traceUtils/oneFile-Deletion/V2");
	
	
	@Test
	public void testOneSourceFile() throws IOException{
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		
		List<FileChangeInfo> diffs = mfd.extractDiffs( V1, V2 );
		
		
		
		SourceLinesMapper slm = new SourceLinesMapper( V1, V2, diffs);
		List<Integer> sourceLines = slm.getOriginalSourcesLines().get("/Recurser.cpp");
		
		int[] expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28};
//		System.out.println();
		
		
		
		check( expectedLines , sourceLines );
	
	}
	
	/**
	 * Bug 200
	 * @throws IOException
	 */
	@Test
	public void testOneSourceFileAddition() throws IOException{
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		
		int[] expectedLines;
		
		List<FileChangeInfo> diffs = mfd.extractDiffs( V1_addition, V2_addition );
		
		
		List<Integer> modifiedLines = TraceUtils.getModifiedLines(diffs, true).get("/Recurser.cpp");
		expectedLines = new int[]{26};
		check( expectedLines , modifiedLines );
		
		modifiedLines = TraceUtils.getModifiedLines(diffs, false).get("/Recurser.cpp");
		expectedLines = new int[]{23,24,25,29};
		check( expectedLines , modifiedLines );
		
		
		
		SourceLinesMapper slm = new SourceLinesMapper( V1, V2, diffs);
		List<Integer> sourceLines = slm.getOriginalSourcesLines().get("/Recurser.cpp");
		
		expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28};
//		System.out.println();
		
		
		
		check( expectedLines , sourceLines );
		
		
		sourceLines = slm.getModifiedSourcesLines().get("/Recurser.cpp");
		
		expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 26, 27, 28, 30, 31};
//		System.out.println();
		
		
		
		check( expectedLines , sourceLines );
	
	}
	
	
	
	
	
	/**
	 * Bug 200
	 * @throws IOException
	 */
	@Test
	public void testOneSourceFileDeletion() throws IOException{
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		
		int[] expectedLines;
		
		List<FileChangeInfo> diffs = mfd.extractDiffs( V1_deletion, V2_deletion );
		
		
		List<Integer> modifiedLines = TraceUtils.getModifiedLines(diffs, true).get("/Recurser.cpp");
		expectedLines = new int[]{23,24,25,29};
		check( expectedLines , modifiedLines );
		
		modifiedLines = TraceUtils.getModifiedLines(diffs, false).get("/Recurser.cpp");
		expectedLines = new int[]{26};
		check( expectedLines , modifiedLines );
		
		
		
		SourceLinesMapper slm = new SourceLinesMapper( V1, V2, diffs);
		List<Integer> sourceLines = slm.getOriginalSourcesLines().get("/Recurser.cpp");
		expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 26, 27, 28, 30, 31};
		check( expectedLines , sourceLines );
		
		
		sourceLines = slm.getModifiedSourcesLines().get("/Recurser.cpp");
		expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28};
		check( expectedLines , sourceLines );
	
	}
	
	
	
	
	
	
	
	
	@Test
	public void testMultipleSourceFiles() throws IOException, SourceMapperException{
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		
		List<FileChangeInfo> diffs = mfd.extractDiffs( V1_multi, V2_multi );
		
		
		
		SourceLinesMapper slm = new SourceLinesMapper( V1, V2, diffs);
		List<Integer> sourceLines = slm.getOriginalSourcesLines().get("/Recurser.cpp");
		
		int[] expectedLines = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28};
//		System.out.println();
		
		
		
		check( expectedLines , sourceLines );
		
		
		assertEquals( 23, slm.getCorrespondingLineInModifiedProject("/WorkersMap.cpp", 22) );
		
	}

	private void check(int[] expectedLines, List<Integer> sourceLines) {
		ArrayList<Integer> expectedLinesList = new ArrayList<Integer>();
		
		for ( int x : expectedLines ){
			expectedLinesList.add(x);
		}
		
		assertEquals(expectedLinesList, sourceLines);
	}
	
	@Test
	public void testBugFind() throws IOException, SourceMapperException {
		// TODO Auto-generated method stub
		Collection<String> originalFoldersAsString = new ArrayList<String>();
		originalFoldersAsString.add("test/unit/artifacts/cpp/traceUtils/findutils_V1/find/");
		
		
		try {
			File originalSoftwareObjDump = new File("/home/fabrizio/Workspaces/workspaceFindUtils/findUtilsCheck/BCT_DATA/findRegression/conf/files/scripts/originalSoftware.objdump");
			RegressionConfigObjDumpListener listener = GdbRegressionConfigCreator.extractFunctionsData(originalSoftwareObjDump , originalFoldersAsString );
			
			Map<String, FunctionMonitoringData> originalSoftwareFunctions = listener.getFunctionsData();
			
			ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
			
			List<File> originalSoftwareFolders = new ArrayList<File>();
			originalSoftwareFolders.add(new File("test/unit/artifacts/cpp/traceUtils/findutils_V1/find/"));
			
			List<File> modifiedSoftwareFolders= new ArrayList<File>();
			modifiedSoftwareFolders.add(new File("test/unit/artifacts/cpp/traceUtils/findutils_V2/find/"));
			
			
			Set<String> commonFiles = new HashSet<String>();
			commonFiles.add("find.c");
			List<FileChangeInfo> diffs_ = ModifiedFunctionsDetector.extractDiffs( commonFiles, originalSoftwareFolders.get(0), modifiedSoftwareFolders.get(0) );
			
			boolean found = containsDiff(diffs_, "/find.c", 1148);
//			assertTrue("An expected difference was not found in line 1148", found);
			
			List<FileChangeInfo> diffs = mfd.extractDiffsFromMultipleSourceFolders(originalSoftwareFolders , modifiedSoftwareFolders);
			SourceLinesMapper sourceMapper = new SourceLinesMapper( V1, V2, diffs );
			
			assertTrue( sourceMapper.containOriginalFile("/find.c") );
			
			List<Integer> or = sourceMapper.getOriginalSourcesLines().get("/find.c");
			List<Integer> mod = sourceMapper.getModifiedSourcesLines().get("/find.c");
			
			for ( int i = 0; i < or.size(); i++){
				System.out.println(or.get(i)+" "+mod.get(i));
			}
			
			assertEquals( 1156, sourceMapper.getCorrespondingLineInModifiedProject("find.c", 1149));
			assertEquals( 1157, sourceMapper.getCorrespondingLineInModifiedProject("find.c", 1150));
			
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	public void testBug2Find() throws IOException, SourceMapperException {
		// TODO Auto-generated method stub
		Collection<String> originalFoldersAsString = new ArrayList<String>();
		originalFoldersAsString.add("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap/src");
		
		Collection<String> modifiedFoldersAsString = new ArrayList<String>();
		modifiedFoldersAsString.add("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap-v2-addedDeleted/src");
		
		try {
			File originalSoftwareObjDump = new File("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap-v2-addedDeleted/BCT_DATA/CheckAddedDeleted/conf/files/scripts/originalSoftware.objdump");
			File modifiedSoftwareObjDump = new File("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap-v2-addedDeleted/BCT_DATA/CheckAddedDeleted/conf/files/scripts/modifiedSoftware.objdump");
			
			RegressionConfigObjDumpListener listener = GdbRegressionConfigCreator.extractFunctionsData(originalSoftwareObjDump , originalFoldersAsString );
			Map<String, FunctionMonitoringData> originalSoftwareFunctions = listener.getFunctionsData();
			
			RegressionConfigObjDumpListener listenerMod = GdbRegressionConfigCreator.extractFunctionsData(modifiedSoftwareObjDump, modifiedFoldersAsString );
			Map<String, FunctionMonitoringData> modifiedSoftwareFunctions = listenerMod.getFunctionsData();
			
			
			ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
			
			List<File> originalSoftwareFolders = new ArrayList<File>();
			originalSoftwareFolders.add(new File("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap/src"));
			
			List<File> modifiedSoftwareFolders= new ArrayList<File>();
			modifiedSoftwareFolders.add(new File("/home/fabrizio/Workspaces/workspaceCPP_RegressionExample/WorkersMap-v2-addedDeleted/src"));
			
			
			Set<String> commonFiles = new HashSet<String>();
			commonFiles.add("/WorkersMap.cpp");
//			List<FileChangeInfo> diffs_ = ModifiedFunctionsDetector.extractDiffs( commonFiles, originalSoftwareFolders.get(0), modifiedSoftwareFolders.get(0) );
			
			
			
			List<FileChangeInfo> diffs = mfd.extractDiffsFromMultipleSourceFolders(originalSoftwareFolders , modifiedSoftwareFolders);
			SourceLinesMapper sourceMapper = new SourceLinesMapper( diffs, originalSoftwareFunctions, modifiedSoftwareFunctions );
			
			assertEquals( 22, sourceMapper.getCorrespondingLineInModifiedProject("WorkersMap.cpp", 22));
			
			assertEquals( "_ZN10WorkersMap12getHisSalaryESs", sourceMapper.getCorrespondingFunction("WorkersMap.cpp", "_ZN10WorkersMap9getSalaryESs" ,22));
			 
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	@Test
	public void testBugGrepA() throws IOException, SourceMapperException {
		// TODO Auto-generated method stub
		Collection<String> originalFoldersAsString = new ArrayList<String>();
		originalFoldersAsString.add("/home/fabrizio/Programs/grep-2.6.1");
		
		Collection<String> modifiedFoldersAsString = new ArrayList<String>();
		modifiedFoldersAsString.add("/home/fabrizio/Programs/grep-2.6.2");
		
		try {
			File originalSoftwareObjDump = new File("/home/fabrizio/Workspaces/workspaceGrep_A/Analysis/BCT_DATA/CheckRegression/conf/files/scripts/originalSoftware.objdump");
			File modifiedSoftwareObjDump = new File("/home/fabrizio/Workspaces/workspaceGrep_A/Analysis/BCT_DATA/CheckRegression/conf/files/scripts/modifiedSoftware.objdump");
			
			RegressionConfigObjDumpListener listener = GdbRegressionConfigCreator.extractFunctionsData(originalSoftwareObjDump , originalFoldersAsString );
			Map<String, FunctionMonitoringData> originalSoftwareFunctions = listener.getFunctionsData();
			
			RegressionConfigObjDumpListener listenerMod = GdbRegressionConfigCreator.extractFunctionsData(modifiedSoftwareObjDump, modifiedFoldersAsString );
			Map<String, FunctionMonitoringData> modifiedSoftwareFunctions = listenerMod.getFunctionsData();
			
			
			ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
			
			List<File> originalSoftwareFolders = new ArrayList<File>();
			originalSoftwareFolders.add(new File("/home/fabrizio/Programs/grep-2.6.1"));
			
			List<File> modifiedSoftwareFolders= new ArrayList<File>();
			modifiedSoftwareFolders.add(new File("/home/fabrizio/Programs/grep-2.6.2"));
			
			
			Set<String> commonFiles = new HashSet<String>();
			commonFiles.add("/lib/exclude.c");
//			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs( commonFiles, originalSoftwareFolders.get(0), modifiedSoftwareFolders.get(0) );
			
			
			
			List<FileChangeInfo> diffs = mfd.extractDiffsFromMultipleSourceFolders(originalSoftwareFolders , modifiedSoftwareFolders);
			SourceLinesMapper sourceMapper = new SourceLinesMapper( diffs, originalSoftwareFunctions, modifiedSoftwareFunctions );
			
			List<Integer> originalLines = sourceMapper.originalSourcesLines.get("/lib/exclude.c");
			Set<String> keys = sourceMapper.originalSourcesLines.keySet();
			
			assertEquals( 344, sourceMapper.getCorrespondingLineInModifiedProject("/lib/exclude.c", 344));
			
//			assertEquals( "_ZN10WorkersMap12getHisSalaryESs", sourceMapper.getCorrespondingFunction("WorkersMap.cpp", "_ZN10WorkersMap9getSalaryESs" ,22));
			 
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Ignore("FIXME")
	@Test
	public void testBugVTT_ArcB() throws IOException, SourceMapperException {
		// TODO Auto-generated method stub
		Collection<String> originalFoldersAsString = new ArrayList<String>();
		originalFoldersAsString.add("/home/fabrizio/Experiments/VTT_ARCb/V0");
		
		Collection<String> modifiedFoldersAsString = new ArrayList<String>();
		modifiedFoldersAsString.add("/home/fabrizio/Experiments/VTT_ARCb/V2");
		
		try {
			File originalSoftwareObjDump = new File("/home/fabrizio/Experiments/VTT_ARCb/V2/BCT_DATA/MonitorRegression/conf/files/scripts/originalSoftware.objdump");
			File modifiedSoftwareObjDump = new File("/home/fabrizio/Experiments/VTT_ARCb/V2/BCT_DATA/MonitorRegression/conf/files/scripts/modifiedSoftware.objdump");
			
			RegressionConfigObjDumpListener listener = GdbRegressionConfigCreator.extractFunctionsData(originalSoftwareObjDump , originalFoldersAsString );
			Map<String, FunctionMonitoringData> originalSoftwareFunctions = listener.getFunctionsData();
			
			RegressionConfigObjDumpListener listenerMod = GdbRegressionConfigCreator.extractFunctionsData(modifiedSoftwareObjDump, modifiedFoldersAsString );
			Map<String, FunctionMonitoringData> modifiedSoftwareFunctions = listenerMod.getFunctionsData();
			
			
			ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
			
			List<File> originalSoftwareFolders = new ArrayList<File>();
			originalSoftwareFolders.add(new File("/home/fabrizio/Experiments/VTT_ARCb/V0"));
			
			List<File> modifiedSoftwareFolders= new ArrayList<File>();
			modifiedSoftwareFolders.add(new File("/home/fabrizio/Experiments/VTT_ARCb/V2"));
			
			
			Set<String> commonFiles = new HashSet<String>();
			commonFiles.add("P2P_Joints_TG.c");
//			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs( commonFiles, originalSoftwareFolders.get(0), modifiedSoftwareFolders.get(0) );
			
			
			
			List<FileChangeInfo> diffs = mfd.extractDiffsFromMultipleSourceFolders(originalSoftwareFolders , modifiedSoftwareFolders);
			SourceLinesMapper sourceMapper = new SourceLinesMapper( diffs, originalSoftwareFunctions, modifiedSoftwareFunctions );
			
			List<Integer> originalLines = sourceMapper.originalSourcesLines.get("P2P_Joints_TG.c");
			Set<String> keys = sourceMapper.originalSourcesLines.keySet();
			
			assertEquals( 1153, sourceMapper.getCorrespondingLineInModifiedProject("P2P_Joints_TG.c", 541));
			//FIXME: if v2 does not terminate with a new line it does not work
			assertEquals( 1154, sourceMapper.getCorrespondingLineInModifiedProject("P2P_Joints_TG.c", 542));
			
//			assertEquals( "_ZN10WorkersMap12getHisSalaryESs", sourceMapper.getCorrespondingFunction("WorkersMap.cpp", "_ZN10WorkersMap9getSalaryESs" ,22));
			 
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	private boolean containsDiff(List<FileChangeInfo> diffs_, String fileName, int line) {
		boolean found = false;
		for ( FileChangeInfo diff : diffs_ ){
			String name = diff.getFile().getName();
			System.out.println(name);
			if ( name.equals(fileName) ) {
				for ( Delta d : diff.getDeltas() ){
					if ( d.getStart() == line ){
						found = true;
					}
				}
			}
		}
		return found;
	}

	

}
