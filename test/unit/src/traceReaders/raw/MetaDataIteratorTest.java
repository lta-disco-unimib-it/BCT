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
package traceReaders.raw;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.Test;



public class MetaDataIteratorTest {
	private static final String voidMetaTrace = "test/unit/artifacts/MetaDataIterator/void.meta";
	private static final String oneElMetaTrace = "test/unit/artifacts/MetaDataIterator/oneEl.meta";
	private static final String manyElsMetaTrace = "test/unit/artifacts/MetaDataIterator/manyEls.meta";
	
	
	@Test
	public void testHasNext_voidTrace() throws IOException {
		
		BufferedReader br = new BufferedReader( new FileReader(voidMetaTrace));
		
		MetaDataIterator it = new MetaDataIterator(br);
		
		assertFalse( it.hasNext() );
	
		br.close();
		
	}

	@Test(expected=java.util.NoSuchElementException.class) 
	public void testNext_void() throws IOException {
		BufferedReader br = new BufferedReader( new FileReader(voidMetaTrace));
		
		MetaDataIterator it = new MetaDataIterator(br);
		
		//trace is void an exception will be thrown
		Object val = it.next();
	
		br.close();
	}
	
	@Test (expected=NoSuchElementException.class)
	public void testNext_oneElement() throws IOException {
		BufferedReader br = new BufferedReader( new FileReader(oneElMetaTrace));
		
		MetaDataIterator it = new MetaDataIterator(br);
		
		String el = (String) it.next();
		
		assertEquals("metaValue0", el);
		
		//no more elements exception thrown
		it.next();
		
		br.close();
	}

	
	
	public void testNext_manyElements() throws IOException {
		BufferedReader br = new BufferedReader( new FileReader(manyElsMetaTrace));
		
		MetaDataIterator it = new MetaDataIterator(br);
		
		String el = (String) it.next();
		
		assertEquals("metaValue0", el);
		
		//no more elements exception thrown
		el = (String) it.next();
		
		assertEquals("metaValue1", el);
		
		el = (String) it.next();
		
		assertEquals("\nmetaValue2\n", el);
		
		assertFalse(it.hasNext());
		
		br.close();
	}
	


}
