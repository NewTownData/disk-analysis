/*
 * Copyright 2021 Voyta Krizek, https://github.com/NewTownData
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.newtowndata.disk.core;

import com.newtowndata.disk.core.entity.PathInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class PathUtils {

  private PathUtils() {}

  public static PathInfo createPathInfo(Path currentPath, BasicFileAttributes attributes)
      throws IOException {
    Path parent = currentPath.getParent();

    List<String> children;
    String fileType;
    if (attributes.isSymbolicLink()) {
      fileType = PathConstants.FILE_TYPE_SYMLINK;
      children = Collections.emptyList();
    } else if (attributes.isDirectory()) {
      fileType = PathConstants.FILE_TYPE_DIRECTORY;
      try (Stream<Path> files = Files.list(currentPath)) {
        children = files.map(p -> p.toString()).toList();
      }
    } else {
      fileType = PathConstants.FILE_TYPE_FILE;
      children = Collections.emptyList();
    }

    return new PathInfo(parent == null ? null : parent.toString(), fileType,
        currentPath.getFileName() == null ? currentPath.toString()
            : currentPath.getFileName().toString(),
        attributes.lastModifiedTime().toMillis(), attributes.size(), children);
  }

  public static PathInfo createEmptyPathInfo(Path currentPath, String fileType) {
    Path parent = currentPath.getParent();

    return new PathInfo(parent == null ? null : parent.toString(), fileType,
        currentPath.getFileName() == null ? currentPath.toString()
            : currentPath.getFileName().toString(),
        0L, 0L, Collections.emptyList());
  }

}
