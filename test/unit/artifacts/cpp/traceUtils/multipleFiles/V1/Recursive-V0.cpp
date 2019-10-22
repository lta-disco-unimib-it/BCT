//============================================================================
// Name        : Recursive-V0.cpp
// Author      : FP
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <string>

#include "Recurser.h"

using namespace std;

int main() {
	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!


	Recurser r;
	ValueWrapper wr(5);
	r.recurse(5, &wr );

	return 0;
}
