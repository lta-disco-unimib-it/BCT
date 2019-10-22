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
package flattener.flatteners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import testSupport.flattener.First;
import testSupport.flattener.ObjectWithRef;
import testSupport.flattener.ThreeDepthObj;
import testSupport.flattener.VarValue;
import conf.EnvironmentalSetter;
import flattener.handlers.RawHandler;

/**
 * This class provides some basic tests for breadth object flattener.
 * 
 * To verify all functionalities of BreadthObjectFlattener run Flattener System test that comes with BCT.
 * 
 * @author Fabrizio Pastore fabrizio.pastore AT gmail.com
 *
 */
public class BreadthFlattenerTest extends TestCase {
	private static final String FLOAT_VALUE = "";// ".floatValue()";
	private static final String DOUBLE_VALUE = "";// ".doubleValue()";
	private static final String LONG_VALUE = "";// ".longValue()";
	private static final String SHORT_VALUE = "";// ".shortValue()";
	private static final String CHAR_VALUE = ""; //".charValue()";
	private static final String BYTE_VALUE = "";// ".byteValue()";
	private static final String BOOLEAN_GETTER = "";// ".booleanValue()";
	private BreadthObjectFlattener flattener = null;
	private static final String rootParameter = "parameter[0]";
	private static final String INT_GETTER = ""; //".intValue()"
	
