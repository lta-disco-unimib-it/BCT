#*******************************************************************************
#    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
#   
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#*******************************************************************************

if (( $#<3 ))
then
	echo "$0 probeName <startingNumber> <N>"
	echo "This script takes a probe template and creates N probe class replica numbered from startingNumber to startingNumber + N -1"
	exit
fi
probe=$1
from=$2
to=$3
i=$from
while ((i<$3))
do
	probename=$probe$i
	probeTemplate=$probe".TEMPLATE"
	echo $probename
	sed s/PROBENUM/$i/g $probeTemplate >> $probe$i".java"
	i=$((i+1))
done

