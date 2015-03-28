package tyRuBa.util;

import java.io.File;
import java.io.IOException;

public class Files {
	
	/**
	 * Delete a directory and all its subdirectories and files.
	 * 
	 * @param dir
	 */
	public static void deleteDirectory(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				File[] children = dir.listFiles();
				for (int i = 0; i < children.length; i++) {
					deleteDirectory(children[i]);
				}
			}  
			dir.delete();
		}
	}
	
	/**
	 * Create a directory if it doesn't already exist.
	 * 
	 * @param dir
	 * @return Returns true if the directory was created, false if it already existed.
	 * @throws IOException If a non directory file exists with the same name
	 */
	public static boolean createDirectory(File dir) throws IOException {
		//TODO: is this method obsolete? Can we use File.mkdirs instead?
		if (dir.exists()) {
			if (!dir.isDirectory())
				throw new IOException("Could not create directory because a file with same name exists: "+dir);
			else
				return false;
		}
		else {
			dir.mkdir();
			return true;
		}
	}
	
}
