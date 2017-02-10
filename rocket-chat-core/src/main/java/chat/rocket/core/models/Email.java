package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Email {

  public abstract String getAddress();

  public abstract boolean isVerified();

  public static Builder builder() {
    return new AutoValue_Email.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setAddress(String address);

    public abstract Builder setVerified(boolean verified);

    public abstract Email build();
  }
}
