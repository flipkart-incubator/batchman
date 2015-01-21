package com.flipkart.fk_android_batchnetworking;

public class GroupSyncPolicy {

    private int syncBatchSize;
    private int syncIdleTime;
    private final int DEFAULT_BATCH_SIZE = 15;
    private final int DEFAULT_UNSYNCED_IDLE_TIME_IN_SECONDS = 120;

    public GroupSyncPolicy(int syncBatchSize,int syncIdleTime) {
        setDefaultValues();
        if(syncIdleTime > 0)
            setSyncIdleTime(syncIdleTime);
        if (syncBatchSize > 0)
            setSyncBatchSize(syncBatchSize);
    }

    public GroupSyncPolicy() {
      setDefaultValues();
    }

    public boolean eligibleForSyncing(Group group) {
        if (group.getCurrentSyncState() != Group.NOT_SYNCED)
            return false;

        // check time based eligibility
        boolean eligible = ((System.currentTimeMillis() - group
                .getLastSyncTryTime()) > (getSyncIdleTime() * 1000))
                && (group.size() > 0);

        if (!eligible) {
            // check number based eligibility
            eligible = (group.size() >= getSyncBatchSize());
        }
        return eligible;
    }

    public int getSyncBatchSize() {
        return syncBatchSize;
    }

    public void setSyncBatchSize(int syncBatchSize) {
        this.syncBatchSize = syncBatchSize;
    }

    public int getSyncIdleTime() {
        return syncIdleTime;
    }

    public void setSyncIdleTime(int syncIdleTime) {
        this.syncIdleTime = syncIdleTime;
    }

    private void setDefaultValues() {
        setSyncIdleTime(DEFAULT_UNSYNCED_IDLE_TIME_IN_SECONDS);
        setSyncBatchSize(DEFAULT_BATCH_SIZE);
    }
}
