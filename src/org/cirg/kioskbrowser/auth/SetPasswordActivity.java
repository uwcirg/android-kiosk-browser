package org.cirg.kioskbrowser.auth;

import java.security.GeneralSecurityException;

import org.cirg.kioskbrowser.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetPasswordActivity extends Activity {
	
	public static final String TAG = "SetPasswordActivity";
	
	public static final String RETURN_PASSWORD_FIELD = "pw";
	
	protected Button mEnterButton, mCancelButton;
	protected EditText mPassword, mPasswordChk;
	
	@Override
	public void onCreate(Bundle savedState){
    	super.onCreate(savedState);
		//allow this activity to run on top of the lock screen
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_password_dialog);
        mPassword = (EditText) findViewById(R.id.set_password0);
        mPasswordChk = (EditText) findViewById(R.id.set_password1);
        mPasswordChk.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
					//TODO make this prettier
					if(!passwordsEqual())
						mPasswordChk.setBackgroundColor(Color.RED);
					else
						mPasswordChk.setBackgroundColor(Color.BLACK);
				}
        });
	}
	
	protected boolean passwordsEqual(){
		if(!mPassword.getText().toString().equals(mPasswordChk.getText().toString()))
			return false;
		return true;
	}
	
	
	public void enter(View view){
		if(passwordsEqual()){
			if(mPassword.getText().toString().equals("")){
				Toast.makeText(this, "Password field cannot be empty", Toast.LENGTH_SHORT).show();
				return;
			}
			
			 Intent returnIntent = new Intent();
			 try{
				 returnIntent.putExtra(RETURN_PASSWORD_FIELD, new String(PbeAuthManager.encrypt(mPassword.getText().toString().toCharArray())));
			 }
			 catch(GeneralSecurityException e){
				//password could not be encrypted, log the error and finish
				Log.e(TAG,e.getMessage());
				setResult(RESULT_CANCELED,returnIntent);     
				finish();
			 }
			 setResult(RESULT_OK,returnIntent);     
			 finish();
		}
		else{
			Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
		}
	}
}