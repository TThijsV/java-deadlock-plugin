package com.maven.plugin.deadlock.core

import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch

class ElementVisitor(val lineage: ArrayList<ElementVisitor>, val currentElement: PsiElement) : PsiElementVisitor(), PsiRecursiveVisitor {

    val children = ArrayList<ElementVisitor>()

    var isSynchronizedScope: Boolean = false

    var isWithinSynchronizedScope: Boolean = false

    var isReoccurring = false

    var isDeadlockable: Boolean = false

    var isFromSource: Boolean = true

    override fun visitElement(element: PsiElement) {
        if (element is PsiMethod) {
            isFromSource = element.getNavigationElement().getContainingFile().getVirtualFile().isInLocalFileSystem()

            localPrintln("${element.name} is Method, current lineage: ${lineage.size} | isFromSource $isFromSource")
            localPrintln(element.text)
            isSynchronizedScope = resolveIsSynchronized()
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
            localPrintln("${element.name}, new current lineage: ${lineage.size}")

            element.children.forEach { elm -> {
                localPrintln("Element: $elm")
                localPrintln("${elm.text}")
            } }


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
            localPrintln("${element} is PsiMethodCallExpression")
            resolveMethod(element)
            return
        } else if (element is PsiNewExpression) {
            localPrintln("${element} is PsiNewExpression")
            resolveNewExpression(element)
            return
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
        val javaCodeReferenceElement: PsiJavaCodeReferenceElement = newExpression.children.first { it is PsiJavaCodeReferenceElement } as PsiJavaCodeReferenceElement
        var resolvedElement = javaCodeReferenceElement.resolve()
        if (resolvedElement is PsiClass) {
            resolvedElement.constructors.forEach {
                val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, it)
                children.add(newElementVisitor)
                it.accept(newElementVisitor)
            }
        }
    }

    private fun resolveMethod(methodCallExpression: PsiMethodCallExpression) {
        // Get the method reference (i.e., the called method)
        val methodReference: PsiReferenceExpression = methodCallExpression.methodExpression
        // Resolve the method being called
        val resolvedMethod = methodReference.resolve()

        if (resolvedMethod is PsiMethod) {
            // Resolve all possible methods from an interface including overloads if containing class is an interface
            val methods = resolveInterface(resolvedMethod)
            // Add the resolved method if we could not resolve interface methods
            if (methods.isEmpty()) {
                methods.add(resolvedMethod)
            }
            for (method in methods) {
                val newElementVisitor = ElementVisitor(lineage.clone() as ArrayList<ElementVisitor>, method)
                children.add(newElementVisitor)
//                newElementVisitor.lineage.add(newElementVisitor)
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

    private fun resolveIsSynchronized(): Boolean {
        if (currentElement is PsiMethod && currentElement.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
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

    fun dropResult() {
        localPrintln()
        localPrintln("Getting result starting from method $currentElement, contains deadlock risks = $isDeadlockable")
        writeResult()
    }

    fun writeResult() {
        localPrintln("${pad()}${getName()}, synced = $isSynchronizedScope, within synced scope = $isWithinSynchronizedScope, internal = $isFromSource")
        children.forEach { it.writeResult() }
    }

    fun pad(): String {
        return "".padEnd((lineage.size-1)*2, ' ')
    }

    private fun getName(): String {
        if (currentElement is PsiMethod) {
            return currentElement.name
        } else if (currentElement is PsiCodeBlock) {
            return "SYNCED"
        }
        return "UNKNOWN"
    }
}