package com.maven.plugin.deadlock.testclasses;

public class ConstructorTestClass {

    public ConstructorTestClass() {
        methodC();
    }

    public ConstructorTestClass(int arg) {
        this(arg, "empty string");
        methodA();
    }

    public ConstructorTestClass(int arg1, String arg2) {
        methodB();
    }

    public static ConstructorTestClass getInstance() {
        return new ConstructorTestClass();
    }

    public static ConstructorTestClass getInstanceWithArgument() {
        return new ConstructorTestClass(42);
    }

    public static ConstructorTestClass getInstanceWithArguments() {
        return new ConstructorTestClass(42, "42");
    }

    public void methodA() {

    }

    public void methodB() {
        methodC();
    }

    public void methodC() {

    }
}
