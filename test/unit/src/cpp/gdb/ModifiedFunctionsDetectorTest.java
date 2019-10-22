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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import util.componentsDeclaration.Component;
import util.componentsDeclaration.MatchingRule;
import util.componentsDeclaration.MatchingRuleInclude;

public class ModifiedFunctionsDetectorTest {

	
	
	
	@Ignore("FIXME")
	@Test
	public void testExtractDiffs() {
		
		File version0 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v0");
		File version1 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v1");
		File objdumpFile = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/originalSoftware.objdump");
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile);
		
		
		ArrayList<String> filesV0 = new ArrayList<String>();
		filesV0.add("WorkersMap.cpp");
		
		ArrayList<String> filesV1 = new ArrayList<String>();
		filesV1.add("WorkersMap.cpp");
		
		Set<String> commonFiles = ModifiedFunctionsDetector.getCommonFiles( filesV0, filesV0 );
		
		assertEquals(1, commonFiles.size());
		
		List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version0, version1);
		
		System.out.println(diffs);
		
		
		assertEquals(1, diffs.size());
		
		FileChangeInfo changeInfo = diffs.get(0);
		
		checkNotChanged(changeInfo,1,11);
		
		
		assertTrue ( changeInfo.isLineChanged(12) );
		assertTrue ( changeInfo.isLineChanged(13) );
		
		checkNotChanged(changeInfo,14,22);
		
		assertTrue ( changeInfo.isLineChanged(23) );
		
		checkNotChanged(changeInfo,24,36);
		
		assertTrue ( changeInfo.isLineChanged(37) );
		
		checkNotChanged(changeInfo,38,53);
		
		assertTrue ( changeInfo.isLineChanged(54) );
		assertTrue ( changeInfo.isLineChanged(55) );
		
		checkNotChanged(changeInfo,56,60);
		
	}
	
	
	private void checkNotChanged(FileChangeInfo changeInfo , int low, int up) {
		for( int i = low; i <= up; i++ ){
			assertFalse ( "Line set has been changed: "+i,changeInfo.isLineChanged(i) );
		}
	}
	
	


	@Ignore("FIXME")
	@Test
	public void testGetChangesAsComponent() {
		
		File version0 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v0");
		File version1 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v1");
		File objdumpFile = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/originalSoftware.objdump");
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile);
		
		List<MatchingRule> rules = changesComponent.getRules();
		
		System.out.println(rules);
		
		assertEquals( 5, rules.size() );
		
		

		
		
		//Constructors appear twice
		MatchingRuleInclude rule_12_1 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapC2Ev" );
		
		
		MatchingRuleInclude rule_12_2 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapC1Ev" );
		
		MatchingRuleInclude rule_23 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap9getSalaryESs" );
		
		MatchingRuleInclude rule_37 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap8isWorkerESs" );
		
		
		MatchingRuleInclude rule_55 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE" );
		
		
		List<MatchingRuleInclude> expectedRules = new ArrayList<MatchingRuleInclude>();
		expectedRules.add(rule_12_1);
		expectedRules.add(rule_12_2);
		expectedRules.add(rule_23);
		expectedRules.add(rule_37);
		expectedRules.add(rule_55);
		
		
		assertEquals( expectedRules, rules );
		
	}
	
	
	
	@Test
	public void testGetChangesAsComponent_MultipleLineChanges() {
		
		File version0 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v0");
		File version1 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/v2");
		File objdumpFile = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/originalSoftware.objdump");
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile);
		
		List<MatchingRule> rules = changesComponent.getRules();
		
		System.out.println(rules);
		
		
		

		
		
		//Constructors appear twice cause both are modified
		MatchingRuleInclude rule_12_1 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapC2Ev" );
		
		
		MatchingRuleInclude rule_12_2 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapC1Ev" );
		
		
		//Destructors appear cause we are not able to filter out changes to doc
		MatchingRuleInclude rule_16_1 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapD2Ev" );
		
		MatchingRuleInclude rule_16_2 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapD1Ev" );
		
		MatchingRuleInclude rule_16_3 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMapD0Ev" );
		
		
		
		MatchingRuleInclude rule_to_remove = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap9addWorkerESsl" );
		
		
		MatchingRuleInclude rule_23 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap9getSalaryESs" );
		
		MatchingRuleInclude rule_37 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap8isWorkerESs" );
		
		
		MatchingRuleInclude rule_55 = new MatchingRuleInclude(
				".*",
				".*",
				"_ZN10WorkersMap16getAverageSalaryESt4listISsSaISsEE" );
		
		
		List<MatchingRuleInclude> expectedRules = new ArrayList<MatchingRuleInclude>();
		
		expectedRules.add(rule_to_remove);
		
		expectedRules.add(rule_12_1);
		expectedRules.add(rule_12_2);
		
		expectedRules.add(rule_16_1);
		expectedRules.add(rule_16_2);
		expectedRules.add(rule_16_3);
		
		
		expectedRules.add(rule_23); 
		expectedRules.add(rule_37);
		expectedRules.add(rule_55);
		
		Comparator<MatchingRule> c = new Comparator<MatchingRule>() {

			@Override
			public int compare(MatchingRule o1, MatchingRule o2) {
				return o1.toString().compareTo(o2.toString());
			}
		};
		Collections.sort(expectedRules, c );
		Collections.sort(rules, c );
		//assertEquals( expectedRules, rules );
		
		System.out.println(rules);
		
		for ( int i = 0; i < expectedRules.size(); i++ ){
			System.out.println(rules.get(i));
			assertEquals(expectedRules.get(i), rules.get(i));
		}
	}
	
	
	
	
	@Test
	public void testExtractDiffs_indent_bug() {
		
		File version0 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/indent-2.2.9");
		File version1 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/indent-2.2.10");
		File objdumpFile = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/indent-2.2.9.objdump");
		File objdumpFileV2 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/indent-2.2.10.objdump");
		
		
		
		
		ArrayList<String> filesV0 = new ArrayList<String>();
		filesV0.add("indent.c");
		
		ArrayList<String> filesV1 = new ArrayList<String>();
		filesV1.add("indent.c");
		
		Set<String> commonFiles = ModifiedFunctionsDetector.getCommonFiles( filesV0, filesV0 );
		
		assertEquals(1, commonFiles.size());
		{
			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version0, version1);

			assertEquals(1, diffs.size());
			FileChangeInfo changeInfo = diffs.get(0);
			assertFalse ( changeInfo.isLineChanged(1512) );

		}

		{

			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version1, version0);			
			assertEquals(1, diffs.size());
			FileChangeInfo changeInfo = diffs.get(0);
			assertTrue ( changeInfo.isLineChanged(1603) );
		}
		
		
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile);
		
		
		for ( MatchingRule  rule : changesComponent.getRules() ){
			System.out.println(rule);
		}
		
		
		assertTrue ( changesComponent.acceptFunction("", "handle_token_decl") );
		
		
	}

	@Test
	public void testExtractDiffs_ifdef_bug2() {
		
		File version0 = new File( "/home/alberto/BCT/workspace_case_studies/gawk_b/gawk-3.1.1");
		File version1 = new File( "/home/alberto/BCT/workspace_case_studies/gawk_b/gawk-3.1.2");
		File objdumpFile = new File( "/home/alberto/BCT/workspace_case_studies/gawk_b/gawk_analysis/BCT_DATA/Regression_conf/conf/files/scripts/originalSoftware.objdump");
		//File objdumpFileV2 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/ifdef_bug_v1.objdump");
		
		
		
		ArrayList<String> filesV0 = new ArrayList<String>();
		filesV0.add("awkgram.c");
		
		ArrayList<String> filesV1 = new ArrayList<String>();
		filesV1.add("awkgram.c");
		
		Set<String> commonFiles = ModifiedFunctionsDetector.getCommonFiles( filesV0, filesV0 );
		
		assertEquals(1, commonFiles.size());
		
			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version0, version1);
		
			for ( FileChangeInfo diff : diffs){
				assertTrue ( diff.isLineChanged(1148) );
			}
			System.out.println(diffs);
	}
	
	@Ignore("FIXME")
	@Test
	public void testExtractDiffs_ifdef_bug() {
		
		File version0 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/ifdef_bug_v0");
		File version1 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/ifdef_bug_v1");
		File objdumpFile = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/ifdef_bug_v0.objdump");
		File objdumpFileV2 = new File( "test/unit/artifacts/cpp/modifiedFunctionsDetector/ifdef_bug_v1.objdump");
		
		
		
		
		ArrayList<String> filesV0 = new ArrayList<String>();
		filesV0.add("BCT_ifdef_parsing_bug.c");
		
		ArrayList<String> filesV1 = new ArrayList<String>();
		filesV1.add("BCT_ifdef_parsing_bug.c");
		
		Set<String> commonFiles = ModifiedFunctionsDetector.getCommonFiles( filesV0, filesV0 );
		
		assertEquals(1, commonFiles.size());
		{
			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version0, version1);

			assertEquals(1, diffs.size());
			FileChangeInfo changeInfo = diffs.get(0);
			assertFalse ( changeInfo.isLineChanged(14) );
			assertTrue ( changeInfo.isLineChanged(15) );
			assertTrue ( changeInfo.isLineChanged(16) );
			assertFalse ( changeInfo.isLineChanged(17) );
			assertFalse ( changeInfo.isLineChanged(18) );
			assertFalse ( changeInfo.isLineChanged(19) );
			assertTrue ( changeInfo.isLineChanged(20) );
			assertFalse ( changeInfo.isLineChanged(21) );
			assertFalse ( changeInfo.isLineChanged(22) );
			assertFalse ( changeInfo.isLineChanged(23) );
			assertFalse ( changeInfo.isLineChanged(24) );

		}
