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
package tools.gdbTraceParser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import tools.gdbTraceParser.DataFlowGdbThreadTraceListener.VarLinesCluster;
import tools.gdbTraceParser.DataFlowGdbThreadTraceListener.VarValueClustersCollection;

import dfmaker.core.Variable;

public class DataFlowGdbThreadTraceListenerTest {

	/**
	 * f 1 a=1 d=1
	 * f 2 a=1 d=1
	 * f 3 a=2 d=3
	 * 
	 * f 4 a=2 d=3
	 * f 5 a=2 d=3
	 */
	@Test
	public void testSingleInvocation() {
		List<String> globalVariableNames = new ArrayList<String>();
		globalVariableNames.add("d");
		
		DataFlowGdbThreadTraceListener listener = new DataFlowGdbThreadTraceListener();
	
		listener.newSession("1");
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		

		
		VarValueClustersCollection clustersCollection = listener.getValueClusters();
		printClusters(clustersCollection);
		
		assertTrue( clustersCollection.sameValues("d", "f", 1) );
		assertTrue( clustersCollection.sameValues("<f>a", "f", 1) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 1, 2) );
		assertTrue( clustersCollection.sameValues("d", 1, 2) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 2, 3) );
		assertFalse( clustersCollection.sameValues("d", 2, 3) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 3, "f") );
		assertTrue( clustersCollection.sameValues("d", 3, "f") );
		
		
		
	}
	
	
	/**
	 * f 1 a=1 d=1
	 * f 2 a=1 d=1
	 * f 3 a=2 d=3
	 * 
	 * f 4 a=2 d=3
	 * f 5 a=2 d=3
	 */
	@Test
	public void testSequentialInvocation() {
		List<String> globalVariableNames = new ArrayList<String>();
		globalVariableNames.add("d");
		
		DataFlowGdbThreadTraceListener listener = new DataFlowGdbThreadTraceListener();
	
		listener.newSession("1");
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionEnter("g",listener, 4, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("g",listener, 4, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("g",listener, 5, localVariables, globalVariableNames);
		}

		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "4", 1));
			functionExit("g",listener, 5, localVariables, globalVariableNames);
		}
		

		
		VarValueClustersCollection clustersCollection = listener.getValueClusters();
		printClusters(clustersCollection);
		
		assertTrue( clustersCollection.sameValues("d", "f", 1) );
		assertTrue( clustersCollection.sameValues("<f>a", "f", 1) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 1, 2) );
		assertTrue( clustersCollection.sameValues("d", 1, 2) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 2, 3) );
		assertFalse( clustersCollection.sameValues("d", 2, 3) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 3, "f") );
		assertTrue( clustersCollection.sameValues("d", 3, "f") );
		
		assertFalse( clustersCollection.sameValues("<g>a", "g", 4) );
		assertTrue( clustersCollection.sameValues("d", "g", 4) );
		
		assertTrue( clustersCollection.sameValues("<g>a", 4, 5) );
		assertTrue( clustersCollection.sameValues("d", 4, 5) );
		
		assertTrue( clustersCollection.sameValues("<g>a", 5, "g") );
		assertFalse( clustersCollection.sameValues("d", 5, "g") );
		
	}
	
	@Test
	public void testInternalInvocation() {
		List<String> globalVariableNames = new ArrayList<String>();
		globalVariableNames.add("d");
		
		DataFlowGdbThreadTraceListener listener = new DataFlowGdbThreadTraceListener();
	
		listener.newSession("1");
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionEnter("g",listener, 4, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("g",listener, 4, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("g",listener, 5, localVariables, globalVariableNames);
		}

		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "2", 1));
			localVariables.add(new Variable("d", "4", 1));
			functionExit("g",listener, 5, localVariables, globalVariableNames);
		}
		
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		
		

		
		VarValueClustersCollection clustersCollection = listener.getValueClusters();
		
		assertTrue( clustersCollection.sameValues("d", "f", 1) );
		assertTrue( clustersCollection.sameValues("<f>a", "f", 1) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 1, 2) );
		assertTrue( clustersCollection.sameValues("d", 1, 2) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 2, 3) );
		assertFalse( clustersCollection.sameValues("d", 2, 3) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 3, "f") );
		assertTrue( clustersCollection.sameValues("d", 3, "f") );
		
		assertFalse( clustersCollection.sameValues("<g>a", "g", 4) );
		assertTrue( clustersCollection.sameValues("d", "g", 4) );
		
		assertTrue( clustersCollection.sameValues("<g>a", 4, 5) );
		assertTrue( clustersCollection.sameValues("d", 4, 5) );
		
		assertTrue( clustersCollection.sameValues("<g>a", 5, "g") );
		assertFalse( clustersCollection.sameValues("d", 5, "g") );
		
	}
	
	
	
	@Test
	public void testRecursiveCall() {
		List<String> globalVariableNames = new ArrayList<String>();
		globalVariableNames.add("d");
		
		DataFlowGdbThreadTraceListener listener = new DataFlowGdbThreadTraceListener();
	
		listener.newSession("1");
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		//RECURSION - START
		
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "-1", 1));
			localVariables.add(new Variable("d", "30", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "-1", 1));
			localVariables.add(new Variable("d", "30", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		//RECURSION - END
		
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		
		

		
		VarValueClustersCollection clustersCollection = listener.getValueClusters();
		printClusters(clustersCollection);
		
		assertTrue( clustersCollection.sameValues("d", "f", 1) );
		assertTrue( clustersCollection.sameValues("<f>a", "f", 1) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 1, 2) );
		assertTrue( clustersCollection.sameValues("d", 1, 2) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 2, 3) );
		assertFalse( clustersCollection.sameValues("d", 2, 3) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 3, "f") );
		assertTrue( clustersCollection.sameValues("d", 3, "f") );
		
		
		
	}
	
	
	
	@Test
	public void testRecursiveCall_differ() {
		List<String> globalVariableNames = new ArrayList<String>();
		globalVariableNames.add("d");
		
		DataFlowGdbThreadTraceListener listener = new DataFlowGdbThreadTraceListener();
	
		listener.newSession("1");
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "1", 1));
			localVariables.add(new Variable("d", "2", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		//RECURSION - START
		
		{	
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			functionEnter("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			programpoint("f",listener, 1, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "20", 1));
			programpoint("f",listener, 2, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "10", 1));
			localVariables.add(new Variable("d", "9", 1));
			programpoint("f",listener, 5, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "-1", 1));
			localVariables.add(new Variable("d", "30", 1));
			functionExit("f",listener, 5, localVariables, globalVariableNames);
		}
		
		//RECURSION - END
		
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			programpoint("f",listener, 3, localVariables, globalVariableNames);
		}
		
		{
			List<Variable> localVariables = new ArrayList<Variable>();
			localVariables.add(new Variable("a", "0", 1));
			localVariables.add(new Variable("d", "3", 1));
			functionExit("f",listener, 3, localVariables, globalVariableNames);
		}
		
		
		

		
		VarValueClustersCollection clustersCollection = listener.getValueClusters();
		
		printClusters(clustersCollection);
		
		
		assertTrue( clustersCollection.sameValues("d", "f", 1) );
		assertTrue( clustersCollection.sameValues("<f>a", "f", 1) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 1, 2) );
		assertTrue( clustersCollection.sameValues("d", 1, 2) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 2, 3) );
		assertFalse( clustersCollection.sameValues("d", 2, 3) );
		
		assertTrue( clustersCollection.sameValues("<f>a", 2, 5) );
		assertFalse( clustersCollection.sameValues("d", 2, 5) );
		
		assertFalse( clustersCollection.sameValues("<f>a", 5, "f") );
		assertFalse( clustersCollection.sameValues("d", 5, "f") );
		
		assertTrue( clustersCollection.sameValues("<f>a", 3, "f") );
		assertTrue( clustersCollection.sameValues("d", 3, "f") );
		
		
		
		
	}

	public void printClusters(VarValueClustersCollection clustersCollection) {
		for ( Entry<String, Set<VarLinesCluster>>  e : clustersCollection.getClusters().entrySet() ){
			System.out.println("Var: "+e.getKey());
			for ( VarLinesCluster cl : e.getValue() ){
				System.out.println("\t"+cl);
			}
		}
	}
	
	
	
	public void programpoint(String functionName, DataFlowGdbThreadTraceListener listener,
			int lineNo, List<Variable> localVariables,
			List<String> globalVariableNames) {
		List<Variable> parameters = new ArrayList<Variable>();
		List<Variable> parentLocalVariables = new ArrayList<Variable>();
		List<Variable> parentArguments = new ArrayList<Variable>();
		List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
		
		
		listener.genericProgramPoint(functionName, parameters, localVariables, parentLocalVariables, parentArguments, 1, "a.c", lineNo, stack, globalVariableNames);
	}

	public void functionEnter(String functionName, DataFlowGdbThreadTraceListener listener,
			int lineNo, List<Variable> localVariables,
			List<String> globalVariableNames) {
		List<Variable> parameters = new ArrayList<Variable>();
		List<Variable> parentLocalVariables = new ArrayList<Variable>();
		List<Variable> parentArguments = new ArrayList<Variable>();
		List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
		
		
		listener.functionEnter(functionName, parameters, localVariables, parentLocalVariables, parentArguments, 1, "a.c", lineNo, stack, globalVariableNames);
	}

	public void functionExit(String functionName, DataFlowGdbThreadTraceListener listener,
			int lineNo, List<Variable> localVariables,
			List<String> globalVariableNames) {
		List<Variable> parameters = new ArrayList<Variable>();
		List<Variable> parentLocalVariables = new ArrayList<Variable>();
		List<Variable> parentArguments = new ArrayList<Variable>();
		List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
		
		
		List<Variable> returnValues = null;
		
		listener.functionExit(functionName, parameters, localVariables, parentLocalVariables, parentArguments, returnValues, 1, "a.c", lineNo, stack, globalVariableNames);
	}
}
