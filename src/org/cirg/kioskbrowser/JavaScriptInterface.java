package org.cirg.kioskbrowser;

import android.util.Log;
import android.widget.Toast;

/**
 * This class can be used to define a javascript API for the app. Methods in this API can be called in a javascript file by calling
 * Android.functionName(...) 
 * 
 * The android activity can also call javascript functions by calling webView.loadURL("javascript:aFunction()");
 * 
 */

public class JavaScriptInterface{
	public static final String TAG = "JavascriptInterface";
	private KioskActivity activity;
	public JavaScriptInterface(KioskActivity  c){
		activity = c;
	}
	
	public void log(String s){
		Log.i(TAG,s);
	}
	
	public void toast(String msg){
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}
	
	public void closeApp(){
		activity.finish();
	}
	
	public void writeConfigFile(String file_contents){
		Log.i(TAG,"Writing config file: " + file_contents);
		activity.writeConfigFile(file_contents);
	}
	
}

