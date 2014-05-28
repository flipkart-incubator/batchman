package com.example.fk_android_batchnetworking;

public class Data {

	public static enum DataCacheState {
		CSTATE_NOT_CACHED,
	    CSTATE_CACHED,
	    CSTATE_DONOT_CACHE
	}

	private long eventId;
	private Object data;
	private long expiry;
	private DataCacheState cacheState;

	public Data() {
		setEventId(System.currentTimeMillis() + System.nanoTime());
        setCacheState(DataCacheState.CSTATE_NOT_CACHED);
	}
	
	public Data(Object dataToPush) {
		setEventId(System.currentTimeMillis() + System.nanoTime());
		setData(dataToPush);
        setCacheState(DataCacheState.CSTATE_NOT_CACHED);
	}

	public DataCacheState getCacheState() {
		return cacheState;
	}

	public Object getData() {
		return data;
	}

	public long getEventId() {
		return eventId;
	}

	public long getExpiry() {
		return expiry;
	}

	public void setCacheState(DataCacheState cacheState) {
		this.cacheState = cacheState;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

}
