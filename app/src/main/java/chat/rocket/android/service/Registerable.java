package chat.rocket.android.service;

/**
 * interface for observer and ddp_subscription.
 */
public interface Registerable {
  /**
   * register.
   */
  void register();

  /**
   * keepalive.
   */
  void keepalive();

  /**
   * unregister.
   */
  void unregister();
}
