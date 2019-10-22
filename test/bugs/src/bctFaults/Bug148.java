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

import junit.framework.TestCase;

import org.eclipse.tptp.logging.events.cbe.CommonBaseEvent;
import org.eclipse.tptp.logging.events.cbe.FormattingException;

import testSupport.TestArtifactsManager;
import util.FileUtil;
import util.cbe.CBELogLoader;

public class Bug148 extends TestCase {

	/**
	 * @param args
	 * @throws FormattingException 
	 */
	public static void testBug() throws FormattingException {
		CBELogLoader loader = new CBELogLoader();
		
		File file = TestArtifactsManager.getBugFile("148/bctCBELog");
		System.out.println(file.getAbsolutePath());
		CommonBaseEvent[] result = loader.loadCBE(file);
		
		assertNotNull(result);
	}

}
