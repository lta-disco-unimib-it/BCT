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
package tools.violationsAnalyzer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import tools.violationsAnalyzer.ViolationsUtil.ViolationData.Type;


import modelsViolations.BctFSAModelViolation;
import modelsViolations.BctIOModelViolation;
import modelsViolations.BctModelViolation;

public class ViolationsUtil {
	public static final String DELIMITERS = "\t ,;+-=/!~|{}():<>?.*[]"; 
	public static final String EXPRESSION_DELIMITERS = "\t ,;+-=/!~|{}():<>?"; //like delimiters but without: . * []
	
	public static class ViolationData{
		private List<VariableData> violatedVariables = new ArrayList<VariableData>();
		private String model;
		private String functionName;
		private Type type;
		private String[] violationStatesNames;
		
		
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(type);
			sb.append("; ");
			sb.append(functionName);
			sb.append("; ");
			sb.append(model);
			sb.append("; {");
			for( VariableData vd : violatedVariables ){
				sb.append(vd);
				sb.append(", ");
			}
			sb.append("} ");
			sb.append("; {");
			if ( violationStatesNames != null ){
				for( String state : violationStatesNames ){
					sb.append(state);
					sb.append(", ");
				}
			}
			sb.append("} ]");
			
			return sb.toString();
		}

		public String[] getViolationStatesNames() {
			return violationStatesNames;
		}

		public void setViolationStatesNames(String[] violationStatesNames) {
			this.violationStatesNames = violationStatesNames;
		}

		public Type getType() {
			return type;
		}

		public enum Type { IO, INTERACTION };
		
		public String getModel() {
			return model;
		}

		public String getFunctionName() {
			return functionName;
		}

		public ViolationData( String functionName, String model, Type type){
			this.functionName = functionName;
			this.model = model;
			this.type = type;
		}
		
		public List<VariableData> getViolatedVariables() {
			return violatedVariables;
		}

		public void addVariableData( VariableData vd ){
			violatedVariables.add(vd);
		}
		
		@Override
		public boolean equals(Object arg0) {
			if ( ! ( arg0 instanceof ViolationData ) ){
				return false;	
			}
			
			ViolationData arg = (ViolationData) arg0;
			
			if ( type !=  arg.type ){
				return false;
			}
			
			if ( functionName == null ){
				if ( ! ( arg.functionName == null ) ) {
					return false;
				}
			} else {
				if( ! this.functionName.equals(arg.functionName) ){
					return false;
				}
			}
			
			if ( model == null ){
				if ( arg.model == null ){
					return false;
				}
			} else {
				if ( ! this.model.equals(arg.model) ){
					return false;
				}
			}
			
			if ( violationStatesNames == null ){
				if ( arg.violationStatesNames != null ){
					return false;
				}
			} else {
				if ( ! Arrays.equals(violationStatesNames, arg.violationStatesNames)){
					return false;
				}
			}
			
			return violatedVariables.equals(arg.violatedVariables);
		}

		public void addAllVariableData(List<VariableData> vars) {
			violatedVariables.addAll(vars);
		}

