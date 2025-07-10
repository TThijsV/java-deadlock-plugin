package com.maven.plugin.deadlock.core

import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.jetbrains.rd.util.AtomicInteger
import java.io.File
import java.lang.System.currentTimeMillis

class ElementVisitor(val lineage: ArrayList<ElementVisitor>, val currentElement: PsiElement) : PsiElementVisitor(), PsiRecursiveVisitor {

    val children = ArrayList<ElementVisitor>()

    var isSynchronizedScope: Boolean = false

    var isWithinSynchronizedScope: Boolean = false

    var isReoccurring = false

    var isDeadlockable: Boolean = false

    var isFromSource: Boolean = true

    var isConstructur: Boolean = false

    override fun visitElement(element: PsiElement) {
        if (element is PsiMethod) {
            isFromSource = element.getNavigationElement().getContainingFile().getVirtualFile().isInLocalFileSystem()

            localPrintln("${element.name} is Method, current lineage: ${lineage.size} | isFromSource $isFromSource")
            if (element.text != null) {
                localPrintln(element.text)
            } else {
                localPrintln("Somehow empty text for method element ${element.name}")
            }
            isSynchronizedScope = resolveIsSynchronized(element)
            isWithinSynchronizedScope = isSynchronizedScope || isWithinSyncedScope()

            if (lineageContainsMethod(element)) {
                localPrintln("${element.name} is already found in the lineage with size ${lineage.size}, stop visiting")
                lineage.add(this)
                if (isWithinSynchronizedScope) {
                    localPrintln("!!${element.name} CAN CAUSE A DEADLOCK!!")
                    lineage.forEach { it.isDeadlockable = true }
                }
                isReoccurring = true
                return
            }

            lineage.add(this)
        } else if (element is PsiSynchronizedStatement) {
            localPrintln("${element} is PsiSynchronizedStatement")
            val synchronizedCodeBlock: PsiCodeBlock = element.children.first { it is PsiCodeBlock } as PsiCodeBlock
            val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, synchronizedCodeBlock)
            newElementVisitor.lineage.add(newElementVisitor)
            newElementVisitor.isSynchronizedScope = true
            newElementVisitor.isWithinSynchronizedScope = true
            children.add(newElementVisitor)
            synchronizedCodeBlock.accept(newElementVisitor)
            return
        } else if (element is PsiMethodCallExpression) {
            localPrintln("${element} is PsiMethodCallExpression, ${lineage.last().getName()}")
            resolveMethod(element)
//            return
        } else if (element is PsiNewExpression) {
            localPrintln("${element} is PsiNewExpression")
            resolveNewExpression(element)
//            return
        }
        element.children.forEach { it.accept(this) }
    }

    private fun isWithinSyncedScope(): Boolean {
        return !lineage.isEmpty() && lineage.last().isWithinSynchronizedScope
    }

    /**
     * Checks whether a method is already found in its lineage
     */
    private fun lineageContainsMethod(method: PsiMethod) : Boolean {
        for (visitor in lineage) {
            if (visitor.currentElement == method) {
                visitor.isWithinSynchronizedScope = visitor.isWithinSynchronizedScope || isWithinSynchronizedScope
                return true
            }
        }
        return false
    }

    private fun resolveNewExpression(newExpression: PsiNewExpression) {
        try {
            val constructor = newExpression.resolveConstructor()
            if (constructor != null) {
                val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, constructor)
                newElementVisitor.isConstructur = true
                children.add(newElementVisitor)
                constructor.accept(newElementVisitor)
            } else {
                val javaCodeReferenceElement: PsiJavaCodeReferenceElement =
                    newExpression.children.first { it is PsiJavaCodeReferenceElement } as PsiJavaCodeReferenceElement
                var resolvedElement = javaCodeReferenceElement.resolve()
                if (resolvedElement is PsiClass) {
                    resolvedElement.constructors.forEach {
                        val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, it)
                        newElementVisitor.isConstructur = true
                        children.add(newElementVisitor)
                        it.accept(newElementVisitor)
                    }
                }
            }
        } catch (noSuchElementException: NoSuchElementException) {
            localPrintln("Could not find element from $newExpression - $noSuchElementException")
        }
    }

    private fun resolveMethod(methodCallExpression: PsiMethodCallExpression) {
        // Get the method reference (i.e., the called method)
        val methodReference: PsiReferenceExpression = methodCallExpression.methodExpression
        // Resolve the method being called
        val resolvedMethod = methodReference.resolve()

        if (resolvedMethod is PsiMethod) {
            // Resolve all possible methods from an interface including overloads if containing class is an interface
//            val methods = resolveInterface(resolvedMethod)
            val methods = mutableListOf<PsiMethod>()
            // Add the resolved method if we could not resolve interface methods
            if (methods.isEmpty()) {
                methods.add(resolvedMethod)
            }
            for (method in methods) {
                val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, method)
                children.add(newElementVisitor)
                method.accept(newElementVisitor)
            }
        }
    }

    private fun resolveInterface(psiMethod: PsiMethod): MutableList<PsiMethod> {
        if (psiMethod.containingClass == null || !psiMethod.containingClass!!.isInterface) {
            return mutableListOf()
        }
        localPrintln("resolveInterface for $psiMethod, ${psiMethod.containingClass!!.name}")
        val currentInterface = psiMethod.containingClass!!
        val project = psiMethod.project
        val searchScope = GlobalSearchScope.projectScope(project)  // Search within the whole project
//        val searchScope = GlobalSearchScope.allScope(project)  // Search within the whole project
        val inheritors = ClassInheritorsSearch.search(currentInterface, searchScope, true).findAll()
        val result: MutableList<PsiMethod> = mutableListOf<PsiMethod>()
        inheritors.forEach { inheritor ->
            inheritor.methods.filter { m -> m.name.equals(psiMethod.name) }
                .forEach { m -> result.add(m)}
        }
        return result
    }

    private fun resolveIsSynchronized(method: PsiMethod): Boolean {
        if (method.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
            return true
        }
        if (methodIsSynchronized(method)) {
            return true
        }
        return false;
    }

    private fun localPrintln() {
        println()
    }
    private fun localPrintln(text: String) {
        println("${lineage.size}: $text")
    }


    fun dropResult() : String {
        val currentMethod = currentElement as PsiMethod

        val dir = File("output/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val currentTime = currentTimeMillis()
        val outputDir = "output/${currentMethod.containingClass!!.name}_$currentTime/"
        val dir2 = File(outputDir)
        if (!dir2.exists()) {
            dir2.mkdirs()
        }
        dropResult(outputDir, false)

        if (isDeadlockable) {
            dropResult(outputDir, true)
        }

        return outputDir
    }

    fun dropResult(outputDir: String) {
        dropResult(outputDir, false)
        if (isDeadlockable) {
            dropResult(outputDir, true)
        }
    }

    fun dropResult(outputDir: String, writeOnlyDeadlocklines: Boolean) : String {
        val currentMethod = currentElement as PsiMethod
        localPrintln()

        val file = File("$outputDir${currentMethod.containingClass!!.name}_${currentMethod.name}_${currentTimeMillis()}${if (writeOnlyDeadlocklines) "_DEADLOCK" else "" }.txt")
        writeToFile(file, "Results starting from method $currentElement, contains found deadlock risks = $isDeadlockable")
        println("Writing result to ${file.absolutePath}")
        appendToFile(file, "")
        val header = "|COUNT|DL|SYNC|WS|EXT|LOOP|DEPTH|${"   SCOPE ".padEnd(200, ' ')}|"
        appendToFile(file, "".padEnd(header.length, '-'))
        appendToFile(file, header)
        appendToFile(file, "|${"".padEnd(header.length-2, '-')}|")
        writeResult(file, AtomicInteger(), writeOnlyDeadlocklines)
        appendToFile(file, "".padEnd(header.length, '-'))
        return outputDir
    }

    fun writeToFile(file: File, content: String) {
        println(content)
        file.writeText(content + '\n')
    }

    fun appendToFile(file: File, content: String) {
        println(content)
        file.appendText(content + '\n')
    }

    fun writeResult(file: File, resultCounter: AtomicInteger, writeOnlyDeadlocklines: Boolean) {
        val resultCounterColumn =  getColumn(resultCounter.getAndAdd(1).toString(), 5)
        if (!writeOnlyDeadlocklines || isDeadlockable) {
            val deadlockColumn = getColumn(isDeadlockable, "DL", 2)
            val synchronizedColumn = getColumn(isSynchronizedScope, "SYNC", 4)
            val withinSynchronizedColumn = getColumn(isWithinSynchronizedScope, "WS", 2)
            val externalColumn = getColumn(!isFromSource, "EXT", 3)
            val reoccuringColumn = getColumn(isReoccurring, "LOOP", 4)
            val depthColumn = getColumn(lineage.size.toString(), 5)
            val scopeColumn = (" " + pad() + getName()).padEnd(200, ' ')
            val resultLine =
                "|" + resultCounterColumn + "|" + deadlockColumn + "|" + synchronizedColumn + "|" + withinSynchronizedColumn + "|" + externalColumn + "|" + reoccuringColumn + "|" + depthColumn + "|" + scopeColumn + "|"
            appendToFile(file, resultLine)
        }
        children.forEach { it.writeResult(file, resultCounter, writeOnlyDeadlocklines) }
    }

    private fun getColumn(option: Boolean, output: String, width: Int) : String {
        return if (option) getColumn(output, width) else getColumn("", width)
    }

    private fun getColumn(text: String, width: Int) : String {
        return text.padStart(width, ' ')
    }

    fun pad(): String {
        return "".padEnd((lineage.size-1)*2, ' ')
    }

    fun getName(): String {
        if (currentElement is PsiMethod) {
            if (isConstructur) {
                return "new ${currentElement.name}()"
            } else {
                return "${currentElement.containingClass?.name}#${currentElement.name}"
            }
        } else if (currentElement is PsiCodeBlock) {
            return "SYNC_BLOCK#" + this.lineage.last { it.currentElement is PsiMethod }.getName()
        }
        return "UNKNOWN"
    }

    companion object {

        private val SYNCHRONIZED_ANNOTATION = "Synchronized"
        private val SYNCHRONIZED_ANNOTATION_FULL = "nl.donna.generiek.client.appmodel.concurrent.Synchronized"
        private val NOT_SYNCHRONIZED_ANNOTATION = "NotSynchronized"
        private val NOT_SYNCHRONIZED_ANNOTATION_FULL = "nl.donna.generiek.client.appmodel.concurrent.NotSynchronized"

        fun classIsSynchronized(psiClass: PsiClass?): Boolean {
            return psiClass != null && !psiClass.annotations.none { annotation ->
                annotation.hasQualifiedName(SYNCHRONIZED_ANNOTATION) || annotation.hasQualifiedName(
                    SYNCHRONIZED_ANNOTATION_FULL
                )
            }
        }

        fun methodIsSynchronized(psiMethod: PsiMethod): Boolean {
            // Only consider methods in a synchronized annotated class
            if (classIsSynchronized(psiMethod.containingClass)) {
                // Exclude static and private methods
                if (psiMethod.hasModifierProperty(PsiModifier.STATIC) || psiMethod.hasModifierProperty(PsiModifier.PRIVATE)) {
                    return false
                }
                // Exclude methods with @NotSynchronized annotation
                return psiMethod.annotations.none { annotation ->
                    annotation.hasQualifiedName(NOT_SYNCHRONIZED_ANNOTATION) || annotation.hasQualifiedName(
                        NOT_SYNCHRONIZED_ANNOTATION_FULL
                    )
                }
            }
            return false
        }
    }
}