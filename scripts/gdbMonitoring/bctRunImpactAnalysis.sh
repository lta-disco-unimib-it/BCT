#!/bin/bash

callgraphFile=$1

modifiedMethods=$2

python $BCT_C_HOME/impactAnalysis/src/change_analyzer.py $callgraphFile $modifiedMethods
