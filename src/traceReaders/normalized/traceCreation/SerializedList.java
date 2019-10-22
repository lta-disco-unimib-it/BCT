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
import util.SerializationUtil;

public class SerializedList<T> implements List<Token> {

	
	
	private File fileId;

	public SerializedList(File file) {
		this.fileId = file;
		
		ArrayList<Token> list = new ArrayList<Token>();
		save(list);
	}

	

	
	@Override
	public boolean add(Token arg0) {
		List<Token> list = load();
		list.add(arg0);
		save(list);
		return true;
	}
	


	@Override
	public void add(int index, Token element) {
		List<Token> list = load();
		list.add(index, element);
		save(list);
	}

	private void save(List<Token> list) {
		try {
			SerializationUtil.write(list, fileId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private List<Token> load() {
		try {
			return (List<Token>) SerializationUtil.load(fileId);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		return null;
	}


	@Override
	public boolean addAll(Collection<? extends Token> arg0) {
		List<Token> list = load();
		list.addAll(arg0);
		save(list);
		return true;
	}


	@Override
	public boolean addAll(int arg0, Collection<? extends Token> arg1) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
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
		save(l);
		return true;
	}

	@Override
	public Token remove(int arg0) {
		List<Token> l = load();
		Token r = l.remove(arg0);
		save(l);
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		List<Token> l = load();
		l.removeAll(arg0);
		save(l);
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
