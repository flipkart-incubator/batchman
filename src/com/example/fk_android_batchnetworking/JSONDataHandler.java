package com.example.fk_android_batchnetworking;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONDataHandler extends GroupDataHandler {

	String characterSetName = "UTF-8";
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
		JSONArray jsonAray = new JSONArray(currentDataForSyncing);
		byte[] body = null;
		try {
			body = jsonAray.toString().getBytes("UTF-8");
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
		return result.getBytes(characterSetName);
	}

	@Override
	public Object deSerializeIndividualData(byte[] data) throws Exception {
		String strdata = new String(data, characterSetName);
		char type = strdata.charAt(0);
		if (type == 'O') {
			return new JSONObject(strdata);
		} else if (type == 'A') {
			return new JSONArray(strdata);
		}
		return strdata;
	}
}
