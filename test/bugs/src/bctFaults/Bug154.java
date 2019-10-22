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


import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import conf.EnvironmentalSetter;

import flattener.core.Flattener;
import flattener.core.Handler;
import flattener.flatteners.BreadthObjectFlattener;
import flattener.handlers.RawHandler;

import recorders.BreadthFlattenerAssembler;
import testSupport.TestArtifactsManager;

public class Bug154 {
	
	public static class AClass{
		
		public class ASuperInnerClass{
			private int inValue = 2;	
		}
		
		public class AInnerClass extends ASuperInnerClass {
			private int inValue = 3;	
		}
		
		private int myValue = 2;
		private AInnerClass inner = new AInnerClass();
		private AClass me = this;
		
		public AClass(){
			
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFlattenerOnSelfReferences() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		File bctHome = TestArtifactsManager.getBugFile("154/BctHome/");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		EnvironmentalSetter.setConfigurationValues();
		RawHandler rHandler = new RawHandler("root");
		BreadthObjectFlattener flattener = new BreadthObjectFlattener( rHandler );
		flattener.setSkipAlreadyVisited(true);

		AClass obj = new AClass();

		flattener.doSmash(obj);



		assertEquals("@root",rHandler.getNodeValue("root.me"));
		assertEquals("2",rHandler.getNodeValue("root.myValue"));
		
		//Pay attention, following assertion fails because we have two fields with the same name.
		//This happens because the two classes declare a private field with the same name.
		//root.inner.inValue.intValue() = 2
		//root.inner.inValue.intValue() = 3
		//
		//probably we should change the implementation in order to retrieve something like
		//root.inner.super.inValue.intValue() = 2
		//root.inner.inValue.intValue() = 3
		assertEquals("3",rHandler.getNodeValue("root.inner.inValue"));
		
		assertEquals("@root",rHandler.getNodeValue("root.inner.this$1"));
		
	}
	
	
	@Test
	public void testFlattenerOnHashSetIterator() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		File bctHome = TestArtifactsManager.getBugFile("154/BctHome/");
		EnvironmentalSetter.setBctHome(bctHome.getAbsolutePath());
		EnvironmentalSetter.setConfigurationValues();
		RawHandler rHandler = new RawHandler("root");
		Flattener flattener = new BreadthObjectFlattener( rHandler );


		Set<String> s = new HashSet<String>();
		s.add("AA");
		s.add("bbb");
		s.iterator();
		
		flattener.doSmash(s.iterator());

		
		assertEquals(null,rHandler.getNodeValue("root.this$0") );
		
	}
	
}
