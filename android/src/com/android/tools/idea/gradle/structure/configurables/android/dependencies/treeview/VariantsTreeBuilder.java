/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.gradle.structure.configurables.android.dependencies.treeview;

import com.android.tools.idea.gradle.structure.configurables.ui.PsdUISettings;
import com.android.tools.idea.gradle.structure.configurables.ui.treeview.AbstractPsdNode;
import com.android.tools.idea.gradle.structure.model.android.PsdAndroidDependencyModel;
import com.android.tools.idea.gradle.structure.model.android.PsdAndroidModuleModel;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import static com.intellij.util.ui.tree.TreeUtil.collapseAll;

public class VariantsTreeBuilder extends AbstractTreeBuilder {
  private static final TreePath[] EMPTY_TREE_PATH = new TreePath[0];

  @NotNull private final DependencySelection myDependencySelectionSource;
  @NotNull private final DependencySelection myDependencySelectionDestination;

  public VariantsTreeBuilder(@NotNull PsdAndroidModuleModel moduleModel,
                             @NotNull final JTree tree,
                             @NotNull DefaultTreeModel treeModel,
                             @NotNull DependencySelection dependencySelectionSource,
                             @NotNull DependencySelection dependencySelectionDestination) {
    super(tree, treeModel, new VariantsTreeStructure(moduleModel), IndexComparator.INSTANCE);
    myDependencySelectionSource = dependencySelectionSource;
    myDependencySelectionDestination = dependencySelectionDestination;

    PsdUISettings.ChangeListener changeListener = new PsdUISettings.ChangeListener() {
      @Override
      public void settingsChanged(@NotNull PsdUISettings settings) {
        AbstractTreeStructure treeStructure = getTreeStructure();

        if (treeStructure instanceof VariantsTreeStructure) {
          final PsdAndroidDependencyModel selected = myDependencySelectionSource.getSelection();

          boolean needsUpdate = ((VariantsTreeStructure)treeStructure).settingsChanged();

          if (needsUpdate) {
            ActionCallback actionCallback = VariantsTreeBuilder.this.queueUpdate();
            actionCallback.doWhenDone(new Runnable() {
              @Override
              public void run() {
                if (selected != null) {
                  myDependencySelectionDestination.setSelection(selected);
                }
              }
            });
          }
        }
      }
    };
    PsdUISettings.getInstance().addListener(changeListener, this);
  }

  @Override
  protected boolean isAutoExpandNode(NodeDescriptor nodeDescriptor) {
    if (nodeDescriptor instanceof AbstractPsdNode) {
      return ((AbstractPsdNode)nodeDescriptor).isAutoExpandNode();
    }
    return super.isAutoExpandNode(nodeDescriptor);
  }

  @Override
  protected boolean isSmartExpand() {
    return true;
  }

  public void expand() {
    JTree tree = getTree();
    if (tree != null) {
      TreeUtil.expandAll(tree);
      getReady(this).doWhenDone(new Runnable() {
        @Override
        public void run() {
          PsdAndroidDependencyModel selection = myDependencySelectionSource.getSelection();
          if (selection != null) {
            myDependencySelectionDestination.setSelection(selection);
          }
        }
      });
    }
  }

  public void collapse() {
    JTree tree = getTree();
    if (tree != null) {
      collapseAll(tree, 1);
      tree.setSelectionPaths(EMPTY_TREE_PATH);
    }
  }
}
