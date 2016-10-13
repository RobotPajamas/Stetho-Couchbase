package com.robotpajamas.android.stethocb;

import android.app.Application;
import android.content.Context;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.facebook.stetho.Stetho;
import com.robotpajamas.stethocb.CouchbaseInspectorModulesProvider;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class MainApplication extends Application {

    private Manager mManager;
    private Database mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(new CouchbaseInspectorModulesProvider(this))
                            .build());
        }
        initializeCouchbase(this);
    }

    // Initialize database and create some fake data
    private void initializeCouchbase(Context context) {
        final Map<String, Object> s = new HashMap<>();
        s.put("key1", "value1");
        s.put("key2", "value2");
        s.put("key3", "value3");
        s.put("key4", "value4");
        s.put("key5", "value5");
        s.put("key6", "value6");
        s.put("key7", "value7");
        s.put("key8", "value8");
        s.put("key9", "value9");
        s.put("key10", "value10");

        try {
            mManager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            mDatabase = mManager.getDatabase("stetho-couchbase-sample");
            Document doc = mDatabase.getDocument("id:123456");
            doc.putProperties(s);
            doc = mDatabase.getDocument("p::abcdefg");
            doc.putProperties(s);
            doc = mDatabase.getDocument("p::123abc");
            doc.putProperties(s);
        } catch (Exception ignored) {

        }
    }
}
