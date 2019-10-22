/*
 * WorkersMap.h
 *
 *  Created on: Nov 25, 2011
 *      Author: usiusi
 */

#ifndef WORKERSMAP_H_
#define WORKERSMAP_H_

#include <string>
#include <map>
#include <list>

using namespace std;

class WorkersMap {

	map<std::string, long> workers;
    std::_Rb_tree_iterator<std::pair<const std::basic_string<char,std::char_traits<char> ,std::allocator<char> > ,long int> > result;

public:
	WorkersMap();
	virtual ~WorkersMap();

	/**
	 * Returns the salary of a person.
	 * Returns NULL if the person is not a worker.
	 *
	 */
	long getSalary( string personId );

	/**
	 * Returns true if the person is a worker
	 *
	 */
	bool isWorker( string personId );

	/**
	 * Returns the average salary of the list of person passed as input.
	 * It considers only workers.
	 *
	 * Returns -1 if nobody is a worker.
	 *
	 */
	long getAverageSalary( list<string> personIds );

	/**
	 * Add a worker with the given salary
	 */
	void addWorker( string personId, long annualSalary );
};

#endif /* WORKERSMAP_H_ */
