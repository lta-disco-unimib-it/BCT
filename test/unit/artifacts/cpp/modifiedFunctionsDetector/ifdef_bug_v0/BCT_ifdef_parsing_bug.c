/*
 ============================================================================
 Name        : BCT_ifdef_parsing_bug_v0.c
 Author      : FP
 Version     :
 Copyright   : Your copyright notice
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>

int myFunc(){
	int x = 9;

	x+1;
	x=x+2;
	x=4;

	x+=3;
	return x;
}

int main(void) {

	int x = myFunc( );

	puts("!!!Hello World!!!"); /* prints !!!Hello World!!! */
	return EXIT_SUCCESS;
}
