package dev.thomasharris.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File

@Suppress("UnstableApiUsage")
open class NewModuleTask : DefaultTask() {

    /**
     * module name may have dashes
     */
    @Input
    @set:Option(option = "name", description = "the name of the module to create")
    var moduleName: String = ""

    @Input
    @set:Option(option = "android", description = "Enable the android library pluging")
    var isAndroid: Boolean = false

    @Input
    @set:Option(option = "package", description = "The package structure to initialize with")
    var packageStructure: String = ""

    @TaskAction
    fun run() {
        if (moduleName.isBlank()) throw GradleException("Please provide a name by doing --name=module-name")
        if (packageStructure.isBlank()) throw GradleException("Please provide a package structure by doing --package=dev.thomasharris.lib.MyClass")

        println("Module: $moduleName")
        println("Path: ${project.rootDir.absolutePath}")
        println("Is android? $isAndroid")
        println("package: ${packageStructure.split(".")}")

        // create the starting class
        val ktName =
            "$moduleName/src/main/java/${packageStructure.split(".").joinToString(separator = "/")}.kt"
        with(File(project.rootDir, ktName)) {
            ensureParentDirsCreated()
            createNewFile()
            writeText("package ${packageStructure.split(".").dropLast(1).joinToString(separator = ".")}")
        }

        // create the res folder if hasAndroid
        if (isAndroid)
            File(project.rootDir, "$moduleName/src/main/res").mkdirs()

        // create test directory
        val testDir =
            "$moduleName/src/test/java/${packageStructure.split(".").dropLast(1).joinToString(
                separator = "/"
            )}"
        File(project.rootDir, testDir).mkdirs()

        // create manifest if hasAndroid
        if (isAndroid) with(File(project.rootDir, "$moduleName/src/main/AndroidManifest.xml")) {
            ensureParentDirsCreated()
            createNewFile()
            writeText(
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    package=\"${packageStructure.split(".").dropLast(1).joinToString(
                            separator = "."
                        )}\" />"
            )
        }

        // create build.gradle.kts
        with(File(project.rootDir, "$moduleName/build.gradle.kts")) {
            createNewFile()
            appendText("import dev.thomasharris.build.testing\n\n")
            appendText("plugins {\n    id(\"dev.thomasharris.claw${if (isAndroid) ".android" else ""}\")\n}\n\n")
            appendText("dependencies {\n    testing()\n}")
        }

        File(project.rootDir, "settings.gradle.kts").appendText("\ninclude(\":$moduleName\")")

        // create proguard-rules.pro
        with (File(project.rootDir, "proguard-rules.pro")){
            createNewFile()
            appendText("# -dontobfuscate")
        }
    }
}