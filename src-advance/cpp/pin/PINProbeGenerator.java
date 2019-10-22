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
package cpp.pin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.ProcessRunner;

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.GdbRegressionConfigCreator;
import cpp.gdb.Parameter;
import edu.emory.mathcs.backport.java.util.Arrays;


public class PINProbeGenerator {

	
	
	private static final String IGNORED_BECAUSE_OF_ABNORMAL_NAME = "//Ignored because of abnormal name: ";
	protected String pinHome = System.getProperty(GdbRegressionConfigCreator.BCT_PIN_HOME);

	protected void writeHeader(BufferedWriter w) throws IOException{
		w.write("#include \"pin.H\"");
		w.newLine();
		w.write("#include <iostream>");
		w.newLine();
		w.write("#include <sstream>");
		w.newLine();
		w.write("#include <fstream>");
		w.newLine();
		w.write("#include <unistd.h>");
		w.newLine();
		w.write("std::ofstream TraceFile;");
		w.newLine();
		w.newLine();
		w.write("KNOB<string> KnobOutputFld(KNOB_MODE_WRITEONCE, \"pintool\",\"o\", \".\", \"specify out folder\");");
		w.newLine();
		w.newLine();
	}


	public void writePinProbe(File probesFolder, String probeName, Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		// TODO Auto-generated method stub
		
		functionsToMonitor = filterFunctionsToMonitor( functionsToMonitor );

		writeCppProbe(probesFolder, probeName, functionsToMonitor);
		writeCompileScript(probesFolder, probeName, functionsToMonitor);

		compile(probesFolder, probeName, functionsToMonitor);
	}

	private Set<FunctionMonitoringData> filterFunctionsToMonitor(
			Set<FunctionMonitoringData> functionsToMonitor) {
		HashSet<FunctionMonitoringData> filtered = new HashSet<>();
		
		for ( FunctionMonitoringData f : functionsToMonitor ){
			if( f.isImplementedWithinProject() ){
				filtered.add(f);
			}
		}
		
		return filtered;
	}


	protected void writeCompileScript(File probesFolder, String probeName, Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		BufferedWriter wr = new BufferedWriter(new FileWriter(new File( probesFolder, "compile.sh" )));

		wr.append("PINHOME="+pinHome);
		wr.newLine();

		wr.append("g++ -DBIGARRAY_MULTIPLIER=1 -Wall -Werror -Wno-unknown-pragmas -fno-stack-protector -DTARGET_IA32E -DHOST_IA32E -fPIC -DTARGET_LINUX  -I${PINHOME}/source/include/pin -I${PINHOME}/source/include/pin/gen -I${PINHOME}/extras/components/include -I${PINHOME}/extras/xed-intel64/include -I${PINHOME}/source/tools/InstLib -O3 -fomit-frame-pointer -fno-strict-aliasing   -c -o "+probeName+".o "+probeName+".cpp");
		wr.newLine();
		
		wr.append("g++ -shared -Wl,--hash-style=sysv -Wl,-Bsymbolic -Wl,--version-script=${PINHOME}/source/include/pin/pintool.ver    -o "+probeName+".so "+probeName+".o  -L${PINHOME}/intel64/lib -L${PINHOME}/intel64/lib-ext -L${PINHOME}/intel64/runtime/glibc -L${PINHOME}/extras/xed-intel64/lib -lpin -lxed -lpindwarf -ldl");
		wr.newLine();
		
		wr.close();
	}
	
	protected void compile(File probesFolder, String probeName, Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		

		String[] compile = new String[]{
				"g++",
				"-DBIGARRAY_MULTIPLIER=1",
				"-Wall",
				"-Werror",
				"-Wno-unknown-pragmas",
				"-fno-stack-protector",
				"-DTARGET_IA32E",
				"-DHOST_IA32E",
				"-fPIC",
				"-DTARGET_LINUX",
				"-I"+pinHome+"/source/include/pin",
				"-I"+pinHome+"/source/include/pin/gen",
				"-I"+pinHome+"/extras/components/include",
				"-I"+pinHome+"/extras/xed-intel64/include",
				"-I"+pinHome+"/source/tools/InstLib",
				"-O3",
				"-fomit-frame-pointer",
				"-fno-strict-aliasing",
				"-c",
				"-o",
				probeName+".o",
				probeName+".cpp"};
		List compileCmd = Arrays.asList(compile);
		
		String[] link = new String[]{
				"g++",
				"-shared",
				"-Wl,--hash-style=sysv",
				"-Wl,-Bsymbolic",
				"-Wl,--version-script="+pinHome+"/source/include/pin/pintool.ver",
				"-o",
				probeName+".so",
				probeName+".o",
				"-L"+pinHome+"/intel64/lib",
				"-L"+pinHome+"/intel64/lib-ext",
				"-L"+pinHome+"/intel64/runtime/glibc",
				"-L"+pinHome+"/extras/xed-intel64/lib",
				"-lpin",
				"-lxed",
				"-lpindwarf",
				"-ldl"};
		
		List linkCmd = Arrays.asList(link);
		{	
			int res = ProcessRunner.run(compileCmd, null, null, 0, probesFolder);
			if ( res != 0 ){
				System.out.println("PIN probe: compile issue");
			}
		}
		
		int res = ProcessRunner.run(linkCmd, null, null, 0, probesFolder);
		if ( res != 0 ){
			System.out.println("PIN probe: linking issue");
		}
	}

