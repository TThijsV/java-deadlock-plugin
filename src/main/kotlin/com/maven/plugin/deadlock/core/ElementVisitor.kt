package com.maven.plugin.deadlock.core

import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.jetbrains.rd.util.AtomicInteger
import java.io.File
import java.lang.System.currentTimeMillis


class ElementVisitor(val lineage: ArrayList<ElementVisitor>, val currentElement: PsiElement, val lockStack: ArrayList<SynchronizeLock>, val riskDescriptions: ArrayList<String>) : PsiElementVisitor(), PsiRecursiveVisitor {

    val children = ArrayList<ElementVisitor>()

    var isWithinSynchronizedScope: Boolean = false

    var isReoccurring = false

    var deadlockRisk: Int = 0

    var isFromSource: Boolean = true

    var isConstructur: Boolean = false

    var lock: SynchronizeLock? = null



    override fun visitElement(element: PsiElement) {
        if (element is PsiMethod) {
            isFromSource = element.getNavigationElement().getContainingFile().getVirtualFile().isInLocalFileSystem()
            localPrintln("${element.name} is Method, current lineage: ${lineage.size} | isFromSource $isFromSource")
            if (element.text != null) {
                localPrintln(element.text)
            } else {
                localPrintln("Somehow empty text for method element ${element.name}")
            }
            // See if method is synchronized and determine lock
            resolveSynchronizedLock(element)
            // Determine if current method is within a synchronized scope
            isWithinSynchronizedScope = lock != null || isWithinSyncedScope()
            // Determine a loop
            isReoccurring = lineageContainsMethod(element)
            // Add current method to lineage
            lineage.add(this)
            // Determine the risk on deadlock
            determineDeadlockRisk()

            // Stop visiting when a loop has been found
            if (isReoccurring) {
                localPrintln("${element.name} is already found in the lineage with size ${lineage.size}, stop visiting")
                return
            }
        } else if (element is PsiSynchronizedStatement) {
            localPrintln("${element} is PsiSynchronizedStatement")
            val synchronizedCodeBlock: PsiCodeBlock = element.children.first { it is PsiCodeBlock } as PsiCodeBlock
            val newElementVisitor = ElementVisitor(
                lineage.clone() as ArrayList<ElementVisitor>,
                synchronizedCodeBlock,
                lockStack.clone() as ArrayList<SynchronizeLock>,
                riskDescriptions)
            newElementVisitor.lock = resolveSynchronizedStatementLock(element)
            newElementVisitor.lockStack.add(newElementVisitor.lock!!)
            newElementVisitor.lineage.add(newElementVisitor)
            newElementVisitor.isWithinSynchronizedScope = true
            newElementVisitor.determineDeadlockRisk()
            children.add(newElementVisitor)
            synchronizedCodeBlock.accept(newElementVisitor)
            return
        } else if (element is PsiMethodCallExpression) {
            localPrintln("${element} is PsiMethodCallExpression, ${lineage.last().getName()}")
            resolveMethod(element)
        } else if (element is PsiNewExpression) {
            localPrintln("${element} is PsiNewExpression")
            resolveNewExpression(element)
        }
        element.children.forEach { it.accept(this) }
    }

     fun isSynchronized() : Boolean {
        return lock != null
    }

    private fun determineDeadlockRisk() {
        // No risk if scope is not synchronized
        if (!isSynchronized() && (!isWithinSynchronizedScope || !isReoccurring)) {
            return
        }
        val uniqueLocks = lockStack.map { it.toString() }.toSet()
        // No risk if only lock is the mutex
        if (uniqueLocks.size == 1 && uniqueLocks.first() == "MUTEX") {
            return
        }
        // No risk if only lock is the same class lock
        if (uniqueLocks.size == 1 && uniqueLocks.first().endsWith(".class")) {
            return
        }
        var risk = 0
        // LOW risk if multiple locks on same object instance
        if (uniqueLocks.size == 1 && uniqueLocks.first().endsWith(" INSTANCE") && lockStack.size > 1) {
            risk = 1
            riskDescriptions.add("LOW:  Multiple locks on object [${uniqueLocks.first()}] in '${getName()}'")
        }
        // MID risk when multiple different locks are used, and no loop is found
        if (uniqueLocks.size > 1 && !isReoccurring) {
            risk = 2
            riskDescriptions.add("MID:  Multiple locks on objects [${uniqueLocks.joinToString(", ")}] in '${getName()}'")
        }
        // HIGH risk when multiple different locks are used, and a loop is found
        if (uniqueLocks.size > 1 && isReoccurring) {
            risk = 3
            riskDescriptions.add("HIGH: Multiple locks on objects [${uniqueLocks.joinToString(", ")}] in '${getName()}' with a loop")
        }
        // HIGH risk when a deadlockable situation is found: For example MUTEX -> SomeObject INSTANCE -> MUTEX
        val riskDescription = checkDeadlockable()
        if (riskDescription != null) {
            risk = 3
            riskDescriptions.add(riskDescription)
        }
        // Set risk in lineage
        lineage.forEach { it.deadlockRisk = Math.max(risk, it.deadlockRisk) }
    }

