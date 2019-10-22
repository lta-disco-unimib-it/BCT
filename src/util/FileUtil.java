/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
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
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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

		if ( ! dir.exists() ){
			return false;
		}

		for ( File file : dir.listFiles() ){
			res &= deleteRecursively(file);
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

	public static ArrayList<File> getContentsRecursively(File expectedFiles, FileFilter filter) {

		ArrayList<File> expected = new ArrayList<File>();

		for ( File f : expectedFiles.listFiles() ){

			if ( f.isDirectory() ){
				expected.addAll( getContentsRecursively(f, filter) );	
			} else if ( filter.accept(f) ) {
				expected.add(f);
			}

		}


		return expected;
	}

	public static ArrayList<File> getDirContents(File expectedFiles) {
		ArrayList<File> expected = new ArrayList<File>();
		for ( File f : expectedFiles.listFiles() ){
			expected.add(f);
		}

		return expected;
	}

	public static boolean checkContentEqual(File expectedFile, File observedFile) throws IOException {
		FileInputStream expected = null;

		FileInputStream observed = null;


		try {
			expected = new FileInputStream(expectedFile);

			observed = new FileInputStream(observedFile);	

			return StreamUtil.contentEquals(expected, observed);

		} finally {
			if ( expected != null ){
				expected.close();
			}

			if ( observed != null ){
				observed.close();
			}
		}
	}

	public static List<String> getLines(File file) throws FileNotFoundException {
		return getLines(file, false);
	}

	public static List<String> getLines(File file, boolean removeWhiteSpaces) throws FileNotFoundException {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader( file ) );

		try {
			String nextLine;
			while ( ( nextLine = br.readLine() ) != null ){
				if ( removeWhiteSpaces ){
					nextLine = nextLine.replaceAll("\\s", "");
				}

				lines.add( nextLine );
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//		Scanner scanner = null;
		//		
		//		try {
		//			scanner = new Scanner(file);
		//			while ( scanner.hasNextLine() ){
		//				String nextLine = scanner.nextLine();
		//
		//				if ( removeWhiteSpaces ){
		//					nextLine = nextLine.replaceAll("\\s", "");
		//				}
		//
		//				lines.add( nextLine ); 
		//			}
		//		} finally {
		//			if ( scanner != null ){
		//				scanner.close();
		//			}
		//		}
		return lines;
	}

	public static List<String> getLines(BufferedReader r) throws IOException {
		List<String> lines = new ArrayList<String>();
		String line;
		while ( ( line = r.readLine() ) != null ){
			lines.add(line);
		}
		return lines;
	}

	public static void writeToTextFile(String outputBuffer,
			File dumpOutput) throws IOException {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter(dumpOutput) );

			writer.write(outputBuffer);


		} finally {
			if ( writer != null ){
				writer.close();
			}
		}
	}

	public static String getCleanPath(String path){
		String separator = File.separator;
		if ( ! path.contains(separator) ){//fix for passing models to windows
			separator="/";
		}
		return getCleanPath(path, separator);
	}

	public static String getCleanPath(String path, String separator){
		LinkedList<String> pathList = new LinkedList<String>();

		boolean endsWithSeparator = false;

		if ( path.endsWith(separator ) ){
			endsWithSeparator = true;
		}

		StringTokenizer st = new StringTokenizer(path,separator,false);
		while ( st.hasMoreTokens() ){
			String token = st.nextToken();

			if ( token.equals(".") ){

			} else if ( token.equals("..") ){
				if ( pathList.size()>0){
					pathList.removeLast();
				} else { //we can just leave '..' at the beginning o fthe path
					pathList.add(token);
				}
			} else {
				pathList.add(token);
			}
		}

		StringBuffer result = new StringBuffer();
		for ( String token : pathList ){
			result.append(token);
			result.append(separator);
		}

		if ( ! endsWithSeparator ){
			if ( result.length() > 0 ){
				result.deleteCharAt(result.length()-1);
			}
		}

		return result.toString();
	}

	public static void copyDirectory(File sourceLocation , File targetLocation) {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {
			copyFile(sourceLocation, targetLocation);
		}
	}

	public static void copyFile(File sourceLocation, File targetLocation) {
		try {
			InputStream in = new FileInputStream(sourceLocation);
			try {
				OutputStream out = new FileOutputStream(targetLocation);

				try {
					// Copy the bits from instream to outstream
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				} catch ( IOException e ){
					System.err.println("Error while working on files: ");
					System.err.println(sourceLocation.getAbsolutePath());
					System.err.println(targetLocation.getAbsolutePath());
				} finally {
					out.close();
				}
			} catch (IOException e ){
				e.printStackTrace();
			} finally {
				in.close();
			}
		} catch (IOException e ){
			System.err.println("Error opening file "+sourceLocation.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public static void writeToTextFile(List<String> lines, File dest) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
		try {
			for (String line : lines ){

				bw.write(line);

				bw.newLine();
			}
		} finally { 
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static File find(File sourceFolder, String fileName) {
		LinkedList<File> filesToProcess = new LinkedList<File>();

		filesToProcess.addLast(sourceFolder);

		while ( ! filesToProcess.isEmpty() ){
			File file = filesToProcess.removeFirst();
			if ( file.getName().equals(fileName) ){
				return file;
			}

			if ( file.isDirectory() ){
				for ( File child : file.listFiles()){
					filesToProcess.addLast(child);
				}
			}
		}

		return null;

	}

	/**
	 * Get the relative path from one file to another, specifying the directory separator. 
	 * If one of the provided resources does not exist, it is assumed to be a file unless it ends with '/' or
	 * '\'.
	 * 
	 * @param targetPath targetPath is calculated to this file
	 * @param basePath basePath is calculated from this file
	 * @param pathSeparator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
	 * @return
	 * @throws IOException 
	 */
	public static String getRelativePath(File baseFile, File targetFile, String pathSeparator) throws IOException {


		String normalizedTargetPath = targetFile.getCanonicalPath();
		String normalizedBasePath = baseFile.getCanonicalPath();



		String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
		String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

		// First get all the common elements. Store them as a string,
		// and also count how many of them there are.
		StringBuffer common = new StringBuffer();

		int commonIndex = 0;
		while (commonIndex < target.length && commonIndex < base.length
				&& target[commonIndex].equals(base[commonIndex])) {
			common.append(target[commonIndex] + pathSeparator);
			commonIndex++;
		}

		if (commonIndex == 0) {
			// No single common path element. This most
			// likely indicates differing drive letters, like C: and D:.
			// These paths cannot be relativized.
			throw new IOException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath
					+ "'");
		}   



		StringBuffer relative = new StringBuffer();

		if (base.length != commonIndex) {
			int numDirsUp = base.length - commonIndex;

			for (int i = 0; i < numDirsUp; i++) {
				relative.append(".." + pathSeparator);
			}
		}
		relative.append(normalizedTargetPath.substring(common.length()));
		return relative.toString();
	}

	public static String getRelativePath(File baseFile, File targetFile ) throws IOException {
		return getRelativePath(baseFile, targetFile, File.separator);
	}

	public static void writeToFile(InputStream in, File file) throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream( file );

		byte[] buffer = new byte[1024];
		int len;
		try {
			len = in.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public static String getContent(File file) throws FileNotFoundException {
		StringBuffer sb = new StringBuffer();
		for ( String line : getLines(file, false) ){
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static BufferedReader createBufferedReaderForTrace(File file) throws IOException {
			
			if ( file.getName().endsWith(".gz") ){
				InputStream fileStream = new FileInputStream(file);
				InputStream gzipStream;
				try {
					gzipStream = new GZIPInputStream(fileStream);
					Reader decoder = new InputStreamReader(gzipStream);
					return new BufferedReader(decoder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			BufferedReader br;
			br = new BufferedReader(new FileReader(file));
			return br;
		}


}
