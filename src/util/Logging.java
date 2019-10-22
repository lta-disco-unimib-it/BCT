/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {

	private static Logger logger;
	
	public static void fine(String arg0) {
		logger.fine(arg0);
	}

	public static void finer(String arg0) {
		logger.finer(arg0);
	}

	public static void finest(String arg0) {
		logger.finest(arg0);
	}

	public static void info(String arg0) {
		logger.info(arg0);
	}

	public static void severe(String arg0) {
		logger.severe(arg0);
	}

	public static void warning(String arg0) {
		logger.warning(arg0);
	}

	static {
		logger = Logger.getLogger("BCT");
	}

	public static boolean fineIsLoggable() {
		return logger.isLoggable(Level.FINE);
	}

//	public static void log(String string) {
//		if ( ! ENABLED ){
//			return;
//		}
//		
//		logger.info(string);
//	}

}
