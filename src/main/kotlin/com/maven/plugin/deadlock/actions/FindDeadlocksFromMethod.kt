package com.maven.plugin.deadlock.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiMethod
import com.maven.plugin.deadlock.core.ElementVisitor

class FindDeadlocksFromMethod: PluginAction() {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        runFindDeadlocks(anActionEvent)
    }


    fun runFindDeadlocks(anActionEvent: AnActionEvent) {
        val psiElement = anActionEvent.getData(CommonDataKeys.PSI_ELEMENT)

        if (psiElement is PsiMethod) {
            println("runFindDeadlocks, start from $psiElement, which is a method")
            val visitor = ElementVisitor(arrayListOf(), psiElement, arrayListOf(), arrayListOf())
            psiElement.accept(visitor)
            visitor.dropResult()
        }
    }

    override fun update(event: AnActionEvent) {
        enableForMethod(event)
    }
}

