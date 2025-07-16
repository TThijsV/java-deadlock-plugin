package com.maven.plugin.deadlock.testclasses;

class LoopingClass {

    public void methodA() {
        methodB();
    }

    public void methodB() {
        methodC();
    }

    public void methodC() {
        methodA();
    }
}