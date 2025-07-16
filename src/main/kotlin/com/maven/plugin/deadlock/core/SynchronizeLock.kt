package com.maven.plugin.deadlock.core

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.util.elementType

class SynchronizeLock {

    enum class LockType {
        MUTEX,
        CLASS_INSTANCE,
        OBJECT_INSTANCE,
    }

    val lockTypes: ArrayList<LockType> = ArrayList()

    var element: PsiElement? = null

    private constructor()

    override fun toString() : String {
        // TODO, multiple locks behandelen, zou eigenlijk niet moeten voorkomen
        if (lockTypes.size > 1){
            return "MULTIPLELOCKS"
        }
        if (lockTypes.contains(LockType.MUTEX)) {
            return "MUTEX"
        } else if (lockTypes.contains(LockType.CLASS_INSTANCE) && element != null) {
            return "${(element as PsiClass).name}.class"
        } else if (lockTypes.contains(LockType.OBJECT_INSTANCE) && element != null) {
            if (element is PsiClass) {
                return "${(element as PsiClass).name} INSTANCE"
            } else if (element is PsiVariable) {
                return "${(element as PsiVariable).type.canonicalText} INSTANCE"
            } else if (element is PsiReferenceExpression) {
                return "${(element as PsiReferenceExpression).type?.canonicalText} INSTANCE"
            }
        }

        // TODO, deze moet ook niet voorkomen
        return "UNDEFINED"
    }

    companion object {
        fun getLockInstance(lockType: LockType?, element: PsiElement?): SynchronizeLock {
            val newInstance = SynchronizeLock()
            if (lockType != null) {
                newInstance.lockTypes.add(lockType)
            }
            newInstance.element = element
            return newInstance
        }
    }
}