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
package regressionTestManager.detectionMatrix;
/*
 * Created on 11-Jun-2004
 * File: SetCover.java
 */
//package uk.ac.man.cs.ipg.dot.testSuites.filtered;

import java.util.Arrays;
import java.util.Vector;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * Allows a 2D Integer Matrix Set Cover problem to be solved using:
 *   - a greedy approximation algorithm
 *   - a branch and bound optimal solution algorithm
 * @author <A HREF="mailto:d.willmor@cs.man.ac.uk">David Willmor</A>
 * @version 1.0
 * @since 1.0
 */
public class DetectionMatrix {

	Vector D;

	public DetectionMatrix() {
		D = new Vector();
	}

	public void addTestVector(Boolean[] t) {
		D.add(t);
		//System.out.println(D);
	}

	public Boolean[] greedy() {
		Boolean[] result = new Boolean[D.size()];
		Arrays.fill(result, new Boolean(false));
		// clone the vector then deep copy its contents
		/*Vector D = (Vector) this.D.clone();
		for (int i = 0; i < D.size(); i++) {
			Boolean[] b = (Boolean[]) D.get(i);
			Boolean[] c = new Boolean[b.length];
			for (int j = 0; j < b.length; j++) {
				c[j] = new Boolean(b[j].booleanValue());
			}
			D.setElementAt(c, i);
		}*/
		int max = -1;
		while (max != 0) {
			//System.out.println("\titeration");
			// find the test case that covers the most elements
			max = 0;
			int pos = -1;
			//System.out.println("D: "+D.size());
			for (int i = 0; i < D.size(); i++) {
				//System.out.println("\titeration2");
				Boolean[] t = (Boolean[]) D.get(i);
				int noT = 0;
				for (int j = 0; j < t.length; j++) {
					if (t[j].booleanValue() == true)
						noT++;
				}
				if (noT > max && noT != 0) {
					pos = i;
					max = noT;
				}
			}
			// if test case has been found
			if (pos != -1) {
				// add the test case to result
				result[pos] = new Boolean(true);
				//System.out.println("pick: "+pos);
				// remove covered elements
				Boolean[] posD = (Boolean[]) D.get(pos);
				Boolean[] cov = new Boolean[posD.length];
				for (int i = 0; i < posD.length; i++) {
					cov[i] = new Boolean(posD[i].booleanValue());
				}
				for (int i = 0; i < D.size(); i++) {
					Boolean[] t = (Boolean[]) D.get(i);
					for (int j = 0; j < cov.length; j++) {
						if (cov[j].booleanValue() == true)
							t[j] = new Boolean(false);
					}
					D.setElementAt(t, i);
				}
			}
		} // repeat until all elements have been covered
		// return the result
		return result;
	}

	public Boolean[] branchbound() {
		Boolean[] result;
		try {
			// no of columns
			int col = D.size();
			//	create the problem (rows, columns)
			LpSolve problem = LpSolve.makeLp(0, col);
			// setVerbose(0) so no outputs
			problem.setVerbose(0);
			// objective funtion
			String objFunc = new String();
			for (int i = 0; i < col; i++)
				objFunc += "1 ";
			objFunc = objFunc.trim();
			// set objective function = min all columns
			problem.strSetObjFn(objFunc);
			// set all columns to be binary
			for (int i = 0; i < col; i++)
				problem.setBinary(i + 1, true);
			// add the constraints
			int noOfC = ((Boolean[]) D.get(0)).length;
			for (int i = 0; i < noOfC; i++) {
				String con = new String();
				boolean good = false;
				for (int j = 0; j < D.size(); j++) {
					Boolean[] c = (Boolean[]) D.elementAt(j);
					if (c[i].booleanValue() == true) {
						con += "1 ";
						good = true;
					} else
						con += "0 ";
				}
				con = con.trim();
				if (good)
					problem.strAddConstraint(con, LpSolve.GE, 1);
			}
			// solve the problem
			problem.solve();
			// print the result
			// problem.printLp();     
			// problem.printObjective();
			// problem.printSolution(1);
			// get the result
			double[] dblR = problem.getPtrVariables();
			// convert result from double to Boolean
			result = new Boolean[dblR.length];
			Arrays.fill(result, new Boolean(false));
			for (int i = 0; i < dblR.length; i++) {
				if (dblR[i] == 1)
					result[i] = new Boolean(true);
				else
					result[i] = new Boolean(false);
			}
			// return result
			return result;
		} catch (LpSolveException e) {
			return new Boolean[0];
		}
	}

	public void print() {
		// get size of test vector
		int tv_size = ((Boolean[]) D.get(0)).length;
		for (int i = 0; i < tv_size; i++) {
			for (int j = 0; j < D.size(); j++) {
				Boolean[] b = (Boolean[]) D.get(j);
				if (b[i].booleanValue())
					System.out.print("1 ");
				else
					System.out.print("0 ");
			}
			System.out.println();
		}
	}


	
}
