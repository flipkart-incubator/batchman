#fk-android-batching
Batching is an android library implementation responsible for batching of events based on the configurations done by the client, and giving the batch back to the client.

The library has been written in a more flexible way, so that the client can plugin his own implementations for batching.
* <b>BatchManager</b> : It is the entry point to the library, where in the client will use the instance of the batch manager to push in data to the library for batching.

* <b>BatchingStrategy</b> : It is an interface, where all the batching logic comes in. The library has 4 batching strategies on its own, or the client can implement the interface, and provide his/her own logic for batching.

* <b>PersistenceStrategy</b> : It is an interface, where all the persistence logic comes in. The library has 3 persistence strategies on its own, or the client can provide his/her own persistence layer to persist the events, just to make sure that there is no loss of events (in case of app crash) 

* <b>OnBatchReadyListener</b> : It is a interface, which gives a callback, whenever the batch is ready. The client can consume the batch, and can make a network call to the server. There are various types of OnBatchReadyListener which will be discussed later.

* <b>Data</b> : It is an abstract class, wherein the client will need to extend this class for his events.



###Sample Code

````

      int MAX_BATCH_SIZE = 5;
      
      //using inMemoryPersistenceStrategy
      PersistenceStrategy persistenceStrategy = new InMemoryPersistenceStrategy();
        
      //using sizeBatchingStrategy. Whenever the number of events is 5, a batch is formed
      SizeBatchingStrategy sizeBatchingStrategy = new SizeBatchingStrategy(MAX_BATCH_SIZE, persistenceStrategy);
        
      GsonSerializationStrategy gsonSerializationStrategy = new GsonSerializationStrategy();
      
      //Handler for all operations
      HandlerThread handlerThread = new HandlerThread("bg");
      handlerThread.start();
      Handler backgroundHandler = new Handler(handlerThread.getLooper());

      BatchManager batchManager = new BatchManager.Builder<>()
              .setBatchingStrategy(sizeBatchingStrategy)
              .setSerializationStrategy(gsonSerializationStrategy)
              .setHandler(backgroundHandler)
              .setOnBatchReadyListener(new OnBatchReadyListener() {
                  @Override
                  public void onReady(BatchingStrategy causingStrategy, Batch batch) {
                      //Callback that the batch is ready
                  }
              }).build(this);

      //push in data to the library
      batchManager.addToBatch(Collections.singleton(new EventData()));
        
````



###Getting Started 

Gradle Dependency : ````compile 'com.flipkart.android.batching:batching:0.9.1.2'````



###[Wiki](www.google.com)



###Dependencies

* For Testing : [JUnit](http://junit.org/), [Roboelectric](http://robolectric.org/), [Mockito](http://mockito.org/)
* For logging : [Slf4J](http://www.slf4j.org/)
* For Persistence : [Tape by Square](https://github.com/square/tape)
* For Serialization/Deserialization : [GSON](https://github.com/google/gson)



###License

fk-android-batching is available under the [MIT](https://opensource.org/licenses/MIT) license.

