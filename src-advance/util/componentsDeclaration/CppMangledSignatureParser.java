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
package util.componentsDeclaration;

import tools.gdbTraceParser.BctCNamesUtil;

public class CppMangledSignatureParser implements SignatureParser {

	
	@Override
	public String getPackageNameFromCompleteMethodSignature(
			String completeMethodSignature) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getMethodSignatureFromCompleteMethodSignature(
			String completeMethodSignature) {
		if ( completeMethodSignature.charAt(completeMethodSignature.length()-1) == BctCNamesUtil.ADDITIONAL_CHAR_FOR_NONDETERMINISTIC_NAMES) {
			completeMethodSignature = completeMethodSignature.substring(0, completeMethodSignature.length()-1);
		}
		return completeMethodSignature.trim();
	}

	@Override
	public String getCompleteMethodSignatureFromBytecodeMethodSignature(
			String bytecodeMethodSignature) {
		if ( bytecodeMethodSignature.charAt( bytecodeMethodSignature.length()-1 ) == BctCNamesUtil.ADDITIONAL_CHAR_FOR_NONDETERMINISTIC_NAMES) {
			bytecodeMethodSignature = bytecodeMethodSignature.substring(0, bytecodeMethodSignature.length()-1);
		}
		return bytecodeMethodSignature;
	}

	@Override
	public String getClassNameFromCompleteMethodSignature(
			String completeMethodSignature) {
		// TODO Auto-generated method stub
		return "";
	}

}
