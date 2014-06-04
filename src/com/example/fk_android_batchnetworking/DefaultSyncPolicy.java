package com.example.fk_android_batchnetworking;

import android.util.Log;

public class DefaultSyncPolicy implements GroupSyncPolicy {
	private static final String TAG = "DefaultSyncPolicy";
	private final int DEFAULT_BATCH_SIZE = 15;
	private final int DEFAULT_UNSYNCED_IDLE_TIME_IN_SECONDS = 120;

	@Override
	public boolean elegibleForSyncing(Group group) {
		if (group.getCurrentSyncState() != Group.NOT_SYNCED)
			return false;

		// check time based eligibility
		boolean eligible = ((System.currentTimeMillis() - group
				.getLastSyncTryTime()) > (syncIdleTime() * 1000))
				&& (group.size() > 0);
		Log.i(TAG, "eligible based on time = " + eligible);

		if (!eligible) {
			// check number based eligibility
			eligible = (group.size() >= syncBatchSize());
			Log.i(TAG, "eligible based on number = " + eligible);
		}
		// Log.i(TAG,
		// "Returning from elegibleForSyncing: eligible = "+eligible);
		return eligible;
	}

	@Override
	public int syncBatchSize() {
		return DEFAULT_BATCH_SIZE;
	}

	@Override
	public int syncIdleTime() {
		return DEFAULT_UNSYNCED_IDLE_TIME_IN_SECONDS;
	}

}
