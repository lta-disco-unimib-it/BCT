package packageD;

import packageI.Interface;

public class D implements Interface {

	public void interfaceMethod() {
		
	}

	public void mb() {
		Interface i = this;
		i.interfaceMethod();
		InternalUse u = new InternalUse();
		u.doSome();
	}

}
