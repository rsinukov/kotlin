/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/expressionSelection")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ExpressionSelectionTestGenerated extends AbstractExpressionSelectionTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTestExpressionSelection, this, testDataFilePath);
    }

    public void testAllFilesPresentInExpressionSelection() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/expressionSelection"), Pattern.compile("^([^.]+)\\.kt$"), null, true);
    }

    @TestMetadata("binaryExpr.kt")
    public void testBinaryExpr() throws Exception {
        runTest("idea/testData/expressionSelection/binaryExpr.kt");
    }

    @TestMetadata("labelledStatement.kt")
    public void testLabelledStatement() throws Exception {
        runTest("idea/testData/expressionSelection/labelledStatement.kt");
    }

    @TestMetadata("labelledThis.kt")
    public void testLabelledThis() throws Exception {
        runTest("idea/testData/expressionSelection/labelledThis.kt");
    }

    @TestMetadata("noExpression.kt")
    public void testNoExpression() throws Exception {
        runTest("idea/testData/expressionSelection/noExpression.kt");
    }
}
