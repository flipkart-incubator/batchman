package com.example.fk_android_batchnetworking;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class BatchNetworking {
	private static final String TAG = "BatchNetworking";
	private final static BatchNetworking INSTANCE = new BatchNetworking();
	private GroupPriorityQueue groupPriorityQueue;
	private Context applicationContext;
	private RequestQueue requestQueue;
	private DBManager dbInstance;

	public RequestQueue getRequestQueue() throws Exception {
		if (requestQueue == null) {
			if (null == getApplicationContext()) {
				throw new Exception("initialize method not called");
			}
			requestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return requestQueue;
	}

	public void initialize(Context applicationContext) {
		Log.i(TAG, "In initialize, applicationContext = " + applicationContext);
		this.applicationContext = applicationContext;
	}

	public GroupPriorityQueue getGroupPriorityQueue() {
		return groupPriorityQueue;
	}

	public static BatchNetworking getDefaultInstance() {
		return INSTANCE;
	}

	private BatchNetworking() {

		// initialize priority queue
		groupPriorityQueue = new GroupPriorityQueue();
	}

	DBManager getDBManagerInstance() throws Exception {
		Log.i(TAG, "In getDBManagerInstance dbInstance = " + dbInstance);
		if (dbInstance == null) {
			if (null == getApplicationContext()) {
				throw new Exception("initialize method not called");
			}
			dbInstance = new DBManager(getApplicationContext());
			
			Log.i(TAG, "In getDBManagerInstance get dbInstance = " + dbInstance);
			// load unsynced data
			dbInstance.loadCachedDataInBatchNetworkingInstance(this);
		}
		return dbInstance;
	}

	public void setGroupDataHandler(GroupDataHandler groupDataHandler) {
		Group group = new Group(groupDataHandler);
		Log.i(TAG,
				"Setting group handler for group id = "
						+ groupDataHandler.getGroupId());
		groupPriorityQueue.addGroup(group, groupDataHandler.getGroupId());
	}

	/**
	 * Push object for the groupd id. Internally it creates the data object and
	 * calls the other overloaded method
	 * 
	 * @param dataToPush
	 *            It could be any object
	 * @param groupId
	 *            Identifier of the group
	 */
	public void pushDataForGroupId(Object dataToPush, String groupId) {
		this.pushDataForGroupId(new Data(dataToPush), groupId);
	}

	/**
	 * 
	 * @param dataToPush
	 * @param groupId
	 * @throws IllegalArgumentException
	 *             This exception is thrown if no GroupDataHandler was supplied
	 *             for this group id
	 */
	public void pushDataForGroupId(Data dataToPush, String groupId)
			throws IllegalArgumentException {
		Group group = groupPriorityQueue.getGroupForGroupId(groupId);
		if (null == group) {
			Log.i(TAG, "No group found for the id " + groupId);
			// A group is created as soon as a GroupDataHandler is provided for
			// the group
			throw new IllegalArgumentException(
					"No data handler found for groupId " + groupId);
		}
		group.push(dataToPush);
	}

	public Context getApplicationContext() {
		Log.i(TAG, "In getApplicationContext, applicationContext = "
				+ applicationContext);
		return applicationContext;
	}
}
