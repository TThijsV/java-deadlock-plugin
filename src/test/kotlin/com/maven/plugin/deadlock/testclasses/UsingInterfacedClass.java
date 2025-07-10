package com.maven.plugin.deadlock.testclasses;

public class UsingInterfacedClass {

    TestInterface testInterface;

    InterfacedClass interfacedClass;

    public void callInterfaceMethod() {
        testInterface.testMethod();
    }

    public void callOtherInterfaceMethod() {
        testInterface.testMethod(10);
    }

    public void callInterfacedClassMethod() {
        interfacedClass.testMethod();
    }

    public void callOtherInterfacedClassMethod() {
        interfacedClass.testMethod(10);
    }
}
