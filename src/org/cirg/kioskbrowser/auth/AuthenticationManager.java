package org.cirg.kioskbrowser.auth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public abstract class AuthenticationManager {

	public static final String TAG = "AuthenticationManager";
	
	final public void authenticate(final String message, FragmentManager fragmentManager, final PriviledgedAction onSuccess, final PriviledgedAction onFailure,final PriviledgedAction onCancel ){
		DialogFragment inputDialog = new DialogFragment(){

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(org.cirg.kioskbrowser.R.layout.auth_dialog, null);
				builder.setView(layout);
				builder.setTitle("Authentication Required");
				builder.setMessage(message);
				final EditText pwField = ((EditText) layout.findViewById(org.cirg.kioskbrowser.R.id.auth_password));
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if(checkPassword(pwField.getText().toString().toCharArray()))
							onSuccess.doAction();
						else
							onFailure.doAction();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onCancel.doAction();
					}
				});
				return builder.create();
			}
			
		};
    	inputDialog.setCancelable(false);
    	inputDialog.show(fragmentManager, "AuthDialog");
	}
	
	final public void handlehttpAuthRequest(final String message, FragmentManager fragmentManager, final PriviledgedAction onSuccess, final PriviledgedAction onCancel ){
		DialogFragment inputDialog = new DialogFragment(){

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater inflater = getActivity().getLayoutInflater();
				final View layout = inflater.inflate(org.cirg.kioskbrowser.R.layout.httpauth_dialog, null);
				builder.setView(layout);
				builder.setTitle("Http Auth Request");
				builder.setMessage(message);
				final EditText usernameField = ((EditText) layout.findViewById(org.cirg.kioskbrowser.R.id.httpauth_username));
				final EditText pwField = ((EditText) layout.findViewById(org.cirg.kioskbrowser.R.id.httpauth_password));
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						storeHttpAuthCredentials(usernameField.getText().toString(),pwField.getText().toString());
						onSuccess.doAction();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onCancel.doAction();
					}
				});
				return builder.create();
			}
			
		};
    	inputDialog.setCancelable(false);
    	inputDialog.show(fragmentManager, "HttpAuthDialog");
	}
	
	public abstract void storeHttpAuthCredentials(String username, String password);
	
	public abstract String getHttpAuthUsername();
	
	public abstract String getHttpAuthPassword();
	
	public abstract void writePasswordHash(char[] pw);
	
	public abstract void writePasswordHash(String pw);
	
	public abstract boolean checkPassword(char[] password);
	
	public abstract boolean passwordIsSet();
		
	public interface PriviledgedAction{
		public void doAction();
	}
	
}
