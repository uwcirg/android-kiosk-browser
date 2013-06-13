package org.cirg.kioskbrowser.config;

import java.io.File;
import java.util.List;

import android.content.Context;

/**
 * This file defines an the interface between the KioskActivity and
 * the configuration file 
 * 
 * The config file allows provides a list of allowed urls and hosts, request
 * to load a page that do not match one of these urls or hosts by the KioskActivity 
 * 
 * The config file also allows JavaScript to be loaded for a specific URL.	
 *
 */
public interface KioskConfigFileManager{
	
		public boolean loadConfigFile(File configFile);
		
		public boolean loadedConfig();
	
		public boolean writeConfigFile(Context parent, String filePath, String fileContents);
		
		public void deleteConfigFile(Context parentContext, String filePath);
		
		public boolean isValidConfigFile(String contents);
		
		public boolean isAllowedSite(String url);
		
		public String getHomeUrl();
		
		public boolean hasUrlSpecificJavascript(String url);
		
		public List<String> getUrlSpecificJavascript(String url);
		
}
