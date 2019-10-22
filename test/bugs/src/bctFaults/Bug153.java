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


import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import testSupport.TestArtifactsManager;

import tools.InvariantGenerator;

import conf.EnvironmentalSetter;

/**
 * Tests a bug that causes an infinite loop.
 * This happens when the flattener records something like:
 * 
 * returnValue.this$0
 * @returnValue.this$0
 * 1
 * 
 * In principle this should not happen, but we want to have an InvariantGenerator able to manage such cases.
 * 
 * @author Fabrizio Pastore
 *
 */
public class Bug153 {



	/**
	 * Fails if the invariant generator works for too much: we have an infinite loop in this case.
	 */
	@Test(timeout=20000)
	public void testInvariantGenerator(){
		File bctHome = TestArtifactsManager.getBugFile("153/BctHome/");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		
		String args[] = new String[]{"-default"};
		InvariantGenerator.main(args);		

		
	}
}
