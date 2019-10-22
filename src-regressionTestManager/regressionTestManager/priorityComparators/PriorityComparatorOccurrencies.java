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
package regressionTestManager.priorityComparators;

import java.util.Comparator;

import regressionTestManager.detectionMatrix.TcDetectionMatrix;
import regressionTestManager.tcData.TestCaseInfo;


/**
 * Is a priority comparator that order elements on the base of thei occurrencies
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class PriorityComparatorOccurrencies implements Comparator<TestCaseInfo> {
	private TcDetectionMatrix dm;
	
	public PriorityComparatorOccurrencies ( TcDetectionMatrix dm ){
		this.dm = dm;
	}
	

	public int compare(TestCaseInfo o1, TestCaseInfo o2) {
		return dm.getTrueCount(o2) - dm.getTrueCount(o1);
	}

}
