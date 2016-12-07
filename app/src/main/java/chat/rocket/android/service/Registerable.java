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
   * unregister.
   */
  void unregister();
}
