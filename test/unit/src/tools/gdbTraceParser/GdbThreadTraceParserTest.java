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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import tools.gdbTraceParser.GdbThreadTraceParser.CallData;

import dfmaker.core.Variable;

public class GdbThreadTraceParserTest {

	@Test
	public void testGetParameters_stackLine_Numbers() throws UnexpectedFormaException {
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		
		List<Variable> parameters = parser.getParameters( "#0  WorkersMap::addWorker (this=0xbffff090, personId=..., annualSalary=50000) at ../src/WorkersMap.cpp:31");
		
		List<Variable> expected = new ArrayList<Variable>();
		expected.add( new Variable( "this","0xbffff090", 1 ) );
		expected.add( new Variable( "personId","!NULL", 1 ) );
		expected.add( new Variable( "annualSalary","50000", 1 ) );
		
		assertEquals( expected.get(0), parameters.get(0) );
		assertEquals( expected.get(1), parameters.get(1) );
		assertEquals( expected.get(2), parameters.get(2) );
		
		assertEquals( expected, parameters );
		
	}
	
	@Test
	public void testGetParameters_stackLine_Strings() throws UnexpectedFormaException {
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		List<Variable> parameters = parser.getParameters( "#0  grepbuf (beg=0x806b000 \"WordA\nwordB\nWORDC\n\", lim=0x806b012 \"\") at grep.c:842");
		
		List<Variable> expected = new ArrayList<Variable>();
		expected.add( new Variable( "beg","\"WordA\nwordB\nWORDC\n\"", 1 ) );
		expected.add( new Variable( "lim","\"\"", 1 ) );
		
		assertEquals( expected, parameters );
		
	}
	
	@Test
	public void testGetParameters_incompleteSequence_BUG195() throws UnexpectedFormaException {
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		List<Variable> parameters = parser.getParameters( "#0  0x0804f2eb in is_reserved (str=0x805a6a0 \"\\204$\\255\", <incomplete sequence \373>, len=3221222399) at gperf.c:133");
		
		List<Variable> expected = new ArrayList<Variable>();
		expected.add( new Variable( "str","\"\\204$\\255\"", 1 ) ); //This could change after fixing TODO for incomplete sequences
		expected.add( new Variable( "len","3221222399", 1 ) );
		
		assertEquals( expected, parameters );
	
	}
	
	@Test
	public void testCreateVariable_objArray(){
		
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
		line.add("!!!BCT-ENTER: main");
		line.add("!!!BCT-locals");
		line.add("!!!BCT-VARIABLE jp");
		line.add("{[0] = {");
		line.add("    a1 = 0, ");
		line.add("    a2 = 0, ");
		line.add("    v = 0, ");
		line.add("    t1 = 0,"); 
		line.add("    t2 = 0, ");
		line.add("   t3 = 0, ");
		line.add("   delta = 0, ");
		line.add("   initPos = 0, ");
		line.add("   initVel = 0");
		line.add(" }}");
		line.add("!!!BCT-VARIABLE jl");
		line.add("  {[0] = 4,");
		line.add("  [1] = 3,");
		line.add("  [2] = 2,");
		line.add("  [3] = 1}");
		line.add("!!!BCT-locals-end");
		
		int lineCount = 0;
		for(String lin :line ){
			parser.processLine(lineCount++, lin);
		}
		
		CallData currentCall = parser.getCurrentCall();
		
		ArrayList<Variable> expectedVariables = new ArrayList<Variable>();

		expectedVariables.add(newVar( "jp[0].a1","0"));
		expectedVariables.add(newVar("jp[0].a2", "0"));
		expectedVariables.add(newVar("jp[0].v", "0"));
		expectedVariables.add(newVar("jp[0].t1", "0"));
		expectedVariables.add(newVar("jp[0].t2", "0"));
		expectedVariables.add(newVar("jp[0].t3", "0"));
		expectedVariables.add(newVar("jp[0].delta", "0"));
		expectedVariables.add(newVar("jp[0].initPos", "0"));
		expectedVariables.add(newVar("jp[0].initVel", "0"));
		expectedVariables.add(newVar("jl[0]", "4"));
		expectedVariables.add(newVar("jl[1]", "3"));
		expectedVariables.add(newVar("jl[2]", "2"));
		expectedVariables.add(newVar("jl[3]", "1"));
		
		for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
			assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
		}
		assertEquals(expectedVariables.size(), currentCall.localVariables.size());
		
