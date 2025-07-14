package com.maven.plugin.deadlock.testclasses;

public class ExtensiveTestClass {

    Object someObject = new Object();

    public void methodA() {
        methodB1();
        methodB2();
        methodB3();
        methodB4();
    }

    public void methodB1() {
        methodC1();
    }

    public synchronized void methodC1() {
        methodD1();
        synchronized (this) {
            methodD1();
        }
    }

    public void methodD1() {

    }

    public void methodD4() {

    }

    public void methodB2() {
        methodC2();
    }

    public synchronized void methodC2() {
        methodD2();
    }

    public void methodD2() {
        methodB2();
    }

    public void methodB3() {
        methodC3();
    }

    public void methodC3() {
        methodD3();
    }

    public void methodD3() {
        methodB3();
    }

    public void methodB4() {
        methodC4();
    }


    public synchronized void methodC4() {
        methodD4();
        synchronized (someObject) {
            methodD4();
        }
    }

}
