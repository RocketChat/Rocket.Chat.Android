package chat.rocket.android.api;

public class TwoStepAuthException extends Exception {

  public static final String TYPE = "totp-required";

  private static final long serialVersionUID = 7063596902054234189L;

  public TwoStepAuthException() {
    super();
  }

  public TwoStepAuthException(String message) {
    super(message);
  }
}
