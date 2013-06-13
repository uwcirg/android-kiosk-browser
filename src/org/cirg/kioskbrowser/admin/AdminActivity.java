package org.cirg.kioskbrowser.admin;

import org.cirg.kioskbrowser.R;
import org.cirg.kioskbrowser.auth.SetPasswordActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class AdminActivity extends Activity {
	
	public static final String TAG = "Admin tools";
	
	/* Arguments that will be sent or returned to this activity in the bundle */
	public static final String ARG_FORCE_PASSWORD_SET = "forcepassword";
	public static final String ARG_KIOSK_MODE= "kioskmode";
	public static final String RETURN_PASSWORD_HASH = "passwordhash";
	public static final String RETURN_RESET_CONFIG = "reset_config";
	
	/*Request codes sent to other intents */
	public static final int SET_PASSWORD_REQUEST_CODE = 0;
	
	/*Settings that the admin may change*/
	private boolean forcePasswordReset;
	private String newPasswordHash;
	
	/*View elements*/
	private Button browseButton, resetButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//allow this activity to run on top of the lock screen
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //get the arguments
		Bundle arguments = getIntent().getExtras(); 
		forcePasswordReset = arguments.getBoolean(ARG_FORCE_PASSWORD_SET);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_tools);
		browseButton = (Button) findViewById(R.id.continue_button);
		resetButton = (Button) findViewById(R.id.reset_config_button);
		if (forcePasswordReset){
			browseButton.setEnabled(false);
			resetButton.setEnabled(false);
			Toast.makeText(this, "You must set your password before returning to the web browser", Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == SET_PASSWORD_REQUEST_CODE){
			if(resultCode ==  RESULT_OK){
				newPasswordHash = data.getExtras().getString(SetPasswordActivity.RETURN_PASSWORD_FIELD);
				Toast.makeText(this, "Password will be changed when the activity restarts", Toast.LENGTH_SHORT).show();
				if(forcePasswordReset){
					forcePasswordReset = false;
					browseButton.setEnabled(true);
					resetButton.setEnabled(true);
				}
			}
			else{
				Toast.makeText(this, "Password was not changed", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void onBackPressed(){
		continueBrowsing(null);
	}
	
	public void setPassword(View view){
    	startActivityForResult(new Intent(this, SetPasswordActivity.class), SET_PASSWORD_REQUEST_CODE);
	}
	
	public void continueBrowsing(View v){
		if(forcePasswordReset){
			Toast.makeText(this, "An admin password must be set before you can return to the browser", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent result = new Intent();
		result.putExtra(RETURN_RESET_CONFIG,false);
		if(newPasswordHash != null)
			result.putExtra(RETURN_PASSWORD_HASH, newPasswordHash);
		setResult(RESULT_OK, result);
		finish();
	}
	
	public void resetConfig(View v){
		Intent result = new Intent();
		result.putExtra(RETURN_RESET_CONFIG,true);
		if(newPasswordHash != null)
			result.putExtra(RETURN_PASSWORD_HASH, newPasswordHash);
		setResult(RESULT_OK, result);
		finish();
		
	}

}
