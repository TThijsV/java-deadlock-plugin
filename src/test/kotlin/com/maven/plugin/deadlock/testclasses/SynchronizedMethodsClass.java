package com.maven.plugin.deadlock.testclasses;

class SynchronizedMethodsClass {

    Object lock = new Object();

    public void nonSynchronizedMethod() {

    }

    public synchronized void synchronizedMethod() {

    }

    public static synchronized void staticSynchronizedMethod() {

    }

    public void synchronizedScopeMethod() {
        synchronized (this) {

        }
    }

    public void synchronizedScopeOnObjectMethod() {
        synchronized (lock) {

        }
    }

    public void notSynchronizedMethodFromSynchronizedScopeMethod() {
        synchronized (this) {
            nonSynchronizedMethod();
        }
    }
}