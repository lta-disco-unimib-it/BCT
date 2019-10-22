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
package tools.gdbTraceParser;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class GdbThreadTraceParserUtilTest {

	@Test
	public void testExitWindows(){
		String line = "[Inferior 1 (process 4008) exited with code 01]";
		
		assertTrue( GdbTraceUtil.isProcessExitCodeWindows(line) );
		
		
		Pattern p = Pattern.compile("\\[Inferior \\d+ \\(process \\d+\\) exited with code (\\d+)\\]");
		Matcher matcher = p.matcher(line);
		assertTrue(matcher.matches());
		
		String exitCodeString = matcher.group(1);
		assertEquals( "01", exitCodeString);
		Integer exitCode = Integer.valueOf(exitCodeString);
	}
}
