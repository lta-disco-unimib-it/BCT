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
package bctFaults;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for bctFaults");
		//$JUnit-BEGIN$
		suite.addTestSuite(Bug136.class);
		suite.addTestSuite(Bug142.class);
		suite.addTestSuite(Bug88Test.class);
		suite.addTestSuite(Bug143Test.class);
		suite.addTestSuite(Bug127Test.class);
		suite.addTestSuite(Bug148.class);
		//suite.addTestSuite(Bug82Test.class);
		suite.addTestSuite(Bug84Test.class);
		suite.addTestSuite(Bug137.class);
		suite.addTestSuite(Bug149Test.class);
		//$JUnit-END$
		return suite;
	}

}
