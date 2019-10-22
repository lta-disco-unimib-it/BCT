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
/*
 * Created on 29-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package traceReaders.raw;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Leonardo Mariani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InteractionTraceReader {
	private final static char SEPARATOR = '#';
	
	private static final int DEFAULT_BUFFERSIZE=512;
	char buffer[];
	int readedCharacters=0;
	int sentChr = 0;
	InputStreamReader fr=null;

	
	/**
	 * 
	 */
	public InteractionTraceReader(InputStreamReader fr) {
		this.fr=fr;
		buffer = new char[DEFAULT_BUFFERSIZE];
	}
	
	public InteractionTraceReader(int bufferSize,InputStreamReader fr) {
		this.fr=fr;
		buffer = new char[bufferSize];
	}

	private void readBlock() throws IOException {
		readedCharacters = fr.read(buffer);
	}
	
	public String getNextToken() throws IOException {
		String tk="";
		

		
		if (readedCharacters == sentChr) {
			readBlock();
			sentChr=0;
		}

		
		if (readedCharacters <=0){ 
			return null;
		}
		
		int i=sentChr;
		
		while ((buffer[i]!=SEPARATOR) && (readedCharacters>0) ) {
			i++;
			if (i==readedCharacters) {
				tk = tk + new String(buffer,sentChr,i-sentChr);
				readBlock();
				i=0;
				sentChr=0;
			}
		}

		if (readedCharacters == -1) {
			return null;
		} else if (readedCharacters == 0) {
			System.out.println("Incorrect input file!");
			return null;
		} else {
			String returnString = tk + new String(buffer,sentChr,i-sentChr);
			sentChr=i+1;
			return returnString;
		}
	}

	

}
