package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Test to determine correct behavior on loops with synchronized scopes
 */
class LoopingSynchronizedVisitorTest : BasePluginTestCase() {

    val fileName = "LoopingSynchronizedMethodsClass.java"

    val notSynchronizedMethodA = "notSynchronizedMethodA"
    val synchronizedMethodB = "synchronizedMethodB"
    val notSynchronizedMethodC = "notSynchronizedMethodC"

    val notSynchronizedMethodD = "notSynchronizedMethodD"
    val synchronizedScopedMethodE = "synchronizedScopedMethodE"
    val notSynchronizedMethodF = "notSynchronizedMethodF"

    fun testLoopingSynchronizedMethods() {
        val notSynchronizedMethodAVisitor = runVisitorMethod(fileName, notSynchronizedMethodA)
        validateMethodVisitor(notSynchronizedMethodAVisitor, notSynchronizedMethodA, 1, 1, false, false, true)
        val synchronizedMethodBVisitor = notSynchronizedMethodAVisitor.children.first()
        validateMethodVisitor(synchronizedMethodBVisitor, synchronizedMethodB, 2, 1, false, true, true, 0, "LoopingSynchronizedMethodsClass INSTANCE")
        val notSynchronizedMethodCVisitor = synchronizedMethodBVisitor.children.first()
        validateMethodVisitor(notSynchronizedMethodCVisitor, notSynchronizedMethodC, 3, 1, false, false, true)
        val secondNotSynchronizedMethodAVisitor = notSynchronizedMethodCVisitor.children.first()
        validateMethodVisitor(secondNotSynchronizedMethodAVisitor, notSynchronizedMethodA, 4, 0, true, false, true)
        notSynchronizedMethodAVisitor.dropResult()
    }

    fun testLoopingSynchronizedScopeInMethods() {
        val notSynchronizedMethodDVisitor = runVisitorMethod(fileName, notSynchronizedMethodD)
        notSynchronizedMethodDVisitor.dropResult()
        validateMethodVisitor(notSynchronizedMethodDVisitor, notSynchronizedMethodD, 1, 1, false, false, true)
        val synchronizedScopedMethodEVisitor = notSynchronizedMethodDVisitor.children.first()
        validateMethodVisitor(synchronizedScopedMethodEVisitor, synchronizedScopedMethodE, 2, 1, false, false, false)
        val synchronizedScopeVisitor = synchronizedScopedMethodEVisitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 3, 1, "LoopingSynchronizedMethodsClass INSTANCE")
        val notSynchronizedMethodFVisitor = synchronizedScopeVisitor.children.first()
        validateMethodVisitor(notSynchronizedMethodFVisitor, notSynchronizedMethodF, 4, 1, false, false, true)
        val secondNotSynchronizedMethodDVisitor = notSynchronizedMethodFVisitor.children.first()
        validateMethodVisitor(secondNotSynchronizedMethodDVisitor, notSynchronizedMethodD, 5, 0, true, false, true)
    }
}
