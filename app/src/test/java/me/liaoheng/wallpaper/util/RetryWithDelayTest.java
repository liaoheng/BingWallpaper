package me.liaoheng.wallpaper.util;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;

import static org.junit.Assert.assertEquals;

public class RetryWithDelayTest {

    @Test
    public void retriesUntilSourceSucceeds() {
        AtomicInteger attempts = new AtomicInteger();
        Observable<String> source = Observable.create(emitter -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                emitter.onError(new IOException("transient " + attempt));
            } else {
                emitter.onNext("ok");
                emitter.onComplete();
            }
        });

        TestObserver<String> observer = source.retryWhen(new RetryWithDelay(5, 0)).test();
        observer.awaitDone(5, TimeUnit.SECONDS);

        observer.assertNoErrors();
        observer.assertValue("ok");
        assertEquals(3, attempts.get());
    }

    @Test
    public void givesUpAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger();
        Observable<String> source = Observable.create(emitter -> {
            attempts.incrementAndGet();
            emitter.onError(new IOException("always fails"));
        });

        TestObserver<String> observer = source.retryWhen(new RetryWithDelay(3, 0)).test();
        observer.awaitDone(5, TimeUnit.SECONDS);

        observer.assertNoValues();
        observer.assertError(IOException.class);
        // initial subscribe plus maxRetries retries
        assertEquals(4, attempts.get());
    }
}
