/*
 * ValueWrapper.h
 *
 *  Created on: Apr 4, 2012
 *      Author: fabrizio
 */

#ifndef VALUEWRAPPER_H_
#define VALUEWRAPPER_H_

namespace myUtil {

class ValueWrapper {
public:
	ValueWrapper(int x);

	virtual ~ValueWrapper();

	int getValue(){ return value; };

private:
	int value;
};

} /* namespace myUtil */
#endif /* VALUEWRAPPER_H_ */
