package kmer;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class Retriable<T> {

    private static Random random = new Random();

    /**
     * runWithRetries - retries a function based on exceptions and validation
     * @param maxRetries - the maximum number of retries
     * @param maxInterval - the maximum wait time on the first retry, subsequent retries are longer
     * @param supplier - the function that is to be retried on exception or validation failure, takes attempt number as an argument
     * @param validator - a validator function, the supplier is retried if the validator returns false when applied to supplier output
     * @param <T>
     * @return
     * @throws InterruptedException
     */
    static <T> Optional<T> runWithRetries(
            int maxRetries,
            int maxInterval,
            Function<Integer,T> supplier,
            Function<T,Boolean> validator
    ) throws InterruptedException {
        int count = 0;
        while (count < maxRetries) {
            try {
                T value = supplier.apply(count);
                if (validator.apply(value)) {
                    return Optional.of(value);
                }
            } catch (Exception e) {
                System.err.println("Retry after exception: " + e.toString());
            }
            Thread.sleep((count + 1) * 1000L + (long)(Retriable.random.nextDouble() * maxInterval));
            if (++count >= maxRetries) break;
        }
        return Optional.empty();
    };

    static <T> Optional<T> runWithRetries(
            int maxRetries,
            int maxInterval,
            Function<Integer, T> supplier) throws InterruptedException {
        return runWithRetries(maxRetries, maxInterval, supplier, t -> true);
    }
};
