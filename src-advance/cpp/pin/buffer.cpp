/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <stddef.h>

#include "pin.H"
#include "portability.H"
using namespace std;

/*
 * Name of the output file
 */
KNOB<string> KnobOutputFld(KNOB_MODE_WRITEONCE, "pintool","o", ".", "specify out folder"); 

/*
 * The ID of the buffer
 */
//BUFFER_ID bufId;

/*
 * Thread specific data
 */
TLS_KEY mlog_key;

/*
 * Number of OS pages for the buffer
 */
#define NUM_BUF_PAGES 1024


/*
 * Record of memory references.  Rather than having two separate
 * buffers for reads and writes, we just use one struct that includes a
 * flag for type.
 */
struct MEMREF
{
	std::stringstream *ea;
};


/*
 * MLOG - thread specific data that is not handled by the buffering API.
 */
class MLOG
{
  public:
    MLOG(THREADID tid);
    ~MLOG();
    
    VOID DumpBufferToFile();
    VOID Add(std::stringstream*);

  private:
    ofstream _ofile;
    MEMREF *data;
    UINT64 dataSize;
};



MLOG::MLOG(THREADID tid)
{
    data = (MEMREF*) malloc ( sizeof ( MEMREF ) * NUM_BUF_PAGES);
    dataSize = 0;
    string filename = KnobOutputFld.Value() + "/bdciTrace." + decstr(time(NULL)) + "." + decstr(getpid_portable()) + "." + decstr(tid) + ".pin";
    
    _ofile.open(filename.c_str());
    

    if ( ! _ofile )
    {
        cerr << "Error: could not open output file." << endl;
        exit(1);
    }
    _ofile << hex;
    _ofile.setf(ios::showbase);
}


MLOG::~MLOG()
{
    DumpBufferToFile();
    _ofile.close();
    free ( data );
}


VOID MLOG::DumpBufferToFile()
{ 

    for(UINT64 i=0; i<dataSize; i++)
    {
            _ofile << data[i].ea->str();
	    delete data[i].ea;
    }
    dataSize=0;
}


VOID MLOG::Add(std::stringstream *sb){

	if ( (dataSize+1) == NUM_BUF_PAGES ){
		DumpBufferToFile();	
	}

	data[dataSize].ea=sb;

	dataSize++;

}


/**************************************************************************
 *
 *  Callback Routines
 *
 **************************************************************************/



/*
 * Note that opening a file in a callback is only supported on Linux systems.
 * See buffer-win.cpp for how to work around this issue on Windows.
 */
VOID ThreadStart(THREADID tid, CONTEXT *ctxt, INT32 flags, VOID *v)
{
    // There is a new MLOG for every thread.  Opens the output file.
    MLOG * mlog = new MLOG(tid);
    
    // A thread will need to look up its MLOG, so save pointer in TLS
    PIN_SetThreadData(mlog_key, mlog, tid);


}


VOID ThreadFini(THREADID tid, const CONTEXT *ctxt, INT32 code, VOID *v)
{
    MLOG * mlog = static_cast<MLOG*>(PIN_GetThreadData(mlog_key, tid));

    delete mlog;

    PIN_SetThreadData(mlog_key, 0, tid);
}


