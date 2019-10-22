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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HexUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testHexToInt() throws FileNotFoundException {
		String value = "0x7fff26980000";
		
		assertFalse(0 == HexUtil.hexToInt(value) );
		
		assertFalse(0 == HexUtil.hexToInt("0xffffffffffff") );
		
		assertFalse(0 == HexUtil.hexToInt("0x7fff26980000") );
		
		assertFalse(0 == HexUtil.hexToInt("0x6f3c9b80") );
		
		
		System.out.println(HexUtil.hexToInt("0xffffffffffff"));
		
		List<String> lines = FileUtil.getLines( new File("/home/user/hexValues.txt") );
		for( String line : lines ){
			assertFalse(0 == HexUtil.hexToInt(line) );
		}
	}

}
