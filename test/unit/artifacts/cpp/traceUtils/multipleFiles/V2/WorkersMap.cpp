/*
 * WorkersMap.cpp
 *
 *  Created on: Nov 25, 2011
 *      Author: usiusi
 */

#include "WorkersMap.h"

WorkersMap::WorkersMap() {
	// TODO Auto-generated constructor stub

}

WorkersMap::~WorkersMap() {
	// TODO Auto-generated destructor stub
}


long WorkersMap::getHisSalary( string workerId ){


	if ( ! isWorker( workerId ) ){
		return -1; //fixed return NULL, it means 0 while we want to distinguish between a worker with no salary and a guy that does not work
	}

	return workers.find( workerId )->second;

}


void WorkersMap::addWorker( string personId, long annualSalary ){
	workers.insert( pair<string,long>(personId, annualSalary) );
}

bool WorkersMap::isWorker( string personId ){
	if ( workers.count( personId ) == 0 ){
		return false;
	}
	return true;
}

long WorkersMap::getAverageSalary( list<string> personIds ){
	list<string>::iterator i;

	long totalSalary = 0;
	int workers = 0;
	for(i=personIds.begin(); i != personIds.end(); ++i){
		if ( getHisSalary( *i ) == 0 ){ //if salary is NULL the person does not work
			continue;
		}
		totalSalary += getHisSalary( *i );
		workers++;
	}

	if ( workers  == 0 ){
		return -1;
	}

	return totalSalary/workers;
}


