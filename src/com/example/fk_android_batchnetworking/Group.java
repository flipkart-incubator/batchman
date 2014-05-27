package com.example.fk_android_batchnetworking;

import java.util.ArrayList;

import com.example.fk_android_batchnetworking.Data.DataCacheState;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class Group {

	enum DataSyncState {
		NOT_SYNCED, QUEUED_FOR_SYNCING, SYNC_SUCCESSFUL, SYNC_FAILED
	}

	private long lastSyncTryTime;
	private GroupDataHandler batchDataHandler;
	private ArrayList<Data> groupData;

	private DataSyncState _syncState;

	int numberOfRecordsSentForSync;

	Handler groupHandler = null;
	ArrayList<Data> currentDataForSyncing = null;

	public Group(GroupDataHandler batchDataHandler) {
		setLastSyncTryTime(System.currentTimeMillis());
		this.batchDataHandler = batchDataHandler;
		groupData = new ArrayList<Data>();
		_syncState = DataSyncState.NOT_SYNCED;

		HandlerThread thread = new HandlerThread("GroupHandler");
		thread.start();
		groupHandler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				DataSyncState state = DataSyncState.values()[msg.what];
				switch (state) {
				case SYNC_SUCCESSFUL:
					if (null != currentDataForSyncing) {
						for (int i = 0; i < currentDataForSyncing.size(); i++) {
							Data data = currentDataForSyncing.get(i);
							groupData.remove(data);
							
							 if (data.getCacheState() == Data.DataCacheState.CSTATE_CACHED) {
								 DBManager.getInstance().removeData(data);
							 }
						}
						_syncState = DataSyncState.NOT_SYNCED;
						currentDataForSyncing.clear();
					}
					BatchNetworking.getDefaultInstance().getGroupPriorityQueue().getNotificationHandler().sendEmptyMessage(GroupPriorityQueue.NOTIFICATION_SYNC_SUCCESSFUL);
					break;
				case SYNC_FAILED:
					_syncState = DataSyncState.NOT_SYNCED;
					currentDataForSyncing.clear();
					BatchNetworking.getDefaultInstance().getGroupPriorityQueue().getNotificationHandler().sendEmptyMessage(GroupPriorityQueue.NOTIFICATION_SYNC_FAILED);
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

	public DataSyncState getCurrentSyncState() {
		return _syncState;
	}

	public synchronized boolean handleSyncPoke() {
		if (batchDataHandler.getSyncPolicy().elegibleForSyncing(this)) {
			_syncState = DataSyncState.QUEUED_FOR_SYNCING;
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
				.syncBatchSize();
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

		// send the batch for syncing
		try {
			batchDataHandler.syncBatch(currentDataForSyncing, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			for (int i = 0; i < numberOfElementsToRemove; i++) {
				Data data = groupData.remove(i);

				// remove data from cache
				if (data.getCacheState() == DataCacheState.CSTATE_CACHED) {
					DBManager.getInstance().removeData(data);
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
					DBManager.getInstance().persistBatchDatum(batchDatum,
							batchDataHandler.getGroupId());

					// if eligible for syncing, let people know
					if (batchDataHandler.getSyncPolicy().elegibleForSyncing(
							group)) {
						BatchNetworking
								.getDefaultInstance()
								.getGroupPriorityQueue()
								.getNotificationHandler()
								.sendEmptyMessage(
										GroupPriorityQueue.NOTIFICATION_POKE_ME);
					}
				}
			});
		} else {
			// if eligible for syncing, let people know
			if (batchDataHandler.getSyncPolicy().elegibleForSyncing(this)) {
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
