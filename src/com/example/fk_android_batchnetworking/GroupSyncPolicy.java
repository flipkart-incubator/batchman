package com.example.fk_android_batchnetworking;

public interface GroupSyncPolicy {

	public boolean elegibleForSyncing(Group group);
	public int syncBatchSize();
	public int syncIdleTime();
	
}
