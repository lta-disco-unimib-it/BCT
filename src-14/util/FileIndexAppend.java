package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.FileIndex.FileIndexException;

public class FileIndexAppend extends FileIndex {
	private File indexFile;
	
	public File getIndexFile() {
		return indexFile;
	}
	public FileIndexAppend(File file) {
		this(file,"");
	}
	/**
	 * @see FileIndex
	 */
	public FileIndexAppend(File file,String suffix) {
		super(file,suffix);
		this.indexFile = file;
	}

	public synchronized String add(String name) throws FileIndexException {
		String res = super.add(name);
		try {
			Writer w = new FileWriter(indexFile,true);
			w.write(name+"="+res+"\n");
			w.close();
		} catch (IOException e) {
			throw new FileIndexException(e);
		}
		return res;
	}
	
	public void deleteAll() {
		String parent = indexFile.getParent();
		Set ids = getIds();
		Iterator it = ids.iterator();
		while ( it.hasNext() ){
			FileUtil.deleteRecursively( new File(parent,(String) it.next()) );
		}
		
		FileUtil.deleteRecursively(indexFile);
		
		clear();
	}

	
}
