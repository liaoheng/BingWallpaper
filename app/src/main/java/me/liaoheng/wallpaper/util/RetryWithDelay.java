package me.liaoheng.wallpaper.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;

/**
 * <a href="https://stackoverflow.com/questions/22066481/rxjava-can-i-use-retry-but-with-delay">rxjava-can-i-use-retry-but-with-delay</a>
 *
 * @author liaoheng
 * @date 2021-07-25 11:11
 */
public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {
    private final int maxRetries;
    private final int retryDelaySeconds;

    public RetryWithDelay(final int maxRetries, final int retryDelaySeconds) {
        this.maxRetries = maxRetries;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> attempts) {
        // local to apply() so each subscription gets its own count
        final AtomicInteger retryCount = new AtomicInteger();
        return attempts
                .flatMap((Function<Throwable, Observable<?>>) throwable -> {
                    if (retryCount.getAndIncrement() < maxRetries) {
                        return Observable.timer(retryDelaySeconds,
                                TimeUnit.SECONDS);
                    }
                    return Observable.error(throwable);
                });
    }
}