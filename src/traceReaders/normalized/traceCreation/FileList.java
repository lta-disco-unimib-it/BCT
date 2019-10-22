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
package traceReaders.normalized.traceCreation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import traceReaders.raw.Token;

public class FileList<T> implements List<Token> {

	
	
	private File fileId;

	public FileList(File file) {
		this.fileId = file;
		
		BufferedWriter w = getWriter();
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private BufferedWriter getWriter(){
		return getWriter(true);
	}
	
	private BufferedWriter getWriter(boolean append){
		try {
			return new BufferedWriter(new FileWriter( fileId, append ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean add(Token arg0) {
		BufferedWriter w = getWriter();
		writeToken(w, arg0);
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean writeToken( BufferedWriter w,  Token arg0 ){
		try {
			w.append(arg0.toString());
			w.newLine();
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void add(int index, Token element) {
		List<Token> list = load();
		list.add(index, element);
		BufferedWriter w = getWriter(false);
		writeAll(w, list);
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Token> load() {
		
		//System.out.println("Loading "+fileId.getAbsolutePath());
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(fileId));
		
		
		List<Token> l = new ArrayList<Token>();
		
		String line;
		
			while (  ( line = r.readLine() ) != null ){
				//System.out.println("Loaded: "+line);
				l.add(new Token(line));
			}
			
//			if ( l.size() > 0 ){  
//					//size == 0 when empty
//					//size == 2 when one element inside
//				l.remove(l.size()-1);
//			}
			
			return l;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if ( r != null ){
				try {
					r.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}


	@Override
	public boolean addAll(Collection<? extends Token> arg0) {
		BufferedWriter w = getWriter();
		writeAll(w, arg0);
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}
	
	public boolean writeAll(BufferedWriter writer, Collection<? extends Token> arg0) {
		for ( Token t : arg0 ){
			writeToken(writer, t);
		}
		return true;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends Token> arg1) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public void clear() {
		BufferedWriter w = getWriter(false);
		try {
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public Token get(int arg0) {
		return load().get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		return load().indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return load().iterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		// TODO Auto-generated method stub
		return load().lastIndexOf(arg0);
	}

	@Override
	public ListIterator<Token> listIterator() {
		// TODO Auto-generated method stub
		return load().listIterator();
	}

	@Override
	public ListIterator<Token> listIterator(int arg0) {
		// TODO Auto-generated method stub
		return load().listIterator(arg0);
	}

	@Override
	public boolean remove(Object arg0) {
		List<Token> l = load();
		l.remove(arg0);
		BufferedWriter writer = getWriter(false);
		writeAll(writer, l);
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public Token remove(int arg0) {
		List<Token> l = load();
		Token r = l.remove(arg0);
		BufferedWriter writer = getWriter(false);
		writeAll(writer, l);
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		List<Token> l = load();
		l.removeAll(arg0);
		BufferedWriter writer = getWriter(false);
		writeAll(writer, l);
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new NotImplementedException();
	}

	@Override
	public Token set(int arg0, Token arg1) {
		throw new NotImplementedException();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return load().size();
	}

	@Override
	public List<Token> subList(int arg0, int arg1) {
		return load().subList(arg0, arg1);
	}

	@Override
	public Object[] toArray() {
		return load().toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return load().toArray(arg0);
	}

}
