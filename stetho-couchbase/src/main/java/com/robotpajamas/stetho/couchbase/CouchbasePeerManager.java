package com.robotpajamas.stetho.couchbase;

import android.content.Context;

import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ManagerOptions;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.facebook.stetho.inspector.console.CLog;
import com.facebook.stetho.inspector.helper.ChromePeerManager;
import com.facebook.stetho.inspector.helper.PeerRegistrationListener;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.protocol.module.Console;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

// TODO: Check support for ForestDB
// TODO: See if opening/closing of managers/database can be optimized
class CouchbasePeerManager extends ChromePeerManager {

    private static final String DOC_PATTERN = "\"(.*?)\"";
    private static final List<String> COLUMN_NAMES = Arrays.asList("key", "value");

    private final Pattern mPattern = Pattern.compile(DOC_PATTERN);

    private final String mPackageName;
    private final Context mContext;

    private Manager mManager;

    CouchbasePeerManager(Context context, String packageName) {
        mContext = context;
        mPackageName = packageName;

        ManagerOptions managerOptions = new ManagerOptions();
        managerOptions.setReadOnly(true);
        try {
            mManager = new Manager(new AndroidContext(mContext), managerOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setListener(new PeerRegistrationListener() {
            @Override
            public void onPeerRegistered(JsonRpcPeer peer) {
                Timber.e("onPeerRegistered");
                setupPeer(peer);
            }

            @Override
            public void onPeerUnregistered(JsonRpcPeer peer) {

            }
        });
    }

    private void setupPeer(JsonRpcPeer peer) {
        Timber.e("setupPeer");
        List<String> potentialDatabases = mManager.getAllDatabaseNames();
        for (String database : potentialDatabases) {
            Timber.e("File: %s", database);
            Database.DatabaseObject databaseParams = new Database.DatabaseObject();
            databaseParams.id = database;
            databaseParams.name = database;
            databaseParams.domain = mPackageName;
            databaseParams.version = "N/A";
            Database.AddDatabaseEvent eventParams = new Database.AddDatabaseEvent();
            eventParams.database = databaseParams;

            peer.invokeMethod("Database.addDatabase", eventParams, null /* callback */);
        }
    }

    List<String> getAllDocumentIds(String databaseId) {
        Timber.d("getAllDocumentIds: %s", databaseId);
        ManagerOptions managerOptions = new ManagerOptions();
        managerOptions.setReadOnly(true);

        DatabaseOptions databaseOptions = new DatabaseOptions();
        databaseOptions.setReadOnly(true);

        com.couchbase.lite.Database database = null;
        try {
            // TODO: Create LiveQuery on this?
            // TODO: Open manager/database and cache result - could be expensive operation
            Manager manager = new Manager(new AndroidContext(mContext), managerOptions);
            database = manager.openDatabase(databaseId, databaseOptions);

            List<String> docIds = new ArrayList<>();
            QueryEnumerator result = database.createAllDocumentsQuery().run();
            while (result.hasNext()) {
                QueryRow row = result.next();
                docIds.add(row.getDocumentId());
            }

            return docIds;
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    Database.ExecuteSQLResponse executeSQL(String databaseId, String query) throws JSONException {
        Timber.d("executeSQL: %s, %s", databaseId, query);

        Database.ExecuteSQLResponse response = new Database.ExecuteSQLResponse();

        Matcher matcher = mPattern.matcher(query);
        if (!matcher.find()) {
            return response;
        }

        String docId = matcher.group(1);
        Timber.d("Parsed doc ID: %s", docId);

        Map<String, String> map = getDocument(databaseId, docId);
        response.columnNames = COLUMN_NAMES;
        response.values = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            response.values.add(entry.getKey());
            response.values.add(entry.getValue());
        }

        // Log to console
        CLog.writeToConsole(Console.MessageLevel.DEBUG, Console.MessageSource.JAVASCRIPT, new JSONObject(map).toString(4));

        return response;
    }


    private Map<String, String> getDocument(String databaseId, String docId) {
        Timber.d("getDocument: %s, %s", databaseId, docId);
        DatabaseOptions databaseOptions = new DatabaseOptions();
        databaseOptions.setReadOnly(true);
        com.couchbase.lite.Database database = null;

        try {
            database = mManager.openDatabase(databaseId, databaseOptions);
            Document doc = database.getExistingDocument(docId);
            if (doc == null) {
                return new TreeMap<>();
            }

            Map<String, String> returnedMap = new TreeMap<>();
            for (Map.Entry<String, Object> entry : doc.getProperties().entrySet()) {
                returnedMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return returnedMap;
        } catch (Exception e) {
            Timber.e(e.toString());
            return new TreeMap<>();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }
}
