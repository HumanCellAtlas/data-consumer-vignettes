package kmer;


import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class RetriableTest {

    @Test
    public void runWithRetriesShouldValidate() throws InterruptedException {
        Optional<Integer> opt = Retriable.runWithRetries( 3, 0, attempt -> attempt, i -> false);
        assertTrue(!opt.isPresent());
    }

    @Test
    public void runWithRetriesShouldReturnSuccessfulValue() throws InterruptedException {
        Optional<Integer> opt = Retriable.runWithRetries( 3, 0, attempt -> attempt, i -> i > 1);
        assertTrue(opt.isPresent());
        assertTrue(opt.get() == 2);
    }
}


