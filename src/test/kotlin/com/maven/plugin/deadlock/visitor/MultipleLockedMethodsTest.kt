package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Test to determine deadlock risk level when multiple locks are used
 */
class MultipleLockedMethodsTest : BasePluginTestCase() {

    val fileName = "MultipleLockedMethods.java"
    val synchronizedMethodCallingSynchronizedMethod = "synchronizedMethodCallingSynchronizedMethod"
    val emptySynchronizedMethod = "emptySynchronizedMethod"
    val emptyStaticSynchronizedMethod = "emptyStaticSynchronizedMethod"
    val staticSynchronizedMethodCallingSynchronizedMethod = "staticSynchronizedMethodCallingSynchronizedMethod"
    val synchronizedMethodCallingStaticSynchronizedMethod = "synchronizedMethodCallingStaticSynchronizedMethod"
    val staticSynchronizedMethodCallingStaticSynchronizedMethod = "staticSynchronizedMethodCallingStaticSynchronizedMethod"

    fun testSynchronizedMethodCallingSynchronizedMethod() {
        val synchronizedVisitor = runVisitorMethod(fileName, synchronizedMethodCallingSynchronizedMethod)
        validateSynchronizedMethodVisitor(synchronizedVisitor, synchronizedMethodCallingSynchronizedMethod, 1, 1, 1, "MultipleLockedMethods INSTANCE")
        val otherSynchronizedVisitor = synchronizedVisitor.children.first()
        validateSynchronizedMethodVisitor(otherSynchronizedVisitor, emptySynchronizedMethod, 2, 0, 1, "MultipleLockedMethods INSTANCE")
        synchronizedVisitor.dropResult()
    }

    fun testStaticSynchronizedMethodCallingStaticSynchronizedMethod() {
        val staticSynchronizedVisitor = runVisitorMethod(fileName, staticSynchronizedMethodCallingStaticSynchronizedMethod)
        validateSynchronizedMethodVisitor(staticSynchronizedVisitor, staticSynchronizedMethodCallingStaticSynchronizedMethod, 1, 1, "MultipleLockedMethods.class")
        val otherStaticSynchronizedVisitor = staticSynchronizedVisitor.children.first()
        validateSynchronizedMethodVisitor(otherStaticSynchronizedVisitor, emptyStaticSynchronizedMethod, 2, 0, "MultipleLockedMethods.class")
        staticSynchronizedVisitor.dropResult()
    }

    fun testSynchronizedMethodCallingStaticSynchronizedMethod() {
        val synchronizedVisitorTest = runVisitorMethod(fileName, synchronizedMethodCallingStaticSynchronizedMethod)
        validateSynchronizedMethodVisitor(synchronizedVisitorTest, synchronizedMethodCallingStaticSynchronizedMethod, 1, 1, 2, "MultipleLockedMethods INSTANCE")
        val staticSynchronizedVisitor = synchronizedVisitorTest.children.first()
        validateSynchronizedMethodVisitor(staticSynchronizedVisitor, emptyStaticSynchronizedMethod, 2, 0, 2, "MultipleLockedMethods.class")
        synchronizedVisitorTest.dropResult()
    }


    fun testStaticSynchronizedMethodCallingSynchronizedMethod() {
        val synchronizedVisitorTest = runVisitorMethod(fileName, staticSynchronizedMethodCallingSynchronizedMethod)
        validateSynchronizedMethodVisitor(synchronizedVisitorTest, staticSynchronizedMethodCallingSynchronizedMethod, 1, 1, 2,"MultipleLockedMethods.class")
        val staticSynchronizedVisitor = synchronizedVisitorTest.children.first()
        validateSynchronizedMethodVisitor(staticSynchronizedVisitor, emptySynchronizedMethod, 2, 0, 2, "MultipleLockedMethods INSTANCE")
        synchronizedVisitorTest.dropResult()
    }
}