		public void setModel(String newModel) {
			model = newModel;
		}

	}
	
	public static class VariableData{	
		private String variableName;
		private Object actualValue;
		public VariableData(String variableName, Object actualValue) {
			super();
			this.variableName = variableName;
			this.actualValue = actualValue;
		}
		public String getVariableName() {
			return variableName;
		}
		public Object getActualValue() {
			return actualValue;
		}
		
		
		
		@Override
		public String toString() {
			return "["+variableName+": "+actualValue+"]";
		}
		@Override
		public boolean equals(Object arg0) {
			if ( ! ( arg0 instanceof VariableData ) ){ 
				return false;
			}
			VariableData arg = (VariableData) arg0;
			
			if ( actualValue == null ){
				if ( ! ( arg.actualValue == null) ){
					return false;
				}
			} else {
				if ( ! ( actualValue.equals(arg.actualValue) ) ){
					return false;
				}
			}
			
			if( variableName == null ){
				if ( ! ( arg.variableName == null) ){
					return false;
				}
			} else {
				if ( ! ( variableName.equals(arg.variableName) ) ){
					return false;
				}
			}
			
			return true;
		}
		
		
		
		
	}
	
	public static List<ViolationData> getViolationData( List<BctModelViolation> viols ){
		List<ViolationData> violsData = new ArrayList<ViolationData>();
		
		for ( BctModelViolation viol : viols ){
			if ( viol instanceof BctIOModelViolation ){
				violsData.add(getViolationData((BctIOModelViolation)viol));
			} else if ( viol instanceof BctFSAModelViolation ){
				violsData.add(getViolationData((BctFSAModelViolation)viol));
			}
		}
		
		return violsData;
	}

	public static ViolationData getViolationData( BctFSAModelViolation viol ){
		ViolationData vds = new ViolationData(viol.getViolatedModel(),viol.getViolation(),Type.INTERACTION);
		
		vds.setViolationStatesNames( viol.getViolationStatesNames() );
		
		return vds;
	}
	
	public static List<String> extractAnomalousVariables(
			String expression,
			Map<String, Object> localVariables) {
		
		ArrayList<String> variablesUsed = new ArrayList<String>(1);
		
		List<VariableData> vars = extractViolatedVariables(expression, localVariables);

		for ( VariableData var : vars ){
			variablesUsed.add(var.variableName);
		}
//		
//		StringTokenizer tokenizer = new StringTokenizer(expression, EXPRESSION_DELIMITERS);
//		while ( tokenizer.hasMoreElements() ){
//			String token = tokenizer.nextToken();
//			if ( localVariables.containsKey(token) ){
//				variablesUsed.add(token);
//			}
//		}
//		
		
		return variablesUsed;
		
	}
	
	
	public static ViolationData getViolationData( BctIOModelViolation viol ){
		String booleanProperty = viol.getViolation();
		HashMap<String, String> parametersMap = getCleanParametersMap ( viol );
		
		
		
		ViolationData vds = new ViolationData(viol.getViolatedModel(),viol.getViolation(),Type.IO);
		
		List<VariableData> vars = extractViolatedVariables(booleanProperty, parametersMap);
		
		vds.addAllVariableData(vars);
		
		return vds;
	}

	/**
	 * Given a set of variables:
	 * 	var
	 * 	arr[2]
	 * 	ptr->next
	 * 	str.field
	 * 
	 *  returns
	 *  	var
	 *  	arr
	 *  	ptr
	 *  	str
	 *  
	 * @param booleanProperty
	 * @return
	 */
	public static Set<String> extractParentVariables( String booleanProperty) {
		return extractParentVariables(booleanProperty, false);
	}
	
	public static Set<String> extractParentVariables( String booleanProperty, boolean removeStar) {
		
		
		Set<String> fullVariableNames = extractVariables(booleanProperty);
		
		
		
		Set<String> vars = new HashSet<String>();
		
		for ( String variable : fullVariableNames ){
			
			StringTokenizer t = new StringTokenizer(variable, "[.-");	
			if ( ! t.hasMoreTokens() ){
				continue;
			}
			
			variable = t.nextToken();
			
			if ( removeStar ){
				variable = variable.replace("*", "");
			}
			
			vars.add(variable);
		}
		
		return vars;
		
	}
	
	public static Set<String> extractParentVariablesNoStar( String booleanProperty) {
		return extractParentVariables(booleanProperty, true);
	}
	
	public static Set<String> extractVariables( String booleanProperty) {

//		booleanProperty = booleanProperty.replace(" one of {", "");
		
		//the values within 'one of' parenthesis are not variables, so cut the one-of expression
		int oneOfStart = booleanProperty.indexOf(" one of {");
		if ( oneOfStart > 0 ){
			booleanProperty = booleanProperty.substring(0, oneOfStart );
		}
		
		
		StringTokenizer st = new StringTokenizer(booleanProperty, " \t(){};");
		Set<String> vars = new HashSet<String>();
		while ( st.hasMoreTokens() ){
			String variable = st.nextToken();
			
			if ( variable.endsWith(",") ){
				variable = variable.substring(0,variable.length()-1);
			}
			
			if ( variable.length() == 0 ){
				continue;
			}
			
			if ( variable.charAt(0) == '='){
				continue;
			}
			
			if ( variable.charAt(0) == '"'){
				continue;
			}
			
			if ( isOperator ( variable ) ){
				continue;
			}
			
			
//			variable = variable.replaceAll("\\[[0-9]*\\]","[]");
			if ( variable.equals("null") ){
				continue;
			}
			
			
			
			
			if ( isNumeric(variable) ){
				continue;
			}
			
			if ( variable.charAt(0) == '&' ){
				variable = variable.substring(1);
			}
			
			vars.add( variable );

		}
		
		vars.remove("and");//FIXME: do a better fix to remove "and" keyword in this case ( X != null and Y != null ) ==> ( X.a > Y.b )
		
		return vars;
	}
	
	private static String operators[] = {"&","-","+","*","%","==>","==",">=","<=",">","<","!=","!","/","|",">>","<<",">>>","<<<","^"};
	private static boolean isOperator(String variable) {
		for ( String operator : operators ){
			if ( variable.equals(operator) ){
				return true;
			}
		}
		return false;
	}

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;
	}

	public static <T> List<VariableData> extractViolatedVariables(
			String booleanProperty, Map<String, T> parametersMap) {
		//		StringTokenizer st = new StringTokenizer(booleanProperty, " +/=*[]");
				StringTokenizer st = new StringTokenizer(booleanProperty, EXPRESSION_DELIMITERS);
				List<VariableData> vars = new ArrayList<ViolationsUtil.VariableData>();
				while ( st.hasMoreTokens() ){
					String variable = st.nextToken();
					
					if (parametersMap != null ){
						if ( ( ! parametersMap.containsKey(variable) ) 
								&& ( ! parametersMap.containsKey("*"+variable) ) ){
							continue;
						}
					}
					
					vars.add(new VariableData(variable, parseValue( parametersMap.get(variable) ) ) );
							
				}
		return vars;
	}

	private static HashMap<String, String> getCleanParametersMap(
			BctIOModelViolation viol) {
		return getCleanParametersMap(viol.getParametersMap());
	}
	
	private static <T> HashMap<String, T> getCleanParametersMap(
			Map<String,T> map) {
		HashMap<String, T> res = new HashMap<String, T>();
		if ( map == null ){
			return res;
		}
		
		for( Entry<String, T> e : map.entrySet() ){
			String variable = e.getKey();
		
			//we do not replace *, because it is part of the variable name in Violations report
			variable = variable.replaceFirst("\\.[a-z]+Value\\(\\)$", "");
			if ( variable.endsWith(".toString()") ){
				variable = variable.substring(0, variable.length()-11);
			}
			
			res.put(variable, e.getValue());
		}
		
		return res;
	}

	public static Object parseValue(Object _value) {
		if ( _value == null ){
			return null;
		}
		
		if ( ! ( _value instanceof String ) ){
			return _value;
		}
		
		String value = (String) _value;
//		System.out.println("Value is: "+value);
		if ( value.startsWith("\"") ){
			value = value.substring(1, value.length() -1 );
			
			
			return ViolationsUtil.unescape( value );
		}
		
		if ( value.startsWith("0x") ){
			try {
				return Long.decode(value);
			} catch ( NumberFormatException e ){
				value = value.substring(10);//get the lowest 8 bytes
				return Long.decode("0x"+value);
//				The following does not return the same as Long.decode(lowest8BytesOfValue) in case of overflow
//				value = value.substring(2);
//				BigInteger b = new BigInteger(value, 16 );
//				return b.longValue();
			}
		}
		
		if ( value.equals("0x0") || value.equals("null") ){
			return null;
		}
		
		if ( value.equals("!NULL") ){
			return "!NULL";
		}
		
		try {
			if ( value.contains(".") ){
				return Double.parseDouble(value);
			}
	
			return Integer.parseInt(value);
		} catch (Exception e) {
			try {
				return Long.parseLong(value);
			} catch (Exception e2) {
				try{
					return new BigInteger(value);
				} catch(Exception e3){
	
				}
			}
		}
	
		
		return value;
	}

	public static String unescape( String escapedString ){
		
		return escapedString
				.replace("\\t", "\t")
				.replace("\\n", "\n") //This leads to errors
				.replace("\\r","\r");
				
				
	}
	
	public static void main(String args[]){
		System.out.println("ptr "+ViolationsUtil.parseValue("0xbffff028") );
		
		System.out.println("end "+ViolationsUtil.parseValue("0xbfffeffc") );
	}

}
