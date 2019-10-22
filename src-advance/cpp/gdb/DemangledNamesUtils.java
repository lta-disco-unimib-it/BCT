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

import java.util.Arrays;

public class DemangledNamesUtils {
	
	public static boolean isPlainCFunction(String demangle) {
		int argumentsStart = demangle.indexOf("::");
		if ( argumentsStart < 0 ){
			return true;
		}
		return false;
	}

	public static String getFunctionNameOnly(String demangled) {
		int parPos = demangled.indexOf('(');
		if ( parPos < 0 ){
			return demangled;
		}
		return demangled.substring(0,parPos);
	}

	public static boolean isCppSignature(String demangle) {
		int argumentsStart = demangle.indexOf('(');
		if ( argumentsStart < 0 ){
			return false;
		}
		return true;
	}
	
	public static String getSignatureNoTemplate(String demangle) {
		//Isograft::Libraries::Standard::Learn<Isograft::Libraries::SenderApplication::Sender_Component_State>::operator()()
		//Isograft::Libraries::Standard::Learn<Isograft::Libraries::Standard::Notifier_Component_State<bool> >::operator()()
		
		int argumentsStart = demangle.indexOf('(');
		if ( argumentsStart < 0 ){
			return demangle;
		}
		
		String signatureNoArgs = demangle.substring(0, argumentsStart);
		
		int methodStart = signatureNoArgs.lastIndexOf("::");
		
		if ( methodStart  == -1 ){
			return demangle;
		} 

		String className = signatureNoArgs.substring(0,methodStart);
		
		
		int templateStart = className.indexOf('<');
		if ( templateStart == -1 ){
			return demangle;
		}
		
		String classNoTemplate = className.substring(0, templateStart);
		String methodWithArgs = demangle.substring(methodStart);
		
		return classNoTemplate + methodWithArgs;
	}
	
	public static String removeTemplatesFromSignature(String signature) {
		

		char[] signatureArray = signature.toCharArray();
		char[] finalSignature = new char[signatureArray.length];
		
		boolean withinArguments = false;
		int templates = 0;
		int c = 0;
		
		for ( int i = 0; i < signatureArray.length; i++ ){
			//				System.out.println(signatureArray[i]);
//			boolean templateStart = false;
//			boolean templateEnd = false;

			if ( signatureArray[i] == '<' ){
				templates++;
			} else if ( signatureArray[i] == '>' ){
				templates--;
				if ( withinArguments && templates == 0){
					finalSignature[c++]=' ';
				}
			} else {
				if ( templates > 0 ){
					continue;
				}
				
				switch ( signatureArray[i] ){
				case '(':
					withinArguments = true;
					break;
				case ')':
				case ';':
				case ',':
					if ( finalSignature[c-1] == ' ' ){
						c--; //delete the last added " "
					}
					break;
				}
//				System.out.println(signatureArray[i]);
				finalSignature[c++]=signatureArray[i];
			}
		}
//		return Arrays.toString(finalSignature);
//		Arrays.copyOf(finalSignature, newLength);
//		return new String ( finalSignature );	
		return new String ( Arrays.copyOfRange(finalSignature, 0, c) );
	}
	
	
	public static String removeNamespacesAndTemplatsFromSignature(String signature) {
		signature = removeTemplatesFromSignature(signature);
		
		int parStart = signature.indexOf('(');
		int parEnd = signature.indexOf(')');
		
		if ( parStart < 0 ){
			return removeNamespacesFromMethod( signature );
		}
	
		String fullyQualifiedMehodName = signature.substring(0, parStart );
		
		String methodNameAndClass = removeNamespacesFromMethod( fullyQualifiedMehodName ); 
		
		StringBuffer finalSignature = new StringBuffer();
		finalSignature.append(methodNameAndClass);
		finalSignature.append("(");
		
		String parametersString = signature.substring(parStart+1,parEnd).trim();
		
		if ( parametersString.length() > 0 ){
			String[] parameters = parametersString.split(",");
			for ( int i = 0; i < parameters.length; i++ ){
				parameters[i] = removeNamespacesFromParameter(parameters[i]);
				finalSignature.append(parameters[i]);
				
				if ( i != parameters.length - 1 ){
					finalSignature.append(",");
				}
			}
		}
		finalSignature.append(")");
		finalSignature.append(signature.substring(parEnd+1));
		
		return finalSignature.toString();
		
		
	}

	private static String removeNamespacesFromMethod(
			String parameterTypeAndName) {
		
		String type = parameterTypeAndName;
		
		
		int ns = type.lastIndexOf("::");
		
		int ms = type.lastIndexOf(".");
		
		if ( ms > ns ){
			ns = ms;
		}
		
		if ( ns < 0 ){
			return parameterTypeAndName;
		}

		type = type.substring(0, ns);

		ns = type.lastIndexOf("::");

		if ( ns < 0 ){
			return parameterTypeAndName;
		}

		return parameterTypeAndName.substring(ns+2);


	}

	private static String removeNamespacesFromParameter(
			String parameterTypeAndName) {
		int spaceStart = parameterTypeAndName.indexOf(' ');

		String type;
		if ( spaceStart > 0 ){
			type = parameterTypeAndName.substring(0, spaceStart);
		} else {
			type = parameterTypeAndName;
		}

		int ns = type.lastIndexOf("::");
		if ( ns > 0 ){
			return parameterTypeAndName.substring(ns+2);
		}

		return parameterTypeAndName;
	}

	

}
