package com.example.fk_android_batchnetworking;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;

public class JSONDataHandler extends GroupDataHandler {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return body;
	}
}
