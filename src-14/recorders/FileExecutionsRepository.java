package recorders;

import java.io.File;
import java.util.Set;

import util.FileIndexAppend;
import util.FileIndex.FileIndexException;

public class FileExecutionsRepository implements ExecutionsRepository {
	private FileIndexAppend fileIndex;
	
	
	public FileExecutionsRepository (File storageDir){
		File indexFile = new File( storageDir, "executionTraces.idx" );
		this.fileIndex = new FileIndexAppend(indexFile, "");
	}
	
	public String newExecution(String string) throws FileIndexException {
		return fileIndex.add(string);
	}
	
	public Set getExecutionsIds() throws FileIndexException {
		return fileIndex.getIds();
	}

}
