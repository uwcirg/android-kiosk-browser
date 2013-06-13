package org.cirg.kioskbrowser;

import org.cirg.kioskbrowser.admin.AdminActivity;
import org.cirg.kioskbrowser.auth.AuthenticationManager;
import org.cirg.kioskbrowser.auth.AuthenticationManager.PriviledgedAction;
import org.cirg.kioskbrowser.auth.PbeAuthManager;
import org.cirg.kioskbrowser.config.KioskConfigFileManager;
import org.cirg.kioskbrowser.config.XMLKioskConfiguration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
/**
 * Android activity for the DHAIR mobile application
 */
public class KioskActivity extends Activity{
	
	public static final String TAG = "KioskBrowserActivity";
	public static final String HELP_URL = "file:///android_asset/noconfig.html";
	public static final String NO_PASSWORD= "file:///android_asset/noPassword.html";
	public static final String ERR_HOST_LOOKUP_FAIL_URL = "file:///android_asset/host_lookup_fail.html";
	public static final String AUTH_FAILURE_PAGE = "file:///android_asset/authfailed.html";
	public static final String CONFIG_FILE = "config.xml";
	private static final String SHARED_PREFS_NAME = "KioskSettings";
	
	public static final int REQUEST_ADMIN_ACTIVITY = 2; 
	
	private WebView webView;
	private CookieManager cManager;
	private KioskConfigFileManager config;
	private AuthenticationManager authManager;
	private SharedPreferences kioskSettings;
	private boolean deleteHistoryAfterPageLoad;
	
