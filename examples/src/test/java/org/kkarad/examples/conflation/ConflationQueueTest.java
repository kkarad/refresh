package org.kkarad.examples.conflation;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConflationQueueTest {

    private ConflationQueue<String, Rate> queue;

    @Before
    public void setUp() throws Exception {
        KeyExtractor<String, Rate> keyExtractor = new KeyExtractor<String, Rate>() {
            @Override
            public String toKey(Rate value) {
                return value.ccyPair;
            }
        };

        queue = new ConflationQueueImpl<>(keyExtractor);

    }

    @Test
    public void queue_conflates() throws Exception {
        queue.put(new Rate("EURGBP", 0.8001));
        queue.put(new Rate("EURGBP", 0.8002));

        Rate first = queue.take();
        assertThat(first.price, equalTo(0.8002));
    }

    @Test
    public void queue_keeps_order() throws Exception {
        queue.put(new Rate("EURGBP", 0.8001));
        queue.put(new Rate("GBPUSD", 1.2500));
        queue.put(new Rate("EURGBP", 0.8002));

        Rate first = queue.take();
        Rate second = queue.take();

        assertThat(first.ccyPair, equalTo("EURGBP"));
        assertThat(second.ccyPair, equalTo("GBPUSD"));
    }

    private static class Rate {
        public final String ccyPair;
        public final double price;

        private Rate(String ccyPair, double price) {
            this.ccyPair = ccyPair;
            this.price = price;
        }
    }
}