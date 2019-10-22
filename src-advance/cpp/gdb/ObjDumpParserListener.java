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

/**
 * This is the interface used to receive information from ObjDump parser
 * 
 * @author fabrizio
 *
 */
public interface ObjDumpParserListener {

	/**
	 * Invoked when a new disassembled  function is reached
	 * 
	 * E.g. the first line in the following example:
	 * 
	 * 080494a7 <emit_bug_reporting_address>:
	 * emit_bug_reporting_address():
	 * /home/fabrizio/Programs/coreutils-7.5/src/system.h:622
	 *  80494a7:       55                      push   %ebp
	 * @param curFunc
	 * @param address
	 */
	void newFunction(String curFunc, String address);

	/**
	 * Invoked when a return instruction is reached.
	 * E.g. the third line in the following example.
	 * 
	 * 
	 * /home/fabrizio/Programs/coreutils-7.5/src/timeout.c:100
 	 *  8049583:       c9                      leave
 	 *  8049584:       c3                      ret
 	 *  
	 * @param address
	 */
	void returnInstruction(String address);

	/**
	 * Invoked when a call instruction is reached.
	 * E.g. last line of the following example.
	 * 
	 * /home/fabrizio/Programs/coreutils-7.5/src/timeout.c:117
 	 *  80495ca:       8b 45 08                mov    0x8(%ebp),%eax
 	 *  80495cd:       89 44 24 04             mov    %eax,0x4(%esp)
 	 *  80495d1:       c7 04 24 00 00 00 00    movl   $0x0,(%esp)
     *  80495d8:       e8 80 ff ff ff          call   804955d <send_sig>
     *  
	 * @param address
	 * @param calleeName
	 */
	void callInstruction(String address, String calleeName);

	/**
	 * Invoked when a line with a source location is reached.
	 * E.g. the third line in the following example:
	 * 
	 * 080494a7 <emit_bug_reporting_address>:
	 * emit_bug_reporting_address():
	 * /home/fabrizio/Programs/coreutils-7.5/src/system.h:622
	 * 80494a7:       55                      push   %ebp
	 * 
	 * @param fileLocation
	 * @param lineNum
	 */
	void newSourceLocation(String fileLocation, int lineNum);

	/**
	 * Invoked when a line containing the function name is reached.
	 * E.g. the second line in the following example:
	 * 
	 * 080494a7 <emit_bug_reporting_address>:
	 * emit_bug_reporting_address():
	 * /home/fabrizio/Programs/coreutils-7.5/src/system.h:622
	 * 80494a7:       55                      push   %ebp
	 * 80494a8:       89 e5                   mov    %esp,%ebp
	 * @param substring
	 */
	void newFunctionName(String substring);

	/**
	 * Invoked when a generic instruction is encountered
	 * 
	 * @param address
	 * @param string
	 */
	void instruction(String address, String string);

	void objdumpEnd();

	void leaveInstruction(String address);

	void jmpInstruction(String address, String jmpToAddress);

	void newLine(String line);

	void popEbp(String address);

	void fldlInstruction(String address, String jmpToAddress);

}
