package org.kkarad.examples.atomic.locks;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadWriteReentrantLockTest {

    @Test
    public void test_reentrant_read_lock() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        System.out.println("init: " + rwLock);
        rwLock.readLock();
        System.out.println("after 1st lock: " + rwLock);
        rwLock.readLock();
        System.out.println("after 2nd lock: " + rwLock);

        System.out.println("Hello!");

        rwLock.readUnlock();
        System.out.println("after 1st unlock: " + rwLock);

    }

    @Test
    public void test_reentrant_write_lock() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        System.out.println("init: " + rwLock);
        rwLock.writeLock();
        System.out.println("after 1st lock: " + rwLock);
        rwLock.writeLock();
        System.out.println("after 2nd lock: " + rwLock);

        System.out.println("Hello!");

        rwLock.writeUnlock();
        System.out.println("after 1st unlock: " + rwLock);
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_read_unlock_without_ownership() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        rwLock.readUnlock();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_write_unlock_without_ownership() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        rwLock.writeUnlock();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_read_write_subsequent_locks() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        rwLock.readLock();
        rwLock.writeLock();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void test_write_read_subsequent_locks() throws Exception {
        ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);
        rwLock.writeLock();
        rwLock.readLock();
    }

    @Test(timeout = 20000)
    public void test_parallel() throws Exception {
        final int nThreads = 500;
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(nThreads);
        final ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(false);

        for (int i = 0; i < nThreads; i++) {
            final int counter = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        if (counter % 2 == 0) {
                            rwLock.readLock();
                            System.out.printf("%s is reading%n", Thread.currentThread().getName());
                            rwLock.readUnlock();
                        } else {
                            rwLock.writeLock();
                            System.out.printf("%s is writing%n", Thread.currentThread().getName());
                            rwLock.writeUnlock();
                        }
                        stopLatch.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            });
        }

        System.out.println("Test starts");
        startLatch.countDown();
        stopLatch.await();
        System.out.println("Test finished");
        executorService.shutdown();
    }

    @Test
    public void test_writer_priority() throws Exception {
        final int nThreads = 200;
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final ReadWriteReentrantLock rwLock = new ReadWriteReentrantLock(true);
        final CountDownLatch stopLatch = new CountDownLatch(nThreads);

        final Runnable readAction = new Runnable() {
            @Override
            public void run() {
                rwLock.readLock();
                System.out.printf("%s is reading%n", Thread.currentThread().getName());
                rwLock.readUnlock();
                stopLatch.countDown();
            }
        };

        final Runnable writeAction = new Runnable() {
            @Override
            public void run() {
                rwLock.writeLock();
                System.out.printf("%s is writing%n", Thread.currentThread().getName());
                rwLock.writeUnlock();
                stopLatch.countDown();
            }
        };

        System.out.println("Test starts");
        int writerIteration = 10;
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(i == writerIteration ? writeAction : readAction);
        }
        stopLatch.await();
        System.out.println("Test finished");
    }
}