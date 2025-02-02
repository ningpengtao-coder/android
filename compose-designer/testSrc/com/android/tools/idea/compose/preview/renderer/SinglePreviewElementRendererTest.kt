/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.compose.preview.renderer

import com.android.tools.adtui.imagediff.ImageDiffUtil
import com.android.tools.idea.compose.ComposeGradleProjectRule
import com.android.tools.idea.compose.preview.SIMPLE_COMPOSE_PROJECT_PATH
import com.android.tools.idea.compose.preview.util.SinglePreviewElementInstance
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class SinglePreviewElementRendererTest {
  @get:Rule
  val projectRule = ComposeGradleProjectRule(SIMPLE_COMPOSE_PROJECT_PATH)

  /**
   * Checks that trying to render an non-existent preview returns a null image
   */
  @Test
  fun testInvalidPreview() {
    assertNull(renderPreviewElement(projectRule.androidFacet(":app"),
                                    SinglePreviewElementInstance.forTesting("google.simpleapplication.MainActivityKt.InvalidPreview")).get())
  }

  /**
   * Checks the rendering of the default `@Preview` in the Compose template.
   */
  @Test
  fun testDefaultPreviewRendering() {
    val defaultRender = renderPreviewElement(projectRule.androidFacet(":app"),
                                             SinglePreviewElementInstance.forTesting("google.simpleapplication.MainActivityKt.DefaultPreview")).get()
    ImageDiffUtil.assertImageSimilar(File("${projectRule.fixture.testDataPath}/${SIMPLE_COMPOSE_PROJECT_PATH}/defaultRender.png"),
                                     defaultRender!!,
                                     0.0)
  }

  /**
   * Checks the rendering of the default `@Preview` in the Compose template with a background
   */
  @Test
  fun testDefaultPreviewRenderingWithBackground() {
    val defaultRenderWithBackground = renderPreviewElement(projectRule.androidFacet(":app"),
                                                           SinglePreviewElementInstance.forTesting(
                                                             "google.simpleapplication.MainActivityKt.DefaultPreview",
                                                             showBackground = true,
                                                             backgroundColor = "#F00")).get()
    ImageDiffUtil.assertImageSimilar(
      File("${projectRule.fixture.testDataPath}/${SIMPLE_COMPOSE_PROJECT_PATH}/defaultRender-withBackground.png"),
      defaultRenderWithBackground!!,
      0.0)
  }

  /**
   * Checks the rendering that rendering an empty preview does not throw an exception.
   * Regression test for b/144722608.
   */
  @Test
  fun testEmptyRender() {
    val defaultRender = renderPreviewElement(projectRule.androidFacet(":app"),
                                             SinglePreviewElementInstance.forTesting("google.simpleapplication.OtherPreviewsKt.EmptyPreview")).get()

    assertTrue(defaultRender!!.width > 0 && defaultRender.height > 0)
  }
}