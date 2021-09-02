/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.testNew

import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.js.testNew.utils.extractTestPackage
import org.jetbrains.kotlin.js.testNew.utils.getMainModuleName
import org.jetbrains.kotlin.js.testNew.utils.getOnlyJsFilesForRunner
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.handlers.JsBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_BACKEND
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.defaultsProvider
import org.jetbrains.kotlin.test.services.moduleStructure
import java.io.File

// Only generate "node.js" file, execution is handled by 'runMocha' task
class NodeJsGeneratorHandler(testServices: TestServices) : JsBinaryArtifactHandler(testServices) {
    val modulesToArtifact = mutableMapOf<TestModule, BinaryArtifacts.Js>()

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (someAssertionWasFailed) return
        val allJsFiles = getOnlyJsFilesForRunner(testServices, modulesToArtifact)

        val globalDirectives = testServices.moduleStructure.allDirectives

        val dontRunGeneratedCode = globalDirectives[JsEnvironmentConfigurationDirectives.DONT_RUN_GENERATED_CODE]
            .contains(testServices.defaultsProvider.defaultTargetBackend?.name)
        val generateNodeJsRunner = JsEnvironmentConfigurationDirectives.GENERATE_NODE_JS_RUNNER in globalDirectives
        val skipNodeJs = JsEnvironmentConfigurationDirectives.SKIP_NODE_JS in globalDirectives

        if (dontRunGeneratedCode || !generateNodeJsRunner || skipNodeJs) return

        val mainModuleName = getMainModuleName(testServices)
        val outputDir = File(JsEnvironmentConfigurator.getJsArtifactsOutputDir(testServices).absolutePath)
        val ignored = globalDirectives[IGNORE_BACKEND].contains(TargetBackend.JS)
        val testPackage = extractTestPackage(testServices)
        val nodeRunnerText = generateNodeRunner(allJsFiles, outputDir, mainModuleName, ignored, testPackage)

        val nodeRunnerName = JsEnvironmentConfigurator.getJsModuleArtifactPath(testServices, mainModuleName) + ".node.js"
        FileUtil.writeToFile(File(nodeRunnerName), nodeRunnerText)
    }

    override fun processModule(module: TestModule, info: BinaryArtifacts.Js) {
        modulesToArtifact[module] = info
    }

    companion object {
        fun generateNodeRunner(files: Collection<String>, dir: File, moduleName: String, ignored: Boolean, testPackage: String?): String {
            val filesToLoad = files.map { FileUtil.getRelativePath(dir, File(it))!!.replace(File.separatorChar, '/') }.map { "\"$it\"" }
            val fqn = testPackage?.let { ".$it" } ?: ""
            val loadAndRun = "load([${filesToLoad.joinToString(",")}], '$moduleName')$fqn.box()"

            val sb = StringBuilder()
            sb.append("module.exports = function(load) {\n")
            if (ignored) {
                sb.append("  try {\n")
                sb.append("    var result = $loadAndRun;\n")
                sb.append("    if (result != 'OK') return 'OK';")
                sb.append("    return 'fail: expected test failure';\n")
                sb.append("  }\n")
                sb.append("  catch (e) {\n")
                sb.append("    return 'OK';\n")
                sb.append("}\n")
            } else {
                sb.append("  return $loadAndRun;\n")
            }
            sb.append("};\n")

            return sb.toString()
        }
    }
}