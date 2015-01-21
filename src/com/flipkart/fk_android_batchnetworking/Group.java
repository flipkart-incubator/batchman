package com.flipkart.fk_android_batchnetworking;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flipkart.fk_android_batchnetworking.Data.DataCacheState;

import java.util.ArrayList;

public class Group {
	private static final String TAG = "Group";
	public static final int NOT_SYNCED = 0;
	public static final int QUEUED_FOR_SYNCING = 1;
	public static final int SYNC_SUCCESSFUL = 2;
	public static final int SYNC_FAILED = 3;

	private long lastSyncTryTime;
	private GroupDataHandler batchDataHandler;
	private ArrayList<Data> groupData;

	private int _syncState;

	int numberOfRecordsSentForSync;

	Handler groupHandler = null;
	ArrayList<Data> currentDataForSyncing = null;

	public Group(GroupDataHandler batchDataHandler) {
		setLastSyncTryTime(System.currentTimeMillis());
		this.batchDataHandler = batchDataHandler;
		groupData = new ArrayList<Data>();
		_syncState = NOT_SYNCED;

		HandlerThread thread = new HandlerThread("GroupHandler");
		thread.setPriority(Thread.NORM_PRIORITY - 2);
		thread.start();
		groupHandler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SYNC_SUCCESSFUL:
					if (null != currentDataForSyncing) {
						for (int i = 0; i < currentDataForSyncing.size(); i++) {
							Data data = currentDataForSyncing.get(i);
							groupData.remove(data);

							if (data.getCacheState() == Data.DataCacheState.CSTATE_CACHED) {
								try {
									BatchNetworking.getDefaultInstance()
											.getDBManagerInstance()
											.removeData(data.getEventId());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						_syncState = NOT_SYNCED;
						currentDataForSyncing.clear();
					}
					BatchNetworking
							.getDefaultInstance()
							.getGroupPriorityQueue()
							.getNotificationHandler()
							.sendEmptyMessage(
									GroupPriorityQueue.NOTIFICATION_SYNC_SUCCESSFUL);
					break;
				case SYNC_FAILED:
					_syncState = NOT_SYNCED;
					if (null != currentDataForSyncing)
						currentDataForSyncing.clear();
					BatchNetworking
							.getDefaultInstance()
							.getGroupPriorityQueue()
							.getNotificationHandler()
							.sendEmptyMessage(
									GroupPriorityQueue.NOTIFICATION_SYNC_FAILED);
					break;
				default:
					break;
				}
			}
		};
	}

	public GroupDataHandler getBatchDataHandler() {
		return batchDataHandler;
	}

	public int getCurrentSyncState() {
		return _syncState;
	}

	public synchronized boolean handleSyncPoke() {
		if (batchDataHandler.getSyncPolicy().eligibleForSyncing(this)) {
			_syncState = QUEUED_FOR_SYNCING;
			groupHandler.post(new Runnable() {
				@Override
				public void run() {
					sync();
				}
			});
			return true;
		}
		return false;
	}

	private void sync() {

		// get a batch for syncing
		int numOfElementsToSync = batchDataHandler.getSyncPolicy()
				.getSyncBatchSize();
		if (numOfElementsToSync > groupData.size()) {
			numOfElementsToSync = groupData.size();
		}

		if (currentDataForSyncing == null)
			currentDataForSyncing = new ArrayList<Data>();
		else
			currentDataForSyncing.clear();
		for (int index = 0; index < numOfElementsToSync; index++) {
			currentDataForSyncing.add(groupData.get(index));
		}
		lastSyncTryTime = System.currentTimeMillis();

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				groupHandler.sendEmptyMessage(SYNC_SUCCESSFUL);
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				// if (error.networkResponse != null) {
				groupHandler.sendEmptyMessage(SYNC_FAILED);
				// }
			}
		};

		// send the batch for syncing
		try {
			batchDataHandler.syncBatch(currentDataForSyncing, listener,
					errorListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getLastSyncTryTime() {
		return lastSyncTryTime;
	}

	private void normalizeData() {
		if (size() > batchDataHandler.getMaxBatchSize()) {
			int numberOfElementsToRemove = batchDataHandler
					.getElementCountToDeleteOnBatchFull();
			if(numberOfElementsToRemove>size())
            {
                numberOfElementsToRemove = size() - batchDataHandler.getMaxBatchSize();
            }
			for (int i = 0; i < numberOfElementsToRemove; i++) {
				Data data = groupData.remove(0);

				// remove data from cache
				if (data.getCacheState() == DataCacheState.CSTATE_CACHED) {
					try {
						BatchNetworking.getDefaultInstance()
								.getDBManagerInstance()
								.removeData(data.getEventId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void push(final Data batchDatum) {
		if (null == batchDatum)
			return;

		normalizeData();
		groupData.add(batchDatum);

		if (batchDatum.getCacheState() == Data.DataCacheState.CSTATE_NOT_CACHED) {
			batchDatum.setCacheState(Data.DataCacheState.CSTATE_CACHED);
			final Group group = this;
			groupHandler.post(new Runnable() {
				@Override
				public void run() {
					// persist in db
					try {
						BatchNetworking
								.getDefaultInstance()
								.getDBManagerInstance()
								.persistData(
										batchDatum.getEventId(),
										batchDataHandler.getGroupId(),
										batchDataHandler
												.serializeIndividualData(batchDatum
														.getData()),
										batchDatum.getExpiry());
					} catch (Exception e) {
						Log.i(TAG, "Error in persisting data");
						e.printStackTrace();
					}

					// if eligible for syncing, let people know
					if (batchDataHandler.getSyncPolicy().eligibleForSyncing(
							group)) {
						BatchNetworking
								.getDefaultInstance()
								.getGroupPriorityQueue()
								.getNotificationHandler()
								.sendEmptyMessage(
										GroupPriorityQueue.NOTIFICATION_POKE_ME);
					} else {
						// Log.i(TAG, "Not elegible for syncing");
					}
				}
			});
		} else {
			// if eligible for syncing, let people know
			if (batchDataHandler.getSyncPolicy().eligibleForSyncing(this)) {
				BatchNetworking
						.getDefaultInstance()
						.getGroupPriorityQueue()
						.getNotificationHandler()
						.sendEmptyMessage(
								GroupPriorityQueue.NOTIFICATION_POKE_ME);
			}
		}
	}

	public void setBatchDataHandler(GroupDataHandler batchDataHandler) {
		this.batchDataHandler = batchDataHandler;
	}

	public void setLastSyncTryTime(long lastSyncTryTime) {
		this.lastSyncTryTime = lastSyncTryTime;
	}

	public int size() {
		return groupData.size();
	}

}
