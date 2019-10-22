#!/bin/bash

version0=$1
version1=$2

FILES_REGEX=".*cpp|.*c|*cc"

resultFile="bct.modifiedFunctions.txt"

#filesV0=`find $version0 -regextype posix-egrep -iregex $FILES_REGEX -type f`
filesV0=`find $version0 -iregex $FILES_REGEX -type f`

#filesV1=`find $version1 -regextype posix-egrep -iregex $FILES_REGEX -type f`
filesV1=`find $version1 -iregex $FILES_REGEX -type f`

len=${#version0}
for file in $filesV0
do
	name=${file:len}
	names0="$names0\n$name"
done

len=${#version1}
for file in $filesV1
do
	name=${file:len}
	names1="$names1\n$name"
done

names="$names0\n$names1"
echo -e $names
namesToProcess=`echo -e $names | sort | uniq`

echo $namesToProcess

for name in $namesToProcess
do
	file0=$version0/$name
	file1=$version1/$name
	if [ ! -e $file0 ]
	then
		continue
	fi
	if [ ! -e $file1 ]
	then
		continue
	fi
	
	diff -W 300 -p $file0 $file1 | grep -G "\*\*\*\*\*\*\*" | awk '{print $3}' >> $resultFile
done


