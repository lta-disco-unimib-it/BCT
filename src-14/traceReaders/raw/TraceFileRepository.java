package traceReaders.raw;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

public abstract class TraceFileRepository implements TraceRepository{
	private FileIndexAppend fileIndex;
	private FileIndexAppend metaIndex;
	private File traceIndexFile; 
	private File metaIndexFile;
	private File storageDir;
	
	private static final String traceIndexFileName = "traces.idx";
	private static final String metaIndexFileName = "meta.idx";
	
	public TraceFileRepository( File storageDir ){
		this.storageDir = storageDir;
		traceIndexFile = new File( storageDir, traceIndexFileName );
		metaIndexFile = new File( storageDir, metaIndexFileName );	
		fileIndex = new FileIndexAppend( traceIndexFile, ".dtrace" );
		metaIndex = new FileIndexAppend( metaIndexFile, ".meta" );
	}
	
	public Object getRawTrace( String methodName ) throws FileReaderException{
		
		
		String file;
		String meta;
		
		if ( ! fileIndex.containsName(methodName) ){
			try {
				file = fileIndex.add(methodName);
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		}
		
		if ( ! metaIndex.containsName(methodName) ){
			try {
				meta = metaIndex.add(methodName);
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		}
		
		try {
			file = fileIndex.getId(methodName);
			meta = metaIndex.getId(methodName);
		} catch (FileIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new FileReaderException("Cannot get file for "+methodName);
		}
		
		return getTraceFile( file, methodName,  getFile ( file ) , getFile ( meta ));
	}
	

	public Object getRawTraceFromId(String id) throws FileReaderException {
		String methodName;
		try {
			methodName = fileIndex.getNameFromId(id);
		} catch (FileIndexException e) {
			throw new FileReaderException("Unconsistent index file: "+e.getMessage());
		}
		
		if ( metaIndex.containsName(methodName)){
			try {
				return  getTraceFile(id,methodName, getFile ( id ) , getFile ( metaIndex.getId(methodName)) );
			} catch (FileIndexException e) {
				throw new FileReaderException(e);
			}
		} else {
			return getTraceFile( id, methodName, getFile ( id ) );
		}
	}
	
	
	public List getRawTraces() throws FileReaderException {
		Set ids = fileIndex.getIds();
		ArrayList traces = new ArrayList();

		Iterator it = ids.iterator();
		while ( it.hasNext() ){
			String id = (String) it.next();
			String methodName;
			try {
				methodName = fileIndex.getNameFromId(id);
			} catch (FileIndexException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				throw new FileReaderException("Unconsistent index file");
			}
			if ( metaIndex.containsName(methodName)){
				try {
					traces.add(  getTraceFile(id,methodName, getFile ( id ) , getFile ( metaIndex.getId(methodName)) ) );
				} catch (FileIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				traces.add( getTraceFile( id, methodName, getFile ( id ) ) );
			}
					
		}
		
		
		
		return traces;
	}

	private File getFile(String id) {
		return new File( storageDir, id );
	}
	


	protected abstract Object getTraceFile(String id, String elementName, File dataId, File metaId);
	
	protected abstract Object getTraceFile(String id, String elementName, File dataId );
	
}
