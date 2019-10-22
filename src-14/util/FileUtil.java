package util;

import java.io.File;

/**
 * This class contains several utilities to manipulate files.
 * 
 * @author Fabrizio Pastore
 *
 */
public class FileUtil {
	
	/**
	 * Delete the contents of a directory leaving the directory on disk.
	 * 
	 * @param dir
	 * @return true if all the contents of dir were succesfully deleted
	 */
	static public boolean deleteDirectoryContents(File dir) {
		boolean res = true;
		File[] files = dir.listFiles();
		for ( int i = 0; i<files.length; i++  ){
			res &= deleteRecursively(files[i]);
		}
		return res;
	}
	
	/**
	 * Recursively delete a the passed file and all its contents if it is a directory
	 * 
	 * @param fileToDelete
	 * @return true if both fileToDelete and its contents were succesfully deleted 
	 */
	static public boolean deleteRecursively(File fileToDelete) {
		boolean res = true;
		if( fileToDelete.isDirectory() ) {
			File[] files = fileToDelete.listFiles();
			for(int i=0; i<files.length; i++) {
				res &= deleteRecursively(files[i]);
			}
		}
		return( res & fileToDelete.delete() );
	}

	/**
	 * Returns the extension of a file.
	 * Returns null if the file has no extension
	 * 
	 * @param interactionModelFile
	 * @return
	 */
	public static String getExtension(File interactionModelFile) {
		String name = interactionModelFile.getName();
		int dotPosition = name.lastIndexOf('.');
		if ( dotPosition < 0 ){
			return null;	
		}
		
		
		return name.substring(dotPosition+1);
		
	}
}
