/*
 * Recurser.cpp
 *
 *  Created on: Jan 25, 2012
 *      Author: fabrizio
 */

#include "Recurser.h"

Recurser::Recurser() {
	// TODO Auto-generated constructor stub

}

Recurser::~Recurser() {
	// TODO Auto-generated destructor stub
}


int Recurser::recurse(int x,  ValueWrapper* r ) {
	cout << "Recursion with" << x << endl;
	sleep(1);
	///ADD
	///ADD
	///ADD
	if ( x == 0 ){
		return r->getValue();
	}
	return recurse(x-1, NULL )+1;
}

