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
package com.android.tools.idea.gradle.project.sync.setup.module.dependency;

import com.google.common.collect.*;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static com.android.tools.idea.gradle.project.sync.setup.module.dependency.Dependency.SUPPORTED_SCOPES;

/**
 * Collection of an IDEA module's dependencies.
 */
public class DependencySet {
  @NotNull public static final DependencySet EMPTY = new DependencySet();

  // Use linked list to maintain insertion order.
  private final Multimap<String, LibraryDependency> myLibrariesByName = LinkedListMultimap.create();
  private final Map<Module, ModuleDependency> myModuleDependenciesByModule = Maps.newLinkedHashMap();

  DependencySet() {
  }

  /**
   * Adds the given dependency to this collection. If this collection already has a dependency under the same name and artifacts, the
   * dependency with the wider scope is stored: {@link com.intellij.openapi.roots.DependencyScope#COMPILE} has wider scope than
   * {@link com.intellij.openapi.roots.DependencyScope#TEST}.
   * <p/>
   * It is not uncommon that the Android Gradle plug-in lists the same dependency as explicitly having both "compile" and "test" scopes. In
   * IDEA there is no such distinction, a dependency with "compile" scope is also available to test code.
   *
   * @param dependency the dependency to add.
   */
  void add(@NotNull LibraryDependency dependency) {
    String originalName = dependency.getName();
    Collection<LibraryDependency> allStored = myLibrariesByName.get(originalName);
    allStored = allStored == null ? null : ImmutableSet.copyOf(allStored);
    if (allStored == null || allStored.isEmpty()) {
      myLibrariesByName.put(originalName, dependency);
      return;
    }

    LibraryDependency toAdd = dependency;
    LibraryDependency replaced = null;

    for (LibraryDependency stored : allStored) {
      if (areSameArtifact(dependency, stored)) {
        toAdd = null;
        if (hasHigherScope(dependency, stored)) {
          // replace the existing one if the new one has higher scope. (e.g. "compile" scope is higher than "test" scope.)
          replaced = stored;
          dependency.setName(stored.getName());
          myLibrariesByName.put(originalName, dependency);
        }
        break;
      }
    }

    if (replaced != null) {
      myLibrariesByName.remove(originalName, replaced);
    }

    if (toAdd != null) {
      String newName = dependency.getName() + "_" + allStored.size();
      dependency.setName(newName);
      myLibrariesByName.put(originalName, dependency);
    }
  }

  /**
   * Adds all the dependencies in other DependencySet to this
   *
   * @param other DependencySet to be added to this
   */
  public void addAll(DependencySet other) {
    for (LibraryDependency libraryDependency : other.onLibraries()) {
      add(libraryDependency);
    }
    for (ModuleDependency moduleDependency : other.onModules()) {
      add(moduleDependency);
    }
  }

  private static boolean areSameArtifact(@NotNull LibraryDependency d1, @NotNull LibraryDependency d2) {
    return Arrays.equals(d1.getBinaryPaths(), d2.getBinaryPaths());
  }

  /**
   * Adds the given dependency to this collection. If this collection already has a dependency under the same name, the dependency with the
   * wider scope is stored: {@link com.intellij.openapi.roots.DependencyScope#COMPILE} has wider scope than
   * {@link com.intellij.openapi.roots.DependencyScope#TEST}.
   * <p>
   * It is not uncommon that the Android Gradle plug-in lists the same dependency as explicitly having both "compile" and "test" scopes. In
   * IDEA there is no such distinction, a dependency with "compile" scope is also available to test code.
   *
   * @param dependency the dependency to add.
   */
  void add(@NotNull ModuleDependency dependency) {
    Module module = dependency.getModule();
    Dependency storedDependency = myModuleDependenciesByModule.get(module);
    if (storedDependency == null || hasHigherScope(dependency, storedDependency)) {
      myModuleDependenciesByModule.put(module, dependency);
    }
  }

  private static <T extends Dependency> boolean hasHigherScope(T d1, T d2) {
    return SUPPORTED_SCOPES.indexOf(d1.getScope()) < SUPPORTED_SCOPES.indexOf(d2.getScope());
  }

  @NotNull
  public ImmutableCollection<LibraryDependency> onLibraries() {
    return ImmutableList.copyOf(myLibrariesByName.values());
  }

  @NotNull
  public ImmutableCollection<ModuleDependency> onModules() {
    return ImmutableList.copyOf(myModuleDependenciesByModule.values());
  }
}
