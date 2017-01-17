package chat.rocket.android.api.rest;

public class Response<T> {
  private final boolean successful;
  private final String protocol;
  private final T data;

  public Response(boolean successful, String protocol, T data) {
    this.successful = successful;
    this.protocol = protocol;
    this.data = data;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public String getProtocol() {
    return protocol;
  }

  public T getData() {
    return data;
  }
}
