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
package dfmaker.core;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

import dfmaker.core.VarTypeResolver.Types;
import dfmaker.core.VarTypeResolver.Types.BooleanType;
import dfmaker.core.VarTypeResolver.Types.DoubleType;
import dfmaker.core.VarTypeResolver.Types.HashcodeType;
import dfmaker.core.VarTypeResolver.Types.IntegerType;
import dfmaker.core.VarTypeResolver.Types.StringType;
/**
 * 
 * @author Fabrizio Pastore [ fabrizio.pastore at gmail dot com ]
 *
 */
public class DaikonDeclarationMaker {
	///PROPERTIES-DESCRIPTION: Options that control how data is preprocessed
	
    private static final int HASH_COMPARISON_KEY = 5;
	///allows BCT to compare the value of pointer variables
    private static final String BCT_COMPARE_POINTERS = "bct.comparePointers";

	public enum DaikonComparisonCriterion {CompareAll,CompareOnlySameDaikonRepresentationType, CompareOnlySameValueType };
    private static HashMap<String,Integer> comparisonTypes = new HashMap<String, Integer>();
	private static boolean comparePointers;
    
	static {
		String comparePointersString = System.getProperty(BCT_COMPARE_POINTERS);
		comparePointers = Boolean.parseBoolean(comparePointersString);
	}
	
    public static void writeAll(SuperstructureCollection structuresCollection, Writer writer, DaikonComparisonCriterion comparisonCriterion ) throws IOException {
        Iterator entryIt = structuresCollection.values().iterator();
        
        while ( entryIt.hasNext() ){
        	Superstructure superStructure = (Superstructure) entryIt.next();
        	writeDown ( superStructure, writer, comparisonCriterion );
        }
		
	}

    public static void write( Writer writer, Superstructure entrySuperstructure, Superstructure exitSuperstructure, DaikonComparisonCriterion comparisonCriterion ) throws IOException{
    	writeDown(entrySuperstructure, writer, comparisonCriterion);
    	writeDown(exitSuperstructure, writer, comparisonCriterion);
    }
    
	
    private static void writeDown(Superstructure entryStructure, Writer writer, DaikonComparisonCriterion comparisonCriterion ) throws IOException {
        Iterator entryIterator = entryStructure.varFields().iterator();
        
        writer.write("DECLARE\n");

        writer.write(entryStructure.getProgramPointName()+"\n");
        
        
        while ( entryIterator.hasNext() ){
        	SuperstructureField field = (SuperstructureField) entryIterator.next();
        	String arrayExtension = "";
        	
        	String varName = field.getVarName();
        	Types fieldVarType = field.getVarType();
        	String varType = fieldVarType.toString();
        	if ( field.isArray() ) {
        		varType += "[]";
        		if ( ! varName.endsWith("[]") )
        			varName += "[..]";
        	}
        	String objType = null;
        	
        	if ( fieldVarType == Types.hashcodeType )
        		objType = "Object";
        	else
        		objType = varType;
        	
        	String extra = getVarGroupId( comparisonCriterion, varName,fieldVarType);
        	
        	
        	if ( comparePointers ){
        		if ( fieldVarType == Types.hashcodeType ){
            		objType = "int";
            		varType = "int";
            		extra = String.valueOf(HASH_COMPARISON_KEY);
        		}
        	}
        	
        	writer.write(varName+"\n"+objType+"\n"+varType+"\n"+extra+"\n");
        }
		
        writer.write("\n");
        		
	}

	private static String getVarGroupId(DaikonComparisonCriterion comparisonCriterion, String varName, Types fieldVarType) {
		int comparability = -1;
		if ( comparisonCriterion == DaikonComparisonCriterion.CompareAll ){
			comparability = 1;
		} else if ( comparisonCriterion == DaikonComparisonCriterion.CompareOnlySameDaikonRepresentationType ){
			if ( fieldVarType == Types.booleanType ){
				comparability = 1;
			} else if ( fieldVarType == Types.stringType  ) {
				comparability = 2;
			} else if ( fieldVarType == Types.doubleType  ) {
				comparability = 3;
			} else if ( fieldVarType == Types.integerType   ) {
				comparability = 4;
			} else if ( fieldVarType == Types.hashcodeType  ) {
				comparability = HASH_COMPARISON_KEY;
			} else {
				comparability = -1;
			}
		} else if ( comparisonCriterion == DaikonComparisonCriterion.CompareOnlySameValueType ){
			if ( ! varName.endsWith(")") ){
				comparability = -1;
			} else {
				int li = varName.lastIndexOf('.');
				String type = varName.substring(li+1);
				Integer comparisonNumber = comparisonTypes.get(type);
				if ( comparisonNumber == null ){
					comparisonNumber = comparisonTypes.size()+1;
					comparisonTypes.put(type, comparisonNumber);
				}
				comparability= comparisonNumber;
			}
		}
		
		//This works only for newer versions of daikon
		//return "comparability "+comparability;
		return String.valueOf(comparability);
		
	}
	
    
}
