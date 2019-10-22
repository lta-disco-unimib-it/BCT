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
package tools.fsa2xml.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testSupport.TestArtifactsManager;
import testSupport.fsa.FSATestSupport;
import testSupport.fsa.FsaComparatorOracle;
import automata.fsa.FiniteStateAutomaton;

@Ignore("Multpile issues on states need to double check if the fault is in the test or in the code")
public class FSABctXmlTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSave0() throws FileNotFoundException, IOException, ClassNotFoundException {
		FiniteStateAutomaton fsa = FSATestSupport.createSingleStateFSA();
		
		
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test0.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" final=\"true\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("</fsa:FSA>", line);
		
		
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	/**
	 * This test verify the saving of an automata
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSave1() throws FileNotFoundException, IOException, ClassNotFoundException {
		FiniteStateAutomaton fsa = FSATestSupport.createSimpleAutomaton();
		
		
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test1.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q1\" final=\"true\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<transitions description=\"A\" to=\"//@states.1\" from=\"//@states.0\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("</fsa:FSA>", line);
		
		
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	
	@Test
	public void testSave2() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		FiniteStateAutomaton fsa = FSATestSupport.createAutomaton3S2T();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test2.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		checkXmlHeader(line);
		
		
		line = reader.readLine();
		assertNotNull(line);
		
		
		checkFSAElement(line);
		int initial = getInitialState(line);
		
		
		ArrayList<String> states = new ArrayList<String>();
		HashMap<Integer,String> stateNames = new HashMap<Integer, String>();
		
		int stateC = 0;
		line = reader.readLine();
		assertNotNull(line);
		states.add(line);
		String name = getStateName(line);
		stateNames.put(stateC++,name);
		
		line = reader.readLine();
		assertNotNull(line);
		states.add(line);
		name = getStateName(line);
		stateNames.put(stateC++,name);
		
		line = reader.readLine();
		assertNotNull(line);
		states.add(line);
		name = getStateName(line);
		stateNames.put(stateC++,name);
		
		
		
		assertEquals("Too much states written down",3,states.size());
		assertTrue( states.contains("<states name=\"q2\" final=\"true\" fsa=\"/\"/>") );
		assertTrue( states.contains("<states name=\"q1\" fsa=\"/\"/>") );
		assertTrue( states.contains("<states name=\"q0\" fsa=\"/\"/>") );
		
		HashMap<String,Integer[]> transitions = new HashMap<String, Integer[]>();
		
		line = reader.readLine();
		assertNotNull(line);
		
		Integer to = Integer.valueOf(getTo(line));
		Integer from = Integer.valueOf(getFrom(line));
		String desc = getDescription(line);
		transitions.put(desc, new Integer[]{from,to});
		
		line = reader.readLine();
		assertNotNull(line);
		
		to = getTo(line);
		from = getFrom(line);
		desc = getDescription(line);
		transitions.put(desc, new Integer[]{from,to});
		
		checkTransition(stateNames, transitions, "A", "q0", "q1");
		checkTransition(stateNames, transitions, "B", "q1", "q2");
		
		checkInitialState(stateNames, "q0", initial );
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("</fsa:FSA>", line);
		
		
		
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}

	/**
	 * Check if the XML header is correct
	 * 
	 * @param line
	 */
	private void checkXmlHeader(String line) {
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
	}

	/**
	 * Check if the initial state in the FSA wrote in the file correspond to the expected one
	 * 
	 * @param stateNames the map with the states loaded from the xml fsa file
	 * @param expectedStateName expected name of te initial state 
	 * @param actualStateID ID of the initial state as reported in the initialState attribute of the fsa
	 */
	private void checkInitialState(HashMap<Integer, String> stateNames,
			String expectedStateName, int actualStateID) {
		if ( ! stateNames.containsKey(actualStateID) ){
			fail("The FSA does not contain all the states, the state is not found "+actualStateID);
		}
		assertEquals(expectedStateName, stateNames.get(actualStateID));
		
	}

	/**
	 * Check if exists a transition with the given characteristics.
	 * Needs a Map with all the states and a map with all the transitions (the transition description must be unique.
	 * 
	 * @param stateNames contains all the state names loaded from the xml file. Maps the numeric id of the state with its name.
	 * @param transitions contains all the transitions of the file: map the transition description with an array of two integers, the from and to state ids.
	 * @param description the description of the transition we check
	 * @param fromStateNameExpected the name of the state from which the transition starts
	 * @param toStateNameExpected the name of the state to which the transition arrives
	 */
	private void checkTransition(HashMap<Integer, String> stateNames, HashMap<String, Integer[]> transitions,
			String description, String fromStateNameExpected, String toStateNameExpected) {
		Integer[] trans = transitions.get(description);
		Integer from = trans[0];
		Integer to = trans[1];
		assertEquals("Checking from for transition "+description,fromStateNameExpected, stateNames.get(from));
		assertEquals("Checking to for transition "+description,toStateNameExpected, stateNames.get(to));
	}

