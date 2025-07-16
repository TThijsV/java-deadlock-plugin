package com.maven.plugin.deadlock.testclasses;

public class ChainedMethodsClass {

    public ChainedMethodsClass() {

    }

    public static void callConstructor() {
        new ChainedMethodsClass();
    }

    public void method1() {
        methodC().methodB().methodA();
    }

    public ChainedMethodsClass methodA() {
        return this;
    }

    public ChainedMethodsClass methodB() {
        return methodE().methodD();
    }

    public ChainedMethodsClass methodC() {
        return this;
    }

    public ChainedMethodsClass methodD() {
        return methodF();
    }

    public ChainedMethodsClass methodE() {
        return this;
    }

    public ChainedMethodsClass methodF() {
        return this;
    }

    public void methodWithConstructorInArgument() {
        methodWithArgument(new ChainedMethodsClass());
    }

    public void methodWithMethodInArgument() {
        methodWithArgument(methodF());
    }

    public void methodWithArgument(ChainedMethodsClass arg) {
        methodE();
    }

    public void methodWithConstructorWithChainedMethodAndConstructorArgument() {
        new ChainedMethodsClass().methodWithArgument(new ChainedMethodsClass());
    }

    public void methodWithConstructorWithChainedMethodAndMethodInArgument() {
        new ChainedMethodsClass().methodWithArgument(new ChainedMethodsClass());
    }
}
