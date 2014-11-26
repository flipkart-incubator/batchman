package com.flipkart.fk_android_batchnetworking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.flipkart.fkvolley.AuthFailureError;
import com.flipkart.fkvolley.DefaultRetryPolicy;
import com.flipkart.fkvolley.Request;
import com.flipkart.fkvolley.Response;
import com.flipkart.fkvolley.toolbox.StringRequest;

public abstract class GroupDataHandler {

	private static final String TAG = "GroupDataHandler";

	public final int PRIORITY_BATCH_LOWEST = Integer.MAX_VALUE;
	public final int PRIORITY_BATCH_DEFAULT = PRIORITY_BATCH_LOWEST / 2;
	public final int PRIORITY_BATCH_HIGHEST = Integer.MIN_VALUE;

	private int priority;
	private String groupId;
	private String url;
	private GroupSyncPolicy syncPolicy;

	private int maxBatchSize;
	private int elementCountToDeleteOnBatchFull;
	private HashMap<String, String> httpHeaders;

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

	protected void syncBatch(final ArrayList<Data> currentDataForSyncing,
			Response.Listener<String> listener,
			Response.ErrorListener errorListener) throws Exception {
		StringRequest request = new StringRequest(Request.Method.POST,
				getUrl(), listener, errorListener) {
			@Override
			public byte[] getBody() throws AuthFailureError {
				return getPackedDataForNetworkPush(currentDataForSyncing);
			}

			@Override
			public String getBodyContentType() {
				if (getContentType() != null)
					return getContentType();
				return super.getBodyContentType();
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				if (httpHeaders != null && httpHeaders.size() > 0) {
                    return httpHeaders;
                } else
                    return super.getHeaders();
			}
		};

		request.setRetryPolicy(new DefaultRetryPolicy(
				DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f));

		// add the request object to the queue to be executed
		BatchNetworking.getDefaultInstance().getRequestQueue().add(request);
	}

	public String getContentType() {
		return null;
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

	public abstract byte[] serializeIndividualData(Object data)
			throws Exception;

	public abstract Object deSerializeIndividualData(byte[] data)
			throws Exception;

	/**
	 * Get custom User-Agent, if set.
	 * 
	 * @deprecated Deprecated since 1.2.0 use {@link #getCustomHttpHeaders()}
	 *             instead.
	 */
	public String getUserAgent() {
		if (httpHeaders != null) {
			return httpHeaders.get("User-Agent");
		}
		return null;
	}

	/**
	 * @deprecated Deprecated since 1.2.0 Use
	 *             {@link #setCustomHttpHeaders(java.util.HashMap)} instead.
	 */
	public void setUserAgent(String userAgent) {
		if (httpHeaders == null && userAgent == null)
			return;

		if (httpHeaders == null)
			httpHeaders = new HashMap<String, String>();
		if (userAgent == null)
			httpHeaders.remove("User-Agent");
		else
			httpHeaders.put("User-Agent", userAgent);
	}

	public HashMap<String, String> getCustomHttpHeaders() {
		if (httpHeaders == null)
			httpHeaders = new HashMap<String, String>();
		return httpHeaders;
	}

	public void setCustomHttpHeaders(HashMap<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}
}
