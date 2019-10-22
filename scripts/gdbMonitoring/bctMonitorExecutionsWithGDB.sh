#!/bin/bash

#This script monitors a program execution with gdb and records method 
#invocations and the values of the associated parameters
#
#The script uses the following input files:
#
#	bct.modifiedFunctions.txt	list of functions (or classes) modified
#					in the new version of the software
#
#	bct.relevantFunctions.txt	list of functions (or classes) that are
#					particularly relevant for developers and
#					thus should be monitored even if not
#					modified	
#
#

###############################################################################
#
#				OPTIONS
#
###############################################################################

#Path of the valgrind executable
VALGRIND=/usr/bin/valgrind


#
#monitoringMode option indicates how to identify the methods to monitor
#
#Possible values are:
#IMPACT: 	Use relevant methods plus methods impacted by software changes
#ALL:		Monitor all the software methods

#monitoringMode=IMPACT
monitoringMode=ALL


#impactDone indicates whether or not to perform impact analysis or reuse data
#already generated in a previous run
impactDone=0
#impactDone=1

#
#impactAnalysis option indicates how to perform impact analysis
#
#Possible values are:
#VALGRIND:	Use the callgraph generated by valgrind/callgrind to identify the
#		methods to monitor
#GDB		Use gdb to determine the callers of the modified methods
#

impactAnalysis=VALGRIND
#impactAnalysis=GDB


#
#skipExecution option indicate whether or not to perform the monitoring
#

skipExecution=0
#skipExecution=1

#BCT_BIN_ALL=/Users/usiusi/Workspaces/workspaceBCTNew/BCT/bin/:/Users/usiusi/Workspaces/workspaceBCTNew/BCT/dist/bct-standalone.jar
BCT_BIN_ALL=$HOME/Workspaces/workspaceBCT/BCT/bin/:$HOME/Workspaces/workspaceBCT/BCT/dist/bct-standalone.jar

###############################################################################
###############################################################################
###############################################################################



#File with the names of the functions modified in the new version of the 
#software
#Never changes this name, is encoded in an AWK script
modifiedFunctions=bct.modifiedFunctions.txt

#File with the names of the functions that are relevant for developers
relevantFunctions=bct.relevantFunctions.txt

stage=""
if [ $BCT_C_MONITORING_STAGE ]
then
	stage=$BCT_C_MONITORING_STAGE
fi

resultFile="bct.gdb.monitoring."$stage".txt"
objdumpFile="bct.objDump"

#GDB configuration file used when GDB is run for impact analysis
tmp=gdbConfigFileImpactAnalysis.tmp

#GDB config file used when GDB is run for the final monitoring
tmpMonitor=gdbConfigFile.tmp

#File that holds the list of all the functions and return points in the program
callsToMonitor=callsToMonitor.tmp

#File that holds the list of the functions and return points in the program
#that have been modified
#We use this list for impact analysis
callsToMonitorImpact=callsToMonitor.impact.tmp

#This file contains the same functions in callsToMonitorImpact but in a format
#that complies with the format used in the valgrind callgraph
callsToMonitorModified=callsToMonitor.impact.valgrind.tmp

#File that holds the names of the functions that will be monitored
functionsToMonitor=bct.functionsToMonitor.txt

#File that holds the list of the functions and return points that will be 
#monitored in the final run
callsToMonitorFinal=callsToMonitor.final.tmp

callsToMonitorParents=callsToMonitor.parents.tmp
callsToMonitorChildren=callsToMonitor.children.tmp

GDB_MATCHING="-F"


rm $functionsToMonitor

rm gdb.impact.txt
rm gdb.monitor.txt



programToExec=$1

programToMonitorWithArgs=$@

#readelf -s $programToExec | awk '
#{ 
#  if($4 == "FUNC" && $2 != 0 ) { 
#	print "# code for " $NF; 
#	print "b *0x" $2; 
#	print "commands"; 
#	print "silent"; 
#	print "echo !!!BCT-CALL: " $NF "\\n"; 
#	print "bt 1"; 
#	print "c"; 
#	print "end"; 
#	print ""; 
#   }	 
#}' >> $tmp; 

function identifyCallPoints() {

objdump -r -d $programToExec | awk '
{ 
  len=length($2);
  pos=substr($2,len,1);
  if ( pos == ":" ) {
  	curFunc=substr($2,0,len);
	enterPos=$1;
	first=true;
	#print "ENTER " curFunc " " enterPos; 
  }
  if ( $3 == "ret" ) {
  	if ( first == true ) { 
		print "ENTER " curFunc " " enterPos; 
	}
	first=false; 
	len=length($1);
	pos=substr($1,0,len);
  	if ( index ( pos, ":" ) > 0 ) {
		len=length(pos);
		pos=substr(pos,1,len-1);
  	}
	print "EXIT " curFunc " " pos;
  }
}'  > $callsToMonitor;
}


