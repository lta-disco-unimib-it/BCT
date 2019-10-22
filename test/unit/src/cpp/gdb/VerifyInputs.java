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

public class VerifyInputs implements ObjDumpParserListener {

	@Override
	public void callInstruction(String address, String calleeName) {
		if ( address == null ){
			throw new IllegalArgumentException("Address cannnot be null");
		}
		if ( calleeName == null ){
			throw new IllegalArgumentException("calleeName cannnot be null");
		}
	}

	@Override
	public void instruction(String address, String string) {
		// TODO Auto-generated method stub
		if ( address == null ){
			throw new IllegalArgumentException("Cannnot be null");
		}
		if ( string == null ){
			throw new IllegalArgumentException("instruction cannnot be null");
		}
	}

	@Override
	public void newFunction(String curFunc, String address) {
		// TODO Auto-generated method stub
		if ( address == null ){
			throw new IllegalArgumentException("Cannnot be null");
		}
		if ( curFunc == null ){
			throw new IllegalArgumentException("curFunc cannnot be null");
		}
	}

	@Override
	public void newFunctionName(String substring) {
		// TODO Auto-generated method stub
		if ( substring == null ){
			throw new IllegalArgumentException("substring cannnot be null");
		}
	}

	@Override
	public void newSourceLocation(String fileLocation, int lineNum) {
		// TODO Auto-generated method stub
		if ( fileLocation == null ){
			throw new IllegalArgumentException("fileLocation cannnot be null");
		}
		if ( lineNum < 0 ){
			throw new IllegalArgumentException("lineNum cannnot be < 0");
		}
	}

	@Override
	public void returnInstruction(String address) {
		// TODO Auto-generated method stub
		if ( address == null ){
			throw new IllegalArgumentException("Cannnot be null");
		}
	}

	@Override
	public void objdumpEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveInstruction(String address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jmpInstruction(String address, String jmpToAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newLine(String line) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void popEbp(String address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fldlInstruction(String address, String jmpToAddress) {
		// TODO Auto-generated method stub
		
	}

}
