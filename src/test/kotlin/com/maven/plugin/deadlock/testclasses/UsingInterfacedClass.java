package com.maven.plugin.deadlock.testclasses;

public class UsingInterfacedClass {

    InterfacedClass interfacedClass;

    public void callInterfacedMethod() {
        interfacedClass.testMethod();
    }

    public void callOtherInterfacedMethod() {
        interfacedClass.testMethod(10);
    }
}
