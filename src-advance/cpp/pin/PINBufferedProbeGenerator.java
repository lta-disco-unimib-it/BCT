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
import java.io.InputStream;
import java.util.Set;

import cpp.gdb.FunctionMonitoringData;
import cpp.gdb.Parameter;

import util.FileUtil;

public class PINBufferedProbeGenerator extends PINProbeGenerator {


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
		w.write("#include \"buffer.cpp\"");
		w.newLine();
		w.newLine();
		
	}

	@Override
	protected void writeFunctionEnter(BufferedWriter wr, FunctionMonitoringData f)
			throws IOException {
		
		String entryPoint = "!!!BCT-ENTER:";
		
		writeFunctionEnterExit(entryPoint, wr, f);
	}

	

	protected void writeFunctionEnterExit(String entryPoint, BufferedWriter wr,
			FunctionMonitoringData f) throws IOException {
		wr.append( "std::stringstream *ssp = new std::stringstream;");
		wr.newLine();
		wr.append( "(*ssp) << \""+entryPoint+"\" << \""+f.getMangledName()+"\" << endl;");
		wr.newLine();
		wr.append( "MLOG * mlog = static_cast<MLOG*>( PIN_GetThreadData( mlog_key, PIN_ThreadId() ) );");
		wr.newLine();
		wr.append( "mlog->Add(ssp);");	
		wr.newLine();
		wr.append( "(*ssp) << \"!!!BCT-locals\" << endl;");
		wr.newLine();
	}
	
	@Override
	protected void writeFunctionEnterExitEnd(BufferedWriter wr, FunctionMonitoringData f)
			throws IOException {
		wr.append( "(*ssp) << \"!!!BCT-locals-end\" << endl;");
		wr.newLine();
	}
	
	@Override
	protected void writeFunctionExit(BufferedWriter wr, FunctionMonitoringData f)
			throws IOException {
		String entryPoint = "!!!BCT-EXIT:";
		
		writeFunctionEnterExit(entryPoint, wr, f);
	}

	



	protected void writeMain(BufferedWriter wr) throws IOException {
		// TODO Auto-generated method stub
		wr.append("VOID Fini(INT32 code, VOID *v)");
		wr.append("{"); wr.newLine();
		
		wr.append("}"); wr.newLine();


		wr.append("int main(int argc, char *argv[])"); wr.newLine();
		wr.append("{"); wr.newLine();
		wr.append("PIN_InitSymbols();"); wr.newLine();

		wr.append(" if( PIN_Init(argc,argv) )"); wr.newLine();
		wr.append("{"); wr.newLine();
		wr.append("return -1;"); wr.newLine();
		wr.append("}"); wr.newLine();


		wr.append("mlog_key = PIN_CreateThreadDataKey(0);");wr.newLine();

		wr.append("PIN_AddThreadStartFunction(ThreadStart, 0);");wr.newLine();
		wr.append("PIN_AddThreadFiniFunction(ThreadFini, 0);");wr.newLine();



		wr.append("IMG_AddInstrumentFunction(Image, 0);"); wr.newLine();
		wr.append("PIN_AddFiniFunction(Fini, 0);"); wr.newLine();

		// Never returns
		wr.append("PIN_StartProgram();"); wr.newLine();

		wr.append("return 0;"); wr.newLine();
		wr.append("}"); wr.newLine();

	}


	protected void writeTracingCode(BufferedWriter wr, Parameter p) throws IOException {


		wr.append( "(*ssp) << \"!!!BCT-VARIABLE "+p.getName()+"\" << endl;");
		wr.newLine();
		//wr.append( "(*ssp) << \""+p.getName()+"\" << endl;");
		//wr.newLine();
		if ( isChar( p ) ){
			wr.append( "(*ssp) << \"'\"; (*ssp) << "+p.getName()+"; (*ssp) << \"'\"; (*ssp) << endl;");
		} else if ( p.isPointer() ) {
			wr.append( "(*ssp) << (void*)"+p.getName()+"; (*ssp) << endl;");
//		} else if ( isString( p ) ) { //FOLLOWING MAY LEAD TO CRASHES IF p is unallocated or not terminated by 0
//			wr.append( "(*ssp) << \"\\\"\"; (*ssp) << "+p.getName()+"; (*ssp) << \"\\\"\"; (*ssp) << endl;");
		} else {
			wr.append( "(*ssp) << "+p.getName()+"; (*ssp) << endl;");
		}
		wr.newLine();
		


	}
	
	private boolean isString(Parameter p) {
		return "char".equals(p.getType()) && p.isPointer() && p.getPointerOperatorsNum() == 1;
	}

	private boolean isChar(Parameter p) {
		return "char".equals(p.getType()) && ! p.isPointer() ;
	}

	@Override
	protected void writeCompileScript(File probesFolder, String probeName, Set<FunctionMonitoringData> functionsToMonitor) throws IOException {
		
		InputStream stream = PINBufferedProbeGenerator.class.getResourceAsStream("/cpp/pin/buffer.cpp");
		FileUtil.writeToFile(stream, new File(probesFolder, "buffer.cpp" ));
		
		BufferedWriter wr = new BufferedWriter(new FileWriter(new File( probesFolder, "compile.sh" )));

		wr.append("PINHOME="+pinHome);
		wr.newLine();

		wr.append("g++ -DBIGARRAY_MULTIPLIER=1 -Wall -Werror -Wno-unknown-pragmas -fno-stack-protector -DTARGET_IA32E -DHOST_IA32E -fPIC -DTARGET_LINUX  -I${PINHOME}/source/include/pin -I${PINHOME}/source/include/pin/gen -I${PINHOME}/extras/components/include -I${PINHOME}/extras/xed-intel64/include -I${PINHOME}/source/tools/InstLib -O3 -fomit-frame-pointer -fno-strict-aliasing   -c -o "+probeName+".o "+probeName+".cpp");
		wr.newLine();
		
		wr.append("g++ -shared -Wl,--hash-style=sysv -Wl,-Bsymbolic -Wl,--version-script=${PINHOME}/source/include/pin/pintool.ver    -o "+probeName+".so "+probeName+".o  -L${PINHOME}/intel64/lib -L${PINHOME}/intel64/lib-ext -L${PINHOME}/intel64/runtime/glibc -L${PINHOME}/extras/xed-intel64/lib -lpin -lxed -lpindwarf -ldl");
		wr.newLine();
		
		wr.close();
	}

}
