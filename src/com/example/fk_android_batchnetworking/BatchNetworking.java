package com.example.fk_android_batchnetworking;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class BatchNetworking {

	private final static BatchNetworking INSTANCE = new BatchNetworking();
	private GroupPriorityQueue groupPriorityQueue;
	private Context applicationContext;
    private RequestQueue requestQueue;

	public RequestQueue getRequestQueue() throws Exception {
		if (requestQueue == null) {
			if (null == getApplicationContext())
			{
				throw new Exception("Application context not set");
			}
			requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
		return requestQueue;
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

		// load unsynced data
		DBManager dbInstance = DBManager.getInstance();
		dbInstance.loadCachedDataInBatchNetworkingInstance(this);
	}

	public void setGroupDataHandler(GroupDataHandler groupDataHandler) {
		Group group = new Group(groupDataHandler);
		groupPriorityQueue.addGroup(group,
				groupDataHandler.getGroupId());
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
			// A group is created as soon as a GroupDataHandler is provided for
			// the group
			throw new IllegalArgumentException(
					"No data handler found for groupId " + groupId);
		}
		group.push(dataToPush);
	}

	public Context getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(Context applicationContext) {
		this.applicationContext = applicationContext;
	}
}
