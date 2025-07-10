package com.maven.plugin.deadlock.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiClass
import com.maven.plugin.deadlock.core.ElementVisitor
import java.io.File

class FindDeadlocksFromClass: PluginAction() {

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        runFindDeadlocks(anActionEvent)
    }


    fun runFindDeadlocks(anActionEvent: AnActionEvent) {
        val psiElement = anActionEvent.getData(CommonDataKeys.PSI_ELEMENT)

        if (psiElement is PsiClass) {
            println("runFindDeadlocks, start from $psiElement, which is a class")
            val deadlockedVisitors = mutableListOf<ElementVisitor>()
            var outputDir: String? = null
            psiElement.methods.forEach {
                println("runFindDeadlocks, start from ${it.name}, which is a method")
                val visitor = ElementVisitor(arrayListOf(), it)
                it.accept(visitor)
                if (outputDir == null) {
                    outputDir = visitor.dropResult()
                } else {
                    visitor.dropResult(outputDir)
                }

                if (visitor.isDeadlockable) {
                    deadlockedVisitors.add(visitor)
                }
            }
            println("Combining results for ${psiElement.methods.size} visitors")
            if (outputDir != null) {
                val outputDir = File(outputDir)
                val outputFile = File("${outputDir}/combined_results.txt")
                // Create or overwrite the output file
                outputFile.printWriter().use { writer ->
                    outputDir.listFiles()?.forEach { file ->
                        if (file.isFile && !file.name.contains("combined_results")) {
                            println("Writing file ${file.name}")
                            file.forEachLine { line ->
                                writer.println(line)
                            }
                            println("Wrote file ${file.name}")
                        }
                    }
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