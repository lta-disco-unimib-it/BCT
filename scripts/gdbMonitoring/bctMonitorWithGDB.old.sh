tmp=gdbConfigFile.tmp
callsToMonitor=callsToMonitor.tmp
rm -f $tmp

echo delete >> $tmp

echo "set pagination off" >> $tmp

programToExec=$1

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
  	if ( first == true ) { 
		print "ENTER " curFunc " " enterPos; 
	}
	first=false; 
	len=length($1);
	pos=substr($1,0,len);
	print "EXIT " curFunc " " pos;
  }
}'  >> $callsToMonitor;


objdump -r -d $programToExec | awk '
{ 
  len=length($2);
  pos=substr($2,len,1);
  if ( pos == ":" ) {
  	curFunc=substr($2,0,len);
	enterPos=$1;
  }
  if ( $3 == "ret" ) { 
	print "# code for ENTER " curFunc " " $1; 
	len=length($1);
	pos=$1;
	print "b *0x" enterPos; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-ENTER: " curFunc "\\n"; 
	print "bt 1"; 
	print "c"; 
	print "end"; 
	print ""; 
	print "# code for EXIT " curFunc; 
	len=length($1);
	pos=substr($1,0,len);
	print "b *0x" pos; 
	print "commands"; 
	print "silent"; 
	print "echo !!!BCT-EXIT: " curFunc "\\n"; 
	print "bt 1"; 
	print "c"; 
	print "end"; 
	print ""; 
  }
}'  >> $tmp;




echo "set editing off" >> $tmp;
echo "set confirm off" >> $tmp;
echo "set verbose off" >> $tmp;
echo "set logging redirect" >> $tmp
echo "set logging file gdb.txt" >> $tmp
echo "set logging on" >> $tmp
echo run >> $tmp;
echo quit >> $tmp;


gdb -silent -n -x $tmp $@

