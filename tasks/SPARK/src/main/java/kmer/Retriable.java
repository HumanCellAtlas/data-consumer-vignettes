package kmer;

import java.util.Optional;
import java.util.Random;

public interface Retriable<T> {
    T run() throws Exception;
    Random random = new Random();

    static <T> Optional<T> runWithRetries(int maxRetries, int maxInterval, Retriable<T> t) throws InterruptedException {
        int count = 0;
        while (count < maxRetries) {
            try {
                T value = t.run();
                return Optional.of(value);
            } catch (Exception e) {
                // simple backoff with jitter added
                Thread.sleep((count + 1) * 1000L + (long)(Retriable.random.nextDouble() * maxInterval));
                if (++count >= maxRetries) break;
            }
        }
        return Optional.empty();
    };
};

