package com.maven.plugin.deadlock.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.maven.plugin.deadlock.core.ElementVisitor

class FindDeadlocksFromClass: PluginAction() {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        runFindDeadlocks(anActionEvent)
    }


    fun runFindDeadlocks(anActionEvent: AnActionEvent) {
        val psiElement = anActionEvent.getData(CommonDataKeys.PSI_ELEMENT)

        if (psiElement is PsiClass) {
            println("runFindDeadlocks, start from $psiElement, which is a class")
            val deadlockedVisitors = mutableListOf<ElementVisitor>()
            psiElement.methods.forEach {
                println("runFindDeadlocks, start from ${it.name}, which is a method")
                val visitor = ElementVisitor(arrayListOf(), it)
                it.accept(visitor)
                visitor.dropResult()

                if (visitor.isDeadlockable) {
                    deadlockedVisitors.add(visitor)
                }
            }
            println("${psiElement.methods.size - deadlockedVisitors.size} methods without deadlock risk, ${deadlockedVisitors.size} with a deadlock risk")
            deadlockedVisitors.forEach { println("${it.getName()} contains a deadlock risk")}
        }
    }

    override fun update(event: AnActionEvent) {
        enableForClass(event)
    }
}