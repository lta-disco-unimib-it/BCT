/*
 * Created on 5-ott-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foo;

import foo.Component3.Component3;
import foo.component1.Component1;
import foo.component2.Component2;
import foo.data.ComplexData;
import foo.data.ExchangedValues;

/**
 * @author Leonardo Mariani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FooApplication {

	public static void main(String[] args) {
		Component1 c1 = new Component1();
		
		Component2 c2 = new Component2(c1);
		Component3 c3 = new Component3(c1);
		
		for(int i=0; i<8; i++) {
			c2.method21(2*i);
		}
		
		ExchangedValues ev = new ExchangedValues();
		ComplexData cd = new ComplexData();
		cd.setVal1(3);
		cd.setVal2("hi2");
		ev.setVal1(cd);
		ev.setVal2(3.2f);
		c2.method22(ev,"hi");
		
		c2.method23(10);
		
		
		c3.addValue(ev);
		cd.setVal1(2);
		cd.setVal2("hi3");
		ev.setVal1(cd);
		ev.setVal2(2.4f);
		c3.addValue(ev);
		cd.setVal1(8);
		cd.setVal2("hi8");
		ev.setVal1(cd);
		ev.setVal2(4.4f);
		c3.addValue(ev);
		cd.setVal1(12);
		cd.setVal2("8hi8");
		ev.setVal1(cd);
		ev.setVal2(6.6f);
		c3.addValue(ev);
		c3.processData();
	
	}
}
