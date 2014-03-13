package org.mapsforge.applications.android.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



import android.os.Environment;


public class DirectoryUtils {

	public static String getBaseDirectoryPath () {
		return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public static String getMapDirectoryPath () {
		String aMapDirPath = getBaseDirectoryPath () + "/" + Samples.DEFAULT_MAP_DATA_DIRECTORY;
		return aMapDirPath;
	}
	
	public static String getRenderThemeDirectoryPath () {
		String aRenderthemeDirPath = getBaseDirectoryPath () + "/" + Samples.DEFAULT_RENDER_DATA_DIRECTORY;
		return aRenderthemeDirPath;
	}
	
	public static String getSeamarkSymbolsDirectoryPath () {
		String aDirPath = getBaseDirectoryPath () + "/" + Samples.DEFAULT_SYMBOLS_DATA_DIRECTORY;
		return aDirPath;
	}

	public  static void copy( InputStream in , OutputStream out)throws IOException {
		byte[] buffer = new byte[0xFFFF];
		for (int len; (len = in.read(buffer)) !=-1;) {
			out.write(buffer,0,len);
		}
	}

	private static boolean copyFile (String src, String dest) {
		boolean result = false;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);
			copy(fis,fos);
			result = true;
		} catch (IOException e){
			
		} 
		  finally {
			if (fis != null)
				try { fis.close(); }catch (IOException e){}
			if (fos != null)
				try { fos.close(); } catch (IOException e) {}
		}
		return result;
	}


/**
* Create the necessary directories to follow the path to the external directory given in pDirPath
* @param pDirPath  the path describing the whole path
*/

public static void createExternalDirectoryIfNecessary(String pDirPath) {

 String result = Environment.getExternalStorageState(); 

 if (result.equals(Environment.MEDIA_MOUNTED)) {

    
	File file = new File(pDirPath);
	try {
		
		if (file.mkdirs()) { // here we need android permission in the manifest, mkdirs with generating parents if necessary
			
		} else {
			
		}
	} catch (SecurityException se) {
		
	} catch (Exception e ) {
		
		
	} // try

}
}





}
