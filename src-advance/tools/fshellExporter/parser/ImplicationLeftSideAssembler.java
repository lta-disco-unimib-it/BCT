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
package tools.fshellExporter.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

public class ImplicationLeftSideAssembler extends Assembler {

	private static final String javaIdentifierChars = "[A-Za-z_\\.]";
	private static final Pattern pointerPattern = Pattern.compile("\\s*\\(\\s*\\*(["+javaIdentifierChars+"]+)\\s*\\)\\s*!=\\s*null\\s*");
	
	public void workOn(Assembly a) {
		Target target = null;
		target = (Target) a.getTarget();
		
		
		try  {
			String leftSide = ( String ) target.top();
			Matcher m = pointerPattern.matcher(leftSide);
//			System.out.println("LEFT BEFORE "+leftSide);
			if ( m.matches() ){
				leftSide = m.replaceAll("$1 != 0");
			}
//			System.out.println("LEFT AFTER "+leftSide);
			target.push(leftSide);
		} catch ( ClassCastException e ){
			EvaluationRuntimeErrors.leftImplictionNotBoolean();
		} catch ( ArrayIndexOutOfBoundsException e ){
			
		}
	}
	
	public static void main(String args[]){
		Matcher m = pointerPattern.matcher(" (*p).x != null ");
		System.out.println(m.matches());
		System.out.println(m.replaceAll("$1 != 0"));
	}

}
