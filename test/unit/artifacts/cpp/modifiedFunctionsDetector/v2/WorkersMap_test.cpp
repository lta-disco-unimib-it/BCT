/*
 * WorkersMap_test.c
 *
 *  Created on: Nov 25, 2011
 *      Author: usiusi
 */

#include  <string>
#include <iostream>

#include "TestException.h"
#include "WorkersMap.h"

using namespace std;

int allTestCases;
int passingTestCases;
int failingTestCases;

void assertEquals ( long v1, long v2  ){
	if ( v1 != v2 ){
		cout << "found " << v2 << " expected " << v1 << endl;
		throw new TestException();
	}
}

void testAddWorker_one(){

	WorkersMap map;

	string worker1("PSTFRZ83D6YETZD");

	map.addWorker( worker1, 50000 );

	assertEquals ( 50000, map.getSalary(worker1) );


}


void testAddWorker_two(){

	WorkersMap map;

	string worker1("PSTFRZ83D6YETZD");

	map.addWorker( worker1, 50000 );


	string worker2("PBTFRZ83D6YETZD");

	map.addWorker( worker2, 10000 );

	assertEquals ( 50000, map.getSalary(worker1) );
	assertEquals ( 10000, map.getSalary(worker2) );


}

void testGetAverageSalary_nobody(){

	WorkersMap map;

	string worker1("PSTFRZ83D6YETZD");
	string worker2("PBGFRZ83D6YETZD");
	string worker3("GATFRZ83D6YETZD");

	list<string> workers;
	workers.push_back(worker1);
	workers.push_back(worker2);
	workers.push_back(worker3);

	assertEquals ( -1, map.getAverageSalary(workers) );


}

void testGetAverageSalary_many(){

	WorkersMap map;

	string worker1("PSTFRZ83D6YETZD");
	map.addWorker( worker1, 50000 );

	string worker2("PBTFRZ83D6YETZD");
	map.addWorker( worker2, 10000 );

	string worker3("JKTFRZ83D6YETZD");
	map.addWorker( worker3, 15000 );
	
	string worker4("RBTFRZ83D6YETZD");
	map.addWorker( worker4, 19000 );
	
	string worker5("ABTFRZ83D6YETZD");
	map.addWorker( worker5, 11000 );

	string worker6("CRLLNN83D6YETZD");
	map.addWorker( worker6, 90000 );

	string worker7("ABTFTZ83D6YETZD");
	map.addWorker( worker7, 13000 );

	list<string> workers;
	workers.push_back(worker1);
	workers.push_back(worker2);
	workers.push_back(worker3);
	workers.push_back(worker4);
	workers.push_back(worker5);
	workers.push_back(worker7);

	assertEquals ( (50000+10000+15000+19000+11000+13000)/6, map.getAverageSalary(workers) );


}

void runTest( string testCaseName, void (*testCase)() ){
	allTestCases++;
	try {
		testCase();
		passingTestCases++;
		cout << "Test passed: " << testCaseName << endl;
	} catch ( ... ) {
		failingTestCases++;
		cout << "Test failed: " << testCaseName << endl;
	}
}

int main(){
	cout << "Running test cases for WorkersMap class" << endl;

	runTest ( "testAddWorker_one", &testAddWorker_one );
	runTest ( "testAddWorker_two", testAddWorker_two );
	runTest ( "testGetAverageSalary_nobody", testGetAverageSalary_nobody );
	runTest ( "testGetAverageSalary_many", testGetAverageSalary_many );

	if ( allTestCases == passingTestCases ){
		cout << "All test cases were successfully executed" << endl;
	} else {
		cout << "Test terminated with failures" << endl;
	}

	cout << "test cases executed: " << allTestCases << endl;

	cout << "test cases passed: " << passingTestCases << endl;

	cout << "test cases failed: " << failingTestCases << endl;

	return 0;
}




