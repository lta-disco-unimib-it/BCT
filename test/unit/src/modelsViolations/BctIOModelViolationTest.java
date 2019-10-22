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
package modelsViolations;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import testSupport.ViolationsAnalyzerSupport;

public class BctIOModelViolationTest {
	
	@Test
	public void test0(){
		BctIOModelViolation v0 = ViolationsAnalyzerSupport.createV0();
		assertEquals( "pack1.ClassB.b()", v0.getViolatedMethod() );
		assertEquals( BctIOModelViolation.Position.ENTER, v0.getPosition() );
		
		BctIOModelViolation v1 = ViolationsAnalyzerSupport.createV1();
		assertEquals( "pack1.ClassB.b()", v0.getViolatedMethod() );
		assertEquals( BctIOModelViolation.Position.EXIT, v1.getPosition() );
	}

}
