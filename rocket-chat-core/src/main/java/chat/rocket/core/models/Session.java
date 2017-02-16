package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Session {

  public enum State {UNAVAILABLE, INVALID, VALID}

  public abstract int getSessionId();

  @Nullable
  public abstract String getToken();

  public abstract boolean isTokenVerified();

  @Nullable
  public abstract String getError();

  public abstract Session withTokenVerified(boolean tokenVerified);

  public abstract Session withError(String error);

  public static Builder builder() {
    return new AutoValue_Session.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setSessionId(int sessionId);

    public abstract Builder setToken(String token);

    public abstract Builder setTokenVerified(boolean tokenVerified);

    public abstract Builder setError(String error);

    public abstract Session build();
  }
}
