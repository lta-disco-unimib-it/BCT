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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class EnumValuesDetector {

	private GdbInfoExtractor data = new GdbInfoExtractor(new File("/usr/bin/gdb"));
	private HashMap<String,String> constantValues = new HashMap<String, String>();
	
	public EnumValuesDetector(File executable) {
		data.setExecutableToAnalyze(executable);
	}

	public static void main(String args[]){
		File executable = new File(args[0]);
		
		EnumValuesDetector valuesDetector = new EnumValuesDetector(executable);
		System.out.println( valuesDetector.getConstantValue("code_eof") );
		System.out.println( valuesDetector.getConstantValue("binary_opAAA") );
		
	}



	public String getConstantValue(String enumConstant) {
		
		enumConstant = enumConstant.trim();
		
		//what happens if two different enums declare members with a same name?
		
		String value = constantValues.get(enumConstant);
		if ( value != null ){
			return value;
		}
		EnumType enumType = runGdbAndGetEnumData("ptype "+enumConstant);
		if ( enumType == null ){
			return "!NULL";
		}
		constantValues.putAll(enumType.getMembers());
		
//		System.out.println(constantValues);
		
		return constantValues.get(enumConstant);
	}

	private EnumType runGdbAndGetEnumData(String enumConstant) {
		
		
		
		String output = data.runGdbAndGetOutput(enumConstant);
		
		
		return extractTypeFromGdbOutput( output );
		
	}

	private EnumType extractTypeFromGdbOutput(String outputBuffer) {
		BufferedReader r = new BufferedReader(new StringReader( outputBuffer ));
		String line;

		
		try {
			while ( ( line = r.readLine() ) != null ){
				if ( ! line.startsWith("type =") ){
					continue;
				}
				
				int membersStart = line.indexOf('{');
				int membersEnd = line.indexOf('}');
				
				if ( membersStart < 0 || membersEnd < 0  ){
					throw new IllegalArgumentException("Unexpected string: "+line);
				}
				
				String type = line.substring(6,membersStart).trim();
				
				String elementsContent = line.substring(membersStart+1, membersEnd);
				StringTokenizer tokenizer = new StringTokenizer(elementsContent, ",");
				
				EnumType enumType = new EnumType( type );
				while ( tokenizer.hasMoreElements() ){
					String memberName = tokenizer.nextToken().trim();
					enumType.addMember( memberName );
				}
				
				return enumType;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
