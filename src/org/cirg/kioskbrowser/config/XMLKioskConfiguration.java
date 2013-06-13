package org.cirg.kioskbrowser.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;

public class XMLKioskConfiguration implements KioskConfigFileManager {

	public static final String TAG = "XMLKioskConfiguration";
	
	private List<String> hostNames;
	private List<String> allowedUrls;
	private List<ScriptPair> customScripts;
	private String homeUrl;
	private boolean loadedConfig;

	private static final String HOSTNAME_QUERY= "config/allowed-sites/host-regex";
	private static final String ALLOWED_URLS_QUERY = "config/allowed-sites/url";
	private static final String HOME_URL_QUERY = "config/allowed-sites/start-url";
	private static final String SCRIPT_QUERY= "config/script ";
	private static final String SCRIPT_HOST_ATTRIBUTE = "host-regex";
	
	public XMLKioskConfiguration(){
		hostNames = new ArrayList<String>();
		allowedUrls = new ArrayList<String>();
		customScripts = new LinkedList<ScriptPair>();
		setDefaults();
	}
	
	private void setDefaults(){
		loadedConfig = false;
	}
	
	public boolean loadConfigFile(File configFile){
    	XPathFactory xpathFactory = XPathFactory.newInstance();
    	XPath xpath = xpathFactory.newXPath();
    	try{
	    	Scanner scan = new Scanner(configFile);
	    	String configText = ""; 
	    	while(scan.hasNextLine())
		    	configText +=  scan.nextLine() + "\n";
	    	
	    	//load the allowed hosts
	    	XPathExpression expr = xpath.compile(HOSTNAME_QUERY);
	    	NodeList result  = (NodeList) expr.evaluate(new InputSource(new StringReader(configText)), XPathConstants.NODESET);
	    	addHostNames(result);
	    	//load the allowed URLS
	    	expr = xpath.compile(ALLOWED_URLS_QUERY);
	    	result = (NodeList) expr.evaluate(new InputSource(new StringReader(configText)),XPathConstants.NODESET);
	    	addUrls(result);
	    	//load the home url 
	    	expr = xpath.compile(HOME_URL_QUERY);
	    	result = (NodeList) expr.evaluate(new InputSource(new StringReader(configText)),XPathConstants.NODESET);
	    	if(result.getLength() > 1){
	    		Log.e(TAG,"Config file parser found more than one home-url node");
	    		return false;
	    	}
	    	homeUrl = result.item(0).getTextContent();
	    	allowedUrls.add(homeUrl);
	    	//load the custom scripts for
	    	expr = xpath.compile(SCRIPT_QUERY);
	    	result  = (NodeList) expr.evaluate(new InputSource(new StringReader(configText)), XPathConstants.NODESET);
	    	addCustomScripts(result);
	    	
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		Log.e(TAG,"Error loading config file: " + e.getMessage());
    		return false;
    	}
    	loadedConfig = true;
    	return true;
	}

	private void addHostNames(NodeList list){
		for (int i = 0; i < list.getLength(); i++) {
			//hostNames.add(Pattern.quote(list.item(i).getTextContent()));
			hostNames.add(list.item(i).getTextContent());
		}
	}
	
	private void addUrls(NodeList list){
		for(int i = 0; i < list.getLength() ; i++) {
			allowedUrls.add(list.item(0).getTextContent());
		}
	}
	
	private void addCustomScripts(NodeList list){
		for(int i = 0; i < list.getLength() ; i++) {
			String url = list.item(i).getAttributes().getNamedItem(SCRIPT_HOST_ATTRIBUTE).getTextContent();
			String script = list.item(i).getTextContent().trim();
			customScripts.add(new ScriptPair(url,script));
		}
	}
	
	public boolean isValidConfigFile(String contents) {
		//TODO implement this 
		return true;
	}

	public boolean loadedConfig() {
		return loadedConfig;
	}

	public boolean writeConfigFile(Context parent,String filePath ,String fileContents) {
    	try{
	    	FileOutputStream fos = parent.openFileOutput(filePath, Context.MODE_PRIVATE);
	    	fos.write(fileContents.getBytes());
	    	fos.close();
	    	return true;
    	}
    	catch(FileNotFoundException e){
    		Log.i(TAG,"File not found exception while writing config file");
    		return false;
    	}
    	catch(IOException e){
    		Log.i(TAG,"IO exception while writing config file");
    		return false;
    	}
    }
	
	public void deleteConfigFile(Context parentContext, String filepath){
		parentContext.deleteFile(filepath);
		loadedConfig = false;
	}
	
	public boolean isAllowedSite(String url) {
		URL urlObj = null; 
		try{
			urlObj = new URL(url);
		}
		catch(MalformedURLException e){
		 	Log.i(TAG,"Malformed URL: " + url);
			return false;
		}
		for(String host : hostNames){
			if (urlObj.getHost().matches(host))
				return true;
		}
		for(String s :allowedUrls){
			URL other; 
			try{
				other = new URL(s);
			}
			catch(Exception e){
				continue;
			}
			if (urlObj.equals(other) || urlObj.toString().equals(s))
				return true;
		}
		return false;
	}

	public String getHomeUrl() {
		return homeUrl;
	}

	public boolean hasUrlSpecificJavascript(String url) {
		for(ScriptPair p : customScripts)
			if(url.matches(p.url))
				return true;
		return false;
	}

	public List<String> getUrlSpecificJavascript(String url) {
		List<String> scripts = new LinkedList<String>();
		for(ScriptPair p : customScripts){
			if(url.matches(p.url))
				scripts.add(p.script);
		}
		return scripts;
	}
	
	class ScriptPair{
		String url, script;
		
		public ScriptPair(String url, String script){
			this.url = url;
			this.script = script;
		}
	}
	
	

}
