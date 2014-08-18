fk-android-batchnetworking
==========================

Batch push of data to the server

## Installation

The library is still not available through Maven or Gradle. You need to add BatchNetworking project as a dependency to your project.

## Concept

The library stores the data based on group. Each group has an id, a data handler, a sync policy, a url, sync priority and few other configurations. 

#### Group id
This defines the uniqueness of the group. This is used for setting the data handler and then pushing the data to the library

#### Priority
Sync priority defines the frequency at which the group will be poked for getting a batch of data from the group for syncing.

#### Sync policy
Sync policy defines the instance when the data would be elegible for syncing and the batch size within each group which has to be used in a single sync call. Sync policy is a member of data handler

#### GroupDataHandler
GroupDataHandler knows about the kind of data which is being sent for a group. It knows about the data and hence handles the task of batch creation from the data group. It maintains the data group, the maximum size of which is defined by it's property ```maxBatchSize```. You can change this to suite your needs. When the data overflows this value, the first-in data is purged in the quantum defined by the property ```elementCountToDeleteOnBatchFull```.

## Example: JSON batching with default values

#### Define the name for the kind of data group you would be pushing
	String performanceEvent = @"perf";

#### Add a GroupDataHandler for the group

An object of a child class of ```GroupDataHandler``` has to be provided for each group. This also asks for the URL to which the data of the group has to be synced

	BatchNetworking.getDefaultInstance().initialize(getApplicationContext());
	try {
		JSONDataHandler jdh = new JSONDataHandler(PERFORMANCE_EVENTS, BASE_URL_STRING);
		BatchNetworking.getDefaultInstance().setGroupDataHandler(jdh);
	} catch (Exception e) {
		e.printStackTrace();
	}

JSONDataHandler uses ```DefaultSyncPolicy``` for syncing. You can set your own sync policy.
You can also set the priority of the group in the data policy. You can set the ```User-Agent``` by using the following method:

	public void setUserAgent(String userAgent)

#### Push data with groupid

	BatchNetworking.getDefaultInstance().pushDataForGroupId(element, PERFORMANCE_EVENTS);

Since you have used JSONDataHandler the ```element``` mentioned in the above method has to be of the type or the child type of ```com.google.mygson.JsonElement```

## Custom data type

You can implement ```GroupDataHandler``` and implement ```DataTypeSyncPolicy``` for custom handling.

## Author

Mudit Krishna Mathur, mudit.mathur@flipkart.com

## License

BatchNetworking is available under the MIT license. See the LICENSE file for more info.
