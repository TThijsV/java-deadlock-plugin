package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

class LoopingVisitorTest : BasePluginTestCase() {

    val fileName = "LoopingClass.java"
    val methodA = "methodA"
    val methodB = "methodB"
    val methodC = "methodC"

    fun runLoopingMethodsTest(method1: String, method2: String, method3: String) {
        val method1Visitor = runVisitorMethod(fileName, method1)
        validateNonSynchronizedMethodVisitor(method1Visitor, method1, 1, 1)
        val method2Visitor = method1Visitor.children.first()
        validateNonSynchronizedMethodVisitor(method2Visitor, method2, 2, 1)
        val method3Visitor = method2Visitor.children.first()
        validateNonSynchronizedMethodVisitor(method3Visitor, method3, 3, 1)
        val secondMethod1Visitor = method3Visitor.children.first()
        validateMethodVisitor(secondMethod1Visitor, method1, 4, 0, true, false, false, false)
        method1Visitor.dropResult()
    }

    fun testLoopingMethodsOrder1() {
        runLoopingMethodsTest(methodA, methodB, methodC)
    }
    fun testLoopingMethodsOrder2() {
        runLoopingMethodsTest(methodB, methodC, methodA)
    }
    fun testLoopingMethodsOrder3() {
        runLoopingMethodsTest(methodC, methodA, methodB)
    }
}
