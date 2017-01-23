package chat.rocket.android.service;

import chat.rocket.android.api.DDPClientWrapper;

/**
 * reference to get fresh DDPClient instance.
 */
public interface DDPClientRef {
  DDPClientWrapper get();
}