		System.out.println(currentCall.localVariables);
	}

	@Test
	public void testCreateVariable_objArray_withNotNullAlsoForNotPointers(){
		
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
		line.add("!!!BCT-ENTER: main");
		line.add("!!!BCT-locals");
		line.add("!!!BCT-VARIABLE jp");
		line.add("{[0] = {");
		line.add("    a1 = 0, ");
		line.add("    a2 = 0, ");
		line.add("    v = 0, ");
		line.add("    t1 = 0,"); 
		line.add("    t2 = 0, ");
		line.add("   t3 = 0, ");
		line.add("   delta = 0, ");
		line.add("   initPos = 0, ");
		line.add("   initVel = 0");
		line.add(" }}");
		line.add("!!!BCT-VARIABLE jl");
		line.add("  {[0] = 4,");
		line.add("  [1] = 3,");
		line.add("  [2] = 2,");
		line.add("  [3] = 1}");
		line.add("!!!BCT-locals-end");
		
		parser.setLimitNotNullToPointers(false);
		int lineCount = 0;
		for(String lin :line ){
			parser.processLine(lineCount++, lin);
		}
		
		CallData currentCall = parser.getCurrentCall();
		
		ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
		expectedVariables.add(newVar( "jp[0]", "!NULL" ));
		expectedVariables.add(newVar( "jp[0].a1","0"));
		expectedVariables.add(newVar("jp[0].a2", "0"));
		expectedVariables.add(newVar("jp[0].v", "0"));
		expectedVariables.add(newVar("jp[0].t1", "0"));
		expectedVariables.add(newVar("jp[0].t2", "0"));
		expectedVariables.add(newVar("jp[0].t3", "0"));
		expectedVariables.add(newVar("jp[0].delta", "0"));
		expectedVariables.add(newVar("jp[0].initPos", "0"));
		expectedVariables.add(newVar("jp[0].initVel", "0"));
		expectedVariables.add(newVar("jl[0]", "4"));
		expectedVariables.add(newVar("jl[1]", "3"));
		expectedVariables.add(newVar("jl[2]", "2"));
		expectedVariables.add(newVar("jl[3]", "1"));
		
		for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
			assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
		}
		assertEquals(expectedVariables.size(), currentCall.localVariables.size());
		
		System.out.println(currentCall.localVariables);
	}

	
	private Variable newVar(String string, String string2) {
		// TODO Auto-generated method stub
		return new Variable(string, string2, 1);
	}

	@Test
	public void testObjArr2(){
	GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
	parser.setParsingOptions_ParseFunctions(true);
	parser.setParsingOptions_ParseVariables(true);
	List<String> line = new ArrayList<String>();
	line.add("!!!BCT-ENTER: main");
	line.add("!!!BCT-locals");
	line.add("!!!BCT-VARIABLE in"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-VARIABLE fb"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-VARIABLE jp"); 
	line.add("  {[0] = {");
	line.add("    a1 = 0,"); 
	line.add("    a2 = 0,"); 
	line.add("    v = 0,"); 
	line.add("    t1 = 0,"); 
	line.add("    t2 = 0,"); 
	line.add("    t3 = 0,"); 
	line.add("    delta = 0,"); 
	line.add("    initPos = 0,"); 
	line.add("    initVel = 0");
	line.add("  }}"); 
	line.add("!!!BCT-VARIABLE jl"); 
	line.add("  {[0] = 0,"); 
	line.add("  [1] = 0,"); 
	line.add("  [2] = 0,"); 
	line.add("  [3] = 0}"); 
	line.add("!!!BCT-locals-end");


	

	
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}
	
	CallData currentCall = parser.getCurrentCall();
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "in[0]", "0" ));
	expectedVariables.add(newVar( "fb[0]", "0" ));
	
	expectedVariables.add(newVar( "jp[0].a1","0"));
	expectedVariables.add(newVar("jp[0].a2", "0"));
	expectedVariables.add(newVar("jp[0].v", "0"));
	expectedVariables.add(newVar("jp[0].t1", "0"));
	expectedVariables.add(newVar("jp[0].t2", "0"));
	expectedVariables.add(newVar("jp[0].t3", "0"));
	expectedVariables.add(newVar("jp[0].delta", "0"));
	expectedVariables.add(newVar("jp[0].initPos", "0"));
	expectedVariables.add(newVar("jp[0].initVel", "0"));
	expectedVariables.add(newVar("jl[0]", "0"));
	expectedVariables.add(newVar("jl[1]", "0"));
	expectedVariables.add(newVar("jl[2]", "0"));
	expectedVariables.add(newVar("jl[3]", "0"));
	
	
	for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
		assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
	}
	
