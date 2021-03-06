package org.kkarad.examples.atomic.locks;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ReadWriteReentrantLock {
    private final Lock counterLock = new ReentrantLock();
    private final Condition readMode = counterLock.newCondition();
    private final Condition writeMode = counterLock.newCondition();
    private Set<Thread> readThreads = new HashSet<>();
    private Thread writeThread = null;
    private int counter = 0;
    private final boolean priorityToWriters;
    private boolean writerWaiting = false;

    public ReadWriteReentrantLock(boolean priorityToWriters) {
        this.priorityToWriters = priorityToWriters;
    }

    public void readLock() {
        counterLock.lock();
        try {
            assertWriteLockDoesntExist();

            if(!readThreads.contains(Thread.currentThread())) {
                while (counter < 0 || isWriterWaiting()) {
                    readMode.awaitUninterruptibly();
                }

                counter++;
                readThreads.add(Thread.currentThread());
            }
        } finally {
            counterLock.unlock();
        }

    }

    private boolean isWriterWaiting() {
        return priorityToWriters && writerWaiting;
    }

    private void assertWriteLockDoesntExist() {
        if(writeThread == Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread owns a write lock");
        }
    }

    public void readUnlock() {
        counterLock.lock();
        try {
            assertReadLockOwnership();

            readThreads.remove(Thread.currentThread());
            counter--;
            if(counter == 0) {
                writeMode.signalAll();
            }
        } finally {
          counterLock.unlock();
        }
    }

    private void assertReadLockOwnership() {
        if(!readThreads.contains(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling thread doesn't own this read lock");
        }
    }


    public void writeLock() {
        counterLock.lock();
        try {
            assertReadLockDoesntExist();

            if(writeThread != Thread.currentThread()) {
                while(counter != 0) {
                    writerIsWaiting(true);
                    writeMode.awaitUninterruptibly();
                }

                counter--;
                writeThread = Thread.currentThread();
            }
        } finally {
            counterLock.unlock();
        }
    }

    private void writerIsWaiting(boolean waiting) {
        if(priorityToWriters) {
            writerWaiting = waiting;
        }
    }

    private void assertReadLockDoesntExist() {
        if(readThreads.contains(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling thread owns a read lock");
        }
    }

    public void writeUnlock() {
        counterLock.lock();
        try {
            checkWriteLockOwnership();

            writeThread = null;
            counter++;
            if(counter == 0) {
                writerIsWaiting(false);
                readMode.signalAll();
                writeMode.signalAll();
            }
        } finally {
            counterLock.unlock();
        }
    }

    private void checkWriteLockOwnership() {
        if(writeThread != Thread.currentThread()) {
            throw new IllegalMonitorStateException("Calling thread doesn't own this write lock");
        }
    }
}