	/**
	 * Given a line that represent a transition element return the description of the transition
	 * @param line
	 * @return
	 */
	private String getDescription(String line) {
		return getElementValue("description", line);
	}

	/**
	 * Given a line that represent an FSA element return the numeric ID of the initial state
	 * @param line
	 * @return
	 */
	private int getInitialState(String line) {
		return Integer.valueOf(getElementValue("initialState", line).substring(10));
	}

	/**
	 * Given a line that represent a transition return the numeric id of the arrival state
	 * @param line
	 * @return
	 */
	private int getTo(String line) {
		return Integer.valueOf(getElementValue("to", line).substring(10));
	}
	
	/**
	 * Given a line that represent a transition return the numeric id of the start state
	 * @param line
	 * @return
	 */
	private int getFrom(String line) {
		return Integer.valueOf(getElementValue("from", line).substring(10));
		
	}
	
	/**
	 * Given a line that represent a state returns the name of the state
	 * @param line
	 * @return
	 */
	private String getStateName(String line) {
		return getElementValue("name", line);
	}
	

	
	private String getElementValue(String elmentName, String line) {
		String[] els = line.split(" ");
		
		String key = elmentName+"=";
		
		for ( String el : els ){
			if ( el.startsWith(key) ){
				return el.split("\"")[1];
			}
		}
		return null;
	}

	private void checkFSAElement(String line) {
		// TODO Auto-generated method stub
		String[] elements = line.split(" ");
		assertEquals("<fsa:FSA",elements[0]);
		assertEquals("xmi:version=\"2.0\"", elements[1]);
		assertEquals("xmlns:xmi=\"http://www.omg.org/XMI\"", elements[2]);
		assertEquals("xmlns:fsa=\"fsa\"", elements[3]);
		assertTrue(elements[4].matches("initialState=\"//@states.[0-9]+\">"));
		
	}

	@Test
	public void testSave3() throws FileNotFoundException, IOException, ClassNotFoundException {
		fail("Rewrite the method");
		
		FiniteStateAutomaton fsa = FSATestSupport.createAutomaton4S3T();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test3.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q1\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q2\" fsa=\"/\"/>", line);
		  
		line = reader.readLine();
	    assertNotNull(line);
        assertEquals("<states name=\"q3\" final=\"true\" fsa=\"/\"/>", line);  		
		  
		
        HashMap<Integer,String> transition = new HashMap<Integer,String>();
		line = reader.readLine();
		int i = 0;
		
		while(!line.equals("</fsa:FSA>")){
			transition.put(i,line);
			i++;
			line = reader.readLine();
		}
		
		
		
		assertTrue(transition.containsValue("<transitions description=\"A\" to=\"//@states.1\" from=\"//@states.0\"/>"));
		
        
		assertTrue(transition.containsValue("<transitions description=\"C\" to=\"//@states.3\" from=\"//@states.2\"/>"));
		
		assertTrue(transition.containsValue("<transitions description=\"B\" to=\"//@states.2\" from=\"//@states.1\"/>"));
		
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	
	@Test
	public void testSave4() throws FileNotFoundException, IOException, ClassNotFoundException {
		fail("Rewrite the method");
		
		FiniteStateAutomaton fsa = FSATestSupport.createAutomaton5S4T();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test4.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q1\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q2\" fsa=\"/\"/>", line);
		  
		line = reader.readLine();
	    assertNotNull(line);
        assertEquals("<states name=\"q3\" final=\"true\" fsa=\"/\"/>", line);  		
       
        line = reader.readLine();
	    assertNotNull(line);
        assertEquals("<states name=\"q4\" final=\"true\" fsa=\"/\"/>", line);   
		 
        
        HashMap transition = new HashMap();
		line = reader.readLine();
		int i = 0;
		
		while(!line.equals("</fsa:FSA>")){
		transition.put(i,line);
		i++;
		line = reader.readLine();
		}
		
		
		
		assertTrue(transition.containsValue("<transitions description=\"B\" to=\"//@states.2\" from=\"//@states.1\"/>"));
		
		assertTrue(transition.containsValue("<transitions description=\"D\" to=\"//@states.4\" from=\"//@states.1\"/>"));
		
