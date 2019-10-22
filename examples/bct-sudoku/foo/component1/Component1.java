/*
 * Created on 29-set-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foo.component1;

import java.util.Iterator;
import java.util.Vector;

import foo.data.ComplexData;
import foo.data.ExchangedValues;

/**
 * @author Leonardo Mariani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Component1 implements Interface1{

	public float method11(Vector evset) {
		float sum = 0.0f;
		
		Iterator it = evset.iterator();
		while(it.hasNext()) {
			ExchangedValues ev = (ExchangedValues)it.next();
			sum += ev.getVal2() + ev.getVal1().getVal1();
		}
		return sum;
	}

	public ComplexData method12(int v1, int v2, String s1) {
		ComplexData returnValue = new ComplexData();
		
		returnValue.setVal1(v1+v2);
		returnValue.setVal2(s1);
		return returnValue;
	}

	public int method13(ComplexData cd) {
		return cd.getVal1()+cd.getVal2().length();
	}

}
