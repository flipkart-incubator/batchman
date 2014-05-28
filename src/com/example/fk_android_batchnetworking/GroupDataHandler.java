package com.example.fk_android_batchnetworking;

import java.util.ArrayList;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public abstract class GroupDataHandler {

	public final int PRIORITY_BATCH_LOWEST = Integer.MAX_VALUE;
	public final int PRIORITY_BATCH_DEFAULT = PRIORITY_BATCH_LOWEST / 2;
	public final int PRIORITY_BATCH_HIGHEST = Integer.MIN_VALUE;

	private int priority;
	private String groupId;
	private String url;
	private GroupSyncPolicy syncPolicy;

	private int maxBatchSize;
	private int elementCountToDeleteOnBatchFull;

	public GroupDataHandler(String groupId, String url) {
		this.groupId = groupId;
		this.url = url;
		this.syncPolicy = new DefaultSyncPolicy();
		this.priority = PRIORITY_BATCH_DEFAULT;
		this.maxBatchSize = 50;
		elementCountToDeleteOnBatchFull = 5;
	}

	public GroupDataHandler(String groupId, String url, GroupSyncPolicy policy,
			int priority) {
		this.groupId = groupId;
		this.url = url;
		this.syncPolicy = policy;
		if (null == syncPolicy) {
			this.syncPolicy = new DefaultSyncPolicy();
		}
		this.priority = priority;
		this.maxBatchSize = 50;
		elementCountToDeleteOnBatchFull = 5;
	}

	protected void syncBatch(final ArrayList<Data> currentDataForSyncing, Response.Listener<String> listener, Response.ErrorListener errorListener) throws Exception
	{
		StringRequest request = new StringRequest(Request.Method.POST,
				getUrl(), listener, errorListener) {
			@Override
			public byte[] getBody() throws AuthFailureError {
		        return getPackedDataForNetworkPush(currentDataForSyncing);
		    }
		};

		// add the request object to the queue to be executed
		BatchNetworking.getDefaultInstance().getRequestQueue().add(request);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public GroupSyncPolicy getSyncPolicy() {
		return syncPolicy;
	}

	public void setSyncPolicy(GroupSyncPolicy syncPolicy) {
		this.syncPolicy = syncPolicy;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	public int getElementCountToDeleteOnBatchFull() {
		return elementCountToDeleteOnBatchFull;
	}

	public void setElementCountToDeleteOnBatchFull(
			int elementCountToDeleteOnBatchFull) {
		this.elementCountToDeleteOnBatchFull = elementCountToDeleteOnBatchFull;
	}

	protected abstract byte[] getPackedDataForNetworkPush(
			ArrayList<Data> currentDataForSyncing);
	
	public abstract byte[] serializeIndividualData(
			Object data) throws Exception;
	
	public abstract Object deSerializeIndividualData(
			byte[] data) throws Exception;

}
