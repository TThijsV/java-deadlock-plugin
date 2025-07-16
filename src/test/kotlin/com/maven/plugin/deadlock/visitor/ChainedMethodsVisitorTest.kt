package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Test to see if chained calls are handled correctly
 */
class ChainedMethodsVisitorTest : BasePluginTestCase() {

    val fileName = "ChainedMethodsClass.java"
    val method1 = "method1"
    val methodA = "methodA"
    val methodB = "methodB"
    val methodC = "methodC"
    val methodD = "methodD"
    val methodE = "methodE"
    val methodF = "methodF"

    val methodWithConstructorInArgument = "methodWithConstructorInArgument"
    val chainedMethodsClass = "ChainedMethodsClass"
    val methodWithArgument = "methodWithArgument"
    val methodWithMethodInArgument = "methodWithMethodInArgument"

    val methodWithConstructorWithChainedMethodAndConstructorArgument = "methodWithConstructorWithChainedMethodAndConstructorArgument"
    val methodWithConstructorWithChainedMethodAndMethodInArgument = "methodWithConstructorWithChainedMethodAndMethodInArgument"

    fun testChainedMethods() {
        val elementVisitor = runVisitorMethod(fileName, method1)
        validateNonSynchronizedMethodVisitor(elementVisitor, method1, 1, 3)
        val methodAVisitor = elementVisitor.children.get(0)
        val methodBVisitor = elementVisitor.children.get(1)
        val methodCVisitor = elementVisitor.children.get(2)
        validateNonSynchronizedMethodVisitor(methodBVisitor, methodB, 2, 2)
        validateNonSynchronizedMethodVisitor(methodAVisitor, methodA, 2, 0)
        validateNonSynchronizedMethodVisitor(methodCVisitor, methodC, 2, 0)

        val methodDVisitor = methodBVisitor.children.get(0)
        val methodEVisitor = methodBVisitor.children.get(1)
        validateNonSynchronizedMethodVisitor(methodDVisitor, methodD, 3, 1)
        validateNonSynchronizedMethodVisitor(methodEVisitor, methodE, 3, 0)

        val methodFVisitor = methodDVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodFVisitor, methodF, 4, 0)
        elementVisitor.dropResult()
    }

    fun testMethodWithConstructorInArgument() {
        val elementVisitor = runVisitorMethod(fileName, methodWithConstructorInArgument)
        validateNonSynchronizedMethodVisitor(elementVisitor, methodWithConstructorInArgument, 1, 2)

        val methodWithArgumentVisitor = elementVisitor.children.get(0)
        val constructorVisitor = elementVisitor.children.get(1)
        validateNonSynchronizedMethodVisitor(methodWithArgumentVisitor, methodWithArgument, 2, 1)
        validateNonSynchronizedMethodVisitor(constructorVisitor, chainedMethodsClass, 2, 0)

        val methodEVisitor = methodWithArgumentVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodEVisitor, methodE, 3, 0)

        elementVisitor.dropResult()
    }

    fun testMethodWithMethodInArgument() {
        val elementVisitor = runVisitorMethod(fileName, methodWithMethodInArgument)
        validateNonSynchronizedMethodVisitor(elementVisitor, methodWithMethodInArgument, 1, 2)

        val methodWithArgumentVisitor = elementVisitor.children.get(0)
        val methodFVisitor = elementVisitor.children.get(1)
        validateNonSynchronizedMethodVisitor(methodWithArgumentVisitor, methodWithArgument, 2, 1)
        validateNonSynchronizedMethodVisitor(methodFVisitor, methodF, 2, 0)

        val methodEVisitor = methodWithArgumentVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodEVisitor, methodE, 3, 0)

        elementVisitor.dropResult()
    }

    fun testMethodWithConstructorWithChainedMethodAndConstructorArgument() {
        val elementVisitor = runVisitorMethod(fileName, methodWithConstructorWithChainedMethodAndConstructorArgument)
        validateNonSynchronizedMethodVisitor(elementVisitor, methodWithConstructorWithChainedMethodAndConstructorArgument, 1, 3)

        val methodWithArgumentVisitor = elementVisitor.children.get(0)
        val firstConstructorVisitor = elementVisitor.children.get(1)
        val secondConstructorVisitor = elementVisitor.children.get(2)
        validateNonSynchronizedMethodVisitor(methodWithArgumentVisitor, methodWithArgument, 2, 1)
        validateNonSynchronizedMethodVisitor(firstConstructorVisitor, chainedMethodsClass, 2, 0)
        validateNonSynchronizedMethodVisitor(secondConstructorVisitor, chainedMethodsClass, 2, 0)

        val methodEVisitor = methodWithArgumentVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodEVisitor, methodE, 3, 0)

        elementVisitor.dropResult()
    }


}