//	System.out.println(currentCall.localVariables);
}

	@Test
	public void testObjArr2_withNOtNullAlsoForNonPointers(){
	GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
	parser.setParsingOptions_ParseFunctions(true);
	parser.setParsingOptions_ParseVariables(true);
	List<String> line = new ArrayList<String>();
	line.add("!!!BCT-ENTER: main");
	line.add("!!!BCT-locals");
	line.add("!!!BCT-VARIABLE in"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-VARIABLE fb"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-VARIABLE jp"); 
	line.add("  {[0] = {");
	line.add("    a1 = 0,"); 
	line.add("    a2 = 0,"); 
	line.add("    v = 0,"); 
	line.add("    t1 = 0,"); 
	line.add("    t2 = 0,"); 
	line.add("    t3 = 0,"); 
	line.add("    delta = 0,"); 
	line.add("    initPos = 0,"); 
	line.add("    initVel = 0");
	line.add("  }}"); 
	line.add("!!!BCT-VARIABLE jl"); 
	line.add("  {[0] = 0,"); 
	line.add("  [1] = 0,"); 
	line.add("  [2] = 0,"); 
	line.add("  [3] = 0}"); 
	line.add("!!!BCT-locals-end");


	

	parser.setLimitNotNullToPointers(false);
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}
	
	CallData currentCall = parser.getCurrentCall();
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "in[0]", "0" ));
	expectedVariables.add(newVar( "fb[0]", "0" ));
	expectedVariables.add(newVar( "jp[0]", "!NULL" ));
	expectedVariables.add(newVar( "jp[0].a1","0"));
	expectedVariables.add(newVar("jp[0].a2", "0"));
	expectedVariables.add(newVar("jp[0].v", "0"));
	expectedVariables.add(newVar("jp[0].t1", "0"));
	expectedVariables.add(newVar("jp[0].t2", "0"));
	expectedVariables.add(newVar("jp[0].t3", "0"));
	expectedVariables.add(newVar("jp[0].delta", "0"));
	expectedVariables.add(newVar("jp[0].initPos", "0"));
	expectedVariables.add(newVar("jp[0].initVel", "0"));
	expectedVariables.add(newVar("jl[0]", "0"));
	expectedVariables.add(newVar("jl[1]", "0"));
	expectedVariables.add(newVar("jl[2]", "0"));
	expectedVariables.add(newVar("jl[3]", "0"));
	
	
	for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
		assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
	}
	