	private void writeCppProbe(File probesFolder, String probeName,
			Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		BufferedWriter wr = new BufferedWriter(new FileWriter(new File( probesFolder, probeName+".cpp" )));

		writeHeader(wr);


		writeFunctionsMonitors( wr, functionsToMonitor );

		writeImage( wr, functionsToMonitor );

		writeMain( wr );

		wr.close();
	}




	protected void writeMain(BufferedWriter wr) throws IOException {
		// TODO Auto-generated method stub
		wr.append("VOID Fini(INT32 code, VOID *v)");
		wr.append("{"); wr.newLine();
		wr.append("TraceFile.close();"); wr.newLine();
		wr.append("}"); wr.newLine();


		wr.append("int main(int argc, char *argv[])"); wr.newLine();
		wr.append("{"); wr.newLine();
		wr.append("PIN_InitSymbols();"); wr.newLine();

		wr.append(" if( PIN_Init(argc,argv) )"); wr.newLine();
		wr.append("{"); wr.newLine();
		wr.append("return -1;"); wr.newLine();
		wr.append("}"); wr.newLine();

		wr.append("long pid = getpid();"); wr.newLine();

		wr.append("std::stringstream ss;"); wr.newLine();
		wr.append("ss << KnobOutputFld.Value();"); wr.newLine();
		wr.append("ss << pid;"); wr.newLine();
		wr.append("ss << \".bdciTraceFunctions.out\";"); wr.newLine();

		wr.append("TraceFile.open(ss.str().c_str());"); wr.newLine();
		wr.append("TraceFile << hex;"); wr.newLine();
		wr.append("TraceFile.setf(ios::showbase);"); wr.newLine();

		wr.append("IMG_AddInstrumentFunction(Image, 0);"); wr.newLine();
		wr.append("PIN_AddFiniFunction(Fini, 0);"); wr.newLine();

		// Never returns
		wr.append("PIN_StartProgram();"); wr.newLine();

		wr.append("return 0;"); wr.newLine();
		wr.append("}"); wr.newLine();

	}


	private void writeImage(BufferedWriter wr,
			Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		wr.append("VOID Image(IMG img, VOID *v)");
		wr.newLine();

		wr.append("{");
		wr.newLine();

		for ( FunctionMonitoringData f : functionsToMonitor ){
			writeImageProbeSetupForFunction(wr, f);
		}

		wr.append("}");
		wr.newLine();

	}


	private void writeImageProbeSetupForFunction(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		
		if ( f.getMangledName().contains(".") ){
			wr.write(IGNORED_BECAUSE_OF_ABNORMAL_NAME+f.getMangledName());
			wr.newLine();
			return;
		}
		
		writeImageProbeSetupEnterExit(wr, f, getNameOfEnterProbe(f) , false);
		writeImageProbeSetupEnterExit(wr, f, getNameOfExitProbe(f), true);
	}


	public void writeImageProbeSetupEnterExit(BufferedWriter wr,
			FunctionMonitoringData f, String probeName, boolean isExit)
			throws IOException {
		wr.append("{");
		wr.newLine();

		wr.append("RTN mallocRtn = RTN_FindByName(img, \""+f.getMangledName()+"\");");
		wr.newLine();

		wr.append("if (RTN_Valid(mallocRtn))");
		wr.newLine();

		wr.append("    {");
		wr.newLine();

		wr.append("RTN_Open(mallocRtn);");
		wr.newLine();

		String point;
		if ( isExit ){
			point = "IPOINT_AFTER";
		} else {
			point = "IPOINT_BEFORE";
		}
		wr.append("RTN_InsertCall(mallocRtn, "+point+", (AFUNPTR)"+probeName+",");
		wr.newLine();

		wr.append("IARG_ADDRINT, \""+f.getMangledName()+"\",");
		wr.newLine();

		for( int i = 0; i < f.getAllArgs().size(); i++ ){
			wr.append("IARG_FUNCARG_ENTRYPOINT_VALUE, "+i+",");
			wr.newLine();
		}
		
		if ( isExit && ! f.isVoidReturn() ){
			wr.append("IARG_FUNCRET_EXITPOINT_VALUE, ");
			wr.newLine();
		}

		wr.append("IARG_END);");
		wr.newLine();

		wr.append("RTN_Close(mallocRtn);");
		wr.newLine();
		
		wr.append("}");
		wr.newLine();

		
		wr.append("}");
		wr.newLine();
	}


	private String getNameOfEnterProbe(FunctionMonitoringData f) {
		return "BDCI_Enter_"+f.getMangledName();
	}
	
	private String getNameOfExitProbe(FunctionMonitoringData f) {
		return "BDCI_Exit_"+f.getMangledName();
	}

