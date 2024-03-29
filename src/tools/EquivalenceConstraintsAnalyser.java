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
package tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import theoremProver.Simplify;
import theoremProver.SimplifyResult;
import traceReaders.normalized.DBGKTailTracesReader;
import traceReaders.normalized.GKTailTracesReader;
import database.DataLayerException;
import database.GKTailMethodCall;

public class EquivalenceConstraintsAnalyser implements GKTailConstraintsAnalyser {
	
	public  ArrayList analyseConstraints (ArrayList al, int idMethodCall, int marker) throws DataLayerException {
		
		GKTailTracesReader gkttr = new DBGKTailTracesReader();
		
		Iterator traceIdMethodCallList = al.iterator();
		String constraint1;
		String constraint2;
		
		int arrayListIdMethodCall;
		while (traceIdMethodCallList.hasNext()) {
			arrayListIdMethodCall = (Integer)traceIdMethodCallList.next();
			constraint1 = gkttr.getConstraint(arrayListIdMethodCall);
			constraint2 = gkttr.getConstraint(idMethodCall);						
			
			// Simplify evaluating (Davide Lorenzoli)
			
			String predicate1 = "(AND " + constraint1 + " )";
			String predicate2 = "(AND " + constraint2 + " )";
			
			
			//System.out.println("METHOD ID: " + arrayListIdMethodCall);
			//System.out.println("EVALUATING CONSTRAINS:");
			//System.out.println("1: " + predicate1);
			//System.out.println("2: " + predicate2);
			
			
			ArrayList<SimplifyResult> results = new ArrayList<SimplifyResult>();
			// Execute Simplify 			
			try {
				Simplify simplify = new Simplify();
				results = simplify.doIMPLIES(predicate1, predicate2);			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (!results.isEmpty()) {
				//System.out.println("EVALUATION RESULT: " + results.get(0).getResult());
			}
			
			// If any result exist
			if (!results.isEmpty()) {
				// The predicate1 implies predicate2 => state merging
				if (results.get(0).getResult() == SimplifyResult.VALID) {
					GKTailMethodCall.updateLine(arrayListIdMethodCall, marker);
					//System.out.println("Update ok for method " + arrayListIdMethodCall);
					
					traceIdMethodCallList.remove();
					al.remove((Integer)arrayListIdMethodCall);									
				}
				// Something went wrong => no state merging
				else {
					switch (results.get(0).getResult()) {
						case SimplifyResult.BAD_INPUT:
							break;
						case SimplifyResult.SYNTAX_ERROR:
							break;
						default:
							break;
					}
				}
			}						
			
			
			
			// Daikon format equivalence evaluating (Mauro Santoro)
			/*
			if(constraint1.equals(constraint2)) {				
				GKTailMethodCall.updateLine(arrayListIdMethodCall, marker);
				System.out.println(getClass().getName() + ".analyseConstraints: Updated: " + arrayListIdMethodCall);
				
				traceIdMethodCallList.remove();
				al.remove((Integer)arrayListIdMethodCall);				
			}
			*/					
		}
		return al;
	}
}
