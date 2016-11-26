/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Flipkart Internet Pvt. Ltd.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

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
import android.webkit.ValueCallback;

import com.flipkart.batching.TagBatchManager;
import com.flipkart.batching.listener.NetworkPersistedBatchReadyListener;
import com.flipkart.batching.listener.PersistedBatchCallback;
import com.flipkart.batching.listener.TrimmedBatchCallback;
import com.flipkart.batching.persistence.TapePersistenceStrategy;
import com.flipkart.batching.strategy.SizeBatchingStrategy;
import com.flipkart.batching.core.Batch;
import com.flipkart.batching.core.SerializationStrategy;
import com.flipkart.batching.core.batch.TagBatch;
import com.flipkart.batching.core.data.Tag;
import com.flipkart.batching.core.data.TagData;
import com.flipkart.batching.gson.GsonSerializationStrategy;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public final static String DEBUG_LOGGER_GROUPID = "debug";
    public final static String PERF_LOGGER_GROUPID = "perf";
    public final static String DG_LOGGER_GROUPID = "dg";
    public Tag debugTag, perfTag, dgTag;
    public TagBatchManager batchManager;
    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        HandlerThread handlerThread = new HandlerThread("bg");
        handlerThread.start();
        Handler backgroundHandler = new Handler(handlerThread.getLooper());

        SerializationStrategy serializationStrategy = new GsonSerializationStrategy<>();
        serializationStrategy.registerDataType(CustomTagData.class);

        debugTag = new Tag(DEBUG_LOGGER_GROUPID);
        perfTag = new Tag(PERF_LOGGER_GROUPID);
        dgTag = new Tag(DG_LOGGER_GROUPID);


        final NetworkPersistedBatchReadyListener perfListener = new NetworkPersistedBatchReadyListener(getApplicationContext(), getCacheDir() + "/" + "perf",
                serializationStrategy, backgroundHandler, new NetworkPersistedBatchReadyListener.NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {

            }
        }, 5, 100, 90, TRIM_MEMORY_BACKGROUND, new TrimmedBatchCallback() {
            @Override
            public void onTrimmed(int oldSize, int newSize) {

            }
        });

        perfListener.setListener(new PersistedBatchCallback() {
            @Override
            public void onPersistFailure(Batch batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(Batch batch) {
                //SystemClock.sleep(2000);
                perfListener.finish(batch);
                Log.e("Perf", "Finish Called");
                ArrayList<CustomTagData> dataArrayList = (ArrayList<CustomTagData>) batch.getDataCollection();
                for (CustomTagData data : dataArrayList) {
                    Log.e("OUT", data.getEvent().toString());
                }
            }

            @Override
            public void onFinish() {

            }
        });

        final NetworkPersistedBatchReadyListener debugListener = new NetworkPersistedBatchReadyListener(getApplicationContext(),
                getCacheDir() + "/" + "debug", serializationStrategy, backgroundHandler, new NetworkPersistedBatchReadyListener.NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {

            }
        }, 5, 100, 90, TRIM_MEMORY_BACKGROUND, new TrimmedBatchCallback() {
            @Override
            public void onTrimmed(int oldSize, int newSize) {

            }
        });

        debugListener.setListener(new PersistedBatchCallback<TagBatch<TagData>>() {
            @Override
            public void onPersistFailure(TagBatch<TagData> batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(TagBatch<TagData> batch) {
                // SystemClock.sleep(2000);
                debugListener.finish(batch);
                Log.e("Debug", "Finish Called");
            }

            @Override
            public void onFinish() {

            }
        });

        final NetworkPersistedBatchReadyListener dgListener = new NetworkPersistedBatchReadyListener(getApplicationContext(),
                getCacheDir() + "/" + "dg", serializationStrategy, backgroundHandler, new NetworkPersistedBatchReadyListener.NetworkBatchListener() {
            @Override
            public void performNetworkRequest(Batch batch, ValueCallback callback) {

            }
        }, 5, 100, 90, TRIM_MEMORY_BACKGROUND, new TrimmedBatchCallback() {
            @Override
            public void onTrimmed(int oldSize, int newSize) {

            }
        });

        dgListener.setListener(new PersistedBatchCallback<TagBatch<TagData>>() {
            @Override
            public void onPersistFailure(TagBatch<TagData> batch, Exception e) {

            }

            @Override
            public void onPersistSuccess(TagBatch<TagData> batch) {
                //SystemClock.sleep(2000);
                dgListener.finish(batch);
                Log.e("Dg", "Finish Called");
            }

            @Override
            public void onFinish() {

            }
        });

        batchManager = new TagBatchManager.Builder<>()
                .setSerializationStrategy(serializationStrategy)
                .setHandler(backgroundHandler)
                .addTag(perfTag, new SizeBatchingStrategy(3, new TapePersistenceStrategy(getCacheDir() + "/perf1", serializationStrategy)), perfListener)
                .addTag(debugTag, new SizeBatchingStrategy(3, new TapePersistenceStrategy(getCacheDir() + "/debug1", serializationStrategy)), debugListener)
                .addTag(dgTag, new SizeBatchingStrategy(3, new TapePersistenceStrategy(getCacheDir() + "/dg1", serializationStrategy)), dgListener)
                .build(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("IN", TrackingHelper.getProductPageViewEvent(String.valueOf(count), "dfg", "fgh").toString());
                batchManager.addToBatch(Collections.singleton(new CustomTagData(perfTag, TrackingHelper.getProductPageViewEvent(String.valueOf(count), "dfg", "fgh"))));
                Snackbar.make(view, "Replace with your own action " + count, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                count++;
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
