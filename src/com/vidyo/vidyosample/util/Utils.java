package com.vidyo.vidyosample.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utils {
	
	protected static final String TAG = Utils.class.getName();
	
	public static String getAndroidInternalMemDir(final Context context)  throws IOException {
		final File fileDir = context.getFilesDir(); 
		if (fileDir != null) {
					String filedir = fileDir.toString() + "/";
		Log.d(TAG, "file directory = " + filedir);
			return filedir;
		} 
		else {
			Log.e(TAG, "Something went wrong, filesDir is null");
		}
		return null;
	}

	public static String getAndroidCacheDir(final Context context) throws IOException {
		final File cacheDir = context.getCacheDir();
		if (cacheDir != null) {
			String filedir = cacheDir.toString() + "/";
			return filedir;
		}
		return null;
	}
	
	public static final String prettyPrint(Document xml) throws Exception {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xml), new StreamResult(out));
		Log.d(TAG, out.toString());
		return out.toString();
	}
	
	public static boolean isWifiConnected(final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
}
