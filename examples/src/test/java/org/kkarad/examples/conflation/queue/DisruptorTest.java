package org.kkarad.examples.conflation.queue;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.Test;

import java.util.concurrent.Executors;

public class DisruptorTest {

    @Test
    public void testIt() throws Exception {
        EventFactory<Rate> eventFactory = new EventFactory<Rate>() {
            @Override
            public Rate newInstance() {
                return new Rate(null, -0.0);
            }
        };

        Disruptor<Rate> disruptor = new Disruptor<>(
                eventFactory,
                1024,
                Executors.newFixedThreadPool(5),
                ProducerType.SINGLE, new BusySpinWaitStrategy());

        disruptor.handleEventsWith(new EventHandler<Rate>() {
            @Override
            public void onEvent(Rate event, long sequence, boolean endOfBatch) throws Exception {
                System.out.println("Rate: " + event);
            }
        });

        disruptor.start();

        EventTranslatorOneArg<Rate, Double> eventTranslator = new EventTranslatorOneArg<Rate, Double>() {
            @Override
            public void translateTo(Rate event, long sequence, Double price) {
                event.ccyPair = "EURGBP";
                event.price = price;
            }
        };

        for (double p = 0.0; p < 100000000; p += 0.001) {
            disruptor.publishEvent(eventTranslator, p);
        }

        disruptor.shutdown();
    }
}
