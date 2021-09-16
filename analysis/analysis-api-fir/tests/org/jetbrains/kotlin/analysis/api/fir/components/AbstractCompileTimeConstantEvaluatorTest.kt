/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.analysis.api.fir.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.analysis.api.fir.test.framework.AbstractHLApiSingleFileTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.base.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.api.analyse
import org.jetbrains.kotlin.analysis.api.symbols.markers.*
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractCompileTimeConstantEvaluatorTest : AbstractHLApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        val element = testServices.expressionMarkerProvider.getSelectedElement(ktFile)
        val expression = when (element) {
            is KtExpression -> element
            is KtValueArgument -> element.getArgumentExpression()
            else -> null
        } ?: testServices.assertions.fail { "Unsupported expression: $element" }
        val constantValue = executeOnPooledThreadInReadAction {
            analyse(expression) { expression.evaluate() }
        }
        val actual = buildString {
            appendLine("expression: ${expression.text}")
            appendLine("constant_value: ${analyse(ktFile) { constantValue?.stringRepresentation() }}")
            appendLine("constant: ${(constantValue as? KtSimpleConstantValue<*>)?.toConst()}")
        }
        testServices.assertions.assertEqualsToFile(testDataFileSibling(".txt"), actual)
    }
}

internal fun KtConstantValue.stringRepresentation(): String {
    return when (this) {
        is KtArrayConstantValue -> buildString {
            appendLine("KtArrayConstantValue [")
            values.joinTo(this, separator = "\n", postfix = "\n") { it.stringRepresentation() }
            append("]")
        }
        is KtAnnotationConstantValue -> buildString {
            append("KtAnnotationConstantValue(")
            append(fqName)
            append(", ")
            arguments.joinTo(this, separator = ", ", prefix = "(", postfix = ")") {
                "${it.name} = ${it.expression.stringRepresentation()}"
            }
            append(")")
        }
        is KtEnumEntryValue -> buildString {
            append("KtEnumEntryValue(")
            append(enumEntrySymbol.callableIdIfNonLocal ?: enumEntrySymbol.name)
            append(")")
        }
        is KtSimpleConstantValue<*> -> toString()
        is KtUnsupportedConstantValue -> "KtUnsupportedConstantValue"
    }
}