		assertTrue(transition.containsValue("<transitions description=\"A\" to=\"//@states.1\" from=\"//@states.0\"/>"));
		
		assertTrue(transition.containsValue("<transitions description=\"C\" to=\"//@states.3\" from=\"//@states.2\"/>"));
		
	
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	
	@Test
	public void testSave5() throws FileNotFoundException, IOException, ClassNotFoundException {
		fail("Rewrite the method");
		FiniteStateAutomaton fsa = FSATestSupport.createAutomaton1S1T();
		
		//FiniteStateAutomaton fsa = FSATestSupport.createAutomaton2();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test5.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" final=\"true\" fsa=\"/\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<transitions description=\"A\" to=\"//@states.0\" from=\"//@states.0\"/>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("</fsa:FSA>", line);
		
		
		//last line
		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	@Test
	public void testSave6() throws FileNotFoundException, IOException, ClassNotFoundException {
		fail("Rewrite the method");
		FiniteStateAutomaton fsa = FSATestSupport.createAutomaton1S2T();
		
	
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test6.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		//first line
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<fsa:FSA xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:fsa=\"fsa\" initialState=\"//@states.0\">", line);
		
		line = reader.readLine();
		assertNotNull(line);
		assertEquals("<states name=\"q0\" final=\"true\" fsa=\"/\"/>", line);
		
		HashMap transition = new HashMap();
		line = reader.readLine();
		int i = 0;
		
		while(!line.equals("</fsa:FSA>")){
		transition.put(i,line);
		i++;
		line = reader.readLine();
		}
		
		
		assertTrue(transition.containsValue("<transitions description=\"A\" to=\"//@states.0\" from=\"//@states.0\"/>"));
		
	
		assertTrue(transition.containsValue("<transitions description=\"B\" to=\"//@states.0\" from=\"//@states.0\"/>"));
	

		line = reader.readLine();
		//file must end here
		assertNull(line);
		
		reader.close();
	}
	@Test
	public void testSave7() throws FileNotFoundException, IOException, ClassNotFoundException {
		fail("This test is not implemented");
		FiniteStateAutomaton fsa = FSATestSupport.createAutomatonComplete();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test7.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		line = reader.readLine();
		assertNotNull(line);
		
		reader.close();
	}
	@Test
	public void testSave8() throws FileNotFoundException, IOException, ClassNotFoundException {
		FiniteStateAutomaton fsa = FSATestSupport.createBigAutomatonComplete();
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test8.fsa");
		codec.saveFSA(fsa, dest);
		
		
		BufferedReader reader = new BufferedReader(new FileReader(dest));
		String line;
		
		line = reader.readLine();
		assertNotNull(line);
		
		reader.close();
		
		
		
	}
	
	
	
	@Test
	public void testLoad0() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		FSABctXml codec = new FSABctXml();
		
		File dest = TestArtifactsManager.getNewUnitTestFile("tools/fsa2xml/codec/impl/test0.fsa");
		FiniteStateAutomaton  fsaToCompare = FSATestSupport.createSingleStateFSA();
		codec.saveFSA(fsaToCompare, dest);
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);
		
	}
	
	
	
	@Test
	public void testLoad1() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test1.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton  fsaToCompare = FSATestSupport.createSimpleAutomaton();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	@Test
	public void testLoad2() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test2.fsa");
		
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomaton3S2T();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	@Test
	public void testLoad3() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test3.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomaton4S3T();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	
	@Test
	public void testLoad4() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test4.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomaton5S4T();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	
	@Test
	public void testLoad5() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test5.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomaton1S1T();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	@Test
	public void testLoad6() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test6.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomaton1S2T();
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);
		
	}
	@Test
	public void testLoad7() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test7.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createAutomatonComplete();
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);
		
	}
	
	@Test
	public void testLoad8() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test8.fsa");
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createBigAutomatonComplete();
		
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}

	@Test
	public void testLoad9() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		
		
		
		FSABctXml codec = new FSABctXml();
		//FIXME: first get stub, then save then load
		File dest = TestArtifactsManager.getUnitTestFile("tools/fsa2xml/codec/impl/test9.fsa");
		
		FiniteStateAutomaton fsaToCompare = FSATestSupport.createLambdaAutomaton();
		codec.saveFSA(fsaToCompare, dest);
		FiniteStateAutomaton fsa = codec.loadFSA(dest);
		System.out.println(fsa.getTransitions()[0].getDescription());
		FsaComparatorOracle.assertFsaEquals(fsaToCompare,fsa);

		
	}
	
}
