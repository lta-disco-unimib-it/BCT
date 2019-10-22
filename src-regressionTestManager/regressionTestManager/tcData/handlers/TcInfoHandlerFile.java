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
package regressionTestManager.tcData.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import regressionTestManager.RegressionTestManagerHandlerSettings;
import regressionTestManager.VariableInfo;
import regressionTestManager.tcData.MethodInfo;
import regressionTestManager.tcData.ProgramPointInfo;
import regressionTestManager.tcData.TcInfoEntity;
import regressionTestManager.tcData.TestCaseInfo;
import util.FileIndex;
import util.FileIndex.FileIndexException;

public class TcInfoHandlerFile implements TcInfoHandler {
	private File dir;
	private FileIndex testCases;
	private FileIndex variables;
	private FileIndex programPoints;
	private FileIndex methods;
	
	private File testCasesFile;
	private File variablesFile;
	private File methodsFile;
	private File programPointsFile;
	
	private HashMap<String,MethodInfo> methModified = new HashMap<String,MethodInfo>();
	private HashMap varModified = new HashMap();
	private HashMap tcModified = new HashMap();
	private HashMap<String,ProgramPointInfo> ppModified = new HashMap<String,ProgramPointInfo>();

	public interface PropertiesKeys {
		public String DIR = "testCaseInfoHandler.dir";
	}

	public void init(RegressionTestManagerHandlerSettings settings) {
		String dirName = settings.getProperty(PropertiesKeys.DIR);
		//System.out.println("CALL");
		dir = new File ( dirName );

		dir.mkdirs();
		
		variablesFile = new File ( dir, "variables.idx" );
		methodsFile = new File ( dir, "methods.idx" );
		testCasesFile = new File ( dir, "testCases.idx" );
		programPointsFile = new File ( dir, "programPoints.idx");
		
		variables = new FileIndex(variablesFile);
		testCases = new FileIndex(testCasesFile);
		methods = new FileIndex(methodsFile);
		programPoints = new FileIndex(programPointsFile);
		
	}
	
	
	
