package com.maven.plugin.deadlock.testclasses;

public class MultipleLockedMethods {

    SynchronizedAnnotatedClass synchronizedAnnotatedClass = new SynchronizedAnnotatedClass();
    static MultipleLockedMethods multipleLockedMethods = new MultipleLockedMethods();

    public synchronized void emptySynchronizedMethod() {

    }

    public static synchronized void emptyStaticSynchronizedMethod() {

    }

    public synchronized void synchronizedMethodCallingSynchronizedMethod() {
        emptySynchronizedMethod();
    }

    public synchronized void synchronizedMethodCallingStaticSynchronizedMethod() {
        emptyStaticSynchronizedMethod();
    }

    public static synchronized void staticSynchronizedMethodCallingSynchronizedMethod() {
        multipleLockedMethods.emptySynchronizedMethod();
    }

    public static synchronized void staticSynchronizedMethodCallingStaticSynchronizedMethod() {
        emptyStaticSynchronizedMethod();
    }

    public synchronized void synchronizedMethodCallingMutexSynchronizedMethod() {
        synchronizedAnnotatedClass.publicMethod();
    }

    public static void synchronizedMethodCallingStaticMutexSynchronizedMethod() {
        SynchronizedAnnotatedClass.publicStaticMethod();
    }

}
