package packageB;

import packageI.Interface;

public class B implements Interface {

	public void interfaceMethod() {
		
	}

	public void mb() {
		interfaceMethod();
		InternalUse u = new InternalUse();
		u.doSome();
	}


}
