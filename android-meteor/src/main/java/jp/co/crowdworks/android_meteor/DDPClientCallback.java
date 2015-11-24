package jp.co.crowdworks.android_meteor;

import android.support.annotation.Nullable;

public class DDPClientCallback {
    public static abstract class Base {
        public DDPClient client;
        public Base(DDPClient client) {
            this.client = client;
        }
    }

    public static abstract class BaseException extends Exception {
        public DDPClient client;
        public BaseException(DDPClient client) {
            this.client = client;
        }
    }

    public static class Connect extends Base {
        public String session;
        public Connect(DDPClient client, String session) {
            super(client);
            this.session = session;
        }
        public static class Failed extends BaseException {
            public String version;
            public Failed(DDPClient client, String version) {
                super(client);
                this.version = version;
            }
        }
    }

    public static class Ping extends Base {
        @Nullable public String id;
        public Ping(DDPClient client, @Nullable String id) {
            super(client);
            this.id = id;
        }
        public static class Timeout extends BaseException {
            public Timeout(DDPClient client) {
                super(client);
            }
        }
    }

}
