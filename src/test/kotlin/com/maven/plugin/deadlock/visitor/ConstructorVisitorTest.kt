package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

/**
 * Test to see if constructors are handled correctly as new scopes
 */
class ConstructorVisitorTest : BasePluginTestCase() {

    val fileName = "ConstructorTestClass.java"
    val getInstance = "getInstance"
    val getInstanceWithArgument = "getInstanceWithArgument"
    val constructorTestClass = "ConstructorTestClass"
    val getInstanceWithArguments = "getInstanceWithArguments"
    val methodA = "methodA"
    val methodB = "methodB"
    val methodC = "methodC"

    fun testDefaultConstructorVisitor() {
        val instanceVisitor = runVisitorMethod(fileName, getInstance)
        validateNonSynchronizedMethodVisitor(instanceVisitor, getInstance, 1, 1)
        val defaultConstructorVisitor = instanceVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(defaultConstructorVisitor, constructorTestClass, 2,1)
        val methodCVisitor = defaultConstructorVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodCVisitor, methodC, 3,0)
        instanceVisitor.dropResult()
    }

    fun testConstructorWithArgumentVisitor() {
        val instanceVisitor = runVisitorMethod(fileName, getInstanceWithArgument)
        validateNonSynchronizedMethodVisitor(instanceVisitor, getInstanceWithArgument, 1, 1)
        val constructorVisitorWithArgVisitor = instanceVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(constructorVisitorWithArgVisitor, constructorTestClass, 2,2)
        val constructorFromConstructorVisitor = constructorVisitorWithArgVisitor.children.get(0)
        val methodAVisitor = constructorVisitorWithArgVisitor.children.get(1)
        validateNonSynchronizedMethodVisitor(constructorFromConstructorVisitor, constructorTestClass, 3,1)
        validateNonSynchronizedMethodVisitor(methodAVisitor, methodA, 3,0)
        val methodBVisitor = constructorFromConstructorVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodBVisitor, methodB, 4,1)
        val methodCVisitor = methodBVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodCVisitor, methodC, 5,0)
        instanceVisitor.dropResult()
    }

    fun testConstructorWithArgumentsVisitor() {
        val instanceVisitor = runVisitorMethod(fileName, getInstanceWithArguments)
        validateNonSynchronizedMethodVisitor(instanceVisitor, getInstanceWithArguments, 1, 1)
        val constructorVisitorWithArgsVisitor = instanceVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(constructorVisitorWithArgsVisitor, constructorTestClass, 2,1)
        val methodBVisitor = constructorVisitorWithArgsVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodBVisitor, methodB, 3,1)
        val methodCVisitor = methodBVisitor.children.get(0)
        validateNonSynchronizedMethodVisitor(methodCVisitor, methodC, 4,0)
        instanceVisitor.dropResult()
    }

}
