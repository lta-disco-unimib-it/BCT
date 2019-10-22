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
package regressionTestManager.tcData;

import java.util.HashMap;
import java.util.Iterator;

import regressionTestManager.tcData.handlers.TcInfoHandler;

/**
 * This class contain informations on test cases:
 * 		id
 * 		methods covered
 * 		variables covered
 * 
 * FIXME: use value holder
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class TestCaseInfo implements TcInfoEntity {
	private HashMap<String,Integer> programPoints;
	private HashMap<String,Integer> meths;
	private TcInfoHandler mapper;
	protected String id;
	protected String name;
	private HashMap values = new HashMap();
	
	private class ProgramPointIterator implements Iterator<ProgramPointInfo> {
		Iterator idIt;
		
		ProgramPointIterator ( Iterator idIterator ){
			idIt = idIterator;
		}
		
		public boolean hasNext() {
			return idIt.hasNext();
		}

		public ProgramPointInfo next() {
			return mapper.getProgramPointInfo( (String)idIt.next() );
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	/**
	 * Create a TestCaseInfo, for a test case having the id and name passed. The mapper is used to save test case data.
	 * 
	 * @param id
	 * @param name
	 * @param mapper
	 */
	public TestCaseInfo ( String id, String name, TcInfoHandler mapper ){
		this( id, name, mapper, new HashMap<String,Integer>(), new HashMap<String,Integer>() );
	}
	/**
	 * Create a TestCaseInfo with the given programPoints and methods associated
	 * 
	 * @param id
	 * @param name
	 * @param mapper
	 * @param programPoints
	 * @param methods
	 */
	public TestCaseInfo ( String id, String name, TcInfoHandler mapper, HashMap<String,Integer> programPoints, HashMap<String,Integer> methods ){
		this.mapper = mapper;
		this.programPoints = programPoints;
		this.meths = methods;
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Add a program point to the current test case
	 * 
	 * @param pp
	 */
	public void addProgramPoint(ProgramPointInfo pp){
		mapper.setModified(this);
		Integer value = programPoints.get(pp);
		if ( value == null ){
			value = 1;
		} else {
			value++;
		}
		programPoints.put(pp.getId(), value);
	}


	public void addMethod(MethodInfo methInfo) {
		//System.out.println("ADD METHOD "+methInfo.getId()+" TO TEST "+this.getName());
		mapper.setModified(this);
		Integer occ = (Integer) meths.get(methInfo.getId());
		//System.out.println("OCC "+occ);
		if ( occ == null ){
			meths.put(methInfo.getId(),1);
		} else {
			meths.put(methInfo.getId(),occ+1);
		}
		
	}

	public String getId() {
		return id;
	}

	public Iterator getMethodsIterator(){
		return meths.keySet().iterator();
	}
	

	public Integer getMethodOccurrencies( String id ){
		
		Integer occ = meths.get(id);
		if ( occ == null )
			return 0;
		
		//System.out.println("OCCURRENCIES FOR "+id+" = "+occ);
		return occ;
	}
	
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		String res = "TestCaseInfo("+id+")";
		
		res += "[";
		Iterator it = meths.keySet().iterator();
		while ( it.hasNext() ){
			String key = ((String)it.next());
			res+=key+":"+meths.get(key)+",";
		}
		res += "]";
		
		res += "[";
		it = programPoints.keySet().iterator();
		while ( it.hasNext() )
			res+=(String)it.next()+",";
		res += "]";
		
		return res;
		
	}
	
	private String getCode( String programPoint, String name ){
		return programPoint+":"+name;
	}
	
	public Iterator getProgramPointsIdsIterator(){
		return programPoints.keySet().iterator();
	}

	public Iterator getProgramPointsIterator() {
		
		return new ProgramPointIterator(programPoints.keySet().iterator());
	}

	public boolean equals (Object o){
		if ( o == this )
			return true;
		if ( ! ( o instanceof TestCaseInfo ) ){
			return false;
		}
		TestCaseInfo rhs = (TestCaseInfo) o;
		
		if ( ! id.equals(rhs.id) )
			return false;
		
		
		if ( ! programPoints.keySet().equals( rhs.programPoints.keySet() ) )
			return false;
			
		if ( ! meths.keySet().equals( rhs.meths.keySet() ) )
			return false;
		
		return true;
	}
	
	public String getTestClassName() {
		int classNameEnd = name.lastIndexOf('.');
		return name.substring(0,classNameEnd);
	}
	

	public int hashCode(){
		return (id.hashCode()|name.hashCode());
	}
}
