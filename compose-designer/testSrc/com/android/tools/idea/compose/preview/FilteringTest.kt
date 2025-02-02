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
package com.android.tools.idea.compose.preview

import com.android.tools.idea.compose.preview.util.PreviewElement
import com.android.tools.idea.compose.preview.util.PreviewElementInstance
import com.android.tools.idea.compose.preview.util.PreviewElementTemplate
import com.android.tools.idea.compose.preview.util.PreviewParameter
import com.android.tools.idea.compose.preview.util.SinglePreviewElementInstance
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class FilteringTest {
  @Test
  fun testGroupFiltering() {
    val groupPreviewProvider = GroupNameFilteredPreviewProvider(StaticPreviewProvider(listOf(
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod1", "PreviewMethod1", "GroupA"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod2", "PreviewMethod2", "GroupA"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod3", "PreviewMethod3", "GroupB"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod4", "PreviewMethod4")
    )))

    // No filtering at all
    assertEquals(4, groupPreviewProvider.previewElements.count())
    assertThat(groupPreviewProvider.availableGroups, `is`(setOf("GroupA", "GroupB")))

    // An invalid group should return all the items
    groupPreviewProvider.groupName = "InvalidGroup"
    assertThat(groupPreviewProvider.previewElements.map { it.displaySettings.name }.toList(),
               `is`(listOf("PreviewMethod1", "PreviewMethod2", "PreviewMethod3", "PreviewMethod4")))

    groupPreviewProvider.groupName = "GroupA"
    assertThat(groupPreviewProvider.previewElements.map { it.displaySettings.name }.toList(),
               `is`(listOf("PreviewMethod1", "PreviewMethod2")))

    groupPreviewProvider.groupName = "GroupB"
    assertEquals("PreviewMethod3", groupPreviewProvider.previewElements.map { it.displaySettings.name }.single())

    groupPreviewProvider.groupName = null
  }

  @Test
  fun testSingleElementFiltering() {
    val singleElementProvider = SinglePreviewElementInstanceFilteredPreviewProvider(StaticPreviewProvider(listOf(
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod1", "PreviewMethod1", "GroupA"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod2", "PreviewMethod2", "GroupA"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod3", "PreviewMethod3", "GroupB"),
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod4", "PreviewMethod4")
    )))

    // No filtering at all
    assertEquals(4, singleElementProvider.previewElements.count())

    // An invalid group should return all the items
    singleElementProvider.instanceId = "com.notvalid.NotValid"
    assertEquals(4, singleElementProvider.previewElements.count())

    singleElementProvider.instanceId = "com.sample.preview.TestClass.PreviewMethod3"
    assertEquals("PreviewMethod3", singleElementProvider.previewElements.single().displaySettings.name)
    singleElementProvider.instanceId = "com.sample.preview.TestClass.PreviewMethod1"
    assertEquals("PreviewMethod1", singleElementProvider.previewElements.single().displaySettings.name)
  }

  private class TestPreviewElementTemplateInstance(private val basePreviewElement: PreviewElement,
                                                   private val index: Int) : PreviewElementInstance, PreviewElement by basePreviewElement {
    override val instanceId: String = "${basePreviewElement.composableMethodFqn}#$index"
  }

  private class TestPreviewElementTemplate(private val basePreviewElement: PreviewElement,
                                           private val instanceCount: Int) :
    PreviewElementTemplate, PreviewElement by basePreviewElement {
    override fun instances(): Sequence<PreviewElementInstance> = generateSequence(instanceCount, {
      if (it > 1) it - 1 else null
    }).map {
      TestPreviewElementTemplateInstance(basePreviewElement, it)
    }
  }

  @Test
  fun testParametrizedElementFiltering() {
    val template = TestPreviewElementTemplate(
      SinglePreviewElementInstance.forTesting("com.sample.preview.TestClass.PreviewMethod", "PreviewMethod"), 10)

    val instances = template.instances().toList()
    val singleElementProvider = SinglePreviewElementInstanceFilteredPreviewProvider(StaticPreviewProvider(instances))

    // No filtering at all
    assertEquals(10, singleElementProvider.previewElements.count())

    singleElementProvider.instanceId = instances[5].instanceId
    assertEquals(1, singleElementProvider.previewElements.count())
  }
}