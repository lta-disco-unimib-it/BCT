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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class VariablesDetectorTest {

	@Test
	public void testFindNewGlobalVariablesInLine_foundNotFound(){

		List<String> vars = new ArrayList<String>();
		vars.add("counter");
		vars.add("score");
		String line = "line = counter + 1";

		List<String> found = VariablesDetector.findNewGlobalVariablesInLine(line, vars, VariablesDetector.stringMatcher);

		List<String> expectedNotFound = new ArrayList<String>();
		expectedNotFound.add("score");
		List<String> expectedFound = new ArrayList<String>();
		expectedFound.add("counter");
		assertEquals( expectedFound, found );
		assertEquals( expectedNotFound, vars );
	}

	@Test
	public void testFindNewGlobalVariablesInLine_doublePresence(){

		List<String> vars = new ArrayList<String>();
		vars.add("counter");
		String line = "counter = counter + 1";

		List<String> found = VariablesDetector.findNewGlobalVariablesInLine(line, vars, VariablesDetector.stringMatcher);

		List<String> expectedNotFound = new ArrayList<String>();
		List<String> expectedFound = new ArrayList<String>();
		expectedFound.add("counter");
		assertEquals( expectedFound, found );
		assertEquals( expectedNotFound, vars );
	}

	@Test
	public void testFindNewGlobalVariablesInLine_array(){

		{
			List<String> vars = new ArrayList<String>();
			vars.add("nodes");
			String line = "nodes[3] = x + 1";

			List<String> found = VariablesDetector.findNewGlobalVariablesInLine(line, vars, VariablesDetector.stringMatcher);

			List<String> expectedNotFound = new ArrayList<String>();
			List<String> expectedFound = new ArrayList<String>();
			expectedFound.add("nodes");
			assertEquals( expectedFound, found );
			assertEquals( expectedNotFound, vars );
		}

		{
			List<String> vars = new ArrayList<String>();
			vars.add("nodes[3]");
			String line = "nodes[3] = x + 1";

			List<String> found = VariablesDetector.findNewGlobalVariablesInLine(line, vars, VariablesDetector.stringMatcher);

			List<String> expectedNotFound = new ArrayList<String>();
			List<String> expectedFound = new ArrayList<String>();
			expectedFound.add("nodes[3]");
			assertEquals( expectedFound, found );
			assertEquals( expectedNotFound, vars );
		}

		{
			List<String> vars = new ArrayList<String>();
			vars.add("nodes[3]");
			String line = "nodes = x + 1";

			List<String> found = VariablesDetector.findNewGlobalVariablesInLine(line, vars, VariablesDetector.stringMatcher);

			List<String> expectedNotFound = new ArrayList<String>();
			List<String> expectedFound = new ArrayList<String>();
			expectedFound.add("nodes[3]");
			assertEquals( expectedFound, found );
			assertEquals( expectedNotFound, vars );
		}

	}


}
