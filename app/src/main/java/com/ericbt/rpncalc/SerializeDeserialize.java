package com.ericbt.rpncalc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import thirdPartyComponents.Base64Coder;
import android.util.Log;

import com.ericbt.rpncalc.javascript.CustomContextFactory;

public class SerializeDeserialize {
	/**
	 * Serialize the Object to a base64 encoded string
	 * @param obj object to serialize
	 * @return base 64 encoded string
	 */
	public static String serialize(Serializable obj) {
		long startTime = System.currentTimeMillis();

		String serializedText = null;
		
		final CustomContextFactory customContextFactory = new CustomContextFactory();

		final Context context = customContextFactory.enterContext();

    	final Scriptable scope = context.initStandardObjects();

    	ByteArrayOutputStream byteArrayOutputStream = null;
    	ObjectOutputStream objectOutputStream = null;
    	
    	try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ScriptableOutputStream(byteArrayOutputStream, scope);
	
			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			
			byteArrayOutputStream.flush();
			
			byte[] serializedBytes = byteArrayOutputStream.toByteArray();
			
			char[] results = Base64Coder.encode(serializedBytes);
			
			byteArrayOutputStream.close();
			objectOutputStream.close();
			
			serializedText = new String(results);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
			Log.e(StringLiterals.LogTag, "SerializeDeserialize.serialize", ex);
		}
    	finally {
    		try {
	    		if (byteArrayOutputStream != null) {
	    			byteArrayOutputStream.close();
	    		}
	    		
	    		if (objectOutputStream != null) {
	    			objectOutputStream.close();
	    		}
    		}
    		catch (Exception ex) {
    			Log.e(StringLiterals.LogTag, ex.getMessage());
    		}
    		
    		Context.exit();
    	}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("SerializeDeserialize.serialize: %d ms", elapsedMilliseconds));

		return serializedText;
	}

	/**
	 * Deserialize the base64 encoded string to an Object
	 * @param serializedText base64 encoded string
	 * @return FontList
	 */
	public static Object deserialize(String serializedText) {
		long startTime = System.currentTimeMillis();

		Object obj = null;

		if (serializedText != null && !serializedText.isEmpty()) {
			final CustomContextFactory customContextFactory = new CustomContextFactory();

			final Context context = customContextFactory.enterContext();

	    	final Scriptable scope = context.initStandardObjects();

	    	try
			{
				byte[] serializedBytes = Base64Coder.decode(serializedText);
				
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
				ObjectInputStream objectInputStream = new ScriptableInputStream(byteArrayInputStream, scope);
				
				obj = objectInputStream.readObject();

				byteArrayInputStream.close();
				objectInputStream.close();
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				Log.e(StringLiterals.LogTag, "SerializeDeserialize.deserialize", ex);
			}
	    	finally {
	    		Context.exit();
	    	}
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("SerializeDeserialize.deserialize: %d ms", elapsedMilliseconds));

		return obj;
	}

}
