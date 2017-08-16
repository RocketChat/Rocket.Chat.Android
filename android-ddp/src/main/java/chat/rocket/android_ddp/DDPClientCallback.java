package chat.rocket.android_ddp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.json.JSONObject;

public class DDPClientCallback {
  public static abstract class Base {
    public DDPClient client;

    public Base(DDPClient client) {
      this.client = client;
    }
  }

  public static abstract class BaseException extends Exception {
    public DDPClient client;

    public BaseException(Class<? extends BaseException> clazz, DDPClient client) {
      super(clazz.getName());
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
        super(Failed.class, client);
        this.version = version;
      }
    }

    public static class Timeout extends BaseException {
      public Timeout(DDPClient client) {
        super(Timeout.class, client);
      }
    }
  }

  public static class Ping extends Base {
    @Nullable public String id;

    public Ping(DDPClient client, @Nullable String id) {
      super(client);
      this.id = id;
    }

    public static class UnMatched extends Base {
      @NonNull public String id;

      public UnMatched(DDPClient client, @NonNull String id) {
        super(client);
        this.id = id;
      }
    }

    public static class Timeout extends BaseException {
      public Timeout(DDPClient client) {
        super(Timeout.class, client);
      }
    }
  }

  public static class RPC extends Base {
    public String id;
    public String result;

    public RPC(DDPClient client, String id, String result) {
      super(client);
      this.id = id;
      this.result = result;
    }

    public static class Error extends BaseException {
      public String id;
      public JSONObject error;

      public Error(DDPClient client, String id, JSONObject error) {
        super(Error.class, client);
        this.id = id;
        this.error = error;
      }
    }

    public static class Timeout extends BaseException {
      public Timeout(DDPClient client) {
        super(Timeout.class, client);
      }
    }
  }

  public static class Closed extends BaseException {
    public Closed(DDPClient client) {
      super(Closed.class, client);
    }
  }
}
