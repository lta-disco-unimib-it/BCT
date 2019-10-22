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
package regressionTestManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import regressionTestManager.detectionMatrix.InteractionTcDetectionMatrix;
import regressionTestManager.detectionMatrix.IoTcDetectionMatrix;
import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.priorityComparators.InteractionPriorityComparator;
import regressionTestManager.priorityComparators.IoPriorityComparator;
import regressionTestManager.priorityComparators.PriorityComparatorOccurrencies;
import regressionTestManager.tcData.TestCaseInfo;


public class TcPrioritySorter {

	/**
	 * Return a list of testCases ordered  accortding to priority rules and removing test cases previously selected
	 * 
	 * @param resultIo
	 * @param dmIO
	 */
	public static List sortIO(RegressionTestCollection resultIo, IoTcDetectionMatrix detectionMatrix, IoPriorityComparator comparator ) {
		return genericSort( resultIo, detectionMatrix, comparator );
	}

	/**
	 * Return a list of testCases ordered  accortding to priority rules and removing test cases previously selected
	 * 
	 * @param resultIo
	 * @param dmIO
	 */
	public static List sortInteraction(RegressionTestCollection resultInteraction, InteractionTcDetectionMatrix detectionMatrix, InteractionPriorityComparator comparator ) {
		return genericSort( resultInteraction, detectionMatrix, comparator );
	}
	
	
	/**
	 * Return a list of testCases ordered  accortding to priority rules and removing test cases previously selected
	 * 
	 * @param resultIo
	 * @param dmIO
	 */
	public static List sortGeneric(RegressionTestCollection resultInteraction, TcDetectionMatrix detectionMatrix, PriorityComparatorOccurrencies comparator ) {
		return genericSort( resultInteraction, detectionMatrix, comparator );
	}
	
	
	private static List<TestCaseInfo> genericSort(RegressionTestCollection suite, TcDetectionMatrix detectionMatrix, Comparator comparator) {
		ArrayList<TestCaseInfo> set = new ArrayList<TestCaseInfo>();
		Iterator<TestCaseInfo> it = detectionMatrix.getTestCasesIterator();
		
		while ( it.hasNext() ){
			TestCaseInfo el = it.next();
			if ( ! suite.containsTestCase(el) )
				set.add(el);
		}
		Collections.sort(set,comparator); 
		return set;
	}

}
