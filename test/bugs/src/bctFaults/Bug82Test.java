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

import org.junit.Ignore;

import junit.framework.TestCase;
import traceReaders.raw.FileIoTrace;
import traceReaders.raw.IoTrace;

@Ignore("FIXME")
public class Bug82Test {

	public void failingTest(){
		IoTrace trace = new FileIoTrace("m",new File("test/bugs/artifacts/82/12.dtrace"), new File("test/bugs/artifacts/82/12.meta") );
		
	}
	
}
