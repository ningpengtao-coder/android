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
package com.android.tools.idea.layoutinspector.resource

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_BACKGROUND
import com.android.SdkConstants.ATTR_BACKGROUND_TINT
import com.android.SdkConstants.ATTR_DRAWABLE_LEFT
import com.android.SdkConstants.ATTR_DRAWABLE_RIGHT
import com.android.SdkConstants.ATTR_TEXT
import com.android.SdkConstants.ATTR_TEXT_COLOR
import com.android.ide.common.rendering.api.ResourceNamespace
import com.android.ide.common.rendering.api.ResourceReference
import com.android.ide.common.resources.configuration.FolderConfiguration
import com.android.resources.ResourceType
import com.android.testutils.MockitoKt.mock
import com.android.tools.idea.configurations.ConfigurationManager
import com.android.tools.idea.layoutinspector.model.ViewNode
import com.android.tools.idea.layoutinspector.properties.InspectorPropertyItem
import com.android.tools.idea.layoutinspector.properties.PropertySection
import com.android.tools.idea.layoutinspector.util.DemoExample
import com.android.tools.idea.testing.AndroidProjectRule
import com.android.tools.layoutinspector.proto.LayoutInspectorProto.Property.Type
import com.google.common.truth.Truth.assertThat
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.EdtRule
import com.intellij.testFramework.RunsInEdt
import org.jetbrains.android.facet.AndroidFacet
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@RunsInEdt
class ResourceLookupResolverTest {
  private val projectRule = AndroidProjectRule.withSdk()

  @get:Rule
  val ruleChain = RuleChain.outerRule(projectRule).around(EdtRule())!!

  @Before
  fun setUp() {
    DemoExample.setUpDemo(projectRule.fixture)
  }

  @Suppress("SameParameterValue")
  private fun createResourceLookupResolver(theme: String, vararg qualifiers: String): ResourceLookupResolver {
    // We will always get qualifiers from the device.
    // In this test we are only concerned about orientation and screen width: give suitable default values if they are omitted.
    val qualifierList = mutableListOf(*qualifiers)
    if (!qualifierList.contains("land") && !qualifierList.contains("port")) {
      qualifierList.add("port")
    }
    if (qualifierList.indexOfFirst { it.startsWith('w') } < 0) {
      qualifierList.add("w640")
    }
    val facet = AndroidFacet.getInstance(projectRule.module)!!
    val folderConfiguration = FolderConfiguration.getConfigFromQualifiers(listOf(*qualifiers))!!
    val mgr = ConfigurationManager.getOrCreateInstance(facet)
    val cache = mgr.resolverCache
    val resourceResolver = cache.getResourceResolver(mgr.target, theme, folderConfiguration)

    return ResourceLookupResolver(projectRule.project, facet, folderConfiguration, resourceResolver)
  }

