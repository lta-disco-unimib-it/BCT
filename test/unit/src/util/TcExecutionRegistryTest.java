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
package util;

import junit.framework.TestCase;

public class TcExecutionRegistryTest extends TestCase {
	private final String signature = "method";
	private final String signature2 = "method2";
	
	public TcExecutionRegistryTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetInstance() {
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();
		TCExecutionRegistry instance2 = TCExecutionRegistry.getInstance();
		assertTrue( instance == instance2 );
	}

	public void testTcEnter() {
		
		
		
	}

	public void testTcExit() {
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();
		try {
			instance.tcEnter(signature);
			instance.tcExit(signature);
		} catch (TCExecutionRegistryException e) {
			fail("Exception");
		}
		
		try {
			instance.tcExit(signature);
			fail("Exception expected");
		} catch (TCExecutionRegistryException e) {
			
		}
	}
	
	public void insertRemoveFirstCorrect(){
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();

		
		try {
			instance.tcEnter(signature);
			assertEquals(signature, instance.getCurrentTest());
			instance.tcExit(signature);
		} catch (TCExecutionRegistryException e) {
			fail("Exception");
		}
	}

	public void insertFirstNotCorrect(){
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();

		
		try {
			instance.tcEnter(signature);
			
		} catch (TCExecutionRegistryException e) {
			fail("Exception");
		}
		
		try {
			instance.tcEnter(signature);
			fail("Exception expected");
		} catch (TCExecutionRegistryException e) {
		
		}
		try {
			assertEquals(signature, instance.getCurrentTest());
		} catch (TCExecutionRegistryException e) {
			fail("Exception");
		}
		
	}
	
	public void removeFirstNotCorrect(){
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();
		try {
			instance.tcExit(signature);
			fail("Exception Expected");
		} catch (TCExecutionRegistryException e) {
			
		}
	}

	public void insertRemoveNCorrect(){
		TCExecutionRegistry instance = TCExecutionRegistry.getInstance();

		
		try {
			instance.tcEnter(signature);
			assertEquals(signature, instance.getCurrentTest());
			instance.tcExit(signature);
			instance.tcEnter(signature2);
			assertEquals(signature2, instance.getCurrentTest());
			instance.tcExit(signature2);
			instance.tcEnter(signature);
			assertEquals(signature, instance.getCurrentTest());
			instance.tcExit(signature);
			instance.tcEnter(signature);
			assertEquals(signature, instance.getCurrentTest());
			instance.tcExit(signature);
			
		} catch (TCExecutionRegistryException e) {
			fail("Exception");
		}
	}
	
}
