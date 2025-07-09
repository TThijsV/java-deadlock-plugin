package com.maven.plugin.deadlock.testclasses;

import com.maven.plugin.deadlock.annotations.NotSynchronized;
import com.maven.plugin.deadlock.annotations.Synchronized;

@Synchronized
public class SynchronizedAnnotatedClass {

    public void publicMethod() {

    }

    protected void protectedMethod() {

    }

    private void privateMethod() {

    }

    public static void publicStaticMethod() {

    }

    protected static void protectedStaticMethod() {

    }

    private static void privateStaticMethod() {

    }

    @NotSynchronized
    public void publicNotSynchronizedAnnotatedMethod() {

    }

    @NotSynchronized
    protected void protectedNotSynchronizedAnnotatedMethod() {

    }

    @NotSynchronized
    private void privateNotSynchronizedAnnotatedMethod() {

    }
}
