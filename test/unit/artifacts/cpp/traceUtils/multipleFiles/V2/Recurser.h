/*
 * Recurser.h
 *
 *  Created on: Jan 25, 2012
 *      Author: fabrizio
 */

#ifndef RECURSER_H_
#define RECURSER_H_

#include <iostream>
#include <string>
#include "ValueWrapper.h"
using namespace std;

using myUtil::ValueWrapper;

class Recurser {
public:
	Recurser();
	virtual ~Recurser();

	int recurse(int x,  ValueWrapper* r);
};

#endif /* RECURSER_H_ */
