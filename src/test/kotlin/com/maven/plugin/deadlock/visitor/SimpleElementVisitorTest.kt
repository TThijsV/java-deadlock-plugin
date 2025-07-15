package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Tests to see if method scopes calling each other are handled correctly
 */
class SimpleElementVisitorTest : BasePluginTestCase() {

    val fileName = "SimpleClass.java"
    val simpleEmptyMethod = "simpleEmptyMethod"
    val simpleNotEmptyMethod = "simpleNotEmptyMethod"
    val otherNotEmptyMethod = "otherNotEmptyMethod"
    val anotherNotEmptyMethod = "anotherNotEmptyMethod"

    fun testSimpleEmptyMethod() {
        val elementVisitor = runVisitorMethod(fileName, simpleEmptyMethod)
        validateNonSynchronizedMethodVisitor(elementVisitor, simpleEmptyMethod, 1, 0)
        elementVisitor.dropResult()
    }

    fun testSimpleNonEmptyMethod() {
        val elementVisitor = runVisitorMethod(fileName, simpleNotEmptyMethod)
        validateNonSynchronizedMethodVisitor(elementVisitor, simpleNotEmptyMethod, 1, 1)
        val simpleEmptyMethodVisitor = elementVisitor.children.first()
        validateNonSynchronizedMethodVisitor(simpleEmptyMethodVisitor, simpleEmptyMethod, 2, 0)
        elementVisitor.dropResult()
    }

    fun testSimpleOtherNotEmptyMethod() {
        val elementVisitor = runVisitorMethod(fileName, otherNotEmptyMethod)
        validateNonSynchronizedMethodVisitor(elementVisitor, otherNotEmptyMethod, 1, 2)
        val simpleEmptyMethodVisitor = elementVisitor.children.first()
        val simpleNotEmptyMethodVisitor = elementVisitor.children.last()
        validateNonSynchronizedMethodVisitor(simpleEmptyMethodVisitor, simpleEmptyMethod, 2, 0)
        validateNonSynchronizedMethodVisitor(simpleNotEmptyMethodVisitor, simpleNotEmptyMethod, 2, 1)
        val secondSimpleEmptyMethodVisitor = simpleNotEmptyMethodVisitor.children.first()
        validateNonSynchronizedMethodVisitor(secondSimpleEmptyMethodVisitor, simpleEmptyMethod, 3, 0)
        elementVisitor.dropResult()
    }

    fun testSimpleAnotherNotEmptyMethod() {
        val elementVisitor = runVisitorMethod(fileName, "anotherNotEmptyMethod")
        validateNonSynchronizedMethodVisitor(elementVisitor, anotherNotEmptyMethod, 1, 2)
        val simpleEmptyMethodVisitor = elementVisitor.children.first()
        val otherNotEmptyMethodVisitor = elementVisitor.children.last()
        validateNonSynchronizedMethodVisitor(simpleEmptyMethodVisitor, simpleEmptyMethod, 2, 0)
        validateNonSynchronizedMethodVisitor(otherNotEmptyMethodVisitor, otherNotEmptyMethod, 2, 2)
        val secondSimpleEmptyMethodVisitor = otherNotEmptyMethodVisitor.children.first()
        val simpleNotEmptyMethodVisitor = otherNotEmptyMethodVisitor.children.last()
        validateNonSynchronizedMethodVisitor(secondSimpleEmptyMethodVisitor, simpleEmptyMethod, 3, 0)
        validateNonSynchronizedMethodVisitor(simpleNotEmptyMethodVisitor, simpleNotEmptyMethod, 3, 1)
        val thirdSimpleEmptyMethodVisitor = simpleNotEmptyMethodVisitor.children.first()
        validateNonSynchronizedMethodVisitor(thirdSimpleEmptyMethodVisitor, simpleEmptyMethod, 4, 0)
        elementVisitor.dropResult()
    }
}