//	System.out.println(currentCall.localVariables);
}
	
	
	@Test
	public void testArrStructPtrs(){
	GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
	parser.setParsingOptions_ParseFunctions(true);
	parser.setParsingOptions_ParseVariables(true);
	List<String> line = new ArrayList<String>();
	line.add("!!!BCT-ENTER: main");
	line.add("!!!BCT-locals");
	line.add("pointer2 = 0x804b018");
	line.add("pnodes =   {[0] = 0x804b028,");
	line.add("[1] = 0x804b038,");
	line.add("[2] = 0x804b048,");
	line.add("[3] = 0x0}");

	line.add("!!!BCT-locals-end");


	

	
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}
	
	CallData currentCall = parser.getCurrentCall();
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "pointer2", "0x804b018" ));
	expectedVariables.add(newVar( "pnodes[0]", "0x804b028" ));
	expectedVariables.add(newVar( "pnodes[1]", "0x804b038" ));
	expectedVariables.add(newVar( "pnodes[2]", "0x804b048" ));
	expectedVariables.add(newVar( "pnodes[3]", "null" ));
	
	assertEquals(expectedVariables, currentCall.localVariables);
	
	System.out.println(currentCall.localVariables);
}
	
	
	@Test
	public void testObjArr3(){
	GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
	parser.setParsingOptions_ParseFunctions(true);
	parser.setParsingOptions_ParseVariables(true);
	List<String> line = new ArrayList<String>();
	line.add("!!!BCT-ENTER: main");
	line.add("!!!BCT-locals");
	line.add("!!!BCT-VARIABLE in"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-VARIABLE fb"); 
	line.add("  {[0] = 0}"); 
	line.add("!!!BCT-locals-end");


	

	
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}
	
	CallData currentCall = parser.getCurrentCall();
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "in[0]", "0" ));
	expectedVariables.add(newVar( "fb[0]", "0" ));
	
	assertEquals(expectedVariables, currentCall.localVariables);
	
	System.out.println(currentCall.localVariables);
}
	
	@Test
	public void testStruct(){
	GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
	parser.setParsingOptions_ParseFunctions(true);
	parser.setParsingOptions_ParseVariables(true);
	List<String> line = new ArrayList<String>();
	line.add("!!!BCT-ENTER: _ZN10WorkersMap9getSalaryESs");
	line.add("!!!BCT-stack");
	line.add("#0  WorkersMap::getSalary (this=..., workerId=...) at ../src/WorkersMap.cpp:20");
	line.add("#1  0x0804a0b5 in testNoWorker () at ../src/WorkersMapTest.cpp:33");
	line.add("#2  0x0804af8f in runTest (testCaseName=..., testCase=...) at ../src/WorkersMapTest.cpp:153");
	line.add("#3  0x0804b0ad in run (testToRun=...) at ../src/WorkersMapTest.cpp:164");
	line.add("#4  0x0804b4be in main (argc=..., argv=...) at ../src/WorkersMapTest.cpp:190");
	line.add("!!!BCT-stack-end");
	line.add("!!!BCT-args");
	line.add("this = 0xbfffeee4");
	line.add("workerId = {");
	line.add(" _M_dataplus = {");
	line.add(" <std::allocator<char>> = {");
	line.add("   <__gnu_cxx::new_allocator<char>> = {<No data fields>}, <No data fields>},");
	line.add(" members of std::basic_string<char, std::char_traits<char>, std::allocator<char> >::_Alloc_hider:");
	line.add(" _M_p = 0x8050034 \"PSTFRZ83D6YETZD\"");
	line.add(" }");
	line.add("}");
	line.add("!!!BCT-args-end");
	line.add("!!!BCT-locals");


	

	
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}
	
	CallData currentCall = parser.getCurrentCall();
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "this", "0xbfffeee4" ));
	expectedVariables.add(newVar( "workerId", "!NULL" ));
	expectedVariables.add(newVar( "workerId._M_dataplus", "!NULL" ));
	expectedVariables.add(newVar( "workerId._M_dataplus.<std::allocator<char>>", "!NULL" ));
	expectedVariables.add(newVar( "workerId._M_dataplus.<std::allocator<char>>.<__gnu_cxx::new_allocator<char>>", "!NULL" ));
	expectedVariables.add(newVar( "workerId._M_dataplus._M_p", "\"PSTFRZ83D6YETZD\"" ));
	
