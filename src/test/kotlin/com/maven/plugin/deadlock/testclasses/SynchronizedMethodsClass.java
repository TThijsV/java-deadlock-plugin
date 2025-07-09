package com.maven.plugin.deadlock.testclasses;

class SynchronizedMethodsClass {

    public void nonSynchronizedMethod() {

    }

    public synchronized void synchronizedMethod() {

    }

    public void synchronizedScopeMethod() {
        synchronized (this) {

        }
    }

    public void notSynchronizedMethodFromSynchronizedScopeMethod() {
        synchronized (this) {
            nonSynchronizedMethod();
        }
    }
}