function identifyModifiedFunctions() {


cat $callsToMonitor | awk '
{ 

  type=$1
  
  address=$3

  len=length($2);
  functionName=substr($2,2,len-2);
  
  command="c++filt \"" functionName "\"| grep -F -f bct.modifiedFunctions.txt | wc -l";
  
  command | getline result; 
  close (command);
  if ( result != 0 ){
    	command2="c++filt \"" functionName "\""
	command2 | getline demangled
	close (command2)
  	print type " " functionName " " address " " demangled; 
  }
  
}' > $callsToMonitorImpact;

}

function runImpactAnalysisValgrind() {

	identifyModifiedFunctions

	rm callgrind.out*

	$VALGRIND --tool=callgrind $programToMonitorWithArgs


	python ${BCT_C_HOME}/impactAnalysis/src/gprof2dot.py -f callgrind callgrind.out* > callgraph.dot

	cat $callsToMonitorImpact | awk '{ res=$4; i=5; while ( i <= NF ){ res=res" "$i; i++ }; print res; }' > $callsToMonitorModified


	bash ${BCT_C_HOME}/bctRunImpactAnalysis.sh callgraph.dot $callsToMonitorModified 

}

function runImpactAnalysisValgrindLazy() {
	
	echo "Valgrind lazy impact analysis"

	rm callgrind.out*

	$VALGRIND --tool=callgrind $programToMonitorWithArgs


	python ${BCT_C_HOME}/impactAnalysis/src/gprof2dot.py -f callgrind callgrind.out* > callgraph.dot


	bash ${BCT_C_HOME}/bctRunImpactAnalysisLazy.sh callgraph.dot $modifiedFunctions 

}




function runImpactAnalysisGDB() {
identifyModifiedFunctions

rm -f $tmp
echo delete >> $tmp
cat $callsToMonitorImpact | awk '  
{
  curFunc=$2;
  addr=$3;
  if ( $1 == "ENTER" ) { 
	print "# code for ENTER " curFunc " " addr; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-ENTER: " curFunc "\\n"; 
	print "bt"; 
	print "c"; 
	print "end"; 
	print ""; 
  } else {
	print "# code for EXIT " curFunc; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-EXIT: " curFunc "\\n"; 
	print "bt"; 
	print "c"; 
	print "end"; 
	print ""; 
  }
}'  >> $tmp;

echo "set pagination off" >> $tmp
echo "set editing off" >> $tmp;
echo "set confirm off" >> $tmp;
echo "set verbose off" >> $tmp;
echo "set logging redirect" >> $tmp
echo "set logging file gdb.impact.txt" >> $tmp
echo "set logging on" >> $tmp
echo run >> $tmp;
echo quit >> $tmp;

gdb -silent -n -x $tmp --args $programToMonitorWithArgs

cat gdb.impact.txt | grep " in " | awk '{print $4}' | sort | uniq >> $functionsToMonitor

}

function identifyCallsToMonitorNew() {

 

#
# 
#  
  
cat $callsToMonitor | awk '
{ 

  type=$1
  
  address=$3

  len=length($2);
  functionName=substr($2,2,len-2);
  
  command="c++filt \"" functionName "\"| grep -F -f bct.impact.parents.txt | wc -l";
  
  command | getline result; 
  close (command);
  if ( result != 0 ){
    	command2="c++filt \"" functionName "\""
	command2 | getline demangled
	close (command2)
  	print type " " functionName " " address " " demangled; 
  }
  
}' > $callsToMonitorParents;  

cat $callsToMonitor | awk '
{ 

  type=$1
  
  address=$3

  len=length($2);
  functionName=substr($2,2,len-2);
  
  command="c++filt \"" functionName "\"| grep -F -f bct.impact.children.txt | wc -l";
  
  command | getline result; 
  close (command);
  if ( result != 0 ){
    	command2="c++filt \"" functionName "\""
	command2 | getline demangled
	close (command2)
  	print type " " functionName " " address " " demangled; 
  }
  
}' > $callsToMonitorChildren;  

}





function identifyCallsToMonitorAll() {

 
echo "Identify all the functions as elements to monitor"
  
cat $callsToMonitor | awk '
{ 

  type=$1
  
  address=$3

  len=length($2);
  functionName=substr($2,2,len-2);
  
  print type " " functionName " " address; 
  
}' > $callsToMonitorFinal;  

}


function identifyCallsToMonitor() {

 

#
# 
#  
  
cat $callsToMonitor | awk '
{ 

  type=$1
  
  address=$3

  len=length($2);
  functionName=substr($2,2,len-2);
  
  command="c++filt \"" functionName "\"| grep -F -f bct.functionsToMonitor.txt | wc -l";
  
  command | getline result; 
  close (command);
  if ( result != 0 ){
  	print type " " functionName " " address; 
  }
  
}' > $callsToMonitorFinal;  

}

