/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package flattener.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This class export a FieldsFilter to a file
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class FieldFilterExporter {

	public static void export ( FieldFilter fieldFilter, File dest ) throws IOException{
		FileWriter fw = null;
		try {
			fw = new FileWriter( dest);
			
			
			for ( FieldCondition condition : fieldFilter.getConditions() ){
				
				StringBuffer lineContent = new StringBuffer();
				
				if ( condition.isAccept() ){
					lineContent.append("INCLUDE");
				} else {
					lineContent.append("EXCLUDE");
				}
				
				lineContent.append("\t");
				
				if ( condition instanceof FieldModifiersCondition ){
					lineContent.append("MODIFIER\t");
					FieldModifiersCondition fmc = (FieldModifiersCondition) condition;
					int modifierMask = fmc.getModifierMask();
					
							for ( Field field : Modifier.class.getFields() ){
								try {
									if ( field.get(null).equals(modifierMask) ){
										lineContent.append(field.getName());
										break;
									}
								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
					
				} else {
					lineContent.append("FIELD\t");
					FieldNameCondition fnc = (FieldNameCondition) condition;
					lineContent.append(fnc.getPackageRegexp());
					lineContent.append(" ");
					lineContent.append(fnc.getClassRegexp());
					lineContent.append(" ");
					lineContent.append(fnc.getFieldRegexp());
				}
				
				lineContent.append("\n");
			}
			
		
		} finally {
			if ( fw!=null){
				
					fw.close();
				
			}
		}
	}
}