  @Test
  fun testTextColorFromLayout() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.textColor, data.demo, 10)
    checkLocation(locations[0], "demo.xml:18", "framework:textColor=\"@color/textRedIndirect\"")
    checkLocation(locations[1], "colors.xml:6", "<color name=\"textRedIndirect\">@color/textRed</color>")
    checkLocation(locations[2], "colors.xml:5", "<color name=\"textRed\">#FF0000</color>")
    assertThat(locations.size).isEqualTo(3)
    assertThat(resolver.findAttributeValue(data.textColor, data.demo)).isEqualTo("#FF0000")
  }

  @Test
  fun testTextColorFromLayoutWithSpecialConfiguration() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme, "w800dp", "land")
    val locations = resolver.findFileLocations(data.textColor, data.demo, 10)
    checkLocation(locations[0], "demo.xml:14", "android:textColor=\"@color/app_text_color\"")
    checkLocation(locations[1], "app_text_color.xml:6", "<item android:alpha=\"0.5\"\n" +
                                                        "        android:color=\"@color/textBlueIndirect\"/>")
    checkLocation(locations[2], "colors.xml:8", "<color name=\"textBlueIndirect\">@color/textBlue</color>")
    checkLocation(locations[3], "colors.xml:4", "<color name=\"textBlue\">#800000FF</color>")
    assertThat(locations.size).isEqualTo(4)
    assertThat(resolver.findAttributeValue(data.textColor, data.demo)).isEqualTo("#400000FF")
  }

  @Test
  fun testBackgroundTintFromLayoutWithThemeReference() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.backgroundTint, data.demo, 10)
    checkLocation(locations[0], "demo.xml:16", "framework:backgroundTint=\"?android:attr/colorBackgroundFloating\"")
    checkLocation(locations[1], "styles.xml:4", "<item name=\"android:colorBackgroundFloating\">@color/yellowBackground</item>")
    checkLocation(locations[2], "colors.xml:3", "<color name=\"yellowBackground\">#CCCC23</color>")
    assertThat(locations.size).isEqualTo(3)
    assertThat(resolver.findAttributeValue(data.backgroundTint, data.demo)).isEqualTo("#CCCC23")
  }

  @Test
  fun testTextColorFromMyTextStyleExtra() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.textColor, data.myTextStyleExtra, 10)
    checkLocation(locations[0], "styles.xml:12", "<item name=\"android:textColor\">#888800</item>")
    assertThat(locations.size).isEqualTo(1)
    assertThat(resolver.findAttributeValue(data.textColor, data.myTextStyleExtra)).isEqualTo("#888800")
  }

  @Test
  fun testTextColorFromMyTextStyle() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.textColor, data.myTextStyle, 10)
    checkLocation(locations[0], "styles.xml:8", "<item name=\"android:textColor\">@color/textBlueIndirect</item>")
    checkLocation(locations[1], "colors.xml:8", "<color name=\"textBlueIndirect\">@color/textBlue</color>")
    checkLocation(locations[2], "colors.xml:7", "<color name=\"textBlue\">#2122F8</color>")
    assertThat(locations.size).isEqualTo(3)
    assertThat(resolver.findAttributeValue(data.textColor, data.myTextStyle)).isEqualTo("#2122F8")
  }

  @Test
  fun testTextColorFromMyTextStyleWithSpecialConfiguration() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme, "w800dp", "land")
    val locations = resolver.findFileLocations(data.textColor, data.myTextStyle, 10)
    checkLocation(locations[0], "styles.xml:4", "<item name=\"android:textColor\">@color/textRedIndirect</item>")
    checkLocation(locations[1], "colors.xml:6", "<color name=\"textRedIndirect\">@color/textRed</color>")
    checkLocation(locations[2], "colors.xml:3", "<color name=\"textRed\">#FF0000</color>")
    assertThat(locations.size).isEqualTo(3)
    assertThat(resolver.findAttributeValue(data.textColor, data.myTextStyle)).isEqualTo("#FF0000")
  }

  @Test
  fun testColorValueFromTextStyleMaterialBody1() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.textColor, data.textStyleMaterialBody1, 10)
    checkLocation(locations[0], "styles_material.xml:230", "<item name=\"textColor\">?attr/textColorPrimary</item>")
    checkLocation(locations[1], "themes_material.xml:436", "<item name=\"textColorPrimary\">@color/text_color_primary</item>")
    checkLocation(locations[2], "text_color_primary.xml:21", "<item android:alpha=\"?attr/primaryContentAlpha\"\n" +
                                                             "        android:color=\"?attr/colorForeground\"/>")
    checkLocation(locations[3], "themes_material.xml:418", "<item name=\"colorForeground\">@color/foreground_material_light</item>")
    checkLocation(locations[4], "colors_material.xml:20", "<color name=\"foreground_material_light\">@color/black</color>")
    checkLocation(locations[5], "colors.xml:39", "<color name=\"black\">#ff000000</color>")
    assertThat(locations.size).isEqualTo(6)
    assertThat(resolver.findAttributeValue(data.textColor, data.textStyleMaterialBody1)).isEqualTo("#DD000000")
  }

  @Test
  fun testColorValueFromTextStyleMaterialWithLimit() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.textColor, data.textStyleMaterial, 2)
    checkLocation(locations[0], "styles_material.xml:156", "<item name=\"textColor\">?attr/textColorPrimary</item>")
    checkLocation(locations[1], "themes_material.xml:436", "<item name=\"textColorPrimary\">@color/text_color_primary</item>")
    assertThat(locations.size).isEqualTo(2) // 3 lines omitted because a limit of 2 was specified
    assertThat(resolver.findAttributeValue(data.textColor, data.textStyleMaterial)).isEqualTo("#DD000000")
  }

  @Test
  fun testDrawableFromLayoutWithVectorDrawable() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.background, data.demo, 10)
    checkLocation(locations[0], "demo.xml:13", "framework:background=\"@drawable/battery\"")
    assertThat(locations.size).isEqualTo(1)
    assertThat(resolver.findAttributeValue(data.background, data.demo)).isEqualTo("@drawable/battery")
  }

  @Test
  fun testDrawableFromLayoutWithVectorDrawableWithIndirection() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.drawableLeft, data.demo, 10)
    checkLocation(locations[0], "demo.xml:14", "framework:drawableLeft=\"@drawable/background_choice\"")
    checkLocation(locations[1], "background_choice.xml:4", "<item android:drawable=\"@drawable/vd\"/>")
    assertThat(locations.size).isEqualTo(2)
    assertThat(resolver.findAttributeValue(data.drawableLeft, data.demo)).isEqualTo("@drawable/vd")
  }

  @Test
  fun testAndroidDrawableFromLayoutWithTripleIndirection() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.drawableRight, data.demo, 10)
    checkLocation(locations[0], "demo.xml:15", "framework:drawableRight=\"@drawable/my_image\"")
    checkLocation(locations[1], "drawables.xml:3", "<item name=\"my_image\" type=\"drawable\">@drawable/old_image</item>")
    checkLocation(locations[2], "drawables.xml:4", "<item name=\"old_image\" type=\"drawable\">@drawable/dsl1</item>")
    checkLocation(locations[3], "dsl1.xml:4", "<item android:drawable=\"@drawable/dsl2\"/>")
    checkLocation(locations[4], "dsl2.xml:4", "<item android:drawable=\"@drawable/dsl3\"/>")
    checkLocation(locations[5], "dsl3.xml:4", "<item android_framework:drawable=\"@android_framework:drawable/arrow_up_float\"/>")
    assertThat(locations.size).isEqualTo(6)
    assertThat(resolver.findAttributeValue(data.drawableRight, data.demo)).isEqualTo("@framework:drawable/arrow_up_float")
  }

  @Test
  fun testTextFromTextFieldWithoutAnId() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val value1 = resolver.findAttributeValue(data.text1, data.demo)
    val value2 = resolver.findAttributeValue(data.text2, data.demo)
    val value3 = resolver.findAttributeValue(data.text3, data.designText)
    // We cannot determine a view without an ID in general:
    assertThat(value1).isNull()
    // Except if this view is the only child of a parent view with an ID, then we assume we have found it:
    assertThat(value2).isEqualTo("TextView without an ID")
    // or if this file only has 1 view, then we assume that view is what we are looking for:
    assertThat(value3).isEqualTo("Tab1")
  }

  @Test
  fun testButtonWithBackgroundColor() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val background = InspectorPropertyItem(
      ANDROID_URI, ATTR_BACKGROUND, ATTR_BACKGROUND, Type.COLOR, "", PropertySection.DECLARED, data.demo, data.button, data.resourceLookup)
    val locations = resolver.findFileLocations(background, data.demo, 10)
    checkLocation(locations[0], "demo.xml:25", "framework:background=\"@color/textBlue\"")
    checkLocation(locations[1], "colors.xml:7", "<color name=\"textBlue\">#2122F8</color>")
    assertThat(locations.size).isEqualTo(2)
    assertThat(resolver.findAttributeValue(background, data.demo)).isEqualTo("#2122F8")
    assertThat(resolver.findAttributeValue(background, data.myButtonStyle)).isEqualTo("@framework:drawable/btn_default_material")
  }

  @Test
  fun testApproximateFileLocation() {
    val data = Data()
    val resolver = createResourceLookupResolver(data.theme)
    val locations = resolver.findFileLocations(data.text1, data.demo, 10)
    checkLocation(locations[0], "demo.xml:?", "<RelativeLayout\n" +
                                              "    xmlns:framework=\"http://schemas.android.com/apk/res/android\"\n" +
                                              "...")
    assertThat(locations.size).isEqualTo(1)
  }

  private fun checkLocation(location: SourceLocation, source: String, xml: String) {
    assertThat(location.source).isEqualTo(source)
    val actualXml = when (val navigatable = location.navigatable) {
      is XmlAttributeValue -> navigatable.parent.text
      is XmlTag -> navigatable.text.lines().joinToString(separator="\n", limit = 2)
      else -> navigatable.toString()
    }
    assertThat(actualXml).isEqualTo(xml)
  }

  private class Data {
    val theme = "@style/AppTheme"
    val exampleNS = ResourceNamespace.fromPackageName("com.example")
    val demo = ResourceReference(exampleNS, ResourceType.LAYOUT, "demo")
    val designText = ResourceReference(exampleNS, ResourceType.LAYOUT, "design_tab_text")
    val myTextStyle = ResourceReference(exampleNS, ResourceType.STYLE, "MyTextStyle")
    val myTextStyleExtra = ResourceReference(exampleNS, ResourceType.STYLE, "MyTextStyle.Extra")
    val textStyleMaterial = ResourceReference(ResourceNamespace.ANDROID, ResourceType.STYLE, "TextAppearance.Material")
    val textStyleMaterialBody1 = ResourceReference(ResourceNamespace.ANDROID, ResourceType.STYLE, "TextAppearance.Material.Body1")
    val myButtonStyle = ResourceReference(ResourceNamespace.ANDROID, ResourceType.STYLE, "Widget.Material.Button")
    val relativeId = ResourceReference(exampleNS, ResourceType.ID, "relativeLayout")
    val frameId = ResourceReference(exampleNS, ResourceType.ID, "frameLayout")
    val titleId = ResourceReference(exampleNS, ResourceType.ID, "title")
    val buttonId = ResourceReference(exampleNS, ResourceType.ID, "button")
    val resourceLookup: ResourceLookup = mock()
    val relativeLayout = ViewNode(1, "RelativeLayout", demo, 0, 0, 300, 900, relativeId, "", 0)
    val title = ViewNode(2, "TextView", demo, 30, 60, 300, 100, titleId, "Hello Folks", 0)
    val frameLayout = ViewNode(3, "RelativeLayout", demo, 0, 200, 300, 700, frameId, "", 0)
    val textView1 = ViewNode(4, "TextView", demo, 400, 60, 300, 100, null, "TextView without an ID", 0)
    val textView2 = ViewNode(5, "TextView", demo, 0, 200, 300, 700, null, "TextView without an ID", 0)
    val button = ViewNode(6, "Button", demo, 30, 400, 300, 100, buttonId, "OK", 0)
    val singleTextView = ViewNode(1, "TextView", designText, 0, 0, 400, 50, null, "Tab3", 0)
    val textColor = InspectorPropertyItem(
      ANDROID_URI, ATTR_TEXT_COLOR, ATTR_TEXT_COLOR, Type.COLOR, "", PropertySection.DECLARED, demo, title, resourceLookup)
    val background = InspectorPropertyItem(
      ANDROID_URI, ATTR_BACKGROUND, ATTR_BACKGROUND, Type.DRAWABLE, "", PropertySection.DECLARED, demo, title, resourceLookup)
    val backgroundTint = InspectorPropertyItem(
      ANDROID_URI, ATTR_BACKGROUND_TINT, ATTR_BACKGROUND_TINT, Type.DRAWABLE, "", PropertySection.DECLARED, demo, title, resourceLookup)
    val drawableLeft = InspectorPropertyItem(
      ANDROID_URI, ATTR_DRAWABLE_LEFT, ATTR_DRAWABLE_LEFT, Type.DRAWABLE, "", PropertySection.DECLARED, demo, title, resourceLookup)
    val drawableRight = InspectorPropertyItem(
      ANDROID_URI, ATTR_DRAWABLE_RIGHT, ATTR_DRAWABLE_RIGHT, Type.DRAWABLE, "", PropertySection.DECLARED, demo, title, resourceLookup)
    val text1 = InspectorPropertyItem(
      ANDROID_URI, ATTR_TEXT, ATTR_TEXT, Type.STRING, "", PropertySection.DECLARED, demo, textView1, resourceLookup)
    val text2 = InspectorPropertyItem(
      ANDROID_URI, ATTR_TEXT, ATTR_TEXT, Type.STRING, "", PropertySection.DECLARED, demo, textView2, resourceLookup)
    val text3 = InspectorPropertyItem(
      ANDROID_URI, ATTR_TEXT, ATTR_TEXT, Type.STRING, "", PropertySection.DECLARED, designText, singleTextView, resourceLookup)

    init {
      setChildren(relativeLayout, title, textView1, frameLayout)
      setChildren(frameLayout, textView2)
    }

    private fun setChildren(parent: ViewNode, vararg views: ViewNode) {
      views.forEach {
        it.parent = parent
        parent.children.add(it)
      }
    }
  }
}
