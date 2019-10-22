/*
 * Created on 5-ott-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foo.Component3;

import java.util.Vector;

import foo.component1.Interface1;
import foo.data.ExchangedValues;

/**
 * @author Leonardo Mariani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Component3 implements Interface3{
	Interface1 usedComponent;
	Vector dataSet = new Vector(); 

	public Component3(Interface1 usedComponent) {
		this.usedComponent = usedComponent;
	}
	
	public void addValue(ExchangedValues ev) {
		dataSet.add(ev);
	}

	public float processData() {
		return usedComponent.method11(dataSet);
	}

}
