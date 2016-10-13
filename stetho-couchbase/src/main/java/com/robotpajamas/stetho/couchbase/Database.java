package com.robotpajamas.stetho.couchbase;

import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;
import com.facebook.stetho.json.ObjectMapper;
import com.facebook.stetho.json.annotation.JsonProperty;

import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

class Database implements ChromeDevtoolsDomain {

    private final CouchbasePeerManager mCouchbasePeerManager;
    private final ObjectMapper mObjectMapper = new ObjectMapper();

    Database(CouchbasePeerManager peerManager) {
        mCouchbasePeerManager = peerManager;
    }

    @ChromeDevtoolsMethod
    @SuppressWarnings("unused")
    public void enable(JsonRpcPeer peer, JSONObject params) {
        Timber.d("Enable Peer: %s", String.valueOf(params));
        mCouchbasePeerManager.addPeer(peer);
    }

    @ChromeDevtoolsMethod
    @SuppressWarnings("unused")
    public void disable(JsonRpcPeer peer, JSONObject params) {
        Timber.d("Disable Peer: %s", String.valueOf(params));
        mCouchbasePeerManager.removePeer(peer);
    }

    @ChromeDevtoolsMethod
    @SuppressWarnings("unused")
    public JsonRpcResult getDatabaseTableNames(JsonRpcPeer peer, JSONObject params) {
        Timber.d("getAllDocumentIds: %s", String.valueOf(params));
        GetDatabaseTableNamesRequest request = mObjectMapper.convertValue(params, GetDatabaseTableNamesRequest.class);
        GetDatabaseTableNamesResponse response = new GetDatabaseTableNamesResponse();
        response.tableNames = mCouchbasePeerManager.getAllDocumentIds(request.databaseId);
        return response;
    }

    @ChromeDevtoolsMethod
    @SuppressWarnings("unused")
    public JsonRpcResult executeSQL(JsonRpcPeer peer, JSONObject params) {
        Timber.d("executeSQL: %s", String.valueOf(params));

        ExecuteSQLResponse response = new ExecuteSQLResponse();
        try {
            ExecuteSQLRequest request = mObjectMapper.convertValue(params, ExecuteSQLRequest.class);
            return mCouchbasePeerManager.executeSQL(request.databaseId, request.query);
        } catch (Exception e) {
            Timber.e(e.toString());
            Error error = new Error();
            error.code = 0;
            error.message = e.getMessage();
            response.sqlError = error;
            return response;
        }
    }


    private static class GetDatabaseTableNamesRequest {
        @JsonProperty(required = true)
        public String databaseId;
    }

    private static class GetDatabaseTableNamesResponse implements JsonRpcResult {
        @JsonProperty(required = true)
        public List<String> tableNames;
    }

    private static class ExecuteSQLRequest {
        @JsonProperty(required = true)
        public String databaseId;

        @JsonProperty(required = true)
        public String query;
    }

    static class ExecuteSQLResponse implements JsonRpcResult {
        @JsonProperty
        public List<String> columnNames;

        @JsonProperty
        public List<Object> values;

        @JsonProperty
        public Error sqlError;
    }

    public static class AddDatabaseEvent {
        @JsonProperty(required = true)
        public DatabaseObject database;
    }

    public static class DatabaseObject {
        @JsonProperty(required = true)
        public String id;

        @JsonProperty(required = true)
        public String domain;

        @JsonProperty(required = true)
        public String name;

        @JsonProperty(required = true)
        public String version;
    }

    public static class Error {
        @JsonProperty(required = true)
        public String message;

        @JsonProperty(required = true)
        public int code;
    }
}
