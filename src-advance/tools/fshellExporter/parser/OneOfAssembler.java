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

import java.lang.reflect.Array;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

public class OneOfAssembler extends Assembler {
  

  private String oneOf(Object x,Object[] yy) {
	  String result = "";
	 for ( int i = 0; i < yy.length; i++ ){
		 if ( i > 0 ){
			 result += " || ";
		 }
		 result+= x+" == "+yy[i];
	 }
	 
	  return result;
  }
  
    
   
  public void workOn(Assembly a) {
    Target target = (Target)a.getTarget();
    
    //Object y = (Object)target.pop();  
    Object y = null;
    try{
    	y = (Object)target.pop();
    }catch(ArrayIndexOutOfBoundsException e) {
      	EvaluationRuntimeErrors.emptyStack();
      	return;
    }
    //Object x = (Object)target.pop();   
    Object x = null;
    try{
    	x = (Object)target.pop();
    }catch(ArrayIndexOutOfBoundsException e) {
      	EvaluationRuntimeErrors.emptyStack();
      	return;
    }
    try{
    if(x.getClass().isArray() && y.getClass().isArray()){
      //
    } else if(y.getClass().isArray())
    	//System.out.println("One of:"+x+" "+y);
    	//System.out.println( target );
      target.push(oneOf(x,(Object[]) y));
    }
    catch (Exception e) {
		EvaluationRuntimeErrors.evaluationError();
		//target.push(Boolean.FALSE);
		return;
	}
  }
}
