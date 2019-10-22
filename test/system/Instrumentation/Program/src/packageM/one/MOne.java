package packageM.one;

import packageM.M;
import packageM.one.two.MOneTwo;
import packageM.one.two.three.MOneTwoThree;
import packageN.one.NOne;

public class MOne {

	public int m(){
		MOneTwo mot = new MOneTwo();
		mot.s();
		M m = new M();
		m.s();
		MOneTwoThree mott = new MOneTwoThree();
		mott.m(new MOne());
		return t();
	}

	public void s() {
		
		
	}

	public int t() {
		return 5;
	}
}
