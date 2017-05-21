package com.flipkart.batching.flatbuffer;

import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.Data;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.SizeBatch;
import com.flipkart.batching.core.batch.SizeTimeBatch;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.batch.TimeBatch;
import com.flipkart.batching.core.data.EventData;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.flatbuffer.fbs.Tag;
import com.google.flatbuffers.FlatBufferBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FlatBufferSerializationStrategy implements SerializationStrategy<Data, Batch> {

    private FlatBufferParser<Data> dataTypeParser;
    private static final FlatBufferParser<Data> DEFAULT_DATA_PARSER = new FlatBufferParser<Data>() {
        @Override
        public byte[] serialize(Data data) {
            FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
            int tag = -1;
            //if data is TagData
            if (data instanceof TagData) {
                tag = Tag.createTag(flatBufferBuilder, flatBufferBuilder.createString(((TagData) data).getTag().getId()));
            }
            com.flipkart.batching.flatbuffer.fbs.Data.startData(flatBufferBuilder);
            com.flipkart.batching.flatbuffer.fbs.Data.addEventId(flatBufferBuilder, data.getEventId());
            if (tag != -1) {
                com.flipkart.batching.flatbuffer.fbs.Data.addTag(flatBufferBuilder, tag);
            }
            int finishOffset = com.flipkart.batching.flatbuffer.fbs.Data.endData(flatBufferBuilder);
            flatBufferBuilder.finish(finishOffset);
            return flatBufferBuilder.sizedByteArray();
        }

        @Override
        public Data deserialize(byte[] bytes) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            com.flipkart.batching.flatbuffer.fbs.Data rootAsData = com.flipkart.batching.flatbuffer.fbs.Data.getRootAsData(wrap);

            if (rootAsData.tag() != null) {
                TagData tagData = new TagData(new com.flipkart.batching.core.data.Tag(rootAsData.tag().id()));
                tagData.setEventId(rootAsData.eventId());
                return tagData;
            }

            EventData eventData = new EventData();
            eventData.setEventId(rootAsData.eventId());
            return eventData;
        }
    };

    private FlatBufferParser<Batch> batchTypeParser;
    private static final FlatBufferParser<Batch> DEFAULT_BATCH_PARSER = new FlatBufferParser<Batch>() {
        @Override
        public byte[] serialize(Batch batch) {
            FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();

            Collection<Data> dataCollection = batch.getDataCollection();
            int[] d = new int[dataCollection.size()];
            for (int i = 0; i < dataCollection.size(); i++) {
                Data data = (Data) dataCollection.toArray()[i];
                int tagOffest = 0;
                //if data is tagdata
                if (data instanceof TagData) {
                    tagOffest = Tag.createTag(flatBufferBuilder, flatBufferBuilder.createString(((TagData) data).getTag().getId()));
                }

                int dataOffset = com.flipkart.batching.flatbuffer.fbs.Data.createData(flatBufferBuilder, data.getEventId(), tagOffest);
                d[i] = dataOffset;
            }

            int dataCollectionVector = com.flipkart.batching.flatbuffer.fbs.Batch.createDataCollectionVector(flatBufferBuilder, d);

            int pos = -1;
            if (batch instanceof TagBatch) {
                int string = flatBufferBuilder.createString(((TagBatch) batch).getTag().getId());
                pos = Tag.createTag(flatBufferBuilder, string);
            }

            com.flipkart.batching.flatbuffer.fbs.Batch.startBatch(flatBufferBuilder);

            //batch is sizebatch
            if (batch instanceof SizeBatch) {
                com.flipkart.batching.flatbuffer.fbs.Batch.addMaxBatchSize(flatBufferBuilder, ((SizeBatch) batch).getMaxBatchSize());
            }

            //batch is timebatch
            if (batch instanceof TimeBatch) {
                com.flipkart.batching.flatbuffer.fbs.Batch.addMaxBatchSize(flatBufferBuilder, ((TimeBatch) batch).getTimeOut());
            }

            //batch is sizeTimeBatch
            if (batch instanceof SizeTimeBatch) {
                com.flipkart.batching.flatbuffer.fbs.Batch.addMaxBatchSize(flatBufferBuilder, ((SizeTimeBatch) batch).getMaxBatchSize());
                com.flipkart.batching.flatbuffer.fbs.Batch.addMaxBatchSize(flatBufferBuilder, ((SizeTimeBatch) batch).getTimeOut());
            }

            if (pos != -1) {
                com.flipkart.batching.flatbuffer.fbs.Batch.addTag(flatBufferBuilder, pos);
            }

            com.flipkart.batching.flatbuffer.fbs.Batch.addDataCollection(flatBufferBuilder, dataCollectionVector);
            int finishOffest = com.flipkart.batching.flatbuffer.fbs.Batch.endBatch(flatBufferBuilder);
            flatBufferBuilder.finish(finishOffest);

            return flatBufferBuilder.sizedByteArray();
        }

        @Override
        public Batch deserialize(byte[] bytes) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            com.flipkart.batching.flatbuffer.fbs.Batch rootAsBatch = com.flipkart.batching.flatbuffer.fbs.Batch.getRootAsBatch(wrap);

            int dataCollectionLength = rootAsBatch.dataCollectionLength();
            Collection<Data> dataCollection = new ArrayList<>();

            for (int i = 0; i < dataCollectionLength; i++) {
                com.flipkart.batching.flatbuffer.fbs.Data data = rootAsBatch.dataCollection(i);
                Data data1 = new EventData();
                data1.setEventId(data.eventId());
                dataCollection.add(data1);
            }

            Batch finalBatch = null;

            if (rootAsBatch.maxTimeout() != 0) {
                finalBatch = new TimeBatch(dataCollection, rootAsBatch.maxTimeout());
            } else if (rootAsBatch.maxBatchSize() != 0) {
                finalBatch = new SizeBatch(dataCollection, (int) rootAsBatch.maxBatchSize());
            } else if (rootAsBatch.maxBatchSize() != 0 && rootAsBatch.maxTimeout() != 0) {
                finalBatch = new SizeTimeBatch(dataCollection, (int) rootAsBatch.maxBatchSize(), rootAsBatch.maxTimeout());
            }

            if (rootAsBatch.tag() != null) {
                String id = rootAsBatch.tag().id();
                com.flipkart.batching.core.data.Tag tag = new com.flipkart.batching.core.data.Tag(id);
                finalBatch = new TagBatch(tag, finalBatch);
            }

            return finalBatch;
        }
    };

    public FlatBufferSerializationStrategy(FlatBufferParser<Data> dataTypeParser, FlatBufferParser<Batch> batchTypeParser) {
        this.dataTypeParser = dataTypeParser;
        this.batchTypeParser = batchTypeParser;
    }

    public FlatBufferSerializationStrategy() {
        this.dataTypeParser = DEFAULT_DATA_PARSER;
        this.batchTypeParser = DEFAULT_BATCH_PARSER;
    }

    @Override
    public void build() {

    }

    @Override
    public byte[] serializeData(Data data) throws IOException {
        return dataTypeParser.serialize(data);
    }

    @Override
    public byte[] serializeCollection(Collection<Data> collection) throws IOException {
        return new byte[0];
    }

    @Override
    public byte[] serializeBatch(Batch batch) throws IOException {
        return batchTypeParser.serialize(batch);
    }

    @Override
    public Data deserializeData(byte[] bytes) throws IOException {
        return dataTypeParser.deserialize(bytes);
    }

    @Override
    public Collection<Data> deserializeCollection(byte[] bytes) throws IOException {
        return null;
    }

    @Override
    public Batch deserializeBatch(byte[] bytes) throws IOException {
        return batchTypeParser.deserialize(bytes);
    }
}