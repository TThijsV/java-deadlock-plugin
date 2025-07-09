package com.maven.plugin.deadlock.testclasses;

class LoopingSynchronizedMethodsClass {

    public void notSynchronizedMethodA() {
        synchronizedMethodB();
    }

    public synchronized void synchronizedMethodB() {
        notSynchronizedMethodC();
    }

    public void notSynchronizedMethodC() {
        notSynchronizedMethodA();
    }

    public void notSynchronizedMethodD() {
        synchronizedScopedMethodE();
    }

    public void synchronizedScopedMethodE() {
        synchronized (this) {
            notSynchronizedMethodF();
        }
    }

    public void notSynchronizedMethodF() {
        notSynchronizedMethodD();
    }
}