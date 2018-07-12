package kmer;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RetriableTest {

    class TestRetriable implements Retriable<Integer> {
        private int counter;

        public TestRetriable() {
            counter = 0;
        }

        public Integer run() throws Exception {
            counter++;
            if (counter < 2) {
                throw new Exception("boom!");
            }
            return counter;
        }

        public int getCounter() {
            return counter;
        }
    }

    @Test
    public void runWithRetriesShouldRetry() throws InterruptedException {
        TestRetriable retriable = new TestRetriable();
        Retriable.runWithRetries( 3, 0, retriable);
        assertEquals(2, retriable.getCounter());
    }
}