function setupMonitoringNew(){

rm -f $tmpMonitor

echo "delete" >> $tmpMonitor
echo "set pagination off" >> $tmpMonitor;

cat $callsToMonitorChildren | awk '  
{
  curFunc=$2;
  addr=$3;
  res=$4; i=5; while ( i <= NF ){ res=res" "$i; i++ };
  
  if ( $1 == "ENTER" ) { 
	print "# code for ENTER " res " " addr; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-ENTER: " res "\\n"; 
	print "bt 1"; 
	print "c"; 
	print "end"; 
	print ""; 
  } else {
	print "# code for EXIT " res; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-EXIT: " res "\\n"; 
	print "bt 1"; 
	print "info all-registers";
	print "c"; 
	print "end"; 
	print ""; 
  }
}'  >> $tmpMonitor;


breakPoints=`grep "# code for" $tmpMonitor | wc -l`

echo "BREAKPOINTS: $breakPoints"

i=0;while [ $i -le $breakPoints ];do echo "disable $i" >> $tmpMonitor; i=$(($i+1));done

cat $callsToMonitorParents | awk -v bp=$breakPoints '  
{
  curFunc=$2;
  addr=$3;
  res=$4; i=5; while ( i <= NF ){ res=res" "$i; i++ };
  if ( $1 == "ENTER" ) { 
	print "# code for parent ENTER " res " " addr; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-ENTER: " res "\\n"; 
	print "bt 1"; 
	for ( i=1; i<=bp ; i++ ){
		print "enable "i;
	}
	print "c"; 
	print "end"; 
	print ""; 
  } else {
	print "# code for parent EXIT " res; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-EXIT: " res "\\n"; 
	print "bt 1"; 
	print "info all-registers";
	for ( i=1; i<=bp ; i++ ){
		print "disable "i;
	}
	print "c"; 
	print "end"; 
	print ""; 
  }
}'  >> $tmpMonitor;


echo "set editing off" >> $tmpMonitor;
echo "set confirm off" >> $tmpMonitor;
echo "set verbose off" >> $tmpMonitor;
echo "set logging redirect" >> $tmpMonitor
echo "set logging file gdb.monitor.txt" >> $tmpMonitor
echo "set logging on" >> $tmpMonitor
echo run >> $tmpMonitor;
echo quit >> $tmpMonitor;
}

function setupMonitoring(){

rm -f $tmpMonitor

echo "delete" >> $tmpMonitor
echo "set pagination off" >> $tmpMonitor;

cat $callsToMonitorFinal | awk '  
{
  curFunc=$2;
  addr=$3;
  res=$4; i=5; while ( i <= NF ){ res=res" "$i; i++ };
  if ( $1 == "ENTER" ) { 
	print "# code for ENTER " res " " addr; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-ENTER: " res "\\n"; 
	print "bt 1"; 
	print "c"; 
	print "end"; 
	print ""; 
  } else {
	print "# code for EXIT " res; 
	print "b *0x" addr; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-EXIT: " res "\\n"; 
	print "bt 1"; 
	print "info all-registers";
	print "c"; 
	print "end"; 
	print ""; 
  }
}'  >> $tmpMonitor;




echo "set editing off" >> $tmpMonitor;
echo "set confirm off" >> $tmpMonitor;
echo "set verbose off" >> $tmpMonitor;
echo "set logging redirect" >> $tmpMonitor
echo "set logging file gdb.monitor.txt" >> $tmpMonitor
echo "set logging on" >> $tmpMonitor
echo run >> $tmpMonitor;
echo quit >> $tmpMonitor;
}

echo "Identifying call points"
identifyCallPoints

if [ $monitoringMode == "IMPACT" ]
then

	if [ $impactDone -eq 0 ]
	then
		if [ $impactAnalysis == "GDB" ]
		then
			echo "Running impact analysis with GDB"
			runImpactAnalysisGDB
		elif [ $impactAnalysis == "VALGRIND" ]
		then
			echo "Running impact analysis with Valgrind"
			runImpactAnalysisValgrindLazy
			#runImpactAnalysisValgrind
		else
			echo "Not running any impact analysis"
		fi
	else
		echo "Impact analysis already done in previous execution"	
	fi
	#Add the functions considered to be relevant
	if [ -e $relevantFunctions ] 
	then 
		cat $relevantFunctions >> $functionsToMonitor
	fi
	
	if [ $impactAnalysis == "GDB" ]
	then
		echo "Identifying calls to monitor"
		identifyCallsToMonitor

		echo "Setting up monitoring"
		setupMonitoring

	else
		echo "Identifying calls to monitor"
		identifyCallsToMonitorNew

		echo "Setting up monitoring"
		setupMonitoringNew
	fi

else
	#Clean up, we do not use it
	echo -e "\n" > $functionsToMonitor
	
	echo "Identifying calls to monitor"

	objdump -r -d $programToExec >> $objdumpFile

	java -cp $BCT_BIN_ALL cpp.gdb.ObjdumpExtractCallPoints $objdumpFile $callsToMonitorFinal
	
	echo "Setting up monitoring"
	java -cp $BCT_BIN_ALL cpp.gdb.GdbMonitorAllConfigCreator $callsToMonitorFinal $tmpMonitor

	#setupMonitoring
fi


if [ $skipExecution -eq 0 ]
then
	gdb -silent -n -x $tmpMonitor --args $programToMonitorWithArgs
	mv gdb.monitor.txt $resultFile
fi
