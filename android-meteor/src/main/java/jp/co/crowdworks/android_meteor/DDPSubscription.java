package jp.co.crowdworks.android_meteor;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

public class DDPSubscription {
    public static abstract class Event {
        public final DDPClient client;

        public Event(DDPClient client) {
            this.client = client;
        }
    }

    public static abstract class BaseException extends Exception {
        public final DDPClient client;

        public BaseException(DDPClient client) {
            this.client = client;
        }
    }

    public static class NoSub extends Event {
        public String id;
        public NoSub(DDPClient client, String id) {
            super(client);
            this.id = id;
        }

        public static class Error extends BaseException {
            String id;
            JSONObject error;
            public Error(DDPClient client, String id, JSONObject error) {
                super(client);
                this.id = id;
                this.error = error;
            }
        }

    }

    public static class Ready extends Event {
        public String id;

        public Ready(DDPClient client, String id) {
            super(client);
            this.id = id;
        }
    }

    public static class Added extends Event {
        public String collection;
        public String docID;
        public JSONObject fields;

        public Added(DDPClient client, String collection, String docID, JSONObject fields) {
            super(client);
            this.collection = collection;
            this.docID = docID;
            this.fields = fields;
        }

        public static class Before extends Added {
            public String before;

            public Before(DDPClient client, String collection, String docID, JSONObject fields, String before) {
                super(client, collection, docID, fields);
                this.before = before;
            }
        }
    }

    public static class Changed extends Event {
        public String collection;
        public String docID;
        public JSONObject fields;
        public JSONArray cleared;

        public Changed(DDPClient client, String collection, String docID, JSONObject fields, @NonNull JSONArray cleared) {
            super(client);
            this.collection = collection;
            this.docID = docID;
            this.fields = fields;
            this.cleared = cleared;
        }
    }

    public static class Removed extends Event {
        public String collection;
        public String docID;

        public Removed(DDPClient client, String collection, String docID) {
            super(client);
            this.collection = collection;
            this.docID = docID;
        }
    }

    public static class MovedBefore extends Event {
        public String collection;
        public String docID;
        public String before;

        public MovedBefore(DDPClient client, String collection, String docID, String before) {
            super(client);
            this.collection = collection;
            this.docID = docID;
            this.before = before;
        }
    }
}
