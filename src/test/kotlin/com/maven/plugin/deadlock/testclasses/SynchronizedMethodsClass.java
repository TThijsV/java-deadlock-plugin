package com.maven.plugin.deadlock.testclasses;

import java.util.ArrayList;
import java.util.List;

class SynchronizedMethodsClass {

    List<Integer> someList = new ArrayList<>();

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
        synchronized (someList) {

        }
    }

    public void notSynchronizedMethodFromSynchronizedScopeMethod() {
        synchronized (this) {
            nonSynchronizedMethod();
        }
    }
}