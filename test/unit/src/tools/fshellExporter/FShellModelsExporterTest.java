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
package tools.fshellExporter;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.LineData;
import cpp.gdb.SourceLinesMapper;
import cpp.gdb.SourceMapperException;
import cpp.gdb.VariablesDetector;

import tools.fshellExporter.FShellModelsExporter.ModelsData;
import tools.violationsAnalyzer.ViolationsUtil;

@Ignore("Deprecated")
public class FShellModelsExporterTest {

	@Test
	public void test() {
		assertEquals( "parameter[0] > 0", ModelsExporter.processExpression("parameter[0] > 0") );
		
		assertEquals( "var > 0", ModelsExporter.processExpression("var > 0"));
		
		assertEquals( "enabled == 0", ModelsExporter.processExpression("enabled == 0"));
		
		assertEquals( "argc == 13", ModelsExporter.processExpression("argc == 13"));
		
		assertEquals( "alt_sep == 0 || alt_sep == 1 || alt_sep == 2", ModelsExporter.processExpression("alt_sep one of { 0, 1, 2 }"));
		
		assertEquals( "! (enabled == 0) || (enabled == 0 && alt_sep == 0)", ModelsExporter.processExpression("(enabled == 0) ==> (alt_sep == 0)"));
		
		//assertEquals( "! (( P != 0 && P.I != 0 )) || (( P != 0 && P.I != 0 ) && P.I.x > 0)", ModelsExporter.processExpression("( P != null and P.I != null ) ==> ( P.I.x > 0 )"));
		
		assertEquals( "! (( P != 0 && P.I != 0 && P.V != 0 )) || (( P != 0 && P.I != 0 && P.V != 0 ) && P.I.x > 0)", ModelsExporter.processExpression("( P != null and P.I != null and P.V != null ) ==> ( P.I.x > P.V.x )"));
	}
	
	@Test
	public void test3I() {
	assertEquals( "! (( P != 0 && P.I != 0 && P.V != 0 )) || (( P != 0 && P.I != 0 && P.V != 0 ) && P.I.x > 0)", ModelsExporter.processExpression("( P != null and P.I != null and P.V != null ) ==> ( P.I.x > P.V.x )"));
	}
	
	@Test
	public void testPointers(){
		assertEquals( "! (p != 0) || (p != 0 && p.x == 2)",  ModelsExporter.processExpression( "(p != null)  ==>  (p.x == 2)" ) );
//		assertEquals( "! (p != 0) || (p != 0 && p->x == 2)",  ModelsExporter.processExpression( "(p != null)  ==>  (p->x == 2)" ) );
		assertEquals( null,  ModelsExporter.processExpression( "*p != null" ) );
		assertEquals( "(*p) != 0",  ModelsExporter.processExpression( "*p != 0" ) );
		assertEquals( "(*p).x != 2",  ModelsExporter.processExpression( "*p.x != 2" ) );
		assertEquals( "! (p != 0) || (p != 0 && (*p).x == 2)",  ModelsExporter.processExpression( "(p != null)  ==>  (*p.x == 2)" ) );
		assertEquals( "! (p != 0) || (p != 0 && (*p).x == 2)",  ModelsExporter.processExpression( "(*p != null)  ==>  (*p.x == 2)" ) );
		assertEquals( "! (p != 0) || (p != 0 && (*p).x == 2)",  ModelsExporter.processExpression( "( *p != null )  ==>  (*p.x == 2)" ) );
		assertEquals( "! (p != 0) || (p != 0 && (*p).x == 2)",  ModelsExporter.processExpression( " ( *p != null )  ==>  (*p.x == 2)" ) );
		assertEquals( "! (point != 0) || (point != 0 && (*point).x == 2)",  ModelsExporter.processExpression( " ( *point != null )  ==>  (*point.x == 2)" ) );
//		assertEquals( "! (p.x != 0) || (p.x != 0 && (*p.x).t == 2)",  ModelsExporter.processExpression( " ( *(p.x) != null )  ==>  (*p.x.t == 2)" ) );
		
		Set<String> varsToIdentify = ViolationsUtil.extractParentVariablesNoStar("ERROR != 0");
		
		Set<String> varsInFile = VariablesDetector.identifyVariableNamesInFile(varsToIdentify, 
				new File("/home/BCT/workspace_BCT_Testing/CBMC_CPP_StructRefs_V0/Point.h"), 
				19, 
				19 //violation is observed at the beginning of line, so we stop looking at the line where violation is found (the passed line is excluded)
				);
		
		System.out.println(varsInFile);
	}
	
	@Ignore
	@Test
	public void testToImplement() {
		
		assertEquals( "parameter[0] > 0", ModelsExporter.processExpression("(alt_sep & need_downward_RA) == 0"));		
	}
	
	@Test
	public void testOrig(){
		assertEquals( null, ModelsExporter.processExpression("jl[2] == orig(jl[2])") );
		assertEquals( null, ModelsExporter.processExpression("(jp[0] != null and orig(jp[0]) != null)  ==>  (jp[0].a1 == orig(jp[0].a1))") );
	}
	
