package com.maven.plugin.deadlock.testclasses;

class SimpleClass {

    public void simpleEmptyMethod() {

    }

    public void simpleNotEmptyMethod() {
        simpleEmptyMethod();
    }

    public void otherNotEmptyMethod() {
        simpleEmptyMethod();
        simpleNotEmptyMethod();
    }

    public void anotherNotEmptyMethod() {
        simpleEmptyMethod();
        otherNotEmptyMethod();
    }
}