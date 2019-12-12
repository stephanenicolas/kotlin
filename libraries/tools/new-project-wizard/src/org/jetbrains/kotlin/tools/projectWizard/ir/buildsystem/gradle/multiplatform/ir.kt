package org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.multiplatform

import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.BuildSystemIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.IrsOwner
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.KotlinIR
import org.jetbrains.kotlin.tools.projectWizard.ir.buildsystem.gradle.GradleIR
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.ModuleSubType
import org.jetbrains.kotlin.tools.projectWizard.plugins.printer.GradlePrinter

interface MultiplatformIR : GradleIR, KotlinIR

interface TargetIR: MultiplatformIR

data class TargetAccessIR(
    val type: ModuleSubType,
    val nonDefaultName: String? // `null` stands for default target name
) : TargetIR {
    override fun GradlePrinter.renderGradle() {
        +type.toString()
        par {
            nonDefaultName?.let { +it.quotified }
        }
    }
}

interface TargetConfigurationIR : MultiplatformIR, IrsOwner

data class DefaultTargetConfigurationIR(
    val targetAccess: TargetAccessIR,
    override val irs: List<BuildSystemIR>
) : TargetConfigurationIR {
    override fun withReplacedIrs(irs: List<BuildSystemIR>): DefaultTargetConfigurationIR =
        copy(irs = irs)

    override fun GradlePrinter.renderGradle() {
        +targetAccess.type.toString()
        if (irs.isEmpty() || targetAccess.nonDefaultName != null) par {
            targetAccess.nonDefaultName?.let { +it.quotified }
        }
        if (irs.isNotEmpty()) {
            +" "; inBrackets { irs.listNl() }
        }
    }
}

data class NonDefaultTargetConfigurationIR(
    val name: String,
    override val irs: List<BuildSystemIR>
) : TargetConfigurationIR {
    override fun withReplacedIrs(irs: List<BuildSystemIR>): NonDefaultTargetConfigurationIR =
        copy(irs = irs)

    override fun GradlePrinter.renderGradle() {
        +name
        when (dsl) {
            GradlePrinter.GradleDsl.KOTLIN -> +".apply"
            GradlePrinter.GradleDsl.GROOVY-> +".with"
        }
        +" "; inBrackets { irs.listNl() }
    }
}

data class CompilationIR(
    val name: String,
    override val irs: List<BuildSystemIR>
) : MultiplatformIR, IrsOwner {
    override fun withReplacedIrs(irs: List<BuildSystemIR>) = copy(irs = irs)
    override fun GradlePrinter.renderGradle() {
        getting(name, "compilations") { irs.listNl() }
    }
}
