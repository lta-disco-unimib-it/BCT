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
package tools.violationsAnalyzer;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ViolationsUtilTest {

	@Test
	public void parseAddress64(){
		Object value64 = ViolationsUtil.parseValue("0xffbee5894855c3c9");
		
		Object value32 = ViolationsUtil.parseValue("0x4855c3c9");
		
		
		
		assertNotNull(value64);
		assertEquals(value32, value64);
	}
	
	@Test
	public void testExtractVariables() {



		{	
			Set<String> extractedVars = ViolationsUtil.extractVariables("a != 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			assertEquals(expected, extractedVars);
		}

		{	
			Set<String> extractedVars = ViolationsUtil.extractVariables("a % b == 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			expected.add("b");
			assertEquals(expected, extractedVars);
		}
		
		{	
			Set<String> extractedVars = ViolationsUtil.extractVariables("a % 5 == 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			assertEquals(expected, extractedVars);
		}
		
		{	
			Set<String> extractedVars = ViolationsUtil.extractVariables("*a != 0");
			Set<String> expected = new HashSet<String>();
			expected.add("*a");
			assertEquals(expected, extractedVars);
		}

		{	
			Set<String> extractedVars = ViolationsUtil.extractVariables("jl[0] >= 0");
			Set<String> expected = new HashSet<String>();
			expected.add("jl[0]");
			assertEquals(expected, extractedVars);
		}

		{
			Set<String> extractedVars = ViolationsUtil.extractVariables("(*this != null)  ==>  (*this.val one of { 0, 5 })");
			Set<String> expected = new HashSet<String>();
			expected.add("*this");
			expected.add("*this.val");
			assertEquals(expected, extractedVars);
		}
		
		//
		{
			Set<String> extractedVars = ViolationsUtil.extractVariables("*this.items._M_t._M_impl.<std::allocator<std::_Rb_tree_node<std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>const,int>>>>.<__gnu_cxx::new_allocator<std::_Rb_tree_node<std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>const,int>>>> != null");
			Set<String> expected = new HashSet<String>();
			expected.add("*this.items._M_t._M_impl.<std::allocator<std::_Rb_tree_node<std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>const,int>>>>.<__gnu_cxx::new_allocator<std::_Rb_tree_node<std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>const,int>>>>");
//			expected.add("*this.val");
			assertEquals(expected, extractedVars);
		}
		
		//
		{
			Set<String> extractedVars = ViolationsUtil.extractVariables("returnValue.eax == -1073746252");
			Set<String> expected = new HashSet<String>();
			expected.add("returnValue.eax");
//			expected.add("*this.val");
			assertEquals(expected, extractedVars);
		}
	}

	@Test
	public void testExtractParentVariables() {

		{	
			Set<String> extractedVars = ViolationsUtil.extractParentVariables("a != 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			assertEquals(expected, extractedVars);
		}

		{	
			Set<String> extractedVars = ViolationsUtil.extractParentVariables("jl[0] >= 0");
			Set<String> expected = new HashSet<String>();
			expected.add("jl");
			assertEquals(expected, extractedVars);
		}

		{
			Set<String> extractedVars = ViolationsUtil.extractParentVariables("(*this != null)  ==>  (*this.val one of { 0, 5 })");
			Set<String> expected = new HashSet<String>();
			expected.add("*this");
			assertEquals(expected, extractedVars);
		}
	}

	@Test
	public void testExtractParentVariablesNoStar() {

		{	
			Set<String> extractedVars = ViolationsUtil.extractParentVariablesNoStar("a != 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			assertEquals(expected, extractedVars);
		}

		{	
			Set<String> extractedVars = ViolationsUtil.extractParentVariablesNoStar("jl[0] >= 0");
			Set<String> expected = new HashSet<String>();
			expected.add("jl");
			assertEquals(expected, extractedVars);
		}

		{
			Set<String> extractedVars = ViolationsUtil.extractParentVariablesNoStar("(*this != null)  ==>  (*this.val one of { 0, 5 })");
			Set<String> expected = new HashSet<String>();
			expected.add("this");
			assertEquals(expected, extractedVars);
		}

		{	
			Set<String> extractedVars = ViolationsUtil.extractParentVariablesNoStar("*a != 0");
			Set<String> expected = new HashSet<String>();
			expected.add("a");
			assertEquals(expected, extractedVars);
		}
	}

	@Test
	public void testExtractAnomalousVariables() {

		Map<String, Object> localVariables = new HashMap<String, Object>();
		localVariables.put("*this", "!NULL");
		localVariables.put("*this.val", "-1");
		localVariables.put("*data", "1");
		
		List<String> extractedVars = ViolationsUtil.extractAnomalousVariables("(*this != null)  ==>  (*this.val one of { 0, 5 })", localVariables);
		Collections.sort(extractedVars);
		
		System.out.println(extractedVars);
		
		String[] expected = new String[]{"*this","*this.val"};
		assertArrayEquals(expected, extractedVars.toArray(new String[0]));
	}

}
