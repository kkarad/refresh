package org.kkarad.examples.conflation.queue;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConflationQueueTest {

    private ConflationQueue<String, Rate> queue;

    @Before
    public void setUp() throws Exception {
        queue = new ArrayBlockingConflationQueue<>();
    }

    @Test
    public void queue_conflates() throws Exception {
        Rate eurgbp1 = new Rate("EURGBP", 0.8001);
        queue.put(eurgbp1.ccyPair, eurgbp1);
        Rate eurgbp2 = new Rate("EURGBP", 0.8002);
        queue.put(eurgbp2.ccyPair, eurgbp2);

        Rate first = queue.take();
        assertThat(first.price, equalTo(0.8002));
    }

    @Test
    public void queue_keeps_order() throws Exception {
        Rate eurgbp1 = new Rate("EURGBP", 0.8001);
        queue.put(eurgbp1.ccyPair, eurgbp1);
        Rate gbpusd = new Rate("GBPUSD", 1.2500);
        queue.put(gbpusd.ccyPair, gbpusd);
        Rate eurgbp2 = new Rate("EURGBP", 0.8002);
        queue.put(eurgbp2.ccyPair, eurgbp2);

        Rate first = queue.take();
        Rate second = queue.take();

        assertThat(first.ccyPair, equalTo("EURGBP"));
        assertThat(second.ccyPair, equalTo("GBPUSD"));
    }

}