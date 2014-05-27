package com.example.fk_android_batchnetworking;


public class DBManager {
	private final static DBManager INSTANCE = new DBManager();
	
	public static DBManager getInstance() {
		return INSTANCE;
	}
	
	private DBManager() {
	}

	public void loadCachedDataInBatchNetworkingInstance(
			BatchNetworking batchNetworking) {
		// TODO implement the method
		
	}

	public void persistBatchDatum(Data batchDatum, String groupId) {
		
		// TODO implement the method
		
	}

	public void removeData(Data data) {
		// TODO implement the method
		
	}
}