//	System.out.println(currentCall.parameters);
	
	assertEquals(expectedVariables, currentCall.parameters);
	
	
	}
	
	@Test
	public void testParsingExitCode(){
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		
		String line = "[Inferior 1 (process 4008) exited with code 01]";
		
		parser.processExitCodeWindows(line);
	}

	
	
	@Test
	public void testStruct_ABB_bug(){
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
		line.add("!!!BCT-ENTER: Voltage_AngleMemory_B_exec:999");
		line.add("!!!BCT-stack");
		line.add("#0  Voltage_ANgleMemory_B_exec (inst=...) at Voltage_AngleMemory_B.i:999");
		line.add("#1  0x0804a0b5 in main () at bctmain.i:54");
		line.add("!!!BCT-stack-end");
		line.add("!!!BCT-args");
		line.add("inst = 0xbfffeee4");
		line.add("!!!BCT-args-end");
		line.add("!!!BCT-locals");
		line.add("!!!BCT-VARIABLE inst");
		line.add("{");
		line.add(" input = {");
		line.add("   currentPh1 = 2653533, ");
		line.add("   currentPh2 = 265353332 ");
		line.add(" },");
		line.add(" output = {");
		line.add("   angleOut1 = 0, ");
		line.add("   angleOut2 = 0, ");
		line.add("   angleOut3 = 0, ");
		line.add("   phValid = 0 ");
		line.add(" },");
		line.add(" var = { ");
		line.add("  aboveCurrent = 1, ");
		line.add("  aboveFict = 	{[0] = 1, ");
		line.add("  [1] = 1 ");
		line.add("  [2] = 1}, ");
		line.add("  cntr = -6 ");
		line.add("  } ");
		line.add("} ");
		line.add("!!!BCT-locals-end");

		
		
		int lineCount = 0;
		for(String lin :line ){
			parser.processLine(lineCount++, lin);
		}

		CallData currentCall = parser.getCurrentCall();

		ArrayList<Variable> expectedVariables = new ArrayList<Variable>();

		expectedVariables.add(newVar( "inst.input.currentPh1", "2653533" ));
		expectedVariables.add(newVar( "inst.input.currentPh2", "265353332" ));
		expectedVariables.add(newVar( "inst.output.angleOut1", "0" ));
		expectedVariables.add(newVar( "inst.output.angleOut2", "0" ));
		expectedVariables.add(newVar( "inst.output.angleOut3", "0" ));
		expectedVariables.add(newVar( "inst.output.phValid", "0" ));
		expectedVariables.add(newVar( "inst.var.aboveCurrent", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[0]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[1]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[2]", "1" ));
		expectedVariables.add(newVar( "inst.var.cntr", "-6" ));




		//	System.out.println(currentCall.parameters);

		for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
			assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
		}

	}
	
	@Test
	public void testStruct_ABB_bug_withNotNullForNonPointers(){
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
		line.add("!!!BCT-ENTER: Voltage_AngleMemory_B_exec:999");
		line.add("!!!BCT-stack");
		line.add("#0  Voltage_ANgleMemory_B_exec (inst=...) at Voltage_AngleMemory_B.i:999");
		line.add("#1  0x0804a0b5 in main () at bctmain.i:54");
		line.add("!!!BCT-stack-end");
		line.add("!!!BCT-args");
		line.add("inst = 0xbfffeee4");
		line.add("!!!BCT-args-end");
		line.add("!!!BCT-locals");
		line.add("!!!BCT-VARIABLE inst");
		line.add("{");
		line.add(" input = {");
		line.add("   currentPh1 = 2653533, ");
		line.add("   currentPh2 = 265353332 ");
		line.add(" },");
		line.add(" output = {");
		line.add("   angleOut1 = 0, ");
		line.add("   angleOut2 = 0, ");
		line.add("   angleOut3 = 0, ");
		line.add("   phValid = 0 ");
		line.add(" },");
		line.add(" var = { ");
		line.add("  aboveCurrent = 1, ");
		line.add("  aboveFict = 	{[0] = 1, ");
		line.add("  [1] = 1 ");
		line.add("  [2] = 1}, ");
		line.add("  cntr = -6 ");
		line.add("  } ");
		line.add("} ");
		line.add("!!!BCT-locals-end");

		parser.setLimitNotNullToPointers(false);
		
		int lineCount = 0;
		for(String lin :line ){
			parser.processLine(lineCount++, lin);
		}

		CallData currentCall = parser.getCurrentCall();

		ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
		expectedVariables.add(newVar( "inst", "!NULL" ));
		expectedVariables.add(newVar( "inst.input", "!NULL" ));
		expectedVariables.add(newVar( "inst.input.currentPh1", "2653533" ));
		expectedVariables.add(newVar( "inst.input.currentPh2", "265353332" ));
		expectedVariables.add(newVar( "inst.output", "!NULL" ));
		expectedVariables.add(newVar( "inst.output.angleOut1", "0" ));
		expectedVariables.add(newVar( "inst.output.angleOut2", "0" ));
		expectedVariables.add(newVar( "inst.output.angleOut3", "0" ));
		expectedVariables.add(newVar( "inst.output.phValid", "0" ));
		expectedVariables.add(newVar( "inst.var", "!NULL" ));
		expectedVariables.add(newVar( "inst.var.aboveCurrent", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[0]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[1]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[2]", "1" ));
		expectedVariables.add(newVar( "inst.var.cntr", "-6" ));




		//	System.out.println(currentCall.parameters);

		for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
			assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
		}

	}
	
	
	@Test
	public void testStruct_ABB_bug_small(){
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
		line.add("!!!BCT-ENTER: Voltage_AngleMemory_B_exec:999");
		line.add("!!!BCT-stack");
		line.add("#0  Voltage_ANgleMemory_B_exec (inst=...) at Voltage_AngleMemory_B.i:999");
		line.add("#1  0x0804a0b5 in main () at bctmain.i:54");
		line.add("!!!BCT-stack-end");
		line.add("!!!BCT-args");
		line.add("inst = 0xbfffeee4");
		line.add("!!!BCT-args-end");
		line.add("!!!BCT-locals");
		line.add("!!!BCT-VARIABLE inst");
		line.add("{");
		
		line.add(" var = { ");
//		line.add("  aboveCurrent = 1, ");
		line.add("  aboveFict = 	{[0] = 1, ");
		line.add("  [1] = 1 ");
		line.add("  [2] = 1}, ");
		line.add("  cntr = -6 ");
		line.add("  } ");
		line.add("} ");
		line.add("!!!BCT-locals-end");

		parser.setLimitNotNullToPointers(false);
		
		int lineCount = 0;
		for(String lin :line ){
			parser.processLine(lineCount++, lin);
		}

		CallData currentCall = parser.getCurrentCall();

		ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
		expectedVariables.add(newVar( "inst", "!NULL" ));
		
		expectedVariables.add(newVar( "inst.var", "!NULL" ));
//		expectedVariables.add(newVar( "inst.var.aboveCurrent", "1" ));
//		expectedVariables.add(newVar( "inst.var.aboveFict", "!NULL" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[0]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[1]", "1" ));
		expectedVariables.add(newVar( "inst.var.aboveFict[2]", "1" ));
		expectedVariables.add(newVar( "inst.var.cntr", "-6" ));




		//	System.out.println(currentCall.parameters);

		for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
			assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
		}

	}
	
	@Test
	public void testStruct_bug_onelineStruct(){
		GdbThreadTraceParser parser = new GdbThreadTraceParser(new ArrayList<GdbThreadTraceListener>());
		parser.setParsingOptions_ParseFunctions(true);
		parser.setParsingOptions_ParseVariables(true);
		List<String> line = new ArrayList<String>();
	line.add("!!!BCT-POINT: EGexecute:295");
	line.add("!!!BCT-stack");
	line.add("#0  EGexecute (buf=..., size=..., endp=...) at search.c:295");
	line.add("#1  0x00000000004027f1 in grepbuf (beg=..., lim=...) at grep.c:604");
	line.add("#2  0x0000000000402a8e in grep (fd=..., file=..., stats=...) at grep.c:692");
	line.add("#3  0x0000000000402dcd in grepfile (file=..., stats=...) at grep.c:802");
	line.add("#4  0x0000000000403ed8 in main (argc=..., argv=...) at grep.c:1386");
	line.add("!!!BCT-stack-end");
	line.add("!!!BCT-args");
	line.add("buf = 0x624701 \"  char *bp, *p, *nl;\"...");
	line.add("size = 22755");
	line.add("endp = 0x7fffffffaf48");
	line.add("!!!BCT-args-end");
	line.add("!!!BCT-locals");
	line.add("buflim = 0x629fe4 \"\\n     && ((argv[opti\"...");
	line.add("beg = 0x6247f9 \"if (p > bp)\\n\\t  do\\n\\t \"...");
	line.add("end = 0x624701 \"  char *bp, *p, *nl;\"...");
	line.add("save = 32 ' '");
	line.add("backref = 0");
	line.add("start = 6440641");
	line.add("len = 0");
	line.add("kwsm = {");
	line.add("  index = 6440370,");
	line.add("  beg =     {[0] = 0x0},");//buggy
	line.add("  size =     {[0] = 6440705}");
	line.add("}");
	line.add("regs = {");
	line.add("  num_regs = 0,");
	line.add("  start = 0x0,");
	line.add("  end = 0x0");
	line.add("}");
	line.add("!!!BCT-locals-end");
	
	
	
	int lineCount = 0;
	for(String lin :line ){
		parser.processLine(lineCount++, lin);
	}

	CallData currentCall = parser.getCurrentCall();

	System.out.println(currentCall.localVariables);
	
	ArrayList<Variable> expectedVariables = new ArrayList<Variable>();
	expectedVariables.add(newVar( "buflim", "\"\\n     && ((argv[opti\"" ));
	expectedVariables.add(newVar( "beg", "\"if (p > bp)\\n\\t  do\\n\\t \"" ));
	expectedVariables.add(newVar( "end", "\"  char *bp, *p, *nl;\"" ));
	expectedVariables.add(newVar( "save", "32" ));
	expectedVariables.add(newVar( "backref", "0" ));
	expectedVariables.add(newVar( "start", "6440641" ));
	expectedVariables.add(newVar( "len", "0" ));
	expectedVariables.add(newVar( "kwsm.index", "6440370" ));
	expectedVariables.add(newVar( "kwsm.beg[0]", "null" ));
	expectedVariables.add(newVar( "kwsm.size[0]", "6440705" ));
	expectedVariables.add(newVar( "regs.num_regs", "0" ));
	expectedVariables.add(newVar( "regs.start", "null" ));
	expectedVariables.add(newVar( "regs.end", "null" ));
	
////	expectedVariables.add(newVar( "inst.var.aboveCurrent", "1" ));
////	expectedVariables.add(newVar( "inst.var.aboveFict", "!NULL" ));
//	expectedVariables.add(newVar( "inst.var.aboveFict[0]", "1" ));
//	expectedVariables.add(newVar( "inst.var.aboveFict[1]", "1" ));
//	expectedVariables.add(newVar( "inst.var.aboveFict[2]", "1" ));
//	expectedVariables.add(newVar( "inst.var.cntr", "-6" ));
//
//
//
//
	//	System.out.println(currentCall.parameters);

	for( int i = 0 ; i < expectedVariables.size() ; i ++ ){
		assertEquals("Unexpected at position "+i,expectedVariables.get(i), currentCall.localVariables.get(i));
	}

}
//0x0804ac62 in runTest (testCaseName=..., testCase=0x8049ffb <testAddWorker_one()>) at ../src/WorkersMap_test.cpp:119
}
