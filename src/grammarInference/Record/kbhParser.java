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
 * Created on 12-lug-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package grammarInference.Record;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * 
 * @author Leonardo Mariani
 *
 * The parser of trace files. It can be used to return an iterator of traces.
 */
public class kbhParser implements TraceParser {
	private static final char chSeparator = '#';
	private static final char traceSeparator = '|';

	private FileReader fr = null;
	public  final static int DEFAULT_BUFFER_SIZE = 256;
	private int bufferSize = DEFAULT_BUFFER_SIZE;


	private int pos = bufferSize;
	private char[] buffer = null;
	private int nChar=bufferSize+1;

	/**
	 * it instantiates a parser associated to an existing file. If the
	 * file is not foudn an exception is generated. The internal syntax
	 * of the file must be compliant with the following syntax:
	 * 
	 * Traces are separated by the | symbol without involving any carriage
	 * return or other blank characters. Each single symbol inside a trace is
	 * separated by the # charactes. Therefore a#fd#b|f#g| is an example of 
	 * a trace file that stores two traces. The fist one with three symbols and
	 * the second one with two symbols.
	 * 
	 * @param fileName the name of the file storing the set or recorded traces
	 * @throws FileNotFoundException the exception is generated if the file
	 * does not exist
	 */
	public kbhParser(String fileName) throws FileNotFoundException {
		fr = new FileReader(fileName);
		buffer = new char[bufferSize];
	}

	/**
	 * It returns an iterator spanning all traces of the trace file.
	 * 
	 * @return a TraceIterator
	 */
	public Iterator<Trace> getTraceIterator() {
		return new TraceIterator(this);
	}

	/**
	 * The method reads a trace from the trace file. Traces are incrementally
	 * read when necessary and stored in an internal buffer.
	 * If the trace is empty a symbol, "", with 0 length is returned 
	 * @return the next trace
	 * @throws IOException the exception is generated if there are problems
	 * when accessing the file.
	 */
	private Trace readTrace() throws IOException {
		//int nChar;
		StringBuffer currentSymbol = new StringBuffer();

		int posEndSymbol = 0;
		boolean endTrace = false;

		Trace trace = new VectorTrace();

		while (!endTrace) {
			if (pos >= buffer.length) { //we have read all the data loaded in buffer
				nChar = fr.read(buffer);
				pos = 0;
				if (nChar == -1){
					return null;	//file is ended
				}
			}
			
			if (pos>=nChar){ //file is ended
				return null;
			}

			DataBuffer db = getPosSymbol(buffer, pos);
			posEndSymbol = db.pos;
			endTrace = db.endTrace;

			if (posEndSymbol == -1) { //symbol not completely contained in buffer
				
				currentSymbol.append ( new String(buffer, pos, buffer.length - pos) );
				pos = buffer.length;
			} else {
				
				if ( currentSymbol.length() > 0 || pos != posEndSymbol ){
					
					currentSymbol.append ( new String(buffer, pos, posEndSymbol - pos) );
					trace.addSymbol(currentSymbol.toString());
					currentSymbol = new StringBuffer();
				}
				pos = posEndSymbol + 1;
				
			}
		}

		return trace;
	}

	/**
	 * This function returns the position of next either end character symbol
	 * or end trace symbol. 
	 * 
	 * @param str the vector of characters where the particular characters must be found
	 * @param start the position where beginning to search
	 * @return the return data structure stores two public fields. The pos
	 * value is used to store the position of the special character. The
	 * endTrace field is used to show if the found character denotes the
	 * end of the trace or not. -1 is returned in the position if no 
	 * characters are found.
	 */
	private DataBuffer getPosSymbol(char[] str, int start) {

		DataBuffer db = new DataBuffer();
		db.endTrace = false;
		db.pos = -1;

		int i = start;

		while ((i < str.length) && (db.pos == -1)) {
			if (str[i] == chSeparator) {
				db.pos = i;
				db.endTrace = false;
			}
			if (str[i] == traceSeparator) {
				db.pos = i;
				db.endTrace = true;
			}
			i++;
		}

		return db;
	}

	/**
	 * 
	 * @author Leonardo Mariani
	 *
	 * The inner class wrapping the result. It stores the position where 
	 * a special character is found and a boolean attribute showing if 
	 * the character denotes the end of the trace of the end of the character.
	 */
	public class DataBuffer {
		public int pos;
		public boolean endTrace;
	}

	/**
	 * 
	 * @author Leonardo Mariani
	 *
	 * The TraceIterator is an iterator over objects of type Trace. The method
	 * remove is not supported by this iterator.
	 */
	public class TraceIterator implements Iterator<Trace> {
		kbhParser parser = null;
		boolean firstTrace = true;
		Trace nextTrace;
		private boolean hasNext;

		/**
		 * The constructor of the TraceIterator necessitates a parser
		 * that it uses to access to the traces stored in the file.
		 * 
		 * @param parser the parser providing providing the data stored 
		 * in the file
		 */
		public TraceIterator(kbhParser parser) {
			nextTrace = null;
			hasNext = true;

			this.parser = parser;

			UpdateTrace();
		}

		/**
		 *  Update the current state of the objects reading next trace
		 */
		private void UpdateTrace() {
			try {
				nextTrace = parser.readTrace();
				
				if (nextTrace == null) {
					hasNext = false;
				} else {
					hasNext = true;
				}
			} catch (IOException e) {
				nextTrace = null;
			}
			firstTrace=false;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return hasNext;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Trace next() {
			Trace tmpTrace = nextTrace;
			UpdateTrace();
			return tmpTrace;
		}

		/**
		 *  this method is not supported
		 */
		public void remove() {
		}
	}

	public int getBufferSize() {
		return bufferSize;
	}
}