package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * An extensive test to test multiple features in one go
 */
class ExtensiveElementVisitorTest : BasePluginTestCase() {

    val fileName = "ExtensiveTestClass.java"
    val methodA = "methodA"
    val methodB1 = "methodB1"
    val methodB2 = "methodB2"
    val methodB3 = "methodB3"
    val methodB4 = "methodB4"
    val methodB5 = "methodB5"
    val methodB6 = "methodB6"
    val methodC1 = "methodC1"
    val methodC2 = "methodC2"
    val methodC3 = "methodC3"
    val methodC4 = "methodC4"
    val methodC5 = "methodC5"
    val methodC6 = "methodC6"
    val methodD1 = "methodD1"
    val methodD2 = "methodD2"
    val methodD3 = "methodD3"
    val methodD4 = "methodD4"
    val methodD5 = "methodD5"
    val methodD6 = "methodD6"

    fun testExtensiveClassVisitor() {
        val elementVisitor = runVisitorMethod(fileName, methodA)
        elementVisitor.dropResult()

        validateMethodVisitor(elementVisitor, methodA, 1, 6, false, false, false, 3)

        // First branch from method A, multiple synchronizations on same object, no looping branch, low risk
        val methodB1Visitor = elementVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodB1Visitor, methodB1, 2, 1, 1)
        val methodC1Visitor = methodB1Visitor.children.first()
        validateSynchronizedMethodVisitor(methodC1Visitor, methodC1, 3, 2, 1, "ExtensiveTestClass INSTANCE")
        val methodD1Visitor = methodC1Visitor.children.first()
        validateMethodVisitor(methodD1Visitor, methodD1, 4, 0, false, false, true)
        val synchronizedScopeVisitor = methodC1Visitor.children.last()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor, 4, 1, 1, "ExtensiveTestClass INSTANCE")
        val secondMethodD1Visitor = synchronizedScopeVisitor.children.first()
        validateMethodVisitor(secondMethodD1Visitor, methodD1, 5, 0, false, false, true, 0)

        // Second branch from method A, looping, no deadlock risk
        val methodB2Visitor = elementVisitor.children.get(1)
        validateMethodVisitor(methodB2Visitor, methodB2, 2, 1, false, false, true)
        val methodC2Visitor = methodB2Visitor.children.first()
        validateMethodVisitor(methodC2Visitor, methodC2, 3, 1, false, true, true, 0, "ExtensiveTestClass INSTANCE")
        val methodDVisitor = methodC2Visitor.children.first()
        validateMethodVisitor(methodDVisitor, methodD2, 4, 1, false, false, true)
        val secondMethodB2Visitor = methodDVisitor.children.first()
        validateMethodVisitor(secondMethodB2Visitor, methodB2, 5, 0, true, false, true)

        // Third branch from method A, looping, no deadlock
        val methodB3Visitor = elementVisitor.children.get(2)
        validateNonSynchronizedMethodVisitor(methodB3Visitor, methodB3, 2, 1)
        val methodC3Visitor = methodB3Visitor.children.first()
        validateNonSynchronizedMethodVisitor(methodC3Visitor, methodC3, 3, 1)
        val methodD3Visitor = methodC3Visitor.children.first()
        validateNonSynchronizedMethodVisitor(methodD3Visitor, methodD3, 4, 1)
        val secondMethodB3Visitor = methodD3Visitor.children.first()
        validateMethodVisitor(secondMethodB3Visitor, methodB3, 5, 0, true, false, false, 0)

        // Fourth branch from method A, no looping, multiple locks, medium deadlock risk
        val methodB4Visitor = elementVisitor.children.get(3)
        validateNonSynchronizedMethodVisitor(methodB4Visitor, methodB4, 2, 1, 2)
        val methodC4Visitor = methodB4Visitor.children.first()
        validateSynchronizedMethodVisitor(methodC4Visitor, methodC4, 3, 2, 2, "ExtensiveTestClass INSTANCE")
        val methodD4Visitor = methodC4Visitor.children.first()
        validateMethodVisitor(methodD4Visitor, methodD4, 4, 0, false, false, true)
        val synchronizedScopeVisitor4 = methodC4Visitor.children.last()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor4, 4, 1, 2, "Object INSTANCE")
        val secondMethodD4Visitor = synchronizedScopeVisitor4.children.first()
        validateMethodVisitor(secondMethodD4Visitor, methodD4, 5, 0, false, false, true)

        // Fifth branch from method A, no looping, multiple alternating locks, high deadlock risk
        val methodB5Visitor = elementVisitor.children.get(4)
        validateSynchronizedMethodVisitor(methodB5Visitor, methodB5, 2, 1, 3, "ExtensiveTestClass INSTANCE")
        val methodC5Visitor = methodB5Visitor.children.first()
        validateMethodVisitor(methodC5Visitor, methodC5, 3, 1, false, false, true, 3)
        val synchronizedScopeVisitor5 = methodC5Visitor.children.first()
        validateSynchronizedScopeVisitor(synchronizedScopeVisitor5, 4, 1, 3, "Object INSTANCE")
        val methodD5Visitor = synchronizedScopeVisitor5.children.first()
        validateSynchronizedMethodVisitor(methodD5Visitor, methodD5, 5, 0, 3, "ExtensiveTestClass INSTANCE")

        // Sixth branch from method A, looping, multiple locks, high deadlock risk
        val methodB6Visitor = elementVisitor.children.get(5)
        validateMethodVisitor(methodB6Visitor, methodB6, 2, 1, false, false, true, 3)
        val methodC6Visitor = methodB6Visitor.children.first()
        validateSynchronizedMethodVisitor(methodC6Visitor, methodC6, 3, 1, 3, "ExtensiveTestClass INSTANCE")
        val methodD6Visitor = methodC6Visitor.children.first()
        validateSynchronizedMethodVisitor(methodD6Visitor, methodD6, 4, 1, 3, "ExtensiveTestClass.class")
        val secondMethodB6Visitor = methodD6Visitor.children.first()
        validateMethodVisitor(secondMethodB6Visitor, methodB6, 5, 0, true, false, true, 3)
        elementVisitor.dropResult()
    }
}
