package chat.rocket.android.helper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * Rx operator and so on.
 */
public class RxHelper {
  public static Function<Flowable<? extends Throwable>, Flowable<?>> exponentialBackoff(
      int maxRetryCount, long base, TimeUnit unit) {

    // ref: https://github.com/ReactiveX/RxJava/blob/a8ba158839b67246a742b6f1531995ffd7545c08/src/main/java/io/reactivex/Observable.java#L9601
    return attempts -> attempts
        .zipWith(Flowable.range(0, maxRetryCount), (error, retryCount) -> retryCount)
        .flatMap(retryCount -> Flowable.timer(base * (long) Math.pow(2, retryCount), unit));
  }
}
