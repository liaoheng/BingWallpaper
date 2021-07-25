package me.liaoheng.wallpaper.util;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

/**
 * <a href="https://stackoverflow.com/questions/22066481/rxjava-can-i-use-retry-but-with-delay">rxjava-can-i-use-retry-but-with-delay</a>
 *
 * @author liaoheng
 * @date 2021-07-25 11:11
 */
public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
    private final int maxRetries;
    private final int retryDelayMillis;
    private int retryCount;

    public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap((Function<Throwable, Observable<?>>) throwable -> {
                    if (++retryCount < maxRetries) {
                        return Observable.timer(retryDelayMillis,
                                TimeUnit.SECONDS);
                    }
                    return Observable.error(throwable);
                });
    }
}