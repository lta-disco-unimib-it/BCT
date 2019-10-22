#!/bin/bash

callsToMonitor=$1

programToExec=$2

if [ ! -e $programToExec ]
then
	echo "Program does not exist"
	exit 1
fi

objdump -r -d $programToExec | awk '
{ 
  len=length($2);
  pos=substr($2,len,1);
  if ( pos == ":" ) {
  	curFunc=substr($2,0,len);
	enterPos=$1;
	first=true;
  }
  if ( $3 == "ret" ) {
  	len=length(curFunc);
  	functionName=substr(curFunc,2,len-2);
  	if ( first == true ) { 
    		command="c++filt \"" functionName "\""
		command | getline demangled
		close (command)
		print "ENTER " curFunc " " enterPos " :;:;: " demangled; 
	}
	first=false; 
	len=length($1);
	pos=substr($1,0,len);
  	if ( index ( pos, ":" ) > 0 ) {
		len=length(pos);
		pos=substr(pos,1,len-1);
  	}
    	command2="c++filt \"" functionName "\""
	command2 | getline demangled
	close (command2)
	print "EXIT " curFunc " " pos " :;:;: " demangled;
  }
}'  > $callsToMonitor;
