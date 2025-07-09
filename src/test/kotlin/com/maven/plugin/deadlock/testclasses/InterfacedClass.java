package com.maven.plugin.deadlock.testclasses;

public class InterfacedClass implements TestInterface {

    @Override
    public void testMethod() {
        otherMethod1();
    }

    @Override
    public void testMethod(int args) {
        otherMethod2();
        otherMethod3();
    }

    public void otherMethod1() {

    }

    public void otherMethod2() {

    }

    public void otherMethod3() {
        otherMethod4();
    }

    public void otherMethod4() {

    }
}
