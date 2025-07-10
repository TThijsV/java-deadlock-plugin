package com.maven.plugin.deadlock.visitor

import com.maven.plugin.deadlock.BasePluginTestCase

// Interfaces resolven werkt (nog) niet vanuit unit test
class InterfaceVisitorTest : BasePluginTestCase() {

    val fileName = "UsingInterfacedClass.java"
    val callInterfaceMethod = "callInterfaceMethod"

    fun testInterfaceVisitorTest() {
        val elementVisitor = runVisitorMethod(fileName, callInterfaceMethod)
        elementVisitor.dropResult()
    }

}
