/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.tools.idea.sqlite.utils

import com.android.tools.idea.concurrency.FutureCallbackExecutor
import com.android.tools.idea.sqlite.databaseConnection.DatabaseConnection
import com.android.tools.idea.sqlite.databaseConnection.jdbc.JdbcDatabaseConnection
import com.android.tools.idea.sqlite.model.SqliteValue
import com.google.common.util.concurrent.ListenableFuture
import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.VirtualFile
import java.sql.DriverManager

internal fun List<Any?>.toSqliteValues() = this.map { SqliteValue.fromAny(it) }

internal fun getJdbcDatabaseConnection(
  rootDisposable: Disposable,
  sqliteFile: VirtualFile,
  executor: FutureCallbackExecutor
): ListenableFuture<DatabaseConnection> {
  return executor.executeAsync {
    val url = "jdbc:sqlite:${sqliteFile.path}"
    val connection = DriverManager.getConnection(url)
    JdbcDatabaseConnection(rootDisposable, connection, sqliteFile, executor)
  }
}