	@Test
	public void testReturnValue(){
		assertEquals( "return != 0", ModelsExporter.processExpression("returnValue.eax != 0") );
		assertEquals( "return >= 1", ModelsExporter.processExpression("returnValue.eax >= 1") );
		assertEquals( "return != 0", ModelsExporter.processExpression("returnValue != null") );
		assertEquals( "a % return == 0", ModelsExporter.processExpression("a % returnValue.eax == 0") );
		assertEquals( "a <= return", ModelsExporter.processExpression("a <= returnValue.eax") );
		assertEquals( "return % a == 0", ModelsExporter.processExpression("returnValue.eax % a == 0") );
	}
	
	@Test
	public void testExportArray(){
		
		assertEquals( "fb[0] < totalTime", ModelsExporter.processExpression("fb[0] < totalTime") );
		
		assertEquals( "i <= in[0]", ModelsExporter.processExpression("i <= in[0]") );
		
		assertEquals( "fb[0] != jp[0].a1", ModelsExporter.processExpression("fb[0] != jp[0].a1") );
		
		assertEquals( "! (jp[0] != 0) || (jp[0] != 0 && fb[0] != jp[0].a1)", ModelsExporter.processExpression("(jp[0] != null)  ==>  (fb[0] != jp[0].a1)" ) );

		assertEquals( "jp[0].a1 != jp[0].t3" , ModelsExporter.processExpression("jp[0].a1 != jp[0].t3") );
		
		assertEquals("! (jp[0] != 0) || (jp[0] != 0 && jp[0].a1 != jp[0].t3)", ModelsExporter.processExpression("(jp[0] != null) ==> (jp[0].a1 != jp[0].t3)") );
		
//				(jp[0] != null)  ==>  (jl[3] % jp[0].a2 == 0)
				
		assertEquals( "jl[3] % jp[0].a2 == 0" , ModelsExporter.processExpression("jl[3] % jp[0].a2 == 0") );
	}
	
	@Test
	public void test2() {
		FShellModelsExporter exp = new FShellModelsExporter(new File(""));
		
		ModelsData data = exp.parseLineInvariant("cover @file('tcas.c')&NOT(@line(58)).{ !(High_Confidence != 0)}.@line(58)");
		assertEquals("tcas.c", data.getFileOriginal());
		assertEquals(58, data.getLineOriginal());
		assertEquals("High_Confidence != 0", data.getAssertion());
		
		data = exp.parseLineInvariant("cover @file('tcas.c')&NOT(@line(58)).{ !(Other_Capability == 0 || Other_Capability == 1 || Other_Capability == 2)}.@line(58)");
		assertEquals("tcas.c", data.getFileOriginal());
		assertEquals(58, data.getLineOriginal());
		assertEquals("Other_Capability == 0 || Other_Capability == 1 || Other_Capability == 2", data.getAssertion());
		
		data = exp.parseLineInvariant("cover @file('tcas.c')&NOT(@line(58)).{ !(Two_of_Three_Reports_Valid == -1 || Two_of_Three_Reports_Valid == 0 || Two_of_Three_Reports_Valid == 1)}.@line(58)");
		assertEquals("tcas.c", data.getFileOriginal());
		assertEquals(58, data.getLineOriginal());
		assertEquals("Two_of_Three_Reports_Valid == -1 || Two_of_Three_Reports_Valid == 0 || Two_of_Three_Reports_Valid == 1", data.getAssertion());
		
		
	}
	
	@Test
	public void testParseBackEntry() throws SourceMapperException {
		FShellModelsExporter exp = new FShellModelsExporter(new File(""));
		
		SourceLinesMapper mapperMock = EasyMock.createMock(SourceLinesMapper.class);
		
		FunctionMonitoringData functionDataMock = EasyMock.createMock(FunctionMonitoringData.class);
		EasyMock.expect(functionDataMock.getFirstSourceLine()).andReturn(new Integer(67));
		
//		FunctionMonitoringData functionDataMock = new FunctionMonitoringData("ALIM");
//		functionDataMock.addLine(new LineData("tcas.c", 67));
		
		EasyMock.expect(mapperMock.getCorrespondingFunction("tcas.c", "ALIM")).andReturn("ALIM");
		EasyMock.expect(mapperMock.getCorrespondingFunctionData("ALIM")).andReturn(functionDataMock);
		
		EasyMock.replay(mapperMock);
		EasyMock.replay(functionDataMock);
		
		
		ModelsData data = exp.parseEntryInvariant("cover @file('tcas.c')&NOT(@entry(ALIM)).{ !(Cur_Vertical_Sep != 0)}.@entry(ALIM)", mapperMock);
		assertEquals("tcas.c", data.getFileOriginal());
		assertEquals(68, data.getLineOriginal());
		assertEquals("Cur_Vertical_Sep != 0", data.getAssertion());
		

		
		EasyMock.verify(mapperMock);
	}

}
