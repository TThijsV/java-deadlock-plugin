package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Specific test to see if the @Synchronized and @NotSynchronized annotations are handeld correctly
 */
class AnnotatedElementVisitorTest : BasePluginTestCase() {

    val fileName = "SynchronizedAnnotatedClass.java"
    val publicMethod = "publicMethod"
    val protectedMethod = "protectedMethod"
    val privateMethod = "privateMethod"

    val publicStaticMethod = "publicStaticMethod"
    val protectedStaticMethod = "protectedStaticMethod"
    val privateStaticMethod = "privateStaticMethod"

    val publicNotSynchronizedAnnotatedMethod = "publicNotSynchronizedAnnotatedMethod"
    val protectedNotSynchronizedAnnotatedMethod = "protectedNotSynchronizedAnnotatedMethod"
    val privateNotSynchronizedAnnotatedMethod = "privateNotSynchronizedAnnotatedMethod"


    fun testMethodsInSynchronizedAnnotatedClass() {
        val publicMethodVisitor = runVisitorMethod(fileName, publicMethod)
        validateSynchronizedMethodVisitor(publicMethodVisitor, publicMethod, 1, 0, "MUTEX")
        val protectedMethodVisitor = runVisitorMethod(fileName, protectedMethod)
        validateSynchronizedMethodVisitor(protectedMethodVisitor, protectedMethod, 1, 0, "MUTEX")
        val privateMethodVisitor = runVisitorMethod(fileName, privateMethod)
        validateNonSynchronizedMethodVisitor(privateMethodVisitor, privateMethod, 1, 0)
    }

    fun testStaticMethodsInSynchronizedAnnotatedClass() {
        val publicStaticMethodVisitor = runVisitorMethod(fileName, publicStaticMethod)
        validateNonSynchronizedMethodVisitor(publicStaticMethodVisitor, publicStaticMethod, 1, 0)
        val protectedStaticMethodVisitor = runVisitorMethod(fileName, protectedStaticMethod)
        validateNonSynchronizedMethodVisitor(protectedStaticMethodVisitor, protectedStaticMethod, 1, 0)
        val privateStaticMethodVisitor = runVisitorMethod(fileName, privateStaticMethod)
        validateNonSynchronizedMethodVisitor(privateStaticMethodVisitor, privateStaticMethod, 1, 0)
    }

    fun testNotSynchronizedAnnotatedMethodsInSynchronizedAnnotatedClass() {
        val publicNotSynchronizedAnnotatedMethodVisitor = runVisitorMethod(fileName, publicNotSynchronizedAnnotatedMethod)
        validateNonSynchronizedMethodVisitor(publicNotSynchronizedAnnotatedMethodVisitor, publicNotSynchronizedAnnotatedMethod, 1, 0)
        val protectedNotSynchronizedAnnotatedMethodVisitor = runVisitorMethod(fileName, protectedNotSynchronizedAnnotatedMethod)
        validateNonSynchronizedMethodVisitor(protectedNotSynchronizedAnnotatedMethodVisitor, protectedNotSynchronizedAnnotatedMethod, 1, 0)
        val privateNotSynchronizedAnnotatedMethodVisitor = runVisitorMethod(fileName, privateNotSynchronizedAnnotatedMethod)
        validateNonSynchronizedMethodVisitor(privateNotSynchronizedAnnotatedMethodVisitor, privateNotSynchronizedAnnotatedMethod, 1, 0)
    }
}
