package chat.rocket.android.api.rest;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CookieInterceptor implements Interceptor {

  private final CookieProvider cookieProvider;

  public CookieInterceptor(CookieProvider cookieProvider) {
    this.cookieProvider = cookieProvider;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    if (chain.request().url().host().equals(cookieProvider.getHostname())) {
      Request newRequest = chain.request().newBuilder()
          .header("Cookie", cookieProvider.getCookie())
          .build();
      return chain.proceed(newRequest);
    }

    return chain.proceed(chain.request());
  }
}