	public MethodInfo getMethodInfo(String methodName) {
		String id = null;
		//System.out.println("Get method info "+methodName);
		if ( ! methods.containsName(methodName) )
			try {
				id = methods.add(methodName);
			} catch (FileIndexException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else
			try {
				id = methods.getId( methodName );
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//System.out.println(" ID for "+methodName+" is "+id);
		if ( methModified.containsKey(id))
			return (MethodInfo) methModified.get(id);
		return new MethodInfo(id);
	}

	/**
	 * This method create a new test case info loading its data from the given file
	 * 
	 * @param id
	 * @param name
	 * @param file
	 * @return
	 */
	private TestCaseInfo loadTestCaseInfoFromFile(String id, String name, File file) {
		//System.out.println("LOAD TC INFO "+id+" "+name+" "+file);
		HashMap<String,Integer> methods = new HashMap<String,Integer>();
		HashMap<String,Integer> pps = new HashMap<String,Integer>();
		
		try {
			BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream ( file )) );
			String line = br.readLine();
			while ( line != null ){
				if ( line.startsWith("M") ){
					addInfoFileLine( methods, line );
				} else { 
					addInfoFileLine( pps, line );
				}
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {
			
		}
		
		return new TestCaseInfo( id, name, this, pps, methods );
	}

	/**
	 * This method add the contents of an info file line to the given HashMap
	 * A line must be in the format:
	 * 
	 * Name#Id
	 * 
	 * Where Name is a String while id is an Integer
	 * 
	 * @param map
	 * @param line
	 */
	private void addInfoFileLine(HashMap<String,Integer> map, String line) {
		
		int sep = line.indexOf('#');
		String id = line.substring(2, sep);
		String occ = line.substring( sep+1);
		Integer occurrencies = Integer.valueOf(occ);
		
		map.put(id, occurrencies);
	}

	/**
	 * Returns the file where to store/load the info associated to a given test case
	 * 
	 * @param dir
	 * @param id
	 * @return
	 */
	private File getTestCaseFile( File dir, String id ){
		return new File(dir,id+".tc");
	}
	
	/**
	 * Returns the file where to store/load the info associated to a given program point
	 * @param dir
	 * @param id
	 * @return
	 */
	private File getProgramPointFile( File dir, String id ){
		return new File(dir,id+".pp");
	}
	

	public TestCaseInfo getTestCaseInfo(String testCaseName) {
		String id = null;
		TestCaseInfo tcInfo;
		
		if ( ! testCases.containsName(testCaseName) ){
			try {
				id = testCases.add( testCaseName );
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tcInfo = new TestCaseInfo(id, testCaseName , this);
			setModified(tcInfo);
			return tcInfo;
		}
		try {
			id = testCases.getId(testCaseName);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( tcModified.containsKey(id) )
			return (TestCaseInfo) tcModified.get(id);
		return loadTestCaseInfoFromFile( id, testCaseName, getTestCaseFile(dir, id) ); 
	}

	public TestCaseInfo getTestCaseInfoFromId( String id ){
		String tcName = null;
		
		try {
			tcName = testCases.getNameFromId(id);
		} catch (FileIndexException e) {
			return null;
		}
		
		if ( tcModified.containsKey(id) )
			return (TestCaseInfo) tcModified.get(id);
		return loadTestCaseInfoFromFile( id, tcName, getTestCaseFile(dir, id) );
	}
	
	public ProgramPointInfo getProgramPointInfo ( String programPointId ){
		if ( ppModified.containsKey(programPointId)){
			return (ProgramPointInfo) ppModified.get(programPointId);
		}
		
		File file = getProgramPointFile(dir, programPointId );
		return loadProgramPointInfo( programPointId, file );
		
	}
	
	public ProgramPointInfo createProgramPointInfo() {
		String id = null;
		try {
			id = programPoints.add(""+programPoints.getIds().size());
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProgramPointInfo pp = new ProgramPointInfo( this, id, new ArrayList() );
		ppModified.put( id, pp );
		//System.out.println("PP CREATED "+id);
		return pp;
	}
	
	private ProgramPointInfo loadProgramPointInfo(String programPointId, File file) {
		ProgramPointInfo pp = null;
		BufferedReader br = null;
		try {
		br = new BufferedReader( new InputStreamReader( new FileInputStream ( file )) );
		String line;
		
		
			line = br.readLine();
		
			ArrayList<String> vars = new ArrayList<String>();
			while ( line != null ){
				vars.add(line);
				line = br.readLine();
			}
			pp = new ProgramPointInfo( this, programPointId, vars );
			ppModified.put(programPointId, pp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if ( br != null ){
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return pp;
	}



	public VariableInfo getVariableInfo( String programPointName, String name, String value) {
		
		String varName = getVarName(programPointName,name,value);
		return getVariableInfo( varName );
	}
	
	public VariableInfo getVariableInfo( String varName ) {
		String id = null;
		VariableInfo varInfo;
		if ( ! variables.containsName(varName) ){
				try {
					id = variables.add(varName);
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				varInfo = new VariableInfo(this, id);
				setModified(varInfo);
				return varInfo;
		}
		try {
			id = variables.getId( varName );
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( varModified.containsKey(id) )
			return (VariableInfo) varModified.get(id);
		return new VariableInfo(this,id);
	}

	private String getVarName(String programPointName, String name, String value) {
		return programPointName+"#"+name+"#"+value;
	}



	public void saveTestCaseInfo(TestCaseInfo info) throws TcInfoHandlerException {
		File file = getTestCaseFile(dir, info.getId() );
		//System.out.println("SAVING TEST "+info.getId()+" "+info.getName()+" to "+file.getAbsolutePath());
		try {
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ) ));
			Iterator mIt = info.getMethodsIterator();
			while ( mIt.hasNext() ){
				String methId = (String) mIt.next();
				Integer occ = info.getMethodOccurrencies(methId);
				bw.write("M "+methId+"#"+occ+"\n");
			}
			Iterator pIt = info.getProgramPointsIdsIterator();
			while ( pIt.hasNext() ){
				String pId = (String) pIt.next();
				bw.write("P "+pId+"#"+"1"+"\n");
			}
			bw.close();
		} catch (FileNotFoundException e) {
			throw new TcInfoHandlerException("Cannot save");
		} catch (IOException e) {
			throw new TcInfoHandlerException("Cannot save");
			
		}
	}
	
	public void setModified( TcInfoEntity tcInfoElement ) {
		if ( tcInfoElement instanceof MethodInfo )
			setModified((MethodInfo)tcInfoElement);
		if ( tcInfoElement instanceof VariableInfo )
			setModified((VariableInfo)tcInfoElement);
		if ( tcInfoElement instanceof TestCaseInfo )
			setModified((TestCaseInfo)tcInfoElement);
	}
	
	public void setModified ( MethodInfo varInfo ){
		methModified.put( varInfo.getId(), varInfo );
	}
	
	public void setModified ( VariableInfo varInfo ){
		varModified.put( varInfo.getId(), varInfo );
	}
	
	public void setModified ( TestCaseInfo varInfo ){
		tcModified.put( varInfo.getId(), varInfo );
	}
	
	public void save() throws TcInfoHandlerException {
		String tcId = null;
		//System.out.println("SAVING");
		Iterator it = tcModified.keySet().iterator(); 
		while ( it.hasNext() ){
			tcId = (String) it.next();
			
			saveTestCaseInfo((TestCaseInfo) tcModified.get(new String(tcId)));
		}
		tcModified.clear();
		
		
		Iterator pit = ppModified.keySet().iterator();
		while ( pit.hasNext() ){
			String ppId = (String) pit.next();
			//System.out.println("SAVING PP "+ppId);
			saveProgramPointInfo( (ProgramPointInfo) ppModified.get(ppId));
		}
		ppModified.clear();
		
		try {
			variables.save( variablesFile );
			testCases.save( testCasesFile );
			methods.save( methodsFile );
			programPoints.save ( programPointsFile );
		} catch (IOException e) {
			throw new TcInfoHandlerException(e.getMessage());
		}
		
	}



	private void saveProgramPointInfo( ProgramPointInfo ppInfo ) {
		File file = getProgramPointFile(dir, ppInfo.getId() );
	
		try {
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ) ));
			Iterator it = ppInfo.getVariablesIdsIt();
			while ( it.hasNext() ){
					bw.write(it.next()+"\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}



	public String getMethodName(String id) {
		try {
			return methods.getNameFromId(id);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public Set<String> getMethodsIds() {
		return methods.getIds();
	}

	public Set<String> getTestCasesIds() {
		return testCases.getIds();
	}

	public Set<String> getVariableIds() {
		return variables.getIds();
	}

	public Set<String> getProgramPointIds() {
		return programPoints.getIds();
	}

	public VariableInfo getVariableFromId(String id) {
		String varName;
		try {
			varName = variables.getNameFromId(id);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return getVariableInfo(null);
		}
		return getVariableInfo(varName);
	}

	public String getVariableValue(String id) {
		String name;
		try {
			name = variables.getNameFromId(id);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return getVariableValueFromName(null);
		}
		return getVariableValueFromName(name);
	}

	private String getVariableValueFromName(String name) {
		
		// for example we have programPoint:::ENTER#parameter[0]#234
		int index = name.indexOf("#");
		
		//get parameter[0]#234
		String nameValue = name.substring(index+1);
		
		//return 234
		index = nameValue.indexOf("#");
		
		return nameValue.substring(index+1);
	}



	public String getVariableName(String id) {
		String name;
		try {
			name = variables.getNameFromId(id);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return getVariableNameFromName(null);
		}
		return getVariableNameFromName(name);
	}



	private String getVariableNameFromName(String name) {
		
//		 for example we have programPoint:::ENTER#parameter[0]#234
		int indexPre = name.indexOf("#");
		
		//get parameter[0]#234
		String nameValue = name.substring(indexPre+1);
		
		//return 234
		int indexPost = nameValue.indexOf("#");
		
		return name.substring(indexPre+1,1+indexPre+indexPost);
	}



	public String getVariableProgramPoint(String id) {
		String name;
		try {
			name = variables.getNameFromId(id);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return getVariableProgramPointFromName(null);
		}
		return getVariableProgramPointFromName(name);
	}



	private String getVariableProgramPointFromName(String name) {
//		 for example we have programPoint:::ENTER#parameter[0]#234
		int index = name.indexOf("#");
		
		
		return name.substring(0,index);
		

	}




}
