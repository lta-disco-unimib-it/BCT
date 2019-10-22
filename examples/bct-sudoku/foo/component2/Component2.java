/*
 * Created on 29-set-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foo.component2;

import foo.component1.Interface1;
import foo.data.ComplexData;
import foo.data.ExchangedValues;

/**
 * @author Leonardo Mariani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Component2 implements Interface2{
	Interface1 usedComponent;
	
	public Component2(Interface1 usedComponent) {
		this.usedComponent = usedComponent;
	}
	
	public void method21(int value) {
			usedComponent.method12(value,value*value,"hello");
	}

	public int method22(ExchangedValues v1, String v2) {
		int res = usedComponent.method13(v1.getVal1());
		if (res>0) { 
			return res;
		} else {
			System.out.println(v2);
			return -1;
		}
	}

	public int method23(int count) {
		ComplexData standardComplexData = new ComplexData();
		standardComplexData.setVal1(1);
		standardComplexData.setVal2("myString");
		int sum =0;
		
		for(int i=0; i<count; i++) {
			sum += usedComponent.method13(standardComplexData);
		}
		
		return sum;
	}

}
