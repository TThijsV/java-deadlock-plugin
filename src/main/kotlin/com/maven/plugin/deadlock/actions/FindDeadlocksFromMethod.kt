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
        System.out.println("runFindDeadlocks FindDeadlocksFromMethod on $anActionEvent")

        val psiElement = anActionEvent.getData(CommonDataKeys.PSI_ELEMENT)
        System.out.println("runFindDeadlocks, start from $psiElement, which is a method")

        if (psiElement is PsiMethod) {
//            val visitor = PsiElementVisitorImpl(null, 0, psiElement, false, ScopeType.METHOD, mutableSetOf())
            val visitor = ElementVisitor(arrayListOf(), psiElement)
            psiElement.accept(visitor)
            visitor.dropResult()
        }
    }

    override fun update(event: AnActionEvent) {
        enableForMethod(event)
    }
}

