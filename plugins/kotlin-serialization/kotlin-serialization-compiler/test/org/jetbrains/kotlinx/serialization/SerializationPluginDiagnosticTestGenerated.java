/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class SerializationPluginDiagnosticTestGenerated extends AbstractSerializationPluginDiagnosticTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInDiagnostics() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("DuplicateSerialName.kt")
    public void testDuplicateSerialName() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/DuplicateSerialName.kt");
    }

    @TestMetadata("IncorrectTransient.kt")
    public void testIncorrectTransient() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/IncorrectTransient.kt");
    }

    @TestMetadata("IncorrectTransient2.kt")
    public void testIncorrectTransient2() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/IncorrectTransient2.kt");
    }

    @TestMetadata("InheritableInfo.kt")
    public void testInheritableInfo() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/InheritableInfo.kt");
    }

    @TestMetadata("LazyRecursionBug.kt")
    public void testLazyRecursionBug() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/LazyRecursionBug.kt");
    }

    @TestMetadata("LocalAndAnonymous.kt")
    public void testLocalAndAnonymous() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/LocalAndAnonymous.kt");
    }

    @TestMetadata("MetaSerializable.kt")
    public void testMetaSerializable() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/MetaSerializable.kt");
    }

    @TestMetadata("NoSuitableCtorInParent.kt")
    public void testNoSuitableCtorInParent() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/NoSuitableCtorInParent.kt");
    }

    @TestMetadata("NonSerializable.kt")
    public void testNonSerializable() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/NonSerializable.kt");
    }

    @TestMetadata("NullabilityIncompatible.kt")
    public void testNullabilityIncompatible() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/NullabilityIncompatible.kt");
    }

    @TestMetadata("ParamIsNotProperty.kt")
    public void testParamIsNotProperty() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/ParamIsNotProperty.kt");
    }

    @TestMetadata("SerializableEnums.kt")
    public void testSerializableEnums() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/SerializableEnums.kt");
    }

    @TestMetadata("SerializableIgnored.kt")
    public void testSerializableIgnored() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/SerializableIgnored.kt");
    }

    @TestMetadata("SerializerTypeCompatibleForSpecials.kt")
    public void testSerializerTypeCompatibleForSpecials() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/SerializerTypeCompatibleForSpecials.kt");
    }

    @TestMetadata("SerializerTypeIncompatible.kt")
    public void testSerializerTypeIncompatible() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/SerializerTypeIncompatible.kt");
    }

    @TestMetadata("Transients.kt")
    public void testTransients() throws Exception {
        runTest("plugins/kotlin-serialization/kotlin-serialization-compiler/testData/diagnostics/Transients.kt");
    }
}
