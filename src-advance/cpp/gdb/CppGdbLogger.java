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

import java.util.logging.Level;
import java.util.logging.Logger;

public class CppGdbLogger {

	public static final Logger INSTANCE = Logger.getLogger("it.unimib.disco.lta.bct.cpp.gdb");

	public static boolean isFine() {
		return true;
	}
	
	public static void fine( String msg ){
		System.out.println(msg);
	}
}
