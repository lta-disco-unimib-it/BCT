 /********************************************************************** 
 * Copyright (c) 2005, 2006 IBM Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html         
 * $Id: ProbeUnitTests.cpp,v 1.1.2.1 2006-12-02 12:41:42 pastore Exp $ 
 * 
 * Contributors: 
 * IBM - Initial API and implementation 
 **********************************************************************/ 

// ProbeUnitTests.cpp : Defines the entry point for the console application.
//

#include "CommonDef.h"
#include "BCIEngProbe.h"
#ifdef _WINDOWS_
#  include "stdafx.h"
#endif
#include <stdio.h>

// Test cases for the wildcard matcher
// These tests should hit every "return" statement in the wildcard matcher,
// and both forks of every "if" and the inside and skip-over case of of every "while."

struct testcase {
	const char* pattern;
	const char* candidate;
	bool expected;
} testcases[] = {
	{ "*", "anything", true }, // pattern is just star
	{ "anything", "*", true }, // candidate is just star - special case
	{ "java*", "java.io.File", true },	// prefix match, trailing star
	{ "java*", "some.java.thing", false },	// initial substring no match
	{ "java.thing", "java.thing", true }, // exact match
	{ "java.thing", "java.thing.other", false }, // more than a match
	{ "java.*.thing", "java..thing", true }, // zero chars for inner star
	{ "java.*.thing", "java.xx.thing", true }, // nonzero chars for inner star
	{ "java.*.xx.*.yy", "java.aa.xx.aa.xx.yy", true }, // two stars
	{ "*java*", "abcde" }, // leading, trailing, but no match
	{ "*tail", "something with a tail", true }, // matching tails
	{ "*tail", "tail", true }, // zero chars, then matching tails
	{ "*middle*", "beginning middle end", true }, // matching middles
	{ "some*more", "somexxmorexx", false }, // more than a match, with wildcard
	{ "java.*.more", "java.123", false }, // not enough to match
	{ "*pat*more", "xyz pat", false }, // consume all of candidate before end of pattern
	{ "abc*longtail*", "abcshort", false }, // candidate tail isn't long enough for pattern tail
	{ "abc*target*", "abctarget", true }, // target substring exactly equals remainder of candidate 
	{ "abc*target*", "abc-and-target", true }, // target appears at end of candidate
	{ "abc*target*", "abc-and-target-and-more", true }, // target appears, not at end of candidate
	{ NULL, NULL, false }
};

bool
WildcardStringMatchTest()
{
	bool result = true;

	for (int i = 0; testcases[i].pattern != NULL; i++) {
		const char* pattern = testcases[i].pattern;
		const char* candidate = testcases[i].candidate;
		bool expected = testcases[i].expected;
		if (CFilterRule::WildcardStringMatch(pattern, candidate) != expected) {
			fprintf(stderr, "Test failed! Pat=\"%s\" Candidate=\"%s\" expected %s\n",
				pattern, candidate, (expected ? "true" : "false"));
			result = false;
		}
	}
	return result;
}

int main(int argc, char* argv[])
{
	if (argc == 2 && strcmp(argv[1], "-wildcardtest") == 0) {
		if (WildcardStringMatchTest()) {
			fprintf(stdout, "WildcardStringMatch test succeeded.\n");
			return 0;
		}
		else {
			fprintf(stderr, "WildcardStringMatch test failed!\n");
			fprintf(stderr, "One or more tests didn't give expected result.\n");
			return 1;
		}
	}
	else {
		fprintf(stderr, "Possible options:\n");
		fprintf(stderr, "  -wildcardtest\n");
		fprintf(stderr, "    Test the wildcard matcher in CFilterRule.\n");
		return 1;
	}
}

