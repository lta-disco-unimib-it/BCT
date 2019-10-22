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
package cpp.gdb;

public class ObjDumpUtil {

	public static String addLeadingZerosToAddress( String address ){
		int missingZeros = 16-address.length();
		if ( missingZeros > 0 ){
			address = addLeadingZeros( address, missingZeros );
		}
		return address;
	}
	
	public static String addLeadingZeros(String address, int zeroesToAdd) {
		StringBuffer zeroesBuf = new StringBuffer(); 
		for ( int i = 0 ; i < zeroesToAdd; i++ ){
			zeroesBuf.append("0");
		}
		
		return zeroesBuf.toString()+address;
	}
}
