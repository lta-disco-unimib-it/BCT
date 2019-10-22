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
package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

public class TestClassLoader extends ClassLoader {

	private Hashtable classes = new Hashtable();
	
    public TestClassLoader(){
        super(TestClassLoader.class.getClassLoader());
    }
  
    public Class loadClass(String className) throws ClassNotFoundException {
         return findClass(className);
    }
 
    public Class findClass(String className){
    	return findClass(className, false);
    }
    
    
    
    private Class findClass(String className,boolean reload){
        byte classByte[];
        Class result=null;
        result = (Class)classes.get(className);
        if(result != null){
            return result;
        }
        
        try{
            return findSystemClass(className);
        }catch(Exception e){
        }
        try{
           String classPath =    ((String)ClassLoader.getSystemResource(className.replace('.',File.separatorChar)+".class").getFile()).substring(1);
           classByte = loadClassData(classPath);
            result = defineClass(className,classByte,0,classByte.length,null);
            classes.put(className,result);
            return result;
        }catch(Exception e){
            return null;
        } 
    }
 
    private byte[] loadClassData(String className) throws IOException{
 
        File f ;
        f = new File(className);
        int size = (int)f.length();
        byte buff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        dis.readFully(buff);
        dis.close();
        return buff;
    }
 
    

	
}
