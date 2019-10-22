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

public class GdbTraceUtil {

	public static boolean isThreadStart(String line) {
		return line.startsWith("[New Thread");
	}

	public static boolean isBctEnter(String line) {
		return line.startsWith("!!!BCT-ENTER:");
	}


	public static boolean isBctPoint(String line) {
		return line.startsWith("!!!BCT-POINT:");
	}
	
	public static boolean isBctExit(String line) {
		return line.startsWith("!!!BCT-EXIT:");
	}

	public static boolean isCurrentMethodOnStack(String line) {
		return line.startsWith("#0");
	}

	public static boolean isThreadSwitch(String line) {
		return line.startsWith("[Switching to Thread");
		
	}

	public static boolean isEaxRegisterData(String line) {
		return line.startsWith("eax");
	}

	public static boolean isLocalVariableData(String line) {
		return line.matches("\\S+ = .*");
	}

	public static boolean isProcessEnd(String line) {
		// TODO Auto-generated method stub
		return line.trim().matches("Program received signal SIGTERM, Terminated.") 
		|| line.contains ( "in __kernel_vsyscall ()");
	}

	public static boolean isGenericStackInfo(String line) {
		return line.matches("^#[0-9].*");
	}

	public static boolean isRunStart(String line) {
		// TODO Auto-generated method stub
		return line.startsWith("!!!BCT-NEW-EXECUTION");
	}

	public static boolean isBctLocals(String line) {
		return line.equals("!!!BCT-locals");
	}
	
	public static boolean isBctParentLocals(String line) {
		return line.equals("!!!BCT-caller-locals");
	}
	
	public static boolean isBctRegisters(String line) {
		return line.equals("!!!BCT-registers");
	}

	public static boolean isBctStackTrace(String line) {
		return line.equals("!!!BCT-stack");
	}

	public static boolean isBctArgs(String line) {
		return line.equals("!!!BCT-args");
	}

	
	public static boolean isBctLocalsEnd(String line) {
		return line.equals("!!!BCT-locals-end");
	}
	
	public static boolean isBctParentLocalsEnd(String line) {
		return line.equals("!!!BCT-caller-locals-end");
	}
	
	public static boolean isBctRegistersEnd(String line) {
		return line.equals("!!!BCT-registers-end");
	}

	public static boolean isBctStackTraceEnd(String line) {
		return line.equals("!!!BCT-stack-end");
	}

	public static boolean isBctArgsEnd(String line) {
		return line.equals("!!!BCT-args-end");
	}

	public static boolean isVariablePrint(String line) {
		return line.startsWith("!!!BCT-VARIABLE");
	}

	public static boolean isSignal(String line) {
		return line.startsWith("Program received signal ");
	}

	public static boolean isBctReturn(String line) {
		return line.startsWith("!!!BCT-RETURN-INSTRUCTION");
	}

	public static boolean isProcessExitCode(String line) {
		return line.startsWith("Program exited with code ");
	}

	public static boolean isBctFloatReturn(String line) {
		return line.startsWith("!!!BCT-FLOAT-RETURN");
	}

	public static boolean isBctDoubleReturn(String line) {
		return line.startsWith("!!!BCT-DOUBLE-RETURN");
	}

	public static boolean isProcessExitCodeWindows(String line) {
		//Inferior 1 (process 4008) exited with code 01]
		return line.matches("\\[Inferior \\d+ \\(process \\d+\\) exited with code \\d+\\]");
	}

	public static boolean isBctEnterExit(String line) {
		return line.startsWith("!!!BCT-ENTEX:");
	}

	public static boolean isBctTestCase(String line) {
		return line.startsWith("!!!BCT-TEST-CASE");
	}

	public static boolean isBctTestFail(String line) {
		return line.startsWith("!!!BCT-TEST-FAIL");
	}
	
	
}
