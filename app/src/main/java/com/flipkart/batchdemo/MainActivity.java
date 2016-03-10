package com.flipkart.batchdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.flipkart.batching.Batch;
import com.flipkart.batching.BatchManager;
import com.flipkart.batching.BatchingStrategy;
import com.flipkart.batching.data.Tag;
import com.flipkart.batching.data.TagData;
import com.flipkart.batching.listener.PersistedBatchCallback;
import com.flipkart.batching.listener.PersistedBatchReadyListener;
import com.flipkart.batching.listener.TagBatchReadyListener;
import com.flipkart.batching.persistence.GsonSerializationStrategy;
import com.flipkart.batching.persistence.InMemoryPersistenceStrategy;
import com.flipkart.batching.persistence.SerializationStrategy;
import com.flipkart.batching.persistence.TagBasedPersistenceStrategy;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.strategy.TagBatchingStrategy;
import com.flipkart.batching.strategy.TimeBatchingStrategy;

import java.io.File;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static String DEBUG_LOGGER_GROUPID = "debug";
    public final static String PERF_LOGGER_GROUPID = "perf";
    public final static String DG_LOGGER_GROUPID = "dg";

    public Tag debugTag, perfTag, dgTag;
    public BatchManager batchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        HandlerThread handlerThread = new HandlerThread("bg");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        SerializationStrategy<TagData, TagBatchingStrategy.TagBatch<TagData>> serializationStrategy = new GsonSerializationStrategy<>();

        debugTag = new Tag(DEBUG_LOGGER_GROUPID);
        perfTag = new Tag(PERF_LOGGER_GROUPID);
        dgTag = new Tag(DG_LOGGER_GROUPID);


        InMemoryPersistenceStrategy<TagData> prefInMemoryPersistenceStrategy = new TapePersistenceStrategy<>(new File(getCacheDir(), "pe"), serializationStrategy);
        TagBasedPersistenceStrategy<TagData> prefTagBatchingPersistence = new TagBasedPersistenceStrategy<>(perfTag, prefInMemoryPersistenceStrategy);
        BatchingStrategy<TagData, Batch<TagData>> prefSizeBatchingStrategy = new SizeBatchingStrategy(2, prefTagBatchingPersistence);

        InMemoryPersistenceStrategy<TagData> debugInMemoryPersistenceStrategy = new TapePersistenceStrategy<>(new File(getCacheDir(), "de"), serializationStrategy);
        TagBasedPersistenceStrategy<TagData> debugTagBatchingPersistence = new TagBasedPersistenceStrategy<>(debugTag, debugInMemoryPersistenceStrategy);
        BatchingStrategy<TagData, Batch<TagData>> debugTimeBatchingStrategy = new TimeBatchingStrategy(5000, debugTagBatchingPersistence);

        InMemoryPersistenceStrategy<TagData> dgInMemoryPersistenceStrategy = new TapePersistenceStrategy<>(new File(getCacheDir(), "dg"), serializationStrategy);
        TagBasedPersistenceStrategy<TagData> dgTagBatchingPersistence = new TagBasedPersistenceStrategy<>(dgTag, dgInMemoryPersistenceStrategy);
        BatchingStrategy<TagData, Batch<TagData>> dgTimeBatchingStrategy = new SizeBatchingStrategy(2, dgTagBatchingPersistence);


        final TagBatchingStrategy<TagData> tagBatchingStrategy = new TagBatchingStrategy<>();
        tagBatchingStrategy.addTagStrategy(perfTag, prefSizeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(debugTag, debugTimeBatchingStrategy);
        tagBatchingStrategy.addTagStrategy(dgTag, dgTimeBatchingStrategy);

        File perfFile = new File(getCacheDir(), "perf");
        File debugFile = new File(getCacheDir(), "debug");
        File dgFile = new File(getCacheDir(), "dg");

        final PersistedBatchReadyListener perfListener = new PersistedBatchReadyListener<>(perfFile, serializationStrategy, backgroundHandler, null);
        perfListener.setListener(new PersistedBatchCallback() {
            @Override
            public void onPersistFailure(Batch batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(Batch batch) {
                //SystemClock.sleep(2000);
                perfListener.finish(batch);
                Log.e("Perf", "Finish Called");
            }

            @Override
            public void onFinish() {

            }
        });

        final PersistedBatchReadyListener debugListener = new PersistedBatchReadyListener<>(debugFile, serializationStrategy, backgroundHandler, null);
        debugListener.setListener(new PersistedBatchCallback<TagBatchingStrategy.TagBatch<TagData>>() {
            @Override
            public void onPersistFailure(TagBatchingStrategy.TagBatch<TagData> batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(TagBatchingStrategy.TagBatch<TagData> batch) {
                // SystemClock.sleep(2000);
                debugListener.finish(batch);
                Log.e("Debug", "Finish Called");
            }

            @Override
            public void onFinish() {

            }
        });

        final PersistedBatchReadyListener dgListener = new PersistedBatchReadyListener<>(dgFile, serializationStrategy, backgroundHandler, null);
        dgListener.setListener(new PersistedBatchCallback<TagBatchingStrategy.TagBatch<TagData>>() {
            @Override
            public void onPersistFailure(TagBatchingStrategy.TagBatch<TagData> batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(TagBatchingStrategy.TagBatch<TagData> batch) {
                //SystemClock.sleep(2000);
                dgListener.finish(batch);
                Log.e("Dg", "Finish Called");
            }

            @Override
            public void onFinish() {

            }
        });

        TagBatchReadyListener<TagData> tagBatchReadyListener = new TagBatchReadyListener<>();
        tagBatchReadyListener.addListenerForTag(perfTag, perfListener);
        tagBatchReadyListener.addListenerForTag(debugTag, debugListener);
        tagBatchReadyListener.addListenerForTag(dgTag, dgListener);

        batchManager = new BatchManager.Builder<>().setBatchingStrategy(tagBatchingStrategy)
                .setSerializationStrategy(serializationStrategy)
                .setHandler(backgroundHandler)
                .setOnBatchReadyListener(tagBatchReadyListener)
                .build(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                batchManager.addToBatch(Collections.singleton(new TagData(perfTag)));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Log.d("db size", persistenceStrategy.getData().size() + "");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            batchManager.addToBatch(Collections.singleton(new TagData(perfTag)));

        } else if (id == R.id.nav_gallery) {
            batchManager.addToBatch(Collections.singleton(new TagData(dgTag)));

        } else if (id == R.id.nav_slideshow) {
            batchManager.addToBatch(Collections.singleton(new TagData(debugTag)));

        } else if (id == R.id.nav_manage) {
            batchManager.addToBatch(Collections.singleton(new TagData(perfTag)));

        } else if (id == R.id.nav_share) {
            batchManager.addToBatch(Collections.singleton(new TagData(dgTag)));

        } else if (id == R.id.nav_send) {
            batchManager.addToBatch(Collections.singleton(new TagData(perfTag)));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