	private void writeFunctionsMonitors(BufferedWriter wr,
			Set<FunctionMonitoringData> functionsToMonitor) throws IOException {

		for ( FunctionMonitoringData f : functionsToMonitor ){
			writeFunctionMonitors( wr, f );
		}
	}


	private void writeFunctionMonitors(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {

		if ( f.getMangledName().contains(".") ){
			wr.append(IGNORED_BECAUSE_OF_ABNORMAL_NAME+f.getMangledName());
			wr.newLine();
			return;
		}
		
		writeFunctionEnterProbe(wr, f);
		
		writeFunctionExitProbe(wr, f);
	}


	private void writeFunctionEnterProbe(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		wr.append("VOID "+getNameOfEnterProbe(f)+"(char* BDCI_fn");

		
		
		
		writeParameterHeaders(wr, f);

		wr.append(")");
		wr.newLine();

		wr.append("{");
		wr.newLine();
		
		writeFunctionEnter(wr, f);

		for ( Parameter p : f.getAllArgs() ){


			traceParameter(wr, p);
		}

		
		writeFunctionEnterExitEnd(wr, f);


		wr.append("}");
		wr.newLine();
	}
	
	private void writeFunctionExitProbe(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		wr.append("VOID "+getNameOfExitProbe(f)+"(char* BDCI_fn");

		
		
		
		int c = writeParameterHeaders(wr, f);
		if( ! f.isVoidReturn() ){
			//if ( c > 0 ){
				wr.append(',');		
			//}
			
			Parameter p = f.getReturnParameter();
			if ( canProcess(p) ){	
				wr.append( extractType(p)+" "+p.getName() );
			} else {
				wr.append( "void* "+p.getName());
			}
		}

		wr.append(")");
		wr.newLine();

		wr.append("{");
		wr.newLine();
		
		writeFunctionExit(wr, f);

		//ATTENTION: we cannot monitor parameter values at the exit point
//		for ( Parameter p : f.getAllArgs() ){
//			traceParameter(wr, p);
//		}

		if ( ! f.isVoidReturn() ){
			Parameter p = f.getReturnParameter();
			traceParameter(wr, p);
		}
		
		writeFunctionEnterExitEnd(wr, f);


		wr.append("}");
		wr.newLine();
	}


	public void traceParameter(BufferedWriter wr, Parameter p)
			throws IOException {
		if ( canProcess(p) ){
			writeTracingCode(wr,p);
		} else {
			if ( p.isPointer() ){
				writeTracingCode(wr, p);
			} else {
			wr.append( "//NOT TRACING: "+extractType(p)+" "+p.getName()+" ("+p.getMainType()+")" );
			wr.newLine();
			}
		}
	}


//	private boolean canProcessReturn(FunctionMonitoringData f) {
//		String rt = f.getReturnType();
//		
//		return Parameter.isBuiltinType(rt);
//	}


	private int writeParameterHeaders(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		int c = 0;
		for ( Parameter p : f.getAllArgs() ){
			
//			if (c!=0){
				wr.append(',');
//			}

			String pname = p.getName();
			if ( pname.isEmpty() ){
				p.setName( "p"+c );
			}
			
			if ( canProcess(p) ){
				wr.append( extractType(p)+" "+p.getName() );
			} else {
				wr.append( "void* "+p.getName());
			}
			
			c++;
		}
		
		return c;

	}


	protected void writeFunctionEnterExitEnd(BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		// TODO Auto-generated method stub
		
	}


	protected void writeFunctionEnter(BufferedWriter wr, FunctionMonitoringData f)
			throws IOException {
		wr.append( "TraceFile << \"!!!BCT-ENTER: \" << \""+f.getMangledName()+"\" << endl;" );
		wr.newLine();
	}


	private String extractType(Parameter p) {
		if ( p.isPointer() ){
			return p.getType()+"*";
		}
		return p.getType();
	}

	protected void writeTracingCode(BufferedWriter wr, Parameter p) throws IOException {
		wr.append( "TraceFile << \"!!!BCT-VARIABLE "+p.getName()+"\" << endl;");
		wr.newLine();
//		wr.append( "TraceFile << \""+p.getName()+"\" << endl;");
//		wr.newLine();
		if ( p.isPointer() ){
			wr.append( "{ std::stringstream ssp; ssp << (void*)"+p.getName()+"; TraceFile << ssp.str() << endl; }" );
				
		} else {
		wr.append( "{ std::stringstream ssp; ssp << "+p.getName()+"; TraceFile << ssp.str() << endl; }" );
		}
//		wr.append( "TraceFile << "+p.getName()+" << endl;");
		wr.newLine();
	}

	private boolean canProcess(Parameter p) {
		if ( p.isPointer() && p.isReference() ){
			return false;
		}
		
		return p.isBuiltinType();
		
	}


	protected void writeFunctionExit(BufferedWriter wr, FunctionMonitoringData f)
			throws IOException {
		// TODO Auto-generated method stub
		wr.append( "TraceFile << \"!!!BCT-EXIT: \" << \""+f.getMangledName()+"\" << endl;" );
		wr.newLine();
	}


}



