package chat.rocket.android.helper;

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Rx operator and so on.
 */
public class RxHelper {
  public static Func1<Observable<? extends Throwable>, Observable<?>> exponentialBackoff(
      int maxRetryCount, long base, TimeUnit unit) {
    return errors -> errors
        .zipWith(Observable.range(0, maxRetryCount), (error, retryCount) -> retryCount)
        .flatMap(retryCount -> Observable.timer(base * (long) Math.pow(2, retryCount), unit));
  }

  public static <T> Single<T> lazy(Func0<Single<T>> func) {
    return Single.just(true).flatMap(_junk -> func.call());
  }
}
