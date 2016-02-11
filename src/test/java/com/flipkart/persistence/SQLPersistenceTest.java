package com.flipkart.persistence;

import android.content.Context;
import android.os.Handler;

import com.flipkart.data.Data;
import com.flipkart.data.EventData;
import com.flipkart.data.Tag;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by anirudh.r on 11/02/16.
 */
public class SQLPersistenceTest {

    private PersistenceStrategy persistenceStrategy;

    @Mock
    private Data eventData;
    @Mock
    private SerializationStrategy serializationStrategy;
    @Mock
    private Context context;
    @Mock
    private Handler handler;
    @Mock
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() {
        persistenceStrategy = new SQLPersistenceStrategy(serializationStrategy, context, handler);
    }

    /**
     * Test to verify that data is retained in InMemoryList after {@link PersistenceStrategy#add(Collection)} is called.
     */
    @Test
    public void testIfDataInMemory() {
        Data eventData = new EventData(new Tag("1"), "e1");
        Data eventData1 = new EventData(new Tag("2"), "e2");
        Data eventData2 = new EventData(new Tag("3"), "e3");
        Data eventData3 = new EventData(new Tag("4"), "e4");
        Data eventData4 = new EventData(new Tag("5"), "e5");

        ArrayList<Data> data = new ArrayList<>();
        data.add(eventData);
        data.add(eventData1);
        data.add(eventData2);
        data.add(eventData3);
        data.add(eventData4);
        persistenceStrategy.add(data);

        Assert.assertEquals(persistenceStrategy.getData().size(), data.size());
    }
}