	public BreadthFlattenerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		EnvironmentalSetter.setConfigurationValues();
		RawHandler handler = new RawHandler( rootParameter );
		flattener = new BreadthObjectFlattener( handler );
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		flattener = null;
		
	}

	public void testBreadthObjectFlattener() {
		
	}

	@Ignore("FIXME")
	public void testDoSmashObject() {
		First first = new First();
		
		try {
			flattener.doSmash( first );
			
			RawHandler rh = (RawHandler) flattener.getDataHandler().getData();
			
			
			ArrayList list = new ArrayList();
			
			list.add( new VarValue(".second.x"+INT_GETTER,"6") );
			list.add( new VarValue(".myInt"+INT_GETTER,"5") );
			
			assertTrue ( testContainExact ( rh, list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean testContainExact(RawHandler rawHandler, ArrayList list) {
		String root = rawHandler.getRootName();
		
		Iterator checkIt = list.iterator();
		Iterator it = rawHandler.getNodeNamesIt();
		
		while ( checkIt.hasNext() ){
			
			
			VarValue var = (VarValue) checkIt.next();
			String name = (String) root+var.getVarPath();
			if ( it.hasNext() )
				it.next();
			else 	
				return false;
			
			String value = rawHandler.getNodeValue( name );
			System.out.println(name+" "+value);
			if ( ! var.getValue().equals(value))
				return false;
		}
		
		if ( it.hasNext() )
			return false;
		return true;
		
	}
	
	private boolean testContainExact(Document document, ArrayList list) {
		Iterator it = list.iterator();
		
		
		while( it.hasNext() ){
			VarValue varValue = (VarValue)it.next();
			
			if ( ! testContain ( document,  varValue.getVarPath(), varValue.getValue() ) ){
				System.out.println("NON contiene"+varValue.getVarPath());
				return false;
			}
		}
		
		if ( document.getDocumentElement().getChildNodes().getLength() != list.size() )
			return false;
			
		return true;
	}

	private boolean testContain(Document document, String varPath, String value ) {
		Element root = document.getDocumentElement();
		
		NodeList children = root.getChildNodes();
		
		for ( int i = 0; i < children.getLength(); i++ ){
			Element element = (Element) children.item(i);
			
			if ( element.getAttribute("name").equals(varPath) && element.getAttribute("value").equals(value) )
					return true; 
		}
		return false;
	}


	private boolean testRootValue(Document document, String value ) {
		Element root = document.getDocumentElement();
		
		System.out.println( root.getAttribute("value") );
		
		if ( root.getAttribute("value").equals(value) )
			return true; 
		
		return false;
	}
	
	
	public void testDoSmashBooleanTrue() {
		try {
			flattener.doSmash(true);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "1" ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean testRootValue(RawHandler handler, String string) {
		
		if ( ! string.equals(handler.getNodeValue("parameter[0]")) )
			return false;
		return ( handler.getNodesNumber() == 1 ); 
		
	}

	public void testDoSmashBooleanFalse() {
		try {
			flattener.doSmash(false);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "0" ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void testDoSmashByte() {
		try {
			flattener.doSmash((byte) 1);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), new Byte((byte) 1).toString() ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testDoSmashChar() {
		
		try {
			flattener.doSmash('a');
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "\"a\"" ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void testDoSmashShort() {
		try {
			short value = 3;
			flattener.doSmash(value);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), ""+value ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void testDoSmashInt() {
		try {
			
			int value = 6;
			
			flattener.doSmash(value);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), ""+value ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashLong() {
		try {
			
			long value = 8;
			
			flattener.doSmash(value);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), ""+value ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashFloat() {
		try {
			
			float value = 9;
			
			flattener.doSmash(value);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), ""+value ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashDouble() {
	
		try {
			
			double value = 2.345678;
			
			flattener.doSmash(value);
						
			assertTrue( testRootValue ( (RawHandler) flattener.getDataHandler().getData(), ""+value ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void testDoSmashBigBooleanTrue() {
		try {
	
			flattener.doSmash(new Boolean(true));
			
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(BOOLEAN_GETTER,"1") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashBigBooleanFalse() {
		try {
			flattener.doSmash(new Boolean(false));
			
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(BOOLEAN_GETTER,"0") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void testDoSmashBigByte() {
		try {
			flattener.doSmash(new Byte((byte)1));
			
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(BYTE_VALUE,"1") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testDoSmashBigChar() {
		
		try {
			flattener.doSmash(new Character('a'));
						
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(CHAR_VALUE,"\"a\"") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashBigShort() {
		try {
			short value = 3;
			flattener.doSmash(new Short(value));
						
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(SHORT_VALUE,"3") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void testDoSmashBigInt() {
		try {
			
			int value = 6;
			
			flattener.doSmash(new Integer(value));
						
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(""+INT_GETTER,"6") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashBigLong() {
		try {
			
			long value = 8;
			
			flattener.doSmash(new Long(value));
						
			
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(LONG_VALUE,"8") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashBigFloat() {
		try {
			
			float value = (float) 9.3;
			
			flattener.doSmash( new Float(value) );
						
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(FLOAT_VALUE,"9.3") );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testDoSmashBigDouble() {
	
		try {
			
			double value = 2.345678;
			
			flattener.doSmash(new Double(value) );
						
			ArrayList list = new ArrayList();
			
			list.add(new VarValue(DOUBLE_VALUE,""+value) );
			
			assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	
	public void testIsPrimitiveArray() {
		assertTrue( flattener.isPrimitiveArray(new int[2]) );
		assertTrue( flattener.isPrimitiveArray(new long[2]) );
		assertTrue( flattener.isPrimitiveArray(new short[2]) );
		assertTrue( flattener.isPrimitiveArray(new byte[2]) );
		assertTrue( flattener.isPrimitiveArray(new double[2]) );
		assertTrue( flattener.isPrimitiveArray(new float[2]) );
		assertTrue( flattener.isPrimitiveArray(new char[2]) );
		
		

		assertFalse( flattener.isPrimitiveArray(new Long[2]) );
		assertFalse( flattener.isPrimitiveArray(new Short[2]) );
		assertFalse( flattener.isPrimitiveArray(new Byte[2]) );
		assertFalse( flattener.isPrimitiveArray(new Double[2]) );
		assertFalse( flattener.isPrimitiveArray(new Float[2]) );
		assertFalse( flattener.isPrimitiveArray(new Character[2]) );
		
		assertFalse( flattener.isPrimitiveArray(new Object[2]) );
		
	}

	public void testIsIgnoredType() {
		
	}

	public void testMaxDepth() {
		ThreeDepthObj obj = new ThreeDepthObj();
		
		ArrayList list = new ArrayList();
		
		list.add(new VarValue(".x"+INT_GETTER,""+obj.getX()) );
		list.add(new VarValue(".firstLevel.value"+INT_GETTER,""+obj.getFirstLevel().getValue()) );
		list.add(new VarValue(".firstLevel.secondLevel.value"+INT_GETTER,""+obj.getFirstLevel().getSecondLevel().getValue()) );
		
		flattener.setMaxDepth(3);
		flattener.doSmash(obj);
		
		assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
		flattener.setDataHandler(new RawHandler(rootParameter));
		
		flattener.setMaxDepth(2);
		
		flattener.doSmash(obj);
		
		list.remove(2);
		
		assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
	}

	public void testSetDataHandlerBreadthHandler() {

		try {
			flattener.doSmash(true);


			assertTrue(testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "1" ));

			flattener.setDataHandler(new RawHandler(rootParameter) );

			//data handler must be void
			assertFalse(testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "0" ));
			
			flattener.doSmash(false);

			assertTrue(testRootValue ( (RawHandler) flattener.getDataHandler().getData(), "0" ));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void testReferences() {
		ObjectWithRef or = new ObjectWithRef();
		flattener.setSkipAlreadyVisited(true);
		flattener.doSmash( or );
		
		ArrayList list = new ArrayList();
		
		list.add(new VarValue(".first.myInt"+INT_GETTER,""+or.getFirst().getMyInt()) );
		list.add(new VarValue(".first.second.x"+INT_GETTER,""+or.getFirst().getSecond().getX() ));
		list.add(new VarValue(".firstRef","@parameter[0].first" ));
		
		assertTrue( testContainExact( (RawHandler) flattener.getDataHandler().getData(), list ) );
		
	}
}