//
//		{
//
//			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version1, version0);			
//			assertEquals(1, diffs.size());
//			FileChangeInfo changeInfo = diffs.get(0);
//			assertTrue ( changeInfo.isLineChanged(17) );
//		}
		
		
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile );
		
		
		for ( MatchingRule  rule : changesComponent.getRules() ){
			System.out.println(rule);
		}
		
		
		assertTrue ( changesComponent.acceptFunction("", "myFunc") );
		
		
	}
	
	@Ignore("FIXME")
	@Test
	public void testExtractDiffs_find_bug() {
		
		File version0 = new File( "/home/alberto/BCT/workspace_case_studies/find_a_ok/findutils_V1/find/");
		File version1 = new File( "/home/alberto/BCT/workspace_case_studies/find_a_ok/findutils_V2/find/");
		File objdumpFile = new File( "/home/alberto/BCT/workspace_case_studies/find_a_ok/Regression_analysis/BCT_DATA/Regression_configuration/conf/files/scripts/originalSoftware.objdump");
		File objdumpFileV2 = new File( "/home/alberto/BCT/workspace_case_studies/find_a_ok/Regression_analysis/BCT_DATA/Regression_configuration/conf/files/scripts/modifiedSoftware.objdump");
		
		
		
		
		ArrayList<String> filesV0 = new ArrayList<String>();
		filesV0.add("find.c");
		
		ArrayList<String> filesV1 = new ArrayList<String>();
		filesV1.add("find.c");
		
		Set<String> commonFiles = ModifiedFunctionsDetector.getCommonFiles( filesV0, filesV0 );
		
		assertEquals(1, commonFiles.size());
		{
			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version0, version1);

			assertEquals(1, diffs.size());
			FileChangeInfo changeInfo = diffs.get(0);
			assertTrue ( changeInfo.isLineChanged(923) );
			

		}
//
//		{
//
//			List<FileChangeInfo> diffs = ModifiedFunctionsDetector.extractDiffs(commonFiles, version1, version0);			
//			assertEquals(1, diffs.size());
//			FileChangeInfo changeInfo = diffs.get(0);
//			assertTrue ( changeInfo.isLineChanged(17) );
//		}
		
		
		
		ModifiedFunctionsDetector mfd = new ModifiedFunctionsDetector();
		Component changesComponent = mfd.getChangesAsComponent(version0, version1, objdumpFile );
		
		
		for ( MatchingRule  rule : changesComponent.getRules() ){
			System.out.println(rule);
		}
		
		
//		assertTrue ( changesComponent.acceptFunction("", "myFunc") );
		
		
	}
	
}
