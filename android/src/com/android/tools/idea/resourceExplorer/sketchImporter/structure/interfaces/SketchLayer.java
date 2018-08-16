/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.resourceExplorer.sketchImporter.structure.interfaces;

import com.android.tools.idea.resourceExplorer.sketchImporter.structure.DrawableShape;
import com.android.tools.idea.resourceExplorer.sketchImporter.structure.SketchExportOptions;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Holds the fields that are shared between all types of layers in the Sketch JSON file.
 */
public abstract class SketchLayer {

  public static final int BOOLEAN_OPERATION_UNION = 0;
  public static final int BOOLEAN_OPERATION_SUBSTRACTION = 1;
  public static final int BOOLEAN_OPERATION_INTERSECTION = 2;
  public static final int BOOLEAN_OPERATION_DIFFERENCE = 3;

  @SerializedName("_class")
  private final String classType;
  @SerializedName("do_objectID")
  private final String objectId;
  private final int booleanOperation;
  private final SketchExportOptions exportOptions;
  private final Rectangle.Double frame;
  private final boolean isFlippedHorizontal;
  private final boolean isFlippedVertical;
  private final boolean isVisible;
  private final String name;
  /**
   * Rotation in degrees (counter-clockwise) ∈ [0, 359]
   * [0, 359] is sometimes equivalent to [0, 180] ∪ [-179, -1] with no apparent rule
   */
  private final int rotation;
  private final boolean shouldBreakMaskChain;

  public SketchLayer(@NotNull String classType,
                     @NotNull String objectId,
                     int booleanOperation,
                     @NotNull SketchExportOptions exportOptions,
                     @NotNull Rectangle.Double frame,
                     boolean isFlippedHorizontal,
                     boolean isFlippedVertical,
                     boolean isVisible,
                     @NotNull String name,
                     int rotation,
                     boolean shouldBreakMaskChain) {
    this.classType = classType;
    this.objectId = objectId;
    this.booleanOperation = booleanOperation;
    this.exportOptions = exportOptions;
    this.frame = frame;
    this.isFlippedHorizontal = isFlippedHorizontal;
    this.isFlippedVertical = isFlippedVertical;
    this.isVisible = isVisible;
    this.name = name;
    this.rotation = rotation;
    this.shouldBreakMaskChain = shouldBreakMaskChain;
  }

  @NotNull
  public String getClassType() {
    return classType;
  }

  @NotNull
  public String getObjectId() {
    return objectId;
  }

  public int getBooleanOperation() {
    return booleanOperation;
  }

  @NotNull
  public SketchExportOptions getExportOptions() {
    return exportOptions;
  }

  @NotNull
  public Rectangle.Double getFrame() {
    return frame;
  }

  public boolean isFlippedHorizontal() {
    return isFlippedHorizontal;
  }

  public boolean isFlippedVertical() {
    return isFlippedVertical;
  }

  public boolean isVisible() {
    return isVisible;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public int getRotation() {
    return rotation;
  }

  public boolean shouldBreakMaskChain() {
    return shouldBreakMaskChain;
  }

  @NotNull
  public List<DrawableShape> getTranslatedShapes(@NotNull Point2D.Double parentCoords) {
    return ImmutableList.of();
  }
}

