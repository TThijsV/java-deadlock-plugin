package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

class SynchronizedVisitorTest : BasePluginTestCase() {

    val fileName = "SynchronizedMethodsClass.java"

    val nonSynchronizedMethod = "nonSynchronizedMethod"
    val synchronizedMethod = "synchronizedMethod"
    val staticSynchronizedMethod = "staticSynchronizedMethod"
    val synchronizedScopeMethod = "synchronizedScopeMethod"
    val synchronizedScopeOnObjectMethod = "synchronizedScopeOnObjectMethod"
    val notSynchronizedMethodFromSynchronizedScopeMethod = "notSynchronizedMethodFromSynchronizedScopeMethod"

    fun testNonSynchronizedMethod() {
        val nonSynchronizedMethodVisitor = runVisitorMethod(fileName, nonSynchronizedMethod)
        validateNonSynchronizedMethodVisitor(nonSynchronizedMethodVisitor, nonSynchronizedMethod, 1, 0)
        nonSynchronizedMethodVisitor.dropResult()
    }

    fun testSynchronizedMethod() {
        val synchronizedMethodVisitor = runVisitorMethod(fileName, synchronizedMethod)
        validateSynchronizedMethodVisitor(synchronizedMethodVisitor, synchronizedMethod, 1, 0, "SynchronizedMethodsClass INSTANCE")
        synchronizedMethodVisitor.dropResult()
    }

    fun testStaticSynchronizedMethod() {
        val staticSynchronizedMethodVisitor = runVisitorMethod(fileName, staticSynchronizedMethod)
        validateSynchronizedMethodVisitor(staticSynchronizedMethodVisitor, staticSynchronizedMethod, 1, 0, "SynchronizedMethodsClass.class")
        staticSynchronizedMethodVisitor.dropResult()
    }

    fun testSynchronizedScopeInMethod() {
        val synchronizedScopeMethodVisitor = runVisitorMethod(fileName, synchronizedScopeMethod)
        validateNonSynchronizedMethodVisitor(synchronizedScopeMethodVisitor, synchronizedScopeMethod, 1, 1)
        val synchronizedScopeVisitor = synchronizedScopeMethodVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 2, 0, "SynchronizedMethodsClass INSTANCE")
        synchronizedScopeMethodVisitor.dropResult()
    }

    fun testSynchronizedScopeOnObjectMethod() {
        val synchronizedScopeMethodVisitor = runVisitorMethod(fileName, synchronizedScopeOnObjectMethod)
        validateNonSynchronizedMethodVisitor(synchronizedScopeMethodVisitor, synchronizedScopeOnObjectMethod, 1, 1)
        val synchronizedScopeVisitor = synchronizedScopeMethodVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 2, 0,"SynchronizedMethodsClass#someList")
        synchronizedScopeMethodVisitor.dropResult()
    }

    fun testNotSynchronizedMethodInSynchronizedScope() {
        val notSynchronizedMethodInSynchronizedScopeVisitor = runVisitorMethod(fileName, notSynchronizedMethodFromSynchronizedScopeMethod)
        validateNonSynchronizedMethodVisitor(notSynchronizedMethodInSynchronizedScopeVisitor, notSynchronizedMethodFromSynchronizedScopeMethod, 1, 1)
        val synchronizedScopeVisitor = notSynchronizedMethodInSynchronizedScopeVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 2, 1, "SynchronizedMethodsClass INSTANCE")
        val notSynchronizedMethodVisitor = synchronizedScopeVisitor.children.first()
        validateMethodVisitor(notSynchronizedMethodVisitor, nonSynchronizedMethod, 3, 0, false, false, true, false)
        notSynchronizedMethodInSynchronizedScopeVisitor.dropResult()
    }

}
