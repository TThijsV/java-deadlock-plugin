package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

class ExtensiveElementVisitorTest : BasePluginTestCase() {

    val fileName = "ExtensiveTestClass.java"
    val methodA = "methodA"
    val methodB1 = "methodB1"
    val methodB2 = "methodB2"
    val methodB3 = "methodB3"
    val methodC1 = "methodC1"
    val methodC2 = "methodC2"
    val methodC3 = "methodC3"
    val methodD1 = "methodD1"
    val methodD2 = "methodD2"
    val methodD3 = "methodD3"

    fun testExtensiveClassVisitor() {
        val elementVisitor = runVisitorMethod(fileName, methodA)
        validateMethodVisitor(elementVisitor, methodA, 1, 3, false, false, false, true)

        // First branch from method A, no looping branch
        val methodB1Visitor = elementVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodB1Visitor, methodB1, 2, 1)
        val methodC1Visitor = methodB1Visitor.children.first()
        validateSynchronizedMethodVisitor(methodC1Visitor, methodC1, 3, 2)
        val methodD1Visitor = methodC1Visitor.children.first()
        validateMethodVisitor(methodD1Visitor, methodD1, 4, 0, false, false, true, false)
        val synchronizedScopeVisitor = methodC1Visitor.children.last()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 4, 1)
        val secondMethodD1Visitor = synchronizedScopeVisitor.children.first()
        validateMethodVisitor(secondMethodD1Visitor, methodD1, 5, 0, false, false, true, false)


        // Second branch from method A, looping, deadlock
        val methodB2Visitor = elementVisitor.children.get(1)
        validateMethodVisitor(methodB2Visitor, methodB2, 2, 1, false, false, true, true)
        val methodC2Visitor = methodB2Visitor.children.first()
        validateMethodVisitor(methodC2Visitor, methodC2, 3, 1, false, true, true, true)
        val methodDVisitor = methodC2Visitor.children.first()
        validateMethodVisitor(methodDVisitor, methodD2, 4, 1, false, false, true, true)
        val secondMethodB2Visitor = methodDVisitor.children.first()
        validateMethodVisitor(secondMethodB2Visitor, methodB2, 5, 0, true, false, true, true)

        // Third branch from method A, looping, no deadlock
        val methodB3Visitor = elementVisitor.children.get(2)
        validateNonSynchronizedMethodVisitor(methodB3Visitor, methodB3, 2, 1)
        val methodC3Visitor = methodB3Visitor.children.first()
        validateNonSynchronizedMethodVisitor(methodC3Visitor, methodC3, 3, 1)
        val methodD3Visitor = methodC3Visitor.children.first()
        validateNonSynchronizedMethodVisitor(methodD3Visitor, methodD3, 4, 1)
        val secondMethodB3Visitor = methodD3Visitor.children.first()
        validateMethodVisitor(secondMethodB3Visitor, methodB3, 5, 0, true, false, false, false)

        elementVisitor.dropResult()
    }
}
