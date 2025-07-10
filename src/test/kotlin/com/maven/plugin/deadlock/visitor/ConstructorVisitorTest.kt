package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

class ConstructorVisitorTest : BasePluginTestCase() {

    val fileName = "ConstructorTestClass.java"
    val getInstance = "getInstance"
    val getInstanceWithArgument = "getInstanceWithArgument"
    val constructorTestClass = "ConstructorTestClass"
    val methodA = "methodA"
    val methodB = "methodB"
    val methodC = "methodC"

    fun runConstructorVisitorTest(getInstanceMethod: String) {
        val instanceVisitor = runVisitorMethod(fileName, getInstanceMethod)
        validateNonSynchronizedMethodVisitor(instanceVisitor, getInstanceMethod, 1, 3)

        val defaultConstructorVisitor = instanceVisitor.children.get(0)
        val constructorVisitorWithArgVisitor = instanceVisitor.children.get(1)
        val constructorVisitorWithArgsVisitor = instanceVisitor.children.get(2)
        validateNonSynchronizedMethodVisitor(defaultConstructorVisitor, constructorTestClass, 2,0)
        validateNonSynchronizedMethodVisitor(constructorVisitorWithArgVisitor, constructorTestClass, 2,2)
        validateNonSynchronizedMethodVisitor(constructorVisitorWithArgsVisitor, constructorTestClass, 2, 1)

        val constructorFromConstructorVisitor = constructorVisitorWithArgVisitor.children.get(0)
        val methodAVisitor = constructorVisitorWithArgVisitor.children.get(1)
        validateNonSynchronizedMethodVisitor(constructorFromConstructorVisitor, constructorTestClass, 3,1)
        validateNonSynchronizedMethodVisitor(methodAVisitor, methodA, 3,0)
        val methodBVisitor = constructorFromConstructorVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodBVisitor, methodB, 4,1)
        val methodCVisitor = methodBVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodCVisitor, methodC, 5,0)

        constructorVisitorWithArgsVisitor.children.get(0)
        val secondMethodBVisitor = constructorVisitorWithArgsVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(secondMethodBVisitor, methodB, 3,1)
        val secondMethodCVisitor = secondMethodBVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(secondMethodCVisitor, methodC, 4,0)

        instanceVisitor.dropResult()
    }

    fun testDefaultConstructorVisitor() {
        runConstructorVisitorTest(getInstance)
    }

    fun testConstructorWithArgumentVisitor() {
        runConstructorVisitorTest(getInstanceWithArgument)
    }

}