    /**
     * Checks for a deadlockable situations. That is identified when multiple locks are alternating each other
     * For example MUTEX -> SomeObject INSTANCE -> MUTEX
     */
    private fun checkDeadlockable() : String? {
        var riskDescription = ""
        val uniqueLocks = lockStack.map { it.toString() }.toSet()
        for (uniqueLock in uniqueLocks) {
            var foundLock = false
            var foundOtherLockAfter = false
            for (lock in lockStack) {
                if (foundLock && foundOtherLockAfter && lock.toString() == uniqueLock) {
                    riskDescription += ", $uniqueLock"
                    return "HIGH: Alternating locks on [$riskDescription] in '${getName()}'"
                }
                if (!foundLock && lock.toString() == uniqueLock) {
                    foundLock = true
                    riskDescription = uniqueLock
                } else if (!foundOtherLockAfter && foundLock && lock.toString() != uniqueLock) {
                    foundOtherLockAfter = true
                    riskDescription += ", $lock"
                }
            }
        }
        return null
    }

    /**
     * Resolved which lock is used in a synchronized scope
     */
    private fun resolveSynchronizedStatementLock(element: PsiSynchronizedStatement) : SynchronizeLock {
        var lockObject = element.children.first { it is PsiJavaToken && it.tokenType.toString() == "LPARENTH" }.nextSibling
        if (lockObject is PsiThisExpression) {
            val classObject = (lineage.last { it.currentElement is PsiMethod }.currentElement as PsiMethod).containingClass!!
            return SynchronizeLock.getLockInstance(SynchronizeLock.LockType.OBJECT_INSTANCE, classObject)
        } else if (lockObject is PsiReferenceExpression) {
            return SynchronizeLock.getLockInstance(SynchronizeLock.LockType.OBJECT_INSTANCE, lockObject)
        }
        return SynchronizeLock.getLockInstance(SynchronizeLock.LockType.OBJECT_INSTANCE, lockObject)
    }

