package com.maven.plugin.deadlock.testclasses;

public class DeadlockMethodsClass {

    Object lock = new Object();
    Object otherLock = new Object();
    String stringLock = new String();

    public synchronized void emptySynchronizedMethod() {

    }

    public static void emptyStaticSynchronizedMethod() {

    }

    public synchronized void synchronizedMethod() {

    }

    public void methodWithSynchronizedScope() {
        synchronized (lock) {

        }
    }
}
