package com.maven.plugin.deadlock.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class FindDeadlocksFromClass: PluginAction() {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        runFindDeadlocks(anActionEvent)
    }


    fun runFindDeadlocks(anActionEvent: AnActionEvent) {
        System.out.println("TODO")
    }

    override fun update(event: AnActionEvent) {
        enableForClass(event)
    }
}