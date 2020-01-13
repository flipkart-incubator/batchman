# BatchMan [![](https://jitpack.io/v/flipkart-incubator/batchman.svg)](https://jitpack.io/#flipkart-incubator/batchman)

| Branch | Build Status |
|--------|--------------|
| master | [![Build Status](https://travis-ci.org/flipkart-incubator/batchman.svg?branch=master)](https://travis-ci.org/flipkart-incubator/batchman) |
| develop    | [![Build Status](https://travis-ci.org/flipkart-incubator/batchman.svg?branch=develop)](https://travis-ci.org/flipkart-incubator/batchman) |

BatchMan (short for batch manager) is an android library implementation responsible for batching of events based on the configurations done by the client, and giving the batch back to the client.

The library has been written in a more flexible way, so that the client can plugin his own implementations for batching.
* <b>BatchManager</b> : It is the entry point to the library, where in the client will use the instance of the batch manager to push in data to the library for batching.

* <b>BatchingStrategy</b> : It is an interface, where all the batching logic comes in. The library has 4 batching strategies on its own, or the client can implement the interface, and provide his/her own logic for batching.

* <b>PersistenceStrategy</b> : It is an interface, where all the persistence logic comes in. The library has 3 persistence strategies on its own, or the client can provide his/her own persistence layer to persist the events, just to make sure that there is no loss of events (in case of app crash) 

* <b>OnBatchReadyListener</b> : It is a interface, which gives a callback, whenever the batch is ready. The client can consume the batch, and can make a network call to the server. There are various types of OnBatchReadyListener which will be discussed later.

* <b>Data</b> : It is an abstract class, wherein the client will need to extend this class for his events.


Get BatchMan
------------

Add it in your root build.gradle at the end of repositories :

````java
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
````	

Add the dependencies :

* <b>Library</b> :

````java
	dependencies {
	        compile 'com.github.flipkart-incubator.batchman:batching:1.3.9'
	}
````

* <b>GSON Serialization</b> :

````java

	dependencies {
	        compile 'com.github.flipkart-incubator.batchman:batching-gson:1.3.9'
	}
````

How to use
----------

### Step 1 :

Initialize persistence strategy, batching strategy will take persistence strategy as one of it's parameters.

````java

// Using inMemoryPersistenceStrategy
PersistenceStrategy persistenceStrategy = new InMemoryPersistenceStrategy();

````

### Step 2 :

Initialize batching strategy with a max batch size and persistence strategy.

````java

int MAX_BATCH_SIZE = 5;

// Using SizeBatchingStrategy. Whenever the number of events is 5, a batch is formed
SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(MAX_BATCH_SIZE, persistenceStrategy);

````

### Step 3 :

Initialize serialization strategy and background handler thread. To include GsonSerializationStrategy, you must have its dependency in your gradle file. To get dependency, look into the Getting Started section.

````java

// Initialize serialization strategy
SerializationStrategy gsonSerializationStrategy = new GsonSerializationStrategy();

// Handler for doing heavy operations like read/write from disk
HandlerThread handlerThread = new HandlerThread("bg");
handlerThread.start();
Handler backgroundHandler = new Handler(handlerThread.getLooper());

````

### Step 4 :

Build batch manager with all the strategies and handler thread we initialized in previous steps. Batch manger will also take a listener for giving callbacks when a batch is ready.

````java

// Initialize batch manager
BatchManager batchManager = new BatchManager.Builder<>()
       .setBatchingStrategy(sizeBatchingStrategy)
       .setSerializationStrategy(gsonSerializationStrategy)
       .setHandler(backgroundHandler)
       //to enable logging while debug
       .enableLogging()
       .setOnBatchReadyListener(new OnBatchReadyListener() {
           @Override
           public void onReady(BatchingStrategy causingStrategy, Batch batch) {
               //Callback with batch when it's ready
           }
       }).build(this);

````

### Step 5 :

Use addToBatch() for adding events to batch manager.

````java

// Push data to batch manager
batchManager.addToBatch(Collections.singleton(new EventData()));

````

Getting Started
---------------

### [Wiki](https://github.com/Flipkart/fk-android-batchnetworking/wiki)


Dependencies
------------

* For Testing : [JUnit](http://junit.org/), [Roboelectric](http://robolectric.org/), [Mockito](http://mockito.org/)
* For Persistence : [Tape by Square](https://github.com/square/tape)
* For Serialization/Deserialization : [GSON](https://github.com/google/gson)


License
-------

    The Apache License
    
    Copyright (c) 2017 Flipkart Internet Pvt. Ltd.
    
    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License.
    
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0 
       
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    
    See the License for the specific language governing permissions and 
    limitations under the License.

