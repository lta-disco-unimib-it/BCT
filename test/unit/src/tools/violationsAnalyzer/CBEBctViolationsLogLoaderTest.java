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
package tools.violationsAnalyzer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import failureDetection.Failure;

import junit.extensions.TestDecorator;

public class CBEBctViolationsLogLoaderTest {

	@Test
	public void testLoadGenericFailure() throws CBEBctViolationsLogLoaderException{
		File file = new File("test/unit/artifacts/tools/violationsAnalyzer/cbeLogWithGenericFailure");
		
		CBEBctViolationsLogLoader loader = new CBEBctViolationsLogLoader();
		Collection<File> cbeLogs = new ArrayList<File>();
		cbeLogs.add(file);
		
		BctViolationsLogData logData = loader.load(cbeLogs);
		
		List<Failure> failures = logData.getFailures();
		
		assertEquals ( 1, failures.size() );
		
		Failure failure = failures.get(0);
		
		assertEquals ( null, failure.getFailingPID() );
	}
}
