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

import org.junit.Test;

import tools.gdbTraceParser.FunctionEntryPointDetector;
import tools.gdbTraceParser.FunctionEntryPointDetector.FunctionEntryPoint;

public class FunctionEntryPointDetectorTest {

	@Test
	public void testFilteringOfAddressWithMultipleLocations() {
		
		 String content = "GNU gdb (GDB) 7.2-ubuntu\n"
		 +"Copyright (C) 2010 Free Software Foundation, Inc.\n"
		 +"License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>\n"
		 +"This is free software: you are free to change and redistribute it.\n"
		 +"There is NO WARRANTY, to the extent permitted by law.  Type \"show copying\"\n"
		 +"and \"show warranty\" for details.\n"
		 +"This GDB was configured as \"x86_64-linux-gnu\".\n"
		 +"For bug reporting instructions, please see:\n"
		 +"<http://www.gnu.org/software/gdb/bugs/>...\n"
		 +"Reading symbols from /home/BCT/workspace_BCT_Testing/ABB_11_V3_orig/ABB_11_V3...done.\n"
		 +"Breakpoint 1 at 0x804892f: file Learner.cpp, line 10. (2 locations)";


		
		FunctionEntryPoint entryPoint = FunctionEntryPointDetector.generateFunctionEntryPointFromGdb("Data::isEmpty()", content, 1);
		
		assertNull(entryPoint);
		
	}
	
	@Test
	public void testBug64() {
		
		 String content = "GNU gdb (GDB) 7.2-ubuntu\n"
		 +"Copyright (C) 2010 Free Software Foundation, Inc.\n"
		 +"License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>\n"
		 +"This is free software: you are free to change and redistribute it.\n"
		 +"There is NO WARRANTY, to the extent permitted by law.  Type \"show copying\"\n"
		 +"and \"show warranty\" for details.\n"
		 +"This GDB was configured as \"x86_64-linux-gnu\".\n"
		 +"For bug reporting instructions, please see:\n"
		 +"<http://www.gnu.org/software/gdb/bugs/>...\n"
		 +"Reading symbols from /home/BCT/workspace_BCT_Testing/ABB_11_V3_orig/ABB_11_V3...done.\n"
		 +"Breakpoint 1 at 0x400a6a: file Data.cpp, line 27.";


		
		FunctionEntryPoint entryPoint = FunctionEntryPointDetector.generateFunctionEntryPointFromGdb("Data::isEmpty()", content, 1);
		
		assertNotNull(entryPoint);
		
		assertEquals(27, entryPoint.getLine());
		assertEquals("0x400a6a", entryPoint.getAddress());
	}

}
