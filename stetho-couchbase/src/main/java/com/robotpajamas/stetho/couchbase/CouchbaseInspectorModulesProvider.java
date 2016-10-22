package com.robotpajamas.stetho.couchbase;

import android.content.Context;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CouchbaseInspectorModulesProvider implements InspectorModulesProvider {

    private final Context mContext;
    private final boolean mShowMetadata;

    CouchbaseInspectorModulesProvider(Builder builder) {
        mContext = builder.context;
        mShowMetadata = builder.showMetadata;
    }

    @Override
    public Iterable<ChromeDevtoolsDomain> get() {
        final List<ChromeDevtoolsDomain> modules = new ArrayList<>();
        for (ChromeDevtoolsDomain domain : Stetho.defaultInspectorModulesProvider(mContext).get()) {
            Timber.d("Domain: %s", domain.toString());
            if (domain instanceof com.facebook.stetho.inspector.protocol.module.Database) {
                continue;
            }
            modules.add(domain);
        }

        modules.add(new Database(new CouchbasePeerManager(mContext, mContext.getPackageName(), mShowMetadata)));
        return modules;
    }

    //region Builder

    public static final class Builder {
        private Context context;
        private boolean showMetadata = true;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

        public Builder showMetadata(boolean showMetadata) {
            this.showMetadata = showMetadata;
            return this;
        }

        public CouchbaseInspectorModulesProvider build() {
            return new CouchbaseInspectorModulesProvider(this);
        }

    }

    //endregion
}