    private fun isWithinSyncedScope(): Boolean {
        return lineage.isNotEmpty() && lineage.last().isWithinSynchronizedScope
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
                // Add resolved constructor as scope
                val newElementVisitor = ElementVisitor(
                    lineage.clone() as ArrayList<ElementVisitor>,
                    constructor,
                    lockStack.clone() as ArrayList<SynchronizeLock>,
                    riskDescriptions)
                newElementVisitor.isConstructur = true
                children.add(newElementVisitor)
                constructor.accept(newElementVisitor)
            } else {
                // Add all found constructors as a possible scope
                val javaCodeReferenceElement: PsiJavaCodeReferenceElement =
                    newExpression.children.first { it is PsiJavaCodeReferenceElement } as PsiJavaCodeReferenceElement
                var resolvedElement = javaCodeReferenceElement.resolve()
                if (resolvedElement is PsiClass) {
                    resolvedElement.constructors.forEach {
                        val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>,
                            it,
                            lockStack.clone() as ArrayList<SynchronizeLock>,
                            riskDescriptions)
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
//            val methods = resolveInterface(resolvedMethod) // TODO
            val methods = mutableListOf<PsiMethod>()
            // Add the resolved method if we could not resolve interface methods
            if (methods.isEmpty()) {
                methods.add(resolvedMethod)
            }
            for (method in methods) {
                val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>,
                    method,
                    lockStack.clone() as ArrayList<SynchronizeLock>,
                    riskDescriptions)
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

    /**
     * Resolved whether a method is synchronized by having the `SYNCHRONIZED` modifier
     * It also sets the name of the used lock for the current visitor
     */
    private fun resolveSynchronizedLock(method: PsiMethod) {
        if (method.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
            if (method.hasModifierProperty(PsiModifier.STATIC)) {
                lock = SynchronizeLock.getLockInstance(SynchronizeLock.LockType.CLASS_INSTANCE, method.containingClass)
            } else {
                lock = SynchronizeLock.getLockInstance(SynchronizeLock.LockType.OBJECT_INSTANCE, method.containingClass)
            }
        }
        if (isMethodSynchronizedByAnnotation(method)) {
            if (lock != null) {
                lock!!.lockTypes.add(SynchronizeLock.LockType.MUTEX)
                // TODO, this should not occur, but need to notify somehow
                localPrintln("Double synchronized for $method!?")
            } else {
                lock = SynchronizeLock.getLockInstance(SynchronizeLock.LockType.MUTEX, method.containingClass)
            }
        }
        if (lock != null) {
            lockStack.add(lock!!)
        }
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

        if (deadlockRisk > 0) {
            dropResult(outputDir, true)
        }

        return outputDir
    }

    fun dropResult(outputDir: String) {
        dropResult(outputDir, false)
        if (deadlockRisk > 0) {
            dropResult(outputDir, true)
        }
    }

    fun dropResult(outputDir: String, writeOnlyDeadlocklines: Boolean) : String {
        val currentMethod = currentElement as PsiMethod
        localPrintln()

        val file = File("$outputDir${currentMethod.containingClass!!.name}_${currentMethod.name}_${currentTimeMillis()}${if (writeOnlyDeadlocklines) "_DEADLOCK" else "" }.txt")
        writeToFile(file, "Results starting from method $currentElement, has a deadlock risk of ${deadLockRiskToLevel(deadlockRisk)}")
        println("Writing result to ${file.absolutePath}")
        appendToFile(file, "")
        val header = "|COUNT|RISK|SYNC|WS|EXT|LOOP|DEPTH|${"LOCK".padEnd(50, ' ')}|${"   SCOPE ".padEnd(100, ' ')}|"
        appendToFile(file, "".padEnd(header.length, '-'))
        appendToFile(file, header)
        appendToFile(file, "|${"".padEnd(header.length-2, '-')}|")
        writeResult(file, AtomicInteger(), writeOnlyDeadlocklines)
        appendToFile(file, "".padEnd(header.length, '-'))

        if (riskDescriptions.isNotEmpty()) {
            appendToFile(file, "")
            appendToFile(file, "Found risks")
            riskDescriptions.forEach { appendToFile(file, it) }
        }
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
        if (!writeOnlyDeadlocklines || deadlockRisk > 0) {
            val synchronizedColumn = getColumn(isSynchronized(), "SYNC", 4)
            val riskColumn = getRiskColumn(deadlockRisk)
            val withinSynchronizedColumn = getColumn(isWithinSynchronizedScope, "WS", 2)
            val externalColumn = getColumn(!isFromSource, "EXT", 3)
            val reoccuringColumn = getColumn(isReoccurring, "LOOP", 4)
            val depthColumn = getColumn(lineage.size.toString(), 5)
            val lockColumn = (if (lock == null) "" else lock!!.toString()).padEnd(50, ' ')
            val scopeColumn = (" " + pad() + getName()).padEnd(100, ' ')
            val resultLine =
                "|$resultCounterColumn|$riskColumn|$synchronizedColumn|$withinSynchronizedColumn|$externalColumn|$reoccuringColumn|$depthColumn|$lockColumn|$scopeColumn|"
            appendToFile(file, resultLine)
        }
        children.forEach { it.writeResult(file, resultCounter, writeOnlyDeadlocklines) }
    }

    fun deadLockRiskToLevel() : String {
        return deadLockRiskToLevel(deadlockRisk)
    }

    private fun deadLockRiskToLevel(risk: Int) : String {
        return when (risk) {
            0 -> ""
            1 -> "LOW"
            2 -> "MID"
            3 -> "HIGH"
            else -> "????"
    }
        }

    private fun getRiskColumn(risk: Int) :String {
        return deadLockRiskToLevel(risk).padEnd(4, ' ')
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

        fun isMethodSynchronizedByAnnotation(psiMethod: PsiMethod): Boolean {
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