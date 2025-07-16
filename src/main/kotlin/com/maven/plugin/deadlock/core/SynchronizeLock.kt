package com.maven.plugin.deadlock.core

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiVariable

class SynchronizeLock {
    enum class LockType {
        MUTEX,
        CLASS_INSTANCE,
        OBJECT_INSTANCE
    }

    val lockType: ArrayList<LockType> = ArrayList()
    var element: PsiElement? = null

    override fun toString(): String {
        var result = ""
        if (lockType.contains(LockType.MUTEX)) {
            result = "MUTEX"
        }
        if (lockType.contains(LockType.CLASS_INSTANCE)) {
            result += "${(element as PsiClass).name!!}.class"
        }
        if (lockType.contains(LockType.OBJECT_INSTANCE)) {
            if (element is PsiReferenceExpression) {
                val resolved = (element as PsiReferenceExpression).resolve()
                val variable = resolved as PsiVariable
                val type = variable.getType().canonicalText
                result += "$type INSTANCE"
            } else {
                result += "${(element as PsiClass).name!!} INSTANCE"
            }
        }
        return result
    }

    fun isEqualTo(other: SynchronizeLock?): Boolean {
        return this.toString() == other.toString()
    }

    companion object {
        fun getLockInstance(lockType: LockType, element: PsiElement?): SynchronizeLock {
            val lock = SynchronizeLock()
            lock.lockType.add(lockType)
            lock.element = element
            return lock
        }
    }
}