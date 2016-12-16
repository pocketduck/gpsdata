package com.chexiao.gpsdata.tools;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class ConfigLoadTool {
	
	public static final Properties pt = new Properties();

	public static  String getConfigFilePath(String fileName){
		String path = null;
		URI uri = null;
		try {
			uri = ConfigLoadTool.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if(uri!=null){
			File classpath = new File(uri);
			path = classpath.getPath();
			if(classpath.isFile()){
				path = classpath.getParent();
			} 
		} 
 		path = path + File.separator + "conf" + File.separator  + fileName;
		return path;
	}
}
