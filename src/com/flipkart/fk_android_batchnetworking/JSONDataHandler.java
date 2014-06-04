package com.flipkart.fk_android_batchnetworking;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;

//import org.json.JSONArray;
//import org.json.JSONObject;
import android.util.Log;

import com.google.mygson.Gson;
import com.google.mygson.JsonArray;
import com.google.mygson.JsonElement;

public class JSONDataHandler extends GroupDataHandler {

	private static final String TAG = "JSONDataHandler";

	/** Charset for request. */
	private static final String PROTOCOL_CHARSET = "utf-8";

	/** Content type for request. */
	private static final String PROTOCOL_CONTENT_TYPE = String.format(
			"application/json; charset=%s", PROTOCOL_CHARSET);

	public JSONDataHandler(String groupId, String url) {
		super(groupId, url);
	}

	public JSONDataHandler(String groupId, String url, GroupSyncPolicy policy,
			int priority) {
		super(groupId, url, policy, priority);
	}

	@Override
	public byte[] getPackedDataForNetworkPush(
			ArrayList<Data> currentDataForSyncing) {

		byte[] body = null;
		try {
			Enumeration<Data> enumeration = Collections
					.enumeration(currentDataForSyncing);

			JsonArray jsonAray = new JsonArray();
			System.out.println("Enumerating through ArrayList");
			while (enumeration.hasMoreElements()) {
				Data data = enumeration.nextElement();
				jsonAray.add((JsonElement) data.getData());
			}

			Gson gson = new Gson();
			body = gson.toJson(jsonAray).getBytes("UTF-8");
			// Log.i(TAG,
			// "getPackedDataForNetworkPush string = "
			// + jsonAray.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return body;
	}

	@Override
	public byte[] serializeIndividualData(Object data) throws Exception {
		if (data == null) {
			throw new Exception("Data can't be null");
		}

		char type = ' ';
		String result = "";
		if (data instanceof String) {
			result = (String) data;
			type = 'S';
		} else if (data instanceof JSONObject) {
			result = ((JSONObject) data).toString();
			type = 'O';
		} else if (data instanceof JSONArray) {
			result = ((JSONArray) data).toString();
			type = 'A';
		}

		if (result == null) {
			throw new Exception("JSONDataHandler couldn'd serialize the data");
		}

		// Add the type prefix to denote the data type. This will be eventually
		// during deserialization.
		result = type + result;
		return result.getBytes(PROTOCOL_CHARSET);
	}

	@Override
	public String getContentType() {
		return PROTOCOL_CONTENT_TYPE;
	}

	@Override
	public Object deSerializeIndividualData(byte[] data) throws Exception {
		String strdata = new String(data, PROTOCOL_CHARSET);
		char type = strdata.charAt(0);
		if (type == 'O') {
			return new JSONObject(strdata);
		} else if (type == 'A') {
			return new JSONArray(strdata);
		}
		return strdata;
	}
}
