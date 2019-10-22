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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import regressionTestManager.tcData.TestCaseInfo;

public class RegressionTestCollection {
	
	private class ReverseComparator implements Comparator<TestCaseInfo> {

		public int compare(TestCaseInfo o1, TestCaseInfo o2) {
			Double second = testMap.get(02);
			Double first = testMap.get(o1);
			if ( first == null || second == null )
				return -1;
			if ( first.equals(second ) )
				return 0;
			if ( first > second )
				return -1;
			return +1;
		}
		
	}
	
	
	private HashMap<TestCaseInfo,Double> testMap = new HashMap<TestCaseInfo,Double>( );

	/**
	 * Add a  test case to the regression test collection.
	 *  
	 * @param testCase
	 * @param Priority
	 */
	public void addTest( TestCaseInfo testCase, Double priority ){
		testMap.put(testCase, priority);
	}
	
	/**
	 * Return an iterator over the test cases ordered by priority
	 *  
	 * @return
	 */
	public Iterator<TestCaseInfo> getOrderedIterator(){
		
		List sortedList = new ArrayList();
		sortedList.addAll(testMap.keySet());
		
		Collections.sort(sortedList, new ReverseComparator() );
		
		
		return sortedList.iterator();
	}

	public boolean containsTestCase(Object key) {
		return testMap.containsKey(key);
	}
	
	public Double getTestCasePriority(Object key) {
		return testMap.get(key);
	}
	
	public String toString(){
		String res = "RegressionTestCollection : ";
		for ( TestCaseInfo test :  testMap.keySet() ){
			res += test.getName();
		}
		return res;
	}
	
}
