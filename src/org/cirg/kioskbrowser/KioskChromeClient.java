package org.cirg.kioskbrowser;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * The WebChromeClient specific to this Activity
 * This class is primarily used for updating the status of loading pages.
 */
public class KioskChromeClient extends WebChromeClient{
	private KioskActivity parent;
	public KioskChromeClient(KioskActivity par){
		super();
		parent = par;
	}
}