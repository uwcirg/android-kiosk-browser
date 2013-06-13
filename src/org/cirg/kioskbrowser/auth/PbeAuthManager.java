package org.cirg.kioskbrowser.auth;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.SharedPreferences;
import android.util.Base64;
public class PbeAuthManager extends AuthenticationManager {

	private static byte[] salt = {
			 (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
             (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	};
	private static int iterationCount = 2974;
	private SharedPreferences kioskSettings;
	private static final String PASSWORD_KEY = "user_password";
	private static final String HTTP_AUTH_USERNAME_KEY = "httpauth_username";
	private static final String HTTP_AUTH_PASSWORD_KEY ="httpauth_password";
	
	public PbeAuthManager(SharedPreferences settings){
		kioskSettings = settings;
	}
	
	@Override
	public void storeHttpAuthCredentials(String username, String password) {
		kioskSettings.edit().putString(HTTP_AUTH_USERNAME_KEY, username).commit();
		kioskSettings.edit().putString(HTTP_AUTH_PASSWORD_KEY, password).commit();
	}

	@Override
	public String getHttpAuthUsername() {
			return kioskSettings.getString(HTTP_AUTH_USERNAME_KEY, null);
	}

	@Override
	public String getHttpAuthPassword() {
			return kioskSettings.getString(HTTP_AUTH_PASSWORD_KEY, null);
	}

	@Override
	final public void writePasswordHash(char[] hash)  {
		kioskSettings.edit().putString(PASSWORD_KEY, new String(hash)).commit();
	}
	
	@Override
	final public void writePasswordHash(String hash)  {
		kioskSettings.edit().putString(PASSWORD_KEY, hash).commit();
	}
	
	@Override
	final public boolean checkPassword(char[] pw) {
		try{
			return kioskSettings.getString(PASSWORD_KEY, "").equals(new String(encrypt(pw)));
		}catch(GeneralSecurityException e){
			return false;
		}
	}
	
	@Override
	public boolean passwordIsSet() {
		return !kioskSettings.getString(PASSWORD_KEY,"").equals(""); 
	}

	public static byte[] encrypt(char[] pw) throws GeneralSecurityException{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(pw));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, iterationCount));
        byte[] toReturn = Base64.encode(pbeCipher.doFinal(), Base64.DEFAULT);
        return toReturn;
	}
}
