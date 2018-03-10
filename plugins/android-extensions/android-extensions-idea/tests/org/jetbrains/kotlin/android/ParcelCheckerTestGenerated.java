/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.android;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ParcelCheckerTestGenerated extends AbstractParcelCheckerTest {
    public void testAllFilesPresentInChecker() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("constructors.kt")
    public void testConstructors() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/constructors.kt");
        doTest(fileName);
    }

    @TestMetadata("customCreator.kt")
    public void testCustomCreator() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/customCreator.kt");
        doTest(fileName);
    }

    @TestMetadata("customParcelers.kt")
    public void testCustomParcelers() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/customParcelers.kt");
        doTest(fileName);
    }

    @TestMetadata("customWriteToParcel.kt")
    public void testCustomWriteToParcel() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/customWriteToParcel.kt");
        doTest(fileName);
    }

    @TestMetadata("delegate.kt")
    public void testDelegate() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/delegate.kt");
        doTest(fileName);
    }

    @TestMetadata("emptyPrimaryConstructor.kt")
    public void testEmptyPrimaryConstructor() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/emptyPrimaryConstructor.kt");
        doTest(fileName);
    }

    @TestMetadata("kt20062.kt")
    public void testKt20062() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/kt20062.kt");
        doTest(fileName);
    }

    @TestMetadata("modality.kt")
    public void testModality() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/modality.kt");
        doTest(fileName);
    }

    @TestMetadata("notMagicParcel.kt")
    public void testNotMagicParcel() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/notMagicParcel.kt");
        doTest(fileName);
    }

    @TestMetadata("properties.kt")
    public void testProperties() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/properties.kt");
        doTest(fileName);
    }

    @TestMetadata("simple.kt")
    public void testSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/simple.kt");
        doTest(fileName);
    }

    @TestMetadata("unsupportedType.kt")
    public void testUnsupportedType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/unsupportedType.kt");
        doTest(fileName);
    }

    @TestMetadata("withoutParcelableSupertype.kt")
    public void testWithoutParcelableSupertype() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/withoutParcelableSupertype.kt");
        doTest(fileName);
    }

    @TestMetadata("wrongAnnotationTarget.kt")
    public void testWrongAnnotationTarget() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/android-extensions/android-extensions-idea/testData/android/parcel/checker/wrongAnnotationTarget.kt");
        doTest(fileName);
    }
}