	/** For this version kiosk mode is always enabled **/
	private final boolean kioskMode = true;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        config = new XMLKioskConfiguration();
        kioskSettings = getSharedPreferences(SHARED_PREFS_NAME,MODE_PRIVATE);
        cManager  = CookieManager.getInstance();
    	authManager = new PbeAuthManager(kioskSettings);
        setContentView(R.layout.main);
    	config.loadConfigFile(getFileStreamPath(CONFIG_FILE));
        configureWebView();
    	if(!authManager.passwordIsSet()){
    		 //password not set, start admin tools activity
    		webView.loadUrl(NO_PASSWORD);
    	}
    	else{
    		startBrowsing();
    	}
    }
    
    /**Called when the application is stopped, forces the user to log out.**/
	protected void onStop(){
		Log.i(TAG,"onStop");
		super.onPause();   	
		endSession();
	}

	/**Called when the application's memory is reclaimed by the memory manager. Forces the user to log out**/
	protected void onDestroy(){
		Log.i(TAG,"onDestroy");
		super.onDestroy();
		endSession();
	}

	public void onNewIntent(Intent i){
    	//Do nothing!
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_ADMIN_ACTIVITY){
			if(resultCode == RESULT_OK){
				Bundle args = data.getExtras();
				if(args.getBoolean(AdminActivity.RETURN_RESET_CONFIG)){
					if(config.loadedConfig()){
						config.deleteConfigFile(this, CONFIG_FILE);
					}
				}
				if(args.containsKey(AdminActivity.RETURN_PASSWORD_HASH)){
	    			authManager.writePasswordHash(args.getString(AdminActivity.RETURN_PASSWORD_HASH));
					Toast.makeText(this, "New password Set", Toast.LENGTH_SHORT).show();
					//the password was set, reload the web view
				}
				startBrowsing();
			}
			else if(requestCode == RESULT_CANCELED){
				//the password setting was cancelled, close the application
				finish();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//this method is called when the orientation of the android device is changed
		//do nothing, we want to keep the screen in landscape mode, 
		//overriding this stops the call of onDestroy() when the orientation changes
		super.onConfigurationChanged(newConfig);	
	}

	private void configureWebView(){
        //load the webView
        webView = (WebView) findViewById(R.id.webview);
        //add the zoom control toolbar
        webView.getSettings().setBuiltInZoomControls(true);
        //enable javascript
        webView.getSettings().setJavaScriptEnabled(true);
        //set the chrome client and webView client
        webView.setWebChromeClient(new KioskChromeClient(this));
        webView.setWebViewClient(new KioskWebViewClient(this));
        //add the javascript interface
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        //disable caching
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //do not save passwords or form data
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        //adjust the view so that it displays as much of the webpage as possible
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setLoadWithOverviewMode(true);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //disable long clicks on the web view
        webView.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				return true;
			}
		});
    }
    
    private void startBrowsing(){
    	deleteHistoryAfterPageLoad = true;
        if (config.loadedConfig()){
        	webView.loadUrl(config.getHomeUrl());
        }
        //no config file was found, navigate to this hard-coded URL
        else {
        	Toast.makeText(this, "Could not load config file. Directing to download page", Toast.LENGTH_LONG).show();
        	webView.loadUrl(HELP_URL);
        }
    }
    
    /**
	 * TOGGLING CONFIG FILE CURRENTLY NOT SUPPORTED
	 */
	private void setKioskMode(boolean enabled){
		Log.i(TAG,"Toggling kiosk mode currently not available");
		//http://stackoverflow.com/questions/6408086/android-can-i-enable-disable-an-activitys-intent-filter-programmatically
		//getPackageManager().setComponentEnabledSetting(new ComponentName("org.cirg.kioskbrowser","KioskMode"), enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

	private void exitBrowser(){
		if (authManager.passwordIsSet()){
	    	authManager.authenticate("Admin password required to exit this application", this.getFragmentManager(), new PriviledgedAction(){
	    		public void doAction(){
	    			startAdminActivity(false);
	    		}
	    	},
	    	new PriviledgedAction(){
	    		public void doAction(){
	    			Toast toast = Toast.makeText(KioskActivity.this, "Incorrect Password", Toast.LENGTH_SHORT);
	    			toast.show();
	    		}
	    	},
	    	new PriviledgedAction(){
	    		public void doAction(){
	    			Toast toast = Toast.makeText(KioskActivity.this, "Cancelled", Toast.LENGTH_SHORT);
	    			toast.show();
	    		}
	    	});
		}
		else
			startAdminActivity(true);
	}

	public void startAdminActivity(boolean forcePasswordSet){
		Bundle b = new Bundle();
		b.putBoolean(AdminActivity.ARG_KIOSK_MODE, kioskMode);
		b.putBoolean(AdminActivity.ARG_FORCE_PASSWORD_SET, forcePasswordSet);
		Intent i = new Intent(this,AdminActivity.class);
		i.putExtras(b);
		startActivityForResult(i,REQUEST_ADMIN_ACTIVITY);
	}

	public void writeConfigFile(final String fileContents){
    	//check to make sure that the file is a valid config file
    	if(!config.isValidConfigFile(fileContents)){
    		Toast.makeText(this, "Invalid Config file", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	//first authenticate the user
    	authManager.authenticate("This website is attempting to set the configuration for this kiosk browser. " +
    			"Enter your password to confirm this action", this.getFragmentManager(), new PriviledgedAction(){
    		public void doAction(){
		    	config.writeConfigFile(KioskActivity.this,CONFIG_FILE,fileContents);
		    	Toast.makeText(KioskActivity.this, "Config File written", Toast.LENGTH_SHORT).show();
		    	config.loadConfigFile(getFileStreamPath(CONFIG_FILE));
		    	startBrowsing();
    		}
    	},  new PriviledgedAction(){
			public void doAction() {
		    	Toast.makeText(KioskActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
			}
		}, new PriviledgedAction(){
			public void doAction() {
		    	Toast.makeText(KioskActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
			}
		});
    }
    
    
    /**
     * Method that tells the webView to navigate forward or backwards in the history
     * @param rel_page the number of steps away from the current page. -1 indicate to the WebView
     * to jump back one step, while 3 indicates to the WebView to jump forward 3 steps
     *	
     * This method is used by the javascript interface
     */
    public void navigatePageHistory(int rel_page){
    	Log.i(TAG, "Navigating: " + rel_page);
    	webView.goBackOrForward(rel_page);
    }
    
    /**Delete all of the user's session information*/
    public void endSession(){
    	Log.i(TAG,"Ending Session");
    	if (config.loadedConfig()){
	    	webView.clearCache(true);
	    	deleteHistoryAfterPageLoad = true;
	    	cManager.removeAllCookie();
	    	deleteDatabase("webview.db");
    	}
    }
    
    @Override
    public void onBackPressed(){
    	if(webView.canGoBack())
	    	webView.goBack();
    }
    
	@Override
    public boolean onKeyDown(int keycode,KeyEvent event){
	    //if the menu button is pressed
		if ((keycode == KeyEvent.KEYCODE_SEARCH)){
			//do nothing
			return true;
		}
		
		if ((keycode == KeyEvent.KEYCODE_HOME)){
			return true;
		}
		return super.onKeyDown(keycode, event);
    }
	
	@Override
	public boolean onKeyLongPress(int keycode, KeyEvent event){
		if(keycode == KeyEvent.KEYCODE_BACK){
			exitBrowser();
			return true;
		}
		return false;
	}

	/**
     * The WebViewClient specific to this Activity
     * This private class is responsible for the following
     * 	> Overriding URL loading, so a webpage within the specified domain is loaded by the webview, not the device's browser
     *  > launching new intents when the user attempts to navigate outside the specified domain
     *  > handling HTTP authorization requests from the server
     */
    public class KioskWebViewClient extends WebViewClient {
		public static final String TAG = "KioskWebViewClient";
		private KioskActivity kioskActivity;
		
		public KioskWebViewClient(KioskActivity main){
			this.kioskActivity = main;
		}
		
		/**
		 * Method that handle ERROR codes given by the webView
		 */
    	@Override
    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    		Log.i(TAG,"ErrorCode: " + errorCode);	
    		Log.i(TAG,"falingURL: " + failingUrl);	
    		
    		if(errorCode == WebViewClient.ERROR_HOST_LOOKUP)
    			webView.loadUrl(ERR_HOST_LOOKUP_FAIL_URL);
    	}
    	
    	/**
    	 * Method that views all attempted page loads, and decides if they should be loaded by the webView, the native
    	 * browser, or not at all
    	 */
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url){
    		if(url.equals("about:blank"))
    			return true;
    		if (config.isAllowedSite(url)){
                // let my WebView load the page
                Log.i(TAG,"Loading new page");
    			return false;
            }
	    	Log.i(TAG,"Not allowed site: " + url + ": Not loading.");
	    	Toast.makeText(kioskActivity, "site not allowed.", Toast.LENGTH_SHORT).show();
	    	return true;
        }
    	
    	@Override
		public void onPageFinished(WebView view, String url) {
    		if(deleteHistoryAfterPageLoad){
    			deleteHistoryAfterPageLoad = false;
    			webView.clearHistory();
    		}
			for(String javascript : config.getUrlSpecificJavascript(url)){
				webView.loadUrl("javascript:" + javascript);
			}
    		super.onPageFinished(view, url);
		}
    	
    	
		/**
    	 * Method called when the server requires user authentication before loading a page
    	 * Calls the showDialog method to prompt the user of a name and password, if a username and password has not already been set
    	 * Once authenticated, this method navigates the user back to the url specified by start_url
    	 */
    	@Override
    	public void onReceivedHttpAuthRequest(WebView view,
    			HttpAuthHandler handler, String host, String realm) {
    		final HttpAuthHandler finalHandler = handler;
			authManager.handlehttpAuthRequest("This website requires authentication",kioskActivity.getFragmentManager(), new PriviledgedAction(){
				public void doAction(){
					finalHandler.proceed(authManager.getHttpAuthUsername(),authManager.getHttpAuthPassword());
				}
			}, new PriviledgedAction(){
				public void doAction(){
					Log.i(TAG,"Canceled");
					Toast.makeText(KioskActivity.this, "Authorization failed", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}