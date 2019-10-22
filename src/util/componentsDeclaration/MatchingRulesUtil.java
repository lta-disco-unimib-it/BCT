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
package util.componentsDeclaration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import util.FileUtil;
import util.ProcessRunner;

public class MatchingRulesUtil {

	public static List updateFilesAccordingToMatchingRules(List files, List<Component> components) {
		if ( components != null ){
			
			String javap = System.getProperty("bct.javap");
			
			
			ArrayList filesNew = new ArrayList<>();
			
			Map<File,String> packages = new HashMap<>();
			
			for ( Object f : files ){
				File file = (File) f;
				
				String className = null;
				String packageName = null;
				
				boolean exception = false;
				try {

					com.sun.org.apache.bcel.internal.classfile.ClassParser cp = new com.sun.org.apache.bcel.internal.classfile.ClassParser(file.getAbsolutePath());
					com.sun.org.apache.bcel.internal.classfile.JavaClass jc = cp.parse();
					className = jc.getClassName();
					packageName = jc.getPackageName();
			
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					System.out.println("!!!IGNORING EXCEPTION: "+e.getMessage());
					System.out.println("!!!EXCLUDING FROM INSTRUMENTATION : "+file.getAbsolutePath());
					exception=true;
				} catch (Throwable e ){
//					e.printStackTrace();
					System.out.println("!!!IGNORING EXCEPTION: "+e.getMessage());
					System.out.println("!!!EXCLUDING FROM INSTRUMENTATION : "+file.getAbsolutePath());
					exception=true;
				}
				
				if ( exception ){
					
					if ( javap != null ){
						List<String> command = new ArrayList<>();
						command.add(javap);
						command.add(file.getAbsolutePath());
						StringBuffer outputBuffer = new StringBuffer();
						StringBuffer errorBuffer = new StringBuffer();
						try {
							ProcessRunner.run(command, outputBuffer, errorBuffer, 0);
							
							List<String> lines = FileUtil.getLines(new BufferedReader(new StringReader(outputBuffer.toString()))); 
							
							for ( String line : lines ){
								if ( line.startsWith("public class") ){
									String fullClassName = line.split(" ")[2];
									
									int sep = fullClassName.lastIndexOf(".");
									
									packageName = fullClassName.substring(0,sep);
									className = fullClassName.substring(sep+1);
									
									System.out.println("!!!PACKAGE: "+packageName+" CLASS: "+className);
									
									break;
								}
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
				
				
				if ( className != null && packageName != null ){
					for ( Component c : components ){
						if ( c.acceptClass(packageName, className) ){
							System.out.println("!!!ADDING FILE: "+file);
							filesNew.add( file );
							break;
						}
					}
				}
				
			}
			files = filesNew;
		}
		return files;
	}

	
	
}
