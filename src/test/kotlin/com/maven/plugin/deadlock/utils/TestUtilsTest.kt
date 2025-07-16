package com.maven.plugin.deadlock.utils

import com.maven.plugin.deadlock.BasePluginTestCase

class TestUtilsTest : BasePluginTestCase() {

    fun testFileFound() {
        val file = getFile("SimpleClass.java")
        assertNotNull(file)
    }

    fun testFileNotFound() {
        val file = getFile( "unknownfile.java")
        assertNull(file)
    }

    fun testFileContainsMethods() {
        val methodsMap = getMethodsFromJavaFile("SimpleClass.java")
        assertSize(4, methodsMap.values)
        assertTrue(methodsMap.containsKey("simpleEmptyMethod"))
        assertTrue(methodsMap.containsKey("simpleNotEmptyMethod"))
    }

    fun testFileContainsMethod() {
        val methodA = getMethodFromJavaFile("SimpleClass.java", "simpleEmptyMethod")
        assertNotNull(methodA)
        val methodB = getMethodFromJavaFile("SimpleClass.java", "simpleNotEmptyMethod")
        assertNotNull(methodB)
    }

    fun testFileDoesNotContainMethod() {
        val methodA = getMethodFromJavaFile("SimpleClass.java", "unknownmethod")
        assertNull(methodA)
    }
}