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
package check.ioInvariantParser;

import java.lang.reflect.Array;

import sjm.parse.Assembler;
import sjm.parse.Assembly;

public class EqualsAssembler extends Assembler {
	static final double DFBias = 0.001;

	private Boolean evaluateNumbers( Number n1 , Number n2) {
//		System.out.println("EvaluateNumbers "+n1.doubleValue()+" "+n2.doubleValue());
		if ( Double.isNaN(n1.doubleValue() ) && Double.isNaN(n2.doubleValue() ) ){
			return true;
		}
		return Boolean.valueOf(Math.abs(n1.doubleValue() - n2.doubleValue())<DFBias);
	}
	private Boolean evaluateStrings( String s1 , String s2) {
		//	  System.out.println("EvaluateStrings "+s1+" "+s2);
		return Boolean.valueOf(s1.compareTo(s2)==0);
	}  
	private Boolean evaluateCharacters( Character c1 , Character c2) {
		//	  System.out.println("EvaluateChars "+c1+" "+c2);
		return Boolean.valueOf(c1.compareTo(c2)==0);
	}    
	private Boolean evaluateArrays( Object a1 , Object a2) {
		int MAX = Array.getLength(a1);
		try {
			for(int i=0;i<MAX;i++) {
				Comparable c1 = (Comparable)Array.get(a1,i);
				Comparable c2 = (Comparable)Array.get(a2,i);
				if(c1.compareTo(c2)!=0)
					return Boolean.FALSE;
			}
		} catch (ClassCastException cce) {
			//TODO: is this correct?
			return Boolean.TRUE;
		}
		return Boolean.TRUE;
	}    
	public void workOn(Assembly a) {
		Target target = (Target)a.getTarget();
//		System.out.println("EqualsAssembler "+target.toString());
		//System.out.flush();
		/*Object parameter2 = (Object)target.pop();
    if(target.isEmpty())
      return;
    Object parameter1 = (Object)target.pop();*/
		Object parameter2 = null;
		try{
			parameter2 = (Object)target.pop();
		}catch(ArrayIndexOutOfBoundsException e) {
			EvaluationRuntimeErrors.emptyStack();
			return;
		}
		Object parameter1 = null;
		try{
			parameter1 = (Object)target.pop();
		}catch(ArrayIndexOutOfBoundsException e) {
			EvaluationRuntimeErrors.emptyStack();
			return;
		}
		try{
			//    	System.out.println("EqualsAssembler working on "+parameter1+" "+parameter2);
			//    	System.out.println(par2ameter1.getClass().getCanonicalName());
			//    	System.out.println(parameter2.getClass().getCanonicalName());
			if(parameter1 == null && parameter2==null){
				//    		System.out.println("A");
				target.push( Boolean.TRUE );
			} if(parameter1 == null || parameter2==null) {
				//    		System.out.println("B");
				target.push(new Boolean(parameter1==parameter2));
			} else if ( parameter1 instanceof Boolean && parameter2 instanceof Boolean ){
				//    		System.out.println("C");
				target.push( evaluateBoolean((Boolean)parameter1,(Boolean)parameter2));
			} else if ( parameter1 instanceof Boolean && parameter2 instanceof Number ){
				//    		System.out.println("D");
				target.push( evaluateBoolean((Boolean)parameter1,new Boolean(((Number)parameter2).doubleValue() > 0)));
			}
			else if ( parameter2 instanceof Boolean && parameter1 instanceof Number ){
				//        	System.out.println("E");
				target.push( evaluateBoolean((Boolean)parameter2,new Boolean(((Number)parameter1).doubleValue() > 0)));
			}
			else if ( parameter1 instanceof Number && parameter2 instanceof Number ){
				//        	System.out.println("F");
				target.push(evaluateNumbers((Number)parameter1,(Number)parameter2));
			} else if (parameter1 instanceof String && parameter2 instanceof String){
				//        	System.out.println("G");
				target.push(evaluateStrings((String)parameter1,(String)parameter2));
			} else if (parameter1 instanceof Character && parameter2 instanceof Character) {
				//        	System.out.println("H");
				target.push(evaluateCharacters((Character)parameter1,(Character)parameter2));
			} else if ((parameter1 instanceof Character && parameter2 instanceof String )) {
				//        	System.out.println("I");
				target.push(evaluateStrings(""+(Character)parameter1,(String)parameter2));
			} else if ((parameter2 instanceof Character && parameter1 instanceof String )) {
				//        	System.out.println("L");
				target.push(evaluateStrings(""+(Character)parameter2,(String)parameter1));
			} else if(parameter1.getClass().isArray() && parameter2.getClass().isArray()) {
				//        	System.out.println("M");
				target.push(evaluateArrays(parameter1,parameter2));
			} else if ( parameter1 instanceof NonExistentVariable || parameter2 instanceof NonExistentVariable ) {
				target.push(Boolean.TRUE);
			} else {
				//        	System.out.println("O");
				target.push( evaluateObjects(parameter1, parameter2) );
			}
		}
		catch (Exception e) {
			//		System.out.println("EA "+target.toString());
			EvaluationRuntimeErrors.evaluationError();
			target.push(Boolean.FALSE);
			return;
		}
		//System.out.println("EA OK");
		//System.out.flush();
	}

	private Boolean evaluateObjects(Object parameter1, Object parameter2) {
		return parameter1 == parameter2;
	}
	private Object evaluateBoolean(Boolean boolean1, Boolean boolean2) {	
		//	  System.out.println("Boolean");
		return boolean1.equals(boolean2);
	}
}