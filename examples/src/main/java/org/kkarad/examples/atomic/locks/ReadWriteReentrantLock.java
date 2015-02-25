package org.kkarad.examples.atomic.locks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ReadWriteReentrantLock {
    private Lock counterLock = new ReentrantLock();
    private Condition readMode = counterLock.newCondition();
    private Condition writeMode = counterLock.newCondition();
    private Set<Thread> readThreads = new HashSet<>();
    private Thread writeThread = null;
    private int counter = 0;

    public void readLock() throws InterruptedException {
        //System.out.printf("%s enters readLock (status=%s)%n", Thread.currentThread().getName(), toString());
        counterLock.lock();
        try {
            //System.out.printf("%s entered readLock (status=%s)%n", Thread.currentThread().getName(), toString());
            assertWriteLockDoesntExist();

            if(!readThreads.contains(Thread.currentThread())) {
                while (counter < 0) {
                    //System.out.printf("%s enters readMode wait (status=%s)%n", Thread.currentThread().getName(), toString());
                    readMode.await();
                    //System.out.printf("%s exits readMode wait (status=%s)%n", Thread.currentThread().getName(), toString());
                }

                counter++;
                readThreads.add(Thread.currentThread());
            }
        } finally {
            //System.out.printf("%s exits readLock (status=%s)%n", Thread.currentThread().getName(), toString());
            counterLock.unlock();
        }

    }

    private void assertWriteLockDoesntExist() {
        if(writeThread == Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread owns a write lock");
        }
    }

    public void readUnlock() {
        //System.out.printf("%s enters readUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
        counterLock.lock();
        try {
            //System.out.printf("%s entered readUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
            assertReadLockOwnership();

            readThreads.remove(Thread.currentThread());
            counter--;
            if(counter == 0) {
                //System.out.printf("%s signals writeMode (status=%s)%n", Thread.currentThread().getName(), toString());
                writeMode.signalAll();
            }
        } finally {
          //System.out.printf("%s exits readUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
          counterLock.unlock();
        }
    }

    private void assertReadLockOwnership() {
        if(!readThreads.contains(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling thread doesn't own this read lock");
        }
    }


    public void writeLock() throws InterruptedException {
        //System.out.printf("%s enters writeLock (status=%s)%n", Thread.currentThread().getName(), toString());
        counterLock.lock();
        try {
            //System.out.printf("%s entered writeLock (status=%s)%n", Thread.currentThread().getName(), toString());
            assertReadLockDoesntExist();

            if(writeThread != Thread.currentThread()) {
                while(counter != 0) {
                    //System.out.printf("%s enters writeMode wait (status=%s)%n", Thread.currentThread().getName(), toString());
                    writeMode.await();
                    //System.out.printf("%s exits writeMode wait (status=%s)%n", Thread.currentThread().getName(), toString());
                }

                counter--;
                writeThread = Thread.currentThread();
            }
        } finally {
            //System.out.printf("%s exits writeLock (status=%s)%n", Thread.currentThread().getName(), toString());
            counterLock.unlock();
        }
    }

    private void assertReadLockDoesntExist() {
        if(readThreads.contains(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling thread owns a read lock");
        }
    }

    public void writeUnlock() {
        //System.out.printf("%s enters writeUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
        counterLock.lock();
        try {
            //System.out.printf("%s entered writeUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
            checkWriteLockOwnership();

            writeThread = null;
            counter++;
            if(counter == 0) {
                //System.out.printf("%s signals readMode (status=%s)%n", Thread.currentThread().getName(), toString());
                readMode.signalAll();
                //System.out.printf("%s signals writeMode (status=%s)%n", Thread.currentThread().getName(), toString());
                writeMode.signalAll();
            }
        } finally {
            //System.out.printf("%s exits writeUnlock (status=%s)%n", Thread.currentThread().getName(), toString());
            counterLock.unlock();
        }
    }

    private void checkWriteLockOwnership() {
        if(writeThread != Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread doesn't own this write lock");
        }
    }

    @Override
    public String toString() {
        return "{" +
                "counter=" + counter +
                ", writeThread=" + writeThread +
                ", readThreads=" + readThreads +
                '}';
    }
}
