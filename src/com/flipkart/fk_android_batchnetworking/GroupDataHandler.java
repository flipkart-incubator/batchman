package com.flipkart.fk_android_batchnetworking;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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
    private boolean isCompressData = false;
    private HashMap<String, String> httpHeaders;

	public GroupDataHandler(String groupId, String url , boolean isCompressData) {
		this.groupId = groupId;
		this.url = url;
        this.isCompressData = isCompressData;
		this.syncPolicy = new GroupSyncPolicy();
		this.priority = PRIORITY_BATCH_DEFAULT;
		this.maxBatchSize = 50;
		elementCountToDeleteOnBatchFull = 5;
 	}

	public GroupDataHandler(String groupId, String url, GroupSyncPolicy policy,
			int priority,boolean isCompressData) {
		this.groupId = groupId;
		this.url = url;
		this.syncPolicy = policy;
        this.isCompressData = isCompressData;
		if (null == syncPolicy) {
			this.syncPolicy = new GroupSyncPolicy();
		}
        if(priority != -1) {
            this.priority = priority;
        } else {
            this.priority = PRIORITY_BATCH_DEFAULT;
        }
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
                byte[] body = getPackedDataForNetworkPush(currentDataForSyncing);
                if(body != null && isCompressData) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try{
                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                        gzipOutputStream.write(body);
                        gzipOutputStream.close();
                        return byteArrayOutputStream.toByteArray();
                    } catch(IOException e){
                        throw new RuntimeException(e);
                    }
                }
				return body;
			}

			@Override
			public String getBodyContentType() {
				if (getContentType() != null)
					return getContentType();
				return super.getBodyContentType();
			}

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
                if(isCompressData) {
                    if (httpHeaders == null) {
                        httpHeaders = new HashMap<String, String>();
                    }
                    httpHeaders.put("Content-Encoding","gzip");
                }
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

}
