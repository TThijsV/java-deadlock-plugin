package com.maven.plugin.deadlock

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.maven.plugin.deadlock.core.ElementVisitor

@TestDataPath("\$CONTENT_ROOT/src/test/kotlin")
abstract class BasePluginTestCase : BasePlatformTestCase() {

    protected fun runVisitorMethod(fileName: String, methodName: String): ElementVisitor {
        val method: PsiMethod = getMethodFromJavaFile(fileName, methodName)!!
        val elementVisitor = ElementVisitor(arrayListOf(), method, arrayListOf())
        method.accept(elementVisitor)
        return elementVisitor
    }

    private fun getFile(project: Project, myFixture: CodeInsightTestFixture?, fileName: String): PsiFile? {
        var virtualFile: VirtualFile? = null
        var psiFile: PsiFile? = null
        try {
            // Write to the virtual file in a write-safe action
            ApplicationManager.getApplication().runWriteAction {
                virtualFile = myFixture?.copyFileToProject(fileName, "Test-$fileName")
            }
            psiFile = PsiManager.getInstance(project).findFile(virtualFile!!) as PsiFile
        } catch (_: AssertionError) {
            // do nothing
        }
        return psiFile
    }

    fun getFile(fileName: String): PsiFile? {
        return getFile(project, myFixture, fileName)
    }

    fun getMethodsFromJavaFile(project: Project, myFixture: CodeInsightTestFixture?, fileName: String): Map<String, PsiMethod> {
        val file = getFile(project, myFixture, fileName)
        val result = HashMap<String, PsiMethod>()
        val javaFile = file as PsiJavaFile
        for (psiClass in javaFile.classes) {
            val methods = psiClass.methods
            methods.forEach { method -> result.put(method.name, method) }
        }
        return result
    }

    fun getMethodsFromJavaFile(fileName: String): Map<String, PsiMethod> {
        return getMethodsFromJavaFile(project, myFixture, fileName)
    }

    fun getMethodFromJavaFile(project: Project, myFixture: CodeInsightTestFixture?, fileName: String, methodName: String): PsiMethod? {
        return getMethodsFromJavaFile(project, myFixture, fileName).get(methodName)
    }

    fun getMethodFromJavaFile(fileName: String, methodName: String): PsiMethod? {
        return getMethodFromJavaFile(project, myFixture, fileName, methodName)
    }

    protected fun validateSynchronizedMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, lock: String) {
        validateSynchronizedMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, 0, lock)
    }

    protected fun validateSynchronizedMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, deadlockRisk: Int, lock: String) {
        validateMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, false, true, true, deadlockRisk, lock)
    }

    protected fun validateNonSynchronizedMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int) {
        validateMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, false, false, false, 0)
    }

    protected fun validateNonSynchronizedMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, deadlockRisk: Int) {
        validateMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, false, false, false, deadlockRisk)
    }

    protected fun validateMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, isReoccurring: Boolean, isSynchronized: Boolean, isWithinSynchronizedScope: Boolean) {
        validateMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, isReoccurring, isSynchronized, isWithinSynchronizedScope, 0)
    }

    protected fun validateMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, isReoccurring: Boolean, isSynchronized: Boolean, isWithinSynchronizedScope: Boolean, deadlockRisk: Int) {
        validateMethodVisitor(methodVisitor, expectedMethodName, expectedLineageSize, expectedChildrenSize, isReoccurring, isSynchronized, isWithinSynchronizedScope, deadlockRisk, null)
    }

    protected fun validateMethodVisitor(methodVisitor: ElementVisitor, expectedMethodName: String, expectedLineageSize: Int, expectedChildrenSize: Int, isReoccurring: Boolean, isSynchronized: Boolean, isWithinSynchronizedScope: Boolean, deadlockRisk: Int, lock: String?) {
        assertTrue(methodVisitor.currentElement is PsiMethod)
        assertEquals((methodVisitor.currentElement as PsiMethod).name, expectedMethodName)
        assertSize(expectedLineageSize, methodVisitor.lineage)
        assertSize(expectedChildrenSize, methodVisitor.children)
        assertEquals(isReoccurring, methodVisitor.isReoccurring)
        assertEquals(isSynchronized, methodVisitor.isSynchronized())
        assertEquals(isWithinSynchronizedScope, methodVisitor.isWithinSynchronizedScope)
        assertEquals(deadlockRisk, methodVisitor.deadlockRisk)
        if (lock == null) {
            assertNull(methodVisitor.lock)
        } else {
            assertEquals(lock, methodVisitor.lock.toString())
        }

    }

    protected fun validateSynchronizedScopeVisitor(visitor: ElementVisitor, expectedLineageSize: Int, expectedChildrenSize: Int, lock: String) {
        validateSynchronizedScopeVisitor(visitor, expectedLineageSize, expectedChildrenSize, 0, lock)
    }

    protected fun validateSynchronizedScopeVisitor(visitor: ElementVisitor, expectedLineageSize: Int, expectedChildrenSize: Int, deadlockRisk: Int, lock: String) {
        assertTrue(visitor.currentElement is PsiCodeBlock)
        assertSize(expectedLineageSize, visitor.lineage)
        assertSize(expectedChildrenSize, visitor.children)
        assertTrue(visitor.isSynchronized())
        assertTrue(visitor.isWithinSynchronizedScope)
        assertEquals(deadlockRisk, visitor.deadlockRisk)
        assertEquals(lock, visitor.lock.toString())
    }


    override fun getTestDataPath() = "src/test/kotlin/com/maven/plugin/deadlock/testclasses"
}
