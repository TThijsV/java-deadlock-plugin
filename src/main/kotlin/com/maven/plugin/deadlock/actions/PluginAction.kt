package com.maven.plugin.deadlock.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiMethodImpl

abstract class PluginAction: AnAction() {

    private fun <T>enableFor(event: AnActionEvent, elementClass: Class<T>) {
        val presentation = event.presentation
        val dataContext = event.dataContext

        // Use PsiElement to determine context
        val psiElement = dataContext.getData(CommonDataKeys.PSI_ELEMENT)!!
        presentation.isEnabledAndVisible = psiElement.javaClass.equals(elementClass)
    }

    fun enableForMethod(event: AnActionEvent) {
        enableFor(event, PsiMethodImpl::class.java)
    }

    fun enableForClass(event: AnActionEvent) {
        enableFor(event, PsiClassImpl::class.java)
    }
}