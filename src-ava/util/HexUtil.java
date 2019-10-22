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

import java.math.BigInteger;

public class HexUtil {

	public static String hexToFloatString( String value ){
		Float f = hexToFloat(value);
		return f.toString();
	}

	public static Float hexToFloat(String value) {
		if ( value.startsWith("0x") ){
			value = value.substring(2);
		}
		Long l = Long.parseLong(value, 16);
		Float f = Float.intBitsToFloat(l.intValue());
		return f;
	}

	public static String hexToDoubleString( String value ){
		Double f = hexToDouble(value);
		return f.toString();
	}

	public static Double hexToDouble(String value) {
		if ( value.startsWith("0x") ){
			value = value.substring(2);
		}
		Long l = new BigInteger(value, 16).longValue();
		Double f = Double.longBitsToDouble(l);
		return f;
	}
	
	public static Long hexToLong(String value) {
		if ( value.startsWith("0x") ){
			value = value.substring(2);
		}
		Long l = new BigInteger(value, 16).longValue();
		return l;
	}
	
	public static int hexToInt(String value) {
		return hexToLong(value).intValue();
	}

}
