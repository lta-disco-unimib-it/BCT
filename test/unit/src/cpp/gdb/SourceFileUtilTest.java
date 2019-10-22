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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SourceFileUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExtractVariablesFromLine() {
		Set<String> expected;
		
		expected = new HashSet<>(Arrays.asList("myStruct","var","t"));
		assertEquals( expected, SourceFileUtil.extractVariablesFromLine("struct myStruct var = t;") );
	}

	@Test
	public void testExtractVariablesDefinedInLine() {
		
		Set<String> expected;
		
		expected = new HashSet<>(Arrays.asList("x"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("int x = 5;") );
		
		expected = new HashSet<>(Arrays.asList("x"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("x = 5;") );
		
		expected = new HashSet<>(Arrays.asList("x"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("		x = 5;") );
		
		expected = new HashSet<>(Arrays.asList("x"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("x=5;") );
		
		
		expected = new HashSet<>(Arrays.asList("var"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("struct myStruct var = t;") );
		
		expected = new HashSet<>(Arrays.asList("var"));
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("struct myStruct **var = t;") );
		
		
		expected = new HashSet<>();
		assertEquals( expected, SourceFileUtil.extractVariablesDefinedInLine("if( x == 5 )") );
	}

}
