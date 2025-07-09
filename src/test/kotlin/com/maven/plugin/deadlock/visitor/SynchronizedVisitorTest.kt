package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

class SynchronizedVisitorTest : BasePluginTestCase() {

    val fileName = "SynchronizedMethodsClass.java"

    val nonSynchronizedMethod = "nonSynchronizedMethod"
    val synchronizedMethod = "synchronizedMethod"
    val synchronizedScopeMethod = "synchronizedScopeMethod"
    val notSynchronizedMethodFromSynchronizedScopeMethod = "notSynchronizedMethodFromSynchronizedScopeMethod"

    fun testNonSynchronizedMethod() {
        val nonSynchronizedMethodVisitor = runVisitorMethod(fileName, nonSynchronizedMethod)
        validateNonSynchronizedMethodVisitor(nonSynchronizedMethodVisitor, nonSynchronizedMethod, 1, 0)
        nonSynchronizedMethodVisitor.dropResult()
    }

    fun testSynchronizedMethod() {
        val synchronizedMethodVisitor = runVisitorMethod(fileName, synchronizedMethod)
        validateSynchronizedMethodVisitor(synchronizedMethodVisitor, synchronizedMethod, 1, 0)
        synchronizedMethodVisitor.dropResult()
    }

    fun testSynchronizedScopeInMethod() {
        val synchronizedScopeMethodVisitor = runVisitorMethod(fileName, synchronizedScopeMethod)
        validateNonSynchronizedMethodVisitor(synchronizedScopeMethodVisitor, synchronizedScopeMethod, 1, 1)
        val synchronizedScopeVisitor = synchronizedScopeMethodVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 2, 0)
        synchronizedScopeMethodVisitor.dropResult()
    }

    fun testNotSynchronizedMethodInSynchronizedScope() {
        val notSynchronizedMethodInSynchronizedScopeVisitor = runVisitorMethod(fileName, notSynchronizedMethodFromSynchronizedScopeMethod)
        validateNonSynchronizedMethodVisitor(notSynchronizedMethodInSynchronizedScopeVisitor, notSynchronizedMethodFromSynchronizedScopeMethod, 1, 1)
        val synchronizedScopeVisitor = notSynchronizedMethodInSynchronizedScopeVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 2, 1)
        val notSynchronizedMethodVisitor = synchronizedScopeVisitor.children.first()
        validateMethodVisitor(notSynchronizedMethodVisitor, nonSynchronizedMethod, 3, 0, false, false, true, false)
        notSynchronizedMethodInSynchronizedScopeVisitor.dropResult()
    }